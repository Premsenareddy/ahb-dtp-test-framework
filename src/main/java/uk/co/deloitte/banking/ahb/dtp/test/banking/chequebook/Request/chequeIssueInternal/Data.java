package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.chequeIssueInternal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Data {
        @JsonProperty("AhbAccountNumber")
        public String ahbAccountNumber = "43643636";
        @JsonProperty("EndToEndReference")
        public String endToEndReference = "202109211701";
}
