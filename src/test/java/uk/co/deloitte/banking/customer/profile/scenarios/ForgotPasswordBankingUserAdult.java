package uk.co.deloitte.banking.customer.profile.scenarios;

import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBAccount4Account;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6Data;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
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
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils.parseLoginResponse;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ForgotPasswordBankingUserAdult {

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private AccountApi accountApi;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    TripleDesUtil tripleDesUtil;

    private static final String REQUEST_VALIDATION = "REQUEST_VALIDATION";
    private static final String BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";
    private static final String LOCKED_CARD_CODE = "UAE.ERROR.LOCKED";
    private static final String LOCKED_CARD_MESSAGE = "Card locked due to exceeded pin validation attempts";

    protected ObjectMapper ob = new ObjectMapper();

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserMarketplace;
    private AlphaTestUser alphaTestUserFresh;

    public void setupTestUser() throws Throwable {
        alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);

        setupCardsForUser(alphaTestUser);

        NOTE("SETUP NOW FINISHED");
    }

    public void setupTestUserFresh() throws Throwable {
        alphaTestUserFresh = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserFresh);

        setupCardsForUser(alphaTestUserFresh);
        NOTE("SETUP NOW FINISHED");
    }

    public void setupTestUserMarketplace() {
        alphaTestUserMarketplace = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
    }

    //    AC3
    @Order(2)
    @Test
    public void happy_path_marketplace_user_generate_and_send_otp_and_store_new_password() {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11746: AC3- Generate, send and validate OTP for marketplace user + store new password for " +
                "marketplace user");
        setupTestUserMarketplace();
        GIVEN("A customer has successfully verified their OTP");
        AND("The customer is a marketplace user");
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUserMarketplace.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword =
                this.authenticateApiV2.initiateResetPassword(alphaTestUserMarketplace, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());

        assertEquals(alphaTestUserMarketplace.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUserMarketplace.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUserMarketplace, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse.getHash());

        WHEN("The client attempts to update the user with a new valid password and hash");
        String newPassword = UUID.randomUUID().toString();

        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequestV1 = UpdateForgottenPasswordRequestV1.builder()
                .hash(validateResetPasswordResponse.getHash())
                .userPassword(newPassword)
                .build();

        UserDto userDto = authenticateApiV2.updateForgottenPassword(alphaTestUserMarketplace,
                updateForgottenPasswordRequestV1);
        assertNotNull(userDto);

        alphaTestUserMarketplace.setUserPassword(newPassword);
        THEN("The platform will store the password and return a 200 Response");
        AND("The platform will unlock the user in case the user was locked");
        UserLoginResponseV2 loginResponseV2 = authenticateApiV2.loginUser(alphaTestUserMarketplace);
        assertNotNull(loginResponseV2);
        assertEquals("customer", loginResponseV2.getScope());
        DONE();
    }

    @Order(5)
    @Test
    public void negative_test_marketplace_store_invalid_new_password_400_response() {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11754: Negative Test - Marketplace user chooses an invalid new password");
        GIVEN("A customer has successfully verified their OTP");
        AND("The customer is a marketplace user");
        AlphaTestUser alphaTestUser0 = new AlphaTestUser();
        alphaTestUser0 = this.alphaTestUserFactory.setupCustomer(alphaTestUser0);
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser0.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword =
                this.authenticateApiV2.initiateResetPassword(alphaTestUser0, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());

        assertEquals(alphaTestUser0.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUser0.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUser0, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse.getHash());

        WHEN("The client attempts to update the user with a password");
        AND("The password they choose is invalid");

        UpdateForgottenPasswordRequestV1 invalidRequest1 = UpdateForgottenPasswordRequestV1.builder()
                .hash(validateResetPasswordResponse.getHash())
                .userPassword("")
                .build();

        OBErrorResponse1 error1 =
                authenticateApiV2.updateForgottenPasswordError(alphaTestUser0, invalidRequest1, 400);
        assertNotNull(error1);
        assertEquals(REQUEST_VALIDATION, error1.getCode());

        UpdateForgottenPasswordRequestV1 invalidRequest2 = UpdateForgottenPasswordRequestV1.builder()
                .hash(validateResetPasswordResponse.getHash())
                .userPassword("1234567")
                .build();

        OBErrorResponse1 error2 =
                authenticateApiV2.updateForgottenPasswordError(alphaTestUser0, invalidRequest2, 400);
        assertNotNull(error2);
        assertEquals(REQUEST_VALIDATION, error2.getCode());
        THEN("The platform will return a 400 Response");

        UpdateForgottenPasswordRequestV1 invalidRequest3 = UpdateForgottenPasswordRequestV1.builder()
                .hash(validateResetPasswordResponse.getHash())
                .userPassword(null)
                .build();

        OBErrorResponse1 error3 =
                authenticateApiV2.updateForgottenPasswordError(alphaTestUser0, invalidRequest3, 400);
        assertNotNull(error3);
        assertEquals(REQUEST_VALIDATION, error3.getCode());
        DONE();
    }

    //    AC1, AC2, AC4, AC6
    @Order(2)
    @Test
    public void happy_path_banking_user_forgot_password_flow_200_response() throws Throwable {
        TEST("AHBDB-3329: AC6 Validation of OTP - Bank User - 501 not implemented");
        TEST("AHBDB-4000: AC6 Negative Test - Validation of OTP - Bank user - 501 Not implemented");
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11619: AC1, AC2, AC4, AC6 - Generate, send and validate OTP + validate card pin for banking user");
        setupTestUser();

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876",
                tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        GIVEN("A customer is a banking user");
        WHEN("The client attempts to generate an OTP for the customer in order to validate them and reset their " +
                "password");
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(alphaTestUser, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());
        assertEquals(alphaTestUser.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUser.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        THEN("The platform will generate the OTP");
        AND("The customer will receive an OTP in an SMS to their phone number");
        AND("The platform will return a 200 OK to the customer");
        DONE();

//        AC2
        GIVEN("The customer has entered the OTP on their phone");
        WHEN("The client attempts to validate the correct OTP against the customer's phone number");
        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUser, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse);
        THEN("The platform will return a 200 Response");
        AND("The platform will return a hash to reset the customer's password");
        String newHash = validateResetPasswordResponse.getHash();
        DONE();

//        AC4
        GIVEN("The customer has successfully validated their OTP");
        AND("The customer is a banking user");
        String newPassword = UUID.randomUUID().toString();
        alphaTestUser.setUserPassword(newPassword);
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(newHash)
                .userPassword(newPassword)
                .build();

        WHEN("The client attempts to update the user with a new valid password and hash");
        UserDto userDto = this.authenticateApiV2.updateForgottenPassword(alphaTestUser, updateForgottenPasswordRequest);
        assertNotNull(userDto);

        UserLoginResponseV2 loginResponse = authenticateApiV2.loginUser(alphaTestUser);
        assertNotNull(loginResponse);

        parseLoginResponse(alphaTestUser, loginResponse);
        THEN("The platform will set the device status to 'Pending card validations'");
        assertEquals(alphaTestUser.getUserId(), loginResponse.getUserId());
        AND("The platform will set the scope of the account to 'Account Limited'");
        assertEquals("accounts-limited", loginResponse.getScope());
        AND("The platform will store the password and return a 200 Response");
        DONE();

//        AC6
        GIVEN("A customer is a banking user");
        AND("The customer has successfully validated their phone number using an OTP");
        AND("The customer has stored their desired new passcode");
        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser,
                ob.writeValueAsString(validatePinRequest));

        this.certificateApi
                .validateCertificate(alphaTestUser, ob.writeValueAsString(validatePinRequest), signedSignature, 204);

        WHEN("The client attempts to validate the correct card PIN against the customer's cardID with the customer's " +
                "desired password");
        authenticateApiV2.cardPinValidation(alphaTestUser, validatePinRequest, cardId, signedSignature);

        UserLoginResponseV2 loginAsAccounts = authenticateApiV2.loginUser(alphaTestUser);

        THEN("The platform will return a 200 Success");
        AND("The platform will return a token with the scope of accounts");
        AND("The customer's account will be unlocked if it was locked");
        assertNotNull(loginAsAccounts);
        assertNotNull(loginAsAccounts.getAccessToken());
        assertEquals("accounts customer", loginAsAccounts.getScope());
        assertEquals(alphaTestUser.getUserId(), loginAsAccounts.getUserId());
        DONE();
    }

    //    Invalid numbers: "", "!@£$%%^"
    @Order(2)
    @Test
    public void negative_test_initiate_reset_password_with_invalid_numbers() throws Throwable {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11751: Negative Test - Invalid Mobile phone number");
        setupTestUser();
        GIVEN("A customer attempts to generate an OTP to initiate the forgot password flow");
        WHEN("They enter their details");
        List errorList = new ArrayList();
        errorList.add(REQUEST_VALIDATION);
        errorList.add(BAD_REQUEST);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber("")
                .build();

        ResetPasswordRequest request2 = ResetPasswordRequest.builder()
                .phoneNumber("!@£$%%^")
                .build();

        OBErrorResponse1 error =
                this.authenticateApiV2.initiateResetPasswordError(alphaTestUser, request, 400);
        assertNotNull(error);
        assertTrue(errorList.contains(error.getCode()));

        OBErrorResponse1 error2 =
                this.authenticateApiV2.initiateResetPasswordError(alphaTestUser, request2, 400);
        assertNotNull(error2);
        assertTrue(errorList.contains(error2.getCode()));
        AND("They use an invalid mobile number");
        THEN("The platform will return a 400 Response");
        DONE();
    }

    @Order(2)
    @Test
    public void negative_test_validate_using_wrong_OTP_400_response() throws Throwable {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11752: Negative Test - Validate with the wrong OTP");
        setupTestUser();
        GIVEN("The customer has received the OTP they generated");
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(alphaTestUser, request);
        assertNotNull(resetPassword.getHash());
        WHEN("The client attempts to validate the wrong OTP against the customer's phone number");
        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp("wrongOTP")
                .build();

        OBErrorResponse1 validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtpError(alphaTestUser, validateResetPasswordRequest, 400);
        assertNotNull(validateResetPasswordResponse);
        THEN("The platform will return a 400 Response");
        DONE();
    }

    @Order(2)
    @Test
    public void negative_test_update_customer_password_using_wrong_hash_400_response() throws Throwable {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11753: Negative Test - Update customer details with wrong hash");
        setupTestUser();
        GIVEN("The customer has validated the OTP they generated");
        AND("Is now ready to change their password");
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(alphaTestUser, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());
        assertEquals(alphaTestUser.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUser.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();

        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUser, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse);
        WHEN("The client attempts to change their password using the wrong generated hash");
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash("wronghash")
                .userPassword(UUID.randomUUID().toString())
                .build();

        WHEN("The client attempts to update the user with a new valid password and hash");
        OBErrorResponse1 error =
                this.authenticateApiV2.updateForgottenPasswordError(alphaTestUser, updateForgottenPasswordRequest, 400);
        assertNotNull(error);
        THEN("The platform will return a 400 Response");
        DONE();
    }

    //    Invalid pass: "", "1234567"
    @Test
    public void negative_test_update_customer_password_using_invalid_password_400_response() throws Throwable {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11754: Negative Test - Update customer details with an invalid password");
        setupTestUser();
        GIVEN("The customer has validated the OTP they generated");
        AND("Is now ready to change their password");
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(alphaTestUser, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());
        assertEquals(alphaTestUser.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUser.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();

        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUser, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse);
        String newHash = validateResetPasswordRequest.getHash();
        WHEN("The client attempts to change their password with an invalid one");
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(newHash)
                .userPassword("")
                .build();

        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest2 = UpdateForgottenPasswordRequestV1.builder()
                .hash(newHash)
                .userPassword("1234567")
                .build();

        WHEN("The client attempts to update the user with a new valid password and hash");
        OBErrorResponse1 error =
                this.authenticateApiV2.updateForgottenPasswordError(alphaTestUser, updateForgottenPasswordRequest, 400);
        assertNotNull(error);
        assertEquals(REQUEST_VALIDATION, error.getCode());

        OBErrorResponse1 error2 =
                this.authenticateApiV2.updateForgottenPasswordError(alphaTestUser, updateForgottenPasswordRequest2,
                        400);
        assertNotNull(error2);
        assertEquals(REQUEST_VALIDATION, error2.getCode());
        THEN("The platform will return a 400 Response");
        DONE();
    }

    @Test
    public void negative_test_update_customer_password_with_null_value_400_response() throws Throwable {

        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11754: Negative Test - Update customer details with an invalid password");
        setupTestUser();
        GIVEN("The customer has validated the OTP they generated");
        AND("Is now ready to change their password");
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(alphaTestUser, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());
        assertEquals(alphaTestUser.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUser.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();

        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUser, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse);
        String newHash = validateResetPasswordRequest.getHash();
        WHEN("The client attempts to change their password with an invalid one");
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(newHash)
                .userPassword(null)
                .build();

        WHEN("The client attempts to update the user with a new valid password and hash");
        OBErrorResponse1 error =
                this.authenticateApiV2.updateForgottenPasswordError(alphaTestUser, updateForgottenPasswordRequest, 400);
        assertNotNull(error);
        assertEquals(REQUEST_VALIDATION, error.getCode());
        THEN("The platform will return a 400 Response");
        DONE();
    }

    //    AC9, AC10
    @Test
    @Order(99)
    public void negative_test_card_pin_could_not_be_validated() throws Throwable {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11750: AC9, AC10 - Card pin could not be validated (1-4 attempts) returns 400 - (5th attempt) " +
                "returns 403");
        setupTestUserFresh();
        GIVEN("A customer is a banking user");
        AND("They have successfully validated their phone number using an OTP");

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUserFresh);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUserFresh, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUserFresh.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(alphaTestUserFresh, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());
        assertEquals(alphaTestUserFresh.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUserFresh.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUserFresh, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse);

        String newHash = validateResetPasswordResponse.getHash();

        String newPassword = UUID.randomUUID().toString();
        alphaTestUserFresh.setUserPassword(newPassword);
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(newHash)
                .userPassword(newPassword)
                .build();

        UserDto userDto = this.authenticateApiV2.updateForgottenPassword(alphaTestUserFresh,
                updateForgottenPasswordRequest);
        assertNotNull(userDto);

        UserLoginResponseV2 loginResponse = authenticateApiV2.loginUser(alphaTestUserFresh);
        assertNotNull(loginResponse);

        parseLoginResponse(alphaTestUserFresh, loginResponse);
        assertEquals(alphaTestUserFresh.getUserId(), loginResponse.getUserId());
        assertEquals("accounts-limited", loginResponse.getScope());
        NOTE("THE USER NOW HAS A SCOPE OF ACCOUNTS LIMITED");

        String invalidPinBlock = tripleDesUtil.encryptUserPin("1234",
                tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(invalidPinBlock).build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUserFresh,
                ob.writeValueAsString(validatePinRequest));

        this.certificateApi
                .validateCertificate(alphaTestUserFresh, ob.writeValueAsString(validatePinRequest), signedSignature,
                        204);

        WHEN("They attempt to validate their pin");
        AND("They make less than 5 wrong attempts");
        THEN("The platform will return a 403 Response");
        OBErrorResponse1 error1 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest, cardId,
                        signedSignature, 403);
        assertNotNull(error1);

        OBErrorResponse1 error2 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest, cardId,
                        signedSignature, 403);
        assertNotNull(error2);

        OBErrorResponse1 error3 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest, cardId,
                        signedSignature, 403);
        assertNotNull(error3);

        OBErrorResponse1 error4 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest, cardId,
                        signedSignature, 403);
        assertNotNull(error4);
        DONE();

        ReadCard1 readCard0 = this.cardsApiFlows.fetchCardsForUser(alphaTestUserFresh);
        assertEquals(cardNumber, readCard0.getData().getReadCard1DataCard().get(0).getCardNumber());

//        AC10
        GIVEN("The customer has tried validate their pin 4 times already");
        WHEN("They try to validate for the 5th time");

        NOTE("Validating PIN for the 5th time");
        OBErrorResponse1 error5 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest, cardId,
                        signedSignature, 403);
        THEN("The platform will return a 403 Response");
        assertNotNull(error5);
        ReadCard1 readCard1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUserFresh);
        assertEquals(cardNumber, readCard1.getData().getReadCard1DataCard().get(0).getCardNumber());

        AND("The customer will be locked out of making further attempts");
        OBErrorResponse1 error6 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest, cardId,
                        signedSignature, 423);
        assertNotNull(error6);
        AND("The customer's card will be blocked");
        assertEquals(LOCKED_CARD_CODE, error6.getCode());
        assertEquals(LOCKED_CARD_MESSAGE, error6.getMessage());
        DONE();
    }

    //    AC8
    @Order(1)
    @Test
    public void negative_test_card_pin_invalid_parameter_pin_400_response() throws Throwable {
        TEST("AHBDB-1594: Forgot Password - Banking User (Adult)");
        TEST("AHBDB-11750: AC9, AC10 - Card pin could not be validated (1-4 attempts) returns 400 - (5th attempt) " +
                "returns 403");
        setupTestUserFresh();
        GIVEN("A customer is a banking user");
        AND("They have successfully validated their phone number using an OTP");

        ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUserFresh);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUserFresh, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(alphaTestUserFresh.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(alphaTestUserFresh, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());
        assertEquals(alphaTestUserFresh.getUserTelephone(), otpCO.getDestination());
        assertEquals(alphaTestUserFresh.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(alphaTestUserFresh, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse);

        String newHash = validateResetPasswordResponse.getHash();

        String newPassword = UUID.randomUUID().toString();
        alphaTestUserFresh.setUserPassword(newPassword);
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(newHash)
                .userPassword(newPassword)
                .build();

        UserDto userDto = this.authenticateApiV2.updateForgottenPassword(alphaTestUserFresh,
                updateForgottenPasswordRequest);
        assertNotNull(userDto);

        UserLoginResponseV2 loginResponse = authenticateApiV2.loginUser(alphaTestUserFresh);
        assertNotNull(loginResponse);

        parseLoginResponse(alphaTestUserFresh, loginResponse);
        assertEquals(alphaTestUserFresh.getUserId(), loginResponse.getUserId());
        assertEquals("accounts-limited", loginResponse.getScope());
        NOTE("THE USER NOW HAS A SCOPE OF ACCOUNTS LIMITED");

//        Tests empty value
        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin("").build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUserFresh,
                ob.writeValueAsString(validatePinRequest));
        this.certificateApi.validateCertificate(alphaTestUserFresh, ob.writeValueAsString(validatePinRequest),
                signedSignature, 204);

        OBErrorResponse1 error1 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest, cardId,
                        signedSignature, 400);
        assertNotNull(error1);
        assertEquals(REQUEST_VALIDATION, error1.getCode());

//        Tests null value
        CardPinValidationRequest validatePinRequest2 = CardPinValidationRequest.builder().pin(null).build();

        String signedSignature2 = alphaKeyService.generateJwsSignature(alphaTestUserFresh,
                ob.writeValueAsString(validatePinRequest2));
        this.certificateApi.validateCertificate(alphaTestUserFresh, ob.writeValueAsString(validatePinRequest2),
                signedSignature2, 204);

        OBErrorResponse1 error2 =
                authenticateApiV2.cardPinValidationError(alphaTestUserFresh, validatePinRequest2, cardId,
                        signedSignature2, 400);
        assertNotNull(error2);
        assertEquals(REQUEST_VALIDATION, error2.getCode());
        DONE();
    }

    //    AC7
    @Order(20)
    @Test
    public void negative_test_marketplace_user_tries_to_validate_card_pin() throws Throwable {
        TEST("AHBDB-1594: Forgot Password - Banking user (Adult)");
        TEST("AHBDB-11748: AC7 - Validate Card PIN - Marketplace customer - 403 Bad request");
        setupTestUserMarketplace();
        GIVEN("A customer is a marketplace user");
        WHEN("They attempt to validate a card pin");
        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(
                "4714840000009904"));

        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUserMarketplace,
                ob.writeValueAsString(validatePinRequest));

        this.certificateApi
                .validateCertificate(alphaTestUserMarketplace, ob.writeValueAsString(validatePinRequest),
                        signedSignature, 204);
        authenticateApiV2.cardPinValidationErrorVoid(alphaTestUserMarketplace, validatePinRequest, "4714849904",
                signedSignature, 403);
        THEN("The platform will return a 403 Response");
        DONE();
    }

    @Test
    public void negative_test_another_customer_attempts_to_validate_another_customers_card() throws Throwable {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUserFresh();

        AlphaTestUser newUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        alphaTestUserBankingCustomerFactory.setUpAccount(newUser);
        setupCardsForUser(newUser);

        GIVEN("A customer has initiated the forgot password flow");
        AND("They have the scope of accounts-limited");

//        We retrieve the cards for one user
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUserFresh);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUserFresh, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876",
                tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .phoneNumber(newUser.getUserTelephone())
                .build();

        ResetPasswordResponse resetPassword = this.authenticateApiV2.initiateResetPassword(newUser, request);
        assertNotNull(resetPassword.getHash());

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPassword.getHash());
        assertNotNull(otpCO.getPassword());
        assertEquals(newUser.getUserTelephone(), otpCO.getDestination());
        assertEquals(newUser.getUserId(), otpCO.getUserId());
        assertEquals(OtpType.FORGOTTEN, otpCO.getType());

        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPassword.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApiV2.validateResetPasswordOtp(newUser, validateResetPasswordRequest);
        assertNotNull(validateResetPasswordResponse);
        String newHash = validateResetPasswordResponse.getHash();

        String newPassword = UUID.randomUUID().toString();
        newUser.setUserPassword(newPassword);
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(newHash)
                .userPassword(newPassword)
                .build();

        UserDto userDto = this.authenticateApiV2.updateForgottenPassword(newUser, updateForgottenPasswordRequest);
        assertNotNull(userDto);

        UserLoginResponseV2 loginResponse = authenticateApiV2.loginUser(newUser);
        assertNotNull(loginResponse);

        parseLoginResponse(newUser, loginResponse);
        assertEquals(newUser.getUserId(), loginResponse.getUserId());
        assertEquals("accounts-limited", loginResponse.getScope());
        DONE();
        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        String signedSignature = alphaKeyService.generateJwsSignature(newUser,
                ob.writeValueAsString(validatePinRequest));

        certificateApi.validateCertificate(newUser,
                ob.writeValueAsString(validatePinRequest), signedSignature, 204);

        WHEN("They attempt to validate their pin using another user's card");

        authenticateApiV2.cardPinValidationError(newUser, validatePinRequest, cardId, signedSignature, 403);

        THEN("The platform will not allow them to do so");

        DONE();
    }

    private void setupCardsForUser(AlphaTestUser alphaTestUserSetup) throws Throwable {
        CreateCard1 createCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUserSetup.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(alphaTestUserSetup.getAccountNumber())
                                .accountType(CardsApiFlows.ACCOUNT_TYPE)
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30",
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();

        final CreateCard1Response createCardResponse = cardsApiFlows.createVirtualDebitCard(alphaTestUserSetup,
                createCard1);

        String createdCardNumber = createCardResponse.getData().getCardNumber();
        ActivateCard1 validActivateCard1 = ActivateCard1.builder()
                .cardNumber(createdCardNumber)
                .lastFourDigits(createdCardNumber.substring(createdCardNumber.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(createCardResponse.getData().getExpiryDate())
                .modificationOperation(ModificationOperation.V)
                .operationReason("Operation reason")
                .build();

        cardsApiFlows.activateDebitCard(alphaTestUserSetup, validActivateCard1, 200);
//            VIRTUAL CARD IS NOW ACTIVATED AT THIS POINT
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUserSetup);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUserSetup, cardId);
        Assertions.assertTrue(!Objects.isNull(cardCVvDetails.getData()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getExpiryDate()));

//            ORDER A PHYSICAL CARD
        OBReadAccount6 response = this.accountApi.getAccountsV2(alphaTestUserSetup);
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
                .phoneNumber(alphaTestUserSetup.getUserTelephone())
                .recipientName(alphaTestUserSetup.getName())
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

        cardsApiFlows.issuePhysicalCard(alphaTestUserSetup, cardId, issuePhysicalCard);

        final ActivateCard1 activateCard1 = ActivateCard1.builder()
                .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                .cardNumberFlag("P")
                .cardNumber(plainCardNumber)
                .lastFourDigits(StringUtils.right(cardNumber, 4))
                .modificationOperation(ModificationOperation.A)
                .operationReason("TEST : activate card")
                .build();
        cardsApiFlows.activateDebitCard(alphaTestUserSetup, activateCard1, 200);

//            PHYSICAL CARD IS NOW ACTIVATED

//            SET CARD PIN AND VALIDATE
        final String pinBlock = tripleDesUtil.encryptUserPin("9876",
                tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        final WriteCardPinRequest1 pinSetRequest = WriteCardPinRequest1.builder()
                .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                .cardNumber(cardNumber)
                .cardNumberFlag("M")
                .lastFourDigits(StringUtils.right(cardNumber, 4))
                .pinServiceType("C")
                .pinBlock(pinBlock)
                .build();
        cardsApiFlows.setDebitCardPin(alphaTestUserSetup, pinSetRequest, 200);

        final CardPinValidation1 cardPinValidation1 = CardPinValidation1.builder()
                .cardPinValidation1Data(CardPinValidation1Data.builder()
                        .pin(pinBlock)
                        .build())
                .build();
        cardsApiFlows.validateDebitCardPin(alphaTestUserSetup, cardId, cardPinValidation1);

//            PHYSICAL CARD IS NOW ACTIVE AND PIN IS VALIDATED -- SETUP COMPLETE
    }
}
