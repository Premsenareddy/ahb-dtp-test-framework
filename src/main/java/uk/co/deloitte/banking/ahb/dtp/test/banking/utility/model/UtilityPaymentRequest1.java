package uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
public class UtilityPaymentRequest1 {

    @JsonProperty("Data")
    private UtilityPaymentRequest1Data utilityPaymentRequestData;
}
