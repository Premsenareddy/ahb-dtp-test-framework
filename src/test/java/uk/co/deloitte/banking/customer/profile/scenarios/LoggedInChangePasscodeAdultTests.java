package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.User;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoggedInChangePasscodeAdultTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;

    private static final int loginMinWeightExpected = 31;
    private static final String AUTH_STEP_403_RESPONSE = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";
    private static final String AUTH_STEP_423_RESPONSE = "UAE.OTP.LIMIT_REACHED";
    private static final String CHANGE_PASSWORD_403_RESPONSE = "UAE.ACCOUNT.FORBIDDEN";
    private static final String CHANGE_PASSWORD_403_MESSAGE = "Not authorised to update user";
    private static final String WEIGHT_ERROR_MESSAGE = "Minimum weight required is 11";
    private static final String WEIGHT_ERROR_CODE = "UAE.AUTH.MIN_WEIGHT_REQUIRED";
    private static final String ERROR_CODE_PASSWORD_LENGTH = "REQUEST_VALIDATION";

    private void setupTestUser() {
        alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
    }

    private void setupTestUserForInvalidPasswordTest() {
        if (alphaTestUser2 == null) {
            alphaTestUser2 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

            authenticateApi.stepUpUserAuthInitiate(alphaTestUser2, StepUpAuthInitiateRequest.builder()
                    .weight(loginMinWeightExpected)
                    .scope(CUSTOMER_SCOPE)
                    .build());

            StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                    .password(alphaTestUser2.getUserPassword())
                    .scope(CUSTOMER_SCOPE)
                    .weight(loginMinWeightExpected)
                    .build();

            authenticateApi.validateUserStepUpAuth(alphaTestUser2, stepUpAuthValidationRequest);
        }
    }

    @Test
    public void happy_path_marketplace_adult_update_new_password_200_response() {
        TEST("AHBDB-1882: AC1 Store new password - 200 success");
        TEST("AHBDB-3858: AC1 Positive Test - Happy Path Store new password - 200 success");
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11186: AC1 - User authentication with existing password - 200 OK - Step up auth (already built) " +
                "and AC2 - Set new password - 200 OK - ADULT");

        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUser();

        AND("AND the customer has an elevated weight of 11");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected)
                .scope(CUSTOMER_SCOPE)
                .build());

        StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password(alphaTestUser.getUserPassword())
                .scope(CUSTOMER_SCOPE)
                .weight(loginMinWeightExpected)
                .build();

        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);

        WHEN("The client attempts to update the password for the customer with a valid password");

        String oldPassword = alphaTestUser.getUserPassword();

        String newPassword = "newValidPassword123";

        UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();

        User updatedUser = authenticateApi.patchUserCredentials(alphaTestUser, updateUserRequestV1);
        assertNotNull(updatedUser);
        alphaTestUser.setUserPassword(newPassword);

        THEN("The platform returns a 200 OK");

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .loginUserProtected(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), true);
        assertEquals(CUSTOMER_SCOPE, userLoginResponseV2.getScope());
        assertEquals(alphaTestUser.getUserId(), userLoginResponseV2.getUserId());

        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        THEN("The platform returns a 401 response if we try the old password");
        UserLoginRequestV2 userLoginRequestOldPassword = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(oldPassword)
                .build();

        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestOldPassword, 401);
        DONE();
    }

    @Test
    public void happy_path_banking_adult_update_password_200_response() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11186: AC1 - User authentication with existing password - 200 OK - Step up auth (already built) " +
                "and AC2 - Set new password - 200 OK - ADULT");
        setupTestUser();
        alphaTestUserBankingCustomerFactory.setUpBankingCustomer(alphaTestUser);
        GIVEN("A banking customer has been authenticated (completed login flow)");
        AND("AND the customer has an elevated weight of 11");
        StepUpAuthInitiateRequest stepUpAuthInitiateRequest2 = StepUpAuthInitiateRequest
                .builder().weight(loginMinWeightExpected)
                .scope(ACCOUNT_SCOPE)
                .build();

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, stepUpAuthInitiateRequest2);

        final StepUpAuthRequest stepUpAuthValidationRequest2 = StepUpAuthRequest.builder()
                .password(alphaTestUser.getUserPassword())
                .scope(ACCOUNT_SCOPE)
                .weight(loginMinWeightExpected).build();

        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest2);

        WHEN("The client attempts to update the password for the customer with a valid password");
        String oldPassword2 = alphaTestUser.getUserPassword();

        String newPassword2 = "bankingUserNewValidPassword123";

        UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .userPassword(newPassword2)
                .build();

        THEN("The platform returns a 200 OK");
        User updatedUser2 = authenticateApi.patchUserCredentials(alphaTestUser, updateUserRequest);
        Assertions.assertNotNull(updatedUser2);
        alphaTestUser.setUserPassword(newPassword2);

        UserLoginRequestV2 userLoginRequest = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        UserLoginResponseV2 userLoginResponse = authenticateApi
                .loginUserProtected(alphaTestUser, userLoginRequest, alphaTestUser.getDeviceId(), true);
        assertEquals(ACCOUNT_SCOPE, userLoginResponse.getScope());
        assertEquals(alphaTestUser.getUserId(), userLoginResponse.getUserId());
        parseLoginResponse(alphaTestUser, userLoginResponse);

        THEN("The platform returns a 401 response if we try the old password");
        UserLoginRequestV2 userLoginRequestOldPassword2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(oldPassword2)
                .build();
        authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestOldPassword2, 401);
        DONE();
    }

    @Test
    public void marketplace_adult_incorrect_existing_password() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11187: AC3 - Incorrect existing password (under 3 attempts) - 403 Forbidden - Step up auth " +
                " AC4 Incorrect existing password (3 or more attempts) - 423 Locked - Step up auth - MARKETPLACE ADULT");

        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUser();

        AND("AND the customer has an elevated weight of 11");
        StepUpAuthInitiateRequest request = StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected)
                .scope(CUSTOMER_SCOPE)
                .build();

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, request);

        AND("The existing password supplied by the user is incorrect");
        StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword")
                .scope(CUSTOMER_SCOPE)
                .weight(loginMinWeightExpected)
                .build();

        AND("The user has made less than 3 attempts or less");
        WHEN("Authenticate the customer using step up auth (re-authenticate the user) ");
        THEN("DTP returned error response 403 forbidden");
        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        AND("If the user has made 3 attempts the user will be logged out ");
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestV2, 423);

        THEN("DTP returned error response 423 locked account");
        OBErrorResponse1 errorResponse4 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());
        DONE();
    }

    @Test
    public void unlock_marketplace_adult() throws InterruptedException {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11188: Unlock the Marketplace Adult");
        /**
         * TODO :: Ignoring as it takes too long because it involves waiting for 2 minutes to unlock the customer
         */
        envUtils.ignoreTestInEnv(Environments.ALL);

        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUser();

        AND("AND the customer has an elevated weight of 11");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected)
                .scope(CUSTOMER_SCOPE)
                .build());

        AND("The user has made less than 3 attempts with an invalid password");
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword")
                .scope(CUSTOMER_SCOPE)
                .weight(loginMinWeightExpected)
                .build();

        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        AND("The user is locked");
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestV2, 423);

        OBErrorResponse1 errorResponse4 =
                authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());

        WHEN("The time has passed after the customer is locked");
        TimeUnit.SECONDS.sleep(130);

        THEN("The customer is unlocked and can login again");
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .loginUserProtected(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), true);
        assertEquals(CUSTOMER_SCOPE, userLoginResponseV2.getScope());
        assertEquals(alphaTestUser.getUserId(), userLoginResponseV2.getUserId());
        DONE();
    }

    @Test
    public void banking_adult_incorrect_existing_password() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11189: AC3 - Incorrect existing password (under 3 attempts) - 403 Forbidden - Step up auth and " +
                "AC4 Incorrect existing password (3 or more attempts) - 423 Locked - Step up auth- BANKING ADULT ");

        GIVEN("A banking customer has been authenticated (completed login flow)");
        setupTestUser();
        alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        AND("AND the banking customer has an elevated weight of 11");
        StepUpAuthInitiateRequest request = StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected)
                .scope(ACCOUNT_SCOPE)
                .build();

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, request);

        AND("The existing password supplied by the user is incorrect");
        StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword")
                .scope("accounts")
                .weight(loginMinWeightExpected)
                .build();

        AND("The user has made less than 3 attempts or less");
        WHEN("Authenticate the customer using step up auth (re-authenticate the user) ");
        THEN("DTP returned error response 403 forbidden");
        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi

                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        AND("If the user has made 3 attempts the user will be logged out ");
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestV2, 423);

        THEN("DTP returned error response 423 locked account");
        OBErrorResponse1 errorResponse4 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());

        DONE();
    }

    @Test
    public void unlock_banking_adult() throws InterruptedException {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11190: AC5 Banking user - Locked account - ADULT");
        /**
         * TODO :: Ignoring as it takes too long because it involves waiting for 2 minutes to unlock the customer
         */
        envUtils.ignoreTestInEnv(Environments.ALL);

        GIVEN("The customer is a banking user ");
        setupTestUser();
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        AND("The customer has made 3 or more unsuccessful passcode attempts");
        StepUpAuthInitiateRequest request = StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected)
                .scope(ACCOUNT_SCOPE)
                .build();

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, request);
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword")
                .scope(ACCOUNT_SCOPE)
                .weight(loginMinWeightExpected)
                .build();

        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        WHEN("The customer’s account is locked");
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestV2, 423);

        OBErrorResponse1 errorResponse4 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());

        TimeUnit.SECONDS.sleep(130);

        THEN("Then the customer’s account will NOT be unlocked automatically after the time expires");
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequestV2, 423);
        DONE();
    }

    @Test
    public void marketplace_and_banking_adult_authentication_without_step_auth_403_forbidden() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11191: AC7 Customer has not done step up auth - 403 Forbidden - ADULT");

        //MARKETPLACE CUSTOMER ADULT
        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUser();

        AND("The customer has an elevated weight of less than 11");
        WHEN("The client attempts to update the password for the customer with a valid password");
        final String newPassword = UUID.randomUUID().toString();
        final UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();

        OBErrorResponse1 updatedUser1 =
                authenticateApi.patchUserCredentialsError(alphaTestUser, updateUserRequest, 403);

        THEN("The platform returns a 403 Forbidden ");
        assertEquals(CHANGE_PASSWORD_403_RESPONSE, updatedUser1.getCode());
        assertEquals(CHANGE_PASSWORD_403_MESSAGE, updatedUser1.getMessage());
        assertEquals(WEIGHT_ERROR_MESSAGE, updatedUser1.getErrors().get(0).getMessage());
        assertEquals(WEIGHT_ERROR_CODE, updatedUser1.getErrors().get(0).getErrorCode());

        // BANKING CUSTOMER ADULT
        GIVEN("A banking customer has been authenticated (completed login flow)");
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        AND("The customer has an elevated weight of less than 11");
        WHEN("The client attempts to update the password for the customer with a valid password");

        OBErrorResponse1 updatedUser2 = authenticateApi.patchUserCredentialsError(alphaTestUser, updateUserRequest, 403);

        THEN("The platform returns a 403 Forbidden ");
        assertEquals(CHANGE_PASSWORD_403_RESPONSE, updatedUser2.getCode());
        assertEquals(CHANGE_PASSWORD_403_MESSAGE, updatedUser2.getMessage());
        assertEquals(WEIGHT_ERROR_MESSAGE, updatedUser2.getErrors().get(0).getMessage());
        assertEquals(WEIGHT_ERROR_CODE, updatedUser2.getErrors().get(0).getErrorCode());
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "abcdefg", "1234567", "!@#$%^&"})
    public void negative_test_update_with_invalid_password(String invalidPassword) {
        TEST("AHBDB-1882: AC2 Invalid password - 400 bad request ");
        TEST("AHBDB-3859: AC2 Invalid password - 400 bad request");

        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUserForInvalidPasswordTest();

        AND("AND the customer has an elevated weight of 11");

        WHEN("The client attempts to update the password for the customer with an invalid password");
        UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(invalidPassword)
                .build();

        OBErrorResponse1 error =
                authenticateApi.patchUserCredentialsError(alphaTestUser2, updateUserRequestV1, 400);
        assertNotNull(error);
        assertEquals(ERROR_CODE_PASSWORD_LENGTH, error.getCode());

        THEN("The platform returns a 400 Response");
        DONE();
    }

    @Test
    public void negative_test_missing_mandatory_password_field() {
        TEST("AHBDB-1882: Exploratory Test ");
        TEST("AHBDB-3860: Missing Mandatory Field - Password");
        setupTestUserForInvalidPasswordTest();
        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        AND("AND the customer has an elevated weight of 11");

        WHEN("The client attempts to update the password for the customer with an invalid password");
        UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(null)
                .build();

        THEN("The platform responds with a 400 Bad request");
        OBErrorResponse1 error =
                authenticateApi.patchUserCredentialsError(alphaTestUser2, updateUserRequestV1, 400);
        assertNotNull(error);
        assertEquals(ERROR_CODE_PASSWORD_LENGTH, error.getCode());
        DONE();
    }
}
