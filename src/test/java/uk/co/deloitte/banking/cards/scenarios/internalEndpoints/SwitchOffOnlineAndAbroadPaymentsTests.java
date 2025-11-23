package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
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
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.filters.UpdateCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class SwitchOffOnlineAndAbroadPaymentsTests {

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
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private BankingConfig bankingConfig;

    private AlphaTestUser alphaTestUser;

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

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
            this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 200);

        }
    }

    private void existingUserLogin() {
        envUtils.shouldSkipHps(Environments.NONE);
        //CRM clear down dev users every evening
        envUtils.ignoreTestInEnv(Environments.DEV);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            alphaTestUser.setUserId(bankingConfig.getBankingUserUserId());
            alphaTestUser.setUserPassword(bankingConfig.getBankingUserPassword());
            alphaTestUser.setAccountNumber(bankingConfig.getBankingUserAccountNumber());
            alphaTestUser.setDeviceId(bankingConfig.getBankingUserDeviceId());
            alphaTestUser.setPrivateKeyBase64(bankingConfig.getBankingUserPrivateKey());
            alphaTestUser.setPublicKeyBase64(bankingConfig.getBankingUserPublicKey());
            UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                    .userId(alphaTestUser.getUserId())
                    .password(alphaTestUser.getUserPassword())
                    .build();
            UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginExistingUserProtected(userLoginRequestV2, alphaTestUser);
            parseLoginResponse(alphaTestUser, userLoginResponseV2);

            final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
            CREATED_CARD_NUMBER =   cards.getData().getReadCard1DataCard().get(0).getCardNumber();
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

    private UpdateCardParameters1 validUpdateCardParameters1() {
        return UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internationalUsage(false)
                .internetUsage(false)
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_user_switches_off_and_on_online_payments() {
        setupTestUser();
        TEST("AHBDB-3295 Switch On/Off online payments for debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to switch off online payments debit card");

        THEN("A 200 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, validUpdateCardParameters1(), 200);

        AND("The users card is shown as having online payments blocked");

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");

        AND("The user can turn back on online payments");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internetUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("online payments is turned back on");

        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        DONE();
    }

    @Order(1)
    @Test()
    public void positive_test_user_switches_off_and_on_overseas_payments() {
        setupTestUser();
        TEST("AHBDB-3296 Switch On/Off payments abroad for debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to switch off online payments debit card");

        THEN("A 200 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, validUpdateCardParameters1(), 200);

        AND("The users card is shown as having online payments blocked");

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");

        AND("The user can turn back on online payments");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internationalUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("online payments is turned back on");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_switches_off_payments_null_cardNumberFlag() {
        setupTestUser();
        TEST("AHBDB-3296 Switch On/Off payments abroad for debit card / AHBDB-3295 Switch On/Off online payments for debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an invalid request to block debit card with null cardNumberFlag");
        UpdateCardParameters1 updateCardParameters1 = validUpdateCardParameters1();
        updateCardParameters1.setCardNumberFlag(null);

        THEN("A 400 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_switches_off_payments_null_cardNumber() {
        setupTestUser();
        TEST("AHBDB-3296 Switch On/Off payments abroad for debit card / AHBDB-3295 Switch On/Off online payments for debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an invalid request to block debit card with null cardNumberFlag");
        UpdateCardParameters1 updateCardParameters1 = validUpdateCardParameters1();
        updateCardParameters1.setCardNumber(null);

        THEN("A 400 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_switches_off_payments_randomCardNumber() {
        TEST("AHBDB-7506 defect fix");
        setupTestUser();
        TEST("AHBDB-3296 Switch On/Off payments abroad for debit card / AHBDB-3295 Switch On/Off online payments for debit card");
        GIVEN("I have a valid customer with accounts scope");

        String randomCard = RandomDataGenerator.generateRandomNumeric(16);
        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an invalid request to block debit card with a random  cardNumber : " + randomCard);
        UpdateCardParameters1 updateCardParameters1 = validUpdateCardParameters1();
        updateCardParameters1.setCardNumber(randomCard);

        THEN("A 404 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 404);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_switches_off_payments_other_users_card() {
        TEST("AHBDB-7506 defect fix");
        setupTestUser();
        TEST("AHBDB-3296 Switch On/Off payments abroad for debit card / AHBDB-3295 Switch On/Off online payments for debit card");
        GIVEN("I have a valid customer with accounts scope");

        String otherUsersCard = cardsConfiguration.getCreatedCard();
        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an invalid request to block debit card with a other users cardNumber : " + otherUsersCard);
        UpdateCardParameters1 updateCardParameters1 = validUpdateCardParameters1();
        updateCardParameters1.setCardNumber(otherUsersCard);

        THEN("A 404 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 404);

        DONE();
    }

    @Order(101)
    @Test()
    public void negative_test_user_switches_off_overseas_payments_incorrect_invalid_token() {
        setupTestUser();
        TEST("AHBDB-3296 Switch On/Off payments abroad for debit card / AHBDB-3295 Switch On/Off online payments for debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to switch off online payments debit card with invalid token");
        alphaTestUser.getLoginResponse().setAccessToken("invalid");

        THEN("A 403 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, validUpdateCardParameters1(), 401);

        DONE();
    }

}
