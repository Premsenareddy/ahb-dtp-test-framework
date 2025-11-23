package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class DebtorAccount1 {

    @JsonProperty("Identification")
    private String identification;


    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("CardNoFlag")
    private String cardNoFlag;

}
