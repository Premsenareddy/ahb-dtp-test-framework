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
public class TransactionResponseHeader {

    @JsonProperty("audit")
    private TransactionResponseAudit audit;
    @JsonProperty("id")
    private String id;
    @JsonProperty("transactionStatus")
    private String transactionStatus;
    @JsonProperty("status")
    private String status;
}
