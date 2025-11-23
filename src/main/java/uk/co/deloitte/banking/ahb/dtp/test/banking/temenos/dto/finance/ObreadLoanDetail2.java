package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ObreadLoanDetail2 {
    @JsonProperty("RemainingPayment")
    public String remainingPayment;
    @JsonProperty("NextPaymentDate")
    public String nextPaymentDate;
    @JsonProperty("ContractDate")
    public String contractDate;
    @JsonProperty("OverdueAmount")
    public String overdueAmount;
    @JsonProperty("ContractStatus")
    public String contractStatus;
    @JsonProperty("LoanType")
    public String loanType;
    @JsonProperty("ContractNumber")
    public String contractNumber;
    @JsonProperty("ProfitRate")
    public String profitRate;
    @JsonProperty("AccountNumber")
    public String accountNumber;
    @JsonProperty("LoanAmount")
    public String loanAmount;
    @JsonProperty("PastDueStatus")
    public String pastDueStatus;
    @JsonProperty("ProductCode")
    public String productCode;
    @JsonProperty("MaturityDate")
    public String maturityDate;
    @JsonProperty("CustomerId")
    public String customerId;
    @JsonProperty("LoanTypeDescription")
    public String loanTypeDescription;
    @JsonProperty("PaymentAccount")
    public String paymentAccount;
    @JsonProperty("NextPaymentAmount")
    public String nextPaymentAmount;
    @JsonProperty("ProductDescription")
    public String productDescription;
}
