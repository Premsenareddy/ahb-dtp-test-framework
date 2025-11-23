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
public class TransactionResponse {

    @JsonProperty("header")
    private TransactionResponseHeader header;

    @JsonProperty("body")
    private TransactionResponseBody body;
}
