package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBCashAccount50;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WriteBeneficiary1Data {

    @JsonProperty("ServiceProvider")
    private String serviceProvider;

    @JsonProperty("ServiceCode")
    private String serviceCode;

    @JsonProperty("ServiceTypeCode")
    private String serviceTypeCode;

    @JsonProperty("ServiceType")
    private String serviceType;

    @JsonProperty("PremiseNumber")
    private String premiseNumber;

    @JsonProperty("ConsumerPin")
    private String consumerPin;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("MobileNumber")
    private String mobileNumber;

    @JsonProperty("CreditorAccount")
    OBCashAccount50 creditor;

}