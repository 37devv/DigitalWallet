package com.oepfelbaum.digitalwalletbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oepfelbaum.digitalwalletbackend.config.NatWestProperties;
import com.oepfelbaum.digitalwalletbackend.dto.*;
import com.oepfelbaum.digitalwalletbackend.model.Account;
import com.oepfelbaum.digitalwalletbackend.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NatWestService {

    private static final Logger log = LoggerFactory.getLogger(NatWestService.class);

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String BANK_NAME = "NatWest";
    private static final String BANK_LOGO = "NW";
    private static final String OAUTH_STATE = "ABC";
    private static final String NATWEST_AUTH_MODE = "AUTO_POSTMAN";
    private static final String CREDIT_DEBIT_INDICATOR_DEBIT = "Debit";
    private static final String BALANCE_TYPE_CLOSING_BOOKED = "ClosingBooked";
    private static final String BALANCE_TYPE_INTERIM_AVAILABLE = "InterimAvailable";
    private static final String TRANSACTION_TYPE_DEBIT = "debit";
    private static final String TRANSACTION_TYPE_CREDIT = "credit";

    private final NatWestProperties props;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.MIN;

    public NatWestService(NatWestProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    // ── Token management ─────────────────────────────────────────────────────

    private synchronized String getAccessToken() {
        // Refresh if missing or within 60 seconds of expiry
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
                        // AUTO_POSTMAN mode: NatWest returns JSON { "redirectUri": "...#code=..." }
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

    // ── API calls ─────────────────────────────────────────────────────────────

    public List<Account> getAccounts() {
        String token = getAccessToken();

        URI uri = UriComponentsBuilder.fromUriString(props.resourceUrl())
                .pathSegment("accounts")
                .build().toUri();

        AccountsResponse response = restClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(AccountsResponse.class);

        return response.data().accounts().stream()
                .map(a -> {
                    log.info("Account id={} subType={} nickname={}", a.accountId(), a.accountSubType(), a.nickname());
                    double balance = fetchBalance(token, a.accountId());
                    String type = formatAccountType(a.accountSubType());
                    String ownerName = (a.accountSchemes() != null && !a.accountSchemes().isEmpty())
                            ? a.accountSchemes().get(0).name() : "";
                    return new Account(a.accountId(), BANK_NAME, type, balance, BANK_LOGO, ownerName);
                })
                .collect(Collectors.toList());
    }

    private double fetchBalance(String token, String accountId) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(props.resourceUrl())
                    .pathSegment("accounts", accountId, "balances")
                    .build().toUri();

            BalancesResponse response = restClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(BalancesResponse.class);

            List<NatWestBalance> balances = response.data().balances();
            if (balances == null || balances.isEmpty()) {
                log.warn("No balances returned for account {}", accountId);
                return 0.0;
            }

            // Log the available balance types so we can see what NatWest returns
            balances.forEach(b -> log.info("Account {} balance type={} indicator={} amount={}",
                    accountId, b.type(), b.creditDebitIndicator(), b.amount().amount()));

            // Prefer ClosingBooked (matches the raw balance stored in sandbox), fall back to InterimAvailable, then first
            NatWestBalance picked = balances.stream()
                    .filter(b -> BALANCE_TYPE_CLOSING_BOOKED.equals(b.type()))
                    .findFirst()
                    .orElseGet(() -> balances.stream()
                            .filter(b -> BALANCE_TYPE_INTERIM_AVAILABLE.equals(b.type()))
                            .findFirst()
                            .orElse(balances.get(0)));

            double amount = Double.parseDouble(picked.amount().amount());
            double signed = CREDIT_DEBIT_INDICATOR_DEBIT.equals(picked.creditDebitIndicator()) ? -amount : amount;
            return roundToFiveCents(signed);
        } catch (Exception e) {
            log.error("Failed to fetch balance for account {}: {}", accountId, e.getMessage());
            return 0.0;
        }
    }

    public List<Transaction> getTransactions(String accountId) {
        String token = getAccessToken();

        // Only fetch the last 30 days of transactions
        String fromDate = Instant.now().minus(30, ChronoUnit.DAYS).toString();

        URI uri = UriComponentsBuilder.fromUriString(props.resourceUrl())
                .pathSegment("accounts", accountId, "transactions")
                .queryParam("fromBookingDateTime", fromDate)
                .build().toUri();

        TransactionsResponse response = restClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(TransactionsResponse.class);

        List<NatWestTransaction> txList = response.data().transactions();
        if (txList == null) return List.of();

        return txList.stream()
                .map(tx -> {
                    double amount = Double.parseDouble(tx.amount().amount());
                    boolean isDebit = CREDIT_DEBIT_INDICATOR_DEBIT.equals(tx.creditDebitIndicator());
                    double signedAmount = roundToFiveCents(isDebit ? -amount : amount);
                    String type = isDebit ? TRANSACTION_TYPE_DEBIT : TRANSACTION_TYPE_CREDIT;
                    String date = formatDate(tx.bookingDateTime());
                    String description = tx.transactionInformation() != null ? tx.transactionInformation() : "-";
                    return new Transaction(tx.transactionId(), date, description, signedAmount, type);
                })
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Rounds to the nearest 0.05.
     * Works correctly for negative amounts (e.g. -979.10 stays -979.10).
     */
    private double roundToFiveCents(double amount) {
        return Math.round(amount * 20.0) / 20.0;
    }

    private String formatAccountType(String subType) {
        if (subType == null) return "Konto";
        return switch (subType) {
            case "CurrentAccount" -> "Privatkonto";
            case "Savings" -> "Sparkonto";
            case "CreditCard" -> "Kreditkarte";
            case "Mortgage" -> "Hypothek";
            default -> subType;
        };
    }

    private String formatDate(String dateTime) {
        if (dateTime == null) return "";
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
            return zdt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return dateTime;
        }
    }

}
