package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestAccount(
        @JsonProperty("AccountId") String accountId,
        @JsonProperty("AccountSubType") String accountSubType,
        @JsonProperty("Nickname") String nickname,
        @JsonProperty("Account") List<AccountScheme> accountSchemes
) {}
