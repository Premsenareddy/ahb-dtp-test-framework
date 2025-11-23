package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
public class UtilityInquiryResponse1 {

    @Schema(name = "referenceNum")
    private String referenceNum;

    @Schema(name = "utilityCompanyCode")
    private String utilityCompanyCode;

    @Schema(name = "utilityAccount")
    private String utilityAccount;

    @Schema(name = "utilityAccountType")
    private String utilityAccountType;

    @Schema(name = "amount")
    private String amount;

    @Schema(name = "amountMin")
    private String amountMin;

    @Schema(name = "amountMax")
    private String amountMax;

    @Schema(name = "billDate")
    private String billDate;

    @Schema(name = "currentTimeStamp")
    private String currentTimeStamp;

}
