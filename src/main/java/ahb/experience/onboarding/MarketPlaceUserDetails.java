package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketPlaceUserDetails {

        @JsonProperty("name")
        @Builder.Default
        public String name = "Abhi Jain";
        @JsonProperty("dateOfBirth")
        @Builder.Default
        public String dateOfBirth = "1988-03-30";
        @JsonProperty("language")
        @Builder.Default
        public String language = "en";
        @JsonProperty("email")
        public String email;
        @JsonProperty("termsAccepted")
        @Builder.Default
        public boolean termsAccepted = true;
        @JsonProperty("gender")
        @Builder.Default
        public String gender = "MALE";
        @JsonProperty("nationality")
        @Builder.Default
        public String nationality = "AE";
        @JsonProperty("consentToMarketingCommunication")
        @Builder.Default
        public boolean consentToMarketingCommunication = true;
        @JsonProperty("consentToPrivacyPolicy")
        @Builder.Default
        public boolean consentToPrivacyPolicy = true;
}
