package ahb.experience.creditCard;

import ahb.experience.onboarding.DebitCard.LinkedAccount;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Introspected
public class ExpGetCreditCardContent {

    @JsonProperty("cardNo")
    private String cardNo;
    @JsonProperty("cardNoEncrypt")
    private String cardNoEncrypt;
    @JsonProperty("cardNoFlag")
    private String cardNoFlag;
    @JsonProperty("cardStatus")
    private String cardStatus;
    @JsonProperty("dueDate")
    private String dueDate;
    @JsonProperty("deliveryCardFlag")
    private String deliveryCardFlag;
    @JsonProperty("activationFlag")
    private String activationFlag;
    @JsonProperty("physicalCardActivationFlag")
    private String physicalCardActivationFlag;
    @JsonProperty("statusCode")
    private String statusCode;
    @JsonProperty("expiryDate")
    private String expiryDate;
    @JsonProperty("cardProduct")
    private String cardProduct;
    @JsonProperty("cardPlastic")
    private String cardPlastic;
    @JsonProperty("cardHolderName")
    private String cardHolderName;
    @JsonProperty("cardProductName")
    private String cardProductName;
    @JsonProperty("cardPlasticName")
    private String cardPlasticName;
    @JsonProperty("linkedAccounts")
    private ArrayList<LinkedAccount> linkedAccounts;
    @JsonProperty("physicalCardPrintStatus")
    private String physicalCardPrintStatus;
    @JsonProperty("cvvNumber")
    private String cvvNumber;
    @JsonProperty("availableBalance")
    private String availableBalance;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("deliveryDate")
    private Date deliveryDate;
    @JsonProperty("onlinePayment")
    private String onlinePayment;
    @JsonProperty("internationalPayment")
    private String internationalPayment;
    @JsonProperty("isCardFrozen")
    private String isCardFrozen;
    @JsonProperty("totalCreditLimit")
    public int totalCreditLimit;
    @JsonProperty("availableCreditLimit")
    public double availableCreditLimit;
    @JsonProperty("spendCreditLimit")
    public double spendCreditLimit;
}
