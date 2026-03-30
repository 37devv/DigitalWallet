package com.oepfelbaum.digitalwalletbackend.service;

import com.oepfelbaum.digitalwalletbackend.config.NatWestProperties;
import com.oepfelbaum.digitalwalletbackend.dto.AccountsResponse;
import com.oepfelbaum.digitalwalletbackend.dto.BalancesResponse;
import com.oepfelbaum.digitalwalletbackend.dto.NatWestBalance;
import com.oepfelbaum.digitalwalletbackend.dto.TransactionsResponse;
import com.oepfelbaum.digitalwalletbackend.model.Account;
import com.oepfelbaum.digitalwalletbackend.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NatWestService {

    private static final Logger log = LoggerFactory.getLogger(NatWestService.class);

    private static final String BALANCE_TYPE_CLOSING_BOOKED = "ClosingBooked";
    private static final String BALANCE_TYPE_INTERIM_AVAILABLE = "InterimAvailable";
    private static final String CREDIT_DEBIT_INDICATOR_DEBIT = "Debit";

    private final NatWestProperties props;
    private final RestClient restClient;
    private final NatWestTokenManager tokenManager;
    private final NatWestMapper mapper;

    public NatWestService(NatWestProperties props, RestClient restClient,
                          NatWestTokenManager tokenManager, NatWestMapper mapper) {
        this.props = props;
        this.restClient = restClient;
        this.tokenManager = tokenManager;
        this.mapper = mapper;
    }

    // ── API calls ─────────────────────────────────────────────────────────────

    public List<Account> getAccounts() {
        String token = tokenManager.getAccessToken();

        URI uri = UriComponentsBuilder.fromUriString(props.resourceUrl())
                .pathSegment("accounts")
                .build().toUri();

        AccountsResponse response = restClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(AccountsResponse.class);

        return response.data().accounts().stream()
                .map(a -> {
                    log.info("Account id={} subType={} nickname={}", a.accountId(), a.accountSubType(), a.nickname());
                    double balance = fetchBalance(token, a.accountId());
                    return mapper.toAccount(a, balance);
                })
                .collect(Collectors.toList());
    }

    private double fetchBalance(String token, String accountId) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(props.resourceUrl())
                    .pathSegment("accounts", accountId, "balances")
                    .build().toUri();

            BalancesResponse response = restClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(BalancesResponse.class);

            List<NatWestBalance> balances = response.data().balances();
            if (balances == null || balances.isEmpty()) {
                log.warn("No balances returned for account {}", accountId);
                return 0.0;
            }

            balances.forEach(b -> log.info("Account {} balance type={} indicator={} amount={}",
                    accountId, b.type(), b.creditDebitIndicator(), b.amount().amount()));

            // Prefer ClosingBooked, fall back to InterimAvailable, then first
            NatWestBalance picked = balances.stream()
                    .filter(b -> BALANCE_TYPE_CLOSING_BOOKED.equals(b.type()))
                    .findFirst()
                    .orElseGet(() -> balances.stream()
                            .filter(b -> BALANCE_TYPE_INTERIM_AVAILABLE.equals(b.type()))
                            .findFirst()
                            .orElse(balances.get(0)));

            double amount = Double.parseDouble(picked.amount().amount());
            double signed = CREDIT_DEBIT_INDICATOR_DEBIT.equals(picked.creditDebitIndicator()) ? -amount : amount;
            return mapper.roundToFiveCents(signed);
        } catch (Exception e) {
            log.error("Failed to fetch balance for account {}: {}", accountId, e.getMessage());
            return 0.0;
        }
    }

    public List<Transaction> getTransactions(String accountId) {
        String token = tokenManager.getAccessToken();

        // Only fetch the last 30 days of transactions
        String fromDate = Instant.now().minus(30, ChronoUnit.DAYS).toString();

        URI uri = UriComponentsBuilder.fromUriString(props.resourceUrl())
                .pathSegment("accounts", accountId, "transactions")
                .queryParam("fromBookingDateTime", fromDate)
                .build().toUri();

        TransactionsResponse response = restClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(TransactionsResponse.class);

        List<?> txList = response.data().transactions();
        if (txList == null) return List.of();

        return response.data().transactions().stream()
                .map(mapper::toTransaction)
                .collect(Collectors.toList());
    }
}
