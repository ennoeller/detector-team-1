package ee.digit25.detector.domain.device;

import ee.digit25.detector.domain.device.external.DeviceRequester;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
// TODO: kasuta @Lazy, et vältida tsüklilisi sõltuvusi
@Lazy
@RequiredArgsConstructor
public class DeviceValidator {

     private final DeviceRequester requester;
     // TODO: kasuta ConcurrentHashMap, et teha thread-safe cache
    private final Map<String, Boolean> blacklistCache = new ConcurrentHashMap<>();

    public boolean isValid(String mac) {
        // TODO: lisa logimine
        if (log.isDebugEnabled()) {
            log.debug("Validating device {}", mac);
        }
        return !isBlacklisted(mac);
    }

        public boolean isBlacklisted(String mac) {
            // TODO: lisa logimine
        if (log.isDebugEnabled()) {
            log.debug("Checking if device {} is blacklisted", mac);
        }

        // TODO: kasuta @Cacheable, et vältida ise cache'i tegemist
        return blacklistCache.computeIfAbsent(mac, key -> {
            var device = requester.get(key);
            return device.getIsBlacklisted();
        });
    }
}
