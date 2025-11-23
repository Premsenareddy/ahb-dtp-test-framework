package uk.co.deloitte.banking.ahb.dtp.test.cards.models;

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
public class ReadCard1DataCard {
    @JsonProperty("CardNumber")
    private String cardNumber;
    @JsonProperty("CardNumberFlag")
    private String cardNumberFlag;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("IsCardDelivered")
    private boolean cardDelivered;
    @JsonProperty("IsCardActivated")
    private boolean cardActivated;
    @JsonProperty("IsVirtualCardActivated")
    private boolean virtualCardActivated;
    @JsonProperty("IsPhysicalCardPrinted")
    private boolean physicalCardPrinted;
    @JsonProperty("StatusCode")
    private String statusCode;
    @JsonProperty("ExpiryDate")
    private String expiryDate;
    @JsonProperty("CardHolderName")
    private String cardHolderName;
    @JsonProperty("CardProduct")
    private CardProduct cardProduct;
    @JsonProperty("CardPlastic")
    private CardPlastic cardPlastic;
    @JsonProperty("LinkedAccount")
    private LinkedAccount linkedAccount;
    @JsonProperty("PhysicalCardPrintStatus")
    private String physicalCardPrintStatus;
    @JsonProperty("ChannelOfOrigin")
    private String channelOfOrigin;
    @JsonProperty("IsPinSet")
    private Boolean isPintSet;
}
