package ee.digit25.detector.domain.account;

import ee.digit25.detector.domain.account.external.AccountRequester;
import ee.digit25.detector.domain.account.external.api.AccountModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountValidator {

    private final AccountRequester requester;
    // TODO: Implement cache invalidation strategy
    private final Map<String, AccountModel> cache = new ConcurrentHashMap<>();

    public boolean isValidSenderAccount(String accountNumber, BigDecimal amount, String senderPersonCode) {
        log.info("Checking if account {} is valid sender account", accountNumber);
        boolean isValid = true;

        // TODO: Optimize to avoid multiple requests
        AccountModel account = getAccount(accountNumber);

        isValid &= !account.getClosed();
        isValid &= senderPersonCode.equals(account.getOwner());
        isValid &= account.getBalance().compareTo(amount) >= 0;

        return isValid;
    }

    public boolean isValidRecipientAccount(String accountNumber, String recipientPersonCode) {
        log.info("Checking if account {} is valid recipient account", accountNumber);
        boolean isValid = true;

        // TODO: Optimize to avoid multiple requests
        AccountModel account = getAccount(accountNumber);

        isValid &= !account.getClosed();
        isValid &= recipientPersonCode.equals(account.getOwner());

        return isValid;
    }

    // TODO: Optimize to avoid multiple requests for the same account in a single processing batch
    private AccountModel getAccount(String accountNumber) {
        return cache.computeIfAbsent(accountNumber, requester::get);
    }

    private boolean isOwner(String accountNumber, String senderPersonCode) {
        log.info("Checking if {} is owner of account {}", senderPersonCode, accountNumber);

        return senderPersonCode.equals(requester.get(accountNumber).getOwner());
    }

    private boolean hasBalance(String accountNumber, BigDecimal amount) {
        log.info("Checking if account {} has balance for amount {}", accountNumber, amount);

        return requester.get(accountNumber).getBalance().compareTo(amount) >= 0;
    }

    private boolean isClosed(String accountNumber) {
        log.info("Checking if account {} is closed", accountNumber);

        return requester.get(accountNumber).getClosed();
    }
}
