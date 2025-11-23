package uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateUpdateRequest1 {

    @JsonProperty("Data")
    private CurrencyRateUpdate1Data data;
}
