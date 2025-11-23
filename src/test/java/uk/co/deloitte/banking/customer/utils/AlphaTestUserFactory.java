package uk.co.deloitte.banking.customer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.commons.util.StringUtils;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUsers;
import uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes.TripleDesUtil;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.email.api.EmailVerificationApi;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.refreshtoken.api.RefreshTokenApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.*;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUsers.USERS_FILE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Slf4j
@Singleton
public class AlphaTestUserFactory {

    @Inject
    private OtpApi otpApi;

    @Inject
    private EmailVerificationApi emailApi;

    @Inject
    private CustomerApiV2 customerApiV2;

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
    private RelationshipApi relationshipApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private RefreshTokenApi refreshTokenApi;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private TripleDesUtil tripleDesUtil;

    private AlphaTestUser alphaTestUser;

    private AlphaTestUser validateUserConflicts(AlphaTestUser alphaTestUser) {
        //Check telephone number
        alphaTestUser = checkMobileConflict(alphaTestUser);
        alphaTestUser = checkEmailConflict(alphaTestUser);
        alphaTestUser = checkEidConflict(alphaTestUser);

        return alphaTestUser;
    }

    private AlphaTestUser checkMobileConflict(AlphaTestUser alphaTestUser) {
        final String userTelephone = alphaTestUser.getUserTelephone();

        if (userTelephone != null) {
            OBReadCustomerId1 customersByMobile = customerApiV2.getCustomersByMobile(userTelephone);
            if (customersByMobile != null && !customersByMobile.getData().isEmpty()) {
                log.warn("checkMobileConflict::userTelephone [{}] - CONFLICT", userTelephone);
                alphaTestUser.generateUserTelephone();
                //Recursively check until a unique mobile is found
                //TODO::Could be replaced with a delete by userId
                return checkMobileConflict(alphaTestUser);
            }
        }
        return alphaTestUser;
    }


    private AlphaTestUser checkEmailConflict(AlphaTestUser alphaTestUser) {
        final String userEmail = alphaTestUser.getUserEmail();
        log.info("checkEmailConflict::userEmail [{}]", userEmail);

        if (userEmail != null) {
            OBReadCustomerId1 customers = customerApiV2.getCustomersByEmail(userEmail);
            if (customers != null && !customers.getData().isEmpty()) {
                log.warn("checkEmailConflict::userEmail [{}] - CONFLICT", userEmail);
                alphaTestUser.setUserEmail(generateRandomEmail());
                //Recursively check until a unique mobile is found
                //TODO::Could be replaced with a delete by userId
                return checkEmailConflict(alphaTestUser);
            }
        }
        return alphaTestUser;
    }

    private AlphaTestUser checkEidConflict(AlphaTestUser alphaTestUser) {
        final String userEid = alphaTestUser.getEid();
        log.info("checkEidConflict::userEid [{}]", userEid);
        if (userEid != null) {
            OBReadCustomerId1 customers = customerApiV2.getCustomersByEid(userEid);
            if (customers != null && !customers.getData().isEmpty()) {
                log.warn("checkEidConflict::userEid [{}] - CONFLICT", userEid);
                alphaTestUser.setEid(generateRandomEID());
                //Recursively check until a unique mobile is found
                //TODO::Could be replaced with a delete by userId
                return checkEidConflict(alphaTestUser);
            }
        }
        return alphaTestUser;
    }

    // Base method to create a new customer record on the DTP platform
    public AlphaTestUser setupUser(AlphaTestUser alphaTestUser) {
        log.info("Creating valid test user");

        alphaTestUser = validateUserConflicts(alphaTestUser);

        LoginResponseV1 loginResponse = authenticateApiV2.registerUserAndDevice(alphaTestUser);
        parseLoginResponse(alphaTestUser, loginResponse);

        LoginResponseV1 response = authenticateApiV2.loginDevice(alphaTestUser);
        parseLoginResponse(alphaTestUser, response);

        final UpdateUserRequestV1 user = UpdateUserRequestV1.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .mail(alphaTestUser.getUserEmail())
                .build();

        this.authenticateApiV2.patchUser(alphaTestUser, user);

        otpApi.sendDestinationToOTP(alphaTestUser, 204);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());

        final String otp = otpCO.getPassword();
        otpApi.postOTPCode(alphaTestUser, 200, otp);

        return setupUserCerts(alphaTestUser);
    }

    /**
     * Create user Certs
     * Upload them
     * Upgrade scope to registration
     */
    public AlphaTestUser setupUserCerts(AlphaTestUser alphaTestUser) {
        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));

        NOTE("Upload cert");
        this.certificateApi.uploadCertificate(alphaTestUser);


        NOTE("Wait for cert event");
        await().atMost(5, SECONDS).with()
                .pollDelay(4, SECONDS)
                .untilAsserted(() -> assertTrue(true));

        NOTE("Set SN to Registration");
        final UpdateUserRequestV1
                user2 = UpdateUserRequestV1.builder()
                .userPassword(alphaTestUser.getUserPassword())
                .sn("REGISTRATION")
                .build();

        this.authenticateApiV2.patchUser(alphaTestUser, user2, true).statusCode(200).assertThat();

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        await().atMost(6, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {

                    UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2.loginUserProtected(alphaTestUser,
                            request,
                            alphaTestUser.getDeviceId(), true);

                    assertNotNull(userLoginResponseV2.getScope());
                    NOTE("Scope returned as " + userLoginResponseV2.getScope());
                    parseLoginResponse(alphaTestUser, userLoginResponseV2);
                });


        return alphaTestUser;
    }

    /**
     * Create user Certs
     * Upload them
     * Upgrade scope to registration
     */
    @Deprecated
    public AlphaTestUser reRegistorDeviceAndLogin(AlphaTestUser alphaTestUser) {
        final ObjectMapper ob = new ObjectMapper();
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
        String signedSignature = null;
        try {
            signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(request));
            this.certificateProtectedApi.validateCertificate(alphaTestUser,
                    alphaTestUser.getDeviceId(),
                    ob.writeValueAsString(request),
                    signedSignature,
                    204);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", certificateProtectedApi);
        }
        UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2.loginUser(alphaTestUser, request, alphaTestUser.getDeviceId(), false);
        TokenUtils.parseLoginResponse(alphaTestUser, userLoginResponseV2);

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = org.apache.commons.lang3.StringUtils.left(cardNumber, 6).concat(org.apache.commons.lang3.StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(org.apache.commons.lang3.StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        String pinBlock = null;
        try {
            pinBlock = tripleDesUtil.encryptUserPin("4321", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }

        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        CardPinValidationRequest pinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);
        TokenUtils.parseLoginResponse(alphaTestUser, loginResponse);

        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);
        String otpPass = otpCO.getPassword();
        otpApi.postOTPCode(alphaTestUser, 200, otpPass);
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        try {
        signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(userLoginRequestV2));
        this.certificateApi.uploadCertificate(alphaTestUser);
            this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(),
                    ob.writeValueAsString(userLoginRequestV2), signedSignature, 204);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", certificateProtectedApi);
        }
        userLoginResponseV2 =
                authenticateApiV2.loginUser(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), false);
        TokenUtils.parseLoginResponse(alphaTestUser, userLoginResponseV2);

        authenticateApiV2.cardPinValidation(alphaTestUser, pinRequest, cardId, signedSignature);
        UserLoginResponseV2 loginResponseV2 = authenticateApiV2.loginUser(alphaTestUser);
        TokenUtils.parseLoginResponse(alphaTestUser, loginResponseV2);

        return alphaTestUser;
    }

    public AlphaTestUser reRegistorDeviceAndLogin1(AlphaTestUser alphaTestUser) throws Throwable {
        final ObjectMapper ob = new ObjectMapper();
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
        try {
            this.certificateProtectedApi.validateCertificate(alphaTestUser,
                    alphaTestUser.getDeviceId(),
                    ob.writeValueAsString(request),
                    signedSignature,
                    204);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", certificateProtectedApi);
        }
        UserLoginResponseV2 userLoginResponseV2 =authenticateApiV2.loginUser(alphaTestUser, request, alphaTestUser.getDeviceId(), false);
        TokenUtils.parseLoginResponse(alphaTestUser, userLoginResponseV2);
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = org.apache.commons.lang3.StringUtils.left(cardNumber, 6).concat(org.apache.commons.lang3.StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(org.apache.commons.lang3.StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("4321", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        CardPinValidationRequest pinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();
//        OBReadCustomer1 getCustomer = customerApiV2.getCurrentCustomer(alphaTestUser);
//        assertEquals(OBCustomerType1.BANKING.toString(), getCustomer.getData().getCustomer().get(0).getCustomerType().toString());
        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);
        TokenUtils.parseLoginResponse(alphaTestUser, loginResponse);
        otpApi.sendDestinationToOTP(alphaTestUser, 204);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);
        String otpPass = otpCO.getPassword();
        otpApi.postOTPCode(alphaTestUser, 200, otpPass);
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();
        signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(userLoginRequestV2));
        this.certificateApi.uploadCertificate(alphaTestUser);
        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(),
                ob.writeValueAsString(userLoginRequestV2), signedSignature, 204);
        userLoginResponseV2 =
                authenticateApiV2.loginUser(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), false);
        TokenUtils.parseLoginResponse(alphaTestUser, userLoginResponseV2);
        authenticateApiV2.cardPinValidation(alphaTestUser, pinRequest, cardId, signedSignature);
        UserLoginResponseV2 loginResponseV2 = authenticateApiV2.loginUser(alphaTestUser);
        TokenUtils.parseLoginResponse(alphaTestUser, loginResponseV2);
        return alphaTestUser;
    }

    public AlphaTestUser setupCustomer(AlphaTestUser alphaTestUser) {
        alphaTestUser = this.setupUser(alphaTestUser);

        customerApiV2.createCustomerSuccess(alphaTestUser, generateCustomer(alphaTestUser));

        this.authenticateApi.patchUserV2(alphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn("CUSTOMER")
                        .build());

        UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2.loginUserProtected(alphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUser.getUserId())
                        .password(alphaTestUser.getUserPassword())
                        .build(),
                alphaTestUser.getDeviceId(), true);

        alphaTestUser = parseLoginResponse(alphaTestUser, userLoginResponseV2);
        return alphaTestUser;
    }

    /**
     * @param alphaTestUser
     * @param userScope
     * @return
     */
    public AlphaTestUser setupV2UserAndV2Customer(AlphaTestUser alphaTestUser, String userScope) {
        alphaTestUser = this.setupUser(alphaTestUser);

        customerApiV2.createCustomerSuccess(alphaTestUser, generateCustomer(alphaTestUser));

        this.authenticateApi.patchUserV2(alphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn(StringUtils.isNotBlank(userScope) ? userScope : CUSTOMER_SCOPE)
                        .build());

        UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2.loginUserProtected(alphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUser.getUserId())
                        .password(alphaTestUser.getUserPassword())
                        .build(),
                alphaTestUser.getDeviceId(), true);

        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        return alphaTestUser;

    }

    public String createChildInForgerock(AlphaTestUser parentUser) {
        return createChildInForgerock(parentUser, "validtestpassword");
    }

    public String createChildInForgerock(AlphaTestUser parentUser, String temporaryPassword) {
        LoginResponse response = this.authenticateApiV2.createRelationshipAndUser(parentUser,
                UserRelationshipWriteRequest.builder().tempPassword(temporaryPassword).build());

        assertNotNull(response.getUserId());
        return response.getUserId();
    }

    public OBWriteDependant1 generateDependantBody(String childId, int ageInYears, String fullName,
                                                   OBGender childGender, OBRelationshipRole parentRole) {
        OBRelationshipRole dependantRole;

        if (childGender == OBGender.MALE) {
            dependantRole = OBRelationshipRole.SON;
        } else {
            dependantRole = OBRelationshipRole.DAUGHTER;
        }

        return OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(childId))
                        .dateOfBirth(LocalDate.now().minusYears(ageInYears))
                        .fullName(fullName)
                        .gender(childGender)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(parentRole)
                        .dependantRole(dependantRole)
                        .build())
                .build();
    }

    public String createChildInCRM(AlphaTestUser parentUser, OBWriteDependant1 obWriteDependant1) {

        OBReadRelationship1 createResponse =
                this.relationshipApi.createDependant(parentUser, obWriteDependant1);
        assertNotNull(createResponse.getData().getRelationships().get(0).getConnectionId().toString());

        return createResponse.getData().getRelationships().get(0).getConnectionId().toString();
    }

    public AlphaTestUser createChildCustomer(AlphaTestUser alphaTestUserParent, AlphaTestUser alphaTestUserChild,
                                             String relationshipId, String dependantId) {
        return this.createChildCustomer(alphaTestUserParent, alphaTestUserChild, relationshipId, dependantId,
                "validtestpassword", "newvalidpassword");
    }

    public AlphaTestUser createChildCustomer(AlphaTestUser alphaTestUserParent, AlphaTestUser alphaTestUserChild,
                                             String relationshipId, String dependantId,
                                             String tempPassword, String newPassword) {
        alphaTestUserChild.setCustomerId(dependantId);
        alphaTestUserChild.setUserId(dependantId);

        //Generate OTP code for kid
        otpApi.sentChildOTPCode(alphaTestUserParent, 204, relationshipId);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        assertNotNull(otpCO);
        String otpCode = otpCO.getPassword();

        //register kids device
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(tempPassword)
                .otp(otpCode)
                .build();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2
                .registerDependantUserDevice(alphaTestUserChild, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(alphaTestUserChild, userLoginResponseV2);

        //do cert signing for kid
        alphaTestUserChild = setupUserCerts(alphaTestUserChild);
        assertNotNull(alphaTestUserChild);

        // update kids temp password
        final String oldPassword = alphaTestUserChild.getUserPassword();

        this.authenticateApiV2.patchUser(alphaTestUserChild,
                UpdateUserRequestV1.builder()
                        .userPassword(newPassword)
                        .build());

        UserLoginResponseV2 userLoginResponse = authenticateApiV2.loginUserProtected(alphaTestUserChild,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUserChild.getUserId())
                        .password(newPassword)
                        .build(),
                alphaTestUserChild.getDeviceId(), true);

        parseLoginResponse(alphaTestUserChild, userLoginResponse);
        assertNotEquals(oldPassword, newPassword);
        alphaTestUserChild.setUserPassword(newPassword);

        //kid log in
        UserLoginResponseV2 loginResponse = this.authenticateApiV2.loginUser(alphaTestUserChild);
        Assertions.assertEquals(REGISTRATION_SCOPE, loginResponse.getScope());
        parseLoginResponse(alphaTestUserChild, loginResponse);

        //kid customer scope
        this.authenticateApiV2.patchUser(alphaTestUserChild,
                UpdateUserRequestV1.builder()
                        .sn("CUSTOMER")
                        .build());

        UserLoginResponseV2 userLoginResponseCustomer = authenticateApiV2.loginUserProtected(alphaTestUserChild,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUserChild.getUserId())
                        .password(alphaTestUserChild.getUserPassword())
                        .build(),
                alphaTestUserChild.getDeviceId(), true);

        Assertions.assertEquals(CUSTOMER_SCOPE, userLoginResponseCustomer.getScope());
        parseLoginResponse(alphaTestUserChild, userLoginResponseCustomer);
        return alphaTestUserChild;
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public AlphaTestUser refreshAccessToken(AlphaTestUser alphaTestUser) {
        if (alphaTestUser == null) {
            return null;
        }

        assertNotNull(alphaTestUser.getRefreshToken());
        assertNotNull(alphaTestUser.getJwtToken());

        RefreshRequest refreshRequest = RefreshRequest.builder()
                .refreshToken(alphaTestUser.getRefreshToken())
                .build();

        UserLoginResponseV2 newToken = refreshTokenApi.refreshAccessToken(refreshRequest);
        assertEquals(alphaTestUser.getScope(), newToken.getScope());
        assertNotEquals(alphaTestUser.getJwtToken(), newToken.getAccessToken());

        parseLoginResponse(alphaTestUser, newToken);

        return alphaTestUser;
    }

    @Deprecated
    public void reAuthenticateToGetDeviceScope(AlphaTestUser alphaTestUser) {
        LoginResponse loginResponse = this.authenticateApi.loginDevice(alphaTestUser);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
        assertEquals("device", loginResponse.getScope());
        alphaTestUser.setLoginResponse(loginResponse);
    }

    @Deprecated
    public void setAccountPasscodeFlow(AlphaTestUser alphaTestUser) throws JsonProcessingException {
        final User user = User.builder()
                .userPassword(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .mail(alphaTestUser.getUserEmail())
                .build();
        User updatedUser = this.authenticateApi.patchUser(alphaTestUser, user);
        assertNotNull(updatedUser);
        alphaTestUser.setUserPassword(alphaTestUser.getUserPassword());
    }

    @Deprecated
    public void reAuthenticateWithUserPasscodeAsDeviceHash(AlphaTestUser alphaTestUser) {
        LoginResponse loginResponse = this.authenticateApi.loginDevice(alphaTestUser);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
        alphaTestUser.setLoginResponse(loginResponse);
    }

    @Deprecated
    public void reauthenticate(AlphaTestUser alphaTestUser) {
        LoginResponse loginResponse = this.authenticateApi.loginUser(alphaTestUser);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
        alphaTestUser.setLoginResponse(loginResponse);
    }

    @Deprecated
    public void upgradeScopeToRegistration(AlphaTestUser alphaTestUser) throws JsonProcessingException {
        NOTE("Upgrade the scope = REGISTRATION");
        final User user = User.builder()
                .userPassword(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .sn("REGISTRATION")
                .build();
        User updatedUser = this.authenticateApi.patchUser(alphaTestUser, user);
        assertNotNull(updatedUser);
        assertEquals("REGISTRATION", updatedUser.getSn());

        LoginResponse registrationloginResponse = this.authenticateApi.loginUser(alphaTestUser);
        assertNotNull(registrationloginResponse);
        AND("Scope of the token is REGISTRATION");
        if (registrationloginResponse.getAccessToken() != null) {
            assertEquals("registration", registrationloginResponse.getScope());
            log.info("Registration token: {}", registrationloginResponse.getAccessToken());
            alphaTestUser.setLoginResponse(registrationloginResponse);
        } else {
            Assertions.fail();
        }
    }

    // This method will create a customer and validate their email address
    public AlphaTestUser createCustomerWithValidatedEmail() {

        //Call the base customer creation method
        alphaTestUser = setupV2UserAndV2Customer(new AlphaTestUser(), null);

        NOTE("Verifying email");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        this.emailApi.verifyEmailLink(alphaTestUser, emailVerification, 200);

        return alphaTestUser;
    }

    private OBWriteCustomer1 generateCustomer(AlphaTestUser alphaTestUser) {
        return OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(generateEnglishRandomString(10))
                        .dateOfBirth(LocalDate.now().minusYears(25))
                        .email(alphaTestUser.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language(alphaTestUser.getLanguage())
                        .firstName(generateEnglishRandomString(10))
                        .lastName(generateEnglishRandomString(10))
                        .fullName(generateEnglishRandomString(20))
                        .gender(alphaTestUser.getGender())
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .termsVersion(LocalDate.of(2020, 12, 20))
                        .termsAccepted(true)
                        .address(OBPostalAddress6.builder()
                                .buildingNumber(generateRandomBuildingNumber())
                                .streetName(generateRandomStreetName())
                                .countrySubDivision(generateRandomCountrySubDivision())
                                .country("AE")
                                .postalCode(generateRandomPostalCode())
                                .addressLine(Collections.singletonList(generateRandomAddressLine()))
                                .build())
                        .build()).build();
    }

    public AlphaTestUser loadFromFile() {
        try {
            final AlphaTestUsers alphaTestUsers = readAlphaTestUsersFromFile();
            if (alphaTestUsers != null && alphaTestUsers.alphaTestUsers != null && !alphaTestUsers.alphaTestUsers.isEmpty()) {
                return alphaTestUsers.alphaTestUsers.get(0);
            }
        } catch (Exception ex) {
            log.info("Exception loading file", ex);
        }
        return null;
    }

    private AlphaTestUsers readAlphaTestUsersFromFile() {
        try {
            return objectMapper.readValue(new FileReader(USERS_FILE), AlphaTestUsers.class);
        } catch (IOException e) {
            log.info("Not loading users from file");
        }
        return null;
    }
}
