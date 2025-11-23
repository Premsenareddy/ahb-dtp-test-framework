package uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin;

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
public class WriteCardPinRequest1 {

    @JsonProperty("CardNumber")
    private String cardNumber;
    @JsonProperty("LastFourDigits")
    private String lastFourDigits;
    @JsonProperty("CardNumberFlag")
    private String cardNumberFlag;
    @JsonProperty("CardExpiryDate")
    private String cardExpiryDate;
    @JsonProperty("PinBlock")
    private String pinBlock;
    @JsonProperty("PinServiceType")
    private String pinServiceType;

}
