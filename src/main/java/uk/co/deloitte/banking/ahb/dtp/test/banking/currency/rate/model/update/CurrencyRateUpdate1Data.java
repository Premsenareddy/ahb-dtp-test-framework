package uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateUpdate1Data {

    @JsonProperty("CurrencyRates")
    private List<CurrencyRate1> currencyRates;

}
