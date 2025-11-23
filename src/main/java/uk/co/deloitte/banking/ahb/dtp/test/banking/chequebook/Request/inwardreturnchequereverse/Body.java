package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Body {
    @JsonProperty("DebitValueDate")
    public String debitValueDate;
    @JsonProperty("DebitCurrency")
    public String debitCurrency;
    @JsonProperty("CreditReference")
    public String creditReference;
    @JsonProperty("EndToEndReference")
    public String endToEndReference;
    @JsonProperty("DebitReference")
    public String debitReference;
    @JsonProperty("ChequeIssueBank")
    public String chequeIssueBank;
    @JsonProperty("CreditValueDate")
    public String creditValueDate;
    @JsonProperty("DebitAmount")
    public String debitAmount;
    @JsonProperty("ChequeNumber")
    public String chequeNumber;
    @JsonProperty("CreditAcctNo")
    public String creditAcctNo;
    @JsonProperty("BcSortCode")
    public String bcSortCode;
    @JsonProperty("CreditCurrency")
    public String creditCurrency;
    @JsonProperty("DebitAcctNo")
    public String debitAcctNo;
}
