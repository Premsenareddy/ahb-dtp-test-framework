package ahb.experience.onboarding.updateProfileManagement.scenario;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin_Para;
import ahb.experience.onboarding.updateProfileManagement.api.updatePreferredName;
import ahb.experience.onboarding.updateProfileManagement.api.getProfile;
import ahb.experience.onboarding.ExperienceProfileDetails;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import ahb.experience.onboarding.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


//import org.hibernate.annotations.Check;


@MicronautTest
@Slf4j
@Singleton
public class updatePreferredNameTest {
    String jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFDWW5Xcm42S3RKamdTMFkxODUwcWFidEVLTzhzTlNndllYMkUySDkwSCtpQUlnTFVzNXJUcW5LajdLVDRPb1wvclI2VnpGUGpRWnVhaCtnVkFrV1NTXC81WFg0PSJ9.Nj5XXWhPwXN-V6f8iNzIw0wGmohsk40Qi12UoFeE8iELS-2tAOQnefLvcRS4l74fjH50y_fGlG6dnBl6d0ag9A";
    String deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD532";
    String mobileNumber="+971501823031";
    String passcode="d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
    String newPreferredName = "";
    String bearerToken="";
    String expValidation = "";

    @Inject
    bankingUserLogin_Para bankingUserLogin;

    @Inject
    updatePreferredName updatePreferredName;

    @Inject
    getProfile getProfile;

    static String getAlphaString(int size)
    {String AlphaChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder alpha = new StringBuilder();
        Random rnd = new Random();
        while (alpha.length() < size) { // length of the random string.
            int index = (int) (rnd.nextFloat() * AlphaChars.length());
            alpha.append(AlphaChars.charAt(index));
        }
        String alphaStr = alpha.toString();
        return alphaStr;
    }

    /***
     * This test is to validate market user can update preferred name
     */
    @Order(1)
    @Test
    public void validateSuccessStatusCode_MarketUser_Positive() {
        newPreferredName = "abc-p's Test"+getAlphaString(3);
        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with valid value");

        int responseStatusCode = updatePreferredName.getUpdateNameStatusCode(bearerToken,newPreferredName);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==204);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Preferred name is incorrect. Expected: "+newPreferredName+", Actual: "+newUserDetails.getName()+"'", newUserDetails.getName().equalsIgnoreCase(newPreferredName));
        DONE();
    }

    /***
     * This test is to check validations on edit name field for min and max chars
     */
    @Order(2)
    @Test
    public void verifyMinAndMaxCharValidation() {
        String[] newPreferredName = {"a","testqwertyuioplkjhGFDSAzxcvBNMC"};
        expValidation = "size must be between 2 and 30";

        TEST("AHBDB-17370 - API | Validate mimimum, maximum required characters");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with invalid value");
        for(String strPreferredValue:newPreferredName){
            Map<String, Object> newPrefNamePayLoad = new HashMap<>();
            newPrefNamePayLoad.put("preferredName", strPreferredValue);
            ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newPrefNamePayLoad);
            THEN("User verifies the min and max char validation with invalid value");
            assertNotNull(val);
            Errors err = val.getErrors();
            Assert.assertTrue("Incorrect validation, Expected: "+expValidation, err.getPreferredName().equalsIgnoreCase(expValidation));
        }
        DONE();
    }

    /***
     * This test is to check validations on edit name field for special characters
     */
    @Order(3)
    @Test
    public void verifyNumericAndSpecialCharValidation() {
        String[] newPreferredName = {"test#","test123"};
        expValidation = "Only alphabets [a-z] [A-Z], NO numbers, no special characters";
        TEST("AHBDB-17370 - API | Validate special,numeric required characters");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with invalid value");
        for(String strPreferredValue:newPreferredName){
            Map<String, Object> newAddressPayLoad = new HashMap<>();
            newAddressPayLoad.put("preferredName", strPreferredValue);
            ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newAddressPayLoad);
            THEN("User verifies char validation with special and numeric value");
            assertNotNull(val);
            Errors err = val.getErrors();
            Assert.assertTrue("Incorrect validation, Expected: "+expValidation, err.getPreferredName().equalsIgnoreCase(expValidation));
        }
        DONE();
    }

    /***
     * This test is to validate bank user can update preferred name
     */
    @Order(4)
    @Test
    public void validateSuccessStatusCode_BankUser_ACCOUNT_CARD_ACTIVATION_Positive() {
        newPreferredName = "abc-p's Test"+getAlphaString(3);

        deviceId="E8B24EBF-55AE-466A-A47C-D98808B4E0D8";
        mobileNumber="+971501823010";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVlDSVFEM1dhMm1uYVBzWXhqaHEva3hKU0FSNndlNnV0bS9VWDFvcmNZS1dJVnk2UUloQU1YRXFsNnpqTkREZFI1N3F5R2YrSStoU1I0RHZCZEt0N0RYQ1NKUTRxV0oifQ.5PYZc6RzdYVocxCpXhSmolCs8frmzJyUnUR6HEemrO4lrSasoMBdNRs5bipayJN3jmDs42ZKmoJDwxVOX0Sngw";

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with valid value");

        int responseStatusCode = updatePreferredName.getUpdateNameStatusCode(bearerToken,newPreferredName);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==204);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Preferred name is incorrect. Expected: "+newPreferredName+", Actual: "+newUserDetails.getName()+"'", newUserDetails.getName().equalsIgnoreCase(newPreferredName));
        DONE();
    }

    /***
     * This test is to validate bank user can update preferred name even if customer state is ACCOUNT_CREATED
     */
    @Order(5)
    @Test
    public void validateSuccessStatusCode_BankUser_ACCOUNT_CREATED_Positive() {
        newPreferredName = "abc-p's Test"+getAlphaString(3);

        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSUJDUlg3VUl4V084NDYyYmhyUEtwYm84c3hFamVJQURJNHhOUmRDcWsyQWVBaUVBeWlRWTljWVFiamw4TE5zY081K0lLMUZHNW5FdUxBekZRenk4V1ZhSXBlaz0ifQ.Dojas7oXgLFb1TnstrlPyj3zV5O72z9U3LdGemEcFuNiyAuymRHCjVu5BzcjVpG8eW6oDejaF03YPBkgGcR05Q";
        deviceId="1e3a3c70-c1641201159620cffb-4b57-90ea-43627203968b";
        mobileNumber="+971501823586";

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with valid value");

        int responseStatusCode = updatePreferredName.getUpdateNameStatusCode(bearerToken,newPreferredName);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==204);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Preferred name is incorrect. Expected: "+newPreferredName+", Actual: "+newUserDetails.getName()+"'", newUserDetails.getName().equalsIgnoreCase(newPreferredName));
        DONE();
    }
}
