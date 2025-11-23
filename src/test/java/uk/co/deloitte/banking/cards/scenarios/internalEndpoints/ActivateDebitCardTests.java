package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ModificationOperation;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class ActivateDebitCardTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private CardsConfiguration cardsConfiguration;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;


    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

    private void setupTestUser() {

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
        }
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
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


    @Order(1)
    @Test()
    public void positive_test_user_can_activate_their_virtual_debit_card_and_deactivate_it() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        WHEN("User makes a call to activate their debit card");
        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 200);

        AND("My card is activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        WHEN("The user makes a call to deactivate their debit card");
        ActivateCard1 activateCardDeact = validActivateCard1();
        activateCardDeact.setModificationOperation(ModificationOperation.W);

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, activateCardDeact, 200);

        AND("My card is deactivated");
        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        Assertions.assertFalse(cards1.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());


        DONE();
    }

    @Order(1)
    @Test()
    public void positive_test_user_tries_to_activate_their_debit_card_null_operationReason() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        WHEN("User makes a call to activate their debit card");

        WHEN("User makes a call to activate their debit card with a null modificationOperation");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setOperationReason(null);

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, activateCard, 200);

        WHEN("The user makes a call to deactivate their debit card");
        ActivateCard1 activateCardDeact = validActivateCard1();
        activateCardDeact.setModificationOperation(ModificationOperation.W);

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, activateCardDeact, 200);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_activate_an_already_activated_card() {
        TEST("AHBDB-13264 - Test fixed");

        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        WHEN("User makes a call to activate their debit card");

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 200);

        WHEN("The user makes a call to activate their debit card again");

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_activate_an_another_users_card() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        WHEN("User makes a call to activate another users debit card");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setCardNumber(cardsConfiguration.getCreatedCard());

        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, activateCard, 404);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_deactivate_an_another_users_card() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        WHEN("User makes a call to deactivate another users debit card");
        THEN("They receive a 200 response back from the service");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setCardNumber(cardsConfiguration.getCreatedCard());
        activateCard.setModificationOperation(ModificationOperation.W);

        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, activateCard, 404);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {15, 17})
    public void negative_test_user_tries_activate_their_card_wrong_card_length(int invalidCardNumberLength) {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String invalidNumber = generateRandomNumeric(invalidCardNumberLength);

        WHEN("User makes a call to activate a card with an invalid card number length : " + invalidNumber);
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setCardNumber(invalidNumber);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, activateCard, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {3, 5})
    public void negative_test_user_tries_activate_their_card_wrong_last_four_length(int invalidLastFourLength) {
        TEST("AHBDB-13264 - Test fixed");

        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String invalidLastFour = generateRandomNumeric(invalidLastFourLength);

        WHEN("User makes a call to activate a card with an invalid last four length : " + invalidLastFour);
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setLastFourDigits(invalidLastFour);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, activateCard, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_null_cardNumber() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to activate their debit card with a null cardNumber");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setCardNumber(null);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardError(alphaTestUser, activateCard, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_null_lastForDigits() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to activate their debit card with a null lastForDigits");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setLastFourDigits(null);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardError(alphaTestUser, activateCard, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_null_cardNumberFlag() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to activate their debit card with a null cardNumberFlag");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setCardNumberFlag(null);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardError(alphaTestUser, activateCard, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_null_cardExpiryDate() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to activate their debit card with a null cardExpiryDate");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setCardExpiryDate(null);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardError(alphaTestUser, activateCard, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_null_modificationOperation() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to activate their debit card with a null modificationOperation");
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setModificationOperation(null);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardError(alphaTestUser, activateCard, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_not_found_card() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");
        String notFoundCardNo = RandomDataGenerator.generateRandomNumeric(16);
        WHEN("User makes a call to activate their debit card with a not found card number : " + notFoundCardNo);
        ActivateCard1 activateCard = validActivateCard1();
        activateCard.setCardNumber(notFoundCardNo);

        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.activateDebitCardError(alphaTestUser, activateCard, 404);

        DONE();
    }


    @Order(102)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_with_incorrect_token() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to activate their debit card with an invalid token ");
        alphaTestUser.getLoginResponse().setAccessToken("invalid");
        ActivateCard1 activateCard = validActivateCard1();


        THEN("They receive a 401 response back from the service");
        this.cardsApiFlows.activateDebitCardErrorVoid(alphaTestUser, activateCard, 401);

        DONE();
    }

}
