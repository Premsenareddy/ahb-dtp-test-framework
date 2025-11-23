package uk.co.deloitte.banking.ahb.dtp.test.aml.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;
import uk.co.deloitte.banking.http.properties.AlphaProperties;

import javax.inject.Singleton;

@Singleton
@Data
public class AmlConfiguration implements AlphaProperties {
    @Value("${sanctions-aml-adapter.path}")
    private String basePath;

    @Value("${sanctions-aml-adapter.apiKey}")
    private String apiKey;

}
