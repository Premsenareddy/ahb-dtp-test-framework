package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1DataCard;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CreateVirtualCardsTests {

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
    private TemenosConfig temenosConfig;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private static final String CARD_MASK = "000000";

    private static final String NULL_ERROR = "must not be null";

    private static final String ACCOUNT_NOT_USERS = "Account Number For Card Request is Invalid";

    private void setupTestUser() {

        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

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
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:45", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_create_virtual_debit_card() {
        setupTestUser();
        TEST("AHBDB-228 / AHBDB-6963 create virtual debit cards");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 200 response");
        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1());
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        AND("The card is created for the user");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());

        DONE();
    }

    @Order(2)
    @Test()
    public void positive_test_create_multiple_virtual_debit_cards() {
        setupTestUser();
        TEST("AHBDB-228 / AHBDB-6963 create virtual debit cards");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("Debit card is already created for the user");
        AND("User wants to create a second valid virtual debit card");

        THEN("A virtual card is created for the user with a 200 response");
        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1());
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        AND("Second card is created for the user");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() == 2);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().stream().map(ReadCard1DataCard::getCardNumber).collect(Collectors.toList()).contains(createCardResponse.getData().getCardNumber()));

        DONE();
    }


    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"2011-12-03", "2011/12/03", "%^"})
    public void negative_test_create_virtual_debit_card_invalid_openDate(String date) {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards null name");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid OpenDate : " + date);
        JSONObject account = new JSONObject();
        account.put("AccountType", AccountType.CURRENT.getDtpValue());
        account.put("AccountNumber", alphaTestUser.getAccountNumber());
        account.put("AccountName", "CURRENT");
        account.put("AccountCurrency", "AED");
        account.put("OpenDate", date);
        account.put("SeqNumber", "1");

        ArrayList<Object> accounts = new ArrayList();
        accounts.add(account);

        JSONObject body = new JSONObject();
        body.put("EmbossedName", alphaTestUser.getName());
        body.put("Accounts", accounts);

        JSONObject data = new JSONObject();
        data.put("Data", body);

        THEN("A virtual card is not created for the user with a 400 response");
        this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, data, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_invalid_account_type() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards null name");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid accountType ");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setAccountType("INVALIDTYPE");

        THEN("A virtual card is not created for the user with a 400 response");
        this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        DONE();
    }


    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_null_name() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards null name");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid null name");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().setEmbossedName(null);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        Assertions.assertTrue(createCardResponse.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_null_currency() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards null currency");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid null currency");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setAccountCurrency(null);


        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);

        Assertions.assertTrue(createCardResponse.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_null_accountName() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards null account name");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid null accountName");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setAccountName(null);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        Assertions.assertTrue(createCardResponse.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_null_accountNumber() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards null accountNumber");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid null accountNumber");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setAccountNumber(null);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        Assertions.assertTrue(createCardResponse.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_null_accountType() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards null accountType");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid null accountType");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setAccountType(null);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        Assertions.assertTrue(createCardResponse.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_null_openDate() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid null openDate");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setOpenDate(null);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        Assertions.assertTrue(createCardResponse.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_null_seqNumber() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid null seqNumber");
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setSeqNumber(null);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        Assertions.assertTrue(createCardResponse.getMessage().contains(NULL_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_ERROR);
        DONE();
    }

    @Order(3)
    @Test()
    public void negative_test_create_virtual_debit_card_too_many_linked_accounts() {
        setupTestUser();
        TEST("AHBDB-6164 create virtual debit cards with invalid amount of linked accounts");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with invalid values");
        CreateCard1 createCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUser.getName())
                        .accounts(List.of(
                                CreateCardAccount1.builder()
                                        .accountCurrency("AED")
                                        .accountName("CURRENT")
                                        .accountNumber(alphaTestUser.getAccountNumber())
                                        .accountType(AccountType.CURRENT.getDtpValue())
                                        .openDate(LocalDateTime.parse("2011-12-03T10:15:45", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                        .seqNumber("1")
                                        .build(),
                                CreateCardAccount1.builder()
                                        .accountCurrency("AED")
                                        .accountName("CURRENT")
                                        .accountNumber(alphaTestUser.getAccountNumber())
                                        .accountType(AccountType.CURRENT.getDtpValue())
                                        .openDate(LocalDateTime.parse("2011-12-03T10:15:45", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                        .seqNumber("2")
                                        .build(),
                                CreateCardAccount1.builder()
                                        .accountCurrency("AED")
                                        .accountName("CURRENT")
                                        .accountNumber(alphaTestUser.getAccountNumber())
                                        .accountType(AccountType.CURRENT.getDtpValue())
                                        .openDate(LocalDateTime.parse("2011-12-03T10:15:45", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                        .seqNumber("3")
                                        .build()
                        ))
                        .build())
                .build();

        THEN("A virtual card is not created for the user with a 400 response");
        this.cardsApiFlows.createVirtualDebitCardErrorVoid(alphaTestUser, createCard1, 400);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_random_account_number() {
        TEST("AHBDB-13264 - Test fixed");
        setupTestUser();
        TEST("AHBDB-228/ AHBDB-6963 create virtual debit cards");
        GIVEN("I have a valid customer with accounts scope");

        String randomCardAccount = generateRandomNumeric(12);
        WHEN("They create a virtual card with random account number : " + randomCardAccount);
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setAccountNumber(randomCardAccount);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);
        Assertions.assertTrue(createCardResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);
        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_create_virtual_debit_card_another_users_account_number() {
        TEST("AHBDB-13264 - Test fixed");
        setupTestUser();
        TEST("AHBDB-228/ AHBDB-6963 create virtual debit cards");
        GIVEN("I have a valid customer with accounts scope");

        String otherUsersAccountNumber = temenosConfig.getCreditorIban();
        WHEN("They create a virtual card with random account number : " + otherUsersAccountNumber);
        final CreateCard1 createCard1 = validCreateCard1();
        createCard1.getData().getAccounts().get(0).setAccountNumber(otherUsersAccountNumber);

        THEN("A virtual card is not created for the user with a 400 response");
        final OBErrorResponse1 createCardResponse = this.cardsApiFlows.createVirtualDebitCardError(alphaTestUser, createCard1, 400);

        Assertions.assertTrue(createCardResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);

        DONE();
    }


    @Order(101)
    @Test()
    public void negative_test_create_virtual_debit_card_incorrect_token() {
        setupTestUser();
        TEST("AHBDB-228 create virtual debit cards invalid token");
        GIVEN("I have a valid customer with accounts scope");
        AND("The token is invalid");
        alphaTestUser.getLoginResponse().setAccessToken("invalid");

        WHEN("They create a virtual card with valid values");
        final CreateCard1 createCard1 = validCreateCard1();

        THEN("A virtual card is not created for the user with a 401 response");
        this.cardsApiFlows.createVirtualDebitCardErrorVoid(alphaTestUser, createCard1, 401);

        DONE();
    }

}
