package uk.co.deloitte.banking.ahb.dtp.test.banking.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.List;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountBalance {

    @JsonProperty("header")
    public AccountHeader header;
    @JsonProperty("body")
    public List<AccountBody> body;
}
