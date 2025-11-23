package ahb.experience.save.chequebook.scenario;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin;
import ahb.experience.save.chequebook.api.EligibilityCheck;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Map;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

@MicronautTest
@Slf4j
@Singleton
public class EligibilityCheckTest {
    @Inject
    EligibilityCheck eligibilityCheck;

    @Inject
    bankingUserLogin bankingUserLogin;

    @Test
    public void checkUserEligibilityApi() throws JSONException {
        TEST("Check User eligibility for cheque book");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken = bankingUserLogin.getAccessToken();
        AND("And I am trying to validate if User Pin and Emirates id is set");
        Map response = (Map) eligibilityCheck.checkEligibility(bearerToken);
        THEN("Validating the response");
        System.out.println("response = " + response);
        Assert.assertEquals(true, response.get("eligible"));
        
      //  Assert.assertEquals("CODE_NOT_ELIGIBLE", response.get("code").toString());
        DONE();
    }
}
