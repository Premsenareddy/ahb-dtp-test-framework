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
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.parameters.WriteCardParameters1;
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
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class UpdateReadParametersKidsTests {

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
        if (this.alphaTestUserChild == null) {
            //creates an account

            this.alphaTestUserFather = new AlphaTestUser();
            this.alphaTestUserFather = alphaTestUserFactory.setupCustomer(alphaTestUserFather);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(alphaTestUserFather);
            createUserRelationship(alphaTestUserFather);
            createDependentCustomer(alphaTestUserFather, OBRelationshipRole.FATHER, kidGender, obRelationshipRoleKid, age);

        }
    }


    private void setupUserChild(OBGender obGender, AlphaTestUser alphaTestUserParent) {
        if (this.alphaTestUserChild == null) {
            //WIP
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
        }
    }

    private void setupUserNoChild() {

        //creates an account
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);


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


    private WriteCardParameters1 validWriteCardParameters1() {
        return WriteCardParameters1.builder()
                .internationalUsage(false)
                .internetUsage(false)
                .nationalDisATM(false)
                .nationalPOS(false)
                .nationalUsage(false)
                .nationalSwitch(false)
                .operationReason("operation reason")
                .build();
    }

    @Order(1)
    @Test()
    public void positive_test_parent_can_turn_off_everything_for_kid() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("The parent can update the kids card parameters with 200");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUserFather, validWriteCardParameters1(), relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUserChild, cardId);
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalPOS(), "N");
        //  assertEquals(cardFilter1.getData().getNationalDisATM(), "N");

        DONE();

    }

    @Test()
    @Order(1)
    public void positive_test_parent_can_turn_off_international_for_kid() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        AND("The parent wants to turn off international usage");
        WriteCardParameters1 writeCardParameters1 = validWriteCardParameters1();
        writeCardParameters1.setInternetUsage(true);
        writeCardParameters1.setNationalPOS(true);
        writeCardParameters1.setNationalDisATM(true);
        writeCardParameters1.setNationalSwitch(true);
        writeCardParameters1.setNationalUsage(true);

        THEN("The parent can update the kids card parameters with 200");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUserFather, writeCardParameters1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUserChild, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter1.getData().getNationalPOS(), "Y");
        //  assertEquals(cardFilter1.getData().getNationalDisATM(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "Y");

        DONE();

    }

    @Test()
    @Order(1)
    public void positive_test_parent_can_turn_off_internet_for_kid() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        AND("The parent wants to turn off internet usage");
        WriteCardParameters1 writeCardParameters1 = validWriteCardParameters1();
        writeCardParameters1.setInternationalUsage(true);
        writeCardParameters1.setNationalPOS(true);
        writeCardParameters1.setNationalDisATM(true);
        writeCardParameters1.setNationalSwitch(true);
        writeCardParameters1.setNationalUsage(true);

        THEN("The parent can update the kids card parameters with 200");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUserFather, writeCardParameters1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUserChild, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalPOS(), "Y");
        //  assertEquals(cardFilter1.getData().getNationalDisATM(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "Y");

        DONE();

    }

    @Test()
    @Order(1)
    public void positive_test_parent_can_turn_off_NationalPOS_for_kid() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        AND("The parent wants to turn off NationalPOS usage");
        WriteCardParameters1 writeCardParameters1 = validWriteCardParameters1();
        writeCardParameters1.setInternationalUsage(true);
        writeCardParameters1.setInternetUsage(true);
        writeCardParameters1.setNationalDisATM(true);
        writeCardParameters1.setNationalSwitch(true);
        writeCardParameters1.setNationalUsage(true);

        THEN("The parent can update the kids card parameters with 200");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUserFather, writeCardParameters1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUserChild, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter1.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter1.getData().getNationalPOS(), "N");
        //  assertEquals(cardFilter1.getData().getNationalDisATM(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "Y");

        DONE();

    }

    @Test()
    @Order(1)
    public void positive_test_parent_can_turn_off_NationalDisATM_for_kid() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        AND("The parent wants to turn off NationalDisATM usage");
        WriteCardParameters1 writeCardParameters1 = validWriteCardParameters1();
        writeCardParameters1.setInternationalUsage(true);
        writeCardParameters1.setInternetUsage(true);
        writeCardParameters1.setNationalPOS(true);
        writeCardParameters1.setNationalSwitch(true);
        writeCardParameters1.setNationalUsage(true);

        THEN("The parent can update the kids card parameters with 200");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUserFather, writeCardParameters1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUserChild, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter1.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter1.getData().getNationalPOS(), "Y");
        //  assertEquals(cardFilter1.getData().getNationalDisATM(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "Y");

        DONE();

    }

    @Test()
    @Order(1)
    public void positive_test_parent_can_turn_off_NationalUsage_for_kid() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        AND("The parent wants to turn off NationalUsage usage");
        WriteCardParameters1 writeCardParameters1 = validWriteCardParameters1();
        writeCardParameters1.setInternationalUsage(true);
        writeCardParameters1.setInternetUsage(true);
        writeCardParameters1.setNationalPOS(true);
        writeCardParameters1.setNationalDisATM(true);
        writeCardParameters1.setNationalSwitch(true);

        THEN("The parent can update the kids card parameters with 200");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUserFather, writeCardParameters1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUserChild, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter1.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter1.getData().getNationalPOS(), "Y");
        //  assertEquals(cardFilter1.getData().getNationalDisATM(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        DONE();

    }


    @Order(2)
    @Test()
    public void negative_test_person_without_relationship_cant_update_kids_card() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("A user without the relationship trues to update the card parameters");
        setupUserNoChild();

        THEN("The service returns a 403");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUser, validWriteCardParameters1(), relationshipId, cardId, 403);

        DONE();

    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"123425674", "24444444444"})
    public void negative_parent_tries_to_update_card_invalid_cardId(String invalidCardId) {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        THEN("A parent tries to update the card with an invalid cardId : " + invalidCardId);

        THEN("The service returns a 400");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUser, validWriteCardParameters1(), relationshipId, invalidCardId, 400);

        DONE();

    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"CARDIDD ", "      ", "$%^HNE"})
    public void negative_parent_tries_to_update_card_invalid_cardId_content(String invalidCardIdContent) {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        THEN("A parent tries to update the card with an invalid cardId : " + invalidCardIdContent);

        THEN("The service returns a 400");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUser, validWriteCardParameters1(), relationshipId, invalidCardIdContent, 400);

        DONE();

    }

    @Order(100)
    @Test
    public void negative_parent_tries_to_update_card_invalid_token() {
        TEST("AHBDB-8856 Update card parameters on behalf of kid by onboarded parent");

        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        AND("They have an invalid token");
        alphaTestUserFather.getLoginResponse().setAccessToken("invalid");

        THEN("The service returns a 401");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUserFather, validWriteCardParameters1(), relationshipId, cardId, 401);

        DONE();

    }

    @Order(1)
    @Test()
    public void positive_test_parent_get_card_parameters_for_kid() {
        envUtils.ignoreTestInEnv("Not deployed in SIT and above yet", Environments.SIT,
                Environments.NFT);

        TEST("AHBDB-15736: Get card parameters by relationship");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        final ReadCardParameters1 cardFilter1 =
                this.cardsRelationshipApi.getCardParametersRelationship(alphaTestUserFather, relationshipId, cardId);

        THEN("The parent can also log on and view their card parameters");
        assertNotNull(cardFilter1.getData().getInternetUsage());
        assertNotNull(cardFilter1.getData().getNationalUsage());
        assertNotNull(cardFilter1.getData().getInternationalUsage());
        assertNotNull(cardFilter1.getData().getNationalPOS());
        assertNotNull(cardFilter1.getData().getMaxATMLimit());
        assertNotNull(cardFilter1.getData().getMaxPOSLimit());
        assertNotNull(cardFilter1.getData().getMaxEcommerceLimit());
        assertNotNull(cardFilter1.getData().getMonthlyATMLimit());
        assertNotNull(cardFilter1.getData().getDailyATMLimit());
        assertNotNull(cardFilter1.getData().getMonthlyEcommerceLimit());

        DONE();

    }

}
