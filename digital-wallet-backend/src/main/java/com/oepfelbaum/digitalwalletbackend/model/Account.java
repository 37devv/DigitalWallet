package com.oepfelbaum.digitalwalletbackend.model;

public record Account(
        String id,
        String bank,
        String type,
        double balance,
        String logo,
        String ownerName
) {}
