package uk.co.deloitte.banking.ahb.dtp.test.otp;

import io.micronaut.context.annotation.Value;
import lombok.Data;


@Data
public class OtpConfiguration {

    @Value("${otp-service.path}")
    private String basePath;

    @Value("${otp-service.apiKey}")
    private String apiKey;
}

