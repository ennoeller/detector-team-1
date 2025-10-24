package ee.digit25.detector.process;

import ee.digit25.detector.domain.transaction.TransactionValidator;
import ee.digit25.detector.domain.transaction.common.TransactionMapper;
import ee.digit25.detector.domain.transaction.external.TransactionRequester;
import ee.digit25.detector.domain.transaction.external.TransactionVerifier;
import ee.digit25.detector.domain.transaction.external.api.TransactionModel;
import ee.digit25.detector.domain.transaction.feature.PersistTransactionFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class Processor {

    private static final int TRANSACTION_BATCH_SIZE = 100;
    private static final int THREAD_POOL_SIZE = 8;
    private static final int QUEUE_CAPACITY = 500;
    private static final int TRIGGER_THRESHOLD = 50;

    private final TransactionRequester requester;
    private final TransactionValidator validator;
    private final TransactionVerifier verifier;
    private final PersistTransactionFeature persistTransactionFeature;
    private final TransactionMapper transactionMapper;

    private final ThreadPoolExecutor customPool = new ThreadPoolExecutor(
            THREAD_POOL_SIZE,
            THREAD_POOL_SIZE,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicBoolean pulling = new AtomicBoolean(false);

    public void startProcessing() {
        log.info("Starting dynamic processing loop...");
        pullAndProcessBatch();
    }

    private void pullAndProcessBatch() {
        // prevent overlapping pulls
        if (!pulling.compareAndSet(false, true)) return;

        List<TransactionModel> transactions = requester.getUnverified(TRANSACTION_BATCH_SIZE);
        log.info("Pulled {} transactions", transactions.size());

        for (TransactionModel tx : transactions) {
            activeTasks.incrementAndGet();

            customPool.submit(() -> {
                try {
                    process(tx);
                } catch (Exception e) {
                    log.error("Error processing tx {}", tx.getId(), e);
                } finally {
                    int remaining = activeTasks.decrementAndGet();

                    // when nearly done, pull next batch
                    if (remaining < TRIGGER_THRESHOLD && !pulling.get()) {
                        log.info("Active tasks below threshold ({}), triggering next batch...", remaining);
                        pullAndProcessBatch();
                    }
                }
            });
        }

        pulling.set(false);
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
