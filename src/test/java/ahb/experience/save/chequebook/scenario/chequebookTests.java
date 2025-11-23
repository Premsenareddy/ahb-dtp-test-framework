package ahb.experience.save.chequebook.scenario;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin;
import ahb.experience.save.checkbook.CheckPINandEmiratesIdSchema;
import ahb.experience.save.chequebook.api.CheckUserPINandEmiratesIDExp;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@Slf4j
@Singleton
public class chequebookTests {

    @Inject
    CheckUserPINandEmiratesIDExp checkUserPINandEmiratesIDExp;

    @Inject
    bankingUserLogin bankingUserLogin;

    @Test
    public void checkuserpinandemiratesIdApi() throws JSONException {
        TEST("Check User PIN and Emirates Id");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("And I am trying to validate if User Pin and Emirates id is set");
        CheckPINandEmiratesIdSchema schemaValidation = checkUserPINandEmiratesIDExp.checkingUserPinandEmiratesid(bearerToken);
        THEN("Validating the response of the code");
        Assert.assertEquals("Pin is already set:",schemaValidation.geteIdValidationPassed(), false);
        Assert.assertEquals("Emirates id Expiration status:",schemaValidation.geteIdValidationPassed(), false);
        DONE();
    }
}

