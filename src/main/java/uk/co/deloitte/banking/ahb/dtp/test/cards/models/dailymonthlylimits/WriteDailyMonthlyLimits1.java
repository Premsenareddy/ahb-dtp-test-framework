package uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits;

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
public class WriteDailyMonthlyLimits1 {
    @JsonProperty("Data")
    @Schema(name = "Data")
    private WriteDailyMonthlyLimits1Data data;
}
