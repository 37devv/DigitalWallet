package com.oepfelbaum.digitalwalletbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oepfelbaum.digitalwalletbackend.config.NatWestProperties;
import com.oepfelbaum.digitalwalletbackend.dto.AuthorizeResponse;
import com.oepfelbaum.digitalwalletbackend.dto.ConsentResponse;
import com.oepfelbaum.digitalwalletbackend.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NatWestTokenManager {

    private static final Logger log = LoggerFactory.getLogger(NatWestTokenManager.class);

    private static final String OAUTH_STATE = "ABC";
    private static final String NATWEST_AUTH_MODE = "AUTO_POSTMAN";

    private final NatWestProperties props;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.MIN;

    public NatWestTokenManager(NatWestProperties props, RestClient restClient, ObjectMapper objectMapper) {
        this.props = props;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public synchronized String getAccessToken() {
        if (cachedToken == null || Instant.now().isAfter(tokenExpiry.minusSeconds(60))) {
            TokenResponse tokenResponse = performFullOAuthFlow();
            cachedToken = tokenResponse.accessToken();
            tokenExpiry = Instant.now().plusSeconds(tokenResponse.expiresIn());
        }
        return cachedToken;
    }

    private TokenResponse performFullOAuthFlow() {
        String appToken = getClientCredentialsToken();
        String consentId = createConsent(appToken);
        String authCode = authorizeConsent(consentId);
        return exchangeCodeForToken(authCode);
    }

    private String getClientCredentialsToken() {
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", props.clientId());
        body.add("client_secret", props.clientSecret());
        body.add("scope", "accounts");

        TokenResponse response = restClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TokenResponse.class);

        return response.accessToken();
    }

    private String createConsent(String appToken) {
        String body = """
                {
                  "Data": {
                    "Permissions": [
                      "ReadAccountsDetail",
                      "ReadBalances",
                      "ReadTransactionsCredits",
                      "ReadTransactionsDebits",
                      "ReadTransactionsDetail"
                    ]
                  },
                  "Risk": {}
                }
                """;

        URI uri = UriComponentsBuilder.fromUriString(props.resourceUrl())
                .pathSegment("account-access-consents")
                .build().toUri();

        ConsentResponse response = restClient.post()
                .uri(uri)
                .header("Authorization", "Bearer " + appToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(ConsentResponse.class);

        return response.data().consentId();
    }

    private String authorizeConsent(String consentId) {
        URI uri = UriComponentsBuilder.fromUriString(props.authorizeUrl() + "/authorize")
                .queryParam("client_id", props.clientId())
                .queryParam("response_type", "code id_token")
                .queryParam("scope", "openid accounts")
                .queryParam("redirect_uri", props.redirectUri())
                .queryParam("state", OAUTH_STATE)
                .queryParam("request", consentId)
                .queryParam("authorization_mode", NATWEST_AUTH_MODE)
                .queryParam("authorization_username", props.psuUsername())
                .build()
                .encode()
                .toUri();

        log.info("Calling authorize URI: {}", uri);

        return restClient.get()
                .uri(uri)
                .exchange((req, res) -> {
                    int status = res.getStatusCode().value();
                    log.info("Authorize response status: {}", status);
                    log.info("Authorize response headers: {}", res.getHeaders());

                    if (status == 200) {
                        String respBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.info("Authorize 200 body: {}", respBody);
                        AuthorizeResponse authorizeResponse = objectMapper.readValue(respBody, AuthorizeResponse.class);
                        return extractAuthCode(authorizeResponse.redirectUri());
                    } else if (status >= 301 && status <= 303) {
                        String location = res.getHeaders().getFirst("Location");
                        log.info("Authorize {} Location: {}", status, location);
                        if (location == null) {
                            throw new RuntimeException("Redirect with no Location header");
                        }
                        if (location.startsWith(props.redirectUri())) {
                            return extractAuthCode(location);
                        }
                        return followConsentRedirect(location);
                    } else {
                        String respBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        throw new RuntimeException("Unexpected authorize response: HTTP " + status + " — " + respBody);
                    }
                });
    }

    private String followConsentRedirect(String location) {
        log.info("Following consent redirect to: {}", location);
        return restClient.get()
                .uri(URI.create(location))
                .exchange((req, res) -> {
                    int status = res.getStatusCode().value();
                    log.info("Consent redirect response status: {}", status);
                    if (status == 200) {
                        String respBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.info("Consent redirect 200 body: {}", respBody);
                        AuthorizeResponse authorizeResponse = objectMapper.readValue(respBody, AuthorizeResponse.class);
                        return extractAuthCode(authorizeResponse.redirectUri());
                    } else if (status >= 301 && status <= 303) {
                        String nextLocation = res.getHeaders().getFirst("Location");
                        log.info("Consent redirect {} Location: {}", status, nextLocation);
                        return extractAuthCode(nextLocation);
                    } else {
                        String respBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        throw new RuntimeException("Unexpected consent redirect response: HTTP " + status + " — " + respBody);
                    }
                });
    }

    private String extractAuthCode(String uriWithFragment) {
        int hashIdx = uriWithFragment.indexOf('#');
        if (hashIdx < 0) {
            throw new RuntimeException("No '#' fragment in authorize URI: " + uriWithFragment);
        }
        String fragment = uriWithFragment.substring(hashIdx + 1);
        Map<String, String> params = Arrays.stream(fragment.split("&"))
                .map(p -> p.split("=", 2))
                .filter(p -> p.length == 2)
                .collect(Collectors.toMap(p -> p[0], p -> URLDecoder.decode(p[1], StandardCharsets.UTF_8)));

        String code = params.get("code");
        if (code == null) {
            throw new RuntimeException("No 'code' in authorize fragment: " + fragment);
        }
        return code;
    }

    private TokenResponse exchangeCodeForToken(String code) {
        var body = new LinkedMultiValueMap<String, String>();
        body.add("client_id", props.clientId());
        body.add("client_secret", props.clientSecret());
        body.add("redirect_uri", props.redirectUri());
        body.add("grant_type", "authorization_code");
        body.add("code", code);

        TokenResponse response = restClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new RuntimeException("Token exchange returned empty response");
        }
        return response;
    }
}
