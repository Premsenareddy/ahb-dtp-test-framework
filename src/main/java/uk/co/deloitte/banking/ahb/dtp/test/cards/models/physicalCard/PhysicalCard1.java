package uk.co.deloitte.banking.ahb.dtp.test.cards.models.physicalCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PhysicalCard1 {

    @JsonProperty("CardNumber")
    private String cardNumber;
    @JsonProperty("LastFourDigits")
    private String lastFourDigits;
    @JsonProperty("CardNumberFlag")
    private String cardNumberFlag;
    @JsonProperty("RecipientName")
    private String recipientName;
    @JsonProperty("PhoneNumber")
    private String phoneNumber;
    @JsonProperty("PostalAddress")
    private OBPostalAddress6 obPostalAddress6;
    @JsonProperty("IBAN")
    private String iban;
    @JsonProperty("AWBRef")
    private String awbRef;
    @JsonProperty("DTPReference")
    private String dtpReference;

}
