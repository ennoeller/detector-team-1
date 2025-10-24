package ee.digit25.detector.domain.transaction.common;

import ee.digit25.detector.domain.account.feature.GetOrCreateAccountFeature;
import ee.digit25.detector.domain.device.feature.GetOrCreateDeviceFeature;
import ee.digit25.detector.domain.person.feature.GetOrCreatePersonFeature;
import ee.digit25.detector.domain.transaction.external.api.TransactionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final GetOrCreatePersonFeature getOrCreatePersonFeature;
    private final GetOrCreateAccountFeature getOrCreateAccountFeature;
    private final GetOrCreateDeviceFeature getOrCreateDeviceFeature;

    public Transaction toEntity(TransactionModel model, boolean legitimate) {
        Transaction transaction = new Transaction();

        transaction.setLegitimate(legitimate);
        transaction.setAmount(model.getAmount());
        transaction.setTimestamp(model.getTimestamp());
        transaction.setDeadline(model.getDeadline());

        transaction.setSender(getOrCreatePersonFeature.byPersonCode(model.getSender()));
        transaction.setSenderAccount(getOrCreateAccountFeature.byNumber(model.getSenderAccount()));
        transaction.setRecipient(getOrCreatePersonFeature.byPersonCode(model.getRecipient()));
        transaction.setRecipientAccount(getOrCreateAccountFeature.byNumber(model.getRecipientAccount()));
        transaction.setDevice(getOrCreateDeviceFeature.byMac(model.getDeviceMac()));

        return transaction;
    }
}
