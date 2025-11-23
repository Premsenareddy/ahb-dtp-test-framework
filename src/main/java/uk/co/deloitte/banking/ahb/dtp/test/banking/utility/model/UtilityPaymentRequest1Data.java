package uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityPaymentRequest1Data {

    @JsonProperty("PaymentAmount")
    @Builder.Default
    private UtilityPaymentAmount amount = UtilityPaymentAmount.builder().build();

    @JsonProperty("DebitAccountId")
    private String debitAccountId;

    @JsonProperty("CreditAccountId")
    @Builder.Default
    private String creditAccountId = "AED1756800040002";

    @JsonProperty("EndToEndReference")
    @Builder.Default
    private String endToEndReference = RandomStringUtils.randomAlphanumeric(12);

    @JsonProperty("RemittanceInformation")
    @Builder.Default
    private List<String> remittanceInformation = Collections.singletonList("Etisalat - 0553019466");

    @JsonProperty("RequestTime")
    @Builder.Default
    private List<String> requestTimes = Collections.singletonList("20210101160102123");

    @JsonProperty("PaymentOrderProduct")
    @Builder.Default
    private String paymentOrderProduct = "AHBUTIL";
}