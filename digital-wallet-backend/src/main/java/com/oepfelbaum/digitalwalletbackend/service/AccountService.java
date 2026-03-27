package com.oepfelbaum.digitalwalletbackend.service;

import com.oepfelbaum.digitalwalletbackend.model.Account;
import com.oepfelbaum.digitalwalletbackend.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final NatWestService natWestService;

    public AccountService(NatWestService natWestService) {
        this.natWestService = natWestService;
    }

    public List<Account> getAllAccounts() {
        return natWestService.getAccounts();
    }

    public Optional<Account> getAccountById(String id) {
        return natWestService.getAccounts().stream()
                .filter(a -> a.id().equals(id))
                .findFirst();
    }

    public Optional<List<Transaction>> getTransactionsByAccountId(String id) {
        return getAccountById(id)
                .map(a -> natWestService.getTransactions(id));
    }
}
