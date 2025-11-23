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
        "CardNumber",
        "CardNumberFlag",
        "Status",
        "IsCardDelivered",
        "IsCardActivated",
        "IsVirtualCardActivated",
        "IsPhysicalCardPrinted",
        "IsPinSet",
        "StatusCode",
        "CardExpiryDate",
        "CardHolderName",
        "CardProduct",
        "CardPlastic",
        "TotalCreditLimit",
        "AvailableLimit",
        "UtilizedLimit"
})
public class FetchDigitalCards3 {
    @JsonProperty("CardNumber")
    public String cardNumber;
    @JsonProperty("CardNumberFlag")
    public String cardNumberFlag;
    @JsonProperty("Status")
    public String status;
    @JsonProperty("IsCardDelivered")
    public Boolean isCardDelivered;
    @JsonProperty("IsCardActivated")
    public Boolean isCardActivated;
    @JsonProperty("IsVirtualCardActivated")
    public Boolean isVirtualCardActivated;
    @JsonProperty("IsPhysicalCardPrinted")
    public Boolean isPhysicalCardPrinted;
    @JsonProperty("IsPinSet")
    public Boolean isPinSet;
    @JsonProperty("StatusCode")
    public String statusCode;
    @JsonProperty("CardExpiryDate")
    public String cardExpiryDate;
    @JsonProperty("CardHolderName")
    public String cardHolderName;
    @JsonProperty("CardProduct")
    public CardProduct cardProduct;
    @JsonProperty("CardPlastic")
    public CardPlastic cardPlastic;
    @JsonProperty("TotalCreditLimit")
    public String totalCreditLimit;
    @JsonProperty("AvailableLimit")
    public String availableLimit;
    @JsonProperty("UtilizedLimit")
    public String utilizedLimit;
}
