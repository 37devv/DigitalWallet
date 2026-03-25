package com.oepfelbaum.digitalwalletbackend.service;

import com.oepfelbaum.digitalwalletbackend.model.Account;
import com.oepfelbaum.digitalwalletbackend.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private static final List<Account> ACCOUNTS = List.of(
            new Account(1, "UBS", "Privatkonto", 11117.60, "UBS", List.of(
                    new Transaction(1, "14.12.2021", "Salärzahlung", 8225.45, "einkommen"),
                    new Transaction(2, "09.12.2021", "Twint +41 79 **** 09", 67.00, "einkommen"),
                    new Transaction(3, "03.12.2021", "Einzahlung bar", 950.00, "einkommen"),
                    new Transaction(4, "13.12.2021", "Migros", -84.30, "ausgaben"),
                    new Transaction(5, "11.12.2021", "SBB Ticket", -52.00, "ausgaben"),
                    new Transaction(6, "08.12.2021", "Netflix", -13.90, "ausgaben"),
                    new Transaction(7, "06.12.2021", "Coop", -67.45, "ausgaben"),
                    new Transaction(8, "01.12.2021", "Miete Dezember", -1850.00, "ausgaben")
            )),
            new Account(2, "Schaffhauser Kantonalbank", "Privatkonto", 6127.45, "SKB", List.of(
                    new Transaction(1, "12.12.2021", "Überweisung UBS", 500.00, "einkommen"),
                    new Transaction(2, "05.12.2021", "Zinsgutschrift", 12.45, "einkommen"),
                    new Transaction(3, "10.12.2021", "Versicherung AXA", -234.00, "ausgaben"),
                    new Transaction(4, "02.12.2021", "Krankenkasse", -412.50, "ausgaben")
            )),
            new Account(3, "Credit Suisse", "Sparkonto", 4000.00, "CS", List.of(
                    new Transaction(1, "01.12.2021", "Einzahlung", 500.00, "einkommen"),
                    new Transaction(2, "15.11.2021", "Zinsgutschrift", 8.00, "einkommen"),
                    new Transaction(3, "07.12.2021", "Abbuchung", -150.00, "ausgaben")
            )),
            new Account(4, "Raiffeisen", "Privatkonto", -979.10, "RF", List.of(
                    new Transaction(1, "14.12.2021", "Kontokorrentkredit", -979.10, "ausgaben"),
                    new Transaction(2, "10.12.2021", "Bargeldbezug", -200.00, "ausgaben"),
                    new Transaction(3, "04.12.2021", "Zahlung Restaurant", -89.00, "ausgaben"),
                    new Transaction(4, "01.12.2021", "Gutschrift", 300.00, "einkommen")
            )),
            new Account(5, "Raiffeisen", "Sparkonto", 3000.00, "RF", List.of(
                    new Transaction(1, "01.12.2021", "Monatliche Einlage", 300.00, "einkommen"),
                    new Transaction(2, "15.11.2021", "Zinsgutschrift", 5.50, "einkommen")
            ))
    );

    public List<Account> getAllAccounts() {
        return ACCOUNTS;
    }

    public Optional<Account> getAccountById(int id) {
        return ACCOUNTS.stream().filter(a -> a.id() == id).findFirst();
    }

    public Optional<List<Transaction>> getTransactionsByAccountId(int id) {
        return getAccountById(id).map(Account::transactions);
    }
}
