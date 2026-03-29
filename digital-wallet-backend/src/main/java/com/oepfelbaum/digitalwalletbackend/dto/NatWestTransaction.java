package com.oepfelbaum.digitalwalletbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestTransaction(
        @JsonProperty("TransactionId") String transactionId,
        @JsonProperty("Amount") MoneyAmount amount,
        @JsonProperty("CreditDebitIndicator") String creditDebitIndicator,
        @JsonProperty("BookingDateTime") String bookingDateTime,
        @JsonProperty("TransactionInformation") String transactionInformation
) {}
