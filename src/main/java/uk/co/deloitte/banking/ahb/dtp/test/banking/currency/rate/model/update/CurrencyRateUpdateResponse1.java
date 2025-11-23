package uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CurrencyRateUpdateResponse1 {

    @JsonProperty("Data")
    private CurrencyRateUpdateResponse1Data data;
}
