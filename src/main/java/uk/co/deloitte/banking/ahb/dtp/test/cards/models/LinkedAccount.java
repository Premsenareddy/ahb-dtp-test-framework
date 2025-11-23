package uk.co.deloitte.banking.ahb.dtp.test.cards.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Introspected
public class LinkedAccount {
    @JsonProperty("AccountNumber")
    private String accountNumber;
    @JsonProperty("AccountType")
    private String accountType;
}
