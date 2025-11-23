package ahb.experience.onboarding.auth.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceProfile {
    @JsonProperty("customerId")
    @Schema(name = "customerId")
    public String customerId;
    @JsonProperty("dateOfBirth")
    @Schema(name = "dateOfBirth")
    public String dateOfBirth;
    @JsonProperty("mobileNumber")
    @Schema(name = "mobileNumber")
    public String mobileNumber;
    @JsonProperty("termsAccepted")
    @Schema(name = "termsAccepted")
    public boolean termsAccepted;
    @JsonProperty("name")
    @Schema(name = "name")
    public String name;
    @JsonProperty("fullName")
    @Schema(name = "fullName")
    public Object fullName;
    @JsonProperty("gender")
    @Schema(name = "gender")
    public String gender;
    @JsonProperty("nationality")
    @Schema(name = "nationality")
    public String nationality;
    @JsonProperty("countryOfBirth")
    @Schema(name = "countryOfBirth")
    public Object countryOfBirth;
    @JsonProperty("cityOfBirth")
    @Schema(name = "cityOfBirth")
    public Object cityOfBirth;
    @JsonProperty("language")
    @Schema(name = "language")
    public String language;
    @JsonProperty("email")
    @Schema(name = "email")
    public String email;
    @JsonProperty("emailState")
    @Schema(name = "emailState")
    public String emailState;
    @JsonProperty("address")
    @Schema(name = "address")
    public Object address;
    @JsonProperty("customerState")
    @Schema(name = "customerState")
    public String customerState;
    @JsonProperty("cif")
    @Schema(name = "cif")
    public Object cif;
    @JsonProperty("customerType")
    @Schema(name = "customerType")
    public String customerType;
    @JsonProperty("age")
    @Schema(name = "age")
    public int age;
    @JsonProperty("customerStatus")
    @Schema(name = "customerStatus")
    public Object customerStatus;
    @JsonProperty("customerStatusReason")
    @Schema(name = "customerStatusReason")
    public Object customerStatusReason;
    @JsonProperty("onboardedBy")
    @Schema(name = "onboardedBy")
    public Object onboardedBy;
    @JsonProperty("onboarderRole")
    @Schema(name = "onboarderRole")
    public String onboarderRole;
    @JsonProperty("ageGroup")
    @Schema(name = "ageGroup")
    public String ageGroup;
    @JsonProperty("consentToPrivacyPolicy")
    @Schema(name = "consentToPrivacyPolicy")
    public boolean consentToPrivacyPolicy;
    @JsonProperty("consentToMarketingCommunication")
    @Schema(name = "consentToMarketingCommunication")
    public boolean consentToMarketingCommunication;
    @JsonProperty("consentToBankingTerms")
    @Schema(name = "consentToBankingTerms")
    public boolean consentToBankingTerms;
}

