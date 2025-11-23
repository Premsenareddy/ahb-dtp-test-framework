package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class UtilityPaymentsConfig {
    @Value("${utility-payments-adapter.path}")
    private String basePath;

    @Value("${utility-payments-adapter.created}")
    private String createdUtilityBene;
}
