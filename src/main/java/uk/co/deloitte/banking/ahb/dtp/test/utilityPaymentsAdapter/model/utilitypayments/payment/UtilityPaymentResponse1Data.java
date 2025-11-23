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
public class UtilityPaymentResponse1Data {


    @JsonProperty("ReferenceNum")
    private String referenceNum;

    @JsonProperty("CreditorAccount")
    private CreditorAccount1 creditorAccount;

    @JsonProperty("DebtorAccount")
    private DebtorAccount1 debtorAccount;

    @JsonProperty("InstructedAmount")
    private InstructedAmount1 instructedAmount;

    @JsonProperty("ReturnCode")
    private String returnCode;

    @JsonProperty("ReturnText")
    private String returnText;
}
