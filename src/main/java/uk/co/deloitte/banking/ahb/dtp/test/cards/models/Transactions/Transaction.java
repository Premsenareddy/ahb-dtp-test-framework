package uk.co.deloitte.banking.ahb.dtp.test.cards.models.Transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import javax.validation.Valid;
import java.util.Date;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class Transaction {
    @JsonProperty("CardNumber")
    public String cardNumber;
    @JsonProperty("CardNumberFlag")
    public String cardNumberFlag;
    @JsonProperty("TransactionDate")
    public Date transactionDate;
    @JsonProperty("PostingDate")
    public Date postingDate;
    @JsonProperty("TransactionDescription")
    public String transactionDescription;
    @JsonProperty("TransactionAmount")
    public TransactionAmount transactionAmount;
    @JsonProperty("BillingAmount")
    public BillingAmount billingAmount;
    @JsonProperty("DebitCreditFlag")
    public String debitCreditFlag;
    @JsonProperty("CategoryType")
    public String categoryType;
    @JsonProperty("CategoryGroup")
    public String categoryGroup;
    @JsonProperty("MatchingStatusFlag")
    public String matchingStatusFlag;
    @JsonProperty("EppindcatorFlag")
    public String eppindcatorFlag;
    @JsonProperty("MicrofilmRefNumber")
    public String microfilmRefNumber;
    @JsonProperty("MicrofilmRefSeq")
    public String microfilmRefSeq;
    @JsonProperty("TransactionBillingStatus")
    public String transactionBillingStatus;
}
