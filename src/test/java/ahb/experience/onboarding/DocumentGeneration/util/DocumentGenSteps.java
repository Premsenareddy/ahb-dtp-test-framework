package ahb.experience.onboarding.DocumentGeneration.util;

import ahb.experience.onboarding.ApplicationType;
import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.childMarketPlace.api.ChildApis;
import ahb.experience.onboarding.childMarketPlace.api.Misc;
import ahb.experience.onboarding.childMarketPlace.api.PrivilegesApi;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceBankAccountUser;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceDebitCardSetup;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceMarketPlaceUser;
import ahb.experience.onboarding.response.Child.Child;
import ahb.experience.onboarding.util.ExperienceTestUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static ahb.experience.onboarding.childMarketPlace.data.AdultOnboardingBuilders.bankingUserOnboardingBuilder;
@Slf4j
@Singleton
public class DocumentGenSteps {

    public Map setQueryParam(String type, String relationValue){
        Map<String, String> queryParams = new HashMap<>();
        if(type.equalsIgnoreCase("kid")){
            queryParams.put("relationshipId",relationValue);
        }
        return queryParams;

    }


}
