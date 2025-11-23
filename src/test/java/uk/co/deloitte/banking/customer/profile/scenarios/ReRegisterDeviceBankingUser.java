package uk.co.deloitte.banking.customer.profile.scenarios;

import com.google.common.collect.Lists;
import io.micronaut.test.annotation.MicronautTest;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBAccount4Account;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6Data;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.CardPinValidationRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRegisterDeviceRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ModificationOperation;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes.TripleDesUtil;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerType1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNTS_LIMITED_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils.parseLoginResponse;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;

@Tag("AHBDB-7463")
@Tag("AHBDB-9233")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReRegisterDeviceBankingUser {

    @Inject
    private AccountApi accountApi;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private CertificateProtectedApi certificateProtectedApi;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private OtpApi otpApi;

    @Inject
    TripleDesUtil tripleDesUtil;

    private static final String LOCKED_CARD_CODE = "UAE.ERROR.LOCKED";
    private static final String LOCKED_CARD_MESSAGE = "Card locked due to exceeded pin validation attempts";

    private static final String UAE_ERROR_NOT_FOUND = "UAE.ERROR.NOT_FOUND";
    private static final String UAE_ERROR_UNAUTHORIZED = "UAE.ERROR.UNAUTHORIZED";

    protected ObjectMapper ob = new ObjectMapper();

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser marketplaceTestUser;

    public void setupTestUser(boolean rebuild) throws Throwable {

        if(this.alphaTestUser == null || rebuild) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            OBWritePartialCustomer1 writePartialCustomer1 = OBWritePartialCustomer1.builder()
                    .data(OBWritePartialCustomer1Data.builder()
                            .customerState(OBCustomerStateV1.ACCOUNT_CREATION_IN_PROGRESS)
                            .build())
                    .build();

            customerApiV2.updateCustomer(alphaTestUser, writePartialCustomer1, 200);
            alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);

            OBWritePartialCustomer1 writePartialCustomer2 = OBWritePartialCustomer1.builder()
                    .data(OBWritePartialCustomer1Data.builder()
                            .customerState(OBCustomerStateV1.ACCOUNT_CREATION_EMBOSS_NAME_SPECIFIED)
                            .build())
                    .build();
            customerApiV2.updateCustomer(alphaTestUser, writePartialCustomer2, 200);

            CreateCard1 createCard1 = CreateCard1.builder()
                    .data(CreateCard1Data.builder()
                            .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                            .embossedName(alphaTestUser.getName())
                            .accounts(List.of(CreateCardAccount1.builder()
                                    .accountCurrency("AED")
                                    .accountName("CURRENT")
                                    .accountNumber(alphaTestUser.getAccountNumber())
                                    .accountType(CardsApiFlows.ACCOUNT_TYPE)
                                    .openDate(LocalDateTime.parse("2011-12-03T10:15:30",
                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                    .seqNumber("1")
                                    .build()))
                            .build())
                    .build();

            final CreateCard1Response createCardResponse = cardsApiFlows.createVirtualDebitCard(alphaTestUser, createCard1);

            String createdCardNumber = createCardResponse.getData().getCardNumber();
            ActivateCard1 validActivateCard1 = ActivateCard1.builder()
                    .cardNumber(createdCardNumber)
                    .lastFourDigits(createdCardNumber.substring(createdCardNumber.length() - 4))
                    .cardNumberFlag("M")
                    .cardExpiryDate(createCardResponse.getData().getExpiryDate())
                    .modificationOperation(ModificationOperation.V)
                    .operationReason("Operation reason")
                    .build();

            cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1, 200);
//            VIRTUAL CARD IS NOW ACTIVATED AT THIS POINT
            final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
            Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
            final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
            final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
            final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
            Assertions.assertTrue(!Objects.isNull(cardCVvDetails.getData()));
            Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
            Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
            Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getExpiryDate()));

//            ORDER A PHYSICAL CARD
            OBReadAccount6 response = this.accountApi.getAccountsV2(alphaTestUser);
            THEN("Status code 200(User Accounts) is returned");
            AND("User Accounts has been returned");
            OBReadAccount6Data data = response.getData();
            assertNotNull(data);

            List<OBAccount6> accountList = response.getData().getAccount();
            OBAccount6 account = accountList.get(0);
            assertNotNull(account);
            List<OBAccount4Account> accounts = account.getAccount();
            assertNotNull(accounts);
            assertEquals(3, accounts.size());
            OBAccount4Account accountDetails = accounts.get(0);
            assertNotNull(accountDetails.getSchemeName());
            assertEquals("IBAN.NUMBER", accountDetails.getSchemeName());
            assertNotNull(accountDetails.getIdentification());
            final WritePhysicalCard1 issuePhysicalCard = WritePhysicalCard1.builder()
                    .awbRef("awbRef")
                    .dtpReference("DTP" + RandomStringUtils.randomAlphanumeric(7))
                    .iban(accountDetails.getIdentification())
                    .phoneNumber(alphaTestUser.getUserTelephone())
                    .recipientName(alphaTestUser.getName())
                    .deliveryAddress(CardDeliveryAddress1.builder()
                            .buildingNumber("123")
                            .country("UAE")
                            .postalCode("wer234")
                            .addressLine(Lists.newArrayList("addressline 1"))
                            .streetName("valley road")
                            .townName("Dubai")
                            .build())
                    .build();
            final String plainCardNumber = tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber());

            cardsApiFlows.issuePhysicalCard(alphaTestUser, cardId, issuePhysicalCard);

            final ActivateCard1 activateCard1 = ActivateCard1.builder()
                    .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                    .cardNumberFlag("P")
                    .cardNumber(plainCardNumber)
                    .lastFourDigits(StringUtils.right(cardNumber, 4))
                    .modificationOperation(ModificationOperation.A)
                    .operationReason("TEST : activate card")
                    .build();
            cardsApiFlows.activateDebitCard(alphaTestUser, activateCard1, 200);

//            PHYSICAL CARD IS NOW ACTIVATED
//            SET CARD PIN AND VALIDATE
            final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
            final WriteCardPinRequest1 pinSetRequest = WriteCardPinRequest1.builder()
                    .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                    .cardNumber(cardNumber)
                    .cardNumberFlag("M")
                    .lastFourDigits(StringUtils.right(cardNumber, 4))
                    .pinServiceType("C")
                    .pinBlock(pinBlock)
                    .build();
            cardsApiFlows.setDebitCardPin(alphaTestUser, pinSetRequest, 200);

            final CardPinValidation1 cardPinValidation1 = CardPinValidation1.builder()
                    .cardPinValidation1Data(CardPinValidation1Data.builder()
                            .pin(pinBlock)
                            .build())
                    .build();
            cardsApiFlows.validateDebitCardPin(alphaTestUser, cardId, cardPinValidation1);
            NOTE("SETUP NOW FINISHED");
        }
    }

    public void setupMarketplaceTestUser() {
        if (this.marketplaceTestUser == null) {
            marketplaceTestUser = new AlphaTestUser();
            marketplaceTestUser = alphaTestUserFactory.setupCustomer(marketplaceTestUser);
        }
    }

    @Order(1)
    @Test
    public void happy_path_register_new_device_banking_customer() throws Throwable {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-9233: Card Pin Validation");
        TEST("AHBDB-11923: AC1, AC5, AC6, AC7, AC8, AC10 - Re-Register Flow for banking customer");
        TEST("AHBDB-11914: AC0, AC1, AC2 - Happy Path - Banking user successfully validates their pin");
        setupTestUser(false);

        var getResponse = customerApiV2.getCurrentCustomer(alphaTestUser);
        assertEquals(OBCustomerType1.BANKING, getResponse.getData().getCustomer().get(0).getCustomerType());

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        CardPinValidationRequest request = CardPinValidationRequest.builder().pin(pinBlock).build();

        WHEN("The customer initiates the register new device flow");
        OBReadCustomer1 getCustomer = customerApiV2.getCurrentCustomer(alphaTestUser);
        assertEquals(OBCustomerType1.BANKING.toString(), getCustomer.getData().getCustomer().get(0).getCustomerType().toString());

        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);
        THEN("The platform responds with a 201");
        AND("Returns a scope of device");
        assertEquals("device", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);
        DONE();

//        AC5
        GIVEN("A banking customer wants to register a new device");
        AND("The customer receives an OTP on their phone");
        WHEN("The customer sends the OTP to the platform");
        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);
        String otpPass = otpCO.getPassword();

        THEN("Then the platform will respond with a 200 Response");
        otpApi.postOTPCode(alphaTestUser, 200, otpPass);
        DONE();

//        AC6
        GIVEN("Given a customer has completed the OTP flow");
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();
        String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(userLoginRequestV2));

        this.certificateApi.uploadCertificate(alphaTestUser);

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(),
                ob.writeValueAsString(userLoginRequestV2), signedSignature, 204);
        WHEN("They want to upload their certificate");
        THEN("A 204 is returned");
        AND("An event is sent to Kafka");
        DONE();

//        AC7, AC8, AC10
        GIVEN("A banking user wants to register a new device");
        AND("They have completed the OTP flow");
        AND("They have uploaded the certificate");

        UserLoginResponseV2 userLoginResponseV2 =
                authenticateApiV2.loginUser(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), false);
        WHEN("The customer logs in with the new device");
        THEN("The platform will return a 200 Response");
        AND("The scope will be set to accounts-limited");
        Assertions.assertEquals(ACCOUNTS_LIMITED_SCOPE, userLoginResponseV2.getScope());
        Assertions.assertEquals(alphaTestUser.getUserId(), userLoginResponseV2.getUserId());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);
        DONE();

        GIVEN("The customer has logged into their new device");
        AND("They have the scope of accounts-limited");
        WHEN("They validate their card pin correctly");
        THEN("The platform will return a 200 Response");
        authenticateApiV2.cardPinValidation(alphaTestUser, request, cardId, signedSignature);
        DONE();

        GIVEN("The customer has successfully validated their card pin");
        WHEN("They log in using their new device");
        UserLoginResponseV2 loginResponseV2 = authenticateApiV2.loginUser(alphaTestUser);
        assertNotNull(loginResponseV2);
        THEN("The customer will have the scope accounts customer");
        assertEquals(ACCOUNT_SCOPE, loginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, loginResponseV2);
        AND("Their old device will be disabled");
        final UserLoginRequestV2 request2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        final String signedSignature2 =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        ob.writeValueAsString(request2),
                        alphaTestUser.getPreviousPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getPreviousDeviceId(),
                ob.writeValueAsString(request2), signedSignature2, 204);

        ValidatableResponse error = authenticateApiV2.loginUserValidatable(alphaTestUser, request2,
                alphaTestUser.getPreviousDeviceId(), false);
        error.statusCode(401).assertThat();
        OBErrorResponse1 errorResponse = error.extract().as(OBErrorResponse1.class);
        assertEquals(UAE_ERROR_UNAUTHORIZED, errorResponse.getCode());
        assertEquals("Device is disabled", errorResponse.getMessage());

        NOTE("Log in to new device fresh");
        final UserLoginRequestV2 request3 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        var loginResponse3 = authenticateApiV2.loginUserProtected(alphaTestUser, request3, alphaTestUser.getDeviceId(), true);
        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"", "!£$%%", "123456"})
    public void negative_test_initiate_re_register_with_invalid_email_400_response(String invalidEmail) throws Throwable {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-11924: AC2 - Invalid Email");
        TEST("AHBDB-11916: AC3 - Invalid Email");
        setupTestUser(false);

        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .email(invalidEmail)
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .build();

        WHEN("The client attempts to register the device with an invalid email/phone");
        OBErrorResponse1 error =
                this.authenticateApiV2.registerNewDeviceError(alphaTestUser, userRegisterDeviceRequestV1, 400);

        THEN("The platform will return a 400 response");
        assertNotNull(error);
        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"", "12345678"})
    public void negative_test_initiate_re_register_with_invalid_phone_400_response(String invalidPhoneNumber) throws Throwable {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-11925: AC2 - Invalid Phone");
        TEST("AHBDB-11916: AC3 - Invalid Phone");
        setupTestUser(false);

        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .phoneNumber(invalidPhoneNumber)
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .build();

        WHEN("The client attempts to register the device with an invalid email/phone");
        OBErrorResponse1 error =
                this.authenticateApiV2.registerNewDeviceError(alphaTestUser, userRegisterDeviceRequestV1, 400);

        THEN("The platform will return a 400 response");
        assertNotNull(error);
        DONE();
    }

    @Order(2)
    @Test
    public void negative_test_initiate_re_register_with_wrong_password_401_response() throws Throwable {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-11926: AC3 - Initiate re register flow with wrong password");
        setupTestUser(false);

        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .password("wrongpassword")
                .deviceId(alphaTestUser.getDeviceId())
                .build();

        WHEN("The client attempts to register the device with the wrong password");
        OBErrorResponse1 error =
                this.authenticateApiV2.registerNewDeviceError(alphaTestUser, userRegisterDeviceRequestV1, 401);

        THEN("The platform will return a 401 response");
        assertNotNull(error);
        DONE();
    }

    @Order(2)
    @Test
    public void negative_test_initiate_re_register_with_user_that_does_not_exist() {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-11927: AC4 - User does not exist");
        TEST("AHBDB-11917: AC4 - User does not exist");
        AlphaTestUser nonExistentUser = new AlphaTestUser();
        GIVEN("A banking customer wants to register a new device");
        UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .phoneNumber(nonExistentUser.getUserTelephone())
                .password(nonExistentUser.getUserPassword())
                .deviceId(nonExistentUser.getDeviceId())
                .build();

        WHEN("That customer is not found");
        OBErrorResponse1 error =
                this.authenticateApiV2.registerNewDeviceError(nonExistentUser, userRegisterDeviceRequestV1, 404);
        THEN("The platform will return a 404 Response");
        assertNotNull(error);
        assertEquals(UAE_ERROR_NOT_FOUND, error.getCode());
        assertEquals("No users found matching the query", error.getMessage());
        DONE();
    }

    @Order(3)
    @Test
    public void happy_path_re_register_device_for_non_banking_customer() throws JsonProcessingException {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-11928: AC9 - Re-Register a marketplace customer");
        setupMarketplaceTestUser();
        marketplaceTestUser.setPreviousDeviceId(marketplaceTestUser.getDeviceId());
        marketplaceTestUser.setPreviousDeviceHash(marketplaceTestUser.getDeviceHash());
        marketplaceTestUser.setPreviousPrivateKeyBase64(marketplaceTestUser.getPrivateKeyBase64());
        marketplaceTestUser.setPreviousPublicKeyBase64(marketplaceTestUser.getPublicKeyBase64());

        marketplaceTestUser.setDeviceId(UUID.randomUUID().toString());
        marketplaceTestUser.setDeviceHash(UUID.randomUUID().toString());
        GIVEN("A marketplace customer wants to register a new device");
        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(marketplaceTestUser);
        assertEquals("device", loginResponse.getScope());

        parseLoginResponse(marketplaceTestUser, loginResponse);

        otpApi.sendDestinationToOTP(marketplaceTestUser, 204);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(marketplaceTestUser.getUserId());
        assertNotNull(otpCO);

        String otp = otpCO.getPassword();
        otpApi.postOTPCode(marketplaceTestUser, 200, otp);

        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();

        marketplaceTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        marketplaceTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(marketplaceTestUser);

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(marketplaceTestUser.getUserId())
                .password(marketplaceTestUser.getUserPassword())
                .phoneNumber(marketplaceTestUser.getUserTelephone())
                .build();

        String signedSignature = alphaKeyService.generateJwsSignature(marketplaceTestUser, ob.writeValueAsString(request));

        this.certificateProtectedApi.validateCertificate(marketplaceTestUser,
                marketplaceTestUser.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2.loginUser(marketplaceTestUser, request,
                marketplaceTestUser.getDeviceId(), false);
        assertEquals(CUSTOMER_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(marketplaceTestUser, userLoginResponseV2);

        WHEN("They go through the re register device flow");

        THEN("When they log in they have a scope of customers");
        final UserLoginRequestV2 request2 = UserLoginRequestV2.builder()
                .userId(marketplaceTestUser.getUserId())
                .password(marketplaceTestUser.getUserPassword())
                .build();

        final String signedSignature2 =
                alphaKeyService.generateJwsSignature(marketplaceTestUser,
                        ob.writeValueAsString(request2),
                        marketplaceTestUser.getPreviousPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(marketplaceTestUser, marketplaceTestUser.getPreviousDeviceId(),
                ob.writeValueAsString(request2), signedSignature2, 204);

        ValidatableResponse error = authenticateApiV2.loginUserValidatable(marketplaceTestUser, request2,
                marketplaceTestUser.getPreviousDeviceId(), false);
        error.statusCode(401).assertThat();

        AND("Their old device is disabled");
        OBErrorResponse1 errorResponse = error.extract().as(OBErrorResponse1.class);
        assertEquals(UAE_ERROR_UNAUTHORIZED, errorResponse.getCode());
        assertEquals("Device is disabled", errorResponse.getMessage());
        DONE();
    }

    @Test
    public void negative_test_banking_user_pin_could_not_be_validated() throws Throwable {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-9233: Card Pin Validation");
        TEST("AHBDB-11918: AC5, AC6 - Unable to validate card pin");
        setupTestUser(true);
        GIVEN("A customer has proceeded through the re register device flow");
        AND("Has a scope of accounts limited");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("1234", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);

        assertEquals("device", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);


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
        String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(userLoginRequestV2));

        this.certificateApi.uploadCertificate(alphaTestUser);

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(),
                ob.writeValueAsString(userLoginRequestV2), signedSignature, 204);

        UserLoginResponseV2 userLoginResponseV2 =
                authenticateApiV2.loginUser(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), false);

        Assertions.assertEquals(ACCOUNTS_LIMITED_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        WHEN("They make 1-4 wrong attempts to validate their card pin");
        OBErrorResponse1 error1 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error1);

        OBErrorResponse1 error2 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error2);

        OBErrorResponse1 error3 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error3);

        OBErrorResponse1 error4 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error4);
        THEN("The platform will respond with a 403 Response");
        DONE();

//        AC6
        GIVEN("A customer has made 4 incorrect attempts to validate card pin");
        AND("The customer has a scope of accounts limited");
        OBErrorResponse1 error5 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error5);
        WHEN("The client makes a 5th incorrect attempt to validate their card pin");
        THEN("The platform will return a 403 Response");
        AND("The customer will be locked out of making further attempts");
        OBErrorResponse1 error6 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 423);
        assertEquals(LOCKED_CARD_CODE, error6.getCode());
        assertEquals(LOCKED_CARD_MESSAGE, error6.getMessage());
        AND("The customer's card will be locked");
        DONE();
    }

    @Test
    public void positive_test_banking_user_makes_4_incorrect_attempts_and_succeeds_on_the_5th_one() throws Throwable {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-9233: Card Pin Validation");
        TEST("AHBDB-11919: Positive Test - Validate card pin on the 5th and final attempt");
        setupTestUser(true);
        GIVEN("A customer has proceeded through the re register device flow");
        AND("Has a scope of accounts limited");
        AND("They make 1-4 wrong attempts to validate their card pin");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        String pinBlock = tripleDesUtil.encryptUserPin("1234", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);

        assertEquals("device", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);


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
        String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(userLoginRequestV2));

        this.certificateApi.uploadCertificate(alphaTestUser);

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(),
                ob.writeValueAsString(userLoginRequestV2), signedSignature, 204);

        UserLoginResponseV2 userLoginResponseV2 =
                authenticateApiV2.loginUser(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), false);

        Assertions.assertEquals(ACCOUNTS_LIMITED_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);
        WHEN("They make 1-4 wrong attempts to validate their card pin");
        OBErrorResponse1 error1 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error1);

        OBErrorResponse1 error2 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error2);

        OBErrorResponse1 error3 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error3);

        NOTE("Validating card pin incorrectly for the 4th time");
        OBErrorResponse1 error4 =
                authenticateApiV2.cardPinValidationError(alphaTestUser, validatePinRequest, cardId, signedSignature, 403);
        assertNotNull(error4);
        WHEN("They make a correct attempt on the 5th one");
        String validPinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        CardPinValidationRequest validatePinRequest2 = CardPinValidationRequest.builder().pin(validPinBlock).build();

        String signedSignature2 = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(validatePinRequest2));

        this.certificateApi
                .validateCertificate(alphaTestUser, ob.writeValueAsString(validatePinRequest2), signedSignature2, 204);

        NOTE("Validating card pin correctly for the last time possible");
        authenticateApiV2.cardPinValidation(alphaTestUser, validatePinRequest2, cardId, signedSignature2);
        THEN("The platform will return a 200");
        AND("When they log back in they have the scope of 'accounts customer'");

        UserLoginResponseV2 loginResponseV22 = authenticateApiV2.loginUser(alphaTestUser);

        Assertions.assertEquals(ACCOUNT_SCOPE, loginResponseV22.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);
        DONE();
    }

    @Test
    public void negative_test_validate_with_wrong_otp_400_response() throws Throwable {
        TEST("AHBDB-7463: Re-Register Device - Banking User (Adult)");
        TEST("AHBDB-11930: Negative Test - Validate with wrong OTP");
        setupTestUser(true);
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        GIVEN("The customer has initiated the register new device flow");
        WHEN("The customer enters the wrong OTP");
        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);

        assertEquals("device", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);

        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);

        THEN("Then the platform will respond with a 400 Response");
        otpApi.postOTPCodeError(alphaTestUser, 400, "123456");
        DONE();
    }
}
