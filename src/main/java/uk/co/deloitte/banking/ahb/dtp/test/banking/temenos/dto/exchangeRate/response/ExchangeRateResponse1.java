package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.exchangeRate.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateResponse1 {

    @Schema(name = "debitCurrency", description = "The debit currency as per exchange rate")
    @JsonProperty("debitCurrency")
    private String debitCurrency;

    @Schema(name = "creditCurrency", description = "The credit currency as per exchange rate")
    @JsonProperty("creditCurrency")
    private String creditCurrency;

    @Schema(name = "customerRate", description = "The customer Rate as per exchange rate")
    @JsonProperty("customerRate")
    private BigDecimal customerRate;

    @Schema(name = "customerSpread", description = "The customer Spread as per exchange rate")
    @JsonProperty("customerSpread")
    private BigDecimal customerSpread;
}
