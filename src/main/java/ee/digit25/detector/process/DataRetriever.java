package ee.digit25.detector.process;

import ee.digit25.detector.domain.account.external.AccountRequester;
import ee.digit25.detector.domain.device.external.DeviceRequester;
import ee.digit25.detector.domain.person.external.PersonRequester;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataRetriever {
    private final AccountRequester accountRequester;
    private final DeviceRequester deviceRequester;
    private final PersonRequester personRequester;

    @EventListener(ApplicationReadyEvent.class)
    public void retrieveData(){
        accountRequester.getAccounts();
        deviceRequester.getBlacklistedDevices();
        personRequester.getPersons();
    }
}
