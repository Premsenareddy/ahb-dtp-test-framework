package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ModificationOperation;
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
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomAlphanumericUpperCase;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CreatePhysicalCardTests {

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
    private CardsConfiguration cardsConfiguration;

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


            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser,
                    validCreateCard1());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
            CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
            CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();

            this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 200);

        }

    }
    private void setupTestUserRefresh() {

        envUtils.shouldSkipHps(Environments.NONE);

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);


            final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser,
                    validCreateCard1());
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
            CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
            CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();

            this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1(), 200);


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
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30",
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
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
                .addressLine(List.of(obPostalAddress6.getAddressLine().get(0),
                        obPostalAddress6.getAddressLine().get(1)))
                .buildingNumber(obPostalAddress6.getBuildingNumber())
                .townName(obPostalAddress6.getTownName())
                .countrySubDivision(obPostalAddress6.getCountrySubDivision())
                .country(obPostalAddress6.getCountry())
                .postalCode(obPostalAddress6.getPostalCode())
                .streetName(obPostalAddress6.getStreetName())
                .build();
    }


    @Order(1)
    @Test
    public void positive_create_physical_card_null_address() {
        TEST("AHBDB-16556- defect raised as it is failing only in cIT environment");
        envUtils.ignoreTestInEnv(Environments.CIT);
        TEST("AHBDB-7419 - test case updated");
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");
        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser,
                validCreateCard1());

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);
        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

//        WHEN("I want to create my Physical card with null postal code");
//        CardDeliveryAddress1 cardDeliveryAddress1 = CardDeliveryAddress1.builder()
//                .addressLine(null)
//                .buildingNumber(null)
//                .countrySubDivision(null)
//                .country(null)
//                .postalCode(null)
//                .streetName(null)
//                .build();

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();


        THEN("A 201 is returned as the address is a non mandatory field");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), cardId,
                201);

        this.cardsApiFlows.createPhysicalCard(alphaTestUser, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), cardId,
                409);

        DONE();
    }


    @Order(2)
    @Test()
    public void negative_create_physical_card_null_recipientName() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();
        WHEN("I want to create my Physical card with null recipient name");


        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        writePhysicalCard1.setRecipientName(null);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_create_physical_card_null_phoneNumber() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();
        WHEN("I want to create my Physical card with null phone number");

        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        writePhysicalCard1.setPhoneNumber(null);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_create_physical_card_null_obPostalAddress6() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();
        WHEN("I want to create my Physical card null postal address");

        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        writePhysicalCard1.setDeliveryAddress(null);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_create_physical_card_null_iban() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("I want to create my Physical card with null iban");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        writePhysicalCard1.setIban(null);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_create_physical_card_null_dtpReference() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("I want to create my Physical card with null dtp reference");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        writePhysicalCard1.setDtpReference(null);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_create_physical_card_null_awbRef() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();
        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("I want to create my Physical card with null awbRef");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        writePhysicalCard1.setAwbRef(null);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {9, 11})
    public void negative_create_physical_card_invalid_card_id_length(int invalidCardIdLength) {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();
        String invalidId = generateRandomNumeric(invalidCardIdLength);

        WHEN("I want to create my Physical card with and invalid card id length : " + invalidId);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser,
                validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), invalidId, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"fkjeifj wd", "^&$%^&*()_"})
    public void negative_create_physical_card_invalid_cardId(String invalidCardId) {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();


        WHEN("I want to create my Physical card with and invalid card id length : " + invalidCardId);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser,
                validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), invalidCardId, 400);

        DONE();
    }


    @Order(2)
    @Test
    public void negative_create_physical_card_invalid_cardId_in_url() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        String randomCardId = generateRandomNumeric(16);

        WHEN("I want to create my Physical card with a random card number : " + randomCardId);

        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser,
                validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), randomCardId, 400);

        DONE();
    }

    @Order(2)
    @Test
    public void negative_create_physical_card_invalid_random_iban() {

        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        String randomIban = generateRandomAlphanumericUpperCase(16);

        WHEN("I want to create my Physical card with a random IBAN : " + randomIban);
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban);
        writePhysicalCard1.setIban(randomIban);


        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"fkjeifj wd    djkfkj 23424145", "^&$%^&*()_(*&^%$"})
    public void negative_create_physical_card_invalid_iban_characters(String invalidIbanCharacters) {

        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");


        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);
        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();

        WHEN("I want to create my Physical card with an invalid IBAN : " + invalidIbanCharacters);
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6),
                invalidIbanCharacters);


        THEN("A 400 is returned");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, writePhysicalCard1, cardId, 400);

        DONE();
    }

    @Test
    public void negative_physical_card_already_issued() {
        TEST("AHBDB-16556- defect raised as it is failing only in CIT environment");
        envUtils.ignoreTestInEnv(Environments.CIT);
        setupTestUserRefresh();
        TEST("AHBDB-12163 - Physical card already issued");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");
        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser,
                validCreateCard1());

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);
        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();


        THEN("A 201 is returned for the first card");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), cardId,
                201);

        THEN("A 409 is returned for the second card request");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), cardId,
                409);

        DONE();
    }


    @Order(100)
    @Test()
    public void negative_create_physical_card_invalid_token() {
        setupTestUser();
        TEST("AHBDB-235 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 =
                this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();
        String iban =
                this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        AND("I have a valid iban : " + iban);
        WHEN("I want to create my Physical card with an invalid token");
        alphaTestUser.getLoginResponse().setAccessToken("invalid");
        THEN("A 401 is returned from the service and the card is created");
        this.cardsApiFlows.createPhysicalCard(alphaTestUser,
                validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban), cardId, 401);


        DONE();
    }

}
