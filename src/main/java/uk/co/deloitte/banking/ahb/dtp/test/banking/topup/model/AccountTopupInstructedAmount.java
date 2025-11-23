package uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class AccountTopupInstructedAmount {

    @JsonProperty("Amount")
    private BigDecimal amount;

    @JsonProperty("Currency")
    private String currency;
}
