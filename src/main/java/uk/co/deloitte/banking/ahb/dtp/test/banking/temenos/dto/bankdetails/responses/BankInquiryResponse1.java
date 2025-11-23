package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.bankdetails.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BankInquiryResponse1 {

    @JsonProperty("ReferenceNum")
    private String referenceNum;

    @JsonProperty("SwiftCode")
    private String swiftCode;

    @JsonProperty("BankName")
    private String bankName;

    @JsonProperty("BankBranch")
    private String bankBranch;

    @JsonProperty("CountryName")
    private String countryName;

    @JsonProperty("CountrySpecificCode")
    private String countrySpecificCode;
}
