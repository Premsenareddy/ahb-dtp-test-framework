package uk.co.deloitte.banking.ahb.dtp.test.cards.models.fetchDigitalCards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ProductName",
        "ProductCode"
})
public class CardProduct {
    @JsonProperty("ProductName")
    public String productName;
    @JsonProperty("ProductCode")
    public String productCode;
}
