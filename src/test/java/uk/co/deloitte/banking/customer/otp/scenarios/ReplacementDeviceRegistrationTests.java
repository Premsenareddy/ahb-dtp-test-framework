package uk.co.deloitte.banking.customer.otp.scenarios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRegisterDeviceRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.util.UUID;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("@BuildCycle2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
public class ReplacementDeviceRegistrationTests {

    @Inject
    private OtpApi otpApi;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private CertificateProtectedApi certificateProtectedApi;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    AlphaTestUser alphaTestUser;
    final ObjectMapper ob = new ObjectMapper();
    static String password = "";
    static String email = "";
    static String phoneNo = "";

    private void setupTestUser() throws JsonProcessingException {
        if (this.alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
            password = alphaTestUser.getUserPassword();
            phoneNo = alphaTestUser.getUserTelephone();
            email = alphaTestUser.getUserEmail();
            this.alphaTestUser = alphaTestUserFactory.setupUser(alphaTestUser);
        } else {
            alphaTestUser.setUserPassword(password);
            alphaTestUser.setUserTelephone(phoneNo);
            alphaTestUser.setUserEmail(email);
        }
    }

    public void storeOldPhoneId() {
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());

        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());
    }

    @Test
    public void register_new_device_happy_path() throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-4638: AC1 Positive Test - Happy Path Scenario - Register new device - 200 OK");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        AND("They have completed the OTP flow ");
        WHEN("The client attempts to register the device with a valid DeviceID");
        storeOldPhoneId();
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        THEN("The platform will add the device to the user’s device list ");
        this.authenticateApi.registerNewDevice(alphaTestUser);
        AND("They will mark the device as active in Forgerock");
        AND("They will mark any other devices against the customer as inactive in Forgerock");
        DONE();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 51})
    public void register_new_device_negative_test_bad_device_id(int stringLength) throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-4639: AC2 Negative Test - Invalid device ID - DeviceId: <DeviceId> - 400 Bad Request");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        WHEN("The client attempts to register the device with an invalid DeviceID");
        storeOldPhoneId();
        alphaTestUser.setDeviceId(RandomDataGenerator.generateRandomString(stringLength));
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        THEN("The platform will return a 400 response");
        this.authenticateApi.registerNewDeviceError(alphaTestUser, 400);
        DONE();
    }

    @Test
    public void register_new_device_and_login_happy_path() throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-4640: AC3 Positive Test - Happy Path Scenario - Login on registered/active device - 200 OK");
        GIVEN("A customer’s device is registered and active");
        setupTestUser();
        storeOldPhoneId();
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        LoginResponseV1 loginResponseV1 = this.authenticateApi.registerNewDevice(alphaTestUser);
        TokenUtils.parseLoginResponse(alphaTestUser, loginResponseV1);
        WHEN("The client attempts to validate the customer’s correct/valid login credentials");
        THEN("The platform will return a 200 with a token ");
        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUser);
        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(request));
        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                alphaTestUser.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);
        authenticateApiV2.loginUser(alphaTestUser, request, alphaTestUser.getDeviceId(), false);
    }

    @Test
    public void register_new_device_and_attempt_to_login_on_old_device_negative_test() throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-4641: AC4 Negative Test - Login on not-registered/inactive device - 401 Unauthorised");
        GIVEN("A customer’s device is not-registered and inactive");
        setupTestUser();
        storeOldPhoneId();
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        LoginResponseV1 loginResponseV1 = this.authenticateApi.registerNewDevice(alphaTestUser);
        TokenUtils.parseLoginResponse(alphaTestUser, loginResponseV1);
        WHEN("The client attempts to validate the customer’s correct/valid login credentials");
        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUser);
        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(request));
        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                alphaTestUser.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);
        THEN("The platform will return a 401 unauthorised");
        authenticateApiV2.loginUserError(alphaTestUser, request, alphaTestUser.getPreviousDeviceId(), false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "fshrbic", "2789535"})
    public void register_new_device_negative_test_bad_device_password(String password) throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-5082: AC2 Negative Test - Invalid Password - Password: <Password> - 400 Bad Request");
        TEST("AHBDB-5012: AC2 Negative Test - Invalid null Password - 400 Bad Request");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        WHEN("The client attempts to register the device with a null or invalid password");
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        alphaTestUser.setUserPassword(password);
        THEN("The platform will return a 400 response");
        this.authenticateApi.registerNewDeviceError(alphaTestUser, 400);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@ahb.com", "a", "fshrbic", "2789535", "dfdhf@", "{}$%^£"})
    public void register_new_device_negative_test_bad_device_email(String email) throws JsonProcessingException {
        //TODO add empty string to valueSource when https://ahbdigitalbank.atlassian.net/browse/AHBDB-5671 is fixed
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-5084: Negative Test - Invalid Email: null value - 400 Bad Request");
        TEST("AHBDB-5013: AC2 Negative Test - Invalid Email - Email: <Email> - 400 Bad Request");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        WHEN("The client attempts to register the device with a null or invalid email");
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        alphaTestUser.setUserEmail(email);
        THEN("The platform will return a 400 response");
        this.authenticateApi.registerNewDeviceError(alphaTestUser, 400);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+555", "1", "+55535837", "+5553583737", "+555358373", "+55535837375", "+555358373752", "dfsjdhgkj", "{}$%^£", "+555 56-401-2345"})
    public void register_new_device_negative_test_bad_device_phone(String phoneNumber) throws JsonProcessingException {
        //TODO add empty string to valueSource when https://ahbdigitalbank.atlassian.net/browse/AHBDB-5671 is fixed
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-5083: Negative Test - Invalid Phone Number: null value - 400 Bad Request");
        TEST("AHBDB-5014: AC2 Negative Test - Invalid PhoneNumber - PhoneNumber: <PhoneNumber> - 400 Bad Request");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        WHEN("The client attempts to register the device with a null or invalid Phone Number");
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        alphaTestUser.setUserTelephone(phoneNumber);
        THEN("The platform will return a 400 response");
        this.authenticateApi.registerNewDeviceError(alphaTestUser, 400);
        DONE();
    }

    @Test
    public void register_new_device_negative_test_unregistered_phone_number() throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-5206: Negative Test - Register new device with phone number not registered in Marketplace");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        WHEN("The client attempts to register the device with an unregistered phone number");
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        alphaTestUser.setUserTelephone("+555501257856");
        THEN("The platform will return a 500 response");
        this.authenticateApi.registerNewDeviceError(alphaTestUser, 404);
        DONE();
    }

    @Test
    public void register_new_device_negative_test_no_password() throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-4637: Negative Test - Missing Mandatory field - Password - 400 response");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        WHEN("The client attempts to register the device with no Password");
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .deviceId(alphaTestUser.getDeviceId())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .email(alphaTestUser.getUserEmail())
                .build();
        THEN("The platform will return a 400 response");
        this.authenticateApi.registerNewDeviceError(alphaTestUser, userRegisterDeviceRequestV1);
        DONE();
    }

    @Test
    public void register_new_device_negative_test_no_device_id() throws JsonProcessingException {
        TEST("AHBDB-1997: Replacement Device registration for registered customer - same phone number");
        TEST("AHBDB-4282: Negative Test - Missing Mandatory field - DeviceId - 400 response");
        GIVEN("A customer exists");
        setupTestUser();
        AND("They want to register a new device with the same phone number");
        WHEN("The client attempts to register the device with no DeviceID");
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .email(alphaTestUser.getUserEmail())
                .build();
        THEN("The platform will return a 400 response");
        this.authenticateApi.registerNewDeviceError(alphaTestUser, userRegisterDeviceRequestV1);
        DONE();
    }
}
