package ee.digit25.detector.domain.transaction.feature;

import ee.digit25.detector.domain.transaction.common.Transaction;
import ee.digit25.detector.domain.transaction.common.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistTransactionFeature {

    private final TransactionRepository repository;

    public Transaction save(Transaction transaction) {
        log.debug("Saving transaction: {}", transaction);

        return repository.save(transaction);
    }
}
