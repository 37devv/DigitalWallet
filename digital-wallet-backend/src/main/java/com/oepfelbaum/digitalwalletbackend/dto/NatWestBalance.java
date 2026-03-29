package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestBalance(
        @JsonProperty("Amount") MoneyAmount amount,
        @JsonProperty("CreditDebitIndicator") String creditDebitIndicator,
        @JsonProperty("Type") String type
) {}
