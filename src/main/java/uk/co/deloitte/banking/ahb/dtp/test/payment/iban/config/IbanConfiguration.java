package uk.co.deloitte.banking.ahb.dtp.test.payment.iban.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class IbanConfiguration {

    @Value("${iban-service.path}")
    private String basePath;

    @Value("${iban-service.apiKey}")
    private String apiKey;
}
