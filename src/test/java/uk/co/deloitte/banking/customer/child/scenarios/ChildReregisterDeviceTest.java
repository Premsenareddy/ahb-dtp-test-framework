package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Assertions;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;

import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChildReregisterDeviceTest {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AuthenticateApi authenticateApiTest;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private OtpApi otpApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private CertificateApi certificateApi;

    private static final String TEMPORARY_PASSWORD = "temporary_password";
    private static final String ERROR_UNAUTHORIZED_MESSAGE = "UAE.ERROR.UNAUTHORIZED";
    private static final String DISABLED_DEVICE_MESSAGE= "Device is disabled";
    private static final String ERROR_FORBIDDEN_MESSAGE = "UAE.ACCOUNT.FORBIDDEN";
    private String dependantId = "";

    static String relationshipId;
    static String otpCode;

    private AlphaTestUser alphaTestUserFresh;
    private AlphaTestUser childAlphaTestUser;

    private void setupTestUserFresh() {
        this.alphaTestUserFresh = new AlphaTestUser();
        this.alphaTestUserFresh = this.alphaTestUserFactory.setupCustomer(this.alphaTestUserFresh);
        createUserRelationship(alphaTestUserFresh);
        createDependentCustomer(alphaTestUserFresh, OBRelationshipRole.FATHER);
    }

    private void createUserRelationship(AlphaTestUser alphaTestUser) {
        UserRelationshipWriteRequest request = UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        assertNotNull(response);
        assertNotNull(response.getUserId());
        dependantId = response.getUserId();
    }

    private void createDependentCustomer(AlphaTestUser alphaTestUser, OBRelationshipRole obRelationshipRole) {
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("dependent full name")
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(obRelationshipRole)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        assertNotNull(response);
        assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();

    }

    private void setupChildUser() {
        if (this.childAlphaTestUser == null) {
            this.childAlphaTestUser = new AlphaTestUser();
            this.childAlphaTestUser = alphaTestUserFactory.createChildCustomer(alphaTestUserFresh, childAlphaTestUser, relationshipId, dependantId);
        }
    }

    @Test
    public void happy_path_reregister_new_device() {
        TEST("AHBDB-9737: Child's Re-register Device");
        TEST("AHBDB-10562: AC8 Register a New Device - device replacement");
        setupTestUserFresh();
        setupChildUser();

        GIVEN("An access token {Child’s UserID, password} is valid ");
        AND("The existing passcode is validated");
        String dependantId = childAlphaTestUser.getUserId();
        otpApi.sentChildOTPCode(alphaTestUserFresh, 204, relationshipId);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);
        otpCode = otpCO.getPassword();
        assertNotNull(otpCode);

        WHEN("DeviceID is sent in the POST request");
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(childAlphaTestUser.getUserPassword())
                .otp(otpCode)
                .build();

        childAlphaTestUser.setPreviousDeviceId(childAlphaTestUser.getDeviceId());
        childAlphaTestUser.setDeviceId(UUID.randomUUID().toString());

        THEN("The DeviceID will be posted and stored in ForgeRock");
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(childAlphaTestUser, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        childAlphaTestUser = parseLoginResponse(childAlphaTestUser, userLoginResponseV2);

        this.certificateApi.uploadCertificate(childAlphaTestUser);

        assertNotNull(childAlphaTestUser);

        AND("The previous device will be deactivated.");
        String currentDeviceId = childAlphaTestUser.getDeviceId();
        childAlphaTestUser.setDeviceId(childAlphaTestUser.getPreviousDeviceId());
        OBErrorResponse1 loginResponse = this.authenticateApi.loginUserError(childAlphaTestUser,
                UserLoginRequestV2.builder()
                        .password(childAlphaTestUser.getUserPassword())
                        .userId(childAlphaTestUser.getUserId())
                        .build(), childAlphaTestUser.getDeviceId(), false);
        AND("Status code 401 is returned");
        Assertions.assertEquals(ERROR_UNAUTHORIZED_MESSAGE, loginResponse.getCode());
        Assertions.assertEquals(DISABLED_DEVICE_MESSAGE, loginResponse.getMessage());
        childAlphaTestUser.setPreviousDeviceId(childAlphaTestUser.getDeviceId());
        childAlphaTestUser.setDeviceId(currentDeviceId);

        AND("AND the new device status is set: ACTIVE");
        UserLoginResponseV2 loginResponse2 = this.authenticateApi.loginUser(childAlphaTestUser);
        Assertions.assertEquals(CUSTOMER_SCOPE, loginResponse2.getScope());
        parseLoginResponse(childAlphaTestUser, loginResponse2);
        DONE();
    }

    @Test
    public void using_old_otp_to_reregister_device() {
        TEST("AHBDB-9737: Child's Re-register Device");
        TEST("AHBDB-10767: Negative Test - Using Old OTP to Re-register Another Device");
        setupTestUserFresh();
        setupChildUser();

        GIVEN("The child wants to register another device.");
        WHEN("He is using the old OTP from the first registration of a device.");
        String dependantId = childAlphaTestUser.getUserId();
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(childAlphaTestUser.getUserPassword())
                .otp(otpCode)
                .build();

        childAlphaTestUser.setPreviousDeviceId(childAlphaTestUser.getDeviceId());
        childAlphaTestUser.setDeviceId(UUID.randomUUID().toString());

        THEN("The response is 403 Forbidden.");
        OBErrorResponse1 response = authenticateApi
                .registerDependantUserDeviceError(childAlphaTestUser, request, 403);

        assertEquals(ERROR_FORBIDDEN_MESSAGE, response.getCode());
    }
}
