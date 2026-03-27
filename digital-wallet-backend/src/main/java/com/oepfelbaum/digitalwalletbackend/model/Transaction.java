package com.oepfelbaum.digitalwalletbackend.model;

public record Transaction(
        String id,
        String date,
        String description,
        double amount,
        String type
) {}
