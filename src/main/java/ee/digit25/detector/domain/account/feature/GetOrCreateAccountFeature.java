package ee.digit25.detector.domain.account.feature;

import ee.digit25.detector.domain.account.common.Account;
import ee.digit25.detector.domain.account.common.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import static ee.digit25.detector.domain.account.common.AccountSpecification.numberEquals;

@Service
@RequiredArgsConstructor
public class GetOrCreateAccountFeature {

    private final AccountRepository repository;

    @Cacheable(value = "accountNumber", key = "#number")
    public Account byNumber(String number) {

        return repository.findOne(numberEquals(number))
                .orElseGet(() -> create(number));
    }

    private Account create(String number) {
        try {
            return repository.save(new Account(number));
        } catch (DataIntegrityViolationException e) {
            return repository.findOne(numberEquals(number)).orElseThrow(() -> e);
        }
    }
}
