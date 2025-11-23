package uk.co.deloitte.banking.banking.chequebook.scenarios;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.banking.chequebook.api.CustomerScoreApi;
import uk.co.deloitte.banking.banking.chequebook.api.ValidateSigCapApi;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ValidateSigCapTest {
    
    @Inject
    private ValidateSigCapApi sigCapApi;
    

    @Inject
    bankingUserLogin bankingUserLogin;

    @Test
    public void testSigCapValidate() {
        TEST("Validate SigCap");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();
        var response = sigCapApi.getSinCapCode(bearerToken);
        String sigCapCode = response.get("id");
        var resp = sigCapApi.sigCapValidate(bearerToken, sigCapCode);
    }
}
