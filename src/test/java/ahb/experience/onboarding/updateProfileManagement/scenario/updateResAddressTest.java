package ahb.experience.onboarding.updateProfileManagement.scenario;

import ahb.experience.onboarding.*;
import ahb.experience.onboarding.updateProfileManagement.api.*;
import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin_Para;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

//import org.hibernate.annotations.Check;


@MicronautTest
@Slf4j
@Singleton
public class updateResAddressTest {

    @Inject
    bankingUserLogin_Para bankingUserLogin;

    @Inject
    updateResAddress updateResAddress;

    @Inject
    getProfile getProfile;

    public static int getRandom(int max){ return (int) (Math.random()*max); }

    String deviceId="";
    String mobileNumber="";
    String passcode="d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
    String jwsSignature = "";
    String bearerToken="";
    String expValidation = "";
    Map<String, Object> newAddressPayLoad = new HashMap<>();

    /***
     * This test is to validate banking user can update residential address having customer state as "ACCOUNT_CARD_ACTIVATION" OR "ACCOUNT_VERIFIED"
     */
    @Order(1)
    @Test
    public void validateSuccessStatusCode_BankUser_ACCOUNT_CARD_ACTIVATION_Positive() {
        deviceId="E8B24EBF-55AE-466A-A47C-D98808B4E0D8";
        mobileNumber="+971501823010";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVlDSVFEM1dhMm1uYVBzWXhqaHEva3hKU0FSNndlNnV0bS9VWDFvcmNZS1dJVnk2UUloQU1YRXFsNnpqTkREZFI1N3F5R2YrSStoU1I0RHZCZEt0N0RYQ1NKUTRxV0oifQ.5PYZc6RzdYVocxCpXhSmolCs8frmzJyUnUR6HEemrO4lrSasoMBdNRs5bipayJN3jmDs42ZKmoJDwxVOX0Sngw";
        newAddressPayLoad.put("buildingName", "name "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("villaNameNumber","villa "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("buildingNumber","num "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("street","street "+getRandom(1000));
        newAddressPayLoad.put("city","Dubai");
        newAddressPayLoad.put("emirate","Dubai");


        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with valid value");

        int responseStatusCode = updateResAddress.getUpdateResAddressStatusCode(bearerToken,newAddressPayLoad);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==204);
        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        ExperienceAddressDetails newAddress = newUserDetails.getAddress();
        Assert.assertTrue("Building name is NOT correct. Expected: "+newAddressPayLoad.get("buildingName")+", Actual: "+newAddress.getBuildingName()+"'", newAddressPayLoad.get("buildingName").toString().equalsIgnoreCase(newAddress.getBuildingName()));
        Assert.assertTrue("villaNameNumber is NOT correct. Expected: "+newAddressPayLoad.get("villaNameNumber")+", Actual: "+newAddress.getVillaNameNumber()+"'", newAddressPayLoad.get("villaNameNumber").toString().equalsIgnoreCase(newAddress.getVillaNameNumber()));
        Assert.assertTrue("buildingNumber is NOT correct. Expected: "+newAddressPayLoad.get("buildingNumber")+", Actual: "+newAddress.getBuildingNumber()+"'", newAddressPayLoad.get("buildingNumber").toString().equalsIgnoreCase(newAddress.getBuildingNumber()));
        Assert.assertTrue("street is NOT correct. Expected: "+newAddressPayLoad.get("street")+", Actual: "+newAddress.getStreet()+"'", newAddressPayLoad.get("street").toString().equalsIgnoreCase(newAddress.getStreet()));
        Assert.assertTrue("City is NOT correct. Expected: "+newAddressPayLoad.get("city")+", Actual: "+newAddress.getCity()+"'", newAddressPayLoad.get("city").toString().equalsIgnoreCase(newAddress.getCity()));
        Assert.assertTrue("Emirate is NOT correct. Expected: "+newAddressPayLoad.get("emirate")+", Actual: "+newAddress.getEmirate()+"'", newAddressPayLoad.get("emirate").toString().equalsIgnoreCase(newAddress.getEmirate()));

        newAddressPayLoad.put("city","Ruwais");
        newAddressPayLoad.put("emirate","Sharjah");

        responseStatusCode = updateResAddress.getUpdateResAddressStatusCode(bearerToken,newAddressPayLoad);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==204);
        newUserDetails = getProfile.getUserDetails(bearerToken);

        newAddress = newUserDetails.getAddress();
        Assert.assertTrue("Building name is NOT correct. Expected: "+newAddressPayLoad.get("buildingName")+", Actual: "+newAddress.getBuildingName()+"'", newAddressPayLoad.get("buildingName").toString().equalsIgnoreCase(newAddress.getBuildingName()));
        Assert.assertTrue("villaNameNumber is NOT correct. Expected: "+newAddressPayLoad.get("villaNameNumber")+", Actual: "+newAddress.getVillaNameNumber()+"'", newAddressPayLoad.get("villaNameNumber").toString().equalsIgnoreCase(newAddress.getVillaNameNumber()));
        Assert.assertTrue("buildingNumber is NOT correct. Expected: "+newAddressPayLoad.get("buildingNumber")+", Actual: "+newAddress.getBuildingNumber()+"'", newAddressPayLoad.get("buildingNumber").toString().equalsIgnoreCase(newAddress.getBuildingNumber()));
        Assert.assertTrue("street is NOT correct. Expected: "+newAddressPayLoad.get("street")+", Actual: "+newAddress.getStreet()+"'", newAddressPayLoad.get("street").toString().equalsIgnoreCase(newAddress.getStreet()));
        Assert.assertTrue("City is NOT correct. Expected: "+newAddressPayLoad.get("city")+", Actual: "+newAddress.getCity()+"'", newAddressPayLoad.get("city").toString().equalsIgnoreCase(newAddress.getCity()));
        Assert.assertTrue("Emirate is NOT correct. Expected: "+newAddressPayLoad.get("emirate")+", Actual: "+newAddress.getEmirate()+"'", newAddressPayLoad.get("emirate").toString().equalsIgnoreCase(newAddress.getEmirate()));

        DONE();
    }

    /***
     * This test is to validate marketplace user can NOT update residential address
     */
    @Order(2)
    @Test
    public void validateFailureStatusCode_MarketUser_Negative() {
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFDWW5Xcm42S3RKamdTMFkxODUwcWFidEVLTzhzTlNndllYMkUySDkwSCtpQUlnTFVzNXJUcW5LajdLVDRPb1wvclI2VnpGUGpRWnVhaCtnVkFrV1NTXC81WFg0PSJ9.Nj5XXWhPwXN-V6f8iNzIw0wGmohsk40Qi12UoFeE8iELS-2tAOQnefLvcRS4l74fjH50y_fGlG6dnBl6d0ag9A";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD532";
        mobileNumber="+971501823031";

        expValidation = "Not a banking user.";

        newAddressPayLoad.put("buildingName", "name "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("villaNameNumber","villa "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("buildingNumber","num "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("street","street "+getRandom(1000));

        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        int responseStatusCode = updateResAddress.getUpdateResAddressStatusCode(bearerToken,newAddressPayLoad);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==500);
        JSONObject nestedJson = updateResAddress.getResidentialValue(newAddressPayLoad);

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,nestedJson);
        Assert.assertTrue("Validation is NOT correct. Expected: "+expValidation+", Actual: "+val.getMessage()+"'", expValidation.equalsIgnoreCase(val.getMessage()));
    }

    /***
     * This test is to validate marketplace with IDV_COMPLETED user can NOT update residential address
     */
    @Order(3)
    @Test
    public void validateFailureStatusCode_MarketUser_IDVCOMPLETED_Negative() {
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSUU4U1lSWURBMFZkbUJHVGNuUUEzQlJKODJRMWtBSUYwM1paNHlTd3JBWjFBaUVBOHRlTmVERWpNTXAxRXNhcjZ3bDc0WUtaZkVHZkdaZlwvdXhXWVBIdFVtb2c9In0.u4mj2O0IQVm645_3yP_l7HfOWFdVTTF2No_4odRpiv74zWJF5hyFx4CFLQdJq6JjR-36F59vgu64X-jnwKOL_w";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD552";
        mobileNumber="+971501823051";
        expValidation = "Not a banking user.";

        newAddressPayLoad.put("buildingName", "name "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("villaNameNumber","villa "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("buildingNumber","num "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("street","street "+getRandom(1000));

        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        int responseStatusCode = updateResAddress.getUpdateResAddressStatusCode(bearerToken,newAddressPayLoad);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==500);
        JSONObject nestedJson = updateResAddress.getResidentialValue(newAddressPayLoad);

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,nestedJson);
        Assert.assertTrue("Validation is NOT correct. Expected: "+expValidation+", Actual: "+val.getMessage()+"'", expValidation.equalsIgnoreCase(val.getMessage()));
    }

    /***
     * This test is to validate banking user can NOT update residential address having customer state as "ACCOUNT_CREATED"
     */
    @Order(4)
    @Test
    public void validateFailureStatusCode_BankUser_AccountCreated_Negative() {
        expValidation="Invalid Customer State for this operation.";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSUFSMEJkeVpveEVYN1pSa0JHYkFqT0NjaCtrZTN6ditXWUNvTk96b0FUTmNBaUVBc2hYU2k5Nzc1RUl6OTFnZXo4WjVrdXVsRnJibDVPWDdRVFVOdUZ6YVYwWT0ifQ.nIkuqpW1jc0pRX3pzp54RZqISTME-eR29i4_Z-EbUYqFJeu8_1D5Ln11Mu58Od-9sqJVS0xgKb2TUMCDW7hL1A";
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD516";
        mobileNumber="+971501823015";

        newAddressPayLoad.put("buildingName", "name "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("villaNameNumber","villa "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("buildingNumber","num "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("street","street "+getRandom(1000));

        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        int responseStatusCode = updateResAddress.getUpdateResAddressStatusCode(bearerToken,newAddressPayLoad);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==500);
        JSONObject nestedJson = updateResAddress.getResidentialValue(newAddressPayLoad);

        ExperienceErrValidations val =  getProfile.getUpdateProfileValidationErr(bearerToken,nestedJson);

        Assert.assertTrue("Validation is NOT correct. Expected: "+expValidation+", Actual: "+val.getMessage()+"'", expValidation.equalsIgnoreCase(val.getMessage()));
    }

    /***
     * This test is to validate banking user can update residential address having customer state as "ACCOUNT_VERIFIED"
     */
    @Order(5)
    @Test
    public void validateSuccessStatusCode_BankUser_ACCOUNT_VERIFIED_Positive() {
        deviceId="4A7B2099-AE76-45F9-B8B3-371E5LD553";
        mobileNumber="+971501823052";
        jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSUU5SE13TlNKaWw4aUNIMkpYYkp2ZG5QYnNKYmpCNFwvS3poUU03cFc1SkNHQWlFQW1UM2RCUmFTNmg2K0Z0TDBRWUg4cExLXC9kRTl5TkZ6T0VBcHRnbk4rRHdvPSJ9.jnUOhzUhNAX-QllvdG7beIAcu5c-E7ZWCO_Xlp4c2lpC0sv4PXrF5jUajKmdGn-cklbNOTWZd3JwDbjxa_0bww";

        newAddressPayLoad.put("buildingName", "name "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("villaNameNumber","villa "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("buildingNumber","num "+String.valueOf(getRandom(1000)));
        newAddressPayLoad.put("street","street "+getRandom(1000));
        newAddressPayLoad.put("city","Abu Dhabi City");
        newAddressPayLoad.put("emirate","Dubai");

        TEST("AHBDB-17370 - API | Validate 204 response for valid update name");
        GIVEN("User has a valid test user with phone number, device Id and passcode");
        WHEN("User login with that user");
        bearerToken= bankingUserLogin.getAccessToken_Common(deviceId,mobileNumber,passcode,jwsSignature);

        AND("User updates the preferred name with valid value");

        int responseStatusCode = updateResAddress.getUpdateResAddressStatusCode(bearerToken,newAddressPayLoad);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==204);
        ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(bearerToken);

        ExperienceAddressDetails newAddress = newUserDetails.getAddress();
        Assert.assertTrue("Building name is NOT correct. Expected: "+newAddressPayLoad.get("buildingName")+", Actual: "+newAddress.getBuildingName()+"'", newAddressPayLoad.get("buildingName").toString().equalsIgnoreCase(newAddress.getBuildingName()));
        Assert.assertTrue("villaNameNumber is NOT correct. Expected: "+newAddressPayLoad.get("villaNameNumber")+", Actual: "+newAddress.getVillaNameNumber()+"'", newAddressPayLoad.get("villaNameNumber").toString().equalsIgnoreCase(newAddress.getVillaNameNumber()));
        Assert.assertTrue("buildingNumber is NOT correct. Expected: "+newAddressPayLoad.get("buildingNumber")+", Actual: "+newAddress.getBuildingNumber()+"'", newAddressPayLoad.get("buildingNumber").toString().equalsIgnoreCase(newAddress.getBuildingNumber()));
        Assert.assertTrue("street is NOT correct. Expected: "+newAddressPayLoad.get("street")+", Actual: "+newAddress.getStreet()+"'", newAddressPayLoad.get("street").toString().equalsIgnoreCase(newAddress.getStreet()));
        Assert.assertTrue("City is NOT correct. Expected: "+newAddressPayLoad.get("city")+", Actual: "+newAddress.getCity()+"'", newAddressPayLoad.get("city").toString().equalsIgnoreCase(newAddress.getCity()));
        Assert.assertTrue("Emirate is NOT correct. Expected: "+newAddressPayLoad.get("emirate")+", Actual: "+newAddress.getEmirate()+"'", newAddressPayLoad.get("emirate").toString().equalsIgnoreCase(newAddress.getEmirate()));
        newAddressPayLoad.put("city","Ruwais");
        newAddressPayLoad.put("emirate","Sharjah");

        responseStatusCode = updateResAddress.getUpdateResAddressStatusCode(bearerToken,newAddressPayLoad);
        THEN("User verified record is updated successfully");
        assertNotNull(responseStatusCode);

        Assert.assertTrue("Incorrect Status code", responseStatusCode==204);
        newUserDetails = getProfile.getUserDetails(bearerToken);

        newAddress = newUserDetails.getAddress();
        Assert.assertTrue("Building name is NOT correct. Expected: "+newAddressPayLoad.get("buildingName")+", Actual: "+newAddress.getBuildingName()+"'", newAddressPayLoad.get("buildingName").toString().equalsIgnoreCase(newAddress.getBuildingName()));
        Assert.assertTrue("villaNameNumber is NOT correct. Expected: "+newAddressPayLoad.get("villaNameNumber")+", Actual: "+newAddress.getVillaNameNumber()+"'", newAddressPayLoad.get("villaNameNumber").toString().equalsIgnoreCase(newAddress.getVillaNameNumber()));
        Assert.assertTrue("buildingNumber is NOT correct. Expected: "+newAddressPayLoad.get("buildingNumber")+", Actual: "+newAddress.getBuildingNumber()+"'", newAddressPayLoad.get("buildingNumber").toString().equalsIgnoreCase(newAddress.getBuildingNumber()));
        Assert.assertTrue("street is NOT correct. Expected: "+newAddressPayLoad.get("street")+", Actual: "+newAddress.getStreet()+"'", newAddressPayLoad.get("street").toString().equalsIgnoreCase(newAddress.getStreet()));
        Assert.assertTrue("City is NOT correct. Expected: "+newAddressPayLoad.get("city")+", Actual: "+newAddress.getCity()+"'", newAddressPayLoad.get("city").toString().equalsIgnoreCase(newAddress.getCity()));
        Assert.assertTrue("Emirate is NOT correct. Expected: "+newAddressPayLoad.get("emirate")+", Actual: "+newAddress.getEmirate()+"'", newAddressPayLoad.get("emirate").toString().equalsIgnoreCase(newAddress.getEmirate()));

        DONE();
    }
}
