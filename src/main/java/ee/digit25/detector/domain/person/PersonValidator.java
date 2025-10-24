package ee.digit25.detector.domain.person;

import ee.digit25.detector.domain.person.external.PersonRequester;
import ee.digit25.detector.domain.person.external.api.PersonModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonValidator {

    private final PersonRequester requester;

    private PersonModel getPerson(String personCode) {
        log.debug("Fetching person ({}) with code", personCode);

        return requester.getPersons().get(personCode);
    }

    private boolean hasWarrantIssued(PersonModel person) {
        log.debug("Cheking if person ({}) has a warrant issued", person.getPersonCode());

        return person.getWarrantIssued();
    }

    private boolean hasContract(PersonModel person) {
        log.debug("Cheking if person ({}) has a contract", person.getPersonCode());

        return person.getHasContract();
    }

    private boolean isBlacklisted(PersonModel person) {
        log.debug("Cheking if person ({}) is blacklisted", person.getPersonCode());

        return person.getBlacklisted();
    }

    public boolean isValid(String personCode) {
        PersonModel person = getPerson(personCode);
        if(hasWarrantIssued(person)) {
            return false;
        }
        if(!hasContract(person)) {
            return false;
        }
        return !isBlacklisted(person);

    }

}
