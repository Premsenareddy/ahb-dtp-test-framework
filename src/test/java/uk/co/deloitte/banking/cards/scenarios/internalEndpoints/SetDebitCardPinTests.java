package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ModificationOperation;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
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
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomTownName;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class SetDebitCardPinTests {

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
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    TripleDesUtil tripleDesUtil;

    private AlphaTestUser alphaTestUser;

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

    private static final String NULL_ERROR = "must not be null";

    private static final String CARD_NOT_FOUND = "Card not found";

    private static final String CARD_MASK = "000000";

    private void setupTestUser() {
        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
            CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
            CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();

            createPhysicalCard();

            this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 200);
        }
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    private void createPhysicalCard() {
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();
        String iban = this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 201);

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
                                .openDate(LocalDateTime.now())
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
    }

    private WriteCardPinRequest1 validWriteCardPinRequest1(String pinBlock) {
        return  WriteCardPinRequest1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .pinBlock(pinBlock)
                .pinServiceType("G")
                .build();
    }

    private ActivateCard1 validActivateCard1() {
        return ActivateCard1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .modificationOperation(ModificationOperation.A)
                .operationReason("Operation reason")
                .build();
    }

    private WritePhysicalCard1 validPhysicalCard1(CardDeliveryAddress1 deliveryAddress, String iban) {
        return WritePhysicalCard1.builder()
                .recipientName(alphaTestUser.getName())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .deliveryAddress(deliveryAddress)
                .dtpReference("dtpReference")
                .iban(iban)
                .awbRef("DT"  + generateRandomNumeric(14))
                .build();
    }

    private CardDeliveryAddress1 cardDeliveryAddress1Valid(OBPostalAddress6 obPostalAddress6) {
        return   CardDeliveryAddress1.builder()
                .addressLine(List.of(obPostalAddress6.getAddressLine().get(0), obPostalAddress6.getAddressLine().get(1)))
                .buildingNumber(obPostalAddress6.getBuildingNumber())
                .townName(generateRandomTownName())
                .countrySubDivision(obPostalAddress6.getCountrySubDivision())
                .country(obPostalAddress6.getCountry())
                .postalCode(obPostalAddress6.getPostalCode())
                .streetName(obPostalAddress6.getStreetName())
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_user_sets_and_updates_pin() throws Throwable {

        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");
        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin");
        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.setDebitCardPin(alphaTestUser, validWriteCardPinRequest1(pinBlock), 200);

        AND("My pin is marked as set");
        final ReadCard1 readCard1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(readCard1.getData().getReadCard1DataCard().get(0).getIsPintSet());

        AND("The user wants to update their pin");
        WHEN("User makes a call to update their pin");
        WriteCardPinRequest1 updateCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        updateCardPinRequest1.setPinServiceType("C");

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.setDebitCardPin(alphaTestUser, updateCardPinRequest1, 200);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_set_pin_null_date() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with null date");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setCardExpiryDate(null);

        THEN("They receive a 400 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 400);

        Assertions.assertTrue(errorResponse1.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_set_pin_null_cardNumber() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with null cardNumber");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setCardNumber(null);

        THEN("They receive a 400 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 400);

        Assertions.assertTrue(errorResponse1.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_set_pin_null_lastFourDigits() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with null lastFourDigits");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setLastFourDigits(null);

        THEN("They receive a 400 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 400);

        Assertions.assertTrue(errorResponse1.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_set_pin_null_cardNumberFlag() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with null cardNumberFlag");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setCardNumberFlag(null);

        THEN("They receive a 400 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 400);

        Assertions.assertTrue(errorResponse1.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_set_pin_null_cardExpiryDate() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with null cardExpiryDate");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setCardExpiryDate(null);

        THEN("They receive a 400 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 400);

        Assertions.assertTrue(errorResponse1.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_set_pin_null_pinBlock() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with null pinBlock");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setPinBlock(null);

        THEN("They receive a 400 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 400);

        Assertions.assertTrue(errorResponse1.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_set_pin_null_pinServiceType() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with pinServiceType");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setPinServiceType(null);

        THEN("They receive a 400 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 400);

        Assertions.assertTrue(errorResponse1.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"4584610000000007", "5584610000000008"})
    public void negative_test_user_tries_to_set_pin_for_not_found_card_number(String notFoundCardNo) throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin with not found card number : " + notFoundCardNo);
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);
        writeCardPinRequest1.setCardNumber(notFoundCardNo);

        THEN("They receive a 404 response back from the service");
        OBErrorResponse1 errorResponse1 = this.cardsApiFlows.setDebitCardPinError(alphaTestUser, writeCardPinRequest1, 404);

        Assertions.assertTrue(errorResponse1.getMessage().contains(CARD_NOT_FOUND), "Error message was not as expected, " +
                "test expected : " + CARD_NOT_FOUND);
        DONE();
    }


    @Order(101)
    @Test()
    public void negative_test_user_tries_to_set_pin_invalid_token() throws Throwable {
        setupTestUser();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        AND("Their token is set as invalid");
        alphaTestUser.getLoginResponse().setAccessToken("invalid");
        WHEN("User makes a call to set their pin with an invalid token");
        WriteCardPinRequest1 writeCardPinRequest1 = validWriteCardPinRequest1(pinBlock);

        THEN("They receive a 401 response back from the service");
        this.cardsApiFlows.setDebitCardPin(alphaTestUser, writeCardPinRequest1, 401);

        DONE();
    }

}
