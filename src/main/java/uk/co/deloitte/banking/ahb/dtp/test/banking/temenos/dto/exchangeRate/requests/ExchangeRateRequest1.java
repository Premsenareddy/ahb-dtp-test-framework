package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.exchangeRate.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.validation.Valid;

@Builder
@Data
@Introspected
@ToString
@Schema(name = "ExchangeRateRequest1", description = "the exchange rate request model")
public class ExchangeRateRequest1 {

   @Valid
   @JsonProperty("Data")
   @Schema(name = "exchangeRateRequestData1", description = "the exchange rate request data")
   private ExchangeRateRequestData1 exchangeRateRequestData1;
}
