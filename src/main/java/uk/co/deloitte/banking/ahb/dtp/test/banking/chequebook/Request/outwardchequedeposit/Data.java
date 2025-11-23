package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequedeposit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Data{
    @JsonProperty("DebitCurrency")
    public String debitCurrency = "AED" ;
    @JsonProperty("DebitValueDate")
    public String debitValueDate = "20220208";
    @JsonProperty("CreditAmount")
    public String creditAmount = "1.34";
    @JsonProperty("DebitTheirRef")
    public String debitTheirRef = "CH0012354DEPOSIT";
    @JsonProperty("CreditAcctNo")
    public String creditAcctNo = "011131191001";
    @JsonProperty("CreditCurrency")
    public String creditCurrency = "AED";
    @JsonProperty("CreditTheirRef")
    public String creditTheirRef = "DEP12354";
    @JsonProperty("CreditValueDate")
    public String creditValueDate = "20220208";
    @JsonProperty("ChequeNumber")
    public String chequeNumber = "123590";
    @JsonProperty("BcSortCode")
    public String bcSortCode = "804030157";
    @JsonProperty("ChequeIssueBank")
    public String chequeIssueBank = "National Bank of Ras Al-Khaimah";
    @JsonProperty("CheqAcNo")
    public String cheqAcNo = "2210480001";
    @JsonProperty("CheqDepBr")
    public String cheqDepBr = "10025";
    @JsonProperty("CheqDate")
    public String cheqDate = "20220208";
    @JsonProperty("EndToEndReference")
    public String endToEndReference = "O220100000100040";
    @JsonProperty("StockNumber")
    public String stockNumber = "12354";
    @JsonProperty("PayeeName")
    public String payeeName = "TEST NAME";
    @JsonProperty("ChequeType")
    public String chequeType = "CURR";
}
