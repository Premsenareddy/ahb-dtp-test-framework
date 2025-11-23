package uk.co.deloitte.banking.cards.scenarios.kids.physical;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
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
import uk.co.deloitte.banking.cards.api.CardsRelationshipApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1.ACTIVATE_PHYSICAL_VIRTUAL;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.TEEN_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CreatePhysicalCardsKidsTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private CardsRelationshipApi cardsRelationshipApi;

    private static final String CARD_MASK = "000000";

    private static final String NULL_ERROR = "must not be null";

    private static final String ACCOUNT_NOT_USERS = "Account doesn't belong us user";


    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;


    static String relationshipId;
    private String dependantId = "";

    private AlphaTestUser alphaTestUserChild;

    private AlphaTestUser alphaTestUserMother;

    private AlphaTestUser alphaTestUserFather;

    private AlphaTestUser alphaTestUser;


    @BeforeEach()
    public void init() {
        // Ignoring all as the first test which set up the users is failing due to TIMEOUT raised as part of AHBDB-14965 - all the other tests are failing because of this
        // envUtils.ignoreTestInEnv("AHBDB-14965", Environments.ALL);
    }

    private void setupFatherTestUser(OBGender kidGender, OBRelationshipRole obRelationshipRoleKid, int age) {
        //creates an account

        this.alphaTestUserFather = new AlphaTestUser();
        this.alphaTestUserFather = alphaTestUserFactory.setupCustomer(alphaTestUserFather);
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(alphaTestUserFather);
        createUserRelationship(alphaTestUserFather);
        createDependentCustomer(alphaTestUserFather, OBRelationshipRole.FATHER, kidGender, obRelationshipRoleKid, age);


    }

    private void setupMotherTestUser(OBGender kidGender, OBRelationshipRole obRelationshipRoleKid, int age) {
        //creates an account
        this.alphaTestUserMother = new AlphaTestUser();
        this.alphaTestUserMother.setGender(OBGender.FEMALE);
        this.alphaTestUserMother = alphaTestUserFactory.setupCustomer(alphaTestUserMother);
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(alphaTestUserMother);
        createUserRelationship(alphaTestUserMother);
        createDependentCustomer(alphaTestUserMother, OBRelationshipRole.MOTHER, kidGender, obRelationshipRoleKid, age);


    }

    private void setupUserChild(OBGender obGender, AlphaTestUser alphaTestUserParent) {
        //WIP
        this.alphaTestUserChild = new AlphaTestUser();
        this.alphaTestUserChild.setGender(obGender);
        this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, relationshipId, dependantId);

        alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, relationshipId);

    }

    private void setupUserNoChild() {

        //creates an account
        alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(alphaTestUser);


    }

    private void setupUserChildNegative(OBGender obGender, AlphaTestUser alphaTestUserParent) {
        if (this.alphaTestUserChild == null) {
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild.setGender(obGender);
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, relationshipId, dependantId);

            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, relationshipId);

            AND("The kid has a bank account set up");
            OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                    OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
            assertNotNull(savings);
            OBWriteAccountResponse1Data data = savings.getData();
            assertNotNull(data.getAccountId());
            alphaTestUserChild.setAccountNumber(data.getAccountId());

            OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
            relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();

            WHEN("They create a virtual card with valid values using their bank account");

            THEN("A virtual card is created for the user with a 201 response");
            CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

            final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserMother, relationshipId, validCreateCard1);
            Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);

            WHEN("User makes a call to get their cards to ESB / HPS");
            final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

            CREATED_CARD_NUMBER = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
            CREATE_CARD_EXP_DATE = cards.getData().getReadCard1DataCard().get(0).getExpiryDate();
        }
    }

    private void setupMotherTestUserNegative(OBGender kidGender, OBRelationshipRole obRelationshipRoleKid, int age) {

        if (this.alphaTestUserMother == null) {
            this.alphaTestUserMother = new AlphaTestUser();
            this.alphaTestUserMother.setGender(OBGender.FEMALE);
            this.alphaTestUserMother = alphaTestUserFactory.setupCustomer(alphaTestUserMother);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(alphaTestUserMother);
            createUserRelationship(alphaTestUserMother);
            createDependentCustomer(alphaTestUserMother, OBRelationshipRole.MOTHER, kidGender, obRelationshipRoleKid, age);
        }
    }


    private void createUserRelationship(AlphaTestUser alphaTestUser) {
        UserRelationshipWriteRequest request = UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        assertNotNull(response);
        assertNotNull(response.getUserId());
        dependantId = response.getUserId();
    }

    private void createDependentCustomer(AlphaTestUser alphaTestUser, OBRelationshipRole obRelationshipRole, OBGender obGender, OBRelationshipRole obRelationshipRoleKid, int age) {
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(age))
                        .fullName("dependent full name")
                        .gender(obGender)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(obRelationshipRole)
                        .dependantRole(obRelationshipRoleKid)
                        .build())
                .build();
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        assertNotNull(response);
        assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();

    }


    private CreateCard1 validCreateCard1(CardProduct cardProduct) {
        return CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(cardProduct)
                        .embossedName(alphaTestUserChild.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(alphaTestUserChild.getAccountNumber())
                                .accountType(AccountType.CURRENT.getDtpValue())
                                .openDate(LocalDateTime.now())
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
    }

    private WriteCardActivation1 validActivateCard1() {
        return WriteCardActivation1.builder()
                .modificationOperation(ACTIVATE_PHYSICAL_VIRTUAL)
                .operationReason("Operation reason")
                .build();
    }

    private WritePhysicalCard1 validPhysicalCard1(CardDeliveryAddress1 deliveryAddress, String iban, AlphaTestUser alphaTestUser) {
        return WritePhysicalCard1.builder()
                .recipientName(alphaTestUserChild.getName())
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
                .countrySubDivision(obPostalAddress6.getCountrySubDivision())
                .country(obPostalAddress6.getCountry())
                .postalCode(obPostalAddress6.getPostalCode())
                .streetName(obPostalAddress6.getStreetName())
                .townName(obPostalAddress6.getCountrySubDivision())
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_parent_can_order_physical_debit_card_for_daughter_father_15_years_old_() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        //CB issue with age groups?
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 201 response");
        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserFather, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();


        WHEN("The parent wants to order a physical card");

        THEN("a 201 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserFather, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather), relationshipId, cardId, 201);

        AND("The kid can log on and view their physical card as ordered");
        final ReadCard1 readCard1 = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(readCard1.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        Assertions.assertTrue(readCard1.getData().getReadCard1DataCard().get(0).isPhysicalCardPrinted());

        THEN("The parent can activate the physical card");
        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUserFather, validActivateCard1(), relationshipId, cardId, 200);

        AND("The kid can log on and view their physical card as ordered");
        final ReadCard1 readCard2 = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(readCard2.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        Assertions.assertTrue(readCard2.getData().getReadCard1DataCard().get(0).isCardActivated());

        DONE();

    }


    @Order(2)
    @Test()
    public void negative_test_user_with_no_relationship_cant_order_physical_card() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUser(OBGender.MALE, OBRelationshipRole.SON, 17);
        setupUserChild(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 201 response");
        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserMother, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("A user with no relationship tries to order the physical card");
        setupUserNoChild();

        THEN("a 403 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUser, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserMother), relationshipId, cardId, 403);

        DONE();

    }

    @Order(2)
    @Test()
    public void negative_test_kid_cant_activate_it_themselves_on_relationship_endPoint() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUser(OBGender.MALE, OBRelationshipRole.SON, 17);
        setupUserChild(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 201 response");
        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserMother, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("the kid tries to activate the card themselves");

        THEN("a 403 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserChild, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserMother), relationshipId, cardId, 403);

        DONE();

    }

    @Order(3)
    @Test()
    public void negative_test_parent_cant_order_card_null_name() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with null name");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather);
        writePhysicalCard1.setRecipientName(null);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, writePhysicalCard1 , relationshipId, cardId, 400);

        DONE();

    }

    @Order(3)
    @Test()
    public void negative_test_parent_cant_order_card_null_phoneNumber() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with null phonenumber");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather);
        writePhysicalCard1.setPhoneNumber(null);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, writePhysicalCard1 , relationshipId, cardId, 400);

        DONE();

    }

    @Order(3)
    @Test()
    public void negative_test_parent_cant_order_card_null_delivery_address() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with null delivery address");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather);
        writePhysicalCard1.setDeliveryAddress(null);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, writePhysicalCard1 , relationshipId, cardId, 400);

        DONE();

    }

    @Order(3)
    @Test()
    public void negative_test_parent_cant_order_card_null_iban() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with null iban");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather);
        writePhysicalCard1.setIban(null);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, writePhysicalCard1 , relationshipId, cardId, 400);

        DONE();

    }


    @Order(3)
    @Test()
    public void negative_test_parent_cant_order_card_null_dtp_reference() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with null dtp reference");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather);
        writePhysicalCard1.setRecipientName(null);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, writePhysicalCard1 , relationshipId, cardId, 400);

        DONE();

    }

    @Order(3)
    @Test()
    public void negative_test_parent_cant_order_card_null_awb_reference() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with null awb reference");
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather);
        writePhysicalCard1.setAwbRef(null);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, writePhysicalCard1 , relationshipId, cardId, 400);

        DONE();

    }

    @Order(3)
    @Test()
    public void negative_test_parent_cant_order_card_other_users_iban() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = temenosConfig.getCreditorIban();

        WHEN("The parent wants to order a physical card with another users iban : " + iban);
        WritePhysicalCard1 writePhysicalCard1 = validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather);
        writePhysicalCard1.setIban(null);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, writePhysicalCard1 , relationshipId, cardId, 400);

        DONE();

    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"123425674", "24444444444"})
    public void negative_test_parent_cant_order_card_invalid_cardId(String invalidCardId) {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with invalid cardId : " + invalidCardId);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather) , relationshipId, invalidCardId, 400);

        DONE();

    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"cardId", "GJJD0kfw6899)(*&"})
    public void negative_test_parent_cant_order_card_invalid_cardId_content(String invalidCardId) {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with invalid cardId : " + invalidCardId);

        THEN("a 400 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather) , relationshipId, invalidCardId, 400);

        DONE();

    }

    @Order(100)
    @Test
    public void negative_test_parent_cant_order_card_invalid_token() {
        TEST("AHBDB-7423 Request Physical Card for kid by onboarded parent");
        setupMotherTestUserNegative(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChildNegative(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getAddress();

        String iban = this.accountApi.getAccountDetails(alphaTestUserChild, alphaTestUserChild.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card with invalid token");
        alphaTestUserMother.getLoginResponse().setAccessToken("invalid");

        THEN("a 401 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUserMother, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUserFather) , relationshipId, cardId, 401);

        DONE();

    }

}
