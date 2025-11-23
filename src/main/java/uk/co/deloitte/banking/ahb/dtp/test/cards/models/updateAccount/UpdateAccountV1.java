package uk.co.deloitte.banking.ahb.dtp.test.cards.models.updateAccount;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateAccountV1 {
    @JsonProperty("CardNumber")
    private String cardNumber;
    @JsonProperty("LastFourDigits")
    private String lastFourDigits;
    @JsonProperty("CardNumberFlag")
    private String cardNumberFlag;
    @JsonProperty("AccountNumber")
    private String accountNumber;
    @JsonProperty("AccountType")
    private String accountType;

}
