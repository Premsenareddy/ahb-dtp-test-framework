package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBAccount4Account;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6Data;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ModificationOperation;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.filters.UpdateCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes.TripleDesUtil;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class TemporaryBlockDebitCardTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private CardsConfiguration cardsConfiguration;

    @Inject
    private BankingConfig bankingConfig;

    @Inject
    TripleDesUtil tripleDesUtil;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private AccountApi accountApi;

    protected ObjectMapper ob = new ObjectMapper();


    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

    private static final String CARD_MASK = "000000";

    private static final String NULL_ERROR = "must not be null";

    private static final String CARD_NOT_FOUND = "Card not found";

    private void setupTestUser() throws Throwable {

        envUtils.shouldSkipHps(Environments.NONE);

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser,
                    validCreateCard1());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
            CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
            CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();
            this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 200);
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
            String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
            final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
            final String plainCardNumber = tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber());

            cardsApiFlows.issuePhysicalCard(alphaTestUser, cardId, issuePhysicalCard);

            final ActivateCard1 activateCard1 = ActivateCard1.builder()
                    .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                    .cardNumberFlag("P")
                    .cardNumber(plainCardNumber)
                    .lastFourDigits(StringUtils.right(CREATED_CARD_NUMBER, 4))
                    .modificationOperation(ModificationOperation.A)
                    .operationReason("TEST : activate card")
                    .build();
            cardsApiFlows.activateDebitCard(alphaTestUser, activateCard1, 200);

            //            PHYSICAL CARD IS NOW ACTIVATED
//            SET CARD PIN AND VALIDATE
            final String pinBlock = tripleDesUtil.encryptUserPin("9876",
                    tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
            final WriteCardPinRequest1 pinSetRequest = WriteCardPinRequest1.builder()
                    .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                    .cardNumber(CREATED_CARD_NUMBER)
                    .cardNumberFlag("M")
                    .lastFourDigits(StringUtils.right(CREATED_CARD_NUMBER, 4))
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

//            PHYSICAL CARD IS NOW ACTIVE AND PIN IS VALIDATED -- SETUP COMPLETE
            NOTE("SETUP NOW FINISHED");
        }
    }

    private ActivateCard1 validActivateCard1() {
        return ActivateCard1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .modificationOperation(ModificationOperation.V)
                .operationReason("Operation reason")
                .build();
    }

    private void refreshUserAccessToken() {
       // alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @BeforeEach
    void setUpTest() {
        refreshUserAccessToken();
    }

    private CreateCard1 validCreateCard1() {
        return CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUser.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(alphaTestUser.getAccountNumber())
                                .accountType(AccountType.CURRENT.getDtpValue())
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30",
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
    }

    private UpdateCardParameters1 validUpdateCardParameters1() {
        return UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .nationalUsage(false)
                .nationalPOS(false)
                .nationalDisATM(false)
                .internationalUsage(false)
                .internetUsage(false)
                .operationReason("operation reason")
                .build();
    }

    private JSONObject validUpdateCardParameters1JSON() {
        JSONObject object = new JSONObject();
        object.put("CardNumber", CREATED_CARD_NUMBER);
        object.put("CardNumberFlag", "M");
        object.put("NationalUsage", "false");
        object.put("NationalDisATM", "false");
        object.put("InternationalUsage", "false");
        object.put("InternetUsage", "false");
        object.put("OperationReason", "operationReason");

        return object;

    }

    private String changeBankingCustomerScopeToAccountsLimited() throws Throwable {

        GIVEN("Customer is banking with accounts scope");
        THEN("Change scope of customer to accounts-limited by forgot password flow");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        GIVEN("A customer is a banking user");
        WHEN("The client attempts to generate an OTP for the customer in order to validate them and reset their password");
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

        TokenUtils.parseLoginResponse(alphaTestUser, loginResponse);
        THEN("The platform will set the device status to 'Pending card validations'");
        assertEquals(alphaTestUser.getUserId(), loginResponse.getUserId());
        AND("The platform will set the scope of the account to 'Account Limited'");
        assertEquals("accounts-limited", loginResponse.getScope());
        AND("The platform will store the password and return a 200 Response");
        return pinBlock;
    }

    private void completeChangePasswordJourney(final String pinBlock, final String cardId) throws JsonProcessingException {
        GIVEN("A customer is a banking user");
        AND("The customer has successfully validated their phone number using an OTP");
        AND("The customer has stored their desired new passcode");
        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(validatePinRequest));

        this.certificateApi
                .validateCertificate(alphaTestUser, ob.writeValueAsString(validatePinRequest), signedSignature, 204);

        WHEN("The client attempts to validate the correct card PIN against the customer's cardID with the customer's desired password");
        authenticateApiV2.cardPinValidation(alphaTestUser, validatePinRequest, cardId, signedSignature);

        UserLoginResponseV2 loginAsAccounts = authenticateApiV2.loginUser(alphaTestUser);

        THEN("The platform will return a 200 Success");
        AND("The platform will return a token with the scope of accounts");
        AND("The customer's account will be unlocked if it was locked");
        assertNotNull(loginAsAccounts);
        assertNotNull(loginAsAccounts.getAccessToken());
        assertEquals("accounts customer", loginAsAccounts.getScope());
        assertEquals(alphaTestUser.getUserId(), loginAsAccounts.getUserId());
        TokenUtils.parseLoginResponse(alphaTestUser, loginAsAccounts);
    }

    @Order(1)
    @Test()
    public void positive_test_user_blocks_their_card() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to block debit card");

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        THEN("A 200 is returned from the service");

        THEN("change customer scope to account-limited ");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCard(alphaTestUser, validUpdateCardParameters1JSON(), cardId, 200);
        THEN("change customer scope to accounts");
        completeChangePasswordJourney(pinBlock, cardId);


        AND("The users card is shown as blocked");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        AND("The user can unblock their card");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("their card is unblocked");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");
        DONE();
    }

    @Order(2)
    @Test()
    public void positive_test_user_blocks_card_null_operationReason() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null operationReason");
        UpdateCardParameters1 updateCardParameters1 = validUpdateCardParameters1();
        updateCardParameters1.setOperationReason(null);
        updateCardParameters1.setNationalUsage(true);

        THEN("A 200 is returned from the service");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        THEN("change customer scope to account-limited ");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCardLimitedApi(alphaTestUser, cardId, 200);
        THEN("change customer scope to accounts");
        completeChangePasswordJourney(pinBlock, cardId);

        AND("The users card is shown as blocked");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        DONE();
    }

    @Order(3)
    @Test()
    public void positive_test_user_blocks_card_null_nationalUsage() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null nationalUsage");
        JSONObject updateCardParameters1 = validUpdateCardParameters1JSON();
        updateCardParameters1.remove("NationalUsage");
        updateCardParameters1.put("InternetUsage", "true");

        THEN("A 200 is returned from the service");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        THEN("change customer scope to account-limited ");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCard(alphaTestUser, updateCardParameters1, cardId, 200);
        THEN("change customer scope to accounts");
        completeChangePasswordJourney(pinBlock, cardId);

        AND("The users card is shown as blocked");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        DONE();
    }

    @Order(4)
    @Test()
    public void positive_test_user_blocks_card_null_nationalDisATM() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null nationalDisATM");
        JSONObject updateCardParameters1 = validUpdateCardParameters1JSON();
        updateCardParameters1.remove("NationalDisATM");
        updateCardParameters1.put("InternationalUsage", "true");

        THEN("A 200 is returned from the service");
        THEN("change customer scope to account-limited ");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCard(alphaTestUser, updateCardParameters1, cardId, 200);
        THEN("change customer scope to accounts");
        completeChangePasswordJourney(pinBlock, cardId);


        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        DONE();
    }

    @Order(5)
    @Test()
    public void positive_test_user_blocks_card_null_internationalUsage() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null internationalUsage");
        JSONObject updateCardParameters1 = validUpdateCardParameters1JSON();
        updateCardParameters1.remove("InternationalUsage");

        THEN("A 200 is returned from the service");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCard(alphaTestUser, updateCardParameters1, cardId, 200);
        THEN("change customer scope to accounts");
        completeChangePasswordJourney(pinBlock, cardId);

        DONE();
    }

    @Order(6)
    @Test()
    public void positive_test_user_blocks_card_null_internetUsage() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null internetUsage");
        JSONObject updateCardParameters1 = validUpdateCardParameters1JSON();
        updateCardParameters1.remove("InternetUsage");

        THEN("A 200 is returned from the service");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCard(alphaTestUser, updateCardParameters1, cardId, 200);
        THEN("change customer scope to accounts");
        completeChangePasswordJourney(pinBlock, cardId);

        DONE();
    }

    @Order(7)
    @Test()
    public void negative_test_user_blocks_card_null_cardNumberFlag() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an invalid request to block debit card with null cardNumberFlag");

        THEN("A 400 is returned from the service");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCardLimitedApi(alphaTestUser, null, 400);
        completeChangePasswordJourney(pinBlock, cardId);

        DONE();
    }

    @Order(8)
    @Test()
    public void negative_test_user_blocks_card_null_cardNumber() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null cardNumber");

        THEN("A 400 is returned from the service");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCardLimitedApi(alphaTestUser, null, 400);
        completeChangePasswordJourney(pinBlock, cardId);

        DONE();
    }

    @Order(9)
    @Test()
    public void negative_test_user_blocks_card_with_another_users_cardNumber() throws Throwable {
        TEST("AHBDB-7506 defect fix");
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null cardNumber");

        THEN("A 404 is returned from the service");
        String cardId = cardsConfiguration.getCreatedCard().replace(CARD_MASK, "");
        final String pinBlock = changeBankingCustomerScopeToAccountsLimited();
        cardsApiFlows.blockCardLimitedApi(alphaTestUser, cardId, 404);

        DONE();
    }


    @Order(101)
    @Test()
    public void negative_test_user_tries_to_block_card_invalid_token() throws Throwable {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        AND("Their token is set as invalid");
        alphaTestUser.getLoginResponse().setAccessToken("invalid");
        AND("They make a invalid request to block debit card with incorrect scope");

        THEN("They receive a 401 response back from the service");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        cardsApiFlows.blockCardLimitedApi(alphaTestUser, cardId, 401);

        DONE();
    }

}
