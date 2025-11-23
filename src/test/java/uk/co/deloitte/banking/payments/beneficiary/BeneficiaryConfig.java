package uk.co.deloitte.banking.payments.beneficiary;


import io.micronaut.context.annotation.Value;
import lombok.Data;

import javax.inject.Singleton;

@Singleton
@Data
public class BeneficiaryConfig {
    @Value("${beneficiary.internal-flag}")
    private String internalFlag;

    @Value("${beneficiary.international-flag}")
    private String internationalFlag;

    @Value("${beneficiary.domestic-flag}")
    private String domesticFlag;

}
