package uk.co.deloitte.banking.journey.scenarios.adult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.MDC;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1DataAmount;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1DataBalance;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.EmailVerification;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ResetPasswordResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateForgottenPasswordRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.User;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserDto;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserScope;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ValidateResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ValidateResetPasswordResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerType1;
import uk.co.deloitte.banking.customer.api.customer.model.OBEIDStatus;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTerm1;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBCRSData2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBTaxResidencyCountry2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentStatus;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBFatcaForm1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatca1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatcaDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBAppType;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBIdType;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBReadIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBResult;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.email.api.EmailVerificationApi;
import uk.co.deloitte.banking.customer.employment.api.EmploymentApiV2;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.REGISTRATION_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils.parseLoginResponse;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomString;
import static uk.co.deloitte.banking.api.test.BDDUtils.TICKET;
import static uk.co.deloitte.banking.customer.api.customer.model.OBTermType.REGISTRATION;

@Slf4j
@MicronautTest
@TestMethodOrder(OrderAnnotation.class)
class AdultMarketplaceCustomerOnScenario {


    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private EmailVerificationApi emailApi;


    @Inject
    private OtpApi otpApi;

    @Inject
    private IdNowApi idNowApi;


    @Inject
    private SanctionsApi sanctionsApi;

    @Inject
    private FatcaApiV2 fatcaApiV2;

    @Inject
    private EmploymentApiV2 employmentApiV2;

    @Inject
    private LocationsApiV2 locationsApiV2;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;


    @Inject
    CertificateProtectedApi certificateProtectedApi;

    static final AlphaTestUser alphaTestUser = new AlphaTestUser();

    static String invalidToken;


    @Inject
    private AlphaKeyService alphaKeyService;


    @Inject
    private CertificateApi certificateApi;

    final ObjectMapper ob = new ObjectMapper();

    @Test
    @Order(1)
    void device_registration_success_test() {
        TEST("AHBDB-213 - Register user device");

        LoginResponseV1 loginResponse = authenticateApi.registerUserAndDevice(alphaTestUser);

        THEN("Status code is 201");
        AND("The device gets registered with a device response containing a token");

        assertNotNull(loginResponse);

        parseLoginResponse(alphaTestUser, loginResponse);
        NOTE("User id is [" + alphaTestUser.getUserId() + "]");

        assertNotNull(alphaTestUser.getJwtToken());
        assertNotNull(alphaTestUser.getUserId());
        assertEquals(ScopeConstants.DEVICE, alphaTestUser.getScope());

        DONE();
    }


    @Test
    @Order(2)
    void device_login_success_test_device_scope() {
        TEST("AHBDB-213 - Login to upgraded token scope");
        WHEN("Client tries to login with their device");

        LoginResponseV1 response = authenticateApi.loginDevice(alphaTestUser);


        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");

        AND("Scope of the token is device");
        assertEquals("device", response.getScope());

        parseLoginResponse(alphaTestUser, response);
    }


    @Test
    @Order(3)
    void user_registration_add_phone_success_test() {
        final String newPhoneNumber = alphaTestUser.generateUserTelephone();

        final String telephone = alphaTestUser.getUserTelephone();
        final UpdateUserRequestV1 user = UpdateUserRequestV1.builder()
                .phoneNumber(telephone)
                .build();


        WHEN("Client calls the update api to update the customer");


        ValidatableResponse response1 = this.authenticateApi.patchUser(alphaTestUser, user);

        THEN("Status code is 200 ( OK )");
        response1.statusCode(200).assertThat();
        final UserDto updatedUser = response1.extract().body().as(UserDto.class);
        AND("The user gets updated and the phone number is set on the profile");
        Assertions.assertNotNull(updatedUser);

        alphaTestUser.setUserTelephone(newPhoneNumber);

        DONE();
    }


    @Test
    @Order(4)
    void confirm_user_otp() {
        TEST("AHBDB-212 - user with a valid destination number receives a 204 response");

        GIVEN("User has a valid access token and wants to validate their telephone number");

        WHEN("The client the client request a otp : " + alphaTestUser.getUserTelephone());
        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        THEN("The client receives a 204 response code");

        TEST("Dev can obtain the otp via the userId");

        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());

        assertNotNull(otpCO);

        final String otp = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otp);
        assertNotNull(otp);

        TEST("Client can validate the OTP");
        otpApi.postOTPCode(alphaTestUser, 200, otp);

        DONE();
    }

    @Test
    @Order(5)
    void upload_users_certificate() {
        TEST("AHBDB-??? - user uploads device certificate");

        //TOOD::BDD

        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUser);
        DONE();
    }


    @Test
    @Order(5)
    void user_registration_set_passcode_success_test() throws JsonProcessingException {

        TEST("AHBDB-216 - Store passcode against user");
        GIVEN("Client is logged in and has a device scope token");

        //TODO:: In the auth adapter
        final UpdateUserRequestV1 user = UpdateUserRequestV1.builder()
                .userPassword(alphaTestUser.getUserPassword())
                .sn("REGISTRATION")
                .build();

        AND("I then send a request with a payload certificate can be validated");

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(user));


        THEN("The client submits the payload and receives a 204 response");

        ValidatableResponse validatableResponse = this.authenticateApi.patchUser(alphaTestUser, user, true);

        DONE();
    }


    @Test
    @Order(6)
    void user_login_success_test_registration_scope() throws JsonProcessingException {
        TEST("AHBDB - XX - Login to upgraded token scope");
        WHEN("Client tries to re-authenticate in order to upgrade the scope");

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();


        UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginUserProtected(alphaTestUser, request,
                alphaTestUser.getDeviceId(), true);

        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");

        AND("Scope of the token is REGISTRATION");
        assertEquals(userLoginResponseV2.getScope(), "registration");
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        assertNotNull(alphaTestUser.getUserId());

        DONE();
    }


    @Test
    @Order(20)
    void user_register_new_device() throws JsonProcessingException {
        TEST("AHBDB - XX - Register new device");
        GIVEN("Client is onboarded with REGISTRATION Scope");

        WHEN("Client tries register with new device");

        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());

        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        LoginResponseV1 loginResponse = this.authenticateApi.registerNewDevice(alphaTestUser);

        THEN("Status code 200(OK) is returned with device scope");
        assertEquals(loginResponse.getScope(), "device");

        invalidToken = alphaTestUser.getJwtToken();

        parseLoginResponse(alphaTestUser, loginResponse);
        DONE();
    }

    @Test
    @Order(23)
    void confirm_user_otp_after_new_device() {
        TEST("AHBDB-212 - user with a valid destination number receives a 204 response");

        GIVEN("User has a valid access token and wants to validate their telephone number");

        WHEN("The client the client request a otp : " + alphaTestUser.getUserTelephone());
        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        THEN("The client receives a 204 response code");

        TEST("Dev can obtain the otp via the userId");

        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());

        assertNotNull(otpCO);

        final String otp = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otp);
        assertNotNull(otp);

        TEST("Client can validate the OTP");
        otpApi.postOTPCode(alphaTestUser, 200, otp);

        DONE();
    }

    @Test
    @Order(25)
    void upload_users_new_device_certificate() {
        TEST("AHBDB-??? - user uploads device certificate");

        assertNotNull(alphaTestUser.getUserId());

        //TOOD::BDD
        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();

        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUser);
        DONE();
    }

    @Test
    @Order(26)
    void user_login_fail_test_with_old_jwt() throws JsonProcessingException {

        TEST("AHBDB - 3873 - Token revocation");
        WHEN("Client tries to use a token from a device which is not active anymore");

        assertNotNull(alphaTestUser.getUserId());

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(request));

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                alphaTestUser.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        ValidatableResponse validatableResponse = authenticateApi.loginUserError(alphaTestUser, request,
                alphaTestUser.getDeviceId(), false, invalidToken);

        THEN("Status code 401(UNAUTHORIZED) is returned");

        DONE();
    }


    @Test
    @Order(26)
    void user_login_success_test_with_new_device() throws JsonProcessingException {
        TEST("AHBDB - XX - Login to upgraded token scope");
        WHEN("Client tries to re-authenticate in order to upgrade the scope");

        assertNotNull(alphaTestUser.getUserId());

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(request));

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                alphaTestUser.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginUser(alphaTestUser, request,
                alphaTestUser.getDeviceId(), false);

        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is REGISTRATION");

        Assertions.assertEquals(REGISTRATION_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        DONE();
    }

    @Test
    @Order(27)
    void user_login_denied_with_old_device_test() throws JsonProcessingException {
        TEST("AHBDB - XX - Login to upgraded token scope");
        WHEN("Client tries to re-authenticate while user is at SN scope");

        assertNotNull(alphaTestUser.getUserId());

        final UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        final String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        ob.writeValueAsString(request),
                        alphaTestUser.getPreviousPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                alphaTestUser.getPreviousDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        ValidatableResponse userLoginResponseV2 = authenticateApi.loginUserValidatable(alphaTestUser, request,
                alphaTestUser.getPreviousDeviceId(), false);

        userLoginResponseV2.statusCode(401).assertThat();


        DONE();
    }

    @Test
    @Order(28)
    void user_login_by_phone_success_test() {

        TEST("AHBDB-218 - Login customer by phone ");
        WHEN("User tries to authenticate with the phone number");


        UserLoginResponseV2 loginResponse =
                this.authenticateApi.loginUserViaTelephoneNumber(alphaTestUser);

        THEN("Status code 200(OK) is returned");
        AND("Body contains a token");
        Assertions.assertNotNull(loginResponse);
        Assertions.assertNotNull(loginResponse.getAccessToken());
        assertFalse(isBlank(loginResponse.getAccessToken()));

        if (loginResponse.getAccessToken() != null) {
            parseLoginResponse(alphaTestUser, loginResponse);

        } else {
            fail();
        }

        DONE();
    }


    // -------------------------------------------------------------------------------------------------------------
    @Test
    @Order(31)
    void create_crm_customer_success_test() {
        TEST("AHBDB-3687:: AC1 Create Customer in CRM");

        GIVEN("Client is logged in and has a customer scope token");
        Assertions.assertNotNull(alphaTestUser.getLoginResponse());
        Assertions.assertNotNull(alphaTestUser.getLoginResponse().getAccessToken());
        assertEquals("registration", alphaTestUser.getLoginResponse().getScope());


        AND("Completes the customer profile with valid date");
        OBWriteCustomer1 customer = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName("Test" + generateRandomString(5))
                        .dateOfBirth(LocalDate.now().minusYears(21))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language("en")
                        //.mobileNumber(alphaTestUser.generateUserTelephone()) <-- Andrei investigate when changing
                        // the phone number fails tests
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .address(OBPostalAddress6.builder()
                                .addressLine(List.of(generateRandomString(10),
                                        generateRandomString(5)))
                                .buildingNumber("101")
                                .streetName("Street")
                                .countrySubDivision("Dubai")
                                .country("AE")
                                .build())
                        .customerState(OBCustomerStateV1.IDV_REVIEW_REQUIRED)
                        .build())
                .build();

        WHEN("The client calls post on the customers endpoint");
        final OBWriteCustomerResponse1 response = this.customerApiV2.createCustomerSuccess(alphaTestUser, customer);

        THEN(("Status 201(CREATED) is returned"));
        AND("Customer is returned with a valid customerId which is equals to user id");
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getData().getCustomerId());
        NOTE("Customer id is [" + response.getData().getCustomerId() + "]");
        assertEquals(alphaTestUser.getUserId(), response.getData().getCustomerId().toString());
        DONE();
    }


    @Test
    @Order(32)
    void user_upgrade_scope_success_test() {
        TEST("AHBDB - XX - Set user to be a customer");
        GIVEN("Client is logged in and has a registration scope token");
        Assertions.assertNotNull(alphaTestUser.getLoginResponse());
        Assertions.assertNotNull(alphaTestUser.getLoginResponse().getAccessToken());
        assertEquals("registration", alphaTestUser.getLoginResponse().getScope());

        GIVEN("There is already an customer linked to this user");

        WHEN("Client calls api to update user status");
        ValidatableResponse validatableResponse = this.authenticateApi.patchUser(alphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn(CUSTOMER)
                        .build());


        THEN("Status code 200(OK) is returned");

        AND("User status is customer");

        DONE();

    }


    @Test
    @Order(35)
    void user_login_by_email_success_test() {

        TEST("AHBDB-218 - Login customer by email");
        WHEN("User tries to authenticate with the email");

        UserLoginResponseV2 loginResponse =
                this.authenticateApi.loginUserViaTelephoneNumber(alphaTestUser);

        THEN("Status code 200(OK) is returned");
        AND("Body contains a token");
        Assertions.assertNotNull(loginResponse);
        Assertions.assertNotNull(loginResponse.getAccessToken());
        assertFalse(isBlank(loginResponse.getAccessToken()));

        if (loginResponse.getAccessToken() != null) {
            parseLoginResponse(alphaTestUser, loginResponse);

        } else {
            fail();
        }

        DONE();
    }


    @Test
    @Order(36)
    void user_login_success_test_customer_scope() {
        TEST("AHBDB - XX - Login to upgraded token scope");
        WHEN("Client tries to re-authenticate in order to upgrade the scope");


        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(alphaTestUser);

        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is CUSTOMER");
        assertEquals("customer", loginResponse.getScope());

        parseLoginResponse(alphaTestUser, loginResponse);
        DONE();


        this.authenticateApi.patchUser(alphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn(CUSTOMER)
                        .build());
        UserLoginResponseV2 userLoginResponseV2 = this.authenticateApi.loginUser(alphaTestUser);
        parseLoginResponse(alphaTestUser, userLoginResponseV2);
        Assertions.assertEquals(userLoginResponseV2.getScope(),
                ScopeConstants.CUSTOMER_SCOPE);
    }

    @Test
    @Order(37)
    public void user_add_terms_successfully() {

        TEST("AHBDB-219 user can update their terms with a valid terms accepted date");
        GIVEN("I have a valid access token and account scope");

        WHEN("I try to update terms with a valid terms accepted date");
        OBWriteTerm1 obWriteTerm1 = OBWriteTerm1.builder()
                .termsAccepted(true)
                .termsAcceptedDate(OffsetDateTime.parse("2021-02-03T22:42:30+02:00"))
                .termsVersion(LocalDate.parse("2021-01-30"))
                .type(REGISTRATION)
                .build();

        THEN("The terms are updated and 200 response is returned");
        this.customerApiV2.postCustomerTerms(alphaTestUser, obWriteTerm1);
        DONE();
    }


    @Test
    @Order(38)
    public void user_add_crs_successfully() {
        TEST("AHBDB-4748 create CRS details for customer");
        GIVEN("I have a valid access token and customer scope");

        final OBTaxResidencyCountry2 obTaxResidencyCountry2 = OBTaxResidencyCountry2.builder()
                .country("RO")
                .missingTinReason("Unable to obtain/disclose a TIN")
                .build();

        final OBCRSData2 obcrsData1 = OBCRSData2.builder()
                .agreedCertification(true)
                .otherResidencyJurisdictions(true)
                .uaeResidencyByInvestmentScheme(true)
                .personalIncomeTaxJurisdictions(List.of("RO"))
                .taxResidencyCountry(List.of(obTaxResidencyCountry2))
                .build();

        WHEN("I try to update customer with valid crs data");
        OBWriteCRS2 obWriteCRS2 = OBWriteCRS2.builder()
                .data(obcrsData1)
                .build();

        THEN("The CRS details are created and 201 response is returned");
        var actualCRS = this.customerApiV2.postCRSDetails(alphaTestUser, obWriteCRS2);
        assertEquals(actualCRS.getData().getAgreedCertification(), obcrsData1.getAgreedCertification());
        DONE();
    }


    @Test
    @Order(39)
    void patch_crm_customer_success_test() {

        TEST("AHBDB-5218:: AC1 Patch Customer in CRM");

        GIVEN("Customer is created");
        OBWritePartialCustomer1 patchCustomer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .preferredName("Test" + generateRandomString(5))
                        .dateOfBirth(LocalDate.now().minusYears(22))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language("en")
                        .gender(OBGender.MALE)
                        .build())
                .build();

        WHEN("The client calls patch on the customers endpoint");
        final OBWriteCustomerResponse1 patchResponse = this.customerApiV2.patchCustomerSuccess(alphaTestUser,
                patchCustomer);

        THEN("we will return a 200 response");
        assertNotNull(patchResponse);

        DONE();
    }

    @Test
    @Order(40)
    void get_crm_customer_success_test() {
        TEST("AHBDB-5218:: AC1 Get Customer in CRM");


        GIVEN("A customer already exists");

        WHEN("The client calls patch on the customers endpoint");
        final OBReadCustomer1 getResponse = this.customerApiV2.getCustomerSuccess(alphaTestUser);

        THEN("we will return a 200 response");
        assertNotNull(getResponse);

        DONE();
    }

    @Test
    @Order(41)
    void get_crm_customer_by_customerId_success_test() {
        TEST("AHBDB-7260:: AC1 Get Customer bu customerId in CRM");

        GIVEN("A customer already exists");

        WHEN("The client calls patch on the customers endpoint");
        final OBReadCustomer1 getResponse = this.customerApiV2.getCustomersByCustomerId(alphaTestUser.getUserId());

        THEN("we will return a 200 response");
        assertNotNull(getResponse);

        DONE();
    }

    @Test
    @Order(42)
    void patch_crm_customer_address_success_test() {
        TEST("AHBDB-8266:: AC1 Patch Customer with a customer’s details with the Address field but with Country and CountrySubDivision not in the payload in CRM");

        GIVEN("Customer is created");
        OBWritePartialCustomer1 patchCustomer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .preferredName("Test" + generateRandomString(5))
                        .dateOfBirth(LocalDate.now().minusYears(22))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .address(OBPartialPostalAddress6.builder().postalCode("P.O. Box 63112").build())
                        .language("en")
                        .gender(OBGender.MALE)
                        .build())
                .build();

        WHEN("The client calls patch on the customers endpoint");
        final OBWriteCustomerResponse1 patchResponse = this.customerApiV2.patchCustomerSuccess(alphaTestUser,
                patchCustomer);

        THEN("we will return a 200 response");
        assertNotNull(patchResponse);

        DONE();
    }
//-----------------------------------------------------------------------------------------------------------------------


    //
    // ----------------- IDV
    @Test
    @Order(50)
    void user_create_idv_applicant_test_success() {


        TEST("AHBDB - 1367 - Set user to have an account");

        GIVEN("Client is logged in and has a customer scope token");
        Assertions.assertNotNull(alphaTestUser.getLoginResponse());
        Assertions.assertNotNull(alphaTestUser.getLoginResponse().getAccessToken());
        assertEquals("customer", alphaTestUser.getLoginResponse().getScope());


        TokenHolder createApplicantResponse = this.idNowApi.createApplicant(alphaTestUser);
        THEN("Status code 201(CREATED) is returned");
        AND("Response contains a token");

        AND("Scope of the token is CUSTOMER");
        if (createApplicantResponse != null) {
            assertNotNull(createApplicantResponse.getSdkToken());
            assertNotNull(createApplicantResponse.getApplicantId());
            alphaTestUser.setApplicantId(createApplicantResponse.getApplicantId());
        } else {
            fail();
        }
        DONE();

    }

    @Test
    @Order(52)
    void verify_email() throws InterruptedException {
        TEST("AHBDB-214: AC2 Update customer email verification status");
        TEST("AHBDB-2395:AC2 Positive - Update customer email verification status");
        TEST("AHBDB-2500: AC1 Generation of email with verification link (Post request) - 204 response");

        Assertions.assertNotNull(alphaTestUser.getApplicantId());

        final OBReadCustomer1 customerBeforeEmail = this.customerApiV2.getCustomerSuccess(alphaTestUser);
        GIVEN("Customer is created and email is not verified");
        Assertions.assertEquals(OBReadEmailState1.NOT_VERIFIED,
                customerBeforeEmail.getData().getCustomer().get(0).getEmailState());


        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        THEN("THEN the platform will return a 200 response");
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        this.emailApi.verifyEmailLink(alphaTestUser, emailVerification, 200);

        await().atMost(10, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    final OBReadCustomer1 customerAfterEmail = this.customerApiV2.getCustomerSuccess(alphaTestUser);
                    THEN("Customer is updated with email verified");
                    Assertions.assertEquals(OBReadEmailState1.VERIFIED,
                            customerAfterEmail.getData().getCustomer().get(0).getEmailState());
                });

        DONE();

    }

    @Test
    @Order(53)
    void send_idnow_webhook_callback() {

        TEST("AHBDB - 211 - Store IDV status and JSON blob");

        GIVEN("IDNow has finished processing the applicant’s ID");
        AND("the verification has been successful");
        Assertions.assertNotNull(alphaTestUser.getApplicantId());

        GIVEN("Customer is created");
        WHEN("the Ident status value “Success” is returned from IDNow");
        var response = this.idNowApi.setIdNowAnswer(alphaTestUser, "SUCCESS");
        THEN("I will trigger an event saying IDV is completed along with the IDNow response");
        assertTrue(response);

        final OBReadCustomer1 customerAfterIdv = this.customerApiV2.getCustomerSuccess(alphaTestUser);
        AND("AHBDB-12345: Customer email is still verified");
        Assertions.assertEquals(OBReadEmailState1.VERIFIED,
                customerAfterIdv.getData().getCustomer().get(0).getEmailState());


        DONE();

    }

    @Test
    @Order(54)
    void fetch_eid_information() {


        TEST("AHBDB - 1368 - Get EID information");

        GIVEN("A customer has completed their ID&V process");
        AND("the verification has been successful");
        Assertions.assertNotNull(alphaTestUser.getApplicantId());

        WHEN("the client attempts to retrieve the applicant’s full IDNow result information with a valid " +
                "JWT token");
        ApplicantExtractedDTO response = this.idNowApi.getApplicantResult(alphaTestUser);
        THEN("the platform will return a 200 response");
        AND("the platform will return the JSON related to the user ID/ transaction ID");
        assertNotNull(response);
        DONE();

    }

    @Test
    @Order(55)
    void persist_eid_information_to_customer() throws JsonProcessingException {


        TEST("AHBDB - 1887 - Persist customer information from IDV");

        GIVEN("A customer exists");
        OBReadCustomer1 currentCustomer = this.customerApiV2.getCurrentCustomer(alphaTestUser);
        AND("The customer has completed his/her ID Check and we have all the information");
        ApplicantExtractedDTO applicantResult = this.idNowApi.getApplicantResult(alphaTestUser);

        WHEN("Client attempt to update customer profile with those information");

        Map<String, Object> userData = applicantResult.getUserData();

        OBWritePartialCustomer1 obWriteCustomer1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .build())
                .build();
        obWriteCustomer1.getData().setFullName(extractValue(userData, "FullName"));
        obWriteCustomer1.getData().setFirstName(extractValue(userData, "FirstName"));
        obWriteCustomer1.getData().setLastName(extractValue(userData, "LastName"));
        obWriteCustomer1.getData().setNationality(extractValue(userData, "Nationality"));
        obWriteCustomer1.getData().setGender(OBGender.valueOf(extractValue(userData, "Gender").toUpperCase(Locale.ROOT)));

        obWriteCustomer1.getData().setCustomerState(OBCustomerStateV1.IDV_COMPLETED);

        OBWriteCustomerResponse1 obWriteCustomerResponse1 = this.customerApiV2.updateCustomer(alphaTestUser,
                obWriteCustomer1, 200);
        AND("Expect information to be persisted");

        currentCustomer = this.customerApiV2.getCurrentCustomer(alphaTestUser);

        assertTrue(isNotBlank(currentCustomer.getData().getCustomer().get(0).getPreferredName()));
        assertEquals(extractValue(userData, "FullName"), currentCustomer.getData().getCustomer().get(0).getFullName());
        DONE();
    }


    @Test
    @Order(56)
    void after_idv_customer_scope_is_account() {

        TEST("AHBDB-7742 - After the IDV is completed the scope is account");

        GIVEN("IDV is completed (see previous test)");
        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        WHEN("User logs in ");
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginUser(alphaTestUser, request,
                alphaTestUser.getDeviceId(), false);

        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is " + ACCOUNT_SCOPE);

        Assertions.assertEquals(ACCOUNT_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

    }


    // ----------- Employment Details


    @Test
    @Order(302)
    void test_create_customer_idv_details() {
        TEST("AHBDB-1887 - AC2 Customer has completed IDV - 201 response");
        TEST("AHBDB-2714 - AC2 Customer has completed IDV - 201 response");
        TEST("AHBDB-3689: Create and get endpoint for IDV declaration in CRM - AC1 Post  ID&V information - 201 Created ");
        TEST("");


        GIVEN("A customer already exists");

        OBReadCustomer1 currentCustomer = customerApiV2.getCurrentCustomer(alphaTestUser);
        assertNotNull(currentCustomer);

        WHEN("Customer enters his idv details");
        OBWriteIdvDetailsResponse1 customerIdvDetails = customerApiV2.createCustomerIdvDetails(alphaTestUser);
        THEN("Employment details are persisted for the customer");
        assertNotNull(customerIdvDetails);

        DONE();
    }

    @Test
    @Order(303)
    void test_get_customer_idv_details() {
        TEST("AHBDB-229 - Retrieve idv details");
        TEST("AHBDB-3689: Create and get endpoint for IDV declaration in CRM");
        TEST("AC5 Get IDV - 200 Success");

        GIVEN("A customer already exists");

        WHEN("The system query for the idv details");
        OBReadIdvDetailsResponse1 result = customerApiV2.getCustomerIdvDetails(alphaTestUser);
        THEN("The details for the customer are returned");
        AND("the platform will return the customer’s IDV information");
        var data = result.getData();
        assertEquals(OBResult.SUCCESS, data.getResult());
        assertEquals("TECH_PHOTO", data.getReason());
        assertEquals("cb3fe4cb-abd9-4647-841f-35ad8aec6f57", data.getTransactionNumber());
        assertEquals("ABC-ABCDE", data.getIdentId());
        assertEquals(OBIdType.IDCARD, data.getIdType());
        assertEquals(alphaTestUser.getEid().replace("-", ""), data.getDocumentNumber());
        assertEquals("123456789", data.getIdNumber());

        assertEquals("AE", data.getIdCountry());
        assertEquals(LocalDate.of(2022, 01, 01), data.getDateOfExpiry());
        assertEquals(OffsetDateTime.of(2021, 01, 01, 10, 10, 0, 0, ZoneOffset.UTC), data.getIdentificationTime());
        assertEquals("GTC-Version", data.getGtcVersion());
        assertEquals(OBAppType.APP, data.getType());
        DONE();

        DONE();
    }


    @Test
    @Order(412)
    void create_fatca_details_for_customer_crm() {
        TEST("AHBDB-4848: Create and get endpoint for FATCA declaration in CRM");
        TEST("AC1 Store FATCA details - 201 response");
        GIVEN("the customer has provided their FATCA details");
        var fatca = OBWriteFatca1.builder().data(OBWriteFatcaDetails1.builder()
                .usCitizenOrResident(true)
                .ssn("123456789")
                .form(OBFatcaForm1.W8)
                .federalTaxClassification("S Corporation")
                .build())
                .build();

        WHEN("the client updates the customer profile with valid FATCA details");
        var result = this.fatcaApiV2.createFatcaDetails(alphaTestUser, fatca);
        THEN("we will return a 201 response");
        assertNotNull(result);

        DONE();
    }

    @Test
    @Order(413)
    void get_fatca_details_for_customer_crm() {
        TEST("AHBDB-4848: Create and get endpoint for FATCA declaration");
        TEST("AC3 Get FATCA details - 200 success response");
        GIVEN("a customer exists with FATCA input fields");
        WHEN("the client attempts to retrieve the applicant’s FATCA information with a valid JWT token");

        var result = this.fatcaApiV2.getFatcaDetails(alphaTestUser);
        THEN("the platform will return a 200 response");

        AND("the platform will return the customer’s FATCA information");
        var data = result.getData();
        assertEquals("123456789", data.getSsn());
        assertEquals(OBFatcaForm1.W8, data.getForm());
        assertEquals("S Corporation", data.getFederalTaxClassification());
        assertTrue(data.getUsCitizenOrResident());

        DONE();
    }


    @Test
    @Order(414)
    void test_create_customer_employment_details_crm() {
        TICKET("AHBDB-4844: Create and get endpoint for Employment declaration in CRM");
        TEST("AC1 Store Employment details - 201 response");
        GIVEN("the customer has provided their Employment details");
        var employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName("AHB")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("36")
                .professionCode("99")
                .build();

        WHEN("the client updates the customer profile with valid employment details");
        var result = this.employmentApiV2.createEmploymentDetails(alphaTestUser, employment);
        THEN("we will return a 201 response");
        assertNotNull(result);
        assertEquals("99", result.getData().getProfessionCode());
        assertEquals("AAN", result.getData().getBusinessCode());

        DONE();

    }

    @Test
    @Order(415)
    void test_get_customer_employment_details_crm() {
        TICKET("AHBDB-4844: Create and get endpoint for employment declaration");
        TEST("AC3 Get employment details - 200 success response");
        GIVEN("a customer exists with employment input fields");
        WHEN("the client attempts to retrieve the applicant’s employment information with a valid JWT token");

        var result = this.employmentApiV2.getEmploymentDetails(alphaTestUser);
        THEN("the platform will return a 200 response");

        AND("the platform will return the customer’s employment information");
        var data = result.getData();
        assertEquals(OBEmploymentStatus.EMPLOYED, data.getEmploymentStatus());
        assertEquals("AHB", data.getCompanyName());
        assertEquals("AED 1234.0", data.getMonthlyIncome());
        assertEquals("salary", data.getIncomeSource());

        DONE();
    }

    @Test
    @Order(416)
    void test_update_customer_employment_details_crm() {
        TICKET("AHBDB-4844: Update and get endpoint for Employment declaration in CRM");
        TEST("AC1 Store Employment details - 201 response");
        GIVEN("the customer has provided their Employment details");
        var employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName("AHB")
                .monthlyIncome("AED 4000")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("36")
                .professionCode("99")
                .build();

        WHEN("the client updates the customer profile with valid employment details");
        var result = this.employmentApiV2.createEmploymentDetails(alphaTestUser, employment);
        THEN("we will return a 201 response");
        assertNotNull(result);
        assertEquals("99", result.getData().getProfessionCode());
        assertEquals("AED 4000.0", result.getData().getMonthlyIncome());
        assertEquals("AAN", result.getData().getBusinessCode());

        DONE();

    }

    @Test
    @Order(419)
    void create_idv_details_for_customer_crm_duplicate_document_number() {
        TEST("AHBDB-5678: [CRM] Unique personal/document number check");
        TEST("AC1 Duplicate personal/document number - 409");
        GIVEN("a customer has completed IDV");

        WHEN("the client tries to save an EID personal/document number which already exists ");
        var result = this.customerApiV2.createCustomerIdvDetailsDuplicateDocumentNumber(alphaTestUser);
        THEN("THEN we will return a 409 conflict error ");
        assertNotNull(result);

        DONE();
    }

    @Test
    @Order(423)
    void create_crs_for_customer_crm() {
        TEST("AHBDB-230 Create and get endpoint for tax residency (CRS) details");

        WHEN("the client calls get on the protected customers endpoint with a valid mobile phone number");

        final OBReadCustomer1 currentCustomer = this.customerApiV2.getCurrentCustomer(alphaTestUser);
        assertNotNull(currentCustomer);

        var currentCustomerByMobile =
                this.customerApiV2.addCRSDetails(alphaTestUser,
                        OBWriteCRS2.builder()
                                .data(OBCRSData2.builder()
                                        .taxResidencyCountry(List.of(OBTaxResidencyCountry2.builder()
                                                .country("AE")
                                                .missingTinReason("Country doesn't issue TINs to its " +
                                                        "residents")
                                                .build()))
                                        .uaeResidencyByInvestmentScheme(true)
                                        .otherResidencyJurisdictions(true)
                                        .personalIncomeTaxJurisdictions(List.of("AE", "CN"))
                                        .agreedCertification(true)
                                        .build())
                                .build());
        assertNotNull(currentCustomerByMobile);

        DONE();
    }

    @Test
    @Order(433)
    void create_location_details_for_customer_crm() {
        TEST("AHBDB-4846: Create and get endpoint for Location declaration");
        TEST("AC1 Create address - 201 response");
        GIVEN("the customer has provided their Location details");
        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("WorkTest")
                .address(OBLocationAddress1.builder()
                        .streetName("Al Saada Street")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 63111")
                        .build())
                .build();

        WHEN("the client updates the customer profile with valid Location details");
        var result = this.locationsApiV2.createLocationDetails(alphaTestUser, location);
        THEN("we will return a 201 response");
        assertNotNull(result);
        assertNotNull(result.getData().get(0).getId());

        DONE();
    }

    @Test
    @Order(434)
    void update_location_details_for_customer_crm() {
        TEST("AHBDB-4846: Create and get endpoint for Location declaration");
        TEST("AC4 Get addresses - 200 success response");
        TEST("AC2 Put additional address - 200 response");
        GIVEN("the customer has provided their Location details");

        AND("the client retrieves the customer profile with valid Location details");
        var getResult = this.locationsApiV2.getLocationsDetails(alphaTestUser);
        AND("we will return a 200 response");
        assertNotNull(getResult);
        assertNotNull(getResult.getData().get(0).getId());

        OBLocationDetails1 updatedLocation = OBLocationDetails1.builder()
                .id(getResult.getData().get(0).getId())
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("CoolDepartment")
                        .subDepartment("CoolSubdepartment")
                        .streetName("CoolStreet")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 55555")
                        .build())
                .build();
        DONE();

        WHEN("the client updates the customer profile with valid Location details");
        var updateResult = this.locationsApiV2.updateLocationDetails(alphaTestUser, updatedLocation);
        THEN("we will return a 200 response");
        assertNotNull(updateResult);
        assertEquals(getResult.getData().get(0).getId(), updateResult.getData().get(0).getId());
    }

    @Test
    @Order(435)
    void delete_location_details_for_customer_crm() {
        TEST("AHBDB-4846: Create and get endpoint for Location declaration");
        TEST("AC5 Delete address - 200 response");
        GIVEN("the customer has provided their Location details");

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("WorkTest")
                .address(OBLocationAddress1.builder()
                        .streetName("Al Saada Street")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 63111")
                        .build())
                .build();

        WHEN("the client updates the customer profile with valid Location details");
        var result = this.locationsApiV2.createLocationDetails(alphaTestUser, location);

        AND("the client retrieves the customer profile with valid Location details");
        var getResult = this.locationsApiV2.getLocationsDetails(alphaTestUser);
        AND("we will return a 200 response");
        assertNotNull(getResult);
        assertNotNull(getResult.getData().get(0).getId());
        assertEquals("AE", getResult.getData().get(0).getAddress().getCountry());


        WHEN("the client deletes the Location details");
        var deleteResult = this.locationsApiV2.deleteLocationDetails(alphaTestUser, getResult.getData().get(0).getId());

        THEN("we will return a 200 response");
        assertNotNull(deleteResult);
    }

    @Test
    @Order(436)
    void test_generate_customer_cif() {
        TEST("AHBDB-4103: Generate Customer CIF and update CustomerType");
        TEST("AHBDB-4103 - AC1 Put Customer CIF - 200 OK");
        GIVEN(" a customer exists with a valid phone number and has ");

        WHEN("the client calls get on the protected customers endpoint with a valid mobile phone number and has idv " +
                "details persisted");

        final OBReadCustomer1 currentCustomer = this.customerApiV2.getCurrentCustomer(alphaTestUser);
        assertNotNull(currentCustomer);

        WHEN("Customer tries to generate cif");
        final OBCustomer1 customerUpdatedWithCif =
                this.customerApiV2.putCustomerCif(alphaTestUser).getData().getCustomer().get(0);

        assertNotNull(customerUpdatedWithCif);

        assertEquals(customerUpdatedWithCif.getCustomerType(), OBCustomerType1.BANKING);
        assertEquals(customerUpdatedWithCif.getMobileNumber().substring(6), customerUpdatedWithCif.getCif());

        DONE();
    }

    @Test
    @Order(437)
    void test_set_eid_status() {
        TEST("AHBDB-8292 - Set EID status");
        GIVEN("Customer exists");
        assertNotNull(alphaTestUser.getLoginResponse());

        WHEN("The customer receives the card and client wants to mark it as validated");
        OBWriteEIDStatus1 build = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();
        OBWriteCustomerResponse1 obWriteCustomerResponse1 = customerApiV2.updateCustomerValidations(alphaTestUser,
                build);
        THEN("Status 200 is returned");
        assertNotNull(obWriteCustomerResponse1);
        AND("EID status is set to VALID");
        OBReadCustomer1 currentCustomer = customerApiV2.getCurrentCustomer(alphaTestUser);
        assertEquals(OBEIDStatus.VALID, currentCustomer.getData().getCustomer().get(0).getEidStatus());
    }

    @Test
    @Order(501)
    void logged_id_change_password_test() {
        WHEN("User chooses OTP stepup option");
        //User triggers OTP flow
        //1) -> initiate step up authentication
        WHEN("Stepup auth request was initiated");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(32).build());
        THEN("User will receive OTP");

        //2) -> extract otp from dev simulator
        //extract otp from device simulator
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(StringUtils.isNotBlank(otpCO.getPassword()));

        AND("Stepup auth validation will be triggered");
        final StepUpAuthRequest stepUpAuthValidationRequest =
                StepUpAuthRequest.builder().otp(otpCO.getPassword()).weight(32).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
        THEN("OTP validation should success");

        WHEN("User changes password");

        final String newPassword = UUID.randomUUID().toString();
        final UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();
        User updatedUser = authenticateApi.patchUserCredentials(alphaTestUser, updateUserRequestV1);
        Assertions.assertNotNull(updatedUser);
        alphaTestUser.setUserPassword(newPassword);

    }

    @Test
    @Order(502)
    void test_forgot_password_flow() {
        TEST("AHBDB-3329 & AHBDB-1365 & AHBDB-1594 - Forgot passcode flow");

        WHEN("A customer forgot it's passcode and he initiates the forgot passcode flow using a valid phone number");
        ResetPasswordResponse resetPasswordResponse = authenticateApi.initiateResetPassword(alphaTestUser,
                ResetPasswordRequest.builder().phoneNumber(alphaTestUser.getUserTelephone()).build());
        THEN("Expect an hash to be returned");
        assertTrue(isNotBlank(resetPasswordResponse.getHash()));

        AND("An otp is generated for the user");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPasswordResponse.getHash());
        assertTrue(isNotBlank(otpCO.getPassword()));

        WHEN("A customer received an SMS with the OTP and he enters it");
        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPasswordResponse.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApi.validateResetPasswordOtp(alphaTestUser, validateResetPasswordRequest);
        THEN("Expect otp to be validated and new hash to be returned");
        assertTrue(isNotBlank(validateResetPasswordResponse.getHash()));

        WHEN("A customer verified the otp and not enter a new passcode");
        String newPassword = UUID.randomUUID().toString();
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(validateResetPasswordResponse.getHash())
                .userPassword(newPassword)
                .build();
        UserDto userDto = authenticateApi.updateForgottenPassword(alphaTestUser, updateForgottenPasswordRequest);
        THEN("The user's password was update and he can login with the new credentials");
        assertNotNull(userDto);
        alphaTestUser.setUserPassword(newPassword);
        UserLoginResponseV2 loginResponse = authenticateApi.loginUser(alphaTestUser);
        assertNotNull(loginResponse);
        assertTrue(isNotBlank(loginResponse.getAccessToken()));

        parseLoginResponse(alphaTestUser, loginResponse);

        DONE();

    }


// --------------

    @Test
    @Order(600)
    void user_login_invalid_password_locked_test() throws JsonProcessingException {
        TEST("AHBDB-13309 - Test failing");

        TEST("AHBDB-1462 - AC1 Locking user - 423 locked response");
        GIVEN("Client is logged in and has a device scope token");
        Assertions.assertNotNull(alphaTestUser.getLoginResponse());
        Assertions.assertNotNull(alphaTestUser.getLoginResponse().getAccessToken());

        UserLoginRequestV2 userLoginRequest = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password("WrongPassword")
                .build();

        WHEN("The client attempts to login with a wrong password for 3 times");
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequest, 401);
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequest, 401);

        THEN("Status code is 423 ( Locked )");
        this.authenticateApi.loginUserProtectedError(alphaTestUser, userLoginRequest, 423);
    }

    // --------------
    //Clean up
//    @Test
//    @Order(9000)
//    void user_delete_test_success() {
//        TEST("AHBDB-1583 - Delete customer API");
//        GIVEN("A customer and a user already exists in the databases");
//
//        WHEN("Calling delete customer api");
//        this.customerApiV2.deleteCustomer(alphaTestUser);
//
//
//        THEN("Status code 204(NO_CONTENT) is returned");
//        AND("Customer is deleted from database");
//        OBReadCustomerId1 response = this.customerApiV2.getCustomersByEmail(alphaTestUser.getUserEmail());
//
//        await().atMost(5, SECONDS).with()
//                .pollInterval(1, SECONDS)
//                .pollDelay(2, SECONDS)
//                .untilAsserted(() -> {
//                    AND("User is deleted from AM as well");
//                    this.authenticateApi.loginUser(alphaTestUser);
//                });
//        DONE();
//    }


//    @Test
//    //@Order(30) //Customer scope
//    @Order(300)
//        //Account scope
//    void dump() {
//        AlphaTestUsers.builder().alphaTestUsers(List.of(this.alphaTestUser))
//                .build()
//                .writeToFile();
//        DONE();
//    }

    private void checkFieldAssertionsForGetUserBalancesTestSuccess(OBReadBalance1DataBalance balance) {
        assertNotNull(balance);

        assertNotNull(balance.getAccountId());

        assertNotNull(balance.getType());

        OBReadBalance1DataAmount balanceAmount = balance.getAmount();
        assertNotNull(balanceAmount);

        assertNotNull(balanceAmount.getCurrency());

        assertEquals("AED", balanceAmount.getCurrency());
    }

    //TODO:: Add to utils
    private void addJwtToMdc(AlphaTestUser alphaTestUser) {
        MDC.put("JWT", alphaTestUser.getJwtToken());
    }


    private void updateUserScopeTo(String scope) {
        if (scope == ACCOUNT_SCOPE) {
            log.error("YOU SHOULD NOT MANUALLY UPGRADE TO ACCOUNT SCOPE");
            return;
        }

        User user = this.authenticateApi.patchUserScope(alphaTestUser,
                UserScope.builder()
                        .sn(scope)
                        .build());

        THEN("Status code is 200(OK)");
        AND("User status is " + scope);

        if (user != null) {
            assertEquals(scope, user.getSn());
        } else {
            fail();
        }

        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(alphaTestUser);

        THEN("Status code 200(OK) is returned");
        AND("Token scope is " + scope);
        if (loginResponse != null && loginResponse.getAccessToken() != null) {
            assertEquals(scope, loginResponse.getScope().toLowerCase());
        } else {
            fail();
        }
        parseLoginResponse(alphaTestUser, loginResponse);
    }

}
