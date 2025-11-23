package ahb.experience.onboarding.experienceOnboardUser.scenario;

import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.ExperienceAddressDetails;
import ahb.experience.onboarding.ExperienceEmploymentDetails;
import ahb.experience.onboarding.ExperienceFATCADetails;
import ahb.experience.onboarding.IDNowDocs.IDDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceMarketPlaceUser;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceTestUserFactory;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

//import org.hibernate.annotations.Check;


@MicronautTest
@Slf4j
@Singleton
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class uaepassTests {

    @Inject
    ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin bankingUserLogin;

    @Inject
    ahb.experience.onboarding.experienceOnboardUser.api.uaepassUserLogin uaepassUserLogin;

    @Inject
    AuthConfiguration authConfiguration;

    @Inject
    ExperienceMarketPlaceUser experienceMarketPlaceUser;

    @Inject
    private EnvUtils envUtils;

    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;


    ExperienceLoginResponse loginResponse;

    private ExperienceTestUser experienceTestUser;



    public void setupTestUser() {
        envUtils.ignoreTestInEnv("Feature not deployed yet on SIT", Environments.SIT);
        experienceTestUser = new ExperienceTestUser();
        loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
    }


    @BeforeAll
    public void uaepasstests() {

        TEST("AHBDB-XXXXX: This test is to Validate User able to send feedback and rating in app");
        setupTestUser();
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        THEN("User should be able to send the rating and comments");
        DONE();
    }




    @Order(1)
    @Test
    public void scan_Passport() {
        TEST("Validate user able to scan passport through uaepass webhook");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to scan the passport for  that user");
        String bearerToken = uaepassUserLogin.UaepassAccessToken();
        uaepassUserLogin.scanPassport(bearerToken);
        THEN("I get the  json response and status code is 201");

        DONE();
    }

    @Order(2)
    @Test
    public void scan_EID() {
        TEST("Validate user able to scan eid through uaepass webhook");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to scan the eid for that user");
        String bearerToken = uaepassUserLogin.UaepassAccessToken();
        uaepassUserLogin.scanEID(bearerToken);
        THEN("I get the json  response and status code is 201");

        DONE();
    }

    @Order(3)
    @Test
    public void update_customerState() {
        TEST("Validate user able to update the customerstate to uaepass_success");

        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to update the customer state for  that user");
        GIVEN("User has created a marketplace user and logged in to the AHB app");
        String strCustomerState = "UAEPASS_SUCCESS";
        experienceTestUserFactory.updateCustomerState(strCustomerState,200);
        uaepassUserLogin.uaepasspatchcustomerState(loginResponse.getToken().getUserId());
        THEN("I get the json response and status code is 200");

        DONE();
    }

    @Order(4)
    @Test
    public void update_EidStatus() {
        TEST("Verify the user able to update the eid status to valid");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to update the eid status for that user");
        uaepassUserLogin.uaepasspatcheidstatus(loginResponse.getToken().getUserId());
        THEN("I get the json response and status code is 200");

        DONE();
    }




}
