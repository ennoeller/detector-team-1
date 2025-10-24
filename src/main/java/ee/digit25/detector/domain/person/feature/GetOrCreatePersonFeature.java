package ee.digit25.detector.domain.person.feature;

import ee.digit25.detector.domain.person.common.Person;
import ee.digit25.detector.domain.person.common.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import static ee.digit25.detector.domain.person.common.PersonSpecification.personCodeEquals;

@Service
@RequiredArgsConstructor
public class GetOrCreatePersonFeature {

    private final PersonRepository repository;

    @Cacheable(value = "personCode", key = "#personCode")
    public Person byPersonCode(String personCode) {

        return repository.findOne(personCodeEquals(personCode))
                .orElseGet(() -> create(personCode));
    }

    private Person create(String personCode) {
        try {
            return repository.save(new Person(personCode));
        } catch (DataIntegrityViolationException e) {
            return repository.findOne(personCodeEquals(personCode)).orElseThrow(() -> e);
        }
    }
}
