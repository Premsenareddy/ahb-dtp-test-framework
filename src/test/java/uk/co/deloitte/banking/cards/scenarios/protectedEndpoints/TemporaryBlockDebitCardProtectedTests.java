package uk.co.deloitte.banking.cards.scenarios.protectedEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.parameters.WriteCardParameters1;
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
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class TemporaryBlockDebitCardProtectedTests {

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

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

    private static final String CARD_MASK = "000000";

    private static final String NULL_ERROR = "must not be null";

    private static final String CARD_NOT_FOUND = "Card not found";

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
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
    }

    private WriteCardParameters1 validUpdateCardParameters2() {
        return WriteCardParameters1.builder()
                .nationalUsage(false)
                .nationalPOS(false)
                .nationalDisATM(false)
                .internationalUsage(false)
                .internetUsage(false)
                .operationReason("operation reason")
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_user_blocks_their_card() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        THEN("They receive a 200 response back from the service");
        AND("They make a valid request to block debit card");

        THEN("A 200 is returned from the service");
        cardsApiFlows.blockCardProtected(validUpdateCardParameters2(), cardId, cif, 200);

        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        AND("The user can unblock their card");
        WriteCardParameters1 updateCardParameters2 = WriteCardParameters1.builder()
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.blockCardProtected(updateCardParameters2, cardId, cif, 200);

        THEN("their card is unblocked");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");
        DONE();
    }

    @Order(1)
    @Test()
    public void positive_test_user_blocks_card_null_operationReason() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        AND("They make an valid request to block debit card with null operationReason");
        WriteCardParameters1 updateCardParameters2 = validUpdateCardParameters2();
        updateCardParameters2.setOperationReason(null);


        THEN("A 200 is returned from the service");
        cardsApiFlows.blockCardProtected(updateCardParameters2, cardId, cif, 200);

        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        THEN("they can unblock their card");
        WriteCardParameters1 unblockCard = WriteCardParameters1.builder()
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.blockCardProtected(unblockCard, cardId, cif, 200);
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");

        DONE();
    }

    @Order(1)
    @Test()
    public void positive_test_user_blocks_card_nationalUsage_true() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        AND("They make an valid request to block debit card with nationalUsage true");
        WriteCardParameters1 updateCardParameters2 = WriteCardParameters1.builder()
                .internetUsage(false)
                .nationalUsage(true)
                .internationalUsage(false)
                .build();

        THEN("A 200 is returned from the service");
        cardsApiFlows.blockCardProtected(updateCardParameters2, cardId, cif, 200);

        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "Y");


        THEN("they can unblock their card");
        WriteCardParameters1 unblockCard = WriteCardParameters1.builder()
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.blockCardProtected(unblockCard, cardId, cif, 200);

        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");

        DONE();
    }

    @Order(1)
    @Test()
    public void positive_test_user_blocks_card_internetUsage_true() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make an valid request to block debit card with null nationalDisATM");
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        AND("They make an valid request to block debit card with internetUsage true");
        WriteCardParameters1 updateCardParameters2 = WriteCardParameters1.builder()
                .internetUsage(true)
                .nationalUsage(false)
                .internationalUsage(false)
                .build();

        THEN("A 200 is returned from the service");
        cardsApiFlows.blockCardProtected(updateCardParameters2, cardId, cif, 200);

        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        THEN("they can unblock their card");
        WriteCardParameters1 unblockCard = WriteCardParameters1.builder()
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.blockCardProtected(unblockCard, cardId, cif, 200);

        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");

        DONE();
    }

    @Order(1)
    @Test()
    public void positive_test_user_blocks_card_internationalUsage_true() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        AND("They make an valid request to block debit card with internationalUsage true");
        WriteCardParameters1 updateCardParameters2 = WriteCardParameters1.builder()
                .internetUsage(false)
                .nationalUsage(false)
                .internationalUsage(true)
                .build();

        THEN("A 200 is returned from the service");
        cardsApiFlows.blockCardProtected(updateCardParameters2, cardId, cif, 200);

        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        THEN("they can unblock their card");
        WriteCardParameters1 unblockCard = WriteCardParameters1.builder()
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.blockCardProtected(unblockCard, cardId, cif, 200);

        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {9, 11})
    public void negative_test_user_blocks_card_invalid_cif_length(int invalidCifLength) {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        String cif = generateRandomNumeric(invalidCifLength);
        AND("I have an invalid CIF : " + cif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("User makes a call to block a card with an invalid cif length : " + cif);

        THEN("A 404 is returned from the service");
        cardsApiFlows.blockCardProtected(validUpdateCardParameters2(), cardId, cif, 404);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"12 34 213 ", "  ", "abcddefwdd"})
    public void negative_test_user_blocks_card_invalid_cif_length(String invalidCif) {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        AND("I have an invalid CIF : " + invalidCif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("User makes a call to block a card with an invalid cif : " + invalidCif);

        THEN("A 404 is returned from the service");
        cardsApiFlows.blockCardProtected(validUpdateCardParameters2(), cardId, invalidCif, 404);

        DONE();
    }

    @Order(2)
    @Test
    public void negative_test_user_blocks_card_invalid_cif_length() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        String cif = generateRandomNumeric(10);
        AND("I have an invalid random CIF : " + cif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("User makes a call to block a card with an invalid random cif : " + cif);

        THEN("A 404 is returned from the service");
        cardsApiFlows.blockCardProtected(validUpdateCardParameters2(), cardId, cif, 404);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_test_user_blocks_another_users_card() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId =  cardsConfiguration.getCreatedCard().replace(CARD_MASK, "");
        AND("They make an valid request to block debit card with another users cardId");

        THEN("A 404 is returned from the service");
        cardsApiFlows.blockCardProtected(validUpdateCardParameters2(), cardId, cif, 404);
        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"12 34 213", "  ", "cardIdarCd"})
    public void negative_test_user_blocks_card_invalid_cardId(String cardId) {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        AND("They make an valid request to block debit card with invalid cardId " + cardId);

        THEN("A 400 is returned from the service");
        cardsApiFlows.blockCardProtected(validUpdateCardParameters2(), cardId, cif, 400);
        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {9, 11})
    public void negative_test_user_blocks_card_invalid_cardId_length(int cardIdLength) {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = generateRandomNumeric(cardIdLength);

        AND("They make an valid request to block debit card with invalid cardId " + cardId);

        THEN("A 400 is returned from the service");
        cardsApiFlows.blockCardProtected(validUpdateCardParameters2(), cardId, cif, 400);
        DONE();
    }

    @Order(101)
    @Test()
    public void negative_test_user_tries_to_block_card_invalid_token() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        AND("They make a invalid request to block debit card with no api key");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        THEN("They receive a 401 response back from the service");
        this.cardsApiFlows.blockCardProtectedNoKey(validUpdateCardParameters2(), cardId, cif, 401);

        DONE();
    }

}
