package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

/**
 * Created by Bharat Gatty on 11/03/2021
 */

@Tag("@BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ForgotPasswordTest {

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private OtpApi otpApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private final String OTP_INVALID_ERROR_CODE = "UAE.ERROR.BAD_REQUEST";
    private final String OTP_INVALID_ERROR_MESSAGE = "The OTP provided is invalid or may have expired";

    private final String OTP_MISSING_ERROR_CODE = "0004";
    private final String OTP_MISSING_ERROR_MESSAGE = "No OTP found";

    private final String REQUEST_VALIDATION_ERROR_CODE = "REQUEST_VALIDATION";
    private final String REQUEST_VALIDATION_ERROR_MESSAGE = "resetPassword.input.phoneNumber: must not be blank";

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private void setupUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    @Test
    @Order(1)
    void forgot_password_generate_otp_test_happy_path() {
        TEST("AHBDB-3329: AC1 - Generation of OTP - 200 Ok");
        TEST("AHBDB-4081: AC1 Positive Test - Happy Patch Scenario - Generation of OTP - 200 Ok");
        TEST("AHBDB-3994: AC4 Positive Test - Happy Path Scenario - Validation of OTP - 200 OK");
        setupUser();
        GIVEN("a customer exists with a phone number");
        WHEN("the client initiates a request to reset password with a valid phone number");
        final ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        ResetPasswordResponse resetPasswordResponse = authenticateApi.initiateResetPassword(alphaTestUser,
                resetPasswordRequest);
        THEN("Expect an hash to be returned");
        THEN("an OTP is sent to the phone number");
        AND("we return a 200 ok with a hash in the body");
        assertTrue(isNotBlank(resetPasswordResponse.getHash()));

        AND("An otp is generated for the user");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPasswordResponse.getHash());
        assertTrue(isNotBlank(otpCO.getPassword()));

        WHEN("WHEN the client initiates a request with a hash and OTP to validate");
        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPasswordResponse.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApi.validateResetPasswordOtp(alphaTestUser, validateResetPasswordRequest);
        THEN("THEN we response with a 200 with the hash as the body");
        assertTrue(isNotBlank(validateResetPasswordResponse.getHash()));

        // set new passcode for the customer
        alphaTestUser.setUserPassword(validateResetPasswordResponse.getHash());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+44701234567", "+22521234567", "+240541234567", "+1123456789", "+27123456789",
            "+91123456789"})
    @Order(1)
    void forgot_password_invalid_phone_number_test_negative_path(String invalidNumberCountryCode) {
        TEST("AHBDB-3329: AC2 Generation of OTP - Invalid phone number - 400 Bad request");
        TEST("AHBDB-4086: AC2 Negative Test - Generation of OTP - Invalid phone number - 400 Bad request");
        setupUser();
        GIVEN("a customer exists with a phone number");
        WHEN("the client initiates a request to reset password with a invalid phone number");
        final ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .phoneNumber(invalidNumberCountryCode)
                .build();

        THEN("400 Bad request is returned");
        OBErrorResponse1 obErrorResponse1 = authenticateApi.initiateResetPasswordNegativeFlow(alphaTestUser,
                resetPasswordRequest, 400);
        DONE();
    }

    @Test
    @Order(1)
    public void validation_of_OTP_invalid_OTP_using_invalid_hash_400_bad_request() {
        TEST("AHBDB-3329: AC5 Validation of OTP - Invalid OTP - 400 Bad request");
        TEST("AHBDB-3996: AC5 Negative Test - Validation of OTP - Invalid OTP using invalid Hash - 400 Bad request");
        setupUser();
        GIVEN("We have sent the customer an OTP");
        AND("The customer doesn't have a bank account");
        final ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        String resetPasswordHash =
                this.authenticateApi.initiateResetPassword(alphaTestUser, resetPasswordRequest).getHash();
        assertTrue(isNotBlank(resetPasswordHash));

        String otp = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPasswordHash).getPassword();
        assertTrue(isNotBlank(otp));

        WHEN("The client initiates a request with an invalid hash");
        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash("invalid")
                .otp(otp)
                .build();
        OBErrorResponse1 error = authenticateApi.validateResetPasswordOtpError(alphaTestUser,
                validateResetPasswordRequest, 400);

        THEN("The Status Code is 400 Bad Request");

        Assertions.assertEquals(error.getCode(), OTP_INVALID_ERROR_CODE, "The Error Code was not expected, " +
                "expected error code: " + OTP_INVALID_ERROR_CODE);
        Assertions.assertEquals(error.getMessage(), OTP_INVALID_ERROR_MESSAGE, "The Error Message was not expected, " +
                "expected error message: " + OTP_INVALID_ERROR_MESSAGE);
        DONE();
    }

    @Test
    @Order(10)
    public void happy_path_generation_of_OTP_customer_not_found_200_OK() {
        TEST("Ignored because of defect AHBDB-7862 - FIXED");


        TEST("AHBDB-3329: AC3 Generation of OTP - Customer not found - 200 OK");
        TEST("AHBDB-4087: AC3 Positive Test - Happy Path Scenario - Generation of OTP - Customer not found - 200 OK");
        setupUser();
        GIVEN("A customer does not exist against a phone number");
        this.customerApi.deleteCustomer(this.alphaTestUser);

        WHEN("The client initiates a request to reset password against that phone number");
        ResetPasswordRequest reset = ResetPasswordRequest.builder()
                .phoneNumber(this.alphaTestUser.getUserTelephone()).build();

        THEN("200 is returned with a hash in body but no SMS is sent");
        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, reset).getHash();
        Assertions.assertTrue(isNotBlank(resetPasswordHash));

        OBErrorResponse1 error =
                this.developmentSimulatorService.retrieveOtpFromDevSimulatorErrorResponse(resetPasswordHash,
                        this.alphaTestUser.getLoginResponse().getAccessToken(), 404);
        Assertions.assertEquals(error.getCode(), OTP_MISSING_ERROR_CODE, "The Error Code was not expected, " +
                "expected error code: " + OTP_MISSING_ERROR_CODE);
        Assertions.assertEquals(error.getMessage(), OTP_MISSING_ERROR_MESSAGE, "The Error Message was not expected, " +
                "expected error message: " + OTP_MISSING_ERROR_MESSAGE);
        DONE();
    }

    @Test
    @Order(1)
    public void generation_of_OTP_invalid_phone_number_phone_number_null_400_bad_request() {
        TEST("AHBDB-3329: AC2 Generation of OTP - Invalid phone number - 400 Bad request");
        TEST("AHBDB-4156: AC2 Negative Test - Generation of OTP - Invalid phone number - Phone Number null - 400 Bad " +
                "request");

        GIVEN("A customer exists with a phone number");
        setupUser();

        WHEN("The client initiates a request to reset password with an invalid phone number");
        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .phoneNumber("").build();

        OBErrorResponse1 error = this.authenticateApi.initiateResetPasswordNegativeFlow(this.alphaTestUser,
                resetPasswordRequest, 400);

        THEN("The Status Code is 400 Bad Request");
        Assertions.assertEquals(error.getCode(), REQUEST_VALIDATION_ERROR_CODE, "Error Code was not expected, " +
                "error code expected: " + REQUEST_VALIDATION_ERROR_CODE);
        Assertions.assertEquals(error.getMessage(), REQUEST_VALIDATION_ERROR_MESSAGE, "Error Message was not " +
                "expected, " +
                "error message expected: " + REQUEST_VALIDATION_ERROR_MESSAGE);

        DONE();
    }

    @Test
    @Order(1)
    public void validation_of_OTP_invalid_OTP_using_invalid_OTP_400_bad_request() {
        TEST("AHBDB-3329: AC5 Validation of OTP - Invalid OTP - 400 Bad request");
        TEST("AHBDB-4158: AC5 Negative Test - Validation of OTP - Invalid OTP using invalid Otp - 400 Bad request");

        GIVEN("We have sent the customer an OTP");
        AND("The customer doesn't have a bank account");
        ResetPasswordRequest reset = ResetPasswordRequest.builder()
                .phoneNumber(this.alphaTestUser.getUserTelephone()).build();

        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, reset).getHash();
        Assertions.assertTrue(isNotBlank(resetPasswordHash));

        String simulatedOtpPasswordReset =
                this.developmentSimulatorService.retrieveOtpFromDevSimulator2(resetPasswordHash,
                        this.alphaTestUser.getLoginResponse().getAccessToken()).getPassword();

        WHEN("The client initiates a request with an invalid OTP");
        ValidateResetPasswordRequest request = ValidateResetPasswordRequest.builder()
                .otp("000000").hash(resetPasswordHash).build();

        OBErrorResponse1 error = this.authenticateApi.validateResetPasswordOtpError(this.alphaTestUser, request, 400);

        THEN("The Status Code is 400 Bad Request");
        Assertions.assertEquals(error.getCode(), OTP_INVALID_ERROR_CODE, "The Error Code was not expected, " +
                "expected error code: " + OTP_INVALID_ERROR_CODE);
        Assertions.assertEquals(error.getMessage(), OTP_INVALID_ERROR_MESSAGE, "The Error Message was not expected, " +
                "expected error message: " + OTP_INVALID_ERROR_MESSAGE);

        DONE();
    }

    @Test
    @Order(1)
    public void missing_mandatory_phone_number_400_response() {
        TEST("AHBDB-3329: Add forgot password functionality on Auth (Generation and Validation)");
        TEST("AHBDB-4159: Missing Mandatory Phone Number - 400 response");

        GIVEN("A customer exists without a phone number");
        ResetPasswordRequest reset = ResetPasswordRequest.builder().build();

        WHEN("The client initiates a request to reset password");
        OBErrorResponse1 error = this.authenticateApi.initiateResetPasswordNegativeFlow(this.alphaTestUser, reset, 400);

        THEN("The Status Code is 400 Bad Request");
        Assertions.assertEquals(error.getCode(), REQUEST_VALIDATION_ERROR_CODE, "Error Code was not expected, " +
                "error code expected: " + REQUEST_VALIDATION_ERROR_CODE);
        Assertions.assertEquals(error.getMessage(), REQUEST_VALIDATION_ERROR_MESSAGE, "Error Message was not " +
                "expected, " +
                "error message expected: " + REQUEST_VALIDATION_ERROR_MESSAGE);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hash", "Otp"})
    @Order(1)
    public void missing_mandatory_fields_400_response(String missing) throws JSONException {
        TEST("AHBDB-3329: Add forgot password functionality on Auth (Generation and Validation)");
        TEST("AHBDB-4161: Missing Mandatory Fields - 400 response");
        ResetPasswordRequest reset = ResetPasswordRequest.builder()
                .phoneNumber(this.alphaTestUser.getUserTelephone()).build();

        String resetPasswordHash = this.authenticateApi.initiateResetPassword(this.alphaTestUser, reset).getHash();
        Assertions.assertTrue(isNotBlank(resetPasswordHash));

        String simulatedOtpPasswordReset =
                this.developmentSimulatorService.retrieveOtpFromDevSimulator2(resetPasswordHash,
                        this.alphaTestUser.getLoginResponse().getAccessToken()).getPassword();

        GIVEN("A customer exists without a mandatory field");
        JSONObject obj = this.authenticateApi.buildJSONObjectForMissingMandatoryField(resetPasswordHash,
                simulatedOtpPasswordReset);
        obj.remove(missing);

        WHEN("The client initiates a request to reset password");
        OBErrorResponse1 error = this.authenticateApi.validateResetPasswordOtpJSON(obj, 400);

        THEN("The Status Code is 400 Bad Request");
        Assertions.assertEquals(error.getCode(), REQUEST_VALIDATION_ERROR_CODE, "Error Code was not expected, " +
                "error code expected: " + REQUEST_VALIDATION_ERROR_CODE);
        Assertions.assertEquals(error.getMessage(), "validateResetPasswordOtp.input." + missing.toLowerCase() + ": " +
                        "must not be blank",
                "Error Message was not expected, error message expected: validateResetPasswordOtp.input." + missing.toLowerCase() + ": must not be blank");

        DONE();
    }
}
