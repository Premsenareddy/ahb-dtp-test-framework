package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.List;

/**
 * AccountListResponse
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AccountListResponse {
    @JsonProperty("header")
    private QueryHeader header;

    @JsonProperty("body")
    @Valid
    private List<TemenosAccount> body;
}

