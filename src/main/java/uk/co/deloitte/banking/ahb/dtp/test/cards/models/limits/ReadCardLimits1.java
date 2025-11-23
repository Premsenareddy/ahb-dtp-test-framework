package uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class ReadCardLimits1 {
    @JsonProperty("Data")
    @Schema(name = "Data")
    private ReadCardLimits1Data data;
}
