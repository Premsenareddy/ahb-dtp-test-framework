package uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import javax.validation.Valid;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class ReadCardCvv1Data {
    @JsonProperty("CardNumber")
    private String cardNumber;
    @JsonProperty("ExpiryDate")
    private String expiryDate;
    @JsonProperty("Cvv")
    private String cvv;
}
