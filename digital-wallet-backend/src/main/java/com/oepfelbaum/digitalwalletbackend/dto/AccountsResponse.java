package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountsResponse(@JsonProperty("Data") AccountsData data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountsData(@JsonProperty("Account") List<NatWestAccount> accounts) {}
}
