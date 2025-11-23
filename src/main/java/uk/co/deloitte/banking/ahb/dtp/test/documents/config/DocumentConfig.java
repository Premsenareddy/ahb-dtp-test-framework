package uk.co.deloitte.banking.ahb.dtp.test.documents.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class DocumentConfig {
    @Value("${document-adapter.path}")
    private String basePath;

    @Value("${document-adapter.apiKey}")
    private String apiKey;
}
