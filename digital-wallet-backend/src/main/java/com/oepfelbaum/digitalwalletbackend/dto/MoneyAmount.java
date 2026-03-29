package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MoneyAmount(
        @JsonProperty("Amount") String amount,
        @JsonProperty("Currency") String currency
) {}
