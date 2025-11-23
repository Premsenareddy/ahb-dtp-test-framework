package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class InternalTransferRequestBody {
    @JsonProperty("creditAmount")
    private BigDecimal creditAmount;

    @JsonProperty("creditCurrencyId")
    private String creditCurrencyId;
    @JsonProperty("debitCurrencyId")
    private String debitCurrencyId;
    @JsonProperty("transactionType")
    private String transactionType;
    @JsonProperty("debitAccountId")
    private String debitAccountId;
    @JsonProperty("creditAccountId")
    private String creditAccountId;
}
