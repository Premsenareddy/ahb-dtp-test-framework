package uk.co.deloitte.banking.ahb.dtp.test.cards.models.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class CreateCard1 {
    @NotNull
    @Schema(name = "Data")
    @JsonProperty("Data")
    private CreateCard1Data data;
}
