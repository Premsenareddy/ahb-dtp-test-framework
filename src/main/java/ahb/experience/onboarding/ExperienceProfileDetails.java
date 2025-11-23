package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceProfileDetails {

    @JsonProperty("customerId")
    public String customerId;
    @JsonProperty("dateOfBirth")
    public String dateOfBirth;
    @JsonProperty("mobileNumber")
    public String mobileNumber;
    @JsonProperty("termsAccepted")
    public boolean termsAccepted;
    @JsonProperty("name")
    public String name;
    @JsonProperty("fullName")
    public String fullName;
    @JsonProperty("gender")
    public String gender;
    @JsonProperty("nationality")
    public String nationality;
    @JsonProperty("countryOfBirth")
    public String countryOfBirth;
    @JsonProperty("cityOfBirth")
    public String cityOfBirth;
    @JsonProperty("language")
    public String language;
    @JsonProperty("email")
    public String email;
    @JsonProperty("emailState")
    public String emailState;
    @JsonProperty("address")
    public ExperienceAddressDetails address;
    @JsonProperty("customerState")
    public String customerState;
    @JsonProperty("cif")
    public String cif;
    @JsonProperty("customerType")
    public String customerType;
    @JsonProperty("age")
    public Integer age;
    @JsonProperty("customerStatus")
    public Object customerStatus;
    @JsonProperty("customerStatusReason")
    public Object customerStatusReason;
    @JsonProperty("onboardedBy")
    public Object onboardedBy;
    @JsonProperty("onboarderRole")
    public String onboarderRole;
    @JsonProperty("ageGroup")
    public String ageGroup;
    @JsonProperty("consentToPrivacyPolicy")
    public boolean consentToPrivacyPolicy;
    @JsonProperty("consentToMarketingCommunication")
    public boolean consentToMarketingCommunication;
    @JsonProperty("consentToBankingTerms")
    public boolean consentToBankingTerms;

}
