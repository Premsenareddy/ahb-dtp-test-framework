package uk.co.deloitte.banking.sit;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.limits.WriteCardLimits1;
import uk.co.deloitte.banking.account.api.card.model.limits.WriteCardLimits1Data;
import uk.co.deloitte.banking.account.api.card.model.parameters.WriteCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadDataTransaction6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventV1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseTypeEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ProcessOriginEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ReasonEnum;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes.TripleDesUtil;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.api.RelationshipAccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.cards.api.CardsRelationshipApi;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBFatcaForm1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatca1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatcaDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBReadLocationResponse1;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.cases.api.CasesApi;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;
import uk.co.deloitte.banking.documents.api.DocumentRelationshipAdapterApi;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1.ACTIVATE_PHYSICAL_VIRTUAL;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.TEEN_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.customer.api.customer.model.OBTermType.BANKING;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EKids {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private OtpApi otpApi;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private SanctionsApi sanctionsApi;

    @Inject
    private FatcaApiV2 fatcaApiV2;

    @Inject
    private DocumentRelationshipAdapterApi documentRelationshipApi;

    @Inject
    private CasesApi casesApi;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

    @Inject
    private RelationshipAccountApi relationshipAccountApi;

    @Inject
    private LocationsApiV2 locationsApiV2;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private CardsRelationshipApi cardsRelationshipApi;

    @Inject
    private AccountApi accountApi;

    @Inject
    private TripleDesUtil tripleDesUtil;

    @Inject
    protected TemenosConfig temenosConfig;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private CardProtectedApi cardProtectedApi;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    private static AlphaTestUser alphaTestUser;
    private static AlphaTestUser alphaTestUserChild;

    private static String childId;
    private static String connectionId;

    private static String TEMPORARY_PASSWORD = "temporary_password";

    private static final String CARD_MASK = "000000";
    private static CreateCard1Response createCardResponse;
    private static String cardId;
    private static ReadCardCvv1 cardCVvDetails;

    private static String CREATED_CARD_NUMBER = null;
    private static String CREATE_CARD_EXP_DATE = null;

    private static final int loginMinWeightExpected = 31;

    public void ignoreAllSwitch() {
        /**
         * TODO :: SIT E2E ONLY, also ignored due to too many transact tests
         */
        envUtils.ignoreTestInEnv(Environments.ALL);
    }


    @Test
    @Order(1)
    @Tag("Individual")
    void child_created_in_ForgeRock_success() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11975: Happy Path Scenario Child is created in ForgeRock");
        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword(TEMPORARY_PASSWORD).build();

        GIVEN("A customer exists.");
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);

        WHEN("Calling post relationship from authenticate api with a temporary password.");
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        THEN("Status code 201 is returned.");
        assertNotNull(response);

        AND("The userId of the newly created user is returned.");
        assertNotNull(response.getUserId());
        childId = response.getUserId();

        DONE();
    }

    @Test
    @Order(2)
    @Tag("Individual")
    public void happy_path_create_child_in_CRM() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11976: Happy Path Scenario Child is created in CRM");
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");
        AND("The child exists in Forgerock with a UserID");

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(childId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.MOTHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();
        WHEN("The client attempts to create a child account on CRM with a child userID that has a paired relationship with the parents userID (in Redis)");
        OBReadRelationship1 createResponse = this.relationshipApi.createDependant(this.alphaTestUser, obWriteDependant1);
        THEN("The platform will create the child record and store the relevant information");
        AND("We will return a 201 created response");
        AND("We will create the relationship");
        AND("We will return the customerID");
        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(alphaTestUser);
        assertEquals(childId, getResponse.getData().getRelationships().get(0).getCustomerId().toString());
        connectionId = createResponse.getData().getRelationships().get(0).getConnectionId().toString();

    }

    @Test
    @Order(3)
    public void verify_relationship_between_parent_and_child() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11977: Happy Path Scenario Relationship between Parent and Child is Verified");

        GIVEN("A get relationship request is sent");
        WHEN("The userID of the parent extracted from the token is valid");

        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(alphaTestUser);

        THEN("The existing customer relationship is retrieved successfully");
        AND("The platform responds with 200 OK");
        AND("All existing relationships with its corresponding connectionID will be returned");
        AND("The child's userID is seen");

        assertEquals(alphaTestUser.getUserId(), getResponse.getData().getCustomerId().toString());
        assertEquals(childId, getResponse.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(connectionId, getResponse.getData().getRelationships().get(0).getConnectionId().toString());
        assertEquals(1, getResponse.getData().getRelationships().size());

        DONE();
    }

    @Test
    @Order(4)
    @Tag("Individual")
    public void happy_path_register_child_device() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11978: Happy Path Scenario Child's Device is Registered - Success'");

        GIVEN("The access tokens for a child are valid");
        WHEN("The deviceId is sent in the POST request");

        otpApi.sentChildOTPCode(alphaTestUser, 204, connectionId);

        OtpCO otpCO = this.developmentSimulatorService.retrieveOtpFromDevSimulator(childId);
        assertNotNull(otpCO);

        THEN("They will retrieve the OTP from their phone: " + otpCO.getPassword());
        assertNotNull(otpCO.getPassword());
        String otpCode = otpCO.getPassword();

        assertEquals(OtpType.TEXT.toString(), otpCO.getType().toString());
        assertEquals(alphaTestUser.getUserTelephone(), otpCO.getDestination());

        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(childId)
                .password(TEMPORARY_PASSWORD)
                .otp(otpCode)
                .build();

        this.alphaTestUserChild = new AlphaTestUser();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(this.alphaTestUserChild, request);

        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(this.alphaTestUserChild, userLoginResponseV2);
        this.alphaTestUserChild.setUserPassword(TEMPORARY_PASSWORD);
        this.alphaTestUserChild.setUserId(childId);
        this.alphaTestUserChild.setCustomerId(childId);

        alphaTestUserChild = alphaTestUserFactory.setupUserCerts(alphaTestUserChild);

        THEN("The deviceId will be posted and stored in ForgeRock");
        AND("The platform will return a 200 OK");
        AND("The device's certificate will be stored in the Certificate Service");
        AND("The device status is set to active");

        assertNotNull(alphaTestUserChild);

        OBReadCustomer1 getResponse = this.customerApi.getCurrentCustomer(alphaTestUserChild);
        assertEquals(alphaTestUser.getUserId(), getResponse.getData().getCustomer().get(0).getOnboardedBy());

        DONE();
    }

    @Test
    @Order(5)
    @Tag("Individual")
    public void happy_path_change_posting_restrictions_to_VALID() {
        ignoreAllSwitch();
        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        THEN("The platform will set the EIDStatus against the CustomerID sent in the path");
        AND("The platform will push an event to Kafka with the customer's EIDStatus, CustomerID and CIF");
        this.customerApi.updateCustomerValidations(alphaTestUserChild, eidStatus);
    }

    @Test
    @Order(5)
    public void happy_path_accept_terms_201_response() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11979: Happy Path Scenario Child's T&Cs Registered - Success'");

        GIVEN("An onboarded parent is registering a banking child");
        OBWriteTerm1 obWriteTerm = OBWriteTerm1.builder()
                .termsAccepted(true)
                .termsAcceptedDate(OffsetDateTime.parse("2021-06-26T12:30:24+04:00"))
                .termsVersion(LocalDate.now())
                .type(BANKING)
                .build();

        WHEN("He/she accepts the T&Cs on child’s behalf");
        THEN("The customer endpoint should be called");
        final OBWriteTerm1 response =
                this.relationshipApi.createDependantTerm(alphaTestUser, connectionId, obWriteTerm).getData();

        AND("The T&Cs data will be saved against the child’s userID");
        AND("201 Create Response will be returned");

        assertNotNull(response);
        assertEquals(response.getType(), OBTermType.BANKING);

        DONE();
    }

    @Test
    @Order(6)
    @Tag("Individual")
    public void happy_path_store_first_password_for_child() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11981: Happy Path Scenario Set Passcode for Child - Success");
        GIVEN("A child is registering their device with a new password");
        WHEN("The client updates the user with password - SHA512 enabled 4 digit passcode");
        NOTE("Setting up the child user up to the point of certificates");
        String newPassword = "ValidPassword";

        UpdateUserRequestV1 request = UpdateUserRequestV1.builder()
                .userPassword(newPassword)
                .build();

        ValidatableResponse response = this.authenticateApi.patchUser(alphaTestUserChild, request);

        response.statusCode(200).assertThat();

        THEN("The platform will store the password in ForgeRock");
        AND("A 200 response is returned");

        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(alphaTestUserChild,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUserChild.getUserId())
                        .password(newPassword)
                        .build(),
                alphaTestUserChild.getDeviceId(), true);

        assertNotNull(userLoginResponse.getAccessToken());
        assertNotNull(userLoginResponse.getRefreshToken());
        assertNotNull(userLoginResponse.getScope());
        assertEquals(childId, userLoginResponse.getUserId());
        alphaTestUserChild.setUserPassword(newPassword);
        parseLoginResponse(alphaTestUserChild, userLoginResponse);

        DONE();
    }

    @Test
    @Order(7)
    @Tag("Individual")
    public void change_child_to_customer_scope() {
        ignoreAllSwitch();
        //kid customer scope
        this.authenticateApi.patchUser(alphaTestUserChild,
                UpdateUserRequestV1.builder()
                        .sn("CUSTOMER")
                        .build());

        UserLoginResponseV2 userLoginResponseCustomer = authenticateApi.loginUserProtected(alphaTestUserChild,
                UserLoginRequestV2.builder()
                        .userId(alphaTestUserChild.getUserId())
                        .password(alphaTestUserChild.getUserPassword())
                        .build(),
                alphaTestUserChild.getDeviceId(), true);

        Assertions.assertEquals(CUSTOMER_SCOPE, userLoginResponseCustomer.getScope());
        parseLoginResponse(alphaTestUserChild, userLoginResponseCustomer);

    }

    @Test
    @Order(8)
    public void happy_path_generate_fatca_w8_form() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11991: Happy Path Scenario Child completed Document Generation - Fatca W8");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getW8DocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "W8", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });

        DONE();
    }

    @Test
    @Order(8)
    public void happy_path_generate_fatca_w9_form() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-12028: Happy Path Scenario Child completed Document Generation - Fatca W9");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getW9DocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W9 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "W9", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    @Order(8)
    public void happy_path_generate_crs_form() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-12029: Happy Path Scenario Child completed Document Generation - CRS");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getCRSDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "CRS", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    @Order(8)
    public void happy_path_generate_account_opening_form() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-12030: Happy Path Scenario Child completed Document Generation - Account Opening");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getAccountOpeningDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "AC_OPEN", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    @Order(8)
    public void happy_path_generate_kyc_form() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-12031: Happy Path Scenario Child completed Document Generation - KYC");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getKYCDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "KYC", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    @Order(8)
    public void happy_path_generate_iban_form() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-12032: Happy Path Scenario Child completed Document Generation - IBAN");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getIBANDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "IBAN", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    @Order(9)
    void reset_passcode_child_success() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11983: Happy Path Scenario Reset Passcode for Child - Success");

        String newPassword = UUID.randomUUID().toString();

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(newPassword)
                .build();

        WHEN("The client calls put to reset child passcode");
        this.authenticateApi.resetChildPasscode(alphaTestUser, resetPasscodeReq, connectionId);


        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUser, 204, connectionId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childId);

        assertNotNull(otpCO);

        String otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        assertNotNull(otpCode);

        GIVEN("Request to validate password is created");
        DependantValidateResetPasswordRequestV2 validatePasscodeResetReq = DependantValidateResetPasswordRequestV2.builder()
                .otp(otpCode)
                .password(newPassword)
                .userId(alphaTestUserChild.getUserId())
                .build();
        UserLoginResponseV2 validatePasswordResponse = this.authenticateApi.validateChildPasscode(alphaTestUserChild, validatePasscodeResetReq, alphaTestUserChild.getDeviceId());

        THEN("we will return a 200 response");
        assertNotNull(validatePasswordResponse);
        alphaTestUserChild.setUserPassword(newPassword);

        DONE();
    }

    @Order(10)
    @Test
    public void happy_path_parent_requests_IDV_and_creates_applicant() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11984: Happy Path Scenario Check Parent IDV and EID for Child");
        WHEN("Experience attempts to initiate the IDV process with the connectionID (new API)");
        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(generateEnglishRandomString(10))
                .lastName(generateEnglishRandomString(10))
                .build();

        TokenHolder childApplicant =
                this.idNowApi.createChildApplicant(alphaTestUser, connectionId, applicantRequest);
        THEN("The platform will be able to verify the relationship by matching the ConnectionID to the one which exists in the list of relationships returned for the parent");
        assertNotNull(childApplicant.getApplicantId());
        assertNotNull(childApplicant.getSdkToken());
        alphaTestUserChild.setApplicantId(childApplicant.getApplicantId());

        DONE();
    }

    @Test
    @Order(10)
    public void retrieve_customerId_with_valid_DocumentNumber_200() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11984: Happy Path Scenario Check Parent IDV and EID for Child");
        GIVEN("A child exists with a EID DocumentNumber");

        this.customerApi.patchChildSuccess(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), connectionId);

        String documentNumber = this.relationshipApi.createChildIdvDetails(alphaTestUser, connectionId).getData().getDocumentNumber();

        WHEN("The client attempts to get a customer using that DocumentNumber");
        String customerIdReturned = this.customerApi.getCustomersByEid(documentNumber)
                .getData().get(0).getCustomerId().toString();

        THEN("The platform responds with a 200 and a list containing ONLY the CustomerId " +
                "(BE generated UUID, not CIF) of the customer who has that DocumentNumber");

        Assertions.assertEquals(childId, customerIdReturned, "Customer ID returned did not match, " +
                "expected: " + childId);

        DONE();
    }

    @Test
    @Order(11)
    public void happy_path_parent_requests_name_screening() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11985: Happy Path Scenario Child Name Screening complete");

        WHEN("The platform sends a request to the e-name checker to run the check on the child");
        THEN("The platform will receive the results from the e-name checker");
        AND("The platform will respond with a 200 Response");
        AND("The platform will trigger an event");

        String fullName = this.alphaTestUserChild.getName();

        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(fullName)
                .country("AE")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("F")
                .build();

        CustomerBlacklistResponseDTO response =
                sanctionsApi.checkBlacklistedChild(alphaTestUser, customerBlacklistRequestDTO, connectionId);
        assertNotNull(response);

        this.customerApi.getCurrentCustomer(alphaTestUser);
        DONE();
    }

    @Test
    @Order(12)
    void child_customer_crs_test() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11986: Happy Path Scenario Information Capture for Child Success");
        alphaTestUserBankingCustomerFactory.setupEmploymentChild(alphaTestUser);
    }

    @Test
    @Order(12)
    void create_fatca_details_for_child() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11986: Happy Path Scenario Information Capture for Child Success");
        GIVEN("the customer has provided their FATCA details");
        var fatca = OBWriteFatca1.builder().data(OBWriteFatcaDetails1.builder()
                .usCitizenOrResident(true)
                .ssn("123456789")
                .form(OBFatcaForm1.W8)
                .federalTaxClassification("S Corporation")
                .build())
                .build();

        WHEN("the client updates the customer profile with valid FATCA details");
        var result = this.fatcaApiV2.createFatcaDetailsChild(alphaTestUser, fatca, connectionId);
        THEN("we will return a 201 response");
        assertNotNull(result);
        DONE();
    }

    @Test
    @Order(12)
    public void happy_path_patch_country_and_city_of_birth_200_response() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11986: Happy Path Scenario Information Capture for Child Success");
        GIVEN("The customer has provided their country and city of birth for the child");

        OBWritePartialCustomer1 patchChild1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .countryOfBirth("IN")
                        .cityOfBirth("Dubai")
                        .build())
                .build();

        this.customerApi.patchChildSuccess(alphaTestUser, patchChild1, connectionId);

        Response dataCustomer = this.customerApi.getChild(alphaTestUser, connectionId, 200);
        assertEquals("IN", dataCustomer.as(OBReadCustomer1.class).getData().getCustomer().get(0).getCountryOfBirth());

        OBWritePartialCustomer1 patchChild = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai Change")
                        .build())
                .build();

        this.customerApi.patchChildSuccess(alphaTestUser, patchChild, connectionId);

        WHEN("The client updates with a valid country and city of birth");

        THEN("We will return a 200 Response");

        Response dataCustomer2 = this.customerApi.getChild(alphaTestUser, connectionId, 200);
        assertEquals("AE", dataCustomer2.as(OBReadCustomer1.class).getData().getCustomer().get(0).getCountryOfBirth());
        DONE();
    }

    @Order(12)
    @Test
    public void positive_test_post_and_put_multiple_additional_addresses() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11986: Happy Path Scenario Information Capture for Child Success");
        GIVEN("A child has an existing address in their list of addresses");

        OBLocationDetails1 location1 = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBReadLocationResponse1 postLocationResponse =
                this.locationsApiV2.createLocationDetailsChild(alphaTestUser, location1, connectionId);
        assertEquals(1, postLocationResponse.getData().size());

        WHEN("A client wants to create another address in the list");
        OBLocationDetails1 location2 = OBLocationDetails1.builder()
                .name("Country House")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank")
                        .subDepartment("50b")
                        .buildingNumber("10")
                        .streetName("Al Marakazi ya")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .build())
                .build();

        OBReadLocationResponse1 postLocationResponse2 =
                this.locationsApiV2.createLocationDetailsChild(alphaTestUser, location2, connectionId);
        assertEquals(2, postLocationResponse2.getData().size());

        String locationId = postLocationResponse2.getData().get(0).getId();

        THEN("The platform will respond with a 201");
        AND("The new location details are persisted");
        DONE();

        GIVEN("A child has 2 or more additional addresses");
        OBLocationDetails1 location3 = OBLocationDetails1.builder()
                .id(locationId)
                .name("Country House")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank")
                        .subDepartment("50b")
                        .buildingNumber("10")
                        .streetName("Al Marakazi ya")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .build())
                .build();
        WHEN("A client attempts to PUT an additional address");
        OBReadLocationResponse1 putResponse =
                this.locationsApiV2.updateLocationDetailsChild(alphaTestUser, location3, connectionId);
        assertEquals(2, postLocationResponse2.getData().size());
        THEN("The platform will respond with a 200 Response");
        DONE();
    }

    @Test
    @Order(13)
    public void happy_path_upload_birth_certificate_for_child() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11987: Happy Path Scenario Birth Certificate Check for Child - Success");

        GIVEN("The connectionID exists in the relationship list of the parent");
        relationshipApi.getRelationships(alphaTestUser);

        WHEN("A call is made from Document Adapter to SharePoint");
        documentRelationshipApi.uploadDocument(alphaTestUser, "BIRTH_CERTIFICATE", connectionId, 201);

        THEN("The birth certificate is saved in the Child's User folder in SharePoint");
        DONE();
    }

    @Test
    @Order(14)
    public void create_case_for_child_success() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11989: Happy Path Scenario Child Case Creation");
        GIVEN("A child has failed name screening");
        WHEN("Experience attempts to push an event to create the case on CRM with the relevant case information");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .country("IN")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("M")
                .build();

        THEN("A CRM agent will be able to access the case");
        CustomerBlacklistResponseDTO response = this.sanctionsApi.checkBlacklistedChild(alphaTestUser, customerBlacklistRequestDTO, connectionId);

        assertEquals("HIT", response.getResult());

        AND("The parent's ID will have been added to the case under ResponsibleContactId field");

        CaseEventV1 casesBody = this.casesApi.generateCaseBody(alphaTestUserChild.getUserId(), CaseTypeEnum.EXCEPTION,
                "TITLE", ProcessOriginEnum.NAME_SCREENING, ReasonEnum.E_NAME_CHECKER_HIT, "High", alphaTestUser.getUserId());
        this.casesApi.createCaseInCRM(casesBody, 200);
        DONE();
    }

    @Test
    @Order(15)
    @Tag("Individual")
    public void happy_path_create_child_CIF() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11990: Happy Path Scenario Child CIF Generation");
        GIVEN("A customer has met all of the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV");
        WHEN("A client attempts to generate a CIF for the child");
        patchChildWithFullName(alphaTestUser, connectionId);

        relationshipApi.createChildIdvDetails(alphaTestUser, connectionId);

        OBReadCustomer1 beforeCif = relationshipApi.getChildBasedOnRelationship(alphaTestUser, connectionId);
        assertEquals(OBCustomerType1.MARKETPLACE, beforeCif.getData().getCustomer().get(0).getCustomerType());

        OBReadCustomer1 putCifResponse = relationshipApi.putChildCif(alphaTestUser, connectionId);
        THEN("The platform returns a 200 Response");
        assertNotNull(putCifResponse);
        AND("It will return the generated CIF");
        assertNotNull(putCifResponse.getData().getCustomer().get(0).getCif());
        AND("The CustomerType will be Banking");
        OBReadCustomer1 getChild = relationshipApi.getChildBasedOnRelationship(alphaTestUser, connectionId);
        assertEquals(OBCustomerType1.BANKING, getChild.getData().getCustomer().get(0).getCustomerType());
        DONE();
    }

    @Test()
    @Order(16)
    @Tag("Individual")
    public void create_child_bank_account() {
        ignoreAllSwitch();
        TEST("AHBDB-11958: R5 SIT CRM/CB Integration Tests - Kids Onboarding");
        TEST("AHBDB-11992: Happy Path Scenario Child becomes Banking Customer");
        this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);
        final OBWriteCustomerResponse1 responseStateUpdate = this.customerApi.updateCustomer(alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.ACCOUNT_CREATED)
                        .build())
                .build(), 200);

        GIVEN("Valid parent customer with accounts scope");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        String connectionId = relationships.getData().getRelationships().get(0).getConnectionId().toString();

        AND("Valid kid account request");
        OBWriteAccount1 request = relationshipAccountApi.createYouthAccountData();

        WHEN("Create dependant account");
        OBWriteAccountResponse1 response = relationshipAccountApi.createDependantCustomerAccount(alphaTestUser,request,connectionId);

        THEN("Kid account should be set up correctly");
        Assertions.assertNotNull(response.getData().getAccountId());

        alphaTestUserChild.setAccountNumber(response.getData().getAccountId());

        final OBWriteCustomerResponse1 responseStateUpdateChild = this.customerApi.patchChildSuccess(alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.ACCOUNT_CREATED)
                        .build())
                .build(), connectionId);
        DONE();
    }

    @Test
    @Order(17)
    @Tag("Individual")
    public void issue_virtual_card_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-12999: AC1 Happy Path Request Virtual Card for Kid");
        CreateCard1 validCreateCard1 = validCreateCard1(TEEN_DIGITAL_DC_PLATINUM);

        createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUser, connectionId, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));
    }

    @Test
    @Order(18)
    @Tag("Individual")
    public void request_physical_card_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13000: AC2 Happy Path Request Physical Card for kid");
        final ReadCard1 cards = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUser, connectionId, "debit", 200);
        CREATED_CARD_NUMBER = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        customerApi.patchChildSuccess(alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(OBPartialPostalAddress6.builder()
                                .buildingNumber(generateRandomBuildingNumber())
                                .streetName(generateRandomStreetName())
                                .countrySubDivision(generateRandomCountrySubDivision())
                                .country("AE")
                                .postalCode(generateRandomPostalCode())
                                .addressLine(Collections.singletonList(generateRandomAddressLine()))
                                .build())
                        .build())
                .build(), connectionId);

        OBPostalAddress6 obPostalAddress6 = this.customerApi.getChild(alphaTestUser, connectionId, 200).as(OBReadCustomer1.class).getData().getCustomer().get(0).getAddress();

        String iban = relationshipAccountApi.getAccountDetails(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        WHEN("The parent wants to order a physical card");

        THEN("a 201 is returned from the service");
        cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUser, validPhysicalCard1(cardDeliveryAddress1Valid(obPostalAddress6), iban, alphaTestUser), connectionId, cardId, 201);

        AND("The kid can log on and view their physical card as ordered");
        final ReadCard1 readCard1 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUser, connectionId, "debit", 200);
        Assertions.assertEquals(readCard1.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        Assertions.assertTrue(readCard1.getData().getReadCard1DataCard().get(0).isPhysicalCardPrinted());

        DONE();
    }

    @Test
    @Order(19)
    @Tag("Individual")
    public void activate_kids_debit_card() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13001: AC3 Happy Path Activate Debit Card for kid");
        final ReadCard1 cards = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUser, connectionId, "debit", 200);
        CREATED_CARD_NUMBER = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        THEN("The parent can activate the physical card");
        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUser, validActivateCard1(), connectionId, cardId, 200);

        AND("The kid can log on and view their physical card as ordered");
        final ReadCard1 readCard2 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUser, connectionId, "debit", 200);
        Assertions.assertEquals(readCard2.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        Assertions.assertTrue(readCard2.getData().getReadCard1DataCard().get(0).isCardActivated());
    }

    @Test
    @Order(20)
    public void update_card_parameters_kid() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13005: AC7 Happy Path Update Card Parameters for kid");
        AND("The kid can log on and view their card");
        THEN("The parent can update the kids card parameters with 200");
        this.cardsRelationshipApi.updateCardParametersRelationship(alphaTestUser, validWriteCardParameters1(), connectionId, cardId, 200);

        THEN("The kid can view the update cards restrictions");
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUser, connectionId, cardId, "ecommerce", 200);

        DONE();
    }

    @Test
    @Order(20)
    public void update_card_limits_kid() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13004: AC6 Happy Path Update Limits for kid");
        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        this.cardsRelationshipApi.updateCardLimitsRelationship(alphaTestUser, validWriteCardLimits1(dailyLimit, monthlyLimit), connectionId, cardId, 200);

        THEN("The kid can view the update cards restrictions for the following type: ecommerce");
        final ReadCardLimits1 readCardLimits1 = cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUser, connectionId, cardId, "ecommerce", 200);

    }

    @Test
    @Order(21)
    public void fetch_debit_limits_kid() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13003: AC5 Happy Path View Limits for kid");
        THEN("The kid can view the update cards restrictions for the following type: ecommerce");
        cardsRelationshipApi.getRelationshipCardsLimits(alphaTestUser, connectionId, cardId, "ecommerce", 200);
    }

    @Test
    @Order(21)
    public void fetch_cvv_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13008: AC9 Happy Path View Debit Card CVV for kid");
        THEN("The parent can view the CVV for the cardId : " + cardId);
        cardCVvDetails = this.cardsRelationshipApi.fetchCVVForRelationship(alphaTestUser, connectionId, cardId, 200);
        Assertions.assertNotNull(cardCVvDetails.getData().getCvv());
    }

    @Test
    @Order(21)
    public void fetch_card_list_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13006: AC8 Happy Path View Debit Card Details for kid");
        WHEN("They create a virtual card with valid values using their bank account");
        THEN("A virtual card is created for the user with a 201 response");

        THEN("The parent can view the relationship card");
        ReadCard1 readCard1 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUser, connectionId, "debit", 200);

        CREATED_CARD_NUMBER = readCard1.getData().getReadCard1DataCard().get(0).getCardNumber();
        CREATE_CARD_EXP_DATE = readCard1.getData().getReadCard1DataCard().get(0).getExpiryDate();
    }

    @Test
    @Order(21)
    public void get_account_details_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12998: R5 SIT CRM/CB Integration Tests - Child Account Retrieval");
        TEST("AHBDB-13009: AC1 Happy Path Get Account Details for Child");
        WHEN("Calling get account details api");
        OBReadAccount6 response = relationshipAccountApi.getAccountDetails(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId);

        THEN("Status 200 is returned");
        AND("Account details has been returned");

        assertNotNull(response);
        DONE();
    }

    @Test
    @Order(21)
    public void get_account_list_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12998: R5 SIT CRM/CB Integration Tests - Child Account Retrieval");
        TEST("AHBDB-13014: AC6 Happy Path Get Account List for Child");
        WHEN("Calling get accounts api");
        OBReadAccount6 response = relationshipAccountApi.getAccounts(alphaTestUser, connectionId);

        THEN("Status 200 is returned");
        AND("User Accounts has been returned");

        assertNotNull(response);
    }

    @Test
    @Order(21)
    public void get_account_balances_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12998: R5 SIT CRM/CB Integration Tests - Child Account Retrieval");
        TEST("AHBDB-13010: AC2 Happy Path Get Account Balances for Child");
        WHEN("Calling account balances api");
        OBReadBalance1 balanceResponse = relationshipAccountApi.getAccountBalances(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId);

        THEN("Status 200 is returned");
        AND("Account balances have been returned");

        assertNotNull(balanceResponse);
        DONE();
    }

    @Test
    @Order(21)
    public void get_locked_amounts_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12998: R5 SIT CRM/CB Integration Tests - Child Account Retrieval");
        TEST("AHBDB-13013: AC5 Happy Path Get Locked Amounts for Child");
        WHEN("Calling get locked amount api");
        OBReadTransaction6 lockedAmount = relationshipAccountApi.getLockedAmount(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId, 1);

        THEN("Status 200 is returned");
        AND("Locked Amount Data is not null");

        assertNotNull(lockedAmount);
        DONE();
    }

    @Test
    @Order(21)
    public void get_account_transactions_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12998: R5 SIT CRM/CB Integration Tests - Child Account Retrieval");
        TEST("AHBDB-13011: AC3 Happy Path Get Account Transactions for Child");
        WHEN("Credit transfer is executed");
        final String debtorAccountId = StringUtils.leftPad(temenosConfig.getSeedAccountId(), 12, "0");
        final String creditorAccountId = StringUtils.leftPad(alphaTestUserChild.getAccountNumber(), 12, "0");
        accountApi.executeInternalTransferBetweenAccounts(alphaTestUser, creditorAccountId,debtorAccountId, BigDecimal.valueOf(11));

        WHEN("Calling get account transactions api");
        OBReadTransaction6 response = relationshipAccountApi.getAccountTransactions(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId);

        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("Account transactions has been returned");
        OBReadDataTransaction6 accountTransactions = response.getData();
        assertNotNull(accountTransactions);
        DONE();
    }

    @Test
    @Order(22)
    public void get_account_transaction_details_child() {
        ignoreAllSwitch();
        TEST("AHBDB-12998: R5 SIT CRM/CB Integration Tests - Child Account Retrieval");
        TEST("AHBDB-13012: AC4 Happy Path Get Transaction Details for Child");
        OBReadTransaction6 response = relationshipAccountApi.getAccountTransactions(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId);

        Assertions.assertNotNull(response);
        String statementId = response.getData().getTransaction().get(0).getTransactionReference();

        AND("Calling get transaction details api");
        OBReadTransaction6 transactionDetailsResponse = relationshipAccountApi.getAccountStatementTransactions(alphaTestUser, alphaTestUserChild.getAccountNumber(),statementId, connectionId);

        THEN("Api returns transaction details");
        assertNotNull(transactionDetailsResponse);
    }

    @Test
    @Order(23)
    public void set_child_PIN() throws Throwable {
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        TEST("AHBDB-13002: AC4 Happy Path Set Debit Card PIN for kid");
        ignoreAllSwitch();
        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        THEN("They receive a 200 response back from the service");
        this.cardsRelationshipApi.setDebitCardPinForRelationship(alphaTestUser, validWriteCardPinRequest1(pinBlock), connectionId, cardId, 200);

        AND("My pin is marked as set");
        final ReadCard1 readCard3 = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUser, connectionId, "debit", 200);
        Assertions.assertTrue(readCard3.getData().getReadCard1DataCard().get(0).getIsPintSet());

        DONE();
    }

    @Test
    @Order(24)
    public void positive_case_create_valid_beneficiary_mother_can_add_herself() {
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        ignoreAllSwitch();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUser.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUser);

        AND("I have a valid beneficiary set up with the mothers own account");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(alphaTestUser.getAccountNumber());

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUser, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 getObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUser, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        DONE();
    }

    @Test
    @Order(24)
    public void positive_get_created_valid_beneficiary_mother() {
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        ignoreAllSwitch();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUser.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUser);

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(alphaTestUser.getAccountNumber());

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUser, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUser, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, GetObBeneficiary5);

        DONE();
    }

    @Test
    @Order(24)
    public void positive_get_created_valid_beneficiary_add_mother() {
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        ignoreAllSwitch();
        AND("I have a child who has been onboarded by the mother");

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUser.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");


        AND("I have a valid beneficiary set up with an external account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(alphaTestUser.getAccountNumber());
        beneficiaryData.setBeneficiaryName(alphaTestUser.getName());
        beneficiaryData.setMobileNumber(alphaTestUser.getUserTelephone());

        AND("The kid has completed step up auth");
        beneStepUpAuthOTP(alphaTestUserChild);

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUserChild, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("The child can get the beneficiary by Id");
        OBBeneficiary5 getObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUserChild, createdObBeneficiary5.getBeneficiaryId()).getData().getBeneficiary().get(0);
        Assertions.assertNotNull(getObBeneficiary5);

        DONE();
    }

    @Test
    @Order(25)
    public void positive_kid_can_transfer_to_mother() {
        TEST("AHBDB-12997: R5 SIT CRM/CB Integration Tests - Card Creation and Update");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        ignoreAllSwitch();
        AND("I have a child who has been onboarded by the mother");

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUser.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("The child's account has a balance");
        cardProtectedApi.createCardDeposit(alphaTestUserChild.getAccountNumber(),
                temenosConfig.getCreditorAccountId(),
                BigDecimal.valueOf(10));
        OBReadBalance1 balanceResponse = this.relationshipAccountApi.getAccountBalances(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId);
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal("10"),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("I submit the valid payment consent request");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUserChild, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        Assertions.assertEquals(consentResponse.getData().getInitiation().getCreditorAccount().getIdentification(), alphaTestUser.getAccountNumber());

        AND("I create the valid matching payment transfer request");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUserChild.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        new BigDecimal("10"), "validReference", "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest);
        Assertions.assertNotNull(transferResponse);

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.relationshipAccountApi.getAccountBalances(alphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId);
        Assertions.assertNotNull(balanceResponse);

        String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);;
        DONE();
    }

    private void beneStepUpAuthOTP(AlphaTestUser alphaTestUser) {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private WriteCardPinRequest1 validWriteCardPinRequest1(String pinBlock) {
        return WriteCardPinRequest1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .pinBlock(pinBlock)
                .pinServiceType("G")
                .build();
    }

    private void patchChildWithFullName(AlphaTestUser alphaTestUserToUse, String connectionIdToUse) {
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName("Test")
                        .lastName("Name")
                        .fullName("SIT Test Name")
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .nationality("AE")
                        .gender(OBGender.MALE)
                        .onboardedBy(alphaTestUserToUse.getCustomerId())
                        .email(generateRandomEmail())
                        .build())
                .build();

        customerApi.patchChildSuccess(alphaTestUserToUse, customer, connectionIdToUse);
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
                .addressLine(List.of(obPostalAddress6.getAddressLine().get(0)))
                .buildingNumber(obPostalAddress6.getBuildingNumber())
                .countrySubDivision(obPostalAddress6.getCountrySubDivision())
                .country(obPostalAddress6.getCountry())
                .postalCode(obPostalAddress6.getPostalCode())
                .streetName(obPostalAddress6.getStreetName())
                .build();
    }

    private WriteCardActivation1 validActivateCard1() {
        return WriteCardActivation1.builder()
                .modificationOperation(ACTIVATE_PHYSICAL_VIRTUAL)
                .operationReason("Operation reason")
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
}
