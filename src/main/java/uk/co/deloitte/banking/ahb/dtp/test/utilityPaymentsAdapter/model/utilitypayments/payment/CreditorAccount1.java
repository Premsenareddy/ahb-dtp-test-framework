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
public class CreditorAccount1 {

    @JsonProperty("CompanyCode")
    private String companyCode;

    @JsonProperty("Identification")
    private String identification;

    @JsonProperty("AccountPin")
    private String accountPin;

    @JsonProperty("AccountType")
    private String accountType;

}
