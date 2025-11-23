package ahb.experience.onboarding.updateProfileManagement.scenario;

import ahb.experience.onboarding.ExperienceProfileDetails;
import ahb.experience.onboarding.GetOTP_Setup;
import ahb.experience.onboarding.SignatureKeys_Setup;
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
import static org.junit.Assert.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

//import org.hibernate.annotations.Check;


@MicronautTest
@Slf4j
@Singleton
public class updateMobileNumberTest {

    @Inject
    bankingUserLogin_Para bankingUserLogin;

    @Inject
    updateMobileNumber updateMobileNumber;

    @Inject
    getProfile getProfile;

    @Inject
    setupUser setupUser;

    public static int getRandom(int max){ return (int) (Math.random()*max); }

    String deviceId="";
    String mobileNumber="";
    String passcode="d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
    String jwsSignature = "";
    String newMobileNumber = "";
    String bearerToken="";
    String expValidation = "";
    String xRequestID = "";

    /**
     * This test is to validate banking user can not update mobile number if EID status is "INVALID"
     */
    @Order(1)
    @Test
    public void validateFailureStatusCode_BankingUser_EIDINVALID_Negative() {
        deviceId="E8B24EBF-55AE-466A-A47C-D98808B4E0D8";
        mobileNumber="+971501823010";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVlDSVFEM1dhMm1uYVBzWXhqaHEva3hKU0FSNndlNnV0bS9VWDFvcmNZS1dJVnk2UUloQU1YRXFsNnpqTkREZFI1N3F5R2YrSStoU1I0RHZCZEt0N0RYQ1NKUTRxV0oifQ.5PYZc6RzdYVocxCpXhSmolCs8frmzJyUnUR6HEemrO4lrSasoMBdNRs5bipayJN3jmDs42ZKmoJDwxVOX0Sngw";
        expValidation = "Invalid Customer EID Status for this operation";

        newMobileNumber = "+97150182"+String.valueOf(getRandom(10000));
        Map<String, Object> newMobileNumPayLoad = new HashMap<>();
        newMobileNumPayLoad.put("mobileNumber", newMobileNumber);
        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        AND("User updates the preferred name with valid value");

        int responseStatusCode = updateMobileNumber.getUpdateMobileNumberStatusCode(bearerToken,newMobileNumber);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==500);

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newMobileNumPayLoad);
        assertNotNull(val);
        System.out.println(val.getMessage());
        Assert.assertTrue("Incorrect validation, Expected: "+expValidation+", Actual: +"+val.getMessage()+"", val.getMessage().toLowerCase().contains(expValidation.toLowerCase()));

        newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Mobile Number should not be updated. Expected: "+mobileNumber+", Actual: "+newUserDetails.getMobileNumber()+"'", newUserDetails.getMobileNumber().equalsIgnoreCase(mobileNumber));
        DONE();
    }

    /**
     * This test is to validate banking user can not update mobile number if EID status is "PENDING"
     */
    @Order(2)
    @Test
    public void validateFailureStatusCode_BankingUser_EIDPENDING_Negative() {
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD567";
        mobileNumber="+971501823066";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSUNxRUI5NmpQejJYeUtla1BiNWs5XC9GUEEzQmVsYm14c3dRUzk4azVsWndKQWlFQWwyTHVKbFlEZU5tWGZGTVc5UVRkMjl0Z1l6aXBodXF1SThZUjJEa1l4REU9In0.eTVoMZbQOaVRC_yhl_t9l-xPIZ7V84VM8NiAIN1ZRF4Ix10l0FIQRM3q9akyNjwNu55jAQowgKUrD3nFgOn7oQ";
        expValidation = "Invalid Customer EID Status for this operation";

        newMobileNumber = "+97150182"+String.valueOf(getRandom(10000));
        Map<String, Object> newMobileNumPayLoad = new HashMap<>();
        newMobileNumPayLoad.put("mobileNumber", newMobileNumber);

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        AND("User updates the preferred name with valid value");

        int responseStatusCode = updateMobileNumber.getUpdateMobileNumberStatusCode(bearerToken,newMobileNumber);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==500);

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newMobileNumPayLoad);
        assertNotNull(val);

        Assert.assertTrue("Incorrect validation, Expected: "+expValidation+", Actual: +"+val.getMessage()+"", val.getMessage().toLowerCase().contains(expValidation.toLowerCase()));

        newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Mobile Number should not be updated. Expected: "+mobileNumber+", Actual: "+newUserDetails.getMobileNumber()+"'", newUserDetails.getMobileNumber().equalsIgnoreCase(mobileNumber));
        DONE();
    }

    /**
     * This test is to validate market user can not edit same/existing mobile number
     */
    @Order(3)
    @Test
    public void validateMobileNumberUpdate_MarketUser_EXISTING_Negative() {
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD522";
        mobileNumber="+971501823021";
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVFDSUc0T1wvNm84R0E0ejlmTWt2bitwQ1hwQXRJS1ZcL09ZaWJsTVFFYTQySjllVUFpQXdwNE4zaGx6N3pteUhObFwvbCs3YTlHOGxCeWZcL0FvUExGZnRlY0t6c2NJQT09In0.b-3uTn_w-jSU43-E8RgOui8TMvnKHcTy7ri-kzbc1HnJFjJnTh2ZUC5M-8sV5JWSORHG827dE-QR3gx4RKhT8g";
        expValidation ="Given mobile number is already in use.";


        Map<String, Object> newMobileNumPayLoad = new HashMap<>();
        newMobileNumPayLoad.put("mobileNumber", mobileNumber);

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        AND("User updates the preferred name with valid value");

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newMobileNumPayLoad);
        THEN("User verifies the min and max char validation with invalid value");
        assertNotNull(val);
        Assert.assertTrue("Incorrect validation, Expected: "+expValidation+", Actual: +"+val.getMessage()+"", val.getMessage().equalsIgnoreCase(expValidation));
        newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Mobile Number should not be updated. Expected: "+mobileNumber+", Actual: "+newUserDetails.getMobileNumber()+"'", newUserDetails.getMobileNumber().equalsIgnoreCase(mobileNumber));
        DONE();
    }

    /**
     * This test is to validate market user can not edit new mobilenumber when OTP is not VALIDATED
     */
    @Order(4)
    @Test
    public void validateMobileNumberUpdate_MarketUser_OTPNOTVALIDATED_Negative() {
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5ED389";
        mobileNumber="+971501823516";
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVlDSVFEZFcxRENidkVqWkcwQUNhWVZhcEhuOFhCUTR5NzZETmZJNnJ3XC9PUUVhZFFJaEFKMVwvWHBMSGFpdVRDeXF6R0Z5aVpNOStIcXpJOVd4eXgwa1MyVkUzcVZTYSJ9.MVuC4Mw24_C6lhWFSWy4ybRgqs22ivzQRur1IgPjGffR0ySi6yvqxQ495LbeHgVL7G0MdcDfkTUnpDWczrh0tA";
        expValidation ="Otp Validation is Pending";

        newMobileNumber = "+97150182"+String.valueOf(getRandom(10000));
        Map<String, Object> newMobileNumPayLoad = new HashMap<>();
        newMobileNumPayLoad.put("mobileNumber", newMobileNumber);

        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);
        SignatureKeys_Setup sigKeys= setupUser.getSignatureKeys(bearerToken);
        setupUser.generateOTP_UpdateUser(bearerToken,newMobileNumber);
        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);
        GetOTP_Setup getOTP = setupUser.getOTP(newUserDetails.getCustomerId(),newMobileNumber,bearerToken,xRequestID);
        AND("User has not validated OTP");

        int responseStatusCode = updateMobileNumber.getUpdateMobileNumberStatusCode(bearerToken,newMobileNumber);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==500);

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newMobileNumPayLoad);
        assertNotNull(val);
        System.out.println(val.getMessage());
        Assert.assertTrue("Incorrect validation, Expected: "+expValidation+", Actual: +"+val.getMessage()+"", val.getMessage().toLowerCase().contains(expValidation.toLowerCase()));

        newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Mobile Number should not be updated. Expected: "+mobileNumber+", Actual: "+newUserDetails.getMobileNumber()+"'", newUserDetails.getMobileNumber().equalsIgnoreCase(mobileNumber));
        bearerToken = null;
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);
        assertNotNull(bearerToken);
        DONE();
    }


    /**
     * This test is to validate market user can not edit new mobilenumber when OTP is not GENERATED
     */
    @Order(5)
    @Test
    public void validateMobileNumberUpdate_MarketUser_OTPNOTGENERATED_Negative() {
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5ED391";
        mobileNumber="+971501823518";
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSUZHQnF0NFlqZWNUTlJDc3R3XC9QdlhCcHBPNTg4a0FzK0JORjRZanl2d3FCQWlFQXRqYXRZZGo0RnZCK2lwcjdPMWNaSmdISlVxMmE4bG95SGpZR0FDcEY1Mkk9In0.go3dTMplfLHmPMxT4SLi6AYFG30FRiaPfsfcPSvofcQzMlehJfU3fPdC0U0TFBo36Bg99vlidhMNkeUsEFpqFw";
        expValidation ="Invalid Request";

        newMobileNumber = "+97150182"+String.valueOf(getRandom(10000));
        Map<String, Object> newMobileNumPayLoad = new HashMap<>();
        newMobileNumPayLoad.put("mobileNumber", newMobileNumber);

        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User has not generated OTP");

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newMobileNumPayLoad);
        assertNotNull(val);

        Assert.assertTrue("Incorrect validation, Expected: "+expValidation+", Actual: +"+val.getMessage()+"", val.getMessage().toLowerCase().contains(expValidation.toLowerCase()));

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Mobile Number should not be updated. Expected: "+mobileNumber+", Actual: "+newUserDetails.getMobileNumber()+"'", newUserDetails.getMobileNumber().equalsIgnoreCase(mobileNumber));
        bearerToken = null;
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);
        assertNotNull(bearerToken);
        DONE();
    }

    /**
     * This test is to validate market user can not edit new mobilenumber when OTP is VALIDATED but inputs different number
     */
    @Order(6)
    @Test
    public void validateMobileNumberUpdate_MarketUser_UPDATEINCORRECTNUMBER_Negative() {
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5ED395";
        mobileNumber="+971501823522";
        jwsSignature="eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFDMk1pSlpuTXJNeXFwYXEyUVhBV2F3XC9FQVwvaVFtYjhDcmdyYXNBdnZCa1d3SWdjditRYXJWSEkrbHc2RlRpdmpcL1A1UFwvUUduMnFwc3FwRTAwRzc4Mnd4SEU9In0.JoymyrE7wHHha9xXgUEmk2bTHiZQO_xPE5Nlc5i-DPG30L_l4E0eGP5DNipbSzGpqdwBFcO76kUDHxfN88LlKQ";
        expValidation ="Invalid mobile number request";

        newMobileNumber = "+97150182"+String.valueOf(getRandom(10000));

        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        String newMobileNumberA = "+97150182"+String.valueOf(getRandom(10000));
        Map<String, Object> newMobileNumPayLoad = new HashMap<>();
        newMobileNumPayLoad.put("mobileNumber", newMobileNumberA);
        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,newMobileNumPayLoad);
        assertNotNull(val);
        System.out.println(val.getMessage());
        Assert.assertTrue("Incorrect validation, Expected: "+expValidation+", Actual: +"+val.getMessage()+"", val.getMessage().toLowerCase().contains(expValidation.toLowerCase()));

        newUserDetails = getProfile.getUserDetails(bearerToken);
        Assert.assertTrue("Mobile Number should not be updated. Expected: "+mobileNumber+", Actual: "+newUserDetails.getMobileNumber()+"'", newUserDetails.getMobileNumber().equalsIgnoreCase(mobileNumber));
        DONE();
    }
}
