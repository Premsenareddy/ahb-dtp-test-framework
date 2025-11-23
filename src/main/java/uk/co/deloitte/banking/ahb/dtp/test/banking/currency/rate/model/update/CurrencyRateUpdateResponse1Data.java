package uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CurrencyRateUpdateResponse1Data {

    @JsonProperty("Status")
    private String status;

    @JsonProperty("CurrencyRates")
    private List<CurrencyRate1> currencyRates;

}
