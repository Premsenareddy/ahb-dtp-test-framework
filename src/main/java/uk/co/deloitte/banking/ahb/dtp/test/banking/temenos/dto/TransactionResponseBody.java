package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TransactionResponseBody {

    @JsonProperty("transactionType")
    private String transactionType;
    @JsonProperty("debitCurrencyId")
    private String debitCurrencyId;
    @JsonProperty("processingDate")
    private String processingDate;
    @JsonProperty("creditCurrencyId")
    private String creditCurrencyId;
    @JsonProperty("debitAccountId")
    private String debitAccountId;
    @JsonProperty("creditAccountId")
    private String creditAccountId;
    @JsonProperty("creditAmount")
    private Integer creditAmount;
}
