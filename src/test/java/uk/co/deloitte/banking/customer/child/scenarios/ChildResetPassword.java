package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildResetPassword {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private OtpApi otpApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private AuthenticateApi authenticateApi;

    private AlphaTestUser alphaTestUserChild;
    private AlphaTestUser alphaTestUserParent;

    private String childId;
    private String connectionId;
    private String fullName = "testUser";
    private final String REQUEST_VALIDATION = "REQUEST_VALIDATION";
    private final String INVALID_PASSWORD_LENGTH_MESSAGE = "size must be between 8 and 2147483647";
    private final String FORBIDDEN_REQUEST_CODE = "UAE.ACCOUNT.FORBIDDEN";
    private final String NULL_PASSWORD_MESSAGE = "must not be blank";
    private final String FORBIDDEN_REQUEST_MESSAGE= "Unable to validate user reset password request";

    private void setupTestUsersChild() {
        alphaTestUserParent = new AlphaTestUser();
        alphaTestUserChild = new AlphaTestUser();
        alphaTestUserParent = alphaTestUserFactory.setupCustomer(alphaTestUserParent);
        childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, "validtestpassword");
        connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent,
                alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
        alphaTestUserChild =
                alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, connectionId, childId);
    }

    private void setupTestUsersChildNegative() {
        
        if (this.alphaTestUserParent == null) {
            alphaTestUserParent = new AlphaTestUser();
            alphaTestUserChild = new AlphaTestUser();
            alphaTestUserParent = alphaTestUserFactory.setupCustomer(alphaTestUserParent);
            childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, "validtestpassword");
            connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent,
                    alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
            alphaTestUserChild =
                    alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, connectionId, childId);
        }
    }

    @Test
    public void happy_path_child_resets_password_old_device() {
        TEST("AHBDB-7818: Reset Password for Child");
        TEST("AHBDB-11993: AC1 Generation of OTP (POST request) and AC2 OTP validated");
        TEST("AHBDB-11993: AC4 Resetting password from the active device and AC6 Define new password on child’s device");

        setupTestUsersChild();
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserParent);
        Assertions.assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        String newPassword = RandomDataGenerator.generateRandomSHA512enabledPassword();

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(newPassword)
                .build();

        WHEN("The client calls put to reset child passcode");
        this.authenticateApiV2.resetChildPasscode(alphaTestUserParent, resetPasscodeReq, connectionId);

        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUserParent, 204, connectionId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childId);

        Assertions.assertNotNull(otpCO);

        String otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        Assertions.assertNotNull(otpCode);

        GIVEN("Request to validate password is created");
        DependantValidateResetPasswordRequestV2 validatePasscodeResetReq = DependantValidateResetPasswordRequestV2.builder()
                .otp(otpCode)
                .password(newPassword)
                .userId(alphaTestUserChild.getUserId())
                .build();
        UserLoginResponseV2 validatePasswordResponse = this.authenticateApiV2.validateChildPasscode(alphaTestUserChild, validatePasscodeResetReq, alphaTestUserChild.getDeviceId());
        parseLoginResponse(alphaTestUserChild, validatePasswordResponse);

        THEN("we will return a 200 response");
        Assertions.assertEquals(ScopeConstants.CUSTOMER_SCOPE, validatePasswordResponse.getScope(),
                "Scope was not as expected, expected: CUSTOMER");

        AND("AND the customer has an elevated weight of 11");
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder().weight(11).scope("customer").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(newPassword).scope("customer").weight(11).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest);

        final UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();

        WHEN("Authorisation Adapter has passed the request in a POST request ");
        User updatedUser = authenticateApiV2.patchUserCredentials(alphaTestUserChild, updateUserRequestV1);
        Assertions.assertNotNull(updatedUser);
        alphaTestUserChild.setUserPassword(newPassword);

        THEN("A 200 (OK) is returned");
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(alphaTestUserChild.getUserPassword())
                .build();
        UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2.loginUserProtected(alphaTestUserChild, userLoginRequestV2, alphaTestUserChild.getDeviceId(), true);
        parseLoginResponse(alphaTestUserChild, userLoginResponseV2);

        DONE();
    }

    @Test
    public void OTP_not_validated_403() {
        TEST("AHBDB-7818: Reset Password for Child");
        TEST("AHBDB-11994: AC3 OTP NOT validated");
        setupTestUsersChild();
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserParent);
        Assertions.assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        String newPassword = UUID.randomUUID().toString();

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(newPassword)
                .build();

        WHEN("The client calls put to reset child passcode");
        this.authenticateApiV2.resetChildPasscode(alphaTestUserParent, resetPasscodeReq, connectionId);

        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUserParent, 204, connectionId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childId);

        Assertions.assertNotNull(otpCO);

        String otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        Assertions.assertNotNull(otpCode);

        WHEN("Request to validate password is created without a valid otp");
        DependantValidateResetPasswordRequestV2 validatePasscodeResetReq = DependantValidateResetPasswordRequestV2.builder()
                .otp(RandomDataGenerator.generateRandomNumeric(7))
                .password(newPassword)
                .userId(alphaTestUserChild.getUserId())
                .build();

        OBErrorResponse1 errorResponse1 = this.authenticateApiV2.validateChildPasscodeError(alphaTestUserChild, validatePasscodeResetReq, alphaTestUserChild.getDeviceId(), 403);

        THEN("A 403 response is returned");
        assertEquals(FORBIDDEN_REQUEST_CODE, errorResponse1.getCode(), "Error Code is not matching, expected "
                + FORBIDDEN_REQUEST_CODE + " but received " + errorResponse1.getCode());
        assertEquals(FORBIDDEN_REQUEST_MESSAGE, errorResponse1.getMessage(), "Error Message is not matching, expected "
                + FORBIDDEN_REQUEST_MESSAGE + " but received " + errorResponse1.getCode());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "abcdefg", "!@#$%^)", ""})
    public void invalid_password_for_resetting_password(String invalidPassword) {
        TEST("AHBDB-7818: Reset Password for Child");
        TEST("AHBDB-11995: Invalid password for reset endpoint");

        setupTestUsersChildNegative();
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserParent);
        Assertions.assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(invalidPassword)
                .build();

        WHEN("The client calls put to reset child passcode with invalid password");
        OBErrorResponse1 errorResponse1 = this.authenticateApiV2.resetChildPasscodeError(alphaTestUserParent, resetPasscodeReq, connectionId, 400);

        THEN("A 400 response is returned");
        assertEquals(REQUEST_VALIDATION, errorResponse1.getCode(), "Error Code is not matching, expected "
                + REQUEST_VALIDATION + " but received " + errorResponse1.getCode());
        assertTrue(errorResponse1.getMessage().contains(INVALID_PASSWORD_LENGTH_MESSAGE));

        DONE();
    }

    @Test
    public void missing_password_for_resetting_password() {
        TEST("AHBDB-7818: Reset Password for Child");
        TEST("AHBDB-11996: Missing password for reset endpoint");

        setupTestUsersChildNegative();
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserParent);
        Assertions.assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(null)
                .build();

        WHEN("The client calls put to reset child passcode with missing password");
        OBErrorResponse1 errorResponse1 = this.authenticateApiV2.resetChildPasscodeError(alphaTestUserParent, resetPasscodeReq, connectionId, 400);

        THEN("A 400 response is returned");
        assertEquals(REQUEST_VALIDATION, errorResponse1.getCode(), "Error Code is not matching, expected "
                + REQUEST_VALIDATION + " but received " + errorResponse1.getCode());
        assertTrue(errorResponse1.getMessage().contains(NULL_PASSWORD_MESSAGE));

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "abcdefg", "!@#$%^)", ""})
    public void invalid_password_for_validating_password(String invalidPassword) {
        TEST("AHBDB-7818: Reset Password for Child");
        TEST("AHBDB-11997: Invalid password for validating endpoint");

        setupTestUsersChildNegative();
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserParent);
        Assertions.assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        String newPassword = RandomDataGenerator.generateRandomSHA512enabledPassword();

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(newPassword)
                .build();

        WHEN("The client calls put to reset child passcode");
        this.authenticateApiV2.resetChildPasscode(alphaTestUserParent, resetPasscodeReq, connectionId);

        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUserParent, 204, connectionId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childId);

        Assertions.assertNotNull(otpCO);

        String otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        Assertions.assertNotNull(otpCode);

        WHEN("Request to validate password with invalid password is created");
        DependantValidateResetPasswordRequestV2 validatePasscodeResetReq = DependantValidateResetPasswordRequestV2.builder()
                .otp(otpCode)
                .password(invalidPassword)
                .userId(alphaTestUserChild.getUserId())
                .build();

        THEN("A 400 response is returned");
        OBErrorResponse1 errorResponse1 = this.authenticateApiV2.validateChildPasscodeError(
                alphaTestUserChild, validatePasscodeResetReq, alphaTestUserChild.getDeviceId(), 400);
        assertTrue(errorResponse1.getMessage().contains(INVALID_PASSWORD_LENGTH_MESSAGE));

        DONE();
    }

    @Test
    public void missing_password_for_validating_password() {
        TEST("AHBDB-7818: Reset Password for Child");
        TEST("AHBDB-11998: Missing password for validating endpoint");

        setupTestUsersChildNegative();
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserParent);
        Assertions.assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        String newPassword = RandomDataGenerator.generateRandomSHA512enabledPassword();

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(newPassword)
                .build();

        WHEN("The client calls put to reset child passcode");
        this.authenticateApiV2.resetChildPasscode(alphaTestUserParent, resetPasscodeReq, connectionId);

        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUserParent, 204, connectionId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childId);

        Assertions.assertNotNull(otpCO);

        String otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        Assertions.assertNotNull(otpCode);

        WHEN("Request to validate password with invalid password is created");
        DependantValidateResetPasswordRequestV2 validatePasscodeResetReq = DependantValidateResetPasswordRequestV2.builder()
                .otp(otpCode)
                .password(null)
                .userId(alphaTestUserChild.getUserId())
                .build();

        THEN("A 400 response is returned");
        OBErrorResponse1 errorResponse1 = this.authenticateApiV2.validateChildPasscodeError(
                alphaTestUserChild, validatePasscodeResetReq, alphaTestUserChild.getDeviceId(), 400);
        assertEquals(REQUEST_VALIDATION, errorResponse1.getCode(), "Error Code is not matching, expected "
                + REQUEST_VALIDATION + " but received " + errorResponse1.getCode());
        assertTrue(errorResponse1.getMessage().contains(NULL_PASSWORD_MESSAGE));

        DONE();
    }
}
