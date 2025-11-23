package uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model;

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
public class UtilityPaymentResponse1Data {

    @JsonProperty("Amount")
    private UtilityPaymentAmount amount;

    @JsonProperty("DebitAccountId")
    private String debitAccountId;

    @JsonProperty("CreditAccountId")
    private String creditAccountId;

    @JsonProperty("EndToEndReference")
    private String endToEndReference;

    @JsonProperty("RemittanceInformation")
    private List<String> remittanceInformation;

    @JsonProperty("RequestTime")
    private List<String> requestTimes;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("PaymentOrderProduct")
    private String paymentOrderProduct;

    @JsonProperty("DebitAccountIBAN")
    private String debitAccountIBAN;

    @JsonProperty("PaymentSystemId")
    private String paymentSystemId;
}
