package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.onusinwardchequeClearance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Data {
        @JsonProperty("DebitAcctNo")
        public String debitAcctNo;

        @JsonProperty("DebitCurrency")
        public String debitCurrency;

        @JsonProperty("DebitValueDate")
        public String debitValueDate;

        @JsonProperty("DebitAmount")
        public String debitAmount;

        @JsonProperty("DebitReference")
        public String debitReference;

        @JsonProperty("CreditCurrency")
        public String creditCurrency;

        @JsonProperty("CreditReference")
        public String creditReference;

        @JsonProperty("CreditValueDate")
        public String creditValueDate;

        @JsonProperty("ChequeNumber")
        public String chequeNumber;

        @JsonProperty("BcSortCode")
        public String bcSortCode;

        @JsonProperty("EndToEndReference")
        public String endToEndReference;

        @JsonProperty("ChequeType")
        public String chequeType;

        @JsonProperty("CreditAcctNo")
        public String creditAcctNo;
}
