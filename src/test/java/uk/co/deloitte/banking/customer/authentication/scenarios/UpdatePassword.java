package uk.co.deloitte.banking.customer.authentication.scenarios;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateForgottenPasswordRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ValidateResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdatePassword {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private OtpApi otpApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    private AlphaTestUser alphaTestUser;

    private final String UPDATED_PASSWORD = "cf89f713e07f6b536d0920016da31f23e2551ad69758934ef68f9770ea0b5395afbba5f20a54a04c9b625bb61936e07bb0b5eeedf0378980ac0814e9b28f018c";

    private final String PASSWORD_LENGTH_ERROR = "updateForgottenPassword.updateForgottenPasswordRequestV1.userPassword: size must be between 8 and 2147483647";

    private final String PASSWORD_MISSING_FIELDS = "updateForgottenPassword.updateForgottenPasswordRequestV1.userPassword: must not be blank";

    private final String INVALID_PASSWORD_ERROR = "Invalid Credentials provided";

    private final String ERROR_CODE_PASSWORD_LENGTH = "REQUEST_VALIDATION";

    private final String ERROR_CODE_INVALID_PASSWORD = "0006";


    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Test
    public void reset_password_200_successful() {

        TEST("AHBDB-13311 Test failed");

        TEST("AHBDB-1365: AC3 - Hash not found - 501 not implemented");

        TEST("AHBDB-4102: AC3 Negative Test - Hash not found - 501 not implemented");

        GIVEN("The client has a bank account");
        AlphaTestUser customerWithAccount = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());

        WHEN("They try to reset their password using just OTP");

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .phoneNumber(customerWithAccount.getUserTelephone()).build();

        String resetPasswordHash = this.authenticateApi.initiateResetPassword(customerWithAccount, resetPasswordRequest).getHash();

        String simulatedOtpPasswordReset = developmentSimulatorService
                .retrieveOtpFromDevSimulator2(resetPasswordHash, customerWithAccount.getLoginResponse().getAccessToken()).getPassword();

        ValidateResetPasswordRequest validate = ValidateResetPasswordRequest.builder()
                .otp(simulatedOtpPasswordReset).hash(resetPasswordHash).build();

        String updatedHash = this.authenticateApi.validateResetPasswordOtp(customerWithAccount, validate).getHash();

        UpdateForgottenPasswordRequestV1 update = UpdateForgottenPasswordRequestV1.builder()
                .hash(updatedHash).userPassword("invalidPassword").build();

        THEN("The Status Code is 200");
        this.authenticateApi.updateForgottenPasswordVoidResponse(customerWithAccount, update, 200);

        DONE();
    }

    @Test
    public void invalid_password_password_with_null_value_400_bad_request() {
        TEST("AHBDB-1365: AC2 - Invalid password - 400 bad request");
        TEST("AHBDB-4105: AC2 Negative Test - Invalid password - Password With Null Value - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a password against the user");
        AND("The password does not satisfy the validation in the data table");

        WHEN("The client updates the user with this password");
        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .phoneNumber(this.alphaTestUser.getUserTelephone()).build();

        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, resetPasswordRequest).getHash();

        String simulatedOtpPasswordReset = this.developmentSimulatorService.retrieveOtpFromDevSimulator2(resetPasswordHash, this.alphaTestUser.getLoginResponse().getAccessToken()).getPassword();

        ValidateResetPasswordRequest validate = ValidateResetPasswordRequest.builder()
                .otp(simulatedOtpPasswordReset).hash(resetPasswordHash).build();

        String updatedHash = this.authenticateApi.validateResetPasswordOtp(this.alphaTestUser, validate).getHash();

        THEN("The Status Code is 400");
        UpdateForgottenPasswordRequestV1 update = UpdateForgottenPasswordRequestV1.builder()
                .hash(updatedHash).userPassword("").build();

        OBErrorResponse1 error = this.authenticateApi.updateForgottenPasswordErrorResponse(this.alphaTestUser, update, 400);

        Assertions.assertTrue(error.getCode().equals(ERROR_CODE_PASSWORD_LENGTH), "Error message was not as expected, " +
                "test expected : " + ERROR_CODE_PASSWORD_LENGTH);
        Assertions.assertTrue(error.getMessage().contains(PASSWORD_LENGTH_ERROR), "Error message was not as expected, " +
                "test expected : " + PASSWORD_LENGTH_ERROR);
        Assertions.assertTrue(error.getMessage().contains(PASSWORD_MISSING_FIELDS), "Error message was not as expected, " +
                "test expected : " + PASSWORD_MISSING_FIELDS);
        DONE();
    }

    @Test
    public void happy_path_store_new_password_for_locked_user_200_success() throws JsonProcessingException {

        TEST("AHBDB-13311 Test failed");

        TEST("AHBDB-1365: AC1 Store new password - 200 success");
        TEST("AHBDB-4213: AC1 Positive Test - Happy Patch Scenario Store new password for locked user - 200 success");
        setupTestUser();
        GIVEN("The client has begun the forgot passcode journey");
        AND("The customer has successfully validated the phone number via OTP");
        LoginResponseV1 loginResponse = this.authenticateApi.loginUserViaTelephoneNumber(this.alphaTestUser);

        TokenUtils.parseLoginResponse(alphaTestUser, loginResponse);

        this.authenticateApi.loginUserViaTelephoneNumberVoid(this.alphaTestUser, UserLoginRequest.builder()
                .password("invalid")
                .phoneNumber(this.alphaTestUser.getUserTelephone())
                .deviceId(this.alphaTestUser.getDeviceId()).build(), 401);

        this.authenticateApi.loginUserViaTelephoneNumberVoid(this.alphaTestUser, UserLoginRequest.builder()
                .password("invalid")
                .phoneNumber(this.alphaTestUser.getUserTelephone())
                .deviceId(this.alphaTestUser.getDeviceId()).build(), 401);

        this.authenticateApi.loginUserViaTelephoneNumberVoid(this.alphaTestUser, UserLoginRequest.builder()
                .password("invalid")
                .phoneNumber(this.alphaTestUser.getUserTelephone())
                .deviceId(this.alphaTestUser.getDeviceId()).build(), 423);

        WHEN("The client attempts to update the user with a new valid password");
        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, ResetPasswordRequest.builder().phoneNumber(this.alphaTestUser.getUserTelephone()).build()).getHash();

        String simulatedOtpPasswordReset = this.developmentSimulatorService.retrieveOtpFromDevSimulator2(resetPasswordHash, this.alphaTestUser.getLoginResponse().getAccessToken()).getPassword();

        ValidateResetPasswordRequest validate = ValidateResetPasswordRequest.builder()
                .otp(simulatedOtpPasswordReset).hash(resetPasswordHash).build();

        String updatedHash = this.authenticateApi.validateResetPasswordOtp(this.alphaTestUser, validate).getHash();
        Assertions.assertNotNull(updatedHash);

        UpdateForgottenPasswordRequestV1 update = UpdateForgottenPasswordRequestV1.builder()
                .hash(updatedHash).userPassword(UPDATED_PASSWORD).build();

        this.authenticateApi.updateForgottenPasswordVoidResponse(this.alphaTestUser, update, 200);

        THEN("The platform will store the password and return a 200 response");
        this.authenticateApi.loginUserViaTelephoneNumberVoid(this.alphaTestUser, UserLoginRequest.builder()
                .password(UPDATED_PASSWORD)
                .phoneNumber(this.alphaTestUser.getUserTelephone())
                .deviceId(this.alphaTestUser.getDeviceId()).build(), 200);

        AND("The platform will unlock the user in case the user was locked");
        ValidatableResponse error = this.authenticateApi.loginUserViaTelephoneNumberError(alphaTestUser, UserLoginRequest.builder()
                .password(this.alphaTestUser.getUserPassword())
                .phoneNumber(this.alphaTestUser.getUserTelephone())
                .deviceId(this.alphaTestUser.getDeviceId()).build(), 401);

        DONE();
    }

    @Test
    public void happy_path_store_new_password_for_unlocked_user_200_success() throws JsonProcessingException {

        TEST("AHBDB-13311 Test failed");

        TEST("AHBDB-1365: AC1 Store new password - 200 success");
        TEST("AHBDB-4215: AC1 Positive Test - Happy Patch Scenario Store new password for unlocked user - 200 success");
        setupTestUser();
        GIVEN("The client has begun the forgot passcode journey");
        AND("The customer has successfully validated the phone number via OTP");

        WHEN("The client attempts to update the user with a new valid password");
        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, ResetPasswordRequest.builder().phoneNumber(this.alphaTestUser.getUserTelephone()).build()).getHash();

        String simulatedOtpPasswordReset = this.developmentSimulatorService.retrieveOtpFromDevSimulator2(resetPasswordHash, this.alphaTestUser.getLoginResponse().getAccessToken()).getPassword();

        ValidateResetPasswordRequest validate = ValidateResetPasswordRequest.builder()
                .otp(simulatedOtpPasswordReset).hash(resetPasswordHash).build();

        String updatedHash = this.authenticateApi.validateResetPasswordOtp(this.alphaTestUser, validate).getHash();
        Assertions.assertNotNull(updatedHash);

        UpdateForgottenPasswordRequestV1 update = UpdateForgottenPasswordRequestV1.builder()
                .hash(updatedHash).userPassword(UPDATED_PASSWORD).build();

        this.authenticateApi.updateForgottenPasswordVoidResponse(this.alphaTestUser, update, 200);

        THEN("The platform will store the password and return a 200 response");
        this.authenticateApi.loginUserViaTelephoneNumberVoid(this.alphaTestUser, UserLoginRequest.builder()
                .password(UPDATED_PASSWORD)
                .phoneNumber(this.alphaTestUser.getUserTelephone())
                .deviceId(this.alphaTestUser.getDeviceId()).build(), 200);

        AND("The platform will unlock the user in case the user was locked");

        ValidatableResponse error = this.authenticateApi.loginUserViaTelephoneNumberError(alphaTestUser, UserLoginRequest.builder()
                .password(this.alphaTestUser.getUserPassword())
                .phoneNumber(this.alphaTestUser.getUserTelephone())
                .deviceId(this.alphaTestUser.getDeviceId()).build(), 401);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1234567", "hagvd"})
    public void invalid_password_password_400_bad_request(String invalidPassword) {
        TEST("AHBDB-1365: AC2 - Invalid password - 400 bad request");
        TEST("AHBDB-4217: AC2 Negative Test - Invalid password - Password - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a password against the user");
        AND("The password does not satisfy the validation in the data table");

        WHEN("The client updates the user with this password");
        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, ResetPasswordRequest.builder().phoneNumber(this.alphaTestUser.getUserTelephone()).build()).getHash();

        String simulatedOtpPasswordReset = this.developmentSimulatorService.retrieveOtpFromDevSimulator2(resetPasswordHash, this.alphaTestUser.getLoginResponse().getAccessToken()).getPassword();

        ValidateResetPasswordRequest validate = ValidateResetPasswordRequest.builder()
                .otp(simulatedOtpPasswordReset).hash(resetPasswordHash).build();

        String updatedHash = this.authenticateApi.validateResetPasswordOtp(this.alphaTestUser, validate).getHash();

        THEN("The Status Code is 400");
        UpdateForgottenPasswordRequestV1 update = UpdateForgottenPasswordRequestV1.builder()
                .hash(updatedHash).userPassword(invalidPassword).build();

        OBErrorResponse1 error = this.authenticateApi.updateForgottenPasswordErrorResponse(this.alphaTestUser, update, 400);

        Assertions.assertTrue(error.getCode().equals(ERROR_CODE_PASSWORD_LENGTH), "Error message was not as expected, " +
                "test expected : " + ERROR_CODE_PASSWORD_LENGTH);
        Assertions.assertTrue(error.getMessage().contains(PASSWORD_LENGTH_ERROR), "Error message was not as expected, " +
                "test expected : " + PASSWORD_LENGTH_ERROR);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hash", "UserPassword"})
    public void missing_mandatory_fields_400_bad_request(String fieldToRemove) throws JSONException {
        TEST("AHBDB-1365: Update password - marketplace user (forgot passcode journey)");
        TEST("AHBDB-4220: Negative Test - Missing Mandatory Fields - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a password against the user");
        AND("The password does not satisfy the validation in the data table");

        WHEN("The client updates the user with this password");
        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, ResetPasswordRequest.builder().phoneNumber(this.alphaTestUser.getUserTelephone()).build()).getHash();

        String simulatedOtpPasswordReset = this.developmentSimulatorService.retrieveOtpFromDevSimulator2(resetPasswordHash, this.alphaTestUser.getLoginResponse().getAccessToken()).getPassword();

        ValidateResetPasswordRequest validate = ValidateResetPasswordRequest.builder()
                .otp(simulatedOtpPasswordReset).hash(resetPasswordHash).build();

        String updatedHash = this.authenticateApi.validateResetPasswordOtp(this.alphaTestUser, validate).getHash();

        THEN("The Status Code is 400");
        JSONObject update = new JSONObject() {
            {
                put("Hash", updatedHash);
                put("Hash", alphaTestUser.getUserPassword());
            }
        };
        update.remove(fieldToRemove);

        OBErrorResponse1 error = this.authenticateApi.updateForgottenPasswordInvalidJSON(this.alphaTestUser, update, 400);

        Assertions.assertTrue(error.getCode().equals(ERROR_CODE_PASSWORD_LENGTH), "Error message was not as expected, " +
                "test expected : " + ERROR_CODE_PASSWORD_LENGTH);
        Assertions.assertTrue(error.getMessage().contains(PASSWORD_MISSING_FIELDS), "Error message was not as expected, " +
                "test expected : " + PASSWORD_MISSING_FIELDS);
        DONE();
    }
}
