package uk.co.deloitte.banking.cards.scenarios.kids.updates;

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

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class FetchCardsKidsTests {

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

    private void setupUserChild(OBGender obGender, AlphaTestUser alphaTestUserParent) {
        //WIP
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

        setUpCardFOrKid();

    }

    private void setupUserChildNoCard(OBGender obGender, AlphaTestUser alphaTestUserParent) {
        this.alphaTestUserChild = new AlphaTestUser();
        this.alphaTestUserChild.setGender(obGender);
        this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, relationshipId, dependantId);

        alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserParent, relationshipId);
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

    private void setUpCardFOrKid() {
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();

        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUserFather, relationshipId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
    }

    @Order(1)
    @Test()
    public void positive_test_parent_can_create_virtual_debit_card_for_daughter_father_12_years_old() {
        TEST("AHBDB-16557- Defect raised as it is failing only in cIT environment");
        envUtils.ignoreTestInEnv(Environments.CIT);
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter father");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 12);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);
        AND("The kid has a bank account set up");
        WHEN("They create a virtual card with valid values using their bank account");
        THEN("A virtual card is created for the user with a 201 response");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertNotNull(cards);

        THEN("The parent can view the relationship card");
        ReadCard1 readCard1 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUserFather, relationshipId, "debit", 200);
        Assertions.assertEquals(readCard1.getData().getReadCard1DataCard().get(0).getCardNumber(), cards.getData().getReadCard1DataCard().get(0).getCardNumber());

        DONE();

    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"debat", "", "cash"})
    public void negative_test_parent_kid_fetch_cards_invalid_card_type(String invalidType) {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter father");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 16);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);
        AND("The kid has a bank account set up");
        WHEN("They create a virtual card with valid values using their bank account");
        THEN("A virtual card is created for the user with a 201 response");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertNotNull(cards);

        THEN("A 400 is returned from the service due to an invalid type being used : " + invalidType);
        this.cardsRelationshipApi.fetchCardForRelationshipError(alphaTestUserFather, relationshipId, invalidType, 400);

        DONE();

    }

    @Order(3)
    @Test
    public void negative_test_parent_kid_fetch_cards_for_kid_no_card() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter father");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 16);
        setupUserChildNoCard(OBGender.FEMALE, alphaTestUserFather);
        AND("The kid has a bank account set up");

        THEN("A 404 is returned from the service as the kid has no card");
        this.cardsRelationshipApi.fetchCardForRelationshipError(alphaTestUserFather, relationshipId, "debit", 404);

        DONE();

    }

    @Order(4)
    @Test
    public void negative_test_parent_kid_fetch_cards_invalid_token() {
        TEST("AHBDB-7422 Issue Virtual Card for kid by onboarded parent daughter father");
        TEST("AHBDB-9383 Fetch Card List for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 16);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);
        AND("The kid has a bank account set up");
        WHEN("They create a virtual card with valid values using their bank account");
        THEN("A virtual card is created for the user with a 201 response");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        Assertions.assertNotNull(cards);

        AND("The parents token is set to an invalid value");
        alphaTestUserFather.getLoginResponse().setAccessToken("invalid");

        THEN("Then a 401 is returned by the service");
        this.cardsRelationshipApi.fetchCardForRelationshipErrorVoid(alphaTestUserFather, relationshipId, "debit", 401);

        DONE();

    }

}
