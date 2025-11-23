package uk.co.deloitte.banking.ahb.dtp.test.banking.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountBody {
    @JsonProperty("accountId")
    public String accountId;
    @JsonProperty("useableBalance")
    public String useableBalance;
    @JsonProperty("lockedAmount")
    public String lockedAmount;
    @JsonProperty("productId")
    public String productId;
    @JsonProperty("workingBalance")
    public String workingBalance;
    @JsonProperty("customerId")
    public String customerId;
    @JsonProperty("currencyId")
    public String currencyId;
}
