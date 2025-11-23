package uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
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
public class ReadCardLimits1DataLimits {
    @JsonProperty("LimitTypeWording")
    private String limitTypeWording;
    @JsonProperty("DailyOnusAmount")
    private String dailyOnusAmount;
    @JsonProperty("DailyOnusTrxs")
    private String dailyOnusTrxs;
    @JsonProperty("DailyNationalAmount")
    private String dailyNationalAmount;
    @JsonProperty("DailyNationalTrxs")
    private String dailyNationalTrxs;
    @JsonProperty("DailyInternationalAmount")
    private String dailyInternationalAmount;
    @JsonProperty("DailyInterNationalTrxs")
    private String dailyInternationalTrxs;
    @JsonProperty("DailyTotalAmount")
    private String dailyTotalAmount;
    @JsonProperty("DailyTotalTrxs")
    private String dailyTotalTrxs;
    @JsonProperty("PeriodicOnusAmount")
    private String periodicOnusAmount;
    @JsonProperty("PeriodicOnusTrxNumber")
    private String periodicOnusTrxNumber;
    @JsonProperty("PeriodicNationalAmount")
    private String periodicNationalAmount;
    @JsonProperty("PeriodicNationalTrxNumber")
    private String periodicNationalTrxNumber;
    @JsonProperty("PeriodicInternationalTrxNumber")
    private String periodicInternationalTrxNumber;
    @JsonProperty("PeriodicTotalAmount")
    private String periodicTotalAmount;
    @JsonProperty("PeriodicTotalTrxs")
    private String periodicTotalTrxs;
}
