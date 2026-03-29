package com.oepfelbaum.digitalwalletbackend.service;

import com.oepfelbaum.digitalwalletbackend.dto.NatWestAccount;
import com.oepfelbaum.digitalwalletbackend.dto.NatWestTransaction;
import com.oepfelbaum.digitalwalletbackend.model.Account;
import com.oepfelbaum.digitalwalletbackend.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NatWestMapper {

    private static final String BANK_NAME = "NatWest";
    private static final String BANK_LOGO = "NW";
    private static final String CREDIT_DEBIT_INDICATOR_DEBIT = "Debit";
    private static final String TRANSACTION_TYPE_DEBIT = "debit";
    private static final String TRANSACTION_TYPE_CREDIT = "credit";

    public Account toAccount(NatWestAccount a, double balance) {
        String type = formatAccountType(a.accountSubType());
        String ownerName = (a.accountSchemes() != null && !a.accountSchemes().isEmpty())
                ? a.accountSchemes().get(0).name() : "";
        return new Account(a.accountId(), BANK_NAME, type, balance, BANK_LOGO, ownerName);
    }

    public Transaction toTransaction(NatWestTransaction tx) {
        double amount = Double.parseDouble(tx.amount().amount());
        boolean isDebit = CREDIT_DEBIT_INDICATOR_DEBIT.equals(tx.creditDebitIndicator());
        double signedAmount = roundToFiveCents(isDebit ? -amount : amount);
        String type = isDebit ? TRANSACTION_TYPE_DEBIT : TRANSACTION_TYPE_CREDIT;
        String date = formatDate(tx.bookingDateTime());
        String description = tx.transactionInformation() != null ? tx.transactionInformation() : "-";
        return new Transaction(tx.transactionId(), date, description, signedAmount, type);
    }

    public double roundToFiveCents(double amount) {
        return Math.round(amount * 20.0) / 20.0;
    }

    private String formatAccountType(String subType) {
        if (subType == null) return "Konto";
        return switch (subType) {
            case "CurrentAccount" -> "Privatkonto";
            case "Savings" -> "Sparkonto";
            case "CreditCard" -> "Kreditkarte";
            case "Mortgage" -> "Hypothek";
            default -> subType;
        };
    }

    private String formatDate(String dateTime) {
        if (dateTime == null) return "";
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
            return zdt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return dateTime;
        }
    }
}
