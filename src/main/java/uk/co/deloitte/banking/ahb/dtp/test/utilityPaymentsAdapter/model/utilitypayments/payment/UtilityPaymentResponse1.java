
package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Introspected
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtilityPaymentResponse1 {

    @JsonProperty("Data")
    private UtilityPaymentResponse1Data data;
}
