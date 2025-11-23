package ahb.experience.creditCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.ArrayList;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Introspected
public class ExpCardTransaction {

    @JsonProperty("content")
    public ArrayList<ExpCardTransactionContent> content;

    @JsonProperty("page")
    public ExpGetCreditCardPage page;
}
