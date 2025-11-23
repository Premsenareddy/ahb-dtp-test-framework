package uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response;

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
public class CreateCard1ResponseData {
    @JsonProperty("CardNumber")
    private String cardNumber;
    @JsonProperty("ExpiryDate")
    private String expiryDate;
}
