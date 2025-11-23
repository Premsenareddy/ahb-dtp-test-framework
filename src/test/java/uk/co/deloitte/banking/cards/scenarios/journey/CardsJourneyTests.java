package uk.co.deloitte.banking.cards.scenarios.journey;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.parameters.WriteCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ModificationOperation;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.filters.UpdateCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.TransactionType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomTownName;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CardsJourneyTests {

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
    private AccountApi accountApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private BankingConfig bankingConfig;

    @Inject
    TripleDesUtil tripleDesUtil;

    private static final String CARD_MASK = "000000";

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {

        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

        }
    }

    private void logExistingIn() {
        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            alphaTestUser.setUserId(bankingConfig.getBankingUserUserId());
            alphaTestUser.setUserPassword(bankingConfig.getBankingUserPassword());
            alphaTestUser.setAccountNumber(bankingConfig.getBankingUserAccountNumber());
            alphaTestUser.setDeviceId(bankingConfig.getBankingUserDeviceId());
            UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                    .userId(alphaTestUser.getUserId())
                    .password(alphaTestUser.getUserPassword())
                    .build();
            UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginExistingUserProtected(userLoginRequestV2, alphaTestUser);
            parseLoginResponse(alphaTestUser, userLoginResponseV2);
        }
    }

    @Order(1)
    @Test()
    public void positive_test_create_virtual_debit_card() {
        setupTestUser();
        TEST("AHBDB-228 / AHBDB-6963 create virtual debit cards");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 200 response");
        CreateCard1 validCreateCard1 = CreateCard1.builder()
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

        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        AND("The card is created for the user");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
        CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();

        DONE();
    }

    @Order(2)
    @Test()
    public void create_physical_card() {
        setupTestUser();
        TEST("AHBDB-235 /AHBDB-6963 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();
        String iban = this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        AND("I have a valid iban : " + iban);
        WHEN("I want to create my Physical card");

        CardDeliveryAddress1 cardDeliveryAddress1Valid = CardDeliveryAddress1.builder()
                .addressLine(List.of(obPostalAddress6.getAddressLine().get(0), obPostalAddress6.getAddressLine().get(1)))
                .buildingNumber(obPostalAddress6.getBuildingNumber())
                .townName(generateRandomTownName())
                //https://ahbdigitalbank.atlassian.net/browse/AHBDB-13286 defect raised  - fixed
                .countrySubDivision(obPostalAddress6.getCountrySubDivision())
                .country(obPostalAddress6.getCountry())
                .postalCode(obPostalAddress6.getPostalCode())
                .streetName(obPostalAddress6.getStreetName())
                .build();


        WritePhysicalCard1 validPhysicalCard1 =
                WritePhysicalCard1.builder()
                        .recipientName(alphaTestUser.getName())
                        .phoneNumber(alphaTestUser.getUserTelephone())
                        .deliveryAddress(cardDeliveryAddress1Valid)
                        .dtpReference("dtpReference")
                        .iban(iban)
                        .awbRef("DT" + generateRandomNumeric(14))
                        .build();


        THEN("A 201 is returned from the service and the card is created");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, validPhysicalCard1, cardId, 201);

        AND("If I get cards, physical card printed is marked as true");
        final ReadCard1 updatedCards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(updatedCards.getData().getReadCard1DataCard().get(0).isPhysicalCardPrinted());

        DONE();
    }

    @Order(3)
    @Test()
    public void positive_test_user_can_activate_their_physical_debit_card() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");
        ActivateCard1 validActivateCard1 = ActivateCard1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .modificationOperation(ModificationOperation.A)
                .operationReason("Operation reason")
                .build();


        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1, 200);

        AND("My card is activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        DONE();
    }

    @Order(5)
    @Test()
    public void positive_test_get_cardCVV() {
        setupTestUser();
        TEST("AHBDB-3414 get debit card cvv for a valid user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        final ReadCardCvv1 cardCVvDetails = this.cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        String cvv = cardCVvDetails.getData().getCvv();
        Assertions.assertNotNull(cvv);
        Assertions.assertNotNull(cardCVvDetails.getData().getCardNumber());
        THEN("The encrypted CVV can be retrieved using the created cardId : " + cvv);

        DONE();
    }

    @Order(6)
    @Test()
    public void user_can_set_pin_on_card() throws Throwable {
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");
        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        String cardId = cards1.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("User makes a call to set their pin");

        WriteCardPinRequest1 writeCardPinRequest1 = WriteCardPinRequest1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .pinBlock(pinBlock)
                .pinServiceType("G")
                .build();


        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.setDebitCardPin(alphaTestUser, writeCardPinRequest1, 200);

        AND("My pin is marked as set");
        final ReadCard1 readCard1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(readCard1.getData().getReadCard1DataCard().get(0).getIsPintSet());
    }

    @Order(7)
    @Test()
    public void positive_test_user_updates_their_spending_limits() {
        TEST("AHBDB-13273 - defect raised");
        envUtils.shouldSkipHps(Environments.ALL);
        TEST("HPS no longer support monthly limit changes, leaving tests here but disabled in case they revert");
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
        WriteDailyMonthlyLimits1Data writeDailyMonthlyLimits1DataValid = WriteDailyMonthlyLimits1Data.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .dailyAtmLimit(dailyLimit)
                .dailyPosLimit(dailyLimit)
                .monthlyAtmLimit(monthlyLimit)
                .monthlyPosLimit(monthlyLimit)
                .dailyEcommLimit(dailyLimit)
                .monthlyEcommLimit(monthlyLimit)
                .build();

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1Valid = WriteDailyMonthlyLimits1.builder().data(writeDailyMonthlyLimits1DataValid).build();

        THEN("They receive a 200 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1Valid, 200);

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

    @Order(7)
    @Test()
    public void positive_test_user_switches_off_and_on_online_and_abroad_payments() {
        TEST("AHBDB-7506 defect fix");
        setupTestUser();
        TEST("AHBDB-3295 Switch On/Off online payments for debit card");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to switch off online payments debit card");

        UpdateCardParameters1 validUpdateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internationalUsage(false)
                .internetUsage(false)
                .build();

        THEN("A 200 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, validUpdateCardParameters1, 200);

        AND("The users card is shown as having online payments blocked");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");


        AND("The user can turn back on online payments");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internetUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("online payments is turned back on");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");

        DONE();
    }

    @Order(8)
    @Test()
    public void positive_test_user_blocks_their_card() {
        TEST("AHBDB-7506 defect fix");
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to block debit card");
        UpdateCardParameters1 validUpdateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .nationalUsage(false)
                .nationalPOS(false)
                .nationalDisATM(false)
                .internationalUsage(false)
                .internetUsage(false)
                .operationReason("operation reason")
                .build();


        THEN("A 200 is returned from the service");
        final UpdateCardParameters1 blockCardParams = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internationalUsage(false)
                .internetUsage(false)
                .nationalDisATM(false)
                .nationalSwitch(false)
                .nationalPOS(false)
                .nationalUsage(false)
                .operationReason("blockCard")
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, blockCardParams, 200);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        AND("The user can unblock their card");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("their card is unblocked");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");
        DONE();
    }




    @Order(10)
    @Test()
    public void positive_test_user_blocks_their_card_using_protected_endpoints() {
        setupTestUser();
        TEST("AHBDB-295 block debit card for user");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        THEN("They receive a 200 response back from the service");
        AND("They make a valid request to block debit card");
        WriteCardParameters1 validUpdateCardParameters2 = WriteCardParameters1.builder()
                .nationalUsage(false)
                .nationalPOS(false)
                .nationalDisATM(false)
                .internationalUsage(false)
                .internetUsage(false)
                .operationReason("operation reason")
                .build();

        THEN("A 200 is returned from the service");

        cardsApiFlows.blockCardProtected(validUpdateCardParameters2, cardId, cif, 200);

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

    @Order(11)
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

    @Order(12)
    @Test()
    public void positive_test_user_can_deactivate_debit_card_protected() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("The user makes a call to deactivate their debit card");
        WriteCardActivation1 writeCardActivation1 =
                WriteCardActivation1.builder()
                        .modificationOperation(CardModificationOperation1.DEACTIVATE_VIRTUAL)
                        .operationReason("Operation reason")
                        .build();

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1, cardId, cif, 200);

        AND("My card is deactivated");
        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertFalse(cards1.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        DONE();
    }

}
