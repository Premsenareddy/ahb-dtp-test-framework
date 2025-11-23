package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.REGISTRATION_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeleteChildRecordTests {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AuthenticateApi authenticateApiTest;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private OtpApi otpApi;

    @Inject
    private CustomerApiV2 customerApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserFresh;
    private AlphaTestUser childAlphaTestUser;
    static String relationshipId;
    static String otpCode;

    private final String ERROR_MESSAGE_404_RESPONSE = "Customer not found";
    private final String ERROR_CODE_NOT_FOUND = "UAE.ERROR.NOT_FOUND";

    private void setupTestUserFresh() {
        

        this.alphaTestUserFresh = new AlphaTestUser();
        this.alphaTestUserFresh = this.alphaTestUserFactory.setupCustomer(this.alphaTestUserFresh);
    }

    public void setupTestUser() {
        

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupUser(this.alphaTestUser);

            this.authenticateApiTest.patchUserV2(alphaTestUser,
                    UpdateUserRequestV1.builder()
                            .sn("CUSTOMER")
                            .build());

            UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginUserProtected(alphaTestUser,
                    UserLoginRequestV2.builder()
                            .userId(alphaTestUser.getUserId())
                            .password(alphaTestUser.getUserPassword())
                            .build(),
                    alphaTestUser.getDeviceId(), true);

            parseLoginResponse(alphaTestUser, userLoginResponseV2);
        }
    }

    private void setupChildCustomer() {
        

        this.childAlphaTestUser = new AlphaTestUser();
        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();
        LoginResponse responseLogin = this.authenticateApi.createRelationshipAndUser(alphaTestUserFresh, request);

        assertNotNull(responseLogin.getUserId());
        String dependantId = responseLogin.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.MOTHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();

        OBReadRelationship1 response = this.relationshipApi.createDependant(this.alphaTestUserFresh, obWriteDependant1);
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();

        otpApi.sentChildOTPCode(alphaTestUserFresh, 204, relationshipId);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);
        otpCode = otpCO.getPassword();
        assertNotNull(otpCode);

        DependantRegisterDeviceRequestV2 request2 = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password("temporary_password")
                .otp(otpCode)
                .build();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(childAlphaTestUser, request2);

        childAlphaTestUser = parseLoginResponse(childAlphaTestUser, userLoginResponseV2);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        childAlphaTestUser = alphaTestUserFactory.setupUserCerts(childAlphaTestUser);
        assertNotNull(childAlphaTestUser);

        final String oldPassword = childAlphaTestUser.getUserPassword();
        String newPassword = "newvalidpassword";
        this.authenticateApi.patchUser(childAlphaTestUser,
                UpdateUserRequestV1.builder()
                        .userPassword(newPassword)
                        .build());
        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(childAlphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(childAlphaTestUser.getUserId())
                        .password(newPassword)
                        .build(),
                childAlphaTestUser.getDeviceId(), true);

        parseLoginResponse(childAlphaTestUser, userLoginResponse);
        assertNotEquals(oldPassword, newPassword);
        childAlphaTestUser.setUserPassword(newPassword);
        childAlphaTestUser.setUserPassword(newPassword);
        parseLoginResponse(childAlphaTestUser, userLoginResponse);

        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(childAlphaTestUser);
        Assertions.assertEquals(REGISTRATION_SCOPE, loginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, loginResponse);

        this.authenticateApi.patchUser(childAlphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn("CUSTOMER")
                        .build());

        UserLoginResponseV2 userLoginResponse2 = authenticateApi.loginUserProtected(childAlphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(childAlphaTestUser.getUserId())
                        .password(childAlphaTestUser.getUserPassword())
                        .build(),
                childAlphaTestUser.getDeviceId(), true);

        Assertions.assertEquals(CUSTOMER_SCOPE, userLoginResponse2.getScope());
        parseLoginResponse(childAlphaTestUser, userLoginResponse2);
    }

    @Test
    public void happy_path_delete_child_record_204_response() {
        TEST("AHBDB-6946: Testing Only - Delete Child Record");
        TEST("AHBDB-10519: AC1 Positive Test - Happy Path Scenario - Delete child record - 204 response");
        setupTestUserFresh();
        GIVEN("A customer (a child) exists with a customer-id.");
        setupChildCustomer();

        WHEN("The client calls delete on the internal endpoint with a valid JWT token for the child");
        this.customerApi.deleteCustomer(childAlphaTestUser);
        THEN("The API will return a 204");

        AND("The customer with that customer id is removed from CRM");
        AND("Any associated relationship is removed from the CRM");
        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(alphaTestUserFresh);

        assertNull(getResponse.getData().getCustomerId());
        assertNull(getResponse.getData().getCustomerState());
        assertNull(getResponse.getData().getStatus());
        assertNull(getResponse.getData().getOnboardedBy());
        assertNull(getResponse.getData().getGender());
        assertNull(getResponse.getData().getRelationships());

        OBErrorResponse1 response = this.customerApi.getCurrentCustomerError(childAlphaTestUser, 404);
        assertEquals(ERROR_CODE_NOT_FOUND, response.getCode());
        assertEquals(ERROR_MESSAGE_404_RESPONSE, response.getMessage());

        DONE();
    }

    @Test
    public void happy_path_delete_parent_record_204_response() {
        TEST("AHBDB-6946: Testing Only - Delete Child Record");
        TEST("AHBDB-10520: AC2 Positive Test - Happy Path Scenario - Delete parent record - 204 response");

        GIVEN("A customer (a parent) exists with a customer-id.");
        setupTestUserFresh();
        setupChildCustomer();

        this.customerApi.getCurrentCustomer(childAlphaTestUser);

        WHEN("The client calls delete on the internal endpoint with a valid JWT token for the parent");
        this.customerApi.deleteCustomer(alphaTestUserFresh);

        THEN("The API will return a 204");

        AND("The customer with that customer id is removed from CRM");
        AND("Any associated relationship is removed from the CRM");
        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(alphaTestUserFresh);

        assertNull(getResponse.getData().getCustomerId());
        assertNull(getResponse.getData().getCustomerState());
        assertNull(getResponse.getData().getStatus());
        assertNull(getResponse.getData().getOnboardedBy());
        assertNull(getResponse.getData().getGender());
        assertNull(getResponse.getData().getRelationships());

        OBReadRelationship1 getResponse2 = this.relationshipApi.getRelationships(childAlphaTestUser);

        assertNull(getResponse2.getData().getCustomerId());
        assertNull(getResponse2.getData().getCustomerState());
        assertNull(getResponse2.getData().getStatus());
        assertNull(getResponse2.getData().getOnboardedBy());
        assertNull(getResponse2.getData().getGender());
        assertNull(getResponse2.getData().getRelationships());

        OBErrorResponse1 response = this.customerApi.getCurrentCustomerError(alphaTestUserFresh, 404);
        assertEquals(ERROR_CODE_NOT_FOUND, response.getCode());
        assertEquals(ERROR_MESSAGE_404_RESPONSE, response.getMessage());

        DONE();
    }

    @Test
    public void customer_does_not_exist_404_response() {
        TEST("AHBDB-6946: Testing Only - Delete Child Record");
        TEST("AHBDB-10521: AC2 Positive Test - Happy Path Scenario - Delete parent record - 204 response");

        GIVEN("A customer (a parent) exists with a customer-id.");
        setupTestUser();

        WHEN("The client calls delete on the internal endpoint with a valid JWT token for the parent");
        OBErrorResponse1 response = this.customerApi.deleteCustomerNegativeFlow(alphaTestUser, 404);

        THEN("The API will return a 404");
        assertEquals(ERROR_CODE_NOT_FOUND, response.getCode(), "Error code does not match, expected: " + ERROR_CODE_NOT_FOUND);
        assertEquals(ERROR_MESSAGE_404_RESPONSE, response.getMessage(), "Error message does not match, expected: " + ERROR_MESSAGE_404_RESPONSE);

        DONE();
    }
}
