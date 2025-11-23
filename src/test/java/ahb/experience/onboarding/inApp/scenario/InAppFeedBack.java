package ahb.experience.onboarding.inApp.scenario;

import ahb.experience.InApp.request.FeedbackData;
import ahb.experience.InApp.request.InAppReqBody;
import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.DocumentGeneration.DocumentFile;
import ahb.experience.onboarding.DocumentGeneration.api.DocumentsAdultApis;
import ahb.experience.onboarding.DocumentGeneration.util.DocumentGenSteps;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.childMarketPlace.utils.ChildOnboardingSteps;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceBankAccountUser;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceDebitCardSetup;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceMarketPlaceUser;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceTestUserFactory;
import ahb.experience.onboarding.inApp.api.InAppFeedbackApis;
import ahb.experience.onboarding.inApp.api.getAppFeedbackApis;
import ahb.experience.onboarding.response.Child.Child;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static ahb.experience.onboarding.childMarketPlace.data.AdultOnboardingBuilders.bankingUserOnboardingBuilder;
import static ahb.experience.onboarding.childMarketPlace.data.ChildOnboardingBuilders.childUserOnboardingData;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;

@MicronautTest
@Slf4j
@Singleton
public class InAppFeedBack {

    @Inject
    ExperienceDebitCardSetup experienceDebitCardSetup;
    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;
    @Inject
    ExperienceMarketPlaceUser experienceMarketPlaceUser;
    @Inject
    ExperienceBankAccountUser experienceBankAccountUser;

    @Inject
    InAppFeedbackApis inAppFeedbackApis;
    @Inject
    getAppFeedbackApis getAppFeedbackApis;

    @Inject
    ChildOnboardingSteps childOnboardingSteps;
    @Inject
    DocumentGenSteps documentGenSteps;

    FeedbackData feedbackData;
    InAppReqBody inAppReqBody;

    private ExperienceTestUser experienceTestUser;
    String strErrMsg = "Invalid Customer EID Status for this operation.";

    public void setupTestUser() {
        experienceTestUser = new ExperienceTestUser();
    }


    @Order(1)
    @Test
    public void sendInAppFeedback() {

        TEST("AHBDB-22461: This test is to Validate User able to send feedback and rating in app and 200 response code");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        THEN("User should be able to send the rating and comments");
        inAppFeedbackApis = inAppFeedbackApis.inAppFeedback(loginResponse.getToken().getAccessToken(),loginResponse.getToken().getUserId(),200);

        DONE();
    }


    @Order(2)
    @Test()
    public void getInAppFeedback() {

        TEST("AHBDB-22461: This test is to Validate User able to fetch feedback and rating in app and 200 response code ");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        inAppFeedbackApis = inAppFeedbackApis.inAppFeedback(loginResponse.getToken().getAccessToken(),loginResponse.getToken().getUserId(),200);
        THEN("User should be able to fetch the rating and comments");
        getAppFeedbackApis = getAppFeedbackApis.getinAppFeedback(loginResponse.getToken().getAccessToken(),loginResponse.getToken().getUserId(),200);

        DONE();
    }

    @Order(3)
    @Test()
    public void getInAppFeedbackErrCustId() {

        TEST("AHBDB-22461: This test is to Validate 400 response and error is displayed for  incorrect customer id");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        inAppFeedbackApis = inAppFeedbackApis.inAppFeedback(loginResponse.getToken().getAccessToken(),loginResponse.getToken().getUserId(),200);
        THEN("User should be able to fetch the rating and comments");
        getAppFeedbackApis = getAppFeedbackApis.getinAppFeedback(loginResponse.getToken().getAccessToken(),loginResponse.getToken().getUserId()+"-010",400);

        DONE();
    }


    @Order(4)
    @Test
    public void sendInAppFeedbackErrCustId() {

        TEST("AHBDB-22461: This test is to Validate 400 response and error is displayed for  incorrect customer id in Post In App Feedback request");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        THEN("User should be able to send the rating and comments");
        inAppFeedbackApis = inAppFeedbackApis.inAppFeedback(loginResponse.getToken().getAccessToken(),loginResponse.getToken().getUserId()+"-010",400);

        DONE();
    }

    @Order(5)
    @Test
    public void sendInAppFeedbackErrRating() {

        TEST("AHBDB-22461: This test is to Validate 400 response and error is displayed for  incorrect rating in Post In App Feedback request");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        inAppReqBody = InAppReqBody.builder().customerId(loginResponse.getToken().getUserId()).rating("10").build();
        feedbackData = FeedbackData.builder().inAppReqBody(inAppReqBody).build();
        THEN("User should be able to send the rating and comments");
        inAppFeedbackApis = inAppFeedbackApis.inAppFeedbackData(loginResponse.getToken().getAccessToken(),feedbackData,400);

        DONE();
    }

    @Order(6)
    @Test
    public void sendInAppFeedbackErrComments() {

        TEST("AHBDB-24110: This test is to Validate 400 response and error is displayed for  incorrect comments in Post In App Feedback request");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        inAppReqBody = InAppReqBody.builder().customerId(loginResponse.getToken().getUserId()).comment("!@#This is for Testing Purpose#$%^").build();
        feedbackData = FeedbackData.builder().inAppReqBody(inAppReqBody).build();
        THEN("User should be able to send the rating and comments");
        inAppFeedbackApis = inAppFeedbackApis.inAppFeedbackData(loginResponse.getToken().getAccessToken(),feedbackData,400);

        DONE();
    }

    @Order(7)
    @Test
    public void sendInAppFeedbackCommentsSpecialCharacters() {

        TEST("AHBDB-24110: This test is to Validate Allow special characters in Comments text box of feedback and 200 response code ");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        inAppReqBody = InAppReqBody.builder().customerId(loginResponse.getToken().getUserId()).comment(", . ? ! ( ) :This is for Testing Purpose, . ? ! ( ) :").build();
        feedbackData = FeedbackData.builder().inAppReqBody(inAppReqBody).build();
        THEN("User should be able to send the rating and comments");
        inAppFeedbackApis = inAppFeedbackApis.inAppFeedbackData(loginResponse.getToken().getAccessToken(),feedbackData,200);

        DONE();
    }


}

