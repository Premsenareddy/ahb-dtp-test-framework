package uk.co.deloitte.banking.ahb.dtp.test.banking.config;


import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class BankingConfig {
    @Value("${banking-adapter.path}")
    private String basePath;

    @Value("${banking-adapter.apiKey}")
    private String apiKey;

    @Value("${banking-adapter.banking-user-password}")
    private String bankingUserPassword;

    @Value("${banking-adapter.banking-user-deviceId}")
    private String bankingUserDeviceId;

    @Value("${banking-adapter.banking-user-userId}")
    private String bankingUserUserId;

    @Value("${banking-adapter.banking-user-accountNumber}")
    private String bankingUserAccountNumber;

    @Value("${banking-adapter.banking-user-Base64PrivateKey}")
    private String bankingUserPrivateKey;

    @Value("${banking-adapter.banking-user-Base64PublicKey}")
    private String bankingUserPublicKey;

    @Value("${banking-adapter.base-url_inward}")
    private String inwardBasePath;
}
