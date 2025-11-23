package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.card.model.ReadCard2;
import uk.co.deloitte.banking.account.api.card.model.WriteCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;


import javax.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class GetCardsTests {

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
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private static final String INVALID_TYPE_ERROR = "Bad request invalid card type";

    private static final String CARD_MASK = "000000";

    private void setupTestUser() {
        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        }
    }


    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
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
    public void get_cards_valid_type() {
        setupTestUser();
        TEST("AHBDB-297 get debit card details of a user with a valid card type");
        TEST("AHBDB-4538 AC1 Success response 200");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));
        DONE();
    }

    @Order(2)
    @Test()
    public void get_cards_valid_type_multiple_cards() {
        TEST("AHBDB-7669 - defect fix");

        setupTestUser();
        TEST("AHBDB-297 get debit card details of a user with a valid card type");
        TEST("AHBDB-4538 AC1 Success response 200");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard1 readCard1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        // only create a second card if the user has 1 card as we are using a hardcoded user
        if (readCard1.getData().getReadCard1DataCard().size() == 1) {
            String cardNumber = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1()).getData().getCardNumber();
            AND("The user creates a second card with card number : " + cardNumber);
        }

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertNotEquals(cards.getData().getReadCard1DataCard().size(), 1);
        DONE();
    }

    @Order(2)
    @ParameterizedTest()
    @ValueSource(strings = {"credit", "savings", "loyalty", "", "$%^&*(", ")(*&^%$R%T^&*(", "123456789"})
    public void negative_test_get_cards_invalid_type(String invalidCardType) {
        setupTestUser();
        TEST("AHBDB-297 get debit card details of a user with an invalid card type returns a 400 - card type : " + invalidCardType);
        TEST("AHBDB-4539 AC2 Error Response 400 - Bad Request/Fields to not match validations");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to get their cards to ESB / HPS with an invalid type parameter");
        final OBErrorResponse1 cardsError = this.cardsApiFlows.getCardsError(alphaTestUser, invalidCardType, 400);

        THEN("A 400 is returned by the service");
        Assertions.assertTrue(cardsError.getMessage().contains(INVALID_TYPE_ERROR), "Error message was not as expected, test expected : " + INVALID_TYPE_ERROR);
        DONE();
    }

    @Order(5)
    @Test
    public void negative_test_get_cards_user_has_invalid_token() {
        setupTestUser();
        TEST("AHBDB-297 get debit card details of a user with a incorrect scope");
        TEST("AHBDB-4542 AC3 Error Response 401 - Disallowed or missing customer token");
        GIVEN("I have a valid customer with an invalid token");

        this.alphaTestUser.getLoginResponse().setAccessToken(null);

        WHEN("User makes a call to get their cards to ESB / HPS");
        this.cardsApiFlows.getCardsErrorVoid(alphaTestUser, "debit", 401);
        THEN("A 401 is returned by the service");
        DONE();
    }

    @Order(6)
    @Test()
    public void get_cards_v2_valid_type() {
        setupTestUser();
        TEST("AHBDB-13191 get debit card details of a user with a valid card type");
        TEST("AHBDB-4538 AC1 Success response 200");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard2 cards = this.cardsApiFlows.fetchCardsForUserV2(alphaTestUser);

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getCards().size() > 0);
        Assertions.assertTrue(cards.getData().getCards().get(0).getCardNumber().contains(CARD_MASK));
        DONE();
    }

    @Order(7)
    @Test()
    public void get_cards_v2_valid_type_multiple_cards() {
        setupTestUser();
        TEST("AHBDB-13191 get debit card details of a user with a valid card type");
        TEST("AHBDB-4538 AC1 Success response 200");
        GIVEN("I have a valid customer with accounts scope");

        final ReadCard2 readCard2 = this.cardsApiFlows.fetchCardsForUserV2(alphaTestUser);

        // only create a second card if the user has 1 card as we are using a hardcoded user
        if (readCard2.getData().getCards().size() == 1) {
            String cardNumber = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1()).getData().getCardNumber();
            AND("The user creates a second card with card number : " + cardNumber);
        }

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard2 cards = this.cardsApiFlows.fetchCardsForUserV2(alphaTestUser);

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertNotEquals(cards.getData().getCards().size(), 1);
        Assertions.assertEquals(cards.getData().getCards().get(0).getLinkedAccounts().size(), 1);

        DONE();
    }

}
