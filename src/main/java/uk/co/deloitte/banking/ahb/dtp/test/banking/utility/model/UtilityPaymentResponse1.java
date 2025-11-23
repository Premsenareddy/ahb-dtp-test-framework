package uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UtilityPaymentResponse1 {

    @JsonProperty("Data")
    private UtilityPaymentResponse1Data utilityPaymentResponseData;
}
