package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.utils;

import ahb.experience.onboarding.MarketPlaceUserDetails;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.util.ExperienceTestUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class ExperienceMarketPlaceUser {

    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;
    public ExperienceLoginResponse marketPlaceUserSignUp(ExperienceTestUser experienceTestUser, MarketPlaceUserDetails marketPlaceUserDetails){

        ExperienceLoginResponse experienceLoginResponse= experienceTestUserFactory.setupExperienceMarketPlaceUser(experienceTestUser,marketPlaceUserDetails.name,marketPlaceUserDetails.language,marketPlaceUserDetails.gender,marketPlaceUserDetails.dateOfBirth,experienceTestUser.getEmail(),marketPlaceUserDetails.nationality,marketPlaceUserDetails.termsAccepted,marketPlaceUserDetails.consentToMarketingCommunication,marketPlaceUserDetails.consentToPrivacyPolicy);
        return experienceLoginResponse;
    }
}
