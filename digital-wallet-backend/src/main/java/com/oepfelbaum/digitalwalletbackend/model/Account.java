package com.oepfelbaum.digitalwalletbackend.model;

import java.util.List;

public record Account(
        int id,
        String bank,
        String type,
        double balance,
        String logo,
        List<Transaction> transactions
) {}
