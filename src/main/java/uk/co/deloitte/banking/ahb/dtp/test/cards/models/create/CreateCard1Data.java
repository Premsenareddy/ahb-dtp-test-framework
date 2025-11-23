package uk.co.deloitte.banking.ahb.dtp.test.cards.models.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class CreateCard1Data {
    @NotNull
    @Schema(name = "EmbossedName")
    @JsonProperty("EmbossedName")
    private String embossedName;

    @NotNull
    @Schema(name = "CardProduct", required = true)
    @JsonProperty("CardProduct")
    private CardProduct cardProduct;

    @NotNull
    @Schema(name = "Accounts")
    @JsonProperty("Accounts")
    private List<CreateCardAccount1> accounts;
}
