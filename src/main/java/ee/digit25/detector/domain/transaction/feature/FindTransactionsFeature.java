package ee.digit25.detector.domain.transaction.feature;

import ee.digit25.detector.domain.transaction.common.Transaction;
import ee.digit25.detector.domain.transaction.common.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static ee.digit25.detector.domain.transaction.common.TransactionSpecification.sender;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindTransactionsFeature {

    private final TransactionRepository repository;

    public List<Transaction> bySender(String sender) {
        log.debug("Fetching transaction history by sender: {}", sender);

        return repository.findAll(sender(sender));
    }
}
