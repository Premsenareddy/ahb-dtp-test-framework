package uk.co.deloitte.banking.ahb.dtp.test.kongAccounts;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class KongAccountsConfig {

    @Value("${kong-accounts-service.path}")
    private String basePath;

    @Value("${kong-accounts-service.apiKey}")
    private String apiKey;
}
