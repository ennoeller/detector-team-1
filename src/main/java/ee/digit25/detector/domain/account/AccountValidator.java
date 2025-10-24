package ee.digit25.detector.domain.account;

import ee.digit25.detector.domain.account.external.AccountRequester;
import ee.digit25.detector.domain.account.external.api.AccountModel;
import ee.digit25.detector.domain.account.external.api.AccountModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountValidator {

    private final AccountRequester requester;

    public boolean isValidSenderAccount(String accountNumber, BigDecimal amount, String senderPersonCode) {
        log.debug("Checking if account {} is valid sender account", accountNumber);
        boolean isValid = true;

        AccountModel account = getAccount(accountNumber);
        if(isClosed(account)) {
            return false;
        }
        if(!isOwner(account, senderPersonCode))
            return false;
        return hasBalance(account, amount);
    }

    public boolean isValidRecipientAccount(String accountNumber, String recipientPersonCode) {
        log.debug("Checking if account {} is valid recipient account", accountNumber);
        boolean isValid = true;
        AccountModel account = getAccount(accountNumber);

        if(isClosed(account)) {
            return false;
        }
        return isOwner(account, recipientPersonCode);
    }

    private AccountModel getAccount(String accountNumber) {
        return requester.getAccounts().get(accountNumber);
    }

    private boolean isOwner(AccountModel account, String senderPersonCode) {
        log.debug("Checking if {} is owner of account {}", senderPersonCode, account.getNumber());

        return senderPersonCode.equals(account.getOwner());
    }

    private boolean hasBalance(AccountModel account, BigDecimal amount) {
        log.debug("Checking if account {} has balance for amount {}", account.getNumber(), amount);

        return account.getBalance().compareTo(amount) >= 0;
    }

    private boolean isClosed(AccountModel account) {
        log.debug("Checking if account {} is closed", account.getNumber());

        return account.getClosed();
    }
}
