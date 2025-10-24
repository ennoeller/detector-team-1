package ee.digit25.detector.domain.device.feature;

import ee.digit25.detector.domain.device.common.Device;
import ee.digit25.detector.domain.device.common.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import static ee.digit25.detector.domain.device.common.DeviceSpecification.macEquals;

@Service
@RequiredArgsConstructor
public class GetOrCreateDeviceFeature {

    private final DeviceRepository repository;

    @Cacheable(value = "mac", key = "#mac")
    public Device byMac(String mac) {

        return repository.findOne(macEquals(mac)).orElseGet(() -> create(mac));
    }

    private Device create(String mac) {
        try {
            return repository.save(new Device(mac));
        } catch (DataIntegrityViolationException e) {
            return repository.findOne(macEquals(mac)).orElseThrow(() -> e);
        }
    }
}
