package uk.co.deloitte.banking.ahb.dtp.test.devSim;

import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class DevSimConfiguration {

    @Value("${devsim-service.path}")
    private String basePath;

    @Value("${devsim-service.apiKey}")
    private String apiKey;

}


