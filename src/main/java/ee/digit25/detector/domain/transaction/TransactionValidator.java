package ee.digit25.detector.domain.transaction;

import ee.digit25.detector.domain.account.AccountValidator;
import ee.digit25.detector.domain.device.DeviceValidator;
import ee.digit25.detector.domain.person.PersonValidator;
import ee.digit25.detector.domain.transaction.common.Transaction;
import ee.digit25.detector.domain.transaction.external.api.TransactionModel;
import ee.digit25.detector.domain.transaction.feature.FindTransactionsFeature;
import lombok.RequiredArgsConstructor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionValidator {

    private final PersonValidator personValidator;
    private final DeviceValidator deviceValidator;
    private final AccountValidator accountValidator;
    private final FindTransactionsFeature findTransactionsFeature;

    public boolean isLegitimate(TransactionModel transaction) {
    // TODO: Optimize to minimize DB requests
    boolean isLegitimate = true;

    isLegitimate &= personValidator.isValid(transaction.getRecipient());
    isLegitimate &= personValidator.isValid(transaction.getSender());
    isLegitimate &= deviceValidator.isValid(transaction.getDeviceMac());
    isLegitimate &= accountValidator.isValidSenderAccount(transaction.getSenderAccount(), transaction.getAmount(), transaction.getSender());
    isLegitimate &= accountValidator.isValidRecipientAccount(transaction.getRecipientAccount(), transaction.getRecipient());

    // ⬇️ OPTIMEERITUD OSA
    var senderTransactions = findTransactionsFeature.bySender(transaction.getSender());
    isLegitimate &= validateNoBurstTransaction(transaction, senderTransactions);
    isLegitimate &= validateNoMultideviceTransactions(transaction, senderTransactions);
    isLegitimate &= validateValidHistory(transaction, senderTransactions);
    // ⬆️ ainult 1 DB-päring
    return isLegitimate;
}

    private boolean validateNoBurstTransaction(TransactionModel transaction, List<Transaction> history) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(30);

        long transactionCountSince = findTransactionsFeature.bySender(transaction.getSender())
                .stream()
                .filter(t -> t.getTimestamp().isAfter(since))
                .count();

        return countBelowThreshold(transactionCountSince, 10);
    }

    private boolean validateNoMultideviceTransactions(TransactionModel transaction, List<Transaction> history) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(10);

        long differentDeviceCountSince = findTransactionsFeature.bySender(transaction.getSender())
                .stream()
                .filter(t -> t.getTimestamp().isAfter(since))
                .map(t -> t.getDevice().getMac())
                .distinct()
                .count();

        return countBelowThreshold(differentDeviceCountSince, 2);
    }

    private boolean validateValidHistory(TransactionModel transaction, List<Transaction> history) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(1);

        return findTransactionsFeature.bySender(transaction.getSender())
                .stream()
                .filter(t -> t.getTimestamp().isAfter(since))
                .allMatch(Transaction::isLegitimate);
    }

    private boolean countBelowThreshold(long count, int threshold) {
        return count < threshold;
    }
}
