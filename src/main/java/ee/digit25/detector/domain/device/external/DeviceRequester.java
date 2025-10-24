package ee.digit25.detector.domain.device.external;

import ee.bitweb.core.retrofit.RetrofitRequestExecutor;
import ee.digit25.detector.domain.device.external.api.DeviceModel;
import ee.digit25.detector.domain.device.external.api.DeviceApi;
import ee.digit25.detector.domain.device.external.api.DeviceApiProperties;
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
public class DeviceRequester {

    private final DeviceApi api;
    private final DeviceApiProperties properties;

    public DeviceModel get(String mac) {
        log.debug("Requesting device with mac({})", mac);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), mac));
    }

    public List<DeviceModel> get(List<String> macs) {
        log.debug("Requesting devices with macs {}", macs);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), macs));
    }

    public List<DeviceModel> get(int pageNumber, int pageSize) {
        log.debug("Requesting persons page {} of size {}", pageNumber, pageSize);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), pageNumber, pageSize));
    }

    @Cacheable(value = "devices", sync = true)
    public Map<String, Boolean> getBlacklistedDevices() {
        Map<String, Boolean> deviceModels = new HashMap<>();
        boolean hasData = true;
        int pageNumber = 0;
        while (hasData) {
            List<DeviceModel> devices = get(pageNumber, 1000);
            hasData = !devices.isEmpty();
            pageNumber++;
            devices.forEach((deviceModel ->
                    deviceModels.put(deviceModel.getMac(), deviceModel.getIsBlacklisted()))
            );
        }

        return deviceModels;

    }
}
