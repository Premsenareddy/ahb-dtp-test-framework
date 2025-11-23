package ahb.experience.onboarding.experienceOnboardUser.utils;

import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.util.ExperienceTestUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

@Slf4j
@Singleton
public class ExperienceMarketPlaceUser {

    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;
    public ExperienceLoginResponse marketPlaceUserSignUp(ExperienceTestUser experienceTestUser){
        String name = "TestUser";
        String dateOfBirth = "2000-01-01";
        String language = "en";
        String email = experienceTestUser.getEmail();
        String gender = "MALE";
        String nationality = "AE";
        boolean termsAccepted = true;
        boolean consentToMarketingCommunication = true;
        boolean consentToPrivacyPolicy = true;

        ExperienceLoginResponse experienceLoginResponse= experienceTestUserFactory.setupExperienceMarketPlaceUser(experienceTestUser,name,language,gender,dateOfBirth,email,nationality,termsAccepted,consentToMarketingCommunication,consentToPrivacyPolicy);
        return experienceLoginResponse;
    }
}
