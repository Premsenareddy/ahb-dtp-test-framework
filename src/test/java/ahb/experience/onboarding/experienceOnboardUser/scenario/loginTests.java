package ahb.experience.onboarding.experienceOnboardUser.scenario;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.InvalidAgeGroup;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

//import org.hibernate.annotations.Check;


@MicronautTest
@Slf4j
@Singleton
public class loginTests {

    @Inject
    ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin bankingUserLogin;

    @Inject
    AuthConfiguration authConfiguration;


    @Test
    public void positive_case_Login() {
        TEST("Validate login for a banking user");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();
        THEN("I get the access token in the response and status code is 200");
        DONE();
    }
}
