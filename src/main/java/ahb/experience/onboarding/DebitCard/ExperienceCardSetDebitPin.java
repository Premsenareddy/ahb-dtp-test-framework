package ahb.experience.onboarding.DebitCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceCardSetDebitPin {

        @JsonProperty("cardExpiryDate")
        private String cardExpiryDate;
        @JsonProperty("cardNumber")
        private String cardNumber;
        @Builder.Default
        @JsonProperty("cardNumberFlag")
        private String cardNumberFlag = "M";
        @Builder.Default
        @JsonProperty("isCardActivate")
        private String isCardActivate = "N";
        @JsonProperty("lastFourDigit")
        private String lastFourDigit;
        @JsonProperty("pinBlock")
        private String pinBlock;
        @Builder.Default
        @JsonProperty("pinServiceType")
        private String pinServiceType = "G";
        @JsonProperty("relationshipId")
        private String relationshipId;
}
