package ahb.experience.onboarding.updateProfileManagement.scenario;

import ahb.experience.onboarding.ExperienceProfileDetails;
import ahb.experience.onboarding.EmailGenerateToken;
import ahb.experience.onboarding.ExperienceErrValidations;
import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin_Para;
import ahb.experience.onboarding.updateProfileManagement.api.*;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@Slf4j
@Singleton
public class updateEmailTest {

    @Inject
    bankingUserLogin_Para bankingUserLogin;

    @Inject
    updateEmailAddress updateEmailAddress;

    @Inject
    getProfile getProfile;

    public static int getRandom(int max){ return (int) (Math.random()*max); }

    String jwsSignature="";
    String deviceId="";
    String mobileNumber="";
    String passcode="d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
    String newEmailAddress = "";
    String expValidation="";
    String bearerToken="";

    /**
     * This test is to validate market user can update email address when email state is "VERIFIED"
     */
    @Order(1)
    @Test
    public void validateSuccessStatusCode_MarketUser_EMAILVERIFIED_Positive() {
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFDT0d6RjNWYU4wbmJ3SERoSE9iMHhVTjEwSU01Vkp2T2lFM1R5cFA4VDljUUlnYis0RnN5aG1NbWhUTml4aGdNbHhpOGR0bllGY3E3S3pMYlFKSklkUzVDMD0ifQ.CAxw80vHimcD8zaY-IYtWgVQbJ3i6DBu4Ji9TTtAg7PDbzCmUhPD8nKGzkdII-yOtYTx7OaPCkj9AcNNU7PEtg";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD523";
        mobileNumber="+971501823022";

        String newEmailAddress = "email"+String.valueOf(getRandom(1000)+"@gmail.com");
        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with valid value");
        int updateEmailStatusCode = updateEmailAddress.updateEmail(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Incorrect Status code", updateEmailStatusCode==204);

        EmailGenerateToken emailGenerateToken = updateEmailAddress.generateEmailToken(bearerToken,newEmailAddress);
        assertNotNull(emailGenerateToken);
        int responseStatusCode = updateEmailAddress.validateEmail(bearerToken,emailGenerateToken.getEmail(),emailGenerateToken.getToken());

        Assert.assertTrue("Incorrect Status code", responseStatusCode==200);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Preferred name is incorrect. Expected: "+newEmailAddress+", Actual: "+newUserDetails.getEmail()+"'", newUserDetails.getEmail().equalsIgnoreCase(newEmailAddress));
        DONE();

    }

    /**
     * This test is to validate market user can NOT update email address if email is NOT VERIFIED
     */
    @Order(2)
    @Test
    public void validateSuccessStatusCode_MarketUser_EMAILNOTVERIFIED_Negative() {
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFDWFgrYlY1MVJUS2RzXC95bzlTVERPWnJ2RlwvWlR4K2VLQ2hMMFRMUVNwVW9BSWdLU1p6T04zWEhFc0l2UUc1Zm9wamNzazRsQ1k3Yyt3ZDBYODE3VGt6TGR3PSJ9.mIp75eQ6MWQNErSBsg4CTx7WH7nAeu-FJLvBh_A9SDQby0uk1yHwbCjSe4TwBkt7ozGMoSxDQO0msWC-bjtSEg";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD524";
        mobileNumber="+971501823023";

        newEmailAddress = "email"+String.valueOf(getRandom(1000)+"@gmail.com");
        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with valid value");
        int updateEmailStatusCode = updateEmailAddress.updateEmail(bearerToken,jwsSignature,newEmailAddress);
        System.out.println("Status "+updateEmailStatusCode);
        Assert.assertTrue("Incorrect Status code", updateEmailStatusCode==500);
        DONE();
    }

    /**
     * This test is to validate market user can NOT update email address associated with other user
     */
    @Order(3)
    @Test
    public void validateFailureStatusCode_MarketUser_EXISTINGEMAILVERIFIED_Negative() {
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFDT0d6RjNWYU4wbmJ3SERoSE9iMHhVTjEwSU01Vkp2T2lFM1R5cFA4VDljUUlnYis0RnN5aG1NbWhUTml4aGdNbHhpOGR0bllGY3E3S3pMYlFKSklkUzVDMD0ifQ.CAxw80vHimcD8zaY-IYtWgVQbJ3i6DBu4Ji9TTtAg7PDbzCmUhPD8nKGzkdII-yOtYTx7OaPCkj9AcNNU7PEtg";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD523";
        mobileNumber="+971501823022";
        newEmailAddress = "abhi@gmail.com";
        expValidation="Customer email already exist.";

        Map<String, Object> newEmailPayLoad = new HashMap<>();
        newEmailPayLoad.put("email", "newEmailAddress");

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User checks validation is applied whether new email can be created");
        int updateEmailStatusCode = updateEmailAddress.updateEmail(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Incorrect Status code", updateEmailStatusCode==500);

        ExperienceErrValidations val =  updateEmailAddress.getUpdateEmailValidationErr(bearerToken,jwsSignature,newEmailAddress);
        Assert.assertTrue("Validation is NOT correct. Expected: "+expValidation+", Actual: "+val.getMessage()+"'", expValidation.equalsIgnoreCase(val.getMessage()));
        DONE();
    }

    /**
     * This test is to validate bank user can update email address when customer state is "ACCOUNT_CARD_ACTIVATION"
     */
    @Order(4)
    @Test
    public void validateSuccessStatusCode_BankUser_ACCOUNT_CARD_ACTIVATION_Positive() {
        deviceId="E8B24EBF-55AE-466A-A47C-D98808B4E0D8";
        mobileNumber="+971501823010";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVlDSVFEM1dhMm1uYVBzWXhqaHEva3hKU0FSNndlNnV0bS9VWDFvcmNZS1dJVnk2UUloQU1YRXFsNnpqTkREZFI1N3F5R2YrSStoU1I0RHZCZEt0N0RYQ1NKUTRxV0oifQ.5PYZc6RzdYVocxCpXhSmolCs8frmzJyUnUR6HEemrO4lrSasoMBdNRs5bipayJN3jmDs42ZKmoJDwxVOX0Sngw";

        newEmailAddress = "email"+String.valueOf(getRandom(1000)+"@gmail.com");
        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the email address with valid value");
        int updateEmailStatusCode = updateEmailAddress.updateEmail(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Incorrect Status code", updateEmailStatusCode==204);

        EmailGenerateToken emailGenerateToken = updateEmailAddress.generateEmailToken(bearerToken,newEmailAddress);
        assertNotNull(emailGenerateToken);
        int responseStatusCode = updateEmailAddress.validateEmail(bearerToken,emailGenerateToken.getEmail(),emailGenerateToken.getToken());

        Assert.assertTrue("Incorrect Status code", responseStatusCode==200);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        Assert.assertTrue("Email address is incorrect. Expected: "+newEmailAddress+", Actual: "+newUserDetails.getEmail()+"'", newUserDetails.getEmail().equalsIgnoreCase(newEmailAddress));
        DONE();
    }

    /**
     * This test is to validate bank user can update email address when customer state is "ACCOUNT_VERIFIED"
     */
    @Order(5)
    @Test
    public void validateSuccessStatusCode_BankUser_ACCOUNT_VERIFIED_Positive() {
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD554";
        mobileNumber="+971501823053";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVlDSVFDYmVHNldSXC9CSXFMcG9udTVkcmlHbGpkT3JnczQ1a2M3Z1RYNVpIdFFpamdJaEFLZVprVERZY3pjY2UzeEp5eVBlVGpLZjNLYTFBaXBoSXRTSjluMFhpbWJwIn0.EJobRNOYVZ3M8Vp3jVPu7SOkdoyBn8oRWvAZwgx4RvWEUeIh2KOrFUbkjiXHZz1P-FgUki0n0heF0G6UUsp4UQ";

        newEmailAddress = "email"+String.valueOf(getRandom(1000)+"@gmail.com");
        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the email address with valid value");
        int updateEmailStatusCode = updateEmailAddress.updateEmail(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Incorrect Status code", updateEmailStatusCode==204);

        EmailGenerateToken emailGenerateToken = updateEmailAddress.generateEmailToken(bearerToken,newEmailAddress);
        assertNotNull(emailGenerateToken);
        int responseStatusCode = updateEmailAddress.validateEmail(bearerToken,emailGenerateToken.getEmail(),emailGenerateToken.getToken());

        Assert.assertTrue("Incorrect Status code", responseStatusCode==200);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        Assert.assertTrue("Email address is incorrect. Expected: "+newEmailAddress+", Actual: "+newUserDetails.getEmail()+"'", newUserDetails.getEmail().equalsIgnoreCase(newEmailAddress));
        DONE();
    }

    /**
     * This test is to validate bank user can NOT update email address with ACCOUNT_CREATED as customer state
     */
    @Order(6)
    @Test
    public void validateFailureStatusCode_BankUser_ACCOUNTCREATED_Negative() {
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSUJDUlg3VUl4V084NDYyYmhyUEtwYm84c3hFamVJQURJNHhOUmRDcWsyQWVBaUVBeWlRWTljWVFiamw4TE5zY081K0lLMUZHNW5FdUxBekZRenk4V1ZhSXBlaz0ifQ.Dojas7oXgLFb1TnstrlPyj3zV5O72z9U3LdGemEcFuNiyAuymRHCjVu5BzcjVpG8eW6oDejaF03YPBkgGcR05Q";
        deviceId="1e3a3c70-c1641201159620cffb-4b57-90ea-43627203968b";
        mobileNumber="+971501823586";
        newEmailAddress = "email"+String.valueOf(getRandom(1000)+"@gmail.com");
        expValidation="Invalid Customer State for this operation.";

        Map<String, Object> newEmailPayLoad = new HashMap<>();
        newEmailPayLoad.put("email", "newEmailAddress");

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User checks validation is applied whether new email can be created");
        int updateEmailStatusCode = updateEmailAddress.updateEmail(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Incorrect Status code", updateEmailStatusCode==500);

        ExperienceErrValidations val =  updateEmailAddress.getUpdateEmailValidationErr(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Validation is NOT correct. Expected: "+expValidation+", Actual: "+val.getMessage()+"'", expValidation.equalsIgnoreCase(val.getMessage()));
        DONE();
    }

    /**
     * This test is to validate Market user can NOT update email address with invalid format
     */
    @Order(7)
    @Test
    public void validateFailureStatusCode_MarketUser_InvalidEmailFormat_Negative() {
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVlDSVFEMFp4U0ppSkIrT2VHTWVvSkdCZ0xUQVBTWjN5UGZYbitsOGg3QlJ3bGtYQUloQUorM1NTbGJ1R2p4WFErd0hlVDdlTDBaa3lWdlhWZ1NVa0h0WFRrOEtZTloifQ.hVL6tE5uBAynwcMSJfT7CdXEFOjt70hudTR_mFVXHPUMDCdUc6ROvy2yPvgIMKA8guJBQ4Hib-xR1tjKJyWTqg";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD556";
        mobileNumber="+971501823055";
        newEmailAddress = "email"+String.valueOf(getRandom(1000));
        expValidation="must be a well-formed email address";

        Map<String, Object> newEmailPayLoad = new HashMap<>();
        newEmailPayLoad.put("email", "newEmailAddress");

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        ExperienceErrValidations val =  updateEmailAddress.getUpdateEmailValidationErr(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Validation is NOT correct. Expected: "+expValidation+", Actual: "+val.getMessage()+"'", val.getMessage().toLowerCase().contains(expValidation.toLowerCase()));
        DONE();
    }

    /**
     * This test is to validate Market user can NOT update email address with invalid format
     */
    @Order(8)
    @Test
    public void validateFailureStatusCode_MarketUser_SAMEEMAIL_Negative() {
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFETlBlZ2RqRXJiY1BlMndrajhJT0tCQTFDWmtQQVVvcmxubEdOczVBdzlaZ0lnVW1BdE1xNUNwVnBCYlordmxxaEFZeGVTaGE4bUxjN09BY3Y5ZnZJQkJPZz0ifQ.51EpTzo0pDxaGW0dB0JSCbSHpxXhlpOeTdrQ1ow2-CFf7JP99dcWni5pgpmxm8P6262nOZ1i0F8GrUEte9_CrA";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5ED411";
        mobileNumber="+971501823538";
        expValidation="Customer email already exist.";

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);
        newEmailAddress = newUserDetails.getEmail();
        Map<String, Object> newEmailPayLoad = new HashMap<>();
        newEmailPayLoad.put("email", "newEmailAddress");
        ExperienceErrValidations val =  updateEmailAddress.getUpdateEmailValidationErr(bearerToken,jwsSignature,newEmailAddress);

        Assert.assertTrue("Validation is NOT correct. Expected: "+expValidation+", Actual: "+val.getMessage()+"'", val.getMessage().toLowerCase().contains(expValidation.toLowerCase()));
        DONE();
    }
}
