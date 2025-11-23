package uk.co.deloitte.banking.cards.scenarios.kids.virtual;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
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
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.TEEN_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomNumeric;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CreateVirtualCardsAndFetchKidsTests {

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

    private static final String CARD_MASK = "000000";

    private static final String NULL_ERROR = "must not be null";

    private static final String ACCOUNT_NOT_USERS = "Account doesn't belong us user";

    static String relationshipId;
    private String dependantId = "";


    private AlphaTestUser alphaTestUserChild;

    private AlphaTestUser alphaTestUserMother;

    private AlphaTestUser alphaTestUserFather;

    private AlphaTestUser alphaTestUser;

    private String CREATED_CARD_NUMBER = null;
    private String CREATE_CARD_EXP_DATE = null;
    private String CARD_ID = null;

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

    @Order(1)
    @Test()
    public void positive_test_parent_can_create_virtual_debit_card_for_daughter_father_12_years_old() {
        envUtils.ignoreTestInEnv("AHBDB-16405", Environments.ALL);
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter father");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");
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

        OBReadCustomer1 relationship = this.relationshipApi.getChildBasedOnRelationship(alphaTestUserFather, relationshipId);
        assertEquals(dependantId, relationship.getData().getCustomer().get(0).getCustomerId().toString());

        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 201 response");
        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserFather, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertFalse(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        THEN("The parent can view the relationship card");
        ReadCard1 readCard1 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUserFather, relationshipId, "debit", 200);
        Assertions.assertEquals(readCard1.getData().getReadCard1DataCard().get(0).getCardNumber(), cards.getData().getReadCard1DataCard().get(0).getCardNumber());

        DONE();

    }

    @Order(1)
    @Test()
    public void positive_test_parent_can_create_virtual_debit_card_for_daughter_mother_8_years_old() {
        envUtils.ignoreTestInEnv("AHBDB-16405", Environments.ALL);
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");

        setupMotherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 8);
        setupUserChild(OBGender.FEMALE, alphaTestUserMother);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_SEGHAAR_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using their bank account");

        THEN("A virtual card is created for the user with a 201 response");
        CreateCard1 validCreateCard1 = validCreateCard1(CardProduct.DIGITAL_SEGHAAR_PINK);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserMother, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertFalse(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        THEN("The parent can view the relationship card");
        ReadCard1 readCard1 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUserFather, relationshipId, "debit", 200);
        Assertions.assertEquals(readCard1.getData().getReadCard1DataCard().get(0).getCardNumber(), cards.getData().getReadCard1DataCard().get(0).getCardNumber());

        DONE();
    }


    @Order(1)
    @Test()
    public void positive_test_kid_who_has_created_card_can_view_it() {
        envUtils.ignoreTestInEnv("AHBDB-16405", Environments.ALL);
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");
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

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertFalse(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        THEN("The parent can view the relationship card");
        ReadCard1 readCard1 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUserMother, relationshipId, "debit", 200);
        Assertions.assertEquals(readCard1.getData().getReadCard1DataCard().get(0).getCardNumber(), cards.getData().getReadCard1DataCard().get(0).getCardNumber());

        DONE();
    }

    @Order(1)
    @Test()
    public void positive_test_parent_can_create_virtual_debit_card_for_son_father_17_years_old() {
        envUtils.ignoreTestInEnv("AHBDB-16405", Environments.ALL);
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");
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

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        String createdCardNumber = createCardResponse.getData().getCardNumber();
        Assertions.assertEquals(createCardResponse.getData().getCardNumber(), createdCardNumber);
        Assertions.assertFalse(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        THEN("The parent can view the relationship card");
        ReadCard1 readCard1 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUserMother, relationshipId, "debit", 200);
        Assertions.assertEquals(readCard1.getData().getReadCard1DataCard().get(0).getCardNumber(), cards.getData().getReadCard1DataCard().get(0).getCardNumber());

        DONE();
    }

    @Order(2)
    @Test()
    public void negative_onboarded_kid_cant_create_card_for_themselves() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
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


        WHEN("They create a virtual card with valid values themselves");

        THEN("A virtual card is not created with a 403");
        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, validCreateCard1, 403);
        DONE();
    }


    @Order(2)
    @Test()
    public void negative_test_user_without_dependent_cant_create_their_card() {
        envUtils.ignoreTestInEnv("AHBDB-16405", Environments.ALL);
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        TEST("AHBDB-11009 - defect");

        setupMotherTestUser(OBGender.MALE, OBRelationshipRole.SON, 12);
        setupUserChild(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_SEGHAAR_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using their bank account");
        CreateCard1 validCreateCard1 = validCreateCard1(CardProduct.DIGITAL_SEGHAAR_BLUE);

        AND("The the user without a dependent tries to activate their card");
        setupUserNoChild();

        THEN("A 403 status is returned to the user");
        final OBErrorResponse1 createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUser, relationshipId, validCreateCard1, 403);

        DONE();

    }

    @Order(100)
    @Test()
    public void negative_invalid_accountType() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using their bank account");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setAccountType("INVALIDTYPE");

        THEN("the service returns a 400");

        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();
    }

    @Order(200)
    @Test()
    public void negative_invalid_null_embossedName() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with invalid null name");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().setEmbossedName(null);

        THEN("the service returns a 400");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();
    }

    @Order(200)
    @Test()
    public void negative_invalid_null_currency() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with invalid null currency");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setAccountCurrency(null);

        THEN("the service returns a 400");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();
    }

    @Order(200)
    @Test()
    public void negative_invalid_null_accountName() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");
        OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data.getAccountId());
        alphaTestUserChild.setAccountNumber(data.getAccountId());

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with invalid null accountName");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setAccountName(null);

        THEN("the service returns a 400");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();
    }

    @Order(200)
    @Test()
    public void negative_invalid_null_accountNumber() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");


        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with invalid null accountNumber");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setAccountNumber(null);
        THEN("the service returns a 400");

        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();
    }

    @Order(200)
    @Test()
    public void negative_invalid_null_accountType() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");


        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with invalid null accountType");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setAccountType(null);

        THEN("the service returns a 400");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();

    }

    @Order(200)
    @Test()
    public void negative_invalid_null_openDate() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with invalid null openDate");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setOpenDate(null);


        THEN("the service returns a 400");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();

    }

    @Order(200)
    @Test()
    public void negative_invalid_null_seqNumber() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");


        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with invalid null seqNumber");
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setSeqNumber(null);


        THEN("the service returns a 400");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 400);
        DONE();

    }


    @Order(200)
    @Test()
    public void negative_invalid_random_account_number() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");


        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        String randomCardAccount = generateRandomNumeric(12);
        WHEN("They create a virtual card with random account number : " + randomCardAccount);
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setAccountNumber(randomCardAccount);


        THEN("the service returns a 403");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 403);
        DONE();

    }

    @Order(200)
    @Test()
    public void negative_invalid_another_users_account() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter mother");
        setupMotherTestUserNegative(OBGender.MALE, OBRelationshipRole.SON, 14);
        setupUserChildNegative(OBGender.MALE, alphaTestUserMother);

        AND("The kid has a bank account set up");


        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        String otherUsersAccountNumber = temenosConfig.getCreditorIban();
        WHEN("They create a virtual card with random account number : " + otherUsersAccountNumber);
        final CreateCard1 createCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);
        createCard1.getData().getAccounts().get(0).setAccountNumber(otherUsersAccountNumber);

        THEN("the service returns a 403");
        final OBErrorResponse1 obErrorResponse1 = this.cardsRelationshipApi.createVirtualDebitCardForRelationshipError(alphaTestUserChild, relationshipId, createCard1, 403);
        DONE();

    }

}
