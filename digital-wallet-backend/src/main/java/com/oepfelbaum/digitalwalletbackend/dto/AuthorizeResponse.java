package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorizeResponse(String redirectUri) {}
