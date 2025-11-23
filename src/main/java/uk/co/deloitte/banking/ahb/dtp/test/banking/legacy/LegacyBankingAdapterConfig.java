package uk.co.deloitte.banking.ahb.dtp.test.banking.legacy;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class LegacyBankingAdapterConfig {
    @Value("${legacy.path}")
    private String path;
}
