package uk.co.deloitte.banking.customer.otp.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DestinationRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ErrorResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;

import javax.inject.Inject;
import java.security.SecureRandom;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PhoneNumberOTPCheckApiTest {

    @Inject
    private OtpApi otpApi;

    @Inject
    private AuthenticateApi authenticateApiTest;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    SecureRandom random = new SecureRandom();

    private static final String INVALID_NUMBER_ERROR = "Destination is not a valid phone number";

    private static final String EMPTY_FIELD_ERROR = "must not be empty";


    public void createUserWithToken() {
        if (alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
            final LoginResponse response = this.authenticateApiTest.authenticateDevice(alphaTestUser);
            Assertions.assertNotNull(response);
            alphaTestUser.setLoginResponse(response);
            alphaTestUser.setUserId(response.getUserId());
            Assertions.assertNotNull(alphaTestUser.getLoginResponse());
            Assertions.assertNotNull(alphaTestUser.getLoginResponse().getAccessToken());
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"+55550", "+55552", "+55554", "+55555", "+55556", "+55558"})
    public void user_with_valid_number_receives_204_response(String validNumber) {

        String number = validNumber + random.ints(1_000_000, 9_999_999).findFirst().getAsInt();

        TEST("AHBDB-212 - user with a valid destination number receives a 204 response");
        TEST("user_with_valid_number_receives_204_response " + number);

        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has a valid destination number : " + number);
        alphaTestUser.setUserTelephone(number);

        NOTE("destination number " + alphaTestUser.getUserTelephone());

        THEN("The client submits the destination payload and receives a 204 response");
        otpApi.sendDestinationToOTP(alphaTestUser, 204);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+44701234567", "+22521234567", "+240541234567", "+1123456789", "+27123456789",
            "+84123456789"})
    public void user_with_an_invalid_number_receives_400_response_wrong_countrycode(String invalidNumberCountryCode) {

        TEST("AHBDB-212 - user with an invalid number without +971 country code receives a 400");
        TEST("user_with_an_invalid_number_receives_400_response_wrong_countrycode " + invalidNumberCountryCode);

        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has an invalid destination number and a valid type");
        final DestinationRequest destinationRequest = DestinationRequest.builder()
                .destination(invalidNumberCountryCode)
                .build();


        NOTE("destination number " + invalidNumberCountryCode);

        THEN("The client submits the destination payload and receives a 400 response");
        final ErrorResponse response = otpApi.sendDestinationToOTPError(alphaTestUser, destinationRequest, 400);
        Assertions.assertTrue(response.getMessage().contains(INVALID_NUMBER_ERROR), "Error message was not as " +
                "expected, test expected : " + INVALID_NUMBER_ERROR);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+55590", "+55521", "+55570"})
    public void user_with_an_invalid_number_receives_400_response_wrong_prefix(String invalidNumberPrefix) {
        TEST("AHBDB-212 - user with an invalid number without a 50, 52, 54, 55, 56 & 58 prefix receives a 400");
        String number = invalidNumberPrefix + random.ints(1_000_000, 9_999_999).findFirst().getAsInt();
        TEST("user_with_an_invalid_number_receives_400_response_wrong_prefix " + invalidNumberPrefix);


        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has an invalid destination number and a valid type");
        final DestinationRequest destinationRequest = DestinationRequest.builder()
                .destination(number)
                .build();

        NOTE("destination number " + number);

        THEN("The client submits the destination payload and receives a 400 response");
        final ErrorResponse response = otpApi.sendDestinationToOTPError(alphaTestUser, destinationRequest, 400);
        Assertions.assertTrue(response.getMessage().contains(INVALID_NUMBER_ERROR), "Error message was not as " +
                "expected, test expected : " + INVALID_NUMBER_ERROR);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+55550", "+555", "+5555412345671", "+55554123456713"})
    public void user_with_an_invalid_number_receives_400_response_wrong_length(String invalidNumberLength) {
        TEST("AHBDB-212 - user with an invalid number length");
        TEST("user_with_an_invalid_number_receives_400_response_wrong_length " + invalidNumberLength);

        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has an invalid destination number and a valid type");
        final DestinationRequest destinationRequest = DestinationRequest.builder()
                .destination(invalidNumberLength)
                .build();


        NOTE("destination number " + invalidNumberLength);

        THEN("The client submits the destination payload and receives a 400 response");
        final ErrorResponse response = otpApi.sendDestinationToOTPError(alphaTestUser, destinationRequest, 400);
        Assertions.assertTrue(response.getMessage().contains(INVALID_NUMBER_ERROR), "Error message was not as " +
                "expected, test expected : " + INVALID_NUMBER_ERROR);
        DONE();
    }

    @Test
    public void user_with_an_invalid_number_receives_400_missing_destination() {

        TEST("AHBDB-212 - user with a missing destination");
        TEST("user_with_an_invalid_number_receives_400_missing_destination");

        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has a missing destination number and a valid type");
        final DestinationRequest destinationRequest = DestinationRequest.builder()
                .destination(null)
                .build();


        THEN("The client submits the destination payload and receives a 400 response");
        final ErrorResponse response = otpApi.sendDestinationToOTPError(alphaTestUser, destinationRequest, 400);
        Assertions.assertTrue(response.getMessage().contains(EMPTY_FIELD_ERROR), "Error message was not as expected, " +
                "test expected : " + EMPTY_FIELD_ERROR);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "12345", "1234", "abcde", "!@#$%", "12abc"})
    public void user_with_valid_number_receives_204_response_then_tries_to_verify_invalid_code(String invalidOtp) {

        TEST("AHBDB-212 - user with a valid destination number receives a 204 response but a 400 when they try to " +
                "validate their invalid code");
        TEST("user_with_valid_number_receives_204_response_then_tries_to_verify_invalid_code " + invalidOtp);

        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has a valid destination number and type");
        alphaTestUser.generateUserTelephone();

        NOTE("destination number " + alphaTestUser.getUserTelephone());

        THEN("The client submits the destination payload and receives a 204 response");
        otpApi.sendDestinationToOTP(alphaTestUser, 204);


        THEN("The client receives a 400 when they try to validate their invalid code");
        otpApi.postOTPCode(alphaTestUser, 400, invalidOtp);
        DONE();
    }

    @Test
    public void user_with_valid_number_receives_204_response_then_verifies_the_code() {

        TEST("AHBDB-2082 - user with a valid destination number receives a 204 response and they can validate their " +
                "code successfully");
        TEST("user_with_valid_number_receives_204_response_then_verifies_the_code");

        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has a valid destination number and type");


        NOTE("destination number " + alphaTestUser.getUserTelephone());

        THEN("The client submits the destination payload and receives a 204 response");
        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        THEN("Then otp code is received by the user");
        final OtpCO response = this.developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());

        THEN("The client receives a 200 when they validate their valid code");
        otpApi.postOTPCode(alphaTestUser, 200, response.getPassword());
        DONE();
    }

    @Test
    public void user_with_valid_number_receives_204_response_then_verifies_the_code_expires_after_first_Use() {

        TEST("AHBDB-2082 - user with a valid destination number receives a 204 response and they can validate their " +
                "code successfully");
        TEST("user_with_valid_number_receives_204_response_then_verifies_the_code_expires_after_first_Use");

        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has a valid destination number and type");
        alphaTestUser.generateUserTelephone();

        NOTE("destination number " + alphaTestUser.getUserTelephone());

        THEN("The client submits the destination payload and receives a 204 response");
        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        THEN("Then otp code is received by the user");
        final OtpCO response = this.developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());

        THEN("The client receives a 200 when they validate their valid code");
        otpApi.postOTPCode(alphaTestUser, 200, response.getPassword());

        THEN("The client receives a 400 when they validate their expired code");
        otpApi.postOTPCode(alphaTestUser, 400, response.getPassword());

        DONE();
    }
}
