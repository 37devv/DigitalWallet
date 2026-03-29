package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsentResponse(@JsonProperty("Data") ConsentData data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConsentData(@JsonProperty("ConsentId") String consentId) {}
}
