package uk.co.deloitte.banking.ahb.dtp.test.documents.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class StatementGenerationRequestEvent {

    @NotBlank
    @JsonProperty("CustomerName")
    private String customerName;
    @NotBlank
    @JsonProperty("CustomerId")
    private String customerId;
    @NotBlank
    @JsonProperty("StatementDate")
    private String statementDate;
    @NotBlank
    @JsonProperty("StatementStartDate")
    private String statementStartDate;
    @NotBlank
    @JsonProperty("StatementEndDate")
    private String statementEndDate;
    @NotBlank
    @JsonProperty("AccountNumber")
    private String accountNumber;
    @NotBlank
    @JsonProperty("ProductDescription")
    private String productDescription;
    @NotBlank
    @JsonProperty("PostCode")
    private String postCode;
    @NotBlank
    @JsonProperty("TownCountry")
    private String townCountry;
    @NotBlank
    @JsonProperty("Country")
    private String country;
    @NotBlank
    @JsonProperty("IbanNumber")
    private String ibanNumber;
    @NotBlank
    @JsonProperty("Currency")
    private String currency;
    @NotBlank
    @JsonProperty("OpeningBalance")
    private String openingBalance;
    @NotBlank
    @JsonProperty("TotalMoneyIn")
    private String totalMoneyIn;
    @NotBlank
    @JsonProperty("TotalMoneyOut")
    private String totalMoneyOut;
    @Valid
    @JsonProperty("TransactionDetails")
    private List<TransactionDetails> transactionDetails;
}
