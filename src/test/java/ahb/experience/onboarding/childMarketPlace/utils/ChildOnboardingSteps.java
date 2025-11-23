package ahb.experience.onboarding.childMarketPlace.utils;

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

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static ahb.experience.onboarding.childMarketPlace.data.AdultOnboardingBuilders.bankingUserOnboardingBuilder;

public class ChildOnboardingSteps {

    @Inject
    Misc misc;

    @Inject
    PrivilegesApi privilegesApi;

    @Inject
    ChildApis childApis;

    @Inject
    ExperienceBankAccountUser experienceBankAccountUser;

    @Inject
    ExperienceDebitCardSetup experienceDebitCardSetup;
    @Inject
    ExperienceMarketPlaceUser experienceMarketPlaceUser;

    public Child childMarketCreateAndLogin(String accessToken, BankingUserOnboardingDto childOnboardingDTO) {
        childApis.saveChild(childOnboardingDTO.childDetailsReqBody, accessToken);
        misc.QRCode(accessToken);

        childApis.getChildOTP(childOnboardingDTO.childOTPReqBody, accessToken)
                .getChildKeys()
                .childDeviceRegistration()
                .childSignature()
                .childSavePassCode()
                .childSignatureUserID()
                .childLogin();

        return childApis.child;
    }


    public void childBankOnboarding(String accessToken, BankingUserOnboardingDto onBoardingDetails) {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "KID");
        queryParams.put("relationshipId", childApis.child.getRelationshipId());

        experienceBankAccountUser.idNowFlowFor(onBoardingDetails, onBoardingDetails.getChildDetailsReqBody().getEmail()
                , ApplicationType.KID, queryParams);
        experienceBankAccountUser.saveDetailsOnReview(onBoardingDetails, queryParams);
        experienceBankAccountUser.saveResidentailDetails(onBoardingDetails, queryParams);
        experienceBankAccountUser.saveFACTCADetails(onBoardingDetails, queryParams);
        experienceBankAccountUser.saveTaxDetails(onBoardingDetails, queryParams);

        privilegesApi.assignPrivileges(accessToken, onBoardingDetails.getPrivilegedReqBody());
        misc.customerService(onBoardingDetails.getCustomerServiceReqBody());

        experienceBankAccountUser.createBankAccount(queryParams);
    }

    public void childDebitCardSetup() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "KID");
        queryParams.put("relationshipId", childApis.child.getRelationshipId());

        experienceDebitCardSetup.setUpDebitCard(queryParams,"Success","pass");
    }

    public ExperienceLoginResponse onboardAdultBankUser(ExperienceTestUser experienceTestUser) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "ADULT");

        experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        ExperienceLoginResponse response = experienceBankAccountUser
                .bankAdultUserOnboard(bankingUserOnboardingBuilder.get(), experienceTestUser, queryParams);
        experienceDebitCardSetup.setUpDebitCard(queryParams,"Success","pass");

        return response;
    }
}
