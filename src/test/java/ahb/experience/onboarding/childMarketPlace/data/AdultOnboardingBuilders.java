package ahb.experience.onboarding.childMarketPlace.data;

import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.ExperienceAddressDetails;
import ahb.experience.onboarding.ExperienceEmploymentDetails;
import ahb.experience.onboarding.ExperienceFATCADetails;
import ahb.experience.onboarding.IDNowDocs.IDDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;

import java.util.function.Supplier;

public class AdultOnboardingBuilders {

    public static final Supplier<BankingUserOnboardingDto> bankingUserOnboardingBuilder = () -> BankingUserOnboardingDto.builder()
            .addressDetails(ExperienceAddressDetails.builder().build())
            .empDetails(ExperienceEmploymentDetails.builder().build())
            .fatcDetails(ExperienceFATCADetails.builder().build())
            .idDetails(IDDetails.builder().build())
            .taxCountries(TaxCountries.builder().build())
            .build();
}
