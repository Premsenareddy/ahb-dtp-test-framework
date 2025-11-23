package uk.co.deloitte.banking.cards.scenarios.kids.virtual;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
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
import static uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1.ACTIVATE_VIRTUAL;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.TEEN_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class ActivateVirtualCardsKidsTests {

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
    private CardsRelationshipApi cardsRelationshipApi;

    @Inject
    private CustomerApiV2 customerApiV2;

    private static final String CARD_MASK = "000000";

    private static final String NULL_ERROR = "must not be null";

    private static final String ACCOUNT_NOT_USERS = "Account doesn't belong us user";

    static String relationshipId;
    static String otpCode;
    private String dependantId = "";


    private static final int loginMinWeightExpected = 31;

    private AlphaTestUser alphaTestUserChild;

    private final static String TEMPORARY_PASSWORD = "validtestpassword";

    private AlphaTestUser alphaTestUserMother;

    private AlphaTestUser alphaTestUserFather;

    private AlphaTestUser alphaTestUser;


    private void setupFatherTestUser(OBGender kidGender, OBRelationshipRole obRelationshipRoleKid, int age) {
        //creates an account

        this.alphaTestUserFather = new AlphaTestUser();
        this.alphaTestUserFather = alphaTestUserFactory.setupCustomer(alphaTestUserFather);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserFather);
        createUserRelationship(alphaTestUserFather);
        createDependentCustomer(alphaTestUserFather, OBRelationshipRole.FATHER, kidGender, obRelationshipRoleKid, age);


    }

    private void setupMotherTestUser(OBGender kidGender, OBRelationshipRole obRelationshipRoleKid, int age) {
        //creates an account
        this.alphaTestUserMother = new AlphaTestUser();
        this.alphaTestUserMother.setGender(OBGender.FEMALE);
        this.alphaTestUserMother = alphaTestUserFactory.setupCustomer(alphaTestUserMother);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserMother);
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
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);


    }

    private void setupUserChildNegative(OBGender obGender, AlphaTestUser alphaTestUserParent) {
        if (this.alphaTestUserChild == null) {
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild.setGender(obGender);
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, relationshipId, dependantId);

            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, relationshipId);
            OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                    OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
            assertNotNull(savings);
            OBWriteAccountResponse1Data data = savings.getData();
            assertNotNull(data.getAccountId());
            alphaTestUserChild.setAccountNumber(data.getAccountId());

            CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
            final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserMother, relationshipId, validCreateCard1);
        }
    }

    private void setupMotherTestUserNegative(OBGender kidGender, OBRelationshipRole obRelationshipRoleKid, int age) {

        if (this.alphaTestUserMother == null) {
            this.alphaTestUserMother = new AlphaTestUser();
            this.alphaTestUserMother.setGender(OBGender.FEMALE);
            this.alphaTestUserMother = alphaTestUserFactory.setupCustomer(alphaTestUserMother);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserMother);
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
                .modificationOperation(ACTIVATE_VIRTUAL)
                .operationReason("Operation reason")
                .build();
    }


    @Order(1)
    @Test()
    public void positive_test_parent_can_activate_virtual_debit_card_for_daughter_father_12_years_old() {
        envUtils.ignoreTestInEnv("AHBDB-16405", Environments.ALL);
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent daughter 12 year old father");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 12);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_SEGHAAR_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 201 response");
        CreateCard1 validCreateCard1 = validCreateCard1(CardProduct.DIGITAL_SEGHAAR_PINK);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserFather, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
        String cardId = createCardResponse.getData().getCardNumber().replace(CARD_MASK, "");

        AND("The parent makes a request to activate their child's card");
        OBReadRelationship1 relationship = this.relationshipApi.getRelationships(alphaTestUserFather);

        THEN("They receive a 200 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUserFather, validActivateCard1(), relationshipId, cardId, 200);

        AND("The kid can log on and view their card and its activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        DONE();

    }

    @Order(2)
    @Test()
    public void positive_test_parent_can_activate_virtual_debit_card_for_daughter_mother_8_years_old() {
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent daughter mother 8 year old");
        setupMotherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 16);
        setupUserChild(OBGender.FEMALE, alphaTestUserMother);

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
        CreateCard1 validCreateCard1 = validCreateCard1(CardProduct.TEEN_DIGITAL_DC_PLATINUM);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserMother, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
        String cardId = createCardResponse.getData().getCardNumber().replace(CARD_MASK, "");

        AND("The parent makes a request to activate their child's card");

        THEN("They receive a 200 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUserMother, validActivateCard1(), relationshipId, cardId, 200);

        AND("The kid can log on and view their card and its activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());
        DONE();
    }


    @Order(3)
    @Test()
    public void positive_test_parent_can_activate_virtual_debit_card_for_son_mother_14_years_old() {
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent son mother 14 year old");
        setupMotherTestUser(OBGender.MALE, OBRelationshipRole.SON, 14);
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
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
        String cardId = createCardResponse.getData().getCardNumber().replace(CARD_MASK, "");

        AND("The parent makes a request to activate their child's card");

        THEN("They receive a 200 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUserMother, validActivateCard1(), relationshipId, cardId, 200);

        AND("The kid can log on and view their card and its activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());
        DONE();
    }

    @Order(4)
    @Test()
    public void positive_test_parent_can_activate_virtual_debit_card_for_son_father_17_years_old() {
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent son father 17 year old");
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
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
        String cardId = createCardResponse.getData().getCardNumber().replace(CARD_MASK, "");

        AND("The parent makes a request to activate their child's card");

        THEN("They receive a 200 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUserMother, validActivateCard1(), relationshipId, cardId, 200);

        AND("The kid can log on and view their card and its activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());
        DONE();

    }

    @Order(5)
    @Test()
    public void negative_test_adult_with_no_relationship_cant_activate_card() {
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent");
        setupMotherTestUser(OBGender.MALE, OBRelationshipRole.SON, 16);
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
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
        String cardId = createCardResponse.getData().getCardNumber().replace(CARD_MASK, "");

        THEN("The adult with no relationship tries to activate the kids card");
        setupUserNoChild();

        THEN("They receive a 400 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationshipError(alphaTestUser, validActivateCard1(), relationshipId, cardId, 403);

        DONE();
    }


    @Order(6)
    @Test()
    public void negative_onboarded_kid_cant_activate_card_for_themselves() {
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent");
        setupMotherTestUser(OBGender.MALE, OBRelationshipRole.SON, 14);
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


        WHEN("and a virtual card is created");
        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserMother, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
        String cardId = createCardResponse.getData().getCardNumber().replace(CARD_MASK, "");

        AND("The kid tries to activate their own card");

        THEN("They receive a 403 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUserChild, validActivateCard1(), relationshipId, cardId, 403);

        DONE();
    }

    @Order(7)
    @Test()
    public void negative_invalid_null_modification_operation() {
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("and a virtual card is created");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertNotNull(cards);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber();

        AND("the activation request is invalid modification operation reason");
        WriteCardActivation1 activateCard1 = validActivateCard1();
        activateCard1.setModificationOperation(null);

        THEN("They receive a 400 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationshipError(alphaTestUserMother, activateCard1, relationshipId, cardId, 400);

        DONE();
    }

    @Order(8)
    @Test()
    public void negative_invalid_null_operation_reason() {
        TEST("AHBDB-7428 Activate kids debit card by onboarded parent");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("virtual card is created");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertNotNull(cards);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber();

        AND("the activation request is invalid operation reason");
        WriteCardActivation1 activateCard1 = validActivateCard1();
        activateCard1.setOperationReason(null);

        THEN("They receive a 400 response back from the service");
        this.cardsRelationshipApi.activateDebitCardForRelationshipError(alphaTestUserMother, activateCard1, relationshipId, cardId, 400);

        DONE();
    }

}
