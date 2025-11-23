package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config;


import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class TemenosConfig {
    @Value("${temenos.seed-account}")
    private String seedAccountId;

    @Value("${temenos.creditor-account}")
    private String creditorAccountId;

    @Value("${temenos.creditor-iban}")
    private String creditorIban;

    @Value("${temenos.path}")
    private String path;

    @Value("${temenos.legacy-iban}")
    private String legacyIban;

    @Value("${temenos.tp-account}")
    private String tpAccount;

    @Value("${temenos.account-balance}")
    private String accountBalancePath;

    @Value("${temenos.webhook}")
    private String webhook_url;
}
