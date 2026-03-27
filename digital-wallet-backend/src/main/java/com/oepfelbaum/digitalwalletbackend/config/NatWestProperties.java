package com.oepfelbaum.digitalwalletbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "natwest")
public record NatWestProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String psuUsername,
        String tokenUrl,
        String authorizeUrl,
        String resourceUrl
) {}
