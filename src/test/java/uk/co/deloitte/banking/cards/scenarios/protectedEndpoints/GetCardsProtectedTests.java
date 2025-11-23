package uk.co.deloitte.banking.cards.scenarios.protectedEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
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
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
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

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class GetCardsProtectedTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private BankingConfig bankingConfig;

    @Inject
    private AuthenticateApi authenticateApi;

    private AlphaTestUser alphaTestUser;

    private static final String INVALID_TYPE_ERROR = "Bad request invalid card type";

    private static final String CARD_MASK = "000000";

    private String CREATED_CARD_NUMBER = null;


    private void setupTestUser() {
        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
            CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
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
            CREATED_CARD_NUMBER = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
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

    @Order(1)
    @Test()
    public void get_cards_valid_type_protected() {
        setupTestUser();
        TEST("AHBDB-6385 get debit card details of a user with a valid card type");
        GIVEN("I have a valid customer with accounts scope");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to get their cards using the protected endpoint");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUserProtected(cif, "debit");

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), CREATED_CARD_NUMBER);

        DONE();
    }

    @Order(2)
    @Test()
    public void get_cards_valid_type_protected_multiple_cards() {
        TEST("AHBDB-7669 - defect fix");

        setupTestUser();
        TEST("AHBDB-6385  get debit card details of a user with a valid card type");
        GIVEN("I have a valid customer with accounts scope");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        final ReadCard1 readCard1 = this.cardsApiFlows.fetchCardsForUserProtected(cif, "debit");

        // only create a second card if the user has 1 card as we are using a hardcoded user
        if (readCard1.getData().getReadCard1DataCard().size() == 1) {
            String cardNumber = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1()).getData().getCardNumber();
            AND("The user creates a second card with card number : " + cardNumber);
        }

        WHEN("User makes a call to get their cards using the protected endpoint");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUserProtected(cif, "debit");

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertNotEquals(cards.getData().getReadCard1DataCard().size(), 1);

        DONE();
    }

    @Order(3)
    @ParameterizedTest()
    @ValueSource(strings = {"credit", "savings", "loyalty", "", "$%^&*(", ")(*&^%$R%T^&*(", "123456789"})
    public void negative_test_get_cards_invalid_type_protected(String invalidCardType) {
        setupTestUser();
        TEST("AHBDB-6385  get debit card details of a user with an invalid card type returns a 400 - card type : " + invalidCardType);
        GIVEN("I have a valid customer with accounts scope");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to get their cards using the protected endpoint with an invalid cardType");
        final OBErrorResponse1 cardsError = this.cardsApiFlows.getCardsErrorProtected(cif, invalidCardType, 400);

        THEN("A 400 is returned by the service");
        Assertions.assertTrue(cardsError.getMessage().contains(INVALID_TYPE_ERROR), "Error message was not as expected, test expected : " + INVALID_TYPE_ERROR);
        DONE();
    }

    @Order(3)
    @ParameterizedTest()
    @ValueSource(ints = {6, 8})
    public void negative_test_get_cards_invalid_cif(int invalidCIFLength) {
        setupTestUser();
        TEST("AHBDB-6385  get debit card details of a user with an invalid cif length 400 : " + invalidCIFLength);
        GIVEN("I have a valid customer with accounts scope");

        String cif = RandomDataGenerator.generateRandomNumeric(invalidCIFLength);
        AND("I have the customers invalid CIF : " + cif);

        WHEN("User makes a call to get their cards using the protected endpoint with an invalid cif length");
        THEN("A 404 is returned by the service");
        this.cardsApiFlows.getCardsErrorProtected(cif, "debit", 404);

        DONE();
    }

    @Order(3)
    @ParameterizedTest()
    @ValueSource(strings = {"oneTwoT", "124 532"})
    public void negative_test_get_cards_invalid_cif(String invalidCIF) {
        setupTestUser();
        TEST("AHBDB-6385  get debit card details of a user with an invalid cif 400 : " + invalidCIF);
        GIVEN("I have a valid customer with accounts scope");

        AND("I have the customers invalid CIF : " + invalidCIF);

        WHEN("User makes a call to get their cards using the protected endpoint with an invalid cif length");
        THEN("A 404 is returned by the service");
        this.cardsApiFlows.getCardsErrorProtected(invalidCIF, "debit", 404);

        DONE();
    }


    @Order(4)
    @Test
    public void negative_test_get_cards_user_has_invalid_token_protected() {
        //AHBDB-12950 - Test failing - fixed by updating dev user


        setupTestUser();
        TEST("AHBDB-6385  get debit card details of a user with a incorrect scope");
        GIVEN("I have a valid customer with an invalid token");
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to get their cards using the protected endpoint");
        this.cardsApiFlows.getCardsErrorNoApiKey(cif, "debit", 401);

        THEN("A 401 is returned by the service");
        DONE();
    }
}
