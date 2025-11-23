package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoggedInChangePasscodeChildTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private static final String AUTH_STEP_403_RESPONSE = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";
    private static final String AUTH_STEP_423_RESPONSE = "UAE.OTP.LIMIT_REACHED";
    private static final String CHANGE_PASSWORD_403_RESPONSE = "UAE.ACCOUNT.FORBIDDEN";
    private static final String CHANGE_PASSWORD_403_MESSAGE = "Not authorised to update user";
    private static final String WEIGHT_ERROR_MESSAGE = "Minimum weight required is 11";
    private static final String WEIGHT_ERROR_CODE = "UAE.AUTH.MIN_WEIGHT_REQUIRED";
    private static final String ERROR_CODE_PASSWORD_LENGTH = "REQUEST_VALIDATION";
    private final static String ERROR_MESSAGE_PASSWORD_LENGTH = "If sent, password cannot be blank";
    private final static String ERROR_MESSAGE_PASSWORD_BLANK = "must not be blank";

    private AlphaTestUser alphaTestUserChild;
    private AlphaTestUser alphaTestUserParent;

    private String childId;
    private String connectionId;
    private final String fullName = "testUser";
    private static final int loginMinWeightExpected = 31;

    private void setupTestUsersChild() {
        alphaTestUserParent = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, "validtestpassword");

        connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent, alphaTestUserFactory
                .generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));

        alphaTestUserChild = alphaTestUserFactory
                .createChildCustomer(alphaTestUserParent, new AlphaTestUser(), connectionId, childId);
    }

    private void setupTestUserChild() {
        if (this.alphaTestUserParent == null) {
            alphaTestUserParent = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

            childId = alphaTestUserFactory
                    .createChildInForgerock(alphaTestUserParent, "validtestpassword");

            connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent, alphaTestUserFactory
                    .generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));

            alphaTestUserChild = alphaTestUserFactory
                    .createChildCustomer(alphaTestUserParent, new AlphaTestUser(), connectionId, childId);
        }
    }

    @Test
    public void marketplace_and_banking_child_set_new_password_200_response() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11194: AC1 - User authentication with existing password - 200 OK - Step up auth (already built)  and " +
                "AC2 - Set new password- CHILD ");

        //MARKETPLACE CUSTOMER CHILD
        GIVEN("A marketplace customer child has been authenticated (completed login flow)");
        setupTestUsersChild();

        AND("AND the customer has an elevated weight of 11");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("customer").build());

        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password(alphaTestUserChild.getUserPassword()).scope("customer").weight(loginMinWeightExpected)
                .build();

        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest);

        WHEN("The client attempts to update the password for the customer with a valid password");
        String oldPassword = alphaTestUserChild.getUserPassword();
        final String newPassword = UUID.randomUUID().toString();
        final UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();
        User updatedUser = authenticateApi.patchUserCredentials(alphaTestUserChild, updateUserRequestV1);
        Assertions.assertNotNull(updatedUser);
        alphaTestUserChild.setUserPassword(newPassword);

        THEN("The platform returns a 200 OK");

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(alphaTestUserChild.getUserPassword())
                .build();
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginUserProtected(alphaTestUserChild,
                userLoginRequestV2, alphaTestUserChild.getDeviceId(), true);

        parseLoginResponse(alphaTestUserChild, userLoginResponseV2);

        THEN("The platform returns a 401 response if we try the old password");
        UserLoginRequestV2 userLoginRequestOldPassword = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(oldPassword)
                .build();
        this.authenticateApi.loginUserProtectedError(alphaTestUserChild, userLoginRequestOldPassword, 401);


        // BANKING CUSTOMER CHILD
        GIVEN("A banking customer child has been authenticated (completed login flow)");
        alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, connectionId);

        AND("AND the customer has an elevated weight of 11");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("accounts").build());

        final StepUpAuthRequest stepUpAuthValidationRequest2 = StepUpAuthRequest.builder()
                .password(alphaTestUserChild.getUserPassword()).scope("accounts").weight(loginMinWeightExpected)
                .build();

        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest2);

        WHEN("The client attempts to update the password for the customer with a valid password");
        String oldPassword2 = alphaTestUserChild.getUserPassword();
        final String newPassword2 = UUID.randomUUID().toString();
        final UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .userPassword(newPassword2)
                .build();

        THEN("The platform returns a 200 OK");
        User updatedUser2 = authenticateApi.patchUserCredentials(alphaTestUserChild, updateUserRequest);
        Assertions.assertNotNull(updatedUser2);
        alphaTestUserChild.setUserPassword(newPassword2);

        UserLoginRequestV2 userLoginRequest = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(alphaTestUserChild.getUserPassword())
                .build();
        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(alphaTestUserChild,
                userLoginRequest, alphaTestUserChild.getDeviceId(), true);

        parseLoginResponse(alphaTestUserChild, userLoginResponse);
        THEN("The platform returns a 401 response if we try the old password");

        UserLoginRequestV2 userLoginRequestOldPassword2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(oldPassword2)
                .build();

        authenticateApi.loginUserProtectedError(alphaTestUserChild, userLoginRequestOldPassword2, 401);
        DONE();
    }

    @Test
    public void marketplace_child_incorrect_existing_password() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11196: AC3 - Incorrect existing password (under 3 attempts) - 403 Forbidden - Step up auth and " +
                "AC4 Incorrect existing password (3 or more attempts) - 423 Locked - Step up auth - MARKETPLACE CHILD");

        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUsersChild();

        AND("AND the customer has an elevated weight of 11");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("customer").build());

        AND("The existing password supplied by the user is incorrect");
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword").scope("customer").weight(loginMinWeightExpected).build();

        AND("The user has made less than 3 attempts or less");
        WHEN("Authenticate the customer using step up auth (re-authenticate the user) ");
        THEN("DTP returned error response 403 forbidden");
        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(alphaTestUserChild.getUserPassword())
                .build();

        AND("If the user has made 3 attempts the user will be logged out ");
        this.authenticateApi.loginUserProtectedError(alphaTestUserChild, userLoginRequestV2, 423);

        THEN("DTP returned error response 423 locked account");
        OBErrorResponse1 errorResponse4 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 423);
        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());
        DONE();
    }

    @Test
    public void unlock_marketplace_child() throws InterruptedException {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11197: Unlock the Marketplace Child");
        envUtils.ignoreTestInEnv("Involves waiting 2 minutes to unlock customer", Environments.ALL);

        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUsersChild();

        AND("AND the customer has an elevated weight of 11");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest
                .builder().weight(loginMinWeightExpected).scope("customer").build());

        AND("The user has made less than 3 attempts with an invalid password");
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword").scope("customer").weight(loginMinWeightExpected).build();

        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(alphaTestUserChild.getUserPassword())
                .build();

        AND("The user is locked");
        this.authenticateApi.loginUserProtectedError(alphaTestUserChild, userLoginRequestV2, 423);

        OBErrorResponse1 errorResponse4 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 423);
        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());

        WHEN("The time has passed after the customer is locked");
        TimeUnit.SECONDS.sleep(130);

        THEN("The customer is unlocked and can login again");
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .loginUserProtected(alphaTestUserChild, userLoginRequestV2, alphaTestUserChild.getDeviceId(), true);
        assertNotNull(alphaTestUserChild.getUserId(), userLoginResponseV2.getUserId());
        DONE();
    }

    @Test
    public void banking_child_incorrect_existing_password() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11198: AC3 - Incorrect existing password (under 3 attempts) - 403 Forbidden - Step up auth and " +
                "AC4 Incorrect existing password (3 or more attempts) - 423 Locked - Step up auth- BANKING CHILD ");

        GIVEN("A banking customer has been authenticated (completed login flow)");
        setupTestUsersChild();
        alphaTestUserBankingCustomerFactory
                .setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, connectionId);

        AND("AND the banking customer has an elevated weight of 11");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("accounts").build());

        AND("The existing password supplied by the user is incorrect");
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword").scope("accounts").weight(loginMinWeightExpected)
                .build();

        AND("The user has made less than 3 attempts or less");
        WHEN("Authenticate the customer using step up auth (re-authenticate the user) ");
        THEN("DTP returned error response 403 forbidden");
        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(alphaTestUserChild.getUserPassword())
                .build();

        AND("If the user has made 3 attempts the user will be logged out ");
        this.authenticateApi.loginUserProtectedError(alphaTestUserChild, userLoginRequestV2, 423);


        THEN("DTP returned error response 423 locked account");
        OBErrorResponse1 errorResponse4 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 423);
        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());
        DONE();
    }

    @Test
    public void unlock_banking_child() throws InterruptedException {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11199: AC5 Banking user - Locked account -CHILD");
        /**
         * TODO :: Ignoring as it takes too long because it involves waiting for 2 minutes to unlock the customer
         */
        envUtils.ignoreTestInEnv(Environments.ALL);

        GIVEN("The customer is a banking user ");
        setupTestUsersChild();
        alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, connectionId);

        AND("The customer has made 3 or more unsuccessful passcode attempts");

        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest
                .builder().weight(loginMinWeightExpected).scope("accounts").build());

        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder()
                .password("incorrectPassword").scope("accounts").weight(loginMinWeightExpected).build();

        OBErrorResponse1 errorResponse1 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse1.getCode());

        OBErrorResponse1 errorResponse2 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse2.getCode());

        OBErrorResponse1 errorResponse3 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 403);
        assertEquals(AUTH_STEP_403_RESPONSE, errorResponse3.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUserChild.getUserId())
                .password(alphaTestUserChild.getUserPassword())
                .build();

        WHEN("The customer’s account is locked");
        this.authenticateApi.loginUserProtectedError(alphaTestUserChild, userLoginRequestV2, 423);

        OBErrorResponse1 errorResponse4 = authenticateApi
                .validateUserStepUpAuthError(alphaTestUserChild, stepUpAuthValidationRequest, 423);

        assertEquals(AUTH_STEP_423_RESPONSE, errorResponse4.getCode());

        TimeUnit.SECONDS.sleep(130);

        THEN("Then the customer’s account will NOT be unlocked automatically after the time expires");
        this.authenticateApi.loginUserProtectedError(alphaTestUserChild, userLoginRequestV2, 423);
        DONE();
    }

    @Test
    public void marketplace_and_banking_child_authentication_without_step_auth_403_forbidden() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11200: AC7 Customer has not done step up auth - 403 Forbidden");

        //MARKETPLACE CUSTOMER CHILD
        GIVEN("A marketplace customer has been authenticated (completed login flow)");
        setupTestUsersChild();

        AND("The customer has an elevated weight of less than 11");
        WHEN("The client attempts to update the password for the customer with a valid password");
        final String newPassword = UUID.randomUUID().toString();
        final UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();

        OBErrorResponse1 updatedUser1 = authenticateApi
                .patchUserCredentialsError(alphaTestUserChild, updateUserRequest, 403);

        THEN("The platform returns a 403 Forbidden ");
        assertEquals(CHANGE_PASSWORD_403_RESPONSE, updatedUser1.getCode());
        assertEquals(CHANGE_PASSWORD_403_MESSAGE, updatedUser1.getMessage());
        assertEquals(WEIGHT_ERROR_MESSAGE, updatedUser1.getErrors().get(0).getMessage());
        assertEquals(WEIGHT_ERROR_CODE, updatedUser1.getErrors().get(0).getErrorCode());

        // BANKING CUSTOMER CHILD
        GIVEN("A banking customer has been authenticated (completed login flow)");
        alphaTestUserBankingCustomerFactory
                .setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, connectionId);

        AND("The customer has an elevated weight of less than 11");
        WHEN("The client attempts to update the password for the customer with a valid password");

        OBErrorResponse1 updatedUser2 =
                authenticateApi.patchUserCredentialsError(alphaTestUserChild, updateUserRequest, 403);

        THEN("The platform returns a 403 Forbidden ");
        assertEquals(CHANGE_PASSWORD_403_RESPONSE, updatedUser2.getCode());
        assertEquals(CHANGE_PASSWORD_403_MESSAGE, updatedUser2.getMessage());
        assertEquals(WEIGHT_ERROR_MESSAGE, updatedUser2.getErrors().get(0).getMessage());
        assertEquals(WEIGHT_ERROR_CODE, updatedUser2.getErrors().get(0).getErrorCode());

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"", "123", "abcdefg", "1234567", "!@#$%^&"})
    public void marketplace_and_banking_adult_child_invalid_new_password_400_response(String invalidPassword) {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11191: AC8 Invalid password - 400 bad request ");
        setupTestUserChild();
        //MARKETPLACE CUSTOMER CHILD

        final UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(invalidPassword)
                .build();

        GIVEN("The marketplace client wants to store a new password against a user ");

        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("customer").build());

        final StepUpAuthRequest stepUpAuthValidationRequest3 = StepUpAuthRequest.builder()
                .password(alphaTestUserChild.getUserPassword()).scope("customer").weight(loginMinWeightExpected)
                .build();

        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest3);

        AND("The new password does not satisfy the validation in the data table ");

        WHEN("The client updates the user with this new password");
        OBErrorResponse1 errorResponse3 =
                authenticateApi.patchUserCredentialsError(alphaTestUserChild, updateUserRequestV1, 400);

        THEN("The platform returns a 400 Bad request");
        assertEquals(ERROR_CODE_PASSWORD_LENGTH, errorResponse3.getCode());
        assertTrue(errorResponse3.getMessage().contains(ERROR_MESSAGE_PASSWORD_LENGTH));

        // BANKING CUSTOMER CHILD
        GIVEN("The banking client wants to store a new password against a user ");
        alphaTestUserBankingCustomerFactory
                .setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, connectionId);

        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("accounts").build());

        final StepUpAuthRequest stepUpAuthValidationRequest4 = StepUpAuthRequest.builder()
                .password(alphaTestUserChild.getUserPassword()).scope("accounts").weight(loginMinWeightExpected)
                .build();

        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest4);

        AND("The new password does not satisfy the validation in the data table ");

        WHEN("The client updates the user with this new password");
        OBErrorResponse1 errorResponse4 =
                authenticateApi.patchUserCredentialsError(alphaTestUserChild, updateUserRequestV1, 400);

        THEN("The platform returns a 400 Bad request");
        assertEquals(ERROR_CODE_PASSWORD_LENGTH, errorResponse4.getCode());
        assertTrue(errorResponse4.getMessage().contains(ERROR_MESSAGE_PASSWORD_LENGTH));
        DONE();
    }

    @Order(2)
    @Test
    public void marketplace_and_banking_adult_and_child_null_new_password_400_response() {
        TEST("AHBDB-8656: Logged in Change Passcode");
        TEST("AHBDB-11193: AC8 Missing password - 400 bad request ");

        setupTestUsersChild();

        //MARKETPLACE CUSTOMER CHILD
        UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(null)
                .build();
        GIVEN("The marketplace client wants to store a new password against a user ");

        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("customer").build());

        final StepUpAuthRequest stepUpAuthValidationRequest3 = StepUpAuthRequest.builder()
                .password(alphaTestUserChild.getUserPassword()).scope("customer").weight(loginMinWeightExpected)
                .build();

        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest3);

        AND("The new password does not satisfy the validation in the data table ");

        WHEN("The client updates the user with this new password");
        OBErrorResponse1 errorResponse3 =
                authenticateApi.patchUserCredentialsError(alphaTestUserChild, updateUserRequestV1, 400);

        THEN("The platform returns a 400 Bad request");
        assertEquals(ERROR_CODE_PASSWORD_LENGTH, errorResponse3.getCode());
        assertTrue(errorResponse3.getMessage().contains(ERROR_MESSAGE_PASSWORD_BLANK));

        // BANKING CUSTOMER CHILD
        GIVEN("The banking client wants to store a new password against a user ");
        alphaTestUserBankingCustomerFactory
                .setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, connectionId);

        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder()
                .weight(loginMinWeightExpected).scope("accounts").build());

        final StepUpAuthRequest stepUpAuthValidationRequest4 = StepUpAuthRequest.builder()
                .password(alphaTestUserChild.getUserPassword()).scope("accounts").weight(loginMinWeightExpected)
                .build();

        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest4);

        AND("The new password does not satisfy the validation in the data table ");

        WHEN("The client updates the user with this new password");
        OBErrorResponse1 errorResponse4 = authenticateApi
                .patchUserCredentialsError(alphaTestUserChild, updateUserRequestV1, 400);

        THEN("The platform returns a 400 Bad request");
        assertEquals(ERROR_CODE_PASSWORD_LENGTH, errorResponse4.getCode());
        assertTrue(errorResponse4.getMessage().contains(ERROR_MESSAGE_PASSWORD_BLANK));
        DONE();
    }
}
