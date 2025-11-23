package ahb.experience.onboarding;

import ahb.experience.onboarding.IDNowDocs.IDDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import ahb.experience.onboarding.request.child.ChildDetailsReqBody;
import ahb.experience.onboarding.request.child.ChildOTPReqBody;
import ahb.experience.onboarding.request.misc.CustomerServiceReqBody;
import ahb.experience.onboarding.request.misc.PrivilegedReqBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankingUserOnboardingDto {
//    @Builder.Default
//    private ExperienceTestUser experienceTestUser = ExperienceTestUser.builder().build();
    @Builder.Default
    private MarketPlaceUserDetails marketPlaceUserDetails = MarketPlaceUserDetails.builder().build();

    private IDDetails idDetails;
    private ExperienceAddressDetails addressDetails;
    private ExperienceEmploymentDetails empDetails;
    private ExperienceFATCADetails fatcDetails;
    private TaxCountries taxCountries;
    private PrivilegedReqBody privilegedReqBody;
    private CustomerServiceReqBody customerServiceReqBody;

    @Builder.Default
    public ChildDetailsReqBody childDetailsReqBody = ChildDetailsReqBody.builder()
            .language("en")
            .name("Narnia")
            .email("moogliinjungle668@test.com")
            .dateOfBirth("2018-03-30")
            .build();
    public ChildOTPReqBody childOTPReqBody;
}
