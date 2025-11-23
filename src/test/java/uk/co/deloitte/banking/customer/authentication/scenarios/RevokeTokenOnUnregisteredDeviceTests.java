package uk.co.deloitte.banking.customer.authentication.scenarios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.test.annotation.MicronautTest;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantRegisterDeviceRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.refreshtoken.api.RefreshTokenApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNTS_LIMITED_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("@BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RevokeTokenOnUnregisteredDeviceTests {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CertificateProtectedApi certificateProtectedApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private OtpApi otpApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private RefreshTokenApi refreshTokenApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserFresh;
    private AlphaTestUser alphaTestUserParent;
    private AlphaTestUser alphaTestUserChild;

    static String relationshipId;
    static String otpCode;
    private String childId;
    private String connectionId;
    private String fullName = "testUser";
    private String temporaryPassword = "validtestpassword";
    private static final String ERROR_UNAUTHORIZED_MESSAGE = "UAE.ERROR.UNAUTHORIZED";
    private static final String DISABLED_DEVICE_MESSAGE = "Device is disabled";
    private static final String UNAUTHORISED_DEVICE_MESSAGE = "User not authorise to use this device";
    private static final String REFRESH_ERROR_MESSAGE = "Unable to refresh token";

    final ObjectMapper ob = new ObjectMapper();

    private void setupTestUser() {
        envUtils.ignoreTestInEnv(Environments.NFT);

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    private void setupTestCustomerFresh() {

        this.alphaTestUserFresh = new AlphaTestUser();
        this.alphaTestUserFresh = this.alphaTestUserFactory.setupCustomer(this.alphaTestUserFresh);
    }

    private void setupTestUsersChild() {

        alphaTestUserParent = new AlphaTestUser();
        alphaTestUserChild = new AlphaTestUser();
        alphaTestUserParent = alphaTestUserFactory.setupCustomer(alphaTestUserParent);
        childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, temporaryPassword);
        connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent,
                alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
        alphaTestUserChild =
                alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, connectionId, childId);
    }

    public void setupTestUserFresh() {
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = this.alphaTestUserFactory.setupUser(this.alphaTestUser);

    }

    @Test
    public void happy_path_add_device_to_whitelist_200_response() {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11706: AC1 Login - Add Device to Whitelist - 200 OK ");

        GIVEN("A customer has registered their device with a valid username (telephone number and/or email address) and password");
        setupTestUserFresh();
        WHEN("The client has successfully logged in ");
        UserLoginResponseV2 userLoginResponse2 = authenticateApi.loginUserProtected(alphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUser.getUserId())
                        .password(alphaTestUser.getUserPassword())
                        .build(),
                alphaTestUser.getDeviceId(), true);

        THEN("The platform will store the userID and deviceID in Redis");

        DONE();
    }

    @Test
    public void happy_path_register_another_device_and_adding_device_to_whitelist_marketplace() throws JsonProcessingException {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11708: AC2 Device Registration - Add Device to Whitelist - 200 OK and AC5 User attempts to make a request - Accepted");

        GIVEN("A customer wants to register a new device");
        AND("The customer is a marketplace user");
        setupTestCustomerFresh();

        alphaTestUserFresh.setDeviceId(UUID.randomUUID().toString());
        alphaTestUserFresh.setDeviceHash(UUID.randomUUID().toString());

        WHEN("The client has successfully registered their new device ");
        LoginResponseV1 loginResponseV1 = this.authenticateApi.registerNewDevice(alphaTestUserFresh);
        TokenUtils.parseLoginResponse(alphaTestUserFresh, loginResponseV1);

        THEN("The platform will store the userID and deviceID of the customer in Redis ");
        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUserFresh.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUserFresh.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUserFresh);
        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUserFresh.getUserId())
                .password(alphaTestUserFresh.getUserPassword())
                .build();
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUserFresh, ob.writeValueAsString(request));
        this.certificateProtectedApi.validateCertificate(alphaTestUserFresh,
                alphaTestUserFresh.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        authenticateApiV2.loginUser(alphaTestUserFresh, request, alphaTestUserFresh.getDeviceId(), false);

        DONE();
    }

    @Test
    public void unhappy_path_register_another_device_and_attempt_to_make_request_marketplace_401_response() throws JsonProcessingException {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11709: AC3 Device Registration - Add Device to Whitelist - 200 OK and AC7 User attempts to make a request- 401 Response");

        GIVEN("A customer’s device has not been whitelisted ");
        setupTestCustomerFresh();

        //adding new device
        alphaTestUserFresh.setPreviousDeviceId(alphaTestUserFresh.getDeviceId());
        alphaTestUserFresh.setPreviousDeviceHash(alphaTestUserFresh.getDeviceHash());
        alphaTestUserFresh.setPreviousPrivateKeyBase64(alphaTestUserFresh.getPrivateKeyBase64());
        alphaTestUserFresh.setPreviousPublicKeyBase64(alphaTestUserFresh.getPublicKeyBase64());

        alphaTestUserFresh.setDeviceId(UUID.randomUUID().toString());
        alphaTestUserFresh.setDeviceHash(UUID.randomUUID().toString());

        LoginResponseV1 loginResponse = this.authenticateApi.registerNewDevice(alphaTestUserFresh);

        assertEquals(loginResponse.getScope(), "device");

        //saving old device's token
        String invalidToken = alphaTestUserFresh.getJwtToken();

        TokenUtils.parseLoginResponse(alphaTestUserFresh, loginResponse);

        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();

        alphaTestUserFresh.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUserFresh.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUserFresh);

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUserFresh.getUserId())
                .password(alphaTestUserFresh.getUserPassword())
                .phoneNumber(alphaTestUserFresh.getUserTelephone())
                .build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUserFresh, ob.writeValueAsString(request));

        this.certificateProtectedApi.validateCertificate(alphaTestUserFresh,
                alphaTestUserFresh.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        WHEN("The client sends a request from the device using a valid JWT token with a deviceID that does not match");
        THEN("We will block the request from being processed by the intended recipient");
        ValidatableResponse validatableResponse = authenticateApi.loginUserError(alphaTestUserFresh, request,
                alphaTestUserFresh.getDeviceId(), false, invalidToken);

        DONE();
    }

    @Test
    public void happy_path_register_another_device_and_adding_device_to_whitelist_banking() throws JsonProcessingException {

        //AHBDB-12886: Tests failing - Passed

        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11710: AC3 Device Registration - Add Device to Whitelist - 200 OK and AC5 User attempts to make a request - Accepted ");

        GIVEN("A customer wants to register a new device");
        AND("The customer is a banking user");
        setupTestCustomerFresh();
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUserFresh);

        WHEN("The client has successfully registered their new device");
        alphaTestUserFresh.setDeviceId(UUID.randomUUID().toString());
        alphaTestUserFresh.setDeviceHash(UUID.randomUUID().toString());
        LoginResponseV1 loginResponse = this.authenticateApi.registerNewDevice(alphaTestUserFresh);
        TokenUtils.parseLoginResponse(alphaTestUserFresh, loginResponse);

        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUserFresh.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUserFresh.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUserFresh);

        THEN("The platform will store the userID and deviceID of the customer in Redis ");
        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUserFresh.getUserId())
                .password(alphaTestUserFresh.getUserPassword())
                .build();
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUserFresh, ob.writeValueAsString(request));
        this.certificateProtectedApi.validateCertificate(alphaTestUserFresh,
                alphaTestUserFresh.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        // Add await as there may be too many messages coming on the same time and it may take a bit more time to process all
        await().atMost(3, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    authenticateApiV2.loginUser(alphaTestUserFresh, request, alphaTestUserFresh.getDeviceId(), false);
                });
        DONE();
    }

    @Test
    public void unhappy_path_register_another_device_and_attempt_to_make_request_banking_401_response() throws JsonProcessingException {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11711: AC3 Device Registration - Add Device to Whitelist - 200 OK and AC7 User attempts to make a request- 401 Unauthorised");

        GIVEN("A customer wants to register a new device");
        AND("The customer is a banking user");
        setupTestCustomerFresh();
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUserFresh);

        //adding new device
        alphaTestUserFresh.setPreviousDeviceId(alphaTestUserFresh.getDeviceId());
        alphaTestUserFresh.setPreviousDeviceHash(alphaTestUserFresh.getDeviceHash());
        alphaTestUserFresh.setPreviousPrivateKeyBase64(alphaTestUserFresh.getPrivateKeyBase64());
        alphaTestUserFresh.setPreviousPublicKeyBase64(alphaTestUserFresh.getPublicKeyBase64());

        alphaTestUserFresh.setDeviceId(UUID.randomUUID().toString());
        alphaTestUserFresh.setDeviceHash(UUID.randomUUID().toString());

        LoginResponseV1 loginResponse = this.authenticateApi.registerNewDevice(alphaTestUserFresh);

        assertEquals(loginResponse.getScope(), "device");

        //saving old device's token
        String invalidToken = alphaTestUserFresh.getJwtToken();

        TokenUtils.parseLoginResponse(alphaTestUserFresh, loginResponse);

        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();

        alphaTestUserFresh.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUserFresh.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUserFresh);

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUserFresh.getUserId())
                .password(alphaTestUserFresh.getUserPassword())
                .phoneNumber(alphaTestUserFresh.getUserTelephone())
                .build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUserFresh, ob.writeValueAsString(request));

        this.certificateProtectedApi.validateCertificate(alphaTestUserFresh,
                alphaTestUserFresh.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        WHEN("The client sends a request from the device using a valid JWT token with a deviceID that does not match");
        THEN("We will block the request from being processed by the intended recipient");
        ValidatableResponse validatableResponse = authenticateApi.loginUserError(alphaTestUserFresh, request,
                alphaTestUserFresh.getDeviceId(), false, invalidToken);

        DONE();
    }

    @Test
    public void happy_path_register_another_device_and_adding_device_to_whitelist_child_marketplace() throws JsonProcessingException {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11712: AC4 Device Registration - Add Device to Whitelist Marketplace - 200 OK and AC5 User attempts to make a request - Accepted");

        GIVEN("A child wants to register a device ");
        setupTestCustomerFresh();
        childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserFresh, temporaryPassword);
        connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserFresh,
                alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));

        WHEN("The client has successfully registered their new device");
        otpApi.sentChildOTPCode(alphaTestUserFresh, 204, connectionId);

        OtpCO otpCO = this.developmentSimulatorService.retrieveOtpFromDevSimulator(childId);
        assertNotNull(otpCO);
        assertNotNull(otpCO.getPassword());
        String otpCode = otpCO.getPassword();

        assertEquals(OtpType.TEXT.toString(), otpCO.getType().toString());
        assertEquals(alphaTestUserFresh.getUserTelephone(), otpCO.getDestination());

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(childId)
                .password(temporaryPassword)
                .otp(otpCode)
                .build();

        this.alphaTestUserChild = new AlphaTestUser();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi.registerDependantUserDevice(this.alphaTestUserChild, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(this.alphaTestUserChild, userLoginResponseV2);
        this.alphaTestUserChild.setUserPassword(temporaryPassword);

        this.alphaTestUserChild = alphaTestUserFactory.setupUserCerts(this.alphaTestUserChild);

        THEN("We will allow the request to be processed by the intended recipient ");
        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(alphaTestUserChild,
                UserLoginRequestV2.builder()
                        .userId(childId)
                        .password(temporaryPassword)
                        .build(),
                alphaTestUserChild.getDeviceId(), true);

        assertNotNull(userLoginResponse.getAccessToken());
        assertNotNull(userLoginResponse.getRefreshToken());
        assertNotNull(userLoginResponse.getScope());
        assertEquals(childId, userLoginResponse.getUserId());

        DONE();
    }

    @Test
    public void happy_path_register_another_device_and_adding_device_to_whitelist_child_banking() {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11714: AC4 Device Registration - Add Device to Whitelist - 200 OK AND AC5 User attempts to make a request - Accepted");
        setupTestUsersChild();
        alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, connectionId);

        GIVEN("A child wants to register a device");
        String dependantId = alphaTestUserChild.getUserId();
        otpApi.sentChildOTPCode(alphaTestUserParent, 204, connectionId);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);
        otpCode = otpCO.getPassword();
        assertNotNull(otpCode);

        WHEN("The client has successfully registered their new device");
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(alphaTestUserChild.getUserPassword())
                .otp(otpCode)
                .build();

        alphaTestUserChild.setPreviousDeviceId(alphaTestUserChild.getDeviceId());
        alphaTestUserChild.setDeviceId(UUID.randomUUID().toString());

        THEN("The platform will store the userID and authGrantId of the customer in Redis");
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(alphaTestUserChild, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        alphaTestUserChild = parseLoginResponse(alphaTestUserChild, userLoginResponseV2);

        this.certificateApi.uploadCertificate(alphaTestUserChild);

        assertNotNull(alphaTestUserChild);

        String currentDeviceId = alphaTestUserChild.getDeviceId();
        alphaTestUserChild.setDeviceId(alphaTestUserChild.getPreviousDeviceId());
        OBErrorResponse1 loginResponse = this.authenticateApi.loginUserError(alphaTestUserChild,
                UserLoginRequestV2.builder()
                        .password(alphaTestUserChild.getUserPassword())
                        .userId(alphaTestUserChild.getUserId())
                        .build(), alphaTestUserChild.getDeviceId(), false);

        Assertions.assertEquals(ERROR_UNAUTHORIZED_MESSAGE, loginResponse.getCode());
        Assertions.assertEquals(DISABLED_DEVICE_MESSAGE, loginResponse.getMessage());
        alphaTestUserChild.setPreviousDeviceId(alphaTestUserChild.getDeviceId());
        alphaTestUserChild.setDeviceId(currentDeviceId);

        AND("AND the new device status is set: ACTIVE");
        UserLoginResponseV2 loginResponse2 = this.authenticateApi.loginUser(alphaTestUserChild);
        Assertions.assertEquals(ACCOUNTS_LIMITED_SCOPE, loginResponse2.getScope());
        parseLoginResponse(alphaTestUserChild, loginResponse2);

        DONE();
    }

    @Test
    public void happy_path_refresh_token_200_response() {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11715: AC6 User attempts to refresh token - 200 OK");

        GIVEN("A customer’s device is active and has a certificate stored in the Certificate Service ");
        setupTestUserFresh();

        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(alphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUser.getUserId())
                        .password(alphaTestUser.getUserPassword())
                        .build(),
                alphaTestUser.getDeviceId(), true);

        WHEN("The client attempts to refresh the token using that deviceID");
        String oldAccessToken = userLoginResponse.getAccessToken();
        String refreshToken = userLoginResponse.getRefreshToken();
        String scope = userLoginResponse.getScope();

        JSONObject obj = this.refreshTokenApi.getJsonObject(refreshToken);

        LoginResponse newResponse = this.refreshTokenApi.refreshAccessToken(obj);

        THEN("We will return a 200 OK ");
        AND("We will return the refreshed access token");
        Assertions.assertNotEquals(newResponse.getAccessToken(), oldAccessToken, "Old Access Token was equal to " +
                "new Access Token");
        Assertions.assertEquals(scope, newResponse.getScope(), "Scope was not equal to expected. " +
                "Scope expected: " + scope);

        DONE();
    }

    @Test
    public void user_attempts_to_make_request_401_response() throws JsonProcessingException {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11716: AC8 User attempts to refresh token - 401");

        GIVEN("A customer’s device has not been activated and does not have a certificate stored in the Certificate Service");
        setupTestUserFresh();

        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(alphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUser.getUserId())
                        .password(alphaTestUser.getUserPassword())
                        .build(),
                alphaTestUser.getDeviceId(), true);

        String refreshToken = userLoginResponse.getRefreshToken();

        //adding new device
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        LoginResponseV1 loginResponseV1 = this.authenticateApi.registerNewDevice(alphaTestUser);
        TokenUtils.parseLoginResponse(alphaTestUser, loginResponseV1);

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

        WHEN("The client attempts to refresh the token using that deviceID");
        THEN("We will return 401");
        JSONObject obj = this.refreshTokenApi.getJsonObject(refreshToken);

        OBErrorResponse1 errorResponse = this.refreshTokenApi.getNewAccessTokenErrorResponse(obj, 401);
        assertEquals(ERROR_UNAUTHORIZED_MESSAGE, errorResponse.getCode(), "Error Code is not matching. Expected "
                + ERROR_UNAUTHORIZED_MESSAGE + " but received " + errorResponse.getCode());
        assertEquals(UNAUTHORISED_DEVICE_MESSAGE, errorResponse.getMessage(), "Error Code is not matching. Expected "
                + UNAUTHORISED_DEVICE_MESSAGE + " but received " + errorResponse.getMessage());
        DONE();
    }

    @Test
    public void user_attempts_to_refresh_token_with_old_refresh_token_401_response() throws JsonProcessingException {
        TEST("AHBDB-3873: Revoke token on unregistered device");
        TEST("AHBDB-11717: AC8 User attempts to refresh token with old jwt - 401 response");

        GIVEN("A customer’s access token is expired ");
        setupTestUserFresh();

        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(alphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUser.getUserId())
                        .password(alphaTestUser.getUserPassword())
                        .build(),
                alphaTestUser.getDeviceId(), true);

        String oldRefreshToken = userLoginResponse.getRefreshToken();
        TokenUtils.parseLoginResponse(alphaTestUser, userLoginResponse);

        UserLoginResponseV2 userLoginResponse2 = authenticateApi.loginUserProtected(alphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUser.getUserId())
                        .password(alphaTestUser.getUserPassword())
                        .build(),
                alphaTestUser.getDeviceId(), true);

        String newRefreshToken = userLoginResponse2.getRefreshToken();

        JSONObject obj = this.refreshTokenApi.getJsonObject(newRefreshToken);
        LoginResponse newResponse = this.refreshTokenApi.refreshAccessToken(obj);

        WHEN("The client attempts to refresh the token using that access token");
        JSONObject obj2 = this.refreshTokenApi.getJsonObject(oldRefreshToken);

        THEN("We will return a 401 Unauthorised");
        OBErrorResponse1 errorResponse = this.refreshTokenApi.getNewAccessTokenErrorResponse(obj, 401);
        assertEquals(ERROR_UNAUTHORIZED_MESSAGE, errorResponse.getCode(), "Error Code is not matching. Expected "
                + ERROR_UNAUTHORIZED_MESSAGE + " but received " + errorResponse.getCode());
        assertEquals(REFRESH_ERROR_MESSAGE, errorResponse.getMessage(), "Error Code is not matching. Expected "
                + REFRESH_ERROR_MESSAGE + " but received " + errorResponse.getMessage());

        DONE();
    }

    @Test
    public void user_login_without_signature() {
        TEST("AHBDB-10804 - Enforce signature checks for authentication-adapter login calls");
        AlphaTestUser user = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(user.getUserId())
                .password(user.getUserPassword())
                .build();

        OBErrorResponse1 error = authenticateApiV2.loginUserProtectedWithoutSignature(user, request,
                user.getDeviceId());
        THEN("Status code 401(UNAUTHORIZED) is returned");
        assertNotNull(error);
        DONE();
    }
}
