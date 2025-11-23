package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SetFirstPasswordForChild {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private OtpApi otpApi;

    @Inject
    private RelationshipApi relationshipApi;

    private static final String TEMPORARY_PASSWORD = "temporary_password";

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private void setupTestUser() {
        
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
            setupChildUser(this.alphaTestUser);
    }

    @Test
    public void happy_path_store_first_password_for_child_and_log_back_in_200_response() {
        TEST("AHBDB-6948: Set first password for child");
        TEST("AHBDB-10502: AC1 Store password - 200 Response");
        setupTestUser();

        GIVEN("A child is registering their device with a new password");
        WHEN("The client updates the user with password - SHA512 enabled 4 digit passcode");
        NOTE("Setting up the child user up to the point of certificates");
        String childId = this.alphaTestUserChild.getUserId();
        String newPassword = "ValidPassword";

        UpdateUserRequestV1 request = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();

        ValidatableResponse response = this.authenticateApi.patchUser(alphaTestUserChild, request);

        response.statusCode(200).assertThat();

        THEN("The platform will store the password in ForgeRock");
        AND("A 200 response is returned");

        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(alphaTestUserChild,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUserChild.getUserId())
                        .password(newPassword)
                        .build(),
                alphaTestUserChild.getDeviceId(), true);

        assertNotNull(userLoginResponse.getAccessToken());
        assertNotNull(userLoginResponse.getRefreshToken());
        assertNotNull(userLoginResponse.getScope());
        assertEquals(childId, userLoginResponse.getUserId());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings={"12345678", "abcdefghijklmno", "@!*($£)&$££$£$)(£$"})
    public void happy_path_store_valid_passwords(String validPassword) {
        TEST("AHBDB-6948: Set first password for child");
        TEST("AHBDB-10486: AC2 Invalid password - 400 Response");
        setupTestUser();
        UpdateUserRequestV1 request = UpdateUserRequestV1.builder().userPassword(validPassword).build();

        ValidatableResponse response = this.authenticateApi.patchUser(alphaTestUserChild, request);
        response.statusCode(200).assertThat();

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings={"", "minlett"})
    public void negative_test_store_invalid_password_400_response(String invalidPassword) {
        TEST("AHBDB-6948: Set first password for child");
        TEST("AHBDB-10488: AC2 Invalid password - 400 Response");
        setupTestUser();
        UpdateUserRequestV1 request = UpdateUserRequestV1.builder().userPassword(invalidPassword).build();

        ValidatableResponse response = this.authenticateApi.patchUser(alphaTestUserChild, request);
        response.statusCode(400).assertThat();

        DONE();
    }

    @Test
    public void negative_test_store_null_password_400_response() {
        TEST("AHBDB-6948: Set first password for child");
        TEST("AHBDB-10489: AC2 Invalid password - 400 Response");
        setupTestUser();
        UpdateUserRequestV1 request = UpdateUserRequestV1.builder().userPassword(null).build();

        ValidatableResponse response = this.authenticateApi.patchUser(alphaTestUserChild, request);
        response.statusCode(400).assertThat();

        DONE();
    }

    private HashMap<String, String> setupChildUser(final AlphaTestUser parentUser) {
        OBReadRelationship1 checkForRelationships = this.relationshipApi.getRelationships(parentUser);

        if (checkForRelationships.getData().getRelationships() == null) {
            UserRelationshipWriteRequest request =
                    UserRelationshipWriteRequest.builder().tempPassword(TEMPORARY_PASSWORD).build();
            LoginResponse response = this.authenticateApi.createRelationshipAndUser(parentUser, request);
            assertNotNull(response.getUserId());
            String childId = response.getUserId();

            OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                    .data(OBWriteDependant1Data.builder()
                            .id(UUID.fromString(childId))
                            .dateOfBirth(LocalDate.now().minusYears(15))
                            .fullName("js " + generateEnglishRandomString(10))
                            .gender(OBGender.MALE)
                            .language("en")
                            .termsVersion(LocalDate.now())
                            .termsAccepted(Boolean.TRUE)
                            .customerRole(OBRelationshipRole.FATHER)
                            .dependantRole(OBRelationshipRole.SON)
                            .build())
                    .build();

            OBReadRelationship1 createResponse =
                    this.relationshipApi.createDependant(parentUser, obWriteDependant1);
            String connectionId = createResponse.getData().getRelationships().get(0).getConnectionId().toString();

            otpApi.sentChildOTPCode(alphaTestUser, 204, connectionId);
            OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childId);
            String otpCode = otpCO.getPassword();
            assertNotNull(otpCode);

            DependantRegisterDeviceRequestV2 registerDeviceRequest = DependantRegisterDeviceRequestV2.builder()
                    .userId(childId)
                    .password(TEMPORARY_PASSWORD)
                    .otp(otpCode)
                    .build();

            this.alphaTestUserChild = new AlphaTestUser();

            UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                    .registerDependantUserDevice(this.alphaTestUserChild, registerDeviceRequest);

            assertNotNull(userLoginResponseV2);
            assertNotNull(userLoginResponseV2.getScope());
            assertNotNull(userLoginResponseV2.getUserId());
            assertNotNull(userLoginResponseV2.getScope());
            assertNotNull(userLoginResponseV2.getAccessToken());

            parseLoginResponse(this.alphaTestUserChild, userLoginResponseV2);
            this.alphaTestUserChild.setUserPassword(TEMPORARY_PASSWORD);

            this.alphaTestUserChild = alphaTestUserFactory.setupUserCerts(this.alphaTestUserChild);

            return new HashMap<>() {
                {
                    put("ChildId", childId);
                    put("ConnectionId", connectionId);
                }
            };
        }

        return new HashMap<>() {
            {
                put("ChildId", checkForRelationships.getData().getRelationships().get(0).getCustomerId().toString());
                put("ConnectionId", checkForRelationships.getData().getRelationships().get(0).getConnectionId().toString());
            }
        };
    }
}
