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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.card.model.limits.WriteCardLimits1;
import uk.co.deloitte.banking.account.api.card.model.limits.WriteCardLimits1Data;
import uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
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
public class UpdateAndFetchLimitsKidsTests {

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
    private CardsConfiguration cardsConfiguration;

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

    private WriteCardLimits1 validWriteCardLimits1(String dailyLimit, String monthlyLimit) {
        return WriteCardLimits1.builder()
                .data(WriteCardLimits1Data.builder()
                        .dailyAtmLimit(dailyLimit)
                        .dailyEcommLimit(dailyLimit)
                        .dailyPosLimit(dailyLimit)
                        .monthlyAtmLimit(monthlyLimit)
                        .monthlyEcommLimit(monthlyLimit)
                        .monthlyPosLimit(monthlyLimit)
                        .build())
                .build();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"purchase", "withdrawal", "ecommerce"})
    public void positive_test_parent_can_update_all_limits_for_kid_and_get_limits(String validTypeAll) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("The parent can update all of the kids limits");
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions for the following type : " + validTypeAll);
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, validTypeAll, 200);

        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();

    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"purchase", "withdrawal", "ecommerce"})
    public void positive_test_parent_can_update_monthly_limits_for_kid_and_get_limits(String validTypeMonthly) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("The parent can update monthly only of the kids limits");
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WriteCardLimits1 writeCardLimits1 = validWriteCardLimits1(dailyLimit, monthlyLimit);
        writeCardLimits1.getData().setDailyAtmLimit(null);
        writeCardLimits1.getData().setDailyPosLimit(null);
        writeCardLimits1.getData().setDailyEcommLimit(null);

        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, writeCardLimits1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions for the following type : " + validTypeMonthly);
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, validTypeMonthly, 200);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();

    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"purchase", "withdrawal", "ecommerce"})
    public void positive_test_parent_can_update_daily_limits_for_kid_and_get_limits(String validTypeDaily) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("The parent can update monthly only of the kids limits");
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WriteCardLimits1 writeCardLimits1 = validWriteCardLimits1(dailyLimit, monthlyLimit);
        writeCardLimits1.getData().setMonthlyAtmLimit(null);
        writeCardLimits1.getData().setMonthlyPosLimit(null);
        writeCardLimits1.getData().setMonthlyEcommLimit(null);

        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, writeCardLimits1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions for the following type : " + validTypeDaily);
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, validTypeDaily, 200);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();

    }

    @Order(1)
    @Test
    public void positive_test_parent_can_update_daily_and_monthly_limits_for_one_type_only_atmLimit() {
        TEST("AHBDB-16557- defect raised as it is failing only in CIT environment");
        envUtils.ignoreTestInEnv(Environments.CIT);
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("The parent can update monthly only of the kids limits");
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WriteCardLimits1 writeCardLimits1 = validWriteCardLimits1(dailyLimit, monthlyLimit);
        writeCardLimits1.getData().setMonthlyEcommLimit(null);
        writeCardLimits1.getData().setDailyEcommLimit(null);
        writeCardLimits1.getData().setDailyPosLimit(null);
        writeCardLimits1.getData().setMonthlyPosLimit(null);


        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, writeCardLimits1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "withdrawal", 200);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits1.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        THEN("only the atm limits are updated kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits2 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "purchase", 200);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        THEN("only the atm limits are updated kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits3 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "ecommerce", 200);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();

    }

    @Order(1)
    @Test
    public void positive_test_parent_can_update_daily_and_monthly_limits_for_one_type_only_ecomLimit() {
        TEST("AHBDB-16557- defect raised as it is failing only in CIT environment");
        envUtils.ignoreTestInEnv(Environments.CIT);
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("The parent can update monthly only of the kids limits");
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WriteCardLimits1 writeCardLimits1 = validWriteCardLimits1(dailyLimit, monthlyLimit);
        writeCardLimits1.getData().setMonthlyAtmLimit(null);
        writeCardLimits1.getData().setDailyAtmLimit(null);
        writeCardLimits1.getData().setDailyPosLimit(null);
        writeCardLimits1.getData().setMonthlyPosLimit(null);


        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, writeCardLimits1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "withdrawal", 200);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        THEN("only the atm limits are updated kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits2 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "purchase", 200);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits2.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        THEN("only the atm limits are updated kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits3 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "ecommerce", 200);
        Assertions.assertEquals(readCardLimits3.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits3.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits3.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits3.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits3.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits3.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits3.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();

    }

    @Order(1)
    @Test
    public void positive_test_parent_can_update_daily_and_monthly_limits_for_one_type_only_purchaseLimit() {
        TEST("AHBDB-16557- defect raised as it is failing only in CIT environment");
        envUtils.ignoreTestInEnv(Environments.CIT);
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        THEN("The parent can update monthly only of the kids limits");
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WriteCardLimits1 writeCardLimits1 = validWriteCardLimits1(dailyLimit, monthlyLimit);
        writeCardLimits1.getData().setMonthlyAtmLimit(null);
        writeCardLimits1.getData().setDailyAtmLimit(null);
        writeCardLimits1.getData().setDailyEcommLimit(null);
        writeCardLimits1.getData().setMonthlyEcommLimit(null);


        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, writeCardLimits1, relationshipId, cardId, 200);

        THEN("The kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "withdrawal", 200);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits1.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        THEN("only the atm limits are updated kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits2 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "purchase", 200);
        Assertions.assertEquals(readCardLimits2.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits2.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits2.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits2.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(readCardLimits2.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits2.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(readCardLimits2.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        THEN("only the atm limits are updated kid can view the update cards restrictions for the following type atmLimit");
        final ReadCardLimits1 readCardLimits3 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUserFather, relationshipId, cardId, "ecommerce", 200);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertNotEquals(readCardLimits3.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();

    }

    @Order(2)
    @Test
    public void negative_test_user_with_no_relationship_cant_update_Limits() {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WHEN("A user with no relationship tries to update the limits");
        setupUserNoChild();
        THEN("A 403 is returned by the service");
        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUser, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, cardId, 403);

        DONE();

    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"purchase", "withdrawal", "ecommerce"})
    public void negative_test_user_with_no_relationship_cant_view_updated_limits(String userWithNoRelationshipLimitType) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, cardId, 200);

        WHEN("A user with no relationship tries to fetch the updated limits : " + userWithNoRelationshipLimitType);
        setupUserNoChild();
        THEN("A 403 is returned by the service");
        this.cardsRelationshipApi.getRelationshipCardsLimitsError(alphaTestUser, relationshipId, cardId, userWithNoRelationshipLimitType, 403);

        DONE();

    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"123423232", "        ", "45422233324"})
    public void negative_parent_tries_to_update_limits_invalid_cardId(String invalidCardId) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WHEN("A user a parent tries to update the limits with an invalid cardId :" + invalidCardId);
        THEN("A 400 is returned by the service");
        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, invalidCardId, 400);

        DONE();

    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"CardID", ")(*&^%"})
    public void negative_parent_tries_to_update_limits_invalid_cardId_content(String invalidCardId) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WHEN("A user a parent tries to update the limits with an invalid cardId :" + invalidCardId);
        THEN("A 400 is returned by the service");
        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, invalidCardId, 400);

        DONE();

    }

    @Order(2)
    @Test
    public void negative_parent_tries_to_update_limits_someone_else_cardId() {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        String cardId = cardsConfiguration.getCreatedCard().replace(CARD_MASK, "");

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WHEN("A user a parent tries to update the limits for another users card :" + cardId);
        THEN("A 404 is returned by the service");
        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, cardId, 404);

        DONE();

    }

    @Order(2)
    @ParameterizedTest
    @CsvSource({"purchase, 9", "purchase, 11", "withdrawal, 9", "withdrawal, 11", "ecommerce, 9", "ecommerce, 11"})
    public void negative_test_parent_with_invalid_cardId_cant_view_LimitsString(String validType, String invalidCardIdLength) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, cardId, 200);

        String invalidId = RandomDataGenerator.generateRandomNumeric(Integer.parseInt(invalidCardIdLength));

        WHEN(String.format("A parent tries to fetch transactionType %s  with cardId %s", validType, invalidId));

        THEN("A 400 is returned by the service");
        this.cardsRelationshipApi.getRelationshipCardsLimitsError(alphaTestUserFather, relationshipId, cardId, invalidId, 400);

        DONE();

    }

    @Order(100)
    @Test
    public void negative_test_user_with_invalid_token_cant_update_limits() {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");


        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        WHEN("A parent with an invalid token tries to update the limits");
        alphaTestUserFather.getLoginResponse().setAccessToken("invalid");

        THEN("A 401 is returned by the service");
        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUserFather, validWriteCardLimits1(dailyLimit, monthlyLimit), relationshipId, cardId, 401);

        DONE();

    }

    @Order(100)
    @ParameterizedTest
    @ValueSource(strings = {"purchase", "withdrawal", "ecommerce"})
    public void negative_test_user_with_invalid_token_cant_view_the_limits(String limitType) {
        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
        setupUserChild(OBGender.FEMALE, alphaTestUserFather);

        GIVEN("there is a valid parent child relationship and a virtual card created");

        AND("The kid can log on and view their card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");

        WHEN("A parent with an invalid token tries to view the limits");
        alphaTestUserFather.getLoginResponse().setAccessToken("invalid");

        THEN("A 401 is returned by the service");
        this.cardsRelationshipApi.getRelationshipCardsLimitsErrorVoid(alphaTestUserFather, relationshipId, cardId, limitType, 401);

        DONE();

    }

//
//    @Order(2)
//    @ParameterizedTest
//    @ValueSource(strings = {"CARDIDD ", "      ", "$%^HNE"})
//    public void negative_parent_tries_to_update_card_invalid_cardId_content(String invalidCardIdContent) {
//        TEST("AHBDB-8857 Update debit card limits on behalf of kid by onboarded parent");
//        TEST("AHBDB-8858 Fetch debit card limits for kid by onboarded parent");
//
//        setupFatherTestUser(OBGender.FEMALE, OBRelationshipRole.DAUGHTER, 15);
//        setupUserChild(OBGender.FEMALE, alphaTestUserFather);
//
//        GIVEN("there is a valid parent child relationship and a virtual card created");
//
//        AND("The kid can log on and view their card");
//        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUserChild);
//
//        THEN("A parent tries to update the card with an invalid cardId : " + invalidCardIdContent);
//
//        THEN("The service returns a 400");
//
//
//        DONE();
//
//    }
//


}
