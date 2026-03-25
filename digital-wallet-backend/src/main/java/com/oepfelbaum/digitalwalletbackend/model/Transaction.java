package com.oepfelbaum.digitalwalletbackend.model;

public record Transaction(
        int id,
        String date,
        String description,
        double amount,
        String type
) {}
