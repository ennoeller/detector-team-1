package ee.digit25.detector.domain.transaction.external;

import ee.bitweb.core.retrofit.RetrofitRequestExecutor;
import ee.digit25.detector.domain.transaction.external.api.TransactionModel;
import ee.digit25.detector.domain.transaction.external.api.TransactionApiProperties;
import ee.digit25.detector.domain.transaction.external.api.TransactionsApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionVerifier {

    private final TransactionsApi api;
    private final TransactionApiProperties properties;

    public void verify(TransactionModel transaction) {
        log.debug("Verifying transaction {}", transaction.getId());

        RetrofitRequestExecutor.executeRaw(api.verify(properties.getToken(), transaction.getId()));
    }

    public void reject(TransactionModel transaction) {
        log.debug("Rejecting transaction {}", transaction.getId());

        RetrofitRequestExecutor.executeRaw(api.reject(properties.getToken(), transaction.getId()));
    }

    public void verify(List<TransactionModel> transactions) {
        List<String> ids = transactions.stream().map(TransactionModel::getId).toList();
        log.debug("Verifying transactions {}", ids);

        RetrofitRequestExecutor.executeRaw(api.verify(properties.getToken(), ids));
    }

    public void reject(List<TransactionModel> transactions) {
        List<String> ids = transactions.stream().map(TransactionModel::getId).toList();
        log.debug("Rejecting transactions {}", ids);

        RetrofitRequestExecutor.executeRaw(api.reject(properties.getToken(), ids));
    }
}
