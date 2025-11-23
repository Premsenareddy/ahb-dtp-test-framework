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
public class UtilityPaymentRequest1Data {


    @JsonProperty("EndToEndIdentification")
    private String endToEndIdentification;

    @JsonProperty("PaymentMode")
    private String paymentMode;

    @JsonProperty("CreditorAccount")
    private CreditorAccount1 creditorAccount;

    @JsonProperty("DebtorAccount")
    private DebtorAccount1 debtorAccount;

    @JsonProperty("DueAmount")
    private DueAmount1 dueAmount;

    @JsonProperty("InstructedAmount")
    private InstructedAmount1 instructedAmount;


    @JsonProperty("TouchpointData")
    private TouchpointData1 touchpointData;

}
