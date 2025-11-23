package uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class WriteDailyMonthlyLimits1Data {
    @JsonProperty(value = "CardNumber")
    @Schema(name = "CardNumber", required = true)
    @Pattern(regexp = "^[0-9]*$")
    @Size(min = 16, max = 16)
    private String cardNumber;
    @JsonProperty(value = "CardNumberFlag")
    private String cardNumberFlag;
    @Schema(name = "DailyATMLimit")
    @JsonProperty("DailyATMLimit")
    private String dailyAtmLimit;
    @Schema(name = "DailyPOSLimit")
    @JsonProperty("DailyPOSLimit")
    private String dailyPosLimit;
    @Schema(name = "MonthlyATMLimit")
    @JsonProperty("MonthlyATMLimit")
    private String monthlyAtmLimit;
    @Schema(name = "MonthlyPOSLimit")
    @JsonProperty("MonthlyPOSLimit")
    private String monthlyPosLimit;
    @Schema(name = "DailyECOMMLimit")
    @JsonProperty("DailyECOMMLimit")
    private String dailyEcommLimit;
    @Schema(name = "MonthlyECOMMLimit")
    @JsonProperty("MonthlyECOMMLimit")
    private String monthlyEcommLimit;
}
