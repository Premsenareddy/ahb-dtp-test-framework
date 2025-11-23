package uk.co.deloitte.banking.ahb.dtp.test.customer.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class CustomerConfig {
    @Value("${customer-adapter.path}")
    private String basePath;

    @Value("${customer-adapter.apiKey}")
    private String apiKey;

    @Value("${customer-adapter.eligibilityPath}")
    private String eligibilityPath;

    @Value("${customer-adapter.sigCapPath}")
    private String sigCapPath;
}
