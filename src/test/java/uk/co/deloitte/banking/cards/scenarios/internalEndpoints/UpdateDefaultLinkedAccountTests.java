package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.card.model.ReadCard2;
import uk.co.deloitte.banking.account.api.card.model.WriteCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class UpdateDefaultLinkedAccountTests {

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
    private AccountApi accountApi;

    @Inject
    TemenosConfig temenosConfig;

    @Inject
    private CardsConfiguration cardsConfiguration;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private String CREATED_CARD_NUMBER = null;

    private String CREATED_CARD_EXP_DATE = null;

    private String CREATED_CARD_ID = null;

    private String CREATED_ACCOUNT_1 = null;

    private String CREATED_ACCOUNT_2 = null;


    private static final String CARD_MASK = "000000";


    private void setupTestUser() {
        
        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

            generateAccountNumbers();

            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1withMultipleAccounts());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);

            CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
            CREATED_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();
            CREATED_CARD_ID = createCardResponse.getData().getCardNumber().replace(CARD_MASK, "");



        }
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    private void generateAccountNumbers() {
        CREATED_ACCOUNT_1 = alphaTestUser.getAccountNumber();
        OBWriteAccountResponse1 current = accountApi.createCustomerCurrentAccount(alphaTestUser);
        Assertions.assertNotNull(current);
        CREATED_ACCOUNT_2 = current.getData().getAccountId();

    }

    private CreateCard1 validCreateCard1withMultipleAccounts() {
        return CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUser.getName())
                        .accounts(List.of(createCardAccount1FirstAccount(), createCardAccount1SecondAccount()))
                        .build())
                .build();
    }

    private CreateCardAccount1 createCardAccount1FirstAccount() {
        return CreateCardAccount1.builder()
                .accountCurrency("AED")
                .accountName("CURRENT")
                .accountNumber(CREATED_ACCOUNT_1)
                .accountType(AccountType.CURRENT.getDtpValue())
                .openDate(LocalDateTime.now())
                .seqNumber("1")
                .build();
    }

    private CreateCardAccount1 createCardAccount1SecondAccount() {
        return CreateCardAccount1.builder()
                .accountCurrency("AED")
                .accountName("SAVINGS")
                .accountNumber(CREATED_ACCOUNT_2)
                .accountType(AccountType.SAVINGS.getDtpValue())
                .openDate(LocalDateTime.now())
                .seqNumber("2")
                .build();
    }

    private WriteCardAccount1 writeCardAccount1V1(String accountNumber, String accountType) {
        return WriteCardAccount1.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_user_updates_their_default_account() {
        TEST("AHBDB-13285 - defect created - retested and passed");
        TEST("AHBDB-13191 Update FetchDebitCard mapping to support multiple LinkedAccounts");
        TEST("AHBDB-4776 user can update their default linked account number");
        setupTestUser();
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a valid card with two linked accounts created");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        THEN("Then default account matches the first sequenced account : " + CREATED_ACCOUNT_1);
        String defaultAccount1 = cards.getData().getReadCard1DataCard().get(0).getLinkedAccount().getAccountNumber();
        assertEquals(defaultAccount1, CREATED_ACCOUNT_1);

        WHEN("User makes a call to update their default linked account number to account number : " + CREATED_ACCOUNT_2);
        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.updateDefaultLinkedAccount(alphaTestUser, writeCardAccount1V1(CREATED_ACCOUNT_2, "10"), CREATED_CARD_ID, 200);

        AND("Their default linked account is updated");
        final ReadCard1 cards2 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String defaultAccount2 = cards2.getData().getReadCard1DataCard().get(0).getLinkedAccount().getAccountNumber();
        assertEquals(defaultAccount2, CREATED_ACCOUNT_2);
        assertNotEquals(defaultAccount1, defaultAccount2);

        WHEN("User with a card with multiple linked accounts makes a call to get their cards to ESB / HPS");
        final ReadCard2 readCard2 = this.cardsApiFlows.fetchCardsForUserV2(alphaTestUser);

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertEquals(readCard2.getData().getCards().size(), 1);

        AND("The card with two linked accounts is returned with the two linked accounts");
        Assertions.assertNotEquals(readCard2.getData().getCards().get(0).getLinkedAccounts().size(), 1);
        Assertions.assertEquals(readCard2.getData().getCards().get(0).getLinkedAccounts().size(), 2);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_updates_their_default_account_null_accountNumber() {
        setupTestUser();
        TEST("AHBDB-4776 user can update their default linked account number");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to update their default linked account number with null cardNumberFlag");
        WriteCardAccount1 writeCardAccount1 = writeCardAccount1V1(CREATED_ACCOUNT_2, "20");
        writeCardAccount1.setAccountNumber(null);
        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.updateDefaultLinkedAccount(alphaTestUser, writeCardAccount1, CREATED_CARD_ID, 400);

        DONE();
    }


    @Order(2)
    @Test()
    public void negative_test_user_updates_their_default_account_incorrect_accountNumber() {
        setupTestUser();
        TEST("AHBDB-4776 user can update their default linked account number");
        GIVEN("I have a valid customer with accounts scope");

        String accountNumber = RandomDataGenerator.generateRandomNumeric(10);
        WHEN("User makes a call to update their default linked account number with an incorrect account number: " + accountNumber);
        WriteCardAccount1 writeCardAccount1  = writeCardAccount1V1(CREATED_ACCOUNT_2, "20");
        writeCardAccount1.setAccountNumber(accountNumber);
        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.updateDefaultLinkedAccount(alphaTestUser, writeCardAccount1, CREATED_CARD_ID, 404);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_updates_their_default_account_another_users_account() {
        setupTestUser();
        TEST("AHBDB-4776 user can update their default linked account number");
        GIVEN("I have a valid customer with accounts scope");

        String accountNumber = temenosConfig.getCreditorAccountId();
        WHEN("User makes a call to update their default linked account number with an incorrect account number: " + accountNumber);
        WriteCardAccount1 writeCardAccount1  = writeCardAccount1V1(CREATED_ACCOUNT_2, "20");
        writeCardAccount1.setAccountNumber(accountNumber);
        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.updateDefaultLinkedAccount(alphaTestUser, writeCardAccount1, CREATED_CARD_ID, 404);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_updates_their_default_account_null_accountType() {
        setupTestUser();
        TEST("AHBDB-4776 user can update their default linked account number");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to update their default linked account number with null cardNumberFlag");
        WriteCardAccount1 writeCardAccount1  = writeCardAccount1V1(CREATED_ACCOUNT_2, "20");
        writeCardAccount1.setAccountType(null);
        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.updateDefaultLinkedAccount(alphaTestUser, writeCardAccount1, CREATED_CARD_ID, 400);

        DONE();
    }

    @Order(3)
    @Test()
    public void positive_test_user_views_all_linked_accounts() {
        setupTestUser();
        TEST("AHBDB-13196 user can update their default linked account number");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a valid card with two linked accounts created");
        final ReadCard2 cards = this.cardsApiFlows.fetchCardsForUserV2(alphaTestUser);

        THEN("Then default account matches the first sequenced account : " + CREATED_ACCOUNT_1);
        AND("Then second account matches the second sequenced account : " + CREATED_ACCOUNT_2);
        String defaultAccount1 = cards.getData().getCards().get(0).getLinkedAccounts().get(0).getAccountNumber();
        String defaultAccount2 = cards.getData().getCards().get(0).getLinkedAccounts().get(1).getAccountNumber();
        assertEquals(defaultAccount1, CREATED_ACCOUNT_1);
        assertEquals(defaultAccount2, CREATED_ACCOUNT_2);
        assertNotEquals(defaultAccount1, defaultAccount2);

        DONE();
    }


    @Order(100)
    @Test()
    public void negative_test_user_updates_their_default_account_invalid_token() {
        setupTestUser();
        TEST("AHBDB-4776 user can update their default linked account number");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to update their default linked account number with invalid token");
        WriteCardAccount1 writeCardAccount1  = writeCardAccount1V1(CREATED_ACCOUNT_2, "20");

        alphaTestUser.getLoginResponse().setAccessToken("Invalid");

        THEN("They receive a 401 response back from the service");
        this.cardsApiFlows.updateDefaultLinkedAccount(alphaTestUser, writeCardAccount1, CREATED_CARD_ID, 401);
        DONE();
    }
}

