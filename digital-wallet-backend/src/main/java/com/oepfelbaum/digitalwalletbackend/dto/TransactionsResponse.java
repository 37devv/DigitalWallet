package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionsResponse(@JsonProperty("Data") TransactionsData data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionsData(@JsonProperty("Transaction") List<NatWestTransaction> transactions) {}
}
