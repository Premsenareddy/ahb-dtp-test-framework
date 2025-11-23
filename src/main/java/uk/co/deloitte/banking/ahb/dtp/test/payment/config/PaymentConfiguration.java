package uk.co.deloitte.banking.ahb.dtp.test.payment.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;
import uk.co.deloitte.banking.http.properties.AlphaProperties;

import javax.inject.Singleton;

@Singleton
@Data
public class PaymentConfiguration implements AlphaProperties {
    @Value("${payment-service.path}")
    private String basePath;

    @Value("${payment-service.apiKey}")
    private String apiKey;

    @Value("${payment-service.transfer-limit}")
    private String transferLimit;

    @Value("${payment-service.max-payment-limit}")
    private String maxPaymentLimit;

    @Value("${payment-service.max-unauth-limit}")
    private String maxUnauthLimit;

    @Value("${payment-service.dependant-id}")
    private String dependantId;
}
