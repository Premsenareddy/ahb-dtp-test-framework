package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantRegisterDeviceRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomIntegerInRange;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildInitialDeviceRegistration {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private OtpApi otpApi;

    @Inject
    private RelationshipApi relationshipApi;

    private static final String TEMPORARY_PASSWORD = "temporary_password";
    private static final String REGISTER_DEVICE_403_RESPONSE = "UAE.ACCOUNT.FORBIDDEN";
    private static final String REGISTER_DEVICE_400_RESPONSE = "REQUEST_VALIDATION";

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;
    private AlphaTestUser alphaTestUserChildNegativeTests;

    private String childId;
    private String relationshipId;
    private String otpForNegativeTests;

    private void setupTestUser() {

        if (this.alphaTestUser == null && this.alphaTestUserChildNegativeTests == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);

            HashMap<String, String> childDetails = this.createDependant(this.alphaTestUser);

            this.childId = childDetails.get("ChildId");
            this.relationshipId = childDetails.get("ConnectionId");

            this.alphaTestUserChildNegativeTests = new AlphaTestUser();
            alphaTestUserChildNegativeTests.setUserPassword(TEMPORARY_PASSWORD);
            alphaTestUserChildNegativeTests.setDateOfBirth(LocalDate.now().minusYears(15));
            alphaTestUserChildNegativeTests.setUserId(this.childId);
            alphaTestUserChildNegativeTests.setCustomerId(this.childId);

            otpApi.sentChildOTPCode(alphaTestUser, 204, this.relationshipId);

            OtpCO otpCO = this.developmentSimulatorService.retrieveOtpFromDevSimulator(this.childId);
            assertNotNull(otpCO);

            this.otpForNegativeTests = otpCO.getPassword();
        }
    }

    private AlphaTestUser setupTestUserFresh() {
        

        AlphaTestUser alphaTestUserFresh = new AlphaTestUser();
        alphaTestUserFresh = this.alphaTestUserFactory.setupCustomer(alphaTestUserFresh);
        return alphaTestUserFresh;
    }

    @Test
    public void verify_relationship_between_parent_and_child() {
        TEST("AHBDB-6179: Child's Initial Device Registration");
        TEST("AHBDB-10360: AC1 - Get existing relationships from CRM");
        TEST("AHBDB-10553: AC1 - Get existing relationships from CRM");
        AlphaTestUser alphaTestUserFresh = setupTestUserFresh();

        HashMap<String, String> childDetails = this.createDependant(alphaTestUserFresh);

        String dependantId = childDetails.get("ChildId");
        String connectionId = childDetails.get("ConnectionId");

        GIVEN("A get relationship request is sent");
        WHEN("The userID of the parent extracted from the token is valid");

        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(alphaTestUserFresh);

        THEN("The existing customer relationship is retrieved successfully");
        AND("The platform responds with 200 OK");
        AND("All existing relationships with its corresponding connectionID will be returned");
        AND("The child's userID is seen");

        assertEquals(alphaTestUserFresh.getUserId(), getResponse.getData().getCustomerId().toString());
        assertEquals(dependantId, getResponse.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(connectionId, getResponse.getData().getRelationships().get(0).getConnectionId().toString());
        assertEquals(1, getResponse.getData().getRelationships().size());

        DONE();
    }

    @Test
    public void relationship_not_verified_404_response() {
        TEST("AHBDB-6179: Child's initial device registration");
        TEST("AHBDB-10361: AC2 - Relationship not verified - 404 Response");
        TEST("AHBDB-10555: AC2 - Relationship not verified - 404 Response");
        setupTestUser();
        GIVEN("A get relationship request is sent");
        WHEN("The provided connectionID does not exist in the relationship list of the parent");
        String invalidConnectionId = UUID.randomUUID().toString();
        OBErrorResponse1 getError = this.relationshipApi
                .getChildBasedOnRelationshipError(this.alphaTestUser, invalidConnectionId, 404);
        THEN("The platform will return a 404 Not Found");
        assertNotNull(getError);
        DONE();
    }

    @Test
    public void negative_test_login_as_parent_error_401_response() {
        TEST("AHBDB-6179: Child's initial device registration");
        TEST("AHBDB-10362: AC3 - Parent is not verified - 401 Response");
        TEST("AHBDB-10557: AC3 - Parent is not verified - 401 Response");
        setupTestUser();
        GIVEN("A customer attempts to login to their account");
        WHEN("The POST request is sent to the authentication adapter");
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(UUID.randomUUID().toString())
                .password(alphaTestUser.getUserPassword())
                .build();

        authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestV2, 401);

        AND("the phone number and userID do not match");
        THEN("The platform will return a 401 response");
        DONE();
    }

    @Test
    public void happy_path_login_as_parent_200_response() {
        TEST("AHBDB-6179: Child's Initial Device Registration");
        TEST("AHBDB-10363: AC4 - Parent is verified");
        TEST("AHBDB-10556: AC4 - Parent is verified");
        AlphaTestUser alphaTestUserFresh = setupTestUserFresh();
        GIVEN("A Get request has been sent to ForgeRock");

//        AlphaTestUser userToUseForLogin = new AlphaTestUser();
//
//        userToUseForLogin.setUserId(alphaTestUserFresh.getUserId());
//        userToUseForLogin.setUserPassword(alphaTestUserFresh.getUserPassword());
//        userToUseForLogin.setDeviceId(alphaTestUserFresh.getDeviceId());
//        userToUseForLogin.setDeviceHash(alphaTestUserFresh.getDeviceHash());
//        userToUseForLogin.setPublicKeyBase64(alphaTestUserFresh.getPublicKeyBase64());
//        userToUseForLogin.setPrivateKeyBase64(alphaTestUserFresh.getPrivateKeyBase64());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserFresh.getUserId())
                .password(alphaTestUserFresh.getUserPassword())
                .build();

        UserLoginResponseV2 loginResponse = this
                .authenticateApi.loginUserProtected(alphaTestUserFresh, userLoginRequestV2, alphaTestUserFresh.getDeviceId(), false);
        WHEN("The phone number and the parent's userID match");
        THEN("A 200 OK is returned");
        assertNotNull(loginResponse.getAccessToken());
        assertNotNull(loginResponse.getRefreshToken());
        assertEquals(alphaTestUserFresh.getUserId(), loginResponse.getUserId());
        DONE();
    }

    @Test
    public void happy_path_generate_otp_on_parents_device_204_response() {
        TEST("AHBDB-6179: Child's initial device registration");
        TEST("AHBDB-10364: AC5 - Generation of OTP (POST Request)");
        TEST("AHBDB-10558: AC5 - Generation of OTP (POST Request)");
        AlphaTestUser alphaTestUserFresh = setupTestUserFresh();
        GIVEN("A customer is trying to onboard their child using their phone");
        AND("The child is already created in CRM");

        HashMap<String, String> childDetails = this.createDependant(alphaTestUserFresh);

        String dependantId = childDetails.get("ChildId");
        String connectionId = childDetails.get("ConnectionId");

        WHEN("The parent generates an OTP to start on-boarding their child");
        AND("Both the childId and parent's phone number are valid");
        otpApi.sentChildOTPCode(alphaTestUserFresh, 204, connectionId);
        OtpCO otpCO = this.developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);

        THEN("They will retrieve the OTP from their phone: " + otpCO.getPassword());
        assertNotNull(otpCO.getPassword());

        assertEquals(OtpType.TEXT.toString(), otpCO.getType().toString());
        assertEquals(alphaTestUserFresh.getUserTelephone(), otpCO.getDestination());

        DONE();
    }

    @Test
    public void happy_path_validate_otp_on_child_phone() {
        TEST("AHBDB-6179: Child's initial device registration");
        TEST("AHBDB-10365: AC6 - Validate OTP on Child's phone");
        TEST("AHBDB-10559: AC6 - Validate OTP on Child's phone");
        AlphaTestUser alphaTestUserFresh = setupTestUserFresh();

        HashMap<String, String> childDetails = this.createDependant(alphaTestUserFresh);

        String dependantId = childDetails.get("ChildId");
        String connectionId = childDetails.get("ConnectionId");

        GIVEN("The access tokens for a child are valid");
        WHEN("The deviceId is sent in the POST request");

        otpApi.sentChildOTPCode(alphaTestUserFresh, 204, connectionId);

        OtpCO otpCO = this.developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);

        THEN("They will retrieve the OTP from their phone: " + otpCO.getPassword());
        assertNotNull(otpCO.getPassword());
        String otpCode = otpCO.getPassword();

        assertEquals(OtpType.TEXT.toString(), otpCO.getType().toString());
        assertEquals(alphaTestUserFresh.getUserTelephone(), otpCO.getDestination());

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(TEMPORARY_PASSWORD)
                .otp(otpCode)
                .build();

        this.alphaTestUserChild = new AlphaTestUser();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(this.alphaTestUserChild, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());
        assertEquals(dependantId, userLoginResponseV2.getUserId());

        THEN("The platform will return a 201");
        AND("The child's userId will be returned");
        DONE();
    }

    @Test
    public void negative_test_validate_phone_with_wrong_otp() {
        TEST("AHBDB-6179: Child's Initial Device Registration");
        TEST("AHBDB-10366: AC7 - OTP not validated - 403 Forbidden");
        TEST("AHBDB-10366: AC7 - OTP not validated - 403 Forbidden");
        setupTestUser();
        GIVEN("A child wants to validate their phone with an OTP");
        AND("The OTP they use does not exist");
        WHEN("They attempt to validate their device");

        String randomOtp = generateRandomIntegerInRange(100000, 999999).toString();

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(this.childId)
                .password(TEMPORARY_PASSWORD)
                .otp(randomOtp)
                .build();

        OBErrorResponse1 errorResponse = authenticateApi
                .registerDependantUserDeviceError(this.alphaTestUserChildNegativeTests, request, 403);

        THEN("The platform will return a 403 Forbidden");
        assertNotNull(errorResponse);
        assertEquals(REGISTER_DEVICE_403_RESPONSE, errorResponse.getCode());

        DONE();
    }

    @Test
    public void happy_path_register_device_validate_otp_and_upload_certificates() {
        TEST("AHBDB-6179: Child's initial device registration");
        TEST("AHBDB-10367: AC8 - Initial registration");
        AlphaTestUser alphaTestUserFresh = setupTestUserFresh();

        HashMap<String, String> childDetails = this.createDependant(alphaTestUserFresh);

        String dependantId = childDetails.get("ChildId");
        String connectionId = childDetails.get("ConnectionId");

        GIVEN("The access tokens for a child are valid");
        WHEN("The deviceId is sent in the POST request");

        otpApi.sentChildOTPCode(alphaTestUserFresh, 204, connectionId);

        OtpCO otpCO = this.developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);

        THEN("They will retrieve the OTP from their phone: " + otpCO.getPassword());
        assertNotNull(otpCO.getPassword());
        String otpCode = otpCO.getPassword();

        assertEquals(OtpType.TEXT.toString(), otpCO.getType().toString());
        assertEquals(alphaTestUserFresh.getUserTelephone(), otpCO.getDestination());

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(TEMPORARY_PASSWORD)
                .otp(otpCode)
                .build();

        this.alphaTestUserChild = new AlphaTestUser();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(this.alphaTestUserChild, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(this.alphaTestUserChild, userLoginResponseV2);
        this.alphaTestUserChild.setUserPassword(TEMPORARY_PASSWORD);

        alphaTestUserChild = alphaTestUserFactory.setupUserCerts(alphaTestUserChild);

        THEN("The deviceId will be posted and stored in ForgeRock");
        AND("The platform will return a 200 OK");
        AND("The device's certificate will be stored in the Certificate Service");
        AND("The device status is set to active");

        assertNotNull(alphaTestUserChild);

        OBReadCustomer1 getResponse = this.customerApi.getCurrentCustomer(alphaTestUserChild);
        assertEquals(alphaTestUserFresh.getUserId(), getResponse.getData().getCustomer().get(0).getOnboardedBy());
        DONE();
    }

    @Test
    public void negative_test_generate_otp_with_invalid_relationship_id() {

        TEST("AHBDB-6179: Child's initial device registration");
        TEST("AHBDB-10368: Negative Test - Generate OTP with invalid relationshipID");
        GIVEN("A customer is trying to onboard their child using their phone");
        AND("The child is already created in CRM");

        WHEN("The parent generates an OTP to start on-boarding their child");
        AND("The relationshipID does not exist");

        String randomUUID = UUID.randomUUID().toString();

        otpApi.sentChildOTPCode(alphaTestUser, 404, randomUUID);

        THEN("The service will return a 400 Bad Request");
        DONE();
    }

    @Test
    public void negative_test_validate_phone_with_wrong_password_403_response() {
        //AHBDB-12767  -Test failing

        TEST("AHBDB-6179: Child's Initial Device Registration");
        TEST("AHBDB-10369: Negative Test - Validate Phone with wrong OTP");
        GIVEN("A child wants to validate their phone");
        AND("They use the wrong temporary password");

        WHEN("They attempt to validate their device");

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(this.childId)
                .password("wrong_password")
                .otp(this.otpForNegativeTests)
                .build();

        this.alphaTestUserChildNegativeTests = new AlphaTestUser();

        OBErrorResponse1 errorResponse = authenticateApi
                .registerDependantUserDeviceError(this.alphaTestUserChildNegativeTests, request, 403);

        THEN("The platform will return a 403 Forbidden");
        assertNotNull(errorResponse);
        assertEquals(REGISTER_DEVICE_403_RESPONSE, errorResponse.getCode());

        DONE();
    }

    @Test
    public void negative_test_validate_phone_with_wrong_child_id_403_response() {
        //AHBDB-12767  -Test failing

        TEST("AHBDB-6179: Child's Initial Device Registration");
        TEST("AHBDB-10370: Negative Test - Validate Phone with wrong childID");
        GIVEN("A child wants to validate their phone");

        WHEN("They attempt to validate their device");
        AND("The service receives a childID that doesn't exist");

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(UUID.randomUUID().toString())
                .password(TEMPORARY_PASSWORD)
                .otp(this.otpForNegativeTests)
                .build();

        this.alphaTestUserChildNegativeTests = new AlphaTestUser();

        OBErrorResponse1 errorResponse = authenticateApi
                .registerDependantUserDeviceError(this.alphaTestUserChildNegativeTests, request, 403);

        THEN("The platform will return a 403 Forbidden");
        assertNotNull(errorResponse);
        assertEquals(REGISTER_DEVICE_403_RESPONSE, errorResponse.getCode());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password", "UserId", "Otp"})
    public void negative_test_validate_phone_with_null_values_403_response(String fieldToRemove) {
        //AHBDB-12767  -Test failing

        TEST("AHBDB-6179: Child's Initial Device Registration");
        TEST("AHBDB-10371: Negative Test - Validate Child's Phone with null values");
        setupTestUser();
        GIVEN("A child wants to validate their phone");

        WHEN("They attempt to validate their device");
        AND("The service receives a request with a null field");

        JSONObject body = new JSONObject() {
            {
                put("Password", TEMPORARY_PASSWORD);
                put("UserId", childId);
                put("Otp", otpForNegativeTests);
                remove(fieldToRemove);
            }
        };

        OBErrorResponse1 errorResponse = authenticateApi
                .registerDependantUserDeviceErrorJson(this.alphaTestUserChildNegativeTests, body, 400);

        THEN("The platform will return a 400 Forbidden");
        assertNotNull(errorResponse);
        assertEquals(REGISTER_DEVICE_400_RESPONSE, errorResponse.getCode());
        DONE();
    }

    @Test
    public void negative_test_register_child_device_twice() {
        TEST("AHBDB-6179: Child's Initial Device Registration");
        TEST("AHBDB-10455: Negative Test - Validate Child's device twice in a row");
        AlphaTestUser alphaTestUserFresh = setupTestUserFresh();

        HashMap<String, String> childDetails = this.createDependant(alphaTestUserFresh);

        String dependantId = childDetails.get("ChildId");
        String connectionId = childDetails.get("ConnectionId");

        GIVEN("A child has validated their phone");

        otpApi.sentChildOTPCode(alphaTestUserFresh, 204, connectionId);

        OtpCO otpCO = this.developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);

        assertNotNull(otpCO.getPassword());
        String otpCode = otpCO.getPassword();

        assertEquals(OtpType.TEXT.toString(), otpCO.getType().toString());
        assertEquals(alphaTestUserFresh.getUserTelephone(), otpCO.getDestination());

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(TEMPORARY_PASSWORD)
                .otp(otpCode)
                .build();

        this.alphaTestUserChild = new AlphaTestUser();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(this.alphaTestUserChild, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(this.alphaTestUserChild, userLoginResponseV2);
        this.alphaTestUserChild.setUserPassword(TEMPORARY_PASSWORD);

        alphaTestUserChild = alphaTestUserFactory.setupUserCerts(alphaTestUserChild);
        assertNotNull(alphaTestUserChild);

        WHEN("They attempt to validate their phone for a second times");

        OBErrorResponse1 error = authenticateApi
                .registerDependantUserDeviceError(this.alphaTestUserChild, request, 403);

        assertEquals(REGISTER_DEVICE_403_RESPONSE, error.getCode());

        THEN("The platform will return a 403 response");
        DONE();
    }

    @Test
    public void negative_test_send_otp_to_wrong_parent() {
        TEST("AHBDB-6179: Child's initial device registration");
        TEST("AHBDB-10481: Negative Test - Generate otp on the wrong device");
        setupTestUser();
        GIVEN("A customer is trying to onboard their child using their phone");
        AND("The child is already created in CRM");

        WHEN("The parent generates an OTP to start on-boarding their child");
        AND("The relationshipID does not exist");

        String randomUUID = UUID.randomUUID().toString();
        String oldNumber = alphaTestUser.getUserTelephone();

        alphaTestUser.setUserTelephone(generateRandomMobile());
        otpApi.sentChildOTPCode(alphaTestUser, 404, randomUUID);

        THEN("The service will return a 400 Bad Request");
        alphaTestUser.setUserTelephone(oldNumber);

        DONE();
    }

    private HashMap<String, String> createDependant(final AlphaTestUser alphaTestUserToUse) {
        OBReadRelationship1 checkForRelationships = this.relationshipApi.getRelationships(alphaTestUserToUse);

        if (checkForRelationships.getData().getRelationships() == null) {
            UserRelationshipWriteRequest request =
                    UserRelationshipWriteRequest.builder().tempPassword(TEMPORARY_PASSWORD).build();
            LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUserToUse, request);
            assertNotNull(response.getUserId());
            String childId = response.getUserId();

            OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                    .data(OBWriteDependant1Data.builder()
                            .id(UUID.fromString(childId))
                            .dateOfBirth(LocalDate.now().minusYears(15))
                            .fullName("js " + generateEnglishRandomString(10))
                            .gender(OBGender.MALE)
                            .language("en")
                            .termsVersion(LocalDate.now())
                            .termsAccepted(Boolean.TRUE)
                            .customerRole(OBRelationshipRole.FATHER)
                            .dependantRole(OBRelationshipRole.SON)
                            .build())
                    .build();

            OBReadRelationship1 createResponse =
                    this.relationshipApi.createDependant(alphaTestUserToUse, obWriteDependant1);
            String connectionId = createResponse.getData().getRelationships().get(0).getConnectionId().toString();

            return new HashMap<>() {
                {
                    put("ChildId", childId);
                    put("ConnectionId", connectionId);
                }
            };
        }

        return new HashMap<>() {
            {
                put("ChildId", checkForRelationships.getData().getRelationships().get(0).getCustomerId().toString());
                put("ConnectionId", checkForRelationships.getData().getRelationships().get(0).getConnectionId().toString());
            }
        };
    }
}
