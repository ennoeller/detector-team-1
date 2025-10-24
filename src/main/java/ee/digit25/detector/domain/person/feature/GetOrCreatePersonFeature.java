package ee.digit25.detector.domain.person.feature;

import ee.digit25.detector.domain.person.common.Person;
import ee.digit25.detector.domain.person.common.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GetOrCreatePersonFeature {

    private final PersonRepository repository;

// TODO: kasuta ConcurrentHashMap, et teha thread-safe cache
    private final Map<String, Person> cache = new ConcurrentHashMap<>();

    public Person byPersonCode(String personCode) {

        // TODO: kasuta cache'i, et vältida korduvaid päringuid
        return cache.computeIfAbsent(personCode, key ->
            repository.findByPersonCode(key)
                    .orElseGet(() -> create(key))
        );
    }

    @Transactional
    private Person create(String personCode) {
        // TODO: käsitle unikaalsuse rikkumist, kui samaaegselt luuakse sama personCode'iga isik
        try {
            Person created = repository.save(new Person(personCode));
            cache.put(personCode, created);
            return created;
        } catch (DataIntegrityViolationException e) {
            Person existing = repository.findByPersonCode(personCode)
                    .orElseThrow(() -> e);
            cache.put(personCode, existing);
            return existing;
        }
    }
}
