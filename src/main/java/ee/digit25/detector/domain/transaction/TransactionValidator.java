package ee.digit25.detector.domain.transaction;

import ee.digit25.detector.domain.account.AccountValidator;
import ee.digit25.detector.domain.device.DeviceValidator;
import ee.digit25.detector.domain.person.PersonValidator;
import ee.digit25.detector.domain.transaction.common.Transaction;
import ee.digit25.detector.domain.transaction.external.api.TransactionModel;
import ee.digit25.detector.domain.transaction.feature.FindTransactionsFeature;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionValidator {

    private final PersonValidator personValidator;
    private final DeviceValidator deviceValidator;
    private final AccountValidator accountValidator;
    private final FindTransactionsFeature findTransactionsFeature;

    private List<Predicate<TransactionModel>> validators;

    private List<Predicate<List<Transaction>>> senderTransactionValidators;

    @PostConstruct
    void setup() {

        validators = List.of(
                t -> personValidator.isValid(t.getRecipient()),
                t -> personValidator.isValid(t.getSender()),
                t -> deviceValidator.isValid(t.getDeviceMac()),
                t -> accountValidator.isValidSenderAccount(t.getSenderAccount(), t.getAmount(), t.getSender()),
                t -> accountValidator.isValidRecipientAccount(t.getRecipientAccount(), t.getRecipient())
        );

        senderTransactionValidators = List.of(
                this::validateNoBurstTransaction,
                this::validateNoMultideviceTransactions,
                this::validateValidHistory
        );
    }

    public boolean isLegitimate(TransactionModel transaction) {
        var start = System.currentTimeMillis();

        for (Predicate<TransactionModel> validator : validators) {
            if (!validator.test(transaction)) return false;
        }

        log.info("After first batch of validators: {}ms", System.currentTimeMillis() - start);

        List<Transaction> transactionsBySender = findTransactionsFeature.bySender(transaction.getSender());

        log.info("After get transactions. {}ms", System.currentTimeMillis() - start);

        for (Predicate<List<Transaction>> validator : senderTransactionValidators) {
            if (!validator.test(transactionsBySender)) return false;
        }

        log.info("After second batch of validators: {}ms", System.currentTimeMillis() - start);

        return true;
    }

    private boolean validateNoBurstTransaction(List<Transaction> transactionsBySender) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(30);

        long transactionCountSince = transactionsBySender
                .stream()
                .filter(t -> t.getTimestamp().isAfter(since))
                .count();

        return countBelowThreshold(transactionCountSince, 10);
    }

    private boolean validateNoMultideviceTransactions(List<Transaction> transaction) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(10);

        long differentDeviceCountSince = transaction
                .stream()
                .filter(t -> t.getTimestamp().isAfter(since))
                .map(t -> t.getDevice().getMac())
                .distinct()
                .count();

        return countBelowThreshold(differentDeviceCountSince, 2);
    }

    private boolean validateValidHistory(List<Transaction> transaction) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(1);

        return transaction
                .stream()
                .filter(t -> t.getTimestamp().isAfter(since))
                .allMatch(Transaction::isLegitimate);
    }

    private boolean countBelowThreshold(long count, int threshold) {
        return count < threshold;
    }
}
