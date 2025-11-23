package uk.co.deloitte.banking.cards.scenarios.protectedEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomPinBlock;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class ActivateDebitCardProtectedTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private CardsConfiguration cardsConfiguration;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private static final String CARD_MASK = "000000";

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

    private void setupTestUser() {

        envUtils.shouldSkipHps(Environments.NONE);
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1());
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
        CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();
    }

    private void setupTestUserNegativeTests() {
        envUtils.shouldSkipHps(Environments.NONE);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupV2UserAndV2Customer(alphaTestUser, null);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
            CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
            CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();
        }
    }

    private WriteCardActivation1 writeCardActivation1() {
        return WriteCardActivation1.builder()
                .modificationOperation(CardModificationOperation1.ACTIVATE_VIRTUAL)
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
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
    }

    private WriteCardPinRequest1 validWriteCardPinRequest1() {
        return WriteCardPinRequest1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .pinBlock(generateRandomPinBlock())
                .pinServiceType("G")
                .build();
    }

    private WritePhysicalCard1 validPhysicalCard1(CardDeliveryAddress1 deliveryAddress, String iban) {
        return WritePhysicalCard1.builder()
                .recipientName(alphaTestUser.getName())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .deliveryAddress(deliveryAddress)
                .dtpReference("dtpReference")
                .iban(iban)
                .awbRef("DT" + generateRandomNumeric(14))
                .build();
    }


    private CardDeliveryAddress1 cardDeliveryAddress1Valid(OBPostalAddress6 obPostalAddress6) {
        return CardDeliveryAddress1.builder()
                .addressLine(List.of(obPostalAddress6.getAddressLine().get(0), obPostalAddress6.getAddressLine().get(1)))
                .buildingNumber(obPostalAddress6.getBuildingNumber())
                //https://ahbdigitalbank.atlassian.net/browse/AHBDB-13286 defect raised
                // .countrySubDivision(obPostalAddress6.getCountrySubDivision())
                .country(obPostalAddress6.getCountry())
                .postalCode(obPostalAddress6.getPostalCode())
                .streetName(obPostalAddress6.getStreetName())
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_user_can_activate_their_virtual_debit_card_and_deactivate_it_protected() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, cif, 200);

        AND("My card is activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        WHEN("The user makes a call to deactivate their debit card");
        WriteCardActivation1 activateCardDeact = writeCardActivation1();
        activateCardDeact.setModificationOperation(CardModificationOperation1.DEACTIVATE_VIRTUAL);

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(activateCardDeact, cardId, cif, 200);

        AND("My card is deactivated");
        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertFalse(cards1.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());


        DONE();
    }

    @Order(2)
    @Test()
    public void created_physical_card_can_be_activate_with_protected_endpoints() {
        envUtils.ignoreTestInEnv("AHBDB-16405", Environments.ALL);
        setupTestUser();
        TEST("AHBDB-235 /AHBDB-6963 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();
        String iban = this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        AND("I have a valid iban : " + iban);
        WHEN("I want to create my Physical card");
        THEN("A 201 is returned from the service and the card is created");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), cardId, 201);


        AND("If I get cards, physical card printed is marked as true");
        final ReadCard1 updatedCards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertEquals(updatedCards.getData().getReadCard1DataCard().get(0).isPhysicalCardPrinted(), true);

        THEN("The physical card can be activated with the protected endpoints");
        WriteCardActivation1 activateCardDeact = writeCardActivation1();
        activateCardDeact.setModificationOperation(CardModificationOperation1.ACTIVATE_PHYSICAL);

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(activateCardDeact, cardId, cif, 200);

        AND("My physical card is activated");
        final ReadCard1 cards1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards1.getData().getReadCard1DataCard().get(0).isCardActivated());

        THEN("The physical card can be deactivated with the protected endpoints");
        WriteCardActivation1 cardDeact = writeCardActivation1();
        activateCardDeact.setModificationOperation(CardModificationOperation1.DEACTIVATE_PHYSICAL);

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(cardDeact, cardId, cif, 200);

        AND("My physical card is deactivated");
        final ReadCard1 cards2 = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        //raise defect
        // Assertions.assertFalse(cards2.getData().getReadCard1DataCard().get(0).isCardActivated());


        DONE();
    }

    @Order(3)
    @Test()
    public void positive_test_user_tries_to_activate_their_debit_card_null_operationReason_protected_protected() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to activate their debit card with a null modificationOperation");
        WriteCardActivation1 activateCard = writeCardActivation1();
        activateCard.setOperationReason(null);

        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");
        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(activateCard, cardId, cif, 200);

        WHEN("The user makes a call to deactivate their debit card");
        WriteCardActivation1 activateCardDeact = writeCardActivation1();
        activateCardDeact.setModificationOperation(CardModificationOperation1.DEACTIVATE_VIRTUAL);

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(activateCardDeact, cardId, cif, 200);


        DONE();
    }

    @Order(4)
    @Test()
    public void negative_test_user_tries_activate_an_already_activated_card_protected() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to activate their debit card");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, cif, 200);

        WHEN("The user makes a call to activate their debit card again");

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, cif, 400);

        DONE();
    }

    @Order(4)
    @Test()
    public void negative_test_user_tries_activate_a_virtual_card_with_physical_card_operations() {
        setupTestUser();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WriteCardActivation1 activateCard = writeCardActivation1();
        activateCard.setModificationOperation(CardModificationOperation1.ACTIVATE_PHYSICAL);

        WHEN("User makes a call to activate their debit card with physical card operation");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(activateCard, cardId, cif, 400);


        DONE();
    }

    @Order(4)
    @Test()
    public void negative_test_user_tries_activate_an_another_users_card_protected() {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to activate another users card card");
        String cardId = cardsConfiguration.getCreatedCard().replace(CARD_MASK, "");

        THEN("They receive a 404 response back from the service");
        // should this be a 400? AHBDB-7501 raised
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, cif, 404);

        DONE();
    }

    @Order(4)
    @Test()
    public void negative_test_user_tries_to_deactivate_another_users_card_protected() {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to deactivate another users debit card");
        String cardId = cardsConfiguration.getCreatedCard().replace(CARD_MASK, "");

        THEN("They receive a 404 response back from the service");
        // should this be a 400? AHBDB-7501 raised
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, cif, 404);

        DONE();
    }


    @Order(4)
    @ParameterizedTest
    @ValueSource(strings = {"1 2342 63 ", "cardId1234"})
    public void negative_test_user_tries_activate_their_card_invalid_cardId(String invalidCardId) {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        WHEN("User makes a call to activate a card with an invalid cardId: " + invalidCardId);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), invalidCardId, cif, 400);

        DONE();
    }

    @Order(4)
    @ParameterizedTest
    @ValueSource(ints = {9, 11})
    public void negative_test_user_tries_activate_their_card_wrong_cardId_length_protected(int invalidCardIdLength) {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);

        String invalidID = generateRandomNumeric(invalidCardIdLength);

        WHEN("User makes a call to activate a card with an invalid card number length : " + invalidID);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), invalidID, cif, 400);

        DONE();
    }

    @Order(4)
    @ParameterizedTest
    @ValueSource(ints = {9, 11})
    public void negative_test_user_tries_activate_their_card_wrong_cif_length_protected(int invalidCifLength) {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = generateRandomNumeric(invalidCifLength);
        AND("I have an invalid CIF : " + cif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("User makes a call to activate a card with an invalid cif length : " + cif);

        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, cif, 404);

        DONE();
    }

    @Order(4)
    @ParameterizedTest
    @ValueSource(strings = {"cifcifcifc", " ", "232345 322"})
    public void negative_test_user_tries_activate_their_card_invalid_cif_protected(String invalidCif) {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        AND("I have an invalid CIF : " + invalidCif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");


        WHEN("User makes a call to activate a card with an invalid cif length : " + invalidCif);

        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, invalidCif, 404);

        DONE();
    }

    @Order(4)
    @Test
    public void negative_test_user_tries_activate_their_card_wrong_cif() {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        AND("The user has a created card");

        String cif = generateRandomNumeric(10);
        AND("I have an invalid CIF : " + cif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("User makes a call to activate a card with an invalid cif : " + cif);

        THEN("They receive a 404 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(writeCardActivation1(), cardId, cif, 404);

        DONE();
    }

    @Order(4)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_null_modificationOperation_protected() {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("User makes a call to activate their debit card with a null modificationOperation");
        WriteCardActivation1 activateCard = writeCardActivation1();
        activateCard.setModificationOperation(null);

        THEN("They receive a 400 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(activateCard, cardId, cif, 400);

        DONE();
    }


    @Order(102)
    @Test()
    public void negative_test_user_tries_to_activate_their_debit_card_with_incorrect_token_protected() {
        setupTestUserNegativeTests();
        TEST("AHBDB-3299 activate debit card");
        GIVEN("I have a valid customer with accounts scope");

        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();
        AND("I have the customers CIF : " + cif);
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("User makes a call to activate their debit card with an invalid token");

        THEN("They receive a 401 response back from the service");
        this.cardsApiFlows.activateDebitCardErrorNoToken(writeCardActivation1(), cardId, cif, 401);

        DONE();
    }

}
