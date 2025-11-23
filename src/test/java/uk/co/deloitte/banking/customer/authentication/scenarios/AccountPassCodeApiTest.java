package uk.co.deloitte.banking.customer.authentication.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserDto;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("BuildCycle1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountPassCodeApiTest {

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private OtpApi otpApi;

    private AlphaTestUser alphaTestUser;

    private static final String UAE_BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";
    private static final String REQUEST_VALIDATION_ERROR = "REQUEST_VALIDATION";

    private void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();

            registerDeviceFlow(alphaTestUser);
            completeOTPFlow(alphaTestUser);
        }
    }


    @Order(100)
    @ParameterizedTest
    @ValueSource(strings = {"As8qv8LSLwkwUmx4ZckquSMVEL8nIPbe96WCofoKj8sWyPocyNB", "+55550123456", "+55550",
            "+555501234567273645", "+55550!@#$%^&", "+447415799507", "+5555012345678", "91-9449258167", ""})
    void user_with_invalid_phone_number_receives_400_response(String invalidPhoneNumber) {
        TEST("AHBDB-216 - AC3 Invalid phone number ");
        TEST("user_with_invalid_phonenumber_receives_400_response " + invalidPhoneNumber);
        setupTestUser();

        GIVEN("Client is logged in, has a valid device scope token and has their phone number validated via OTP");

        WHEN("Client calls api to update user and the body contains invalid phone number");
        UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(alphaTestUser.getUserPassword())
                .phoneNumber(invalidPhoneNumber)
                .build();

        OBErrorResponse1 error = authenticateApi.patchUserError(alphaTestUser, updateUserRequestV1, 400);
        THEN("The client receives a 400 response");
        assertEquals(UAE_BAD_REQUEST, error.getCode());
        assertEquals("Phone number is not matching the pattern", error.getMessage());
        DONE();
    }

    @Order(101)
    @ParameterizedTest
    @ValueSource(strings = {""})
    void user_with_invalid_passcode_receives_400_response(String invalidPassword) {

        TEST("AHBDB-13315 - Test fix");

        TEST("AHBDB-216 - AC2 Invalid password");
        TEST("user_with_invalid_passcode_receives_400_response " + invalidPassword);
        setupTestUser();
        GIVEN("Client is logged in, has a valid device scope token and has their phone number validated via OTP");

        UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(invalidPassword)
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        WHEN("Client calls api to update user and the body contains the invalid password");
        OBErrorResponse1 error = authenticateApi.patchUserError(alphaTestUser, updateUserRequestV1, 400);
        THEN("The client receives a 400 response");
        assertEquals(REQUEST_VALIDATION_ERROR, error.getCode());
        DONE();
    }

    @Order(900)
    @Test
    void user_with_valid_passcode_receives_200_response() {
        TEST("AHBDB-216 - AC1 Valid passcode Test ");
        alphaTestUser = null;

        setupTestUser();
        GIVEN("Client is logged in, has a valid device scope token and has their phone number validated via OTP");
        WHEN("Client calls api to update user and the body contains the password");

        final UpdateUserRequestV1 user = UpdateUserRequestV1.builder()
                .userPassword(alphaTestUser.getUserPassword())
                .sn("REGISTRATION")
                .build();

        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUser);

        UserDto updatedUser = authenticateApi.patchUser(alphaTestUser, user).extract().as(UserDto.class);
        Assertions.assertNotNull(user);
        assertEquals("REGISTRATION", updatedUser.getSn());
        DONE();
    }

    public void registerDeviceFlow(AlphaTestUser alphaTestUser) {
        LoginResponseV1 loginResponse = authenticateApi.registerUserAndDevice(alphaTestUser);
        parseLoginResponse(alphaTestUser, loginResponse);
    }

    public void completeOTPFlow(AlphaTestUser alphaTestUser) {
        otpApi.sendDestinationToOTP(alphaTestUser, 204);
        final OtpCO otpResponse = this.developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        otpApi.postOTPCode(alphaTestUser, 200, otpResponse.getPassword());
    }
}
