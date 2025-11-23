package ahb.experience.save.transactionList;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "transactionDescription",
        "date",
        "transactionId",
        "amount",
        "currency",
        "transactionType",
        "status",
        "transactionCategory",
        "beneficiaryAccountNumber",
        "remark",
        "detail",
        "purpose",
        "accountId"
})
@Generated("jsonschema2pojo")
@NoArgsConstructor
public class Transaction {

    @JsonProperty("transactionDescription")
    private String transactionDescription;
    @JsonProperty("date")
    private String date;
    @JsonProperty("transactionId")
    private String transactionId;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("transactionType")
    private String transactionType;
    @JsonProperty("status")
    private String status;
    @JsonProperty("transactionCategory")
    private String transactionCategory;
    @JsonProperty("beneficiaryAccountNumber")
    private String beneficiaryAccountNumber;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("detail")
    private String detail;
    @JsonProperty("purpose")
    private String purpose;
    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("transactionDescription")
    public String getTransactionDescription() {
        return transactionDescription;
    }

    @JsonProperty("transactionDescription")
    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("transactionId")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonProperty("amount")
    public Double getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("transactionType")
    public String getTransactionType() {
        return transactionType;
    }

    @JsonProperty("transactionType")
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("transactionCategory")
    public String getTransactionCategory() {
        return transactionCategory;
    }

    @JsonProperty("transactionCategory")
    public void setTransactionCategory(String transactionCategory) {
        this.transactionCategory = transactionCategory;
    }

    @JsonProperty("beneficiaryAccountNumber")
    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    @JsonProperty("beneficiaryAccountNumber")
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    @JsonProperty("remark")
    public String getRemark() {
        return remark;
    }

    @JsonProperty("remark")
    public void setRemark(String remark) {
        this.remark = remark;
    }

    @JsonProperty("detail")
    public String getDetail() {
        return detail;
    }

    @JsonProperty("detail")
    public void setDetail(String detail) {
        this.detail = detail;
    }

    @JsonProperty("purpose")
    public String getPurpose() {
        return purpose;
    }

    @JsonProperty("purpose")
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @JsonProperty("accountId")
    public String getAccountId() {
        return accountId;
    }

    @JsonProperty("accountId")
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}
