package uk.co.deloitte.banking.customer.profile.scenarios;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.email.api.EmailVerificationApi;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserStatus.REGISTRATION;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils.parseLoginResponse;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
/**
 * Created by Bharat Gatty on 09/03/2021
 */
public class UpdatePasswordTest {
    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private OtpApi otpApi;

    @Inject
    private EmailVerificationApi emailApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private void setupTestUser() {

        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupUser(new AlphaTestUser());

            NOTE("Verifying email");
            this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
            EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
            this.emailApi.verifyEmailLink(alphaTestUser, emailVerification, 200);

        }
    }

    @Test
    void update_password_user_logged_via_mobile_happy_path_test() throws JsonProcessingException {
        TEST("AHBDB-1882: AC1 Store new password - 200 success");
        TEST("AHBDB-3858: AC1 Positive Test - Happy Path Store new password - 200 success");
        setupTestUser();
        GIVEN("customer has been created on the platform and their phone number validated");
        WHEN("The client has logged in via mobile and they receive a 200 response");
        UserLoginResponseV2 loginResponse =
                this.authenticateApi.loginUserViaTelephoneNumber(this.alphaTestUser);
        assertEquals("registration", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);

        AND("the client attempts to update the user with a new valid password");
        String newPassword = RandomDataGenerator.generateRandomSHA512enabledPassword();
        final User user = User.builder()
                .userPassword(newPassword)
                .build();

        THEN("the platform will store the password and return a 200 response");
        User updatedUser = this.authenticateApi.patchUser(alphaTestUser, user);
        Assertions.assertNotNull(updatedUser);
        alphaTestUser.setUserPassword(newPassword);
        DONE();
    }

    @Test
    void update_password_user_logged_via_email_happy_path_test() throws JsonProcessingException {
        TEST("AHBDB-1882: AC1 Store new password - 200 success");
        TEST("AHBDB-3858: AC1 Positive Test - Happy Path Store new password - 200 success");
        setupTestUser();
        GIVEN("customer has been created on the platform and their email address validated");
        WHEN("The client has logged in via email and they receive a 200 response");

        UserLoginResponseV2 loginResponse =
                this.authenticateApi.loginUserViaTelephoneNumber(this.alphaTestUser);

        assertEquals("registration", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);

        AND("the client attempts to update the user with a new valid password");
        String newPassword = RandomDataGenerator.generateRandomSHA512enabledPassword();
        final User user = User.builder()
                .userPassword(newPassword)
                .build();

        THEN("the platform will store the password and return a 200 response");
        User updatedUser = this.authenticateApi.patchUser(alphaTestUser, user);
        Assertions.assertNotNull(updatedUser);
        alphaTestUser.setUserPassword(newPassword);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "123", ""})
    void update_password_invalid_password_negative_test(String invalidpassword) throws JsonProcessingException {

        TEST("AHBDB-1882: AC2 Invalid password - 400 bad request ");
        TEST("AHBDB-3859: AC2 Invalid password - 400 bad request");
        setupTestUser();
        GIVEN("customer has been created on the platform and their email address validated");
        WHEN("The client has logged in via email and they receive a 200 response");
        UserLoginResponseV2 loginResponse =
                this.authenticateApi.loginUserViaTelephoneNumber(this.alphaTestUser);
        assertEquals("registration", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);

        AND("the client attempts to update the user with a invalid valid password");
        final User user = User.builder()
                .userPassword(invalidpassword)
                .build();

        THEN("THEN The platform responds with a 400 Bad request");
        OBErrorResponse1 response = this.authenticateApi.updateUserFail(alphaTestUser, user, 400);
        Assertions.assertNotNull(response);
        DONE();
    }
}
