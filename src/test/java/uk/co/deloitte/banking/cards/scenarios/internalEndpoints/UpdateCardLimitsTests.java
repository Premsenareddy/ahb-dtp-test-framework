package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1Data;

import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.TransactionType;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class UpdateCardLimitsTests {

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

    private static final String CARD_MASK = "000000";

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;


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
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
    }

    private WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1Valid(String dailyLimit, String monthlyLimit) {
        return  WriteDailyMonthlyLimits1.builder().data(writeDailyMonthlyLimits1DataValid(dailyLimit, monthlyLimit)).build();
    }

    private WriteDailyMonthlyLimits1Data writeDailyMonthlyLimits1DataValid(String dailyLimit, String monthlyLimit) {
        return WriteDailyMonthlyLimits1Data.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .dailyAtmLimit(dailyLimit)
                .dailyPosLimit(dailyLimit)
                .monthlyAtmLimit(monthlyLimit)
                .monthlyPosLimit(monthlyLimit)
                .dailyEcommLimit(dailyLimit)
                .monthlyEcommLimit(monthlyLimit)
                .build();
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

    @Order(1)
    @Test()
    public void positive_test_user_updates_their_monthly_limits_only() {
        TEST("AHBDB-13273 - defect raised");
        TEST("AHBDB-16556- defect raised as it is failing only in CIT environment");
        TEST("HPS no longer support monthly limit changes, leaving tests here but disabled in case they revert");
        envUtils.ignoreTestInEnv(Environments.CIT);
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();

        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        AND("They try to update their daily limits with null monthlyLimits");

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1 = writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit);
        writeDailyMonthlyLimits1.getData().setDailyAtmLimit(null);
        writeDailyMonthlyLimits1.getData().setDailyEcommLimit(null);
        writeDailyMonthlyLimits1.getData().setDailyPosLimit(null);

        THEN("They receive a 200 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1, 200);

        AND("Their withdrawal limits are updated");
        final ReadCardLimits1 cardLimitsWithdrawal = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), cardId);

        Assertions.assertNotEquals(cardLimitsWithdrawal.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(cardLimitsWithdrawal.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(cardLimitsWithdrawal.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(cardLimitsWithdrawal.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        AND("Their purchase limits are updated");
        final ReadCardLimits1 cardLimitsPurchase = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.PURCHASE.getLabel(), cardId);
        Assertions.assertNotEquals(cardLimitsPurchase.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(cardLimitsPurchase.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(cardLimitsPurchase.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(cardLimitsPurchase.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();
    }

    @Order(2)
    @Test()
    public void positive_test_user_updates_their_spending_limits_daily_only() {
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        AND("They try to update their daily limits with null monthlyLimits");

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1 = writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit);
        writeDailyMonthlyLimits1.getData().setMonthlyAtmLimit(null);
        writeDailyMonthlyLimits1.getData().setMonthlyPosLimit(null);
        writeDailyMonthlyLimits1.getData().setMonthlyEcommLimit(null);

        THEN("They receive a 200 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1, 200);

        AND("Their withdrawal limits are updated");
        final ReadCardLimits1 cardLimitsWithdrawal = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), cardId);

        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyOnusAmount(), dailyLimit);

        Assertions.assertNotEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);


        AND("Their purchase limits are updated");
        final ReadCardLimits1 cardLimitsPurchase = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.PURCHASE.getLabel(), cardId);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyOnusAmount(), dailyLimit);

        Assertions.assertNotEquals(cardLimitsPurchase.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(cardLimitsPurchase.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(cardLimitsPurchase.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);



        DONE();
    }

    @Order(3)
    @Test()
    public void positive_test_user_updates_their_spending_limits() {
        TEST("AHBDB-13273 - defect raised");
        TEST("AHBDB-16556- defect raised as it is failing only in CIT environment");
        TEST("HPS no longer support monthly limit changes, leaving tests here but disabled in case they revert");
        envUtils.ignoreTestInEnv(Environments.CIT);
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        AND(String.format("They update their daily limits to %s and monthly limits to %s ", dailyLimit, monthlyLimit));

        THEN("They receive a 200 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit), 200);

        AND("Their withdrawal limits are updated");
        final ReadCardLimits1 cardLimitsWithdrawal = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), cardId);

        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        AND("Their purchase limits are updated");
        final ReadCardLimits1 cardLimitsPurchase = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.PURCHASE.getLabel(), cardId);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();
    }

    @Order(4)
    @Test()
    public void negative_test_user_updates_their_spending_limits_null_card_number() {
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        AND("They try to update their daily limits with null cardNumber");

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1 = writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit);
        writeDailyMonthlyLimits1.getData().setCardNumber(null);

        THEN("They receive a 200 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1, 400);

        DONE();
    }

    @Order(5)
    @Test()
    public void negative_test_user_updates_their_spending_limits_null_cardNumberFlag() {
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        AND("They try to update their daily limits with null cardNumberFlag");

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1 = writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit);
        writeDailyMonthlyLimits1.getData().setCardNumberFlag(null);

        THEN("They receive a 400 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1, 400);

        DONE();
    }

    @Order(5)
    @Test()
    public void negative_test_user_updates_their_spending_limits_another_users_card() {
        TEST("AHBDB-7506 defect fix");
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        String otherUsersCard = cardsConfiguration.getCreatedCard();
        AND("They try to update the daily limits of another users card " + otherUsersCard);

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1 = writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit);
        writeDailyMonthlyLimits1.getData().setCardNumber(otherUsersCard);

        THEN("They receive a 404 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1, 404);

        DONE();
    }

    @Order(5)
    @Test()
    public void negative_test_user_updates_their_spending_limits_random_card() {
        TEST("AHBDB-7506 defect fix");
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));


        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        String randomCard = RandomDataGenerator.generateRandomNumeric(16);
        AND("They try to update the daily limits of a random card " + randomCard);

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1 = writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit);
        writeDailyMonthlyLimits1.getData().setCardNumber(randomCard);

        THEN("They receive a 404 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1, 404);

        DONE();
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(ints = {15, 17})
    public void negative_test_user_updates_their_spending_limits_wrong_card_length(int invalidLength) {
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        String invalidLengthCard =  RandomDataGenerator.generateRandomNumeric(invalidLength);
        AND("They try to update the daily limits of another users card " + invalidLengthCard);

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1 = writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit);
        writeDailyMonthlyLimits1.getData().setCardNumber(invalidLengthCard);

        THEN("They receive a 400 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1, 400);

        DONE();
    }

    @Order(101)
    @Test()
    public void negative_test_user_updates_their_spending_limits_invalid_token() {
        TEST("AHBDB-4953 user can update their limits on their card");
        GIVEN("I have a test user with a created card");
        setupTestUser();
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        AND("They try to update their daily limits with wrong token scope");
        alphaTestUser.getLoginResponse().setAccessToken("Invalid");

        THEN("They receive a 401 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1Valid(dailyLimit, monthlyLimit), 401);


        DONE();
    }

}
