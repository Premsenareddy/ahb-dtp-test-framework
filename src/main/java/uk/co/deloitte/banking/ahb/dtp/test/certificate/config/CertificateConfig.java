package uk.co.deloitte.banking.ahb.dtp.test.certificate.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class CertificateConfig {

    @Value("${certificate-service.path}")
    private String basePath;

    @Value("${certificate-service.apiKey}")
    private String apiKey;

}
