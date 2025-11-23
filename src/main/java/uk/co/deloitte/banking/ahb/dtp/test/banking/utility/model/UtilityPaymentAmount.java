package uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
public class UtilityPaymentAmount {

    @JsonProperty("Amount")
    @Builder.Default
    private BigDecimal amount = new BigDecimal("100");

    @JsonProperty("PaymentCurrency")
    @Builder.Default
    private String paymentCurrencyId = "AED";
}
