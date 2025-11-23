package uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
public class CurrencyRate1 {

    @JsonProperty("Market")
    private Integer market;

    @JsonProperty("BuyRate")
    private BigDecimal buyRate;

    @JsonProperty("SellRate")
    private BigDecimal sellRate;

    @JsonProperty("ForexSpread")
    private BigDecimal forexSpread;

    @JsonProperty("CustomerSpread")
    private BigDecimal customerSpread;
}
