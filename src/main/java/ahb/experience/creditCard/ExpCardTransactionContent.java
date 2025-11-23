package ahb.experience.creditCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.Date;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class ExpCardTransactionContent {

    @JsonProperty("amount")
    public double amount;
    @JsonProperty("currency")
    public String currency;
    @JsonProperty("type")
    public String type;
    @JsonProperty("description")
    public String description;
    @JsonProperty("date")
    public Date date;
    @JsonProperty("transactionBillingStatus")
    public String transactionBillingStatus;
    @JsonProperty("referenceNumber")
    public String referenceNumber;
}
