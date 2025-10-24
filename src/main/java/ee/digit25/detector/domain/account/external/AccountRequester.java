package ee.digit25.detector.domain.account.external;

import ee.bitweb.core.retrofit.RetrofitRequestExecutor;
import ee.digit25.detector.domain.account.external.api.AccountModel;
import ee.digit25.detector.domain.account.external.api.AccountApi;
import ee.digit25.detector.domain.account.external.api.AccountApiProperties;
import ee.digit25.detector.domain.device.external.api.DeviceModel;
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
public class AccountRequester {

    private final AccountApi api;
    private final AccountApiProperties properties;

    public AccountModel get(String accountNumber) {
        log.debug("Requesting account {}", accountNumber);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), accountNumber));
    }

    public List<AccountModel> get(List<String> numbers) {
        log.debug("Requesting accounts with numbers {}", numbers);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), numbers));
    }

    public List<AccountModel> get(int pageNumber, int pageSize) {
        log.debug("Requesting accounts page {} of size {}", pageNumber, pageSize);

        return RetrofitRequestExecutor.executeRaw(api.get(properties.getToken(), pageNumber, pageSize));
    }

    @Cacheable(value = "accounts", sync = true)
    public Map<String, AccountModel> getAccounts() {
        Map<String, AccountModel> accountModels = new HashMap<>();
        boolean hasData = true;
        int pageNumber = 0;
        while (hasData) {
            List<AccountModel> accounts = get(pageNumber, 1000);
            hasData = !accounts.isEmpty();
            pageNumber++;
            accounts.forEach((accountModel ->
                    accountModels.put(accountModel.getNumber(), accountModel))
            );
        }

        return accountModels;

    }
}
