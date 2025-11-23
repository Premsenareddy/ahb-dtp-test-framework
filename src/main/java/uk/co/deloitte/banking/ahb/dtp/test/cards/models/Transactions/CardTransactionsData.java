package uk.co.deloitte.banking.ahb.dtp.test.cards.models.Transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import javax.validation.Valid;
import java.util.ArrayList;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class CardTransactionsData {
    @JsonProperty("Transaction")
    public ArrayList<Transaction> transaction;
}
