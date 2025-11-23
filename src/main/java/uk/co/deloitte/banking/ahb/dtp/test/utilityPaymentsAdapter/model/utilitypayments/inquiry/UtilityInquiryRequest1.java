package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
@Introspected
public class UtilityInquiryRequest1 {


    @JsonProperty("ReferenceNum")
    private String referenceNum;

    @JsonProperty("UtilityCompanyCode")
    private String utilityCompanyCode;

    @JsonProperty("UtilityAccount")
    private String utilityAccount;

    @JsonProperty("UtilityAccountPin")
    private String utilityAccountPin;

    @JsonProperty("UtilityAccountType")
    private String utilityAccountType;

}
