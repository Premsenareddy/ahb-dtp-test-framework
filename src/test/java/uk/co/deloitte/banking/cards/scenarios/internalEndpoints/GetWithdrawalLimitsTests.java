package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.TransactionType;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
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
import java.util.List;

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class GetWithdrawalLimitsTests {

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

    private static final String CARD_NOT_FOUND_ERROR = "Card Id not found - Please provide a valid Card Id";

    private static final String CARD_REGEX_ERROR = "must match \"^[0-9]*$\"";

    private static final String CARD_LENGTH_ERROR = "size must be between 10 and 10";

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

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Order(1)
    @Test()
    public void get_withdrawal_limit_for_user() {
        setupTestUser();
        TEST("AHBDB-3298 View cards withdrawal limit");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        final ReadCardLimits1 cardLimits = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), cardId);
        THEN("Should return withdrawal limits for provided card");

        Assertions.assertEquals(cardLimits.getData().getLimits().getLimitTypeWording(),TransactionType.WITHDRAWAL.getLabel().toUpperCase());

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"14714841736", "147148417", " "})
    public void negative_test_get_withdrawal_limit_invalid_cardNo_length(String InvalidCardNoLength) {
        setupTestUser();
        TEST("AHBDB-3298 View cards withdrawal limit with an invalid card length : " + InvalidCardNoLength);
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a cardNo of invalid length : " + InvalidCardNoLength);

        THEN("The get cvv service will return a 400");
        final OBErrorResponse1  cardLimits = cardsApiFlows.fetchCardLimitsForTransactionTypeError(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), InvalidCardNoLength, 400);

        Assertions.assertTrue(cardLimits.getMessage().contains(CARD_LENGTH_ERROR), "Error message was not as expected, " +
                "test expected : " + CARD_LENGTH_ERROR);

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"@££$$%^&*(", "abcdevfdew"})
    public void negative_test_get_withdrawal_limit_invalid_cardNo_content(String InvalidCardNoContent) {

        setupTestUser();
        TEST("AHBDB-3298 View cards withdrawal limit with an invalid card content : " + InvalidCardNoContent);
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a cardNo of invalid content : " + InvalidCardNoContent);

        THEN("The get cvv service will return a 400");
        final OBErrorResponse1  cardLimits = cardsApiFlows.fetchCardLimitsForTransactionTypeError(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), InvalidCardNoContent, 400);

        Assertions.assertTrue(cardLimits.getMessage().contains(CARD_REGEX_ERROR), "Error message was not as expected, " +
                "test expected : " + CARD_REGEX_ERROR);

        DONE();
    }

    @Order(4)
    @Test
    public void negative_test_get_withdrawal_limit_invalid_cardNo_not_found() {
        setupTestUser();
        String InvalidCardIDNotFound = RandomDataGenerator.generateRandomNumeric(10);
        TEST("AHBDB-3298 View cards withdrawal limit with a not found cardNo : " + InvalidCardIDNotFound);
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a not found cardNo : " + InvalidCardIDNotFound);

        THEN("The get cvv service will return a 404");
        final OBErrorResponse1  cardLimits = cardsApiFlows.fetchCardLimitsForTransactionTypeError(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), InvalidCardIDNotFound, 404);
        Assertions.assertTrue(cardLimits.getMessage().contains(CARD_NOT_FOUND_ERROR), "Error message was not as expected, " +
                "test expected : " + CARD_NOT_FOUND_ERROR);
        DONE();
    }

    @Order(4)
    @Test
    public void negative_test_get_withdrawal_limit_another_users_card() {
        setupTestUser();
        String cardId = cardsConfiguration.getCreatedCard().replace(CARD_MASK, "");

        TEST("AHBDB-3298 View cards withdrawal limit with a not found cardID : " + cardId);
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a not found cardNo : " + cardId);

        THEN("The get cvv service will return a 404");
        this.cardsApiFlows.fetchCardLimitsForTransactionTypeError(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), cardId, 404);
        DONE();
    }

    @Order(6)
    @Test
    public void negative_test_get_withdrawal_limit_invalid_token() {
        setupTestUser();
        TEST("AHBDB-3298 View cards withdrawal limit for a valid user with an invalid token returns 401");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        WHEN("The user's scope is incorrect");
        this.alphaTestUser.getLoginResponse().setAccessToken("invalidToken");

        THEN("The service will return a 403");
        this.cardsApiFlows.fetchCardLimitsForTransactionTypeErrorVoid(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), cardId, 401);
        DONE();
    }

}
