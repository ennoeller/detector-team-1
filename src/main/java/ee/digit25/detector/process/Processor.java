package ee.digit25.detector.process;

import ee.digit25.detector.domain.transaction.TransactionValidator;
import ee.digit25.detector.domain.transaction.common.TransactionMapper;
import ee.digit25.detector.domain.transaction.external.TransactionRequester;
import ee.digit25.detector.domain.transaction.external.TransactionVerifier;
import ee.digit25.detector.domain.transaction.external.api.TransactionModel;
import ee.digit25.detector.domain.transaction.feature.PersistTransactionFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
@RequiredArgsConstructor
public class Processor {

    private static final int POOL_SIZE = 4;
    private static final int TRANSACTION_BATCH_SIZE = 10;
    private final TransactionRequester requester;
    private final TransactionValidator validator;
    private final TransactionVerifier verifier;
    private final PersistTransactionFeature persistTransactionFeature;
    private final TransactionMapper transactionMapper;

    private final ForkJoinPool customPool = new ForkJoinPool(POOL_SIZE);

    @Scheduled(fixedDelay = 1000) //Runs every 1000 ms after the last run
    public void process() {
        log.info("Starting to process a batch of transactions of size {}", TRANSACTION_BATCH_SIZE);

        List<TransactionModel> transactions = requester.getUnverified(TRANSACTION_BATCH_SIZE);

        customPool.submit(() ->
                transactions.parallelStream()
                        .forEach(this::process)
        );

        log.info("Finished processing a batch of transactions of size {}", TRANSACTION_BATCH_SIZE);
    }

    private void process(TransactionModel transaction) {
        boolean valid = validator.isLegitimate(transaction);
        if (valid) {
            log.info("Legitimate transaction {}", transaction.getId());
            verifier.verify(transaction);
        } else {
            log.info("Not legitimate transaction {}", transaction.getId());
            verifier.reject(transaction);
        }

        persistTransactionFeature.save(
                transactionMapper.toEntity(
                        transaction,
                        valid
                )
        );
    }
}
