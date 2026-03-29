package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BalancesResponse(@JsonProperty("Data") BalancesData data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BalancesData(@JsonProperty("Balance") List<NatWestBalance> balances) {}
}
