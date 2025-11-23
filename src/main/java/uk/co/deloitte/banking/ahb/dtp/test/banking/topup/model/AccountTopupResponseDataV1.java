package uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AccountTopupResponseDataV1 {

    @JsonProperty("InstructedAmount")
    private AccountTopupInstructedAmount instructedAmount;

    @JsonProperty("EndToEndIdentification")
    private String endToEndReference;

    @JsonProperty("RemittanceInformation")
    private List<String> remittanceInformation;

    @JsonProperty("CreditorAccount")
    private AccountTopupCreditorAccount account;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("PaymentSystemId")
    private String paymentSystemId;

    @JsonProperty("PaymentOrderProduct")
    private String paymentOrderProduct;
}
