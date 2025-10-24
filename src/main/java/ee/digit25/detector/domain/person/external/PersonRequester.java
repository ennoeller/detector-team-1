package ee.digit25.detector.domain.person.external;

import ee.bitweb.core.retrofit.RetrofitRequestExecutor;
import ee.digit25.detector.domain.person.external.api.PersonApi;
import ee.digit25.detector.domain.person.external.api.PersonApiProperties;
import ee.digit25.detector.domain.person.external.api.PersonModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonRequester {

    private final PersonApi api;
    private final PersonApiProperties properties;

    public PersonModel get(String personCode) {
        log.info("Requesting person with personCode {}", personCode);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), personCode));
    }

    public List<PersonModel> get(List<String> personCodes) {
        log.info("Requesting persons with personCodes {}", personCodes);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), personCodes));
    }


    public List<PersonModel> get(int pageNumber, int pageSize) {
        log.info("Requesting persons page {} of size {}", pageNumber, pageSize);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), pageNumber, pageSize));
    }

    @Cacheable(value = "persons", sync = true)
    public Map<String, PersonModel> getPersons() {
        Map<String, PersonModel> personModels = new HashMap<>();
        boolean hasData = true;
        int pageNumber = 0;
        while (hasData) {
            List<PersonModel> persons = get(pageNumber, 1000);
            hasData = !persons.isEmpty();
            pageNumber++;
            persons.forEach((personModel ->
                    personModels.computeIfAbsent(personModel.getPersonCode(), k -> personModel))
            );
        }

        return personModels;

    }
}
