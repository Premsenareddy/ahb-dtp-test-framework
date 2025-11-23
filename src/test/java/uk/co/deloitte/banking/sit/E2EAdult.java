package uk.co.deloitte.banking.sit;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardPaymentResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardWithdrawalResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadDataTransaction6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer;
import uk.co.deloitte.banking.account.api.payment.model.international.*;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
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
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifResponse;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventV1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseTypeEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ProcessOriginEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ReasonEnum;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes.TripleDesUtil;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.BeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.ReadBeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.WriteBeneficiary1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.WriteBeneficiary1Data;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRSResponse2;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBReadIdvDetailsResponse1;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.cases.api.CasesApi;
import uk.co.deloitte.banking.customer.cif.CifsApi;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.taxresidency.api.TaxResidencyApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;
import uk.co.deloitte.banking.payments.serviceProviderBeneficiary.api.ServiceProviderBeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.international.api.InternationalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code.BORNE_BY_DEBTOR;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.INTERNATIONAL_IBAN;

@Tag("@SITRegression")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EAdult {
    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private OtpApi otpApi;

    @Inject
    private CustomerApi customerApiV1;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private BeneficiaryApiFlows beneficiaryApi;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private ServiceProviderBeneficiaryApiFlows serviceProviderBeneficiaryApiFlows;

    @Inject
    private CardProtectedApi cardProtectedApi;

    @Inject
    private CasesApi casesApi;

    @Inject
    private CifsApi cifsApi;

    @Inject
    private TaxResidencyApiV2 taxResidencyApi;

    @Inject
    private SanctionsApi sanctionsApi;

    @Inject
    private TripleDesUtil tripleDesUtil;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private CertificateProtectedApi certificateProtectedApi;

    @Inject
    private InternationalTransferApiFlows internationalTransferApiFlows;

    private AlphaTestUser alphaTestUser;

    private OBBeneficiary5 createdBeneficiary;

    private static final String CARD_MASK = "000000";

    private String CREATED_CARD_NUMBER = null;

    private String CREATE_CARD_EXP_DATE = null;

    private String CREATED_CURRENT_ACCOUNT_CARD_NUMBER = null;

    private String CREATE_CURRENT_ACCOUNT_EXP_DATE = null;

    private String currentAccountNumber = "0";

    private String EMIRATES_ID = null;
    private String creditorAccountNumber = null;

    private String debtorAccountNumber = null;
    private static final int loginMinWeightExpected = 31;
    private static final int changePasscodeMinWeightExpected = 11;

    private static final String INSUFFICIENT_FUNDS = "You do not have sufficient balance in account";
    private static final String CREDITOR_NOT_FOUND = "Invalid account";
    private static final String INVALID_IBAN = "Invalid IBAN Number";
    private static final String ACCOUNT_NOT_USERS = "account doesn't belongs to user";
    private static final String SOURCE_DESTINATION_SAME = "Source and destination accounts cannot be same";
    private static final String LIMIT_EXCEEDED = "Payment would exceed daily limit of";
    private static final String INVALID_PHONE_NUMBER_MESSAGE = "Invalid phone number";
    private static final String UAE_ERROR_UNAUTHORIZED = "UAE.ERROR.UNAUTHORIZED";

    protected ObjectMapper ob = new ObjectMapper();

    public void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        }
    }

    public void ignoreAllSwitch() {
        /**
         * TODO :: SIT E2E ONLY
         */
        //envUtils.ignoreTestInEnv(Environments.ALL);
    }

    public void refreshToken() {
        this.alphaTestUserFactory.refreshAccessToken(this.alphaTestUser);
    }

    @Test
    @Order(1)
    @Tag("HappyPath")
    @Tag("Individual")
    public void a_new_user_is_created_CRM() {
        ignoreAllSwitch();
        setupTestUser();
    }

    @Test
    @Order(2)
    public void marketplace_customer_re_registers_device() throws JsonProcessingException {
        ignoreAllSwitch();
        TEST("AHBDB-10093: R5 SIT CRM/CB Integration Tests - Register and Re-Register Device");
        TEST("AHBDB-12282: Marketplace customer re-registers device");
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        GIVEN("A marketplace customer wants to register a new device");
        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);
        assertEquals("device", loginResponse.getScope());

        parseLoginResponse(alphaTestUser, loginResponse);

        otpApi.sendDestinationToOTP(alphaTestUser, 204);
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);

        String otp = otpCO.getPassword();
        otpApi.postOTPCode(alphaTestUser, 200, otp);

        final AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();

        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUser);

        UserLoginRequestV2 request = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(request));

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                alphaTestUser.getDeviceId(),
                ob.writeValueAsString(request),
                signedSignature,
                204);

        UserLoginResponseV2 userLoginResponseV2 = authenticateApiV2.loginUser(alphaTestUser, request,
                alphaTestUser.getDeviceId(), false);
        assertEquals(CUSTOMER_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        WHEN("They go through the re register device flow");

        THEN("When they log in they have a scope of customers");
        final UserLoginRequestV2 request2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        final String signedSignature2 =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        ob.writeValueAsString(request2),
                        alphaTestUser.getPreviousPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getPreviousDeviceId(),
                ob.writeValueAsString(request2), signedSignature2, 204);

        ValidatableResponse error = authenticateApiV2.loginUserValidatable(alphaTestUser, request2,
                alphaTestUser.getPreviousDeviceId(), false);
        error.statusCode(401).assertThat();

        AND("Their old device is disabled");
        OBErrorResponse1 errorResponse = error.extract().as(OBErrorResponse1.class);
        assertEquals(UAE_ERROR_UNAUTHORIZED, errorResponse.getCode());
        assertEquals("Device is disabled", errorResponse.getMessage());
        DONE();
    }

    @Test
    @Order(2)
    @Tag("HappyPath")
    @Tag("Individual")
    public void customer_completes_IDV() {
        ignoreAllSwitch();
        this.customerApiV2.updateCustomer(alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApiV2.createCustomerIdvDetails(alphaTestUser);

        OBReadIdvDetailsResponse1 responseIdv = this.customerApiV2.getCustomerIdvDetails(alphaTestUser);
        EMIRATES_ID = responseIdv.getData().getDocumentNumber();
    }

    @Test
    @Order(3)
    @Tag("HappyPath")
    public void happy_path_generate_legacy_CIF_when_EID_does_not_exist_in_service() {
        TEST("AHBDB-9814: AC6 - EID does not exist in CIF service - Generate CIF");
        TEST("AHBDB-10140: AC1 Happy Path Scenario - Generate CIF when EID does not exist in CIF Service");
        ignoreAllSwitch();
        GIVEN("A customer's EID is not already matched to a CIF in the CIF service");

        AND("The user is logged in with a scope of customer or account");
        WHEN("The customer adapter attempts to generate a CIF with DateOfBirth, PhoneNumber and DocumentNumber");
        CifResponse cifResponse = this.cifsApi.generateCifNumber(alphaTestUser, EMIRATES_ID);

        THEN("The CIF service will generate a CIF according to the generation rules");
        AND("The CIF service will return a 201 OK");
        AND("The CIF service will return the new CIF");
        Assertions.assertFalse(cifResponse.getCifNumber().isBlank());

        AND("The new CIF can be viewed in CRM");
        //Manual check for CIF in CRM

        DONE();
    }

    @Test
    @Order(3)
    public void customer_tries_to_generate_CIF_with_invalid_phone_number() {
        TEST("AHBDB-9814: R5 SIT Integration Tests");
        TEST("AHBDB-10143: Negative Test - Customer tries to generate CIF with invalid information");
        ignoreAllSwitch();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has populated EID and IDV complete");
        JSONObject jsonObject = this.cifsApi.createCifBodyJson("+5555250020", EMIRATES_ID, alphaTestUser.getDateOfBirth().toString());

        WHEN("The customer adapter attempts to generate a CIF with invalid fields");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 error = this.cifsApi.generateCifError(alphaTestUser, jsonObject, 400);

        THEN("The platform will return a 400");
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", error.getCode(), "Error code not as expected");
        Assertions.assertTrue(error.getMessage().contains(INVALID_PHONE_NUMBER_MESSAGE), "Error message not as expected");

        DONE();
    }

    @Test
    @Order(3)
    public void customer_tries_to_generate_CIF_with_invalid_date_of_birth() {
        TEST("AHBDB-9814: R5 SIT Integration Tests");
        TEST("AHBDB-10143: Negative Test - Customer tries to generate CIF with invalid information");
        ignoreAllSwitch();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has populated EID and IDV complete");
        JSONObject jsonObject = this.cifsApi.createCifBodyJson(alphaTestUser.getUserTelephone(), EMIRATES_ID, "2021-02-29");

        WHEN("The customer adapter attempts to generate a CIF with invalid fields");
        this.cifsApi.generateCifError(alphaTestUser, jsonObject, 400);

        THEN("The platform will return a 400");

        DONE();
    }

    @Test
    @Order(3)
    public void customer_tries_to_generate_CIF_with_0_at_start() {
        TEST("AHBDB-9814: R5 SIT Integration Tests");
        TEST("AHBDB-10494: Negative Test - Customer tries to generate CIF with 0 at the start");
        ignoreAllSwitch();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has populated EID and IDV complete");
        String cifNumber = "0" + randomNumeric(6);

        WHEN("The customer adapter attempts to generate a CIF with a 0 at the beginning");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 error = this.cifsApi.submitCifNumberError(EMIRATES_ID, cifNumber, 400);

        THEN("The platform will return a 400");
        DONE();
    }

    @Test
    @Order(4)
    public void happy_path_generate_new_CIF_when_EID_exists_in_service() {
        TEST("AHBDB-9814: AC7 - EID exists in CIF service - Generate new CIF");
        TEST("AHBDB-10141: AC2 Happy Path Scenario - Generate new CIF when EID exists in CIF Service");
        ignoreAllSwitch();
        GIVEN("A customer's EIF is matched to a CIF in the CIF service");

        AND("The user is logged in with a scope of customer or account");
        AND("The CIF exists in the database with SystemOfOrigin == Legacy");
        String cifNumber = randomNumeric(7).replace("0", "1");
        CifResponse cifResponse = this.cifsApi.submitCifNumber(EMIRATES_ID, cifNumber);
        Assertions.assertFalse(cifResponse.getCifNumber().isBlank());

        WHEN("The customer adapter attempts to generate a CIF with DateOfBirth, PhoneNumber and DocumentNumber");
        CifResponse cifResponse1 = this.cifsApi.generateCifNumber(alphaTestUser, EMIRATES_ID);

        THEN("The CIF service will generate a CIF according to the generation rules");
        AND("The CIF service will return a 201 OK");
        AND("The CIF service will return the new CIF");
        Assertions.assertFalse(cifResponse1.getCifNumber().isBlank());

        AND("The new CIF can be viewed in CRM");
        DONE();
    }



    @Test
    @Order(4)
    @Tag("HappyPath")
    public void client_does_EID_verification_success() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: R5 SIT CRM/CB Integration Tests - EID Verification at time of card delivery");
        TEST("AHBDB-10836: AC1 Happy Path Scenario - Retrieve CustomerID - Success");
        GIVEN("The DocumentNumber of the EID is sent to experience services by Fetchr");
        AND("The DocumentNumber exists against a CustomerID in CRM");
        String customerID = this.customerApiV2.getCustomerSuccess(this.alphaTestUser)
                .getData().getCustomer().get(0).getCustomerId().toString();

        WHEN("The client attempts to get the CustomerId by DocumentNumber");
        OBReadCustomerId1 response = this.customerApiV2.getCustomersByEid(EMIRATES_ID);

        THEN("The platform will return a 200 OK");
        AND("The platform will return the CustomerID that the DocumentNumber belongs to");
        Assertions.assertEquals(customerID, response.getData().get(0).getCustomerId().toString(),
                "Customer Id's do not match");

        DONE();
    }

    @Test
    @Order(4)
    @Tag("HappyPath")
    @Tag("Individual")
    public void client_updates_EIDStatus_record_success() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: R5 SIT CRM/CB Integration Tests - EID Verification at time of card delivery");
        TEST("AHBDB-10844: AC2 - Update Customer EIDStatus Record");
        GIVEN("The client wants to update the EIDStatus for the customer in the customer record");
        OBReadCustomer1Data getCustomer = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData();
        Assertions.assertEquals(OBEIDStatus.PENDING, getCustomer.getCustomer().get(0).getEidStatus());

        WHEN("The client attempts to update the customer profile with a valid EIDStatus against a valid CustomerID");
        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        THEN("The platform will set the EIDStatus against the CustomerID sent in the path");
        AND("The platform will push an event to Kafka with the customer's EIDStatus, CustomerID and CIF");
        this.customerApiV2.updateCustomerValidations(alphaTestUser, eidStatus);

        AND("The platform will return a 200 OK");
        AND("The platform will return the CustomerID of the customer");
        OBReadCustomer1Data data = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData();

        Assertions.assertEquals(OBEIDStatus.VALID, data.getCustomer().get(0).getEidStatus());

        DONE();
    }

    @Test
    @Order(4)
    public void emiratesID_not_found_in_service() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: R5 SIT CRM/CB Integration Tests - EID Verification at time of card delivery");
        TEST("AHBDB-10843: Negative Test - EmiratesId not found");
        GIVEN("The DocumentNumber of the EID is sent to experience services by Fetchr");
        AND("The DocumentNumber does not exist against a CustomerID in CRM");
        WHEN("The client attempts to get the CustomerID by DocumentNumber");
        OBReadCustomerId1 response = this.customerApiV2.getCustomersByEid(generateRandomEID());

        THEN("The platform will return a 200 OK");
        AND("The platform will return an empty list");
        Assertions.assertTrue(response.getData().isEmpty());

        DONE();
    }

    @Test
    @Order(4)
    public void EID_does_not_match_create_case() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: R5 SIT CRM/CB Integration Tests - EID Verification at time of card delivery");
        TEST("AHBDB-10845: AC3 - EID does not match - create case");
        String customerId = alphaTestUser.getUserId();

        GIVEN("The client wants to create a case due to EID verification failure");
        CaseEventV1 body = this.casesApi.generateCaseBody(customerId, CaseTypeEnum.EXCEPTION,
                "CAS_Testing_SIT", ProcessOriginEnum.EID_VERIFICATION, ReasonEnum.EID_CANNOT_BE_VERIFIED,
                "High");

        WHEN("The client attempts to push an event with the relevant case information so that an agent can investigate");
        this.casesApi.createCaseInCRM(body, 200);

        THEN("The platform will consume this event");
        AND("The platform will create the case on CRM");
        DONE();
    }

    @Test
    @Order(5)
    @Tag("HappyPath")
    @Tag("Individual")
    public void a_new_customer_is_successfully_created_in_transact() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC1 - Create Customer Account - Success");
        TEST("AHBDB-7327: AC1 Happy Path Scenario - Create Customer Account - Success");
        GIVEN("a valid new customer exists within DTP");
        THEN("They can successfully create a new bank account");
        this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
        //Check that the customer account is set up in Transact
        System.err.println(alphaTestUser.getPrivateKeyBase64());
        System.err.println(alphaTestUser.getPublicKeyBase64());
        WHEN("I check Transact's User Interface");
        THEN("I see the customer and account have been created");
        final OBWriteCustomerResponse1 responseStateUpdate = this.customerApiV2.updateCustomer(alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.ACCOUNT_CREATED)
                        .build())
                .build(), 200);
        //Confirm that the account has been created in Transact

        DONE();
    }

    @Test
    @Order(6)
    public void happy_path_do_not_generate_CIF_when_EID_exists_in_service() {
        TEST("AHBDB-9814: AC8 - EID exists in CIF service - Do not generate CIF");
        TEST("AHBDB-10142: AC3 Happy Path Scenario - Do not generate CIF when EID exists in CIF Service");
        ignoreAllSwitch();
        GIVEN("A customer's EID is matched to a CIF in the CIF service");
        AND("The CIF can be viewed in CRM");
        String cifFromPhoneNumber = alphaTestUser.getUserTelephone().substring(alphaTestUser.getUserTelephone().length() - 7);
        AND("The user is logged in with a scope of customer or account");
        AND("The CIF exists in the database with SystemOfOrigin == DTP");
        //This is already done by previous method

        WHEN("The customer adapter attempts to generate a CIF with DateOfBirth, PhoneNumber and DocumentNumber");
        CifResponse cifResponse = this.cifsApi.generateCifNumber(alphaTestUser,EMIRATES_ID);

        THEN("The CIF service will return a 201 OK");
        AND("The CIF service will return the CIF value of the customer who exists with that EmiratesID number");
        Assertions.assertEquals(cifFromPhoneNumber, cifResponse.getCifNumber());

        AND("The CIF will be the same as seen previously in CRM");
        DONE();
    }

    @Test
    @Order(6)
    @Tag("HappyPath")
    public void a_new_current_account_is_successfully_created_in_transact() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC1 - Create Customer Account - Success");
        TEST("AHBDB-7327: AC1 Happy Path Scenario - Create Customer Account - Success");
        GIVEN("a valid new customer exists within DTP");
        THEN("They can successfully create a new bank account");
        OBWriteAccountResponse1 response = this.accountApi.createCustomerCurrentAccount(this.alphaTestUser);
        currentAccountNumber = response.getData().getAccountId();
        //Check that the customer account is set up in Transact
        WHEN("I check Transact's User Interface");
        THEN("I see the customer and account have been created");
        //Confirm that the account has been created in Transact
        refreshToken();
        DONE();
    }

    @Test
    @Order(7)
    public void banking_customer_enters_invalid_passcode_when_updating_passcode() {
        ignoreAllSwitch();
        TEST("AHBDB-10096: AC4 Invalid/missing password - 400 bad request");
        TEST("AHBDB-11427: Negative Test - Banking customer enters an invalid passcode when changing their passcode");
        GIVEN("The banking client wants to store a new password against a user ");

        final UpdateUserRequestV1 updateUserRequestV1 = UpdateUserRequestV1.builder()
                .userPassword("1234567")
                .build();

        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(changePasscodeMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest2 = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(changePasscodeMinWeightExpected).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest2);

        AND("The new password does not satisfy the validation in the data table ");

        WHEN("The client updates the user with this new password");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 errorResponse2 = authenticateApiV2.patchUserCredentialsError(alphaTestUser, updateUserRequestV1, 400);

        THEN("The platform returns a 400 Bad request");
        assertEquals("REQUEST_VALIDATION", errorResponse2.getCode(), "Error Code is not matching, expected REQUEST_VALIDATION but received " + errorResponse2.getCode());
        assertTrue(errorResponse2.getMessage().contains("password cannot be blank"), "Expected the error message to contain: password cannot be blank");
    }

    @Test
    @Order(7)
    public void customer_posts_incorrect_form_values() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8354: Document Generation - User posts form with incorrect values");
        GIVEN("A customer exists as a banking user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        WHEN("The client attempts to generate a form for the customer with invalid fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getIBANDocumentRequest(this.alphaTestUser);
        documentRequest.getRequest().setTemplateId("CRS");

        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        THEN("The form will not be generated");
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentList = this.documentAdapterApi.getDocumentList(this.alphaTestUser, "CRS", this.alphaTestUser.getAccountNumber(),
                            "2021-01" , "2021-12", 200);

                    Assertions.assertNull(documentList.getData().getDocumentFiles());
                });

        AND("It will not be stored in Sharepoint");
        DONE();
    }

    @Test
    @Order(7)
    @Tag("HappyPath")
    public void customer_posts_KYC_form() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC9 - Document Generation - KYC - Success");
        TEST("AHBDB-8302: AC9 - Document Generation - KYC - Success");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        WHEN("The client attempts to generate a KYC form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getKYCDocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the KYC form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "KYC", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    @Order(7)
    public void customer_posts_Account_Opening_form() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC9 - Document Generation - Account Opening - Success");
        TEST("AHBDB-8297: AC9 - Document Generation - Account Opening - Success");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        WHEN("The client attempts to generate an Account Opening form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getAccountOpeningDocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the Account Opening form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "AC_OPEN", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    @Order(7)
    public void customer_posts_IBAN_form() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC9 - Document Generation - IBAN - Success");
        TEST("AHBDB-8298: AC9 - Document Generation - IBAN - Success");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        WHEN("The client attempts to generate a IBAN form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getIBANDocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the IBAN form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "IBAN", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    @Order(7)
    public void customer_posts_CRS_form() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC9 - Document Generation - CRS - Success");
        TEST("AHBDB-8296: AC9 - Document Generation - CRS - Success");
        TEST("The customer posts their CRS form");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        alphaTestUser.setCustomerId(customerId);
        WHEN("The client attempts to generate a CRS form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent CRSdocumentRequest = this.developmentSimulatorService.getCRSDocumentRequest(alphaTestUser);

        THEN("We will generate the form against the CRS form existing template");
        this.developmentSimulatorService.generateDocument(CRSdocumentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "CRS", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    @Order(7)
    public void customer_posts_FATCA_form() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC9 - Document Generation - FATCA - Success");
        TEST("AHBDB-8294: AC9 - Document Generation - FATCA - Success");
        TEST("The customer posts the FATCA Form");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        alphaTestUser.setCustomerId(customerId);

        AND("The customer's 'Form' field is set to W8");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the " +
                "mandatory consolidated list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getW9DocumentRequest(alphaTestUser);

        THEN("We will generate the form against the W8 form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "W9", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });
        refreshToken();
    }

    @Test
    @Order(8)
    @Tag("HappyPath")
    public void create_virtual_savings_account_card_for_customer() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC8 - Customer creates virtual card - Success");
        TEST("AHBDB-7681: AC8 Customer Creates Virtual Card - Success");
        GIVEN("A customer has a valid bank account");
        AND("They request a virtual card");
        CreateCard1 validCreateCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUser.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("SAVINGS")
                                .accountNumber(alphaTestUser.getAccountNumber())
                                .accountType(AccountType.SAVINGS.getDtpValue())
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:45", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();

        WHEN("We call the API to create a virtual card for the customer");

        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        THEN("The API returns 201");
        AND("A card is created");

        CREATED_CARD_NUMBER = createCardResponse.getData().getCardNumber();
        CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();

        DONE();
    }

    @Test
    @Order(8)
    public void create_virtual_current_account_card_for_customer() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC8 - Customer creates virtual card - Success");
        TEST("AHBDB-7681: AC8 Customer Creates Virtual Card - Success");
        GIVEN("A customer has a valid bank account");
        AND("They request a virtual card");
        CreateCard1 validCreateCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUser.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(currentAccountNumber)
                                .accountType(AccountType.CURRENT.getDtpValue())
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:45", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();

        WHEN("We call the API to create a virtual card for the customer");

        final CreateCard1Response createCardResponse = this.cardsApiFlows.createVirtualDebitCard(alphaTestUser, validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        THEN("The API returns 201");
        AND("A card is created");

        CREATED_CURRENT_ACCOUNT_CARD_NUMBER = createCardResponse.getData().getCardNumber();
        CREATE_CURRENT_ACCOUNT_EXP_DATE = createCardResponse.getData().getExpiryDate();

        DONE();
    }

    @Order(9)
    @Test()
    @Tag("HappyPath")
    public void create_physical_card() {
        refreshToken();

        ignoreAllSwitch();
        TEST("AHBDB-235 /AHBDB-6963 user creates physical card");
        GIVEN("I have a valid customer with accounts scope with an activated virtual card");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        OBPostalAddress6 obPostalAddress6 = this.customerApiV1.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getAddress();
        String iban = this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber()).getData().getAccount().get(0).getAccount().get(0).getIdentification();

        AND("I have a valid iban : " + iban);
        WHEN("I want to create my Physical card");

        CardDeliveryAddress1 cardDeliveryAddress1Valid = CardDeliveryAddress1.builder()
                .addressLine(List.of(obPostalAddress6.getAddressLine().get(0), obPostalAddress6.getAddressLine().get(1)))
                .buildingNumber(obPostalAddress6.getBuildingNumber())
                .countrySubDivision(obPostalAddress6.getCountrySubDivision())
                .country(obPostalAddress6.getCountry())
                .postalCode(obPostalAddress6.getPostalCode())
                .streetName(obPostalAddress6.getStreetName())
                .townName(generateRandomTownName())
                .name(alphaTestUser.getName())
                .department(obPostalAddress6.getDepartment())
                .subDepartment(obPostalAddress6.getSubDepartment())
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

    @Test
    @Order(7)
    @Tag("HappyPath")
    public void customer_completes_CRS() {
        ignoreAllSwitch();
        OBWriteCRS2 crs = taxResidencyApi.getObWriteCRS2();
        OBWriteCRSResponse2 postCrsResponse = this.customerApiV2.postCRSDetails(alphaTestUser, crs);
        taxResidencyApi.getTaxInformation(alphaTestUser, 200);
    }

    @Test
    @Order(7)
    @Tag("HappyPath")
    public void customer_completes_eName_check() {
        ignoreAllSwitch();
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(alphaTestUser.getName())
                .country("IN")
                .dateOfBirth(LocalDate.parse("1935-07-03"))
                .gender("F")
                .build();
        CustomerBlacklistResponseDTO responseAML = sanctionsApi.checkBlacklistedCustomer(alphaTestUser, customerBlacklistRequestDTO);
        assertEquals("NOHIT", responseAML.getResult());
    }

    @Test
    @Order(13)
    @Tag("HappyPath")
    @Tag("Individual")
    public void customer_deposits_money_into_account() {
        ignoreAllSwitch();
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(300));
        refreshToken();
    }

    @Test
    @Order(7)
    @Tag("HappyPath")
    public void retrieve_account_details() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC7 - Get Account Details - Success");
        TEST("AHBDB-7332: AC7 - Retrieve Customer Account Details - Success");
        GIVEN("A customer has created an account");
        AND("I want to retrieve the account details");
        THEN("Their account details can be retrieved from the core banking adapter");
        OBReadAccount6 response = this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(response.getData().getAccount().get(0).getOpeningDate());
        Assertions.assertNotNull(response.getData().getAccount().get(0).getDescription());

        WHEN("I check Transact's User Interface");
        THEN("The account details match those retrieved by the API");
        //Check the API call returns the correct information

        DONE();
    }

    @Test
    @Order(7)
    @Tag("HappyPath")
    public void customer_retrieves_all_accounts() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: AC1 - Customer retrieves list of accounts - Success");
        TEST("AHBDB-10528: AC1 - Happy Path Scenario - Customer retrieves list of accounts");
        GIVEN("The customer has a banking account");
        CifResponse cifResponse1 = this.cifsApi.generateCifNumber(alphaTestUser, EMIRATES_ID);

        AND("They want to retrieve a list of their accounts");
        WHEN("The client sends a request to get the customer's accounts");
        OBReadAccount6 accountList = this.accountApi.getAccountByCif(alphaTestUser, cifResponse1.getCifNumber());

        THEN("The platform will respond with the list of the customer's accounts");
        Assertions.assertTrue(accountList.getData().getAccount().get(0).getAccountType().equals(OBExternalAccountType1Code.AHB_BASIC_SAV)
                || accountList.getData().getAccount().get(0).getAccountType().equals(OBExternalAccountType1Code.AHB_BASIC_CUR_AC));

        DONE();
    }

    @Test
    @Order(9)
    @Tag("HappyPath")
    public void retrieve_card_details() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC9 - Retrieve user's cards - Success");
        TEST("AHBDB-7682: AC9 - Retrieve user's cards - Success");
        GIVEN("A customer has created an account with virtual card");
        AND("I want to retrieve their virtual cards details");
        WHEN("I call the API to retrieve virtual cards details");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertNotNull(cards.getData().getReadCard1DataCard().get(0).getCardNumber());

        THEN("The API returns a 200 OK");
        AND("Their virtual card details can be retrieved from the HPS adapter");

        DONE();
    }

    @Test
    @Order(7)
    @Tag("HappyPath")
    public void happy_path_customer_creates_service_beneficiary() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC11 Manage Service Beneficiaries");
        TEST("AHBDB-9398: AC11 - Happy Path - Create a valid service beneficiary - Success");
        GIVEN("A customer wants to create or manage their service beneficiaries");
        WriteBeneficiary1 beneficiaryData = createValidBeneData();

        WHEN("The customer sends a request to create or manage their service beneficiaries");
        BeneficiaryResponse1 beneficiaryResponseData = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, beneficiaryData);

        THEN("The client will respond with a list of the customer's service beneficiaries");
        Assertions.assertNotNull(beneficiaryResponseData.getData());

        DONE();
    }

    @Test
    @Order(7)
    public void create_service_beneficiary_invalid_fields() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-9399: Negative Test - Create Service Beneficiary - Invalid Fields");
        GIVEN("A customer wants to create a service beneficiary");
        WriteBeneficiary1 beneficiaryData = WriteBeneficiary1.builder()
                .data(WriteBeneficiary1Data.builder()
                        .serviceCode("")
                        .serviceProvider("1234")
                        .serviceTypeCode("$&*^£")
                        .serviceType("!@£$")
                        .premiseNumber("97273084")
                        .consumerPin("97273084")
                        .phoneNumber("971739583")
                        .mobileNumber("971739583")
                        .creditor(OBCashAccount50.builder()
                                .schemeName("schemePlaceholder")
                                .name("Service Bene Nickname field of more than fifty char")
                                .identification("12345678901212")
                                .secondaryIdentification("secondary identification")
                                .build())
                        .build())
                .build();

        WHEN("The customer sends an invalid request to create a service beneficiary");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, beneficiaryData, 400);

        THEN("The client will respond with a 400 Bad Request");
        DONE();
    }

    @Test
    @Order(7)
    public void create_service_beneficiary_invalid_customer_token() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-9400: Negative Test - Create Service Beneficiary - Invalid Customer Token");
        GIVEN("A customer wants to create a service beneficiary");
        WriteBeneficiary1 beneficiaryData = createValidBeneData();

        AND("The customer does not have a valid bearer token");
        WHEN("The customer sends a valid request to create a service beneficiary");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryWithoutBearerToken(alphaTestUser, beneficiaryData, 401);

        THEN("The client will respond with a 401 Unauthorised");
        DONE();
    }

    @Test
    @Order(7)
    public void customer_unable_to_create_beneficiary_without_step_up_authentication() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8340: Create Beneficiary without Step Up Authentication");
        GIVEN("A customer wants to create a beneficiary");
        OBReadBeneficiary5 getBeneficiaries = this.beneficiaryApi.getBeneficiaries(this.alphaTestUser);
        Assertions.assertNull(getBeneficiaries.getData().getBeneficiary());

        WHEN("The customer doesn't complete step up authentication");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName("Test Beneficiary");
        OBErrorResponse1 error = this.beneficiaryApi.createBeneErrorResponse(this.alphaTestUser, beneficiaryData, 403);

        THEN("The beneficiary cannot be created");
        Assertions.assertEquals("UAE.AUTH.STEP_UP_AUTH_REQUIRED", error.getCode(), "Error codes do not match, expected: UAE.AUTH.STEP_UP_AUTH_REQUIRED");
        Assertions.assertEquals("Step up permissions needed to perform this operations", error.getMessage());

        DONE();
    }

    @Test
    @Order(7)
    @Tag("HappyPath")
    public void create_beneficiary_for_user_in_payments() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC10 - Step Up Authentication for Beneficiaries");
        TEST("AHBDB-8320: AC10 - Step Up Authentication - Create Beneficiary - Success");
        TEST("AHBDB-7060: AC4 - Create Beneficiaries - Success");
        TEST("AHBDB-7061: AC4 Create Beneficiaries - Success");
        GIVEN("The customer has an Account with no beneficiaries");
        OBReadBeneficiary5 getBeneficiaries = this.beneficiaryApi.getBeneficiaries(this.alphaTestUser);
        Assertions.assertNull(getBeneficiaries.getData().getBeneficiary());

        beneStepUpAuthOTP();
        WHEN("The user tries to create a beneficiary");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName("Test Beneficiary");

        createdBeneficiary = this.beneficiaryApi.createBeneficiaryFlex(this.alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("A beneficiary is created successfully");
        Assertions.assertNotNull(createdBeneficiary.getBeneficiaryId());
        Assertions.assertEquals(beneficiaryData.getNickName(), createdBeneficiary.getSupplementaryData().getNickname());

        DONE();
    }

    @Test
    @Order(8)
    @Tag("HappyPath")
    public void happy_path_retrieve_service_beneficiaries() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC11 Manage Service Beneficiaries");
        TEST("AHBDB-9401: AC11 - Happy Path - Customer retrieves service beneficiaries");
        GIVEN("A customer wants to retrieve their service beneficiaries");
        WHEN("The customer requests a list of service beneficiaries");
        ReadBeneficiaryResponse1 beneficiaryResponse = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("The client will respond with the list of the customer's service beneficiaries");
        Assertions.assertNotNull(beneficiaryResponse.getData().getBeneficiaryList());

        DONE();
    }

    @Test
    @Order(8)
    public void retrieve_service_beneficiary_invalid_customer_token() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-9409: Negative Test - Get Service Beneficiary - Invalid customer token");
        GIVEN("A customer wants to get a list of their service beneficiaries");
        AND("The customer does not have a valid bearer token");
        WHEN("The customer sends a valid request to get their service beneficiaries");
        this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiariesInvalidBearerToken(401);

        THEN("The client will respond with a 401 Unauthorised");
        DONE();
    }

    @Test
    @Order(8)
    @Tag("HappyPath")
    public void retrieve_beneficiary_from_account() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC5 - Get Beneficiaries - Success");
        TEST("AHBDB-7325: AC5 Retrieve Beneficiaries - Success");
        GIVEN("The customer has an account with beneficiaries");
        WHEN("I try to retrieve a list of the beneficiaries");
        OBReadBeneficiary5 getAllBeneficiaries = this.beneficiaryApi.getBeneficiaries(this.alphaTestUser);
        Assertions.assertEquals(1, getAllBeneficiaries.getData().getBeneficiary().size());

        THEN("The API will return 200 OK");
        AND("I will receive a list of the customer's beneficiaries");
        Assertions.assertNotNull(getAllBeneficiaries.getData().getBeneficiary());
        AND("The beneficiary returned by the payments service matches the one the user submitted");
        Assertions.assertEquals(getAllBeneficiaries.getData().getBeneficiary().get(0), createdBeneficiary);

        //Check the API call returns the correct information

        DONE();
    }

    @Test
    @Order(8)
    public void retrieve_beneficiary_from_account_using_BeneficiaryId() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC5 - Get Beneficiaries - Success");
        TEST("AHBDB-7329: AC5 Retrieve Beneficiaries using BeneficiaryID - Success");
        GIVEN("The customer has an account with beneficiaries");
        WHEN("I try to retrieve a list of the beneficiaries");
        OBReadBeneficiary5 getAllBeneficiaries = this.beneficiaryApi.getBeneficiaryById(this.alphaTestUser,
                createdBeneficiary.getBeneficiaryId());

        THEN("The API will return 200 OK");
        AND("I will receive the beneficiary requested");
        Assertions.assertNotNull(getAllBeneficiaries.getData().getBeneficiary());
        System.out.println();
        System.out.println("Nickname: " + getAllBeneficiaries.getData().getBeneficiary().get(0).getSupplementaryData().getNickname());
        System.out.println("Beneficiary ID: " + getAllBeneficiaries.getData().getBeneficiary().get(0).getBeneficiaryId());
        System.out.println("Mobile Number: " + getAllBeneficiaries.getData().getBeneficiary().get(0).getSupplementaryData().getMobileNumber());
        System.out.println();

        WHEN("I check Transact's User Interface");
        THEN("The beneficiary will match the received");
        //Check the API call returns the correct information

        DONE();
    }

    @Test
    @Order(9)
    public void customer_unable_to_update_beneficiary_without_step_up_authentication() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8338: Update Beneficiary without Step Up Authentication");
        GIVEN("A customer wants to update a beneficiary");
        OBBeneficiary5 getBeneficiary = this.beneficiaryApi.getBeneficiaryById(this.alphaTestUser,
                createdBeneficiary.getBeneficiaryId())
                .getData().getBeneficiary().get(0);
        getBeneficiary.setReference("Test Ref");

        WHEN("The customer doesn't complete step up authentication");
        OBErrorResponse1 error = this.beneficiaryApi.updateBeneficiaryError(this.alphaTestUser, getBeneficiary, 403);

        THEN("The beneficiary cannot be updated");
        Assertions.assertEquals("UAE.AUTH.STEP_UP_AUTH_REQUIRED", error.getCode(), "Error codes do not match, expected: UAE.AUTH.STEP_UP_AUTH_REQUIRED");
        Assertions.assertEquals("Step up permissions needed to perform this operations", error.getMessage());

        DONE();
    }

    @Test
    @Order(9)
    @Tag("HappyPath")
    public void update_beneficiary_in_account() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC10 - Step Up Authentication for Beneficiaries");
        TEST("AHBDB-8321: Step Up Authentication - Update Beneficiary - Success");
        TEST("AHBDB-7060: AC6 - Update Beneficiaries - Success");
        TEST("AHBDB-7328: AC6 Update Beneficiaries - Success");
        GIVEN("The customer has an account with beneficiaries");
        AND("They wish to update a beneficiary's details");
        OBBeneficiary5 getBeneficiary = this.beneficiaryApi.getBeneficiaryById(this.alphaTestUser,
                createdBeneficiary.getBeneficiaryId())
                .getData().getBeneficiary().get(0);
        getBeneficiary.setReference("Test Ref");

        WHEN("I call the API to update the beneficiary");
        beneStepUpAuthOTP();
        THEN("The API will return 200 OK");
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApi.updateBeneficiary(this.alphaTestUser, getBeneficiary);

        AND("I will receive a list of the customer's beneficiaries with updated information");
        Assertions.assertEquals("Test Ref", updatedBeneficiary.getData().getBeneficiary().get(0).getReference());
        System.out.println();
        System.out.println("Nickname: " + updatedBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getNickname());
        System.out.println("Beneficiary ID: " + updatedBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());
        System.out.println("Mobile Number: " + updatedBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getMobileNumber());
        System.out.println();

        WHEN("I check Transact's User Interface");
        THEN("The beneficiaries will match those in the received list");
        //Check the update has been made in Transact

        DONE();
    }

    @Test
    @Order(11)
    @Tag("HappyPath")
    public void happy_path_customer_deletes_service_beneficiary() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC11 - Manage Service Beneficiaries");
        TEST("AHBDB-9407: AC11 - Happy Path - Customer deletes service beneficiary by ID");
        GIVEN("A customer wants to delete a service beneficiary from their account");
        ReadBeneficiaryResponse1 response1 = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        WHEN("The customer requests to delete that service beneficiary");
        String beneId = response1.getData().getBeneficiaryList().get(0).getBeneficiaryId();

        THEN("The beneficiary will be deleted from the customer's list");
        this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiary(alphaTestUser, beneId, 204);

        AND("The client will return a list of the customer's service beneficiaries less the deleted one");
        ReadBeneficiaryResponse1 getBenes = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);
        Assertions.assertNull(getBenes.getData().getBeneficiaryList());

        DONE();
    }

    @Test
    @Order(10)
    public void delete_service_beneficiary_invalid_customer_token() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBBD-9410: Negative Test - Delete Service Beneficiary - Invalid customer token");
        GIVEN("A customer wants to delete a service beneficiary");
        ReadBeneficiaryResponse1 response1 = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        AND("The customer does not have a valid bearer token");
        String beneId = response1.getData().getBeneficiaryList().get(0).getBeneficiaryId();

        WHEN("The customer sends a valid request to delete the service beneficiary");
        this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiaryNoBearerToken(beneId);

        THEN("The client will respond with a 401 Unauthorised");
        DONE();
        refreshToken();
    }

    @Order(12)
    @Test
    @Tag("HappyPath")
    public void positive_test_user_sets_pin() throws Throwable {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC10 - Set Debit Card PIN - Success");
        TEST("AHBDB-7756: AC10 - User sets PIN on debit card - Success");
        GIVEN("I have a valid customer with accounts scope");
        ReadCard1 cards1 = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        assertTrue(cards1.getData().getReadCard1DataCard().size() > 0);

        String cardNumber = cards1.getData().getReadCard1DataCard().get(0).getCardNumber();
        String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        ReadCardCvv1 cardCVvDetails1 = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);

        AND("They have a valid debit card");

        WHEN("User makes a call to set their pin");
        String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails1.getData().getCardNumber()));
        WriteCardPinRequest1 pinSetRequest = WriteCardPinRequest1.builder()
                .cardExpiryDate(cardCVvDetails1.getData().getExpiryDate())
                .cardNumber(cardNumber)
                .cardNumberFlag("M")
                .lastFourDigits(StringUtils.right(cardNumber, 4))
                .pinServiceType("C")
                .pinBlock(pinBlock)
                .build();
        cardsApiFlows.setDebitCardPin(alphaTestUser, pinSetRequest, 200);
        DONE();
    }

    @Order(13)
    @Test
    public void customer_validates_PIN() throws Throwable {
        ignoreAllSwitch();
        envUtils.ignoreTestInEnv("Failing", Environments.ALL);
        TEST("AHBDB-10093: R5 SIT CRM/CB Integration Tests - Register and Re-Register Device");
        TEST("AHBDB-12283: Banking customer validates PIN");
        GIVEN("A customer has proceeded through the re register device flow");
        AND("Has a scope of accounts limited");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        String pinBlock = tripleDesUtil.encryptUserPin("1234", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        CardPinValidationRequest validatePinRequest = CardPinValidationRequest.builder().pin(pinBlock).build();

        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);

        assertEquals("device", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);


        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);
        String otpPass = otpCO.getPassword();

        otpApi.postOTPCode(alphaTestUser, 200, otpPass);

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();
        String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(userLoginRequestV2));

        this.certificateApi.uploadCertificate(alphaTestUser);

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(),
                ob.writeValueAsString(userLoginRequestV2), signedSignature, 204);

        UserLoginResponseV2 userLoginResponseV2 =
                authenticateApiV2.loginUser(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), false);

        Assertions.assertEquals(ACCOUNTS_LIMITED_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        String validPinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        CardPinValidationRequest validatePinRequest2 = CardPinValidationRequest.builder().pin(validPinBlock).build();

        String signedSignature2 = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(validatePinRequest2));

        this.certificateApi
                .validateCertificate(alphaTestUser, ob.writeValueAsString(validatePinRequest2), signedSignature2, 204);

        NOTE("Validating card pin correctly for the last time possible");
        authenticateApiV2.cardPinValidation(alphaTestUser, validatePinRequest2, cardId, signedSignature2);
        THEN("The platform will return a 200");
        AND("When they log back in they have the scope of 'accounts customer'");

        UserLoginResponseV2 loginResponseV22 = authenticateApiV2.loginUser(alphaTestUser);

        Assertions.assertEquals(ACCOUNT_SCOPE, loginResponseV22.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);
        DONE();
        refreshToken();
    }

    @Test
    @Order(14)
    public void banking_customer_registers_new_device() throws Throwable {
        ignoreAllSwitch();
        envUtils.ignoreTestInEnv("Failing", Environments.ALL);
        TEST("AHBDB-10093: R5 SIT CRM/CB Integration Tests - Register and Re-Register Device");
        TEST("AHBDB-12281: Banking customer registers new device");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        GIVEN("A banking user wants to register a new device");
        AND("The device has not been previously onboarded");
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());

        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());

        CardPinValidationRequest request = CardPinValidationRequest.builder().pin(pinBlock).build();

        WHEN("The customer initiates the register new device flow");
        LoginResponseV1 loginResponse = this.authenticateApiV2.registerNewDevice(alphaTestUser);
        THEN("The platform responds with a 201");
        AND("Returns a scope of device");
        assertEquals("device", loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);
        DONE();

//        AC5
        GIVEN("A banking customer wants to register a new device");
        AND("The customer receives an OTP on their phone");
        WHEN("The customer sends the OTP to the platform");
        otpApi.sendDestinationToOTP(alphaTestUser, 204);

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);
        String otpPass = otpCO.getPassword();

        THEN("Then the platform will respond with a 200 Response");
        otpApi.postOTPCode(alphaTestUser, 200, otpPass);
        DONE();

//        AC6
        GIVEN("Given a customer has completed the OTP flow");
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();
        String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(userLoginRequestV2));

        this.certificateApi.uploadCertificate(alphaTestUser);

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(),
                ob.writeValueAsString(userLoginRequestV2), signedSignature, 204);
        WHEN("They want to upload their certificate");
        THEN("A 204 is returned");
        AND("An event is sent to Kafka");
        DONE();

//        AC7, AC8, AC10
        GIVEN("A banking user wants to register a new device");
        AND("They have completed the OTP flow");
        AND("They have uploaded the certificate");

        UserLoginResponseV2 userLoginResponseV2 =
                authenticateApiV2.loginUser(alphaTestUser, userLoginRequestV2, alphaTestUser.getDeviceId(), false);
        WHEN("The customer logs in with the new device");
        THEN("The platform will return a 200 Response");
        AND("The scope will be set to accounts-limited");
        Assertions.assertEquals(ACCOUNTS_LIMITED_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);
        DONE();

        TEST("AHBDB-9233: Card Pin Validation");
        TEST("AHBDB-XXXX: AC1 - Validate Card Pin");
        GIVEN("The customer has logged into their new device");
        AND("They have the scope of accounts-limited");
        WHEN("They validate their card pin correctly");
        THEN("The platform will return a 200 Response");
        authenticateApiV2.cardPinValidation(alphaTestUser, request, cardId, signedSignature);
        DONE();

        GIVEN("The customer has successfully validated their card pin");
        WHEN("They log in using their new device");
        UserLoginResponseV2 loginResponseV2 = authenticateApiV2.loginUser(alphaTestUser);
        assertNotNull(loginResponseV2);
        THEN("The customer will have the scope accounts customer");
        assertEquals(ACCOUNT_SCOPE, loginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, loginResponseV2);
        AND("Their old device will be disabled");
        final UserLoginRequestV2 request2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        final String signedSignature2 =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        ob.writeValueAsString(request2),
                        alphaTestUser.getPreviousPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(alphaTestUser, alphaTestUser.getPreviousDeviceId(),
                ob.writeValueAsString(request2), signedSignature2, 204);

        ValidatableResponse error = authenticateApiV2.loginUserValidatable(alphaTestUser, request2,
                alphaTestUser.getPreviousDeviceId(), false);
        error.statusCode(401).assertThat();
        OBErrorResponse1 errorResponse = error.extract().as(OBErrorResponse1.class);
        assertEquals(UAE_ERROR_UNAUTHORIZED, errorResponse.getCode());
        assertEquals("Device is disabled", errorResponse.getMessage());

        NOTE("Log in to new device fresh");
        final UserLoginRequestV2 request3 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        var loginResponse3 = authenticateApiV2.loginUserProtected(alphaTestUser, request3, alphaTestUser.getDeviceId(), true);
        DONE();
    }

    @Order(10)
    @Test
    @Tag("HappyPath")
    public void positive_test_user_can_activate_their_virtual_debit_card() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC11 - Debit Card Management - Success");
        TEST("AHBDB-7757: AC11 - Debit Card Management - User activates card - Success");
        GIVEN("I have a valid customer with accounts scope");
        AND("They have a valid debit card");
        ActivateCard1 validActivateCard1 = ActivateCard1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .lastFourDigits(CREATED_CARD_NUMBER.substring(CREATED_CARD_NUMBER.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(CREATE_CARD_EXP_DATE)
                .modificationOperation(ModificationOperation.A)
                .operationReason("Operation reason")
                .build();

        WHEN("I receive a call to activate the customer's debit card");
        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1, 200);

        AND("The card is activated");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        DONE();
        refreshToken();
    }

    @Order(11)
    @Test
    @Tag("HappyPath")
    public void positive_test_get_cardCVV() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC9 - Get user's cards - Success");
        TEST("AHBDB-7759: AC9 - Retrieve user's cards - CVV - Success");
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

    @Order(11)
    @Test
    @Tag("HappyPath")
    public void positive_test_user_updates_spending_limits() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC11 - Debit Card Management - Success");
        TEST("AHBDB-7881: AC11 - Debit Card Management - User updates spending limits - Success");
        GIVEN("I have a test user with a created card");
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

    @Order(11)
    @Test
    @Tag("HappyPath")
    public void positive_test_user_updates_online_and_abroad_payments() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC11 - Debit Card Management - Success");
        TEST("AHBDB-7886: AC11 - Debit Card Management - User updates online and abroad payments - Success");
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

    @Test
    @Order(12)
    public void make_a_transfer_insufficient_funds_in_account() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8531: Make an Internal Transfer - Insufficient funds in account");
        TEST("AHBDB-8573: Make a Domestic Transfer - Insufficient funds in account");
        TEST("AHBDB-8548: Make a Legacy Transfer - Insufficient funds in account");
        GIVEN("A customer has a valid bank account");
        AND("The customer doesn't have enough money to make a transaction");
        AND("A valid consent payload is created");
        final OBWriteDomesticConsent4 internalConsent = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("11"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsent4 domesticConsent = PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "This is additional information",
                BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsent4 legacyConsent = PaymentRequestUtils.prepareLegacyConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "This is additional information",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("An internal transfer is initiated");
        THEN("The valid payment request is submitted");
        final OBErrorResponse1 internalConsentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, internalConsent, 422);

        final OBErrorResponse1 domesticConsentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, domesticConsent, 422);

        final OBErrorResponse1 legacyConsentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, legacyConsent, 422);

        AND("It will return a 422 Insufficient Funds error");
        Assertions.assertTrue(internalConsentResponse.getMessage().contains(INSUFFICIENT_FUNDS), "Error message was not as expected, " +
                "test expected : " + INSUFFICIENT_FUNDS);

        Assertions.assertTrue(domesticConsentResponse.getMessage().contains(INSUFFICIENT_FUNDS), "Error message was not as expected, " +
                "test expected : " + INSUFFICIENT_FUNDS);

        Assertions.assertTrue(legacyConsentResponse.getMessage().contains(INSUFFICIENT_FUNDS), "Error message was not as expected, " +
                "test expected : " + INSUFFICIENT_FUNDS);

        DONE();
    }

    @Test
    @Order(12)
    public void POS_insufficient_funds() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: R5 SIT Integration Tests");
        TEST("AHBDB-9515: Negative Test - Insufficient Funds in Account");
        GIVEN("A customer has a valid bank account with card and PIN details");
        AND("The customer wants to perform a POS transaction");
        AND("The customer does not have enough funds in their account");
        OBReadBalance1 balance = this.accountApi.getAccountBalances(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertEquals("0.00", balance.getData().getBalance().get(0).getAmount().getAmount());

        WHEN("ESB sends POS Transaction requests to the Core Banking adapter");
        this.cardProtectedApi.createCardPaymentError(alphaTestUser.getAccountNumber(), BigDecimal.TEN, 422);

        THEN("The Core Banking adapter passes the request to Transact and receives a 422 Insufficient Funds");
        DONE();
    }

    @Test
    @Order(12)
    public void customer_tries_to_check_balance_insufficient_funds_for_ATM_fee() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: R5 SIT CRM/CB Integration Tests");
        TEST("AHBDB-9519: Negative Test - Insufficient Funds for ATM Fee");
        GIVEN("A customer has a valid bank account with card and PIN details");
        AND("The customer wants to retrieve their account balance");
        AND("The customer does not have enough funds to pay the ATM fee");
        WHEN("ESB sends account balance request to the Core Banking adapter");
        OBErrorResponse1 error = this.accountApi.getAccountBalancesATMError(alphaTestUser.getAccountNumber(), 422);

        THEN("The Core Banking adapter passes the request to Transact and receives a 422 Insufficient Funds");
        Assertions.assertTrue(error.getCode().contains("INSUFFICIENT_FUNDS"));
        Assertions.assertTrue(error.getErrors().get(0).getMessage().contains("Unauthorised overdraft"));

        DONE();
    }

    @Test
    @Order(14)
    @Tag("HappyPath")
    public void happy_path_account_balance_retrieved_ATM() {
        ignoreAllSwitch();
        TEST("AHBDB-9512: R5 SIT CRM/CB Integration Tests - Balance Enquiry");
        TEST("AHBDB-9517: AC1 - Happy Path Scenario - Account balance retrieved from Transact");
        GIVEN("A customer has a valid account");
        WHEN("The customer requests their balance");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("charge", "true");
        OBReadBalance1 balances = this.accountApi.getAccountBalancesATM(alphaTestUser.getAccountNumber(), queryParams);

        THEN("The client returns the customer's balance");
        Assertions.assertNotNull(balances.getData().getBalance());

        DONE();
        refreshToken();
    }

    @Test
    @Order(14)
    public void customer_enters_invalid_details_checking_balance_ATM() {
        ignoreAllSwitch();
        TEST("AHBDB-9512: R5 SIT CRM/CB Integration Tests - Balance Enquiry");
        TEST("AHBDB-9518: Negative Test - Invalid Account Details");
        GIVEN("A customer has a valid bank account with card and PIN details");
        AND("The customer wants to retrieve their account balance");
        AND("The customer enters invalid information");
        WHEN("ESB sends account balance request to the Core Banking adapter");
        OBErrorResponse1 error = this.accountApi.getAccountBalancesATMError(generateRandomNumeric(12), 404);

        THEN("The Core Banking adapter passes the request to Transact and receives a 404 Account Not Found");
        Assertions.assertTrue(error.getCode().contains("UAE.ERROR.NOT_FOUND"));
        Assertions.assertTrue(error.getErrors().get(0).getMessage().contains("No records were found"));

        DONE();
    }

    @Test
    @Order(14)
    public void internal_transfer_creditor_account_not_found() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8533: Make an Internal Transfer - Creditor account not found");
        GIVEN("A customer has a valid bank account");
        AND("A valid consent payload is created");
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                "123456781191",
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("An internal transfer is initiated");
        THEN("The valid payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent, 404);

        AND("It will return a 404 account not found");
        Assertions.assertTrue(consentResponse.getMessage().contains(CREDITOR_NOT_FOUND),
                "Error message was not as expected, expected: " + CREDITOR_NOT_FOUND);

        DONE();
    }

    @Test
    @Order(14)
    public void domestic_transfer_creditor_account_not_found() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8574: Make a Domestic Transfer - Creditor account not found");
        GIVEN("A customer has a valid bank account");
        AND("A valid consent payload is created");
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                "GB24BARC202016300934591",
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "Unstructured",
                BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("A domestic transfer to a non-AHB Account is initiated");
        THEN("The valid payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent, 404);

        AND("It will return a 404 account not found");
        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_IBAN),
                "Error message was not expected, expected: " + INVALID_IBAN);

        DONE();
    }

    @Test
    @Order(14)
    public void legacy_transfer_creditor_account_not_found() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8549: Make a Legacy Transfer - Creditor account not found");
        GIVEN("A customer has a valid bank account");
        AND("A valid consent payload is created");
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareLegacyConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                "GB24BARC202016300934591",
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("A domestic payment to an AHB Account is initiated");
        THEN("The valid payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent, 404);

        AND("It will return a 404 account not found");
        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_IBAN),
                "Error message was not as expected, expected: " + INVALID_IBAN);

        DONE();
    }

    @ParameterizedTest
    @CsvSource({"ACCOUNTNUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, GB94BARC10203410093459, ACCOUNT_IBAN, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNTNUMBER, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, -1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AEF, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AED, qwertyuioplkjfffffuytrdxcvbnjki, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AED, Reference, qwertyuioplkjfffffuytrdxcvbnjki"})
    @Order(14)
    public void legacy_transfer_invalid_consent_fields(String accountType1, String creditorAccount, String accountType2, String amount,
                                                       String currency, String reference, String unstructured) {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8550: Make a Legacy Transfer - Invalid consent fields");
        GIVEN("A customer has a valid bank account");
        AND("An invalid consent payload is created");
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareLegacyConsent(
                alphaTestUser.getAccountNumber(),
                accountType1,
                creditorAccount,
                accountType2,
                new BigDecimal(amount),
                currency,
                reference,
                unstructured,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("A domestic transfer to an AHB Account is initiated");
        THEN("The invalid payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent, 400);

        AND("It will return a 400 bad request error");
        Assertions.assertTrue(consentResponse.getMessage().contains("Bad request"),
                "Error message expected to contain: Bad request");
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"ACCOUNTNUMBER, 016489511001, ACCOUNT_NUMBER, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, 12345678901234567, ACCOUNT_NUMBER, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, 016489511001, ACCOUNTNUMBER, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, 016489511001, ACCOUNT_NUMBER, -1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, 016489511001, ACCOUNT_NUMBER, 1, AEF, Reference, unstructured",
                "ACCOUNT_NUMBER, 016489511001, ACCOUNT_NUMBER, 1, AED, qwertyuioplkjfffffuytrdxcvbnjki, unstructured",
                "ACCOUNT_NUMBER, 016489511001, ACCOUNT_NUMBER, 1, AED, Reference, qwertyuioplkjfffffuytrdxcvbnjki"})
    @Order(14)
    public void internal_transfer_invalid_consent_fields(String accountType1, String creditorAccount, String accountType2, String amount,
                                                         String currency, String reference, String unstructured) {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8535: Make an Internal Transfer - Invalid consent fields");
        GIVEN("A customer has a valid bank account");
        AND("An invalid consent payload is created");
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                accountType1,
                creditorAccount,
                accountType2,
                new BigDecimal(amount),
                currency,
                reference,
                unstructured,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("An internal transfer is initiated");
        THEN("The invalid payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent, 400);

        AND("It will return a 400 bad request error");
        Assertions.assertTrue(consentResponse.getMessage().contains("Bad request"),
                "Error message expected to contain: Bad request");

        DONE();
    }

    @ParameterizedTest
    @CsvSource({"ACCOUNTNUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, GB94BARC10203410093459, ACCOUNT_IBAN, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNTNUMBER, 1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, -1, AED, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AEF, Reference, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AED, qwertyuioplkjfffffuytrdxcvbnjki, unstructured",
                "ACCOUNT_NUMBER, TL380010012345678910106, ACCOUNT_IBAN, 1, AED, Reference, qwertyuioplkjfffffuytrdxcvbnjki"})
    @Order(14)
    public void domestic_transfer_invalid_consent_fields(String accountType1, String creditorAccount, String accountType2, String amount,
                                                       String currency, String reference, String unstructured) {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8575: Make a Domestic Transfer - Invalid consent fields");
        GIVEN("A customer has a valid bank account");
        AND("An invalid consent payload is created");
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                accountType1,
                creditorAccount,
                accountType2,
                new BigDecimal(amount),
                currency,
                reference,
                unstructured,
                BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("A domestic transfer to a non-AHB Account is initiated");
        THEN("The invalid payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent, 400);

        AND("It will return a 400 bad request error");
        Assertions.assertTrue(consentResponse.getMessage().contains("Bad request"),
                "Error message expected to contain: Bad request");

        DONE();
    }

    @Test
    @Order(14)
    public void customer_uses_account_not_theirs() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8542: Make an Internal Transfer - Customer uses account that is not theirs");
        TEST("AHBDB-8576: Make a Domestic Transfer - Customer uses account that is not theirs");
        TEST("AHBDB-8555: Make a Legacy Transfer - Customer uses account that is not theirs");
        GIVEN("A customer has a valid bank account");
        WHEN("Payment consent is created using a bank account not attached to the customer");
        final OBWriteDomesticConsent4 internalConsent = PaymentRequestUtils.prepareInternalConsent(
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal("11"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsent4 domesticConsent = PaymentRequestUtils.prepareDomesticConsent(
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "This is additional information",
                BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsent4 legacyConsent = PaymentRequestUtils.prepareLegacyConsent(
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "This is additional information",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        AND("The payment consent request is submitted");
        final OBErrorResponse1 internalConsentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, internalConsent, 403);

        final OBErrorResponse1 domesticConsentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, domesticConsent, 403);

        final OBErrorResponse1 legacyConsentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, legacyConsent, 403);

        THEN("A 403 Unauthorised error will be returned");
        Assertions.assertTrue(internalConsentResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);

        Assertions.assertTrue(domesticConsentResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);

        Assertions.assertTrue(legacyConsentResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);

        DONE();
    }

    @Test
    @Order(14)
    public void internal_transfer_source_and_destination_account_are_the_same() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8544: Make an Internal Transfer - Source and Destination account are the same");
        GIVEN("A customer has a valid bank account");
        WHEN("Payment consent is created with creditor and debtor account the same");
        OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        AND("The payment consent request is submitted");
        OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        THEN("A 400 bad request error will be returned");
        Assertions.assertTrue(consentResponse.getMessage().contains(SOURCE_DESTINATION_SAME),
                "Error message was not as expected, test expected : " + SOURCE_DESTINATION_SAME);

        DONE();
        refreshToken();
    }

    @Test
    @Order(15)
    @Tag("HappyPath")
    public void happy_path_domestic_transfer() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC5 - Make a domestic transfer to a non-AHB Account - Success");
        TEST("AHBDB-8146: AC5 - Happy Path - Make a Domestic Transfer to a non-AHB Account");
        GIVEN("A customer has a valid bank account");
        WHEN("A domestic transfer to a non-AHB Account is initiated");
        OBWriteDomesticConsent4 consent = createDomesticConsent(10, "BORNE_BY_DEBTOR");
        OBWriteDomesticConsentResponse5 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WriteDomesticPayment1 paymentRequest = PaymentRequestUtils.prepareDomesticTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("10"),
                "EDU",
                "unstructured",
                WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_DEBTOR,
                consentResponse.getData().getInitiation().getEndToEndIdentification());

        OBWriteDomesticResponse5 paymentResponse = this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);

        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(paymentResponse);

        DONE();
        refreshToken();
    }

    @Test
    @Order(15)
    @Tag("HappyPath")
    @Tag("Individual")
    public void happy_path_internal_transfer_different_account_holders() {
        envUtils.ignoreTestInEnv("AHBDB-15221", Environments.SIT);
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC3 - Make an internal transfer - Success");
        TEST("AHBDB-8532: AC3 - Happy Path - Make an Internal Transfer - Different Account Holders");
        GIVEN("A customer has a valid bank account");
        WHEN("An internal transfer is initiated");
        OBWriteDomesticConsent4 consent = createInternalConsentToDifferentAccount(10);

        OBWriteDomesticConsentResponse5 consentResponse = this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        new BigDecimal("10"), "EDU", "1",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(transferResponse);

        DONE();
        refreshToken();
    }

    @Test
    @Order(15)
    @Tag("HappyPath")
    public void happy_path_legacy_transfer() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC4 - Make a domestic transfer to an AHB Account - Success");
        TEST("AHBDB-8145: AC4 - Happy Path - Make a Legacy Transfer");
        GIVEN("A customer has a valid bank account");
        WHEN("A domestic transfer to an AHB Account is initiated");
        OBWriteDomesticConsent4 consent = createLegacyConsent(10);

        OBWriteDomesticConsentResponse5 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        AND("There are no charges");
        Assertions.assertNotNull(consentResponse);
        Assertions.assertNull(consentResponse.getData().getCharges());

        OBWriteDomestic2 paymentRequest = PaymentRequestUtils.prepareLegacyRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getLegacyIban(),
                ACCOUNT_IBAN,
                new BigDecimal("10"),
                consentResponse.getData().getInitiation().getRemittanceInformation().getReference(),
                "1",
                consentResponse.getData().getInitiation().getEndToEndIdentification(),
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        OBWriteDomesticResponse5 paymentResponse = domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);

        THEN("Their account and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(paymentResponse);

        DONE();
        refreshToken();
    }

    @ParameterizedTest
    @ValueSource(strings = {"BORNE_BY_DEBTOR", "BORNE_BY_CREDITOR", "SHARED"})
    @Order(15)
    public void happy_path_domestic_transfer_different_charges(String fee) {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC5 - Make a domestic transfer to a non-AHB Account - Success");
        TEST("AHBDB-8564: AC5 - Happy Path - Make a Domestic Transfer to a non-AHB Account with Different Charges");
        GIVEN("A customer has a valid bank account");
        AND("The payment consent has a valid charges field");
        OBWriteDomesticConsent4 consent = createDomesticConsent(1, fee);
        OBWriteDomesticConsentResponse5 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());
        Assertions.assertEquals(OBChargeBearerType1Code.valueOf(fee), consentResponse.getData().getCharges().get(0).getChargeBearer());

        WHEN("A domestic transfer to a non-AHB Account is initiated");
        WriteDomesticPayment1 paymentRequest = PaymentRequestUtils.prepareDomesticTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "EDU",
                "unstructured",
                WriteDomesticPayment1RequestedChargeCodePaymentBearer.valueOf(fee),
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        AND("Their fee matches that given by the charges field in the consent");
        OBWriteDomesticResponse5 paymentResponse = this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);

        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(paymentResponse);

        DONE();
    }

    @Test
    @Order(15)
    public void happy_path_internal_transfer_same_account_holder() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC3 - Make an internal transfer - Success");
        TEST("AHBDB-8144: AC3 - Happy Path - Make an Internal Transfer - Same Account Holder");
        GIVEN("A customer has a valid savings account");
        AND("The customer has a valid current account");
        WHEN("An internal transfer is initiated");
        OBWriteDomesticConsent4 consent = createInternalConsentBetweenSavingsAndCurrentAccount(10);

        OBWriteDomesticConsentResponse5 consentResponse = this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 transferRequest = PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                currentAccountNumber,
                ACCOUNT_NUMBER,
                new BigDecimal(10), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

        THEN("Their account balances will be modified appropriately in T24");
        Assertions.assertNotNull(transferResponse);

        DONE();
    }

    @Test
    @Order(16)
    public void no_limits_transferring_between_accounts_belonging_to_same_customer() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: AC3 - Make an internal transfer - Success");
        TEST("AHBDB-8665: AC3 - Happy Path - There are no transfer limits when transferring between accounts belonging to the same customer");
        String maxPaymentLimit = paymentConfiguration.getTransferLimit();
        int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 100;

        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(testTransferAmount));

        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);
        GIVEN("A customer has a valid current account");
        AND("The customer has a valid savings account");

        WHEN("An internal transfer is initiated between those two accounts with amount over the maximum transfer limit");
        OBWriteDomesticConsent4 consent = createInternalConsentBetweenSavingsAndCurrentAccount(testTransferAmount);

        OBWriteDomesticConsentResponse5 consentResponse = this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 transferRequest = PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                currentAccountNumber,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

        THEN("Both account balances will be modified appropriately in T24");
        Assertions.assertNotNull(transferResponse);

        DONE();
    }

    @Test
    @Order(17)
    public void customer_adds_money_over_max_payment_limit() {
        ignoreAllSwitch();
        GIVEN("A customer has a valid bank account");
        WHEN("Money is transferred to their account");
        int testTransferAmount = Integer.parseInt(paymentConfiguration.getMaxPaymentLimit()) + 10;
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(testTransferAmount));
        THEN("The customer has money to spend/transfer");

        DONE();
    }

    @Test
    @Order(18)
    public void internal_transfer_one_transaction_over_transfer_limit() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8568: Make an Internal Transfer - One transaction over transfer limit");
        int testTransferAmount = Integer.parseInt(paymentConfiguration.getMaxPaymentLimit()) + 10;

        GIVEN("A customer has a valid bank account");
        WHEN("Payment consent is created with a value over the maximum transfer limit");
        OBWriteDomesticConsent4 consent = createInternalConsentToDifferentAccount(testTransferAmount);

        AND("The payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent, 422);

        THEN("A 422 will be returned");
        Assertions.assertTrue(consentResponse.getMessage().contains(LIMIT_EXCEEDED),
                "Error Message was not expected, expected: " + LIMIT_EXCEEDED);

        AND("The transaction over the limit cannot be viewed in the booked transactions");
        DONE();
    }

    @Test
    @Order(18)
    public void legacy_transfer_one_transaction_over_transfer_limit() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8566: Make a Legacy Transfer - Customer makes one transaction over limit");
        GIVEN("A customer has a valid bank account");
        int testTransferAmount = Integer.parseInt(paymentConfiguration.getMaxPaymentLimit()) + 10;

        WHEN("Payment consent is created with a value over the maximum transfer limit");
        OBWriteDomesticConsent4 consent = createLegacyConsent(testTransferAmount);

        AND("The payment consent request is submitted");
        OBErrorResponse1 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent, 422);

        THEN("A 422 will be returned");
        Assertions.assertTrue(consentResponse.getMessage().contains(LIMIT_EXCEEDED),
                "Error Message was not expected, expected: " + LIMIT_EXCEEDED);

        AND("The transaction over the limit cannot be viewed in the booked transactions");
        DONE();
    }

    @Test
    @Order(18)
    public void domestic_transfer_one_transaction_over_transfer_limit() {
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8578: Make a Domestic Transfer - Customer makes one transaction over limit");
        GIVEN("A customer has a valid bank account");
        int testTransferAmount = Integer.parseInt(paymentConfiguration.getMaxPaymentLimit()) + 10;

        WHEN("Payment consent is created with a value over the maximum transfer limit");
        OBWriteDomesticConsent4 consent = createDomesticConsent(testTransferAmount, "BORNE_BY_DEBTOR");

        OBErrorResponse1 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent, 422);

        THEN("A 422 will be returned");
        Assertions.assertTrue(consentResponse.getMessage().contains(LIMIT_EXCEEDED),
                "Error Message was not expected, expected: " + LIMIT_EXCEEDED);

        AND("The transaction over the limit cannot be viewed in the booked transactions");
        refreshToken();
        DONE();
    }

    @Test
    @Order(19)
    public void multiple_transactions_totalling_over_the_limit() {
        envUtils.ignoreTestInEnv("AHBDB-15221", Environments.SIT);
        ignoreAllSwitch();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8569: Make an Internal Transfer - Multiple transactions that total over transfer limit");
        TEST("AHBDB-8577: Make an Domestic Transfer - Multiple transactions that total over transfer limit");
        TEST("AHBDB-8567: Make an Legacy Transfer - Multiple transactions that total over transfer limit");
        GIVEN("A customer has a valid bank account");
        int testTransferAmount = Integer.parseInt(paymentConfiguration.getMaxPaymentLimit()) + 3;

        WHEN("Multiple payment requests are created which total over the maximum transfer limit");
        int individualTransactionValue = testTransferAmount / 3;
        OBWriteDomesticConsent4 consent = createInternalConsentToDifferentAccount(individualTransactionValue);
        OBWriteDomesticConsent4 consent2 = createDomesticConsent(individualTransactionValue, "BORNE_BY_DEBTOR");
        OBWriteDomesticConsent4 consent3 = createLegacyConsent(individualTransactionValue);

        AND("The payment requests are submitted");
        //First Transaction
        internalPaymentsStepUpAuthBiometrics();

        OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 transferRequest = PaymentRequestUtils.prepareInternalTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(individualTransactionValue), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);

        internalPaymentsStepUpAuthBiometrics();

        OBWriteDomesticConsentResponse5 consentResponse2 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse2.getData().getConsentId());

        OBWriteDomestic2 transferRequest2 = PaymentRequestUtils.prepareInternalTransferRequest(
                consentResponse2.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(individualTransactionValue), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse2 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest2);
        Assertions.assertNotNull(transferResponse2);

        //Second Transaction
        /*internalPaymentsStepUpAuthBiometrics();

        OBWriteDomesticConsentResponse5 consentResponse2 =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent2);
        Assertions.assertNotNull(consentResponse2.getData().getConsentId());

        WriteDomesticPayment1 transferRequest2 = PaymentRequestUtils.prepareDomesticTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "EDU",
                "unstructured",
                WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse2 =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest2);
        Assertions.assertNotNull(transferResponse2);*/

        //Third Transaction
        internalPaymentsStepUpAuthBiometrics();

        OBErrorResponse1 consentResponse3 =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent, 422);

        THEN("A 422 bad request will be returned");
        Assertions.assertTrue(consentResponse3.getMessage().contains(LIMIT_EXCEEDED), "Error message was not as " +
                "expected, test expected : " + LIMIT_EXCEEDED);

        AND("All previous transactions can be viewed in the booked transactions");
        AND("The transaction over the limit cannot be viewed in the booked transactions");
        DONE();
    }

    /*#######################################################
       TestCaseID:AHBDB-15298
       Description:	[POST internal/v1/international-payments] Positive flow 200 Response code and user should be able to do international payment when user provides a valid data without optional parameters also
       CreatedBy:Shilpi Agrawal
       UpdatedBy:
       LastUpdatedOn:
       Comments:Due to defect Can not pass full description in Purpose
       AHBDB-15316-
       AHBDB-15390 - Not able to do payment without RemittanceInformation unstructured as blank even though it is Optional Parameter
    #######################################################*/
    @ParameterizedTest
    @Order(19)
    //Test Data for Sanctioned country
    /*@CsvSource({
            "10.00,'IR710570029971601460641001',BKBPIRTH,IR, AED, '', BORNE_BY_CREDITOR, CHC-Charitable",
            "11.00,'',BOELKPP1XXX,KP, AED, '', BORNE_BY_CREDITOR, CHC-Charitable",
            "1, '','ARBXSYDA',SY, AED,, BORNE_BY_DEBTOR, OAT-Own Account",
            "4,'','BCCUCUHHSLB',CU,AED,,BORNE_BY_DEBTOR, EDU-purpose"
    })


    @CsvSource({
            "10.00,'',ICICINBB012,IN, INR, 'ICI0000012', BORNE_BY_CREDITOR, CHC-Charitable contributions",
            "1, '',ROYCCAT2VIC,CA, CAD, 010, SHARED, EMI-Equated monthly instalments",
            "1, IE64IRCE92050112345678,'IRCEIE2D',IE, EUR, 981234, BORNE_BY_DEBTOR, OAT-Own Account Transfer",
            "2,AT483200000012345864,'RLNWATWW',AU,AUD,032732,BORNE_BY_DEBTOR, RNT-Rent Payments",
            "3,JO71CBJO0000000000001234567890,'CBJOJOAX',JO,JOD,'',BORNE_BY_DEBTOR, RNT-Rent Payments",
            "4,'','IIIGGB22',GB,GBP,200415,BORNE_BY_DEBTOR, EDU-Educational Support",
            "4,'GB33BUKB20201555555555','BUKBGB22',GB,GBP,202678,BORNE_BY_DEBTOR, EDU-Educational Support",
            "5,'','IZZBOMRU',OM,OMR,'',BORNE_BY_DEBTOR, EDU-purpose",
            "6,'','IBOCUS44',US,USD,'',BORNE_BY_DEBTOR, RNT-Rent Payments"
    })*/
    @CsvSource({
            "2,AT483200000012345864,'RLNWATWW',AU,AUD,032732,BORNE_BY_DEBTOR, RNT-Rent Payments"
    })


    public void happyflow_international_payment_without_optionalParameters(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment) {
        TEST("AHBDB-8621 / AHBDB-8625 - Make International Transfer request:: ");
        TEST("AHBDB-15298 - [POST internal/v1/international-payments] Positive flow 200 Response code and user should be able to do international payment when user provides a valid data without optional parameters also With Following Test Data" + " Payment Amount" + paymentAmount + " IBan " + iBan + "swift Code as"+swiftCode+"currency Code as "+currencyCode+"Currency Code as "+currencyCode+"Secondary Identification(SortCode/BSB/TransitCode/IFSC) as"+scIfscBsbTransitNum+" charge Bearer Type " + chargeBearerType + "pupose Of payment " + puposeOfpayment);

        GIVEN("I have a valid access token and account scope and bank account");
        //need to remove 2700 & 2701
        /*setupTestUser();
        this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);*/


        debtorAccountNumber = alphaTestUser.getAccountNumber();
        creditorAccountNumber = temenosConfig.getCreditorIban();
        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                debtorAccountNumber,
                BigDecimal.valueOf(5000.00));
        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");


        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);
        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);
        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

       beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = internationalTransferRequestDataValid(consentResponse.getData().getConsentId(), iBan);
        transferRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        transferRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        transferRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        transferRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        transferRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        transferRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        transferRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        transferRequest.getData().getInitiation().setEndToEndIdentification(consentResponse.getData().getInitiation().getEndToEndIdentification());
        transferRequest.getData().getInitiation().getSupplementaryData().setDebitCurrency("");
        transferRequest.getData().getInitiation().getSupplementaryData().setBeneficiaryNickName("");
        transferRequest.getData().getInitiation().getSupplementaryData().setOrgRefNumber("");
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2("");
        /** TODO Due to Defect it is commented AHBDB-15390 */
        //transferRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        transferRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);

        assertEquals(OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED,paymentResponse.getData().getStatus());

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);


        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 transactionResponse = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());

        DONE();
    }


    @Test
    @Order(19)
    @Tag("HappyPath")
    public void get_account_transactions_from_transact() {
        envUtils.ignoreTestInEnv("AHBDB-15221", Environments.SIT);
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC2 - Get Account Transactions - Success");
        TEST("AHBDB-7053: AC2 Happy Path Scenario - Get Account Transactions - Success");
        GIVEN("A customer has created a transaction to another customer");
        //Done using other tests in set

        WHEN("I call Transact to get the customer's transactions");
        OBReadTransaction6 transactions =
                this.accountApi.getAccountTransactionsWithRange(this.alphaTestUser,
                        this.alphaTestUser.getAccountNumber());

        THEN("The API will return 200 OK");
        AND("I receive a list of the customer's transactions");
        Assertions.assertNotNull(transactions.getData(), "No transactions were found");

        WHEN("I check Transact's User Interface");
        THEN("The transactions will match the list returned by the API");
        //Check the API call returns the correct information

        DONE();
    }

    @Test
    @Order(19)
    @Tag("HappyPath")
    void get_locked_amount_from_transact() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC3 Get Account Balances - Success");
        TEST("AHBDB-8011: AC3 Get Account Balances - Locked Amount - Success");
        GIVEN("A customer has created an account");
        WHEN("Calling get locked amount api");
        OBReadTransaction6 lockedAmount = this.accountApi.getLockedAmount(alphaTestUser,
                alphaTestUser.getAccountNumber(), 1);

        THEN("Status code 200 is returned");
        assertNotNull(lockedAmount);

        AND("Locked Amount Data is not null");
        OBReadDataTransaction6 lockedAmountData = lockedAmount.getData();
        assertNotNull(lockedAmountData);

        DONE();
    }

    @Test
    @Order(19)
    @Tag("HappyPath")
    public void get_account_balances_from_transact() {
        ignoreAllSwitch();
        TEST("AHBDB-7060: AC3 - Get Account Balances - Success");
        TEST("AHBDB-7054: AC3 Happy Path Scenario - Get Account Balances - Success");
        GIVEN("The client wants a list of a customer's account balances");
        WHEN("I call transact to get the customer's account balances");
        OBReadBalance1 balance =
                this.accountApi.getAccountBalances(this.alphaTestUser,
                        this.alphaTestUser.getAccountNumber());

        THEN("The API will return 200 OK");
        AND("I receive a list of the customer's account balances");
        Assertions.assertNotNull(balance);

        WHEN("I check Transact's User Interface");
        THEN("The account balances match the list returned by the API");
        //Check the API call returns the correct information

        DONE();
    }

    @Order(19)
    @Test
    @Tag("HappyPath")
    public void happy_path_customer_makes_POS_transaction() {
        ignoreAllSwitch();
        TEST("AHBDB-9814: AC1 - Customer makes a POS Transaction - Success");
        TEST("AHBDB-9513: AC1 - Happy Path Scenario - Customer makes a POS Transaction - Success");
        GIVEN("A customer has a valid bank account with card and PIN details");
        AND("The customer wants to perform a POS transaction");
        WHEN("ESB sends POS Transaction to the Core Banking adapter");
        OBWriteCardPaymentResponse1 response = cardProtectedApi.createCardPayment(alphaTestUser.getAccountNumber(), BigDecimal.TEN);

        THEN("The Core Banking adapter passes the request to Transact and receives a success response");
        assertNotNull(response);

        AND("The request can be viewed in T24");
        DONE();
    }

    @Test
    @Order(19)
    @Tag("HappyPath")
    @Tag("Individual")
    void happy_path_customer_makes_withdrawal_AHB_ATM() {
        ignoreAllSwitch();
        final String debtorAccountId = alphaTestUser.getAccountNumber();
        final String creditorAccountId = temenosConfig.getCreditorAccountId();

        BigDecimal PAYMENT_AMOUNT = BigDecimal.TEN;
        WHEN("Calling card withdrawal api");
        OBWriteCardWithdrawalResponse1 response = cardProtectedApi.createCardWithdrawal(debtorAccountId,
                creditorAccountId,
                PAYMENT_AMOUNT);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Card withdrawal response has been returned");
        DONE();
    }

    @Test
    @Order(19)
    public void POS_customer_enters_incorrect_information() {
        TEST("AHBDB-9814: R5 SIT Integration Tests");
        TEST("AHBDB-9514: Negative Test - Customer enters incorrect information");
        ignoreAllSwitch();
        GIVEN("A customer has a valid bank account with card and PIN details");
        AND("The customer wants to perform a POS transaction");
        AND("The customer inputs their information incorrectly");
        String debtorId = "012345678901";

        WHEN("ESB sends POS Transaction request to the Core Banking adapter");
        this.cardProtectedApi.createCardPaymentError(debtorId, BigDecimal.TEN, 404);

        THEN("The Core Banking adapter passes the request to Transact and receives a 400 Bad Request");
        DONE();
    }

    @Test
    @Order(20)
    public void customer_blocks_card() {
        ignoreAllSwitch();
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to block their debit card");

        UpdateCardParameters1 validUpdateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internationalUsage(false)
                .internetUsage(false)
                .nationalDisATM(false)
                .nationalPOS(false)
                .nationalSwitch(false)
                .nationalUsage(false)
                .build();

        THEN("A 200 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, validUpdateCardParameters1, 200);

        AND("The users card is shown as having all payments blocked");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");


        AND("The user can unblock their card");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internationalUsage(true)
                .internetUsage(true)
                .nationalDisATM(true)
                .nationalPOS(true)
                .nationalSwitch(true)
                .nationalUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("All payments are turned back on");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");

        DONE();
    }

    @Test
    @Order(20)
    public void positive_test_customer_deactivates_their_card() {
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        ignoreAllSwitch();
        GIVEN("I have a valid customer with accounts scope");
        String cif = this.customerApiV1.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();

        AND("The user has a created card");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("The user makes a call to deactivate their debit card");
        WriteCardActivation1 cardActivation = WriteCardActivation1.builder()
                .modificationOperation(CardModificationOperation1.DEACTIVATE_PHYSICAL_VIRTUAL)
                .operationReason("Operation reason")
                .build();

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(cardActivation, cardId, cif, 200);

        AND("The customer's card is deactivated");
        ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertFalse(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        DONE();
        refreshToken();
    }

    @Test
    @Order(22)
    public void banking_customer_changes_passcode_success() {
        ignoreAllSwitch();
        TEST("AHBDB-10096: AC1 Set new password - 200 OK");
        TEST("AHBDB-11423: AC1 Customer changes passcode as a banking user - Success");
        GIVEN("A customer has been authenticated (completed login flow)");
        AND("The customer has an elevated weight of 11");
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(changePasscodeMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest2 = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(changePasscodeMinWeightExpected).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest2);

        WHEN("The client attempts to update the password for the customer with a valid password");
        String oldPassword2 = alphaTestUser.getUserPassword();
        final String newPassword2 = "12345678901234567890aBc!";
        final UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .userPassword(newPassword2)
                .build();

        THEN("The platform returns a 200 OK");
        User updatedUser2 = authenticateApiV2.patchUserCredentials(alphaTestUser, updateUserRequest);
        Assertions.assertNotNull(updatedUser2);
        alphaTestUser.setUserPassword(newPassword2);

        UserLoginRequestV2 userLoginRequest = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();
        UserLoginResponseV2 userLoginResponse = authenticateApiV2.loginUserProtected(alphaTestUser, userLoginRequest, alphaTestUser.getDeviceId(), true);

        DONE();
    }

    @Test
    @Order(23)
    public void banking_customer_is_locked_out_of_account() {
        envUtils.ignoreTestInEnv("Breaks when customer changes passcode, can ignore that one to run this one", Environments.ALL);
        ignoreAllSwitch();
        TEST("AHBDB-10096: AC2 Incorrect existing password (3 attempts or less) - 403 Forbidden");
        TEST("AHBDB-10096: AC3 Incorrect existing password (4 or more attempts) - 423 Locked");
        TEST("AHBDB-11425: AC2 - Banking Customer enters incorrect passcode over 4 times");

        AND("AND the customer has an elevated weight of 11");
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(changePasscodeMinWeightExpected).scope("customer").build());

        AND("The existing password supplied by the user is incorrect");
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password("incorrectPassword").scope("customer").weight(changePasscodeMinWeightExpected).build();

        AND("The user has made less than 3 attempts or less");
        WHEN("Authenticate the customer using step up auth (re-authenticate the user) ");
        THEN("DTP returned error response 403 forbidden");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 errorResponse1 = authenticateApiV2.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals("UAE.AUTH.STEP_UP_AUTH_REQUIRED", errorResponse1.getCode(), "Code Error not as expected, expected " + "UAE.AUTH.STEP_UP_AUTH_REQUIRED" + " but it was" + errorResponse1.getCode());

        uk.co.deloitte.alpha.error.model.OBErrorResponse1 errorResponse2 = authenticateApiV2.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals("UAE.AUTH.STEP_UP_AUTH_REQUIRED", errorResponse2.getCode(), "Code Error not as expected, expected " + "UAE.AUTH.STEP_UP_AUTH_REQUIRED" + " but it was" + errorResponse1.getCode());

        uk.co.deloitte.alpha.error.model.OBErrorResponse1 errorResponse3 = authenticateApiV2.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        assertEquals("UAE.AUTH.STEP_UP_AUTH_REQUIRED", errorResponse3.getCode(), "Code Error not as expected, expected " + "UAE.AUTH.STEP_UP_AUTH_REQUIRED" + " but it was" + errorResponse1.getCode());

        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();

        AND("If the user has made 3 attempts the user will be logged out ");
        this.authenticateApiV2.loginUserProtectedError(alphaTestUser, userLoginRequestV2, 423);


        THEN("DTP returned error response 423 locked account");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 errorResponse5 = authenticateApiV2.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
        assertEquals("UAE.OTP.LIMIT_REACHED", errorResponse5.getCode(), "Code Error not as expected, expected " + "UAE.OTP.LIMIT_REACHED" + " but it was" + errorResponse1.getCode());

        DONE();
    }

    @Test
    public void spending_limits_are_reset_every_day() {
        /**
         * TODO :: This should never be run in regression pack, only when checking spending limits the next day
         */
        envUtils.ignoreTestInEnv(Environments.ALL);
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        TEST("AHBDB-8664: AC3 - Happy Path - Internal Transfer Limits are reset each day");
        TEST("AHBDB-8662: AC4 - Happy Path - Legacy Transfer Limits are reset each day");
        TEST("AHBDB-8659: AC5 - Happy Path - Domestic Transfer Limits are reset each day");
        int testTransferAmount = Integer.parseInt(paymentConfiguration.getMaxPaymentLimit());
        int individualTransactionValue = 10000;

        GIVEN("A customer has a valid bank account");
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser.setUserId("473fb89c-481a-349a-8956-c7a79598da5f");
        this.alphaTestUser.setUserPassword("12345678901234567890aBc!");
        this.alphaTestUser.setAccountNumber("019414952001");
        this.alphaTestUser.setDeviceId("bb191ab5-cceb-4bfc-8e7e-225a42a12fb2");
        this.alphaTestUser.setPrivateKeyBase64("AMMPhmfS9eAfUpONvJUlgUfzJMCnam9DrU1zyf/BqcaI");
        this.alphaTestUser.setPublicKeyBase64("A3Q6PyG8dAQok2+ZAsgKuZ6QGxH0A+bQe+RMqddCkYbB");
        currentAccountNumber = "019414952002";
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .build();
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginExistingUserProtected(userLoginRequestV2, alphaTestUser);
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        WHEN("A transfer is initiated with amount close to but not above the maximum transfer limit");
        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        //This is all done with the previous tests in this class

        WHEN("A transfer is initiated the next day with amount close to but not above the maximum transfer limit");
        OBWriteDomesticConsent4 internalConsent = createInternalConsentToDifferentAccount(individualTransactionValue);
        OBWriteDomesticConsent4 domesticConsent = createDomesticConsent(individualTransactionValue + 1, "BORNE_BY_DEBTOR");
        OBWriteDomesticConsent4 legacyConsent = createLegacyConsent(individualTransactionValue + 2);

        //First Transaction
        internalPaymentsStepUpAuthBiometrics();

        OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, internalConsent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 transferRequest = PaymentRequestUtils.prepareInternalTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(individualTransactionValue), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        /*final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);
         */

        //Second Transaction
        internalPaymentsStepUpAuthBiometrics();

        OBWriteDomesticConsentResponse5 consentResponse2 =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, domesticConsent);
        Assertions.assertNotNull(consentResponse2.getData().getConsentId());

        WriteDomesticPayment1 transferRequest2 = PaymentRequestUtils.prepareDomesticTransferRequest(
                consentResponse2.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(individualTransactionValue + 1),
                "EDU",
                "unstructured",
                WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse2 =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest2);
        Assertions.assertNotNull(transferResponse2);

        //Third Transaction
        OBWriteDomesticConsentResponse5 consentResponse3 = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, legacyConsent);
        Assertions.assertNotNull(consentResponse3.getData().getConsentId());

        OBWriteDomestic2 transferRequest3 = PaymentRequestUtils.prepareLegacyRequest(
                consentResponse3.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getLegacyIban(),
                ACCOUNT_IBAN,
                new BigDecimal(individualTransactionValue + 2),
                consentResponse.getData().getInitiation().getRemittanceInformation().getReference(),
                "Unstructured",
                consentResponse.getData().getInitiation().getEndToEndIdentification(),
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        final OBWriteDomesticResponse5 transferResponse3 =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest3);
        Assertions.assertNotNull(transferResponse3);

        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        DONE();
    }


    private void beneStepUpAuthOTP() {
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private void internalPaymentsStepUpAuthBiometrics() {
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private OBWriteDomesticConsent4 createInternalConsentBetweenSavingsAndCurrentAccount(int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                currentAccountNumber,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private OBWriteDomesticConsent4 createInternalConsentToDifferentAccount(int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount),
                "AED",
                "EDU",
                "1",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private OBWriteDomesticConsent4 createLegacyConsent(int testTransferAmount) {
        return PaymentRequestUtils.prepareLegacyConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getLegacyIban(),
                ACCOUNT_IBAN,
                new BigDecimal(testTransferAmount),
                "AED",
                "EDU",
                "1",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");
    }

    private OBWriteDomesticConsent4 createDomesticConsent(int testTransferAmount, String fee) {
        return PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(testTransferAmount),
                "AED",
                "EDU",
                "unstructured",
                OBChargeBearerType1Code.valueOf(fee),
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private WriteBeneficiary1 createValidBeneData() {
        return WriteBeneficiary1.builder()
                .data(WriteBeneficiary1Data.builder()
                        .serviceCode("01")
                        .serviceProvider("Estisalat")
                        .serviceTypeCode("04")
                        .serviceType("Mobile(GSM)")
                        .premiseNumber("123456789")
                        .consumerPin("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
                        .phoneNumber(alphaTestUser.getUserTelephone())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .creditor(OBCashAccount50.builder()
                                .schemeName("schemePlaceholder")
                                .name("Service Bene Nickname")
                                .identification(temenosConfig.getCreditorAccountId())
                                .secondaryIdentification("secondary identification")
                                .build())
                        .build())
                .build();
    }
    public void beneStepUpAuthBiometrics() {
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }
    public OBWriteInternationalConsent5 internationalPaymentConsentRequestDataValidWithAccount(String iBan) {
        final OBWriteInternational3DataInitiationCreditorAccount creditorAccount;

        if ((iBan.isBlank()))
        {
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .identification(creditorAccountNumber)
                    //.name("Test Account Owner Name"+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .name("Test Account Owner")
                    .secondaryIdentification("ICI0000012")
                    .build();

        }
        else if(StringUtils.isNumeric(iBan)) {
            /**Following LInes of code be used to Account NUmber in Test Data of International Payment Consent Request
             */
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"))
                    .identification(temenosConfig.getCreditorIban())
                    //.identification(iBan)
                    .name("Test Account Owner Name" + RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    //.secondaryIdentification("")
                    .build();
        }
        else{
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .identification(iBan)
                    .name("Test Account Owner Name" + RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    //.secondaryIdentification("12345678901") /* TODO Need to check Hard Coded Value*/
                    .build();
        }
        final OBWriteInternational3DataInitiationDebtorAccount debtorAccount = OBWriteInternational3DataInitiationDebtorAccount.builder()
                .schemeName("UAE.AccountNumber")
                .identification(debtorAccountNumber)
                .build();
        String reference = RandomDataGenerator.generateRandomAlphanumericUpperCase(12);
        final OBWriteInternational3DataInitiationRemittanceInformation inf = OBWriteInternational3DataInitiationRemittanceInformation.builder()
                .reference("PIN-Personal investments")
                .unstructured("Test")
                .build();
        String endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(16);
        return OBWriteInternationalConsent5.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteInternationalConsent5Data.builder()
                        .initiation(OBWriteInternational3DataInitiation.builder()
                                .endToEndIdentification(endToEndReference)
                                .creditor(OBWriteInternational3DataInitiationCreditor.builder()
                                        .name("JHON " + RandomDataGenerator.generateEnglishRandomString(4))
                                        .postalAddress(OBWriteInternational3DataInitiationPostalAddress.builder()
                                                .addressLine1("House No" + RandomDataGenerator.generateRandomBuildingNumber())
                                                .addressLine2(RandomDataGenerator.generateRandomAddressLine())
                                                .addressLine3(RandomDataGenerator.generateRandomCityOfBirth())
                                                .country("IN")
                                                .build())
                                        .build())
                                .creditorAgent(OBWriteInternational3DataInitiationCreditorAgent.builder()
                                        .identification("CHASUS33")
                                        .postalAddress(uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBPostalAddress6.builder()
                                                .country("IN")
                                                .build())
                                        .build())
                                .creditorAccount(creditorAccount)
                                .debtorAccount(debtorAccount)
                                .remittanceInformation(inf)
                                .chargeBearer(OBChargeBearerType1Code.BORNE_BY_CREDITOR)
                                .instructedAmount(OBWriteInternational3DataInitiationInstructedAmount.builder()
                                        .amount(BigDecimal.valueOf(10.00))
                                        .currency("INR").build())
                                .supplementaryData(OBWriteInternational3DataInitiationSupplementaryData.builder()
                                        .debitCurrency("AED")
                                        //.orgRefNumber("12345678980")
                                        .amountInAed(BigDecimal.valueOf(10.00))
                                        .beneficiaryNickName(RandomDataGenerator.generateEnglishRandomString(3))
                                        .build())
                                .build())
                        .build())
                .build();

    }

    public OBWriteInternational3 internationalTransferRequestDataValid(String consentID, String iBan) {

        final OBWriteInternational3DataInitiationCreditorAccount creditorAccount;
        //final OBWriteInternational3DataInitiationDebtorAccount debtorAccount;
        if (!(iBan.isBlank())) {
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    //.schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf(INTERNATIONAL_IBAN))

                    .identification(iBan)
                    .name("Test Account Owner Name" + RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .secondaryIdentification("ICI0000012")
                    .build();

        } else {
            /**Following LInes of code be used to Account NUmber in Test Data of International Payment Consent Request
             */
            /*creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"))
                    .identification(alphaTestUser.getAccountNumber())
                    .name("Test Account Owner Name"+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .secondaryIdentification("ICI0000012")
                    .build();*/
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    //.schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf(INTERNATIONAL_IBAN))

                    .identification(creditorAccountNumber)
                    .name("Test Account Owner")
                    .secondaryIdentification("ICI0000012")
                    .build();
        }

        final OBWriteInternational3DataInitiationDebtorAccount debtorAccount = OBWriteInternational3DataInitiationDebtorAccount.builder()
                //.schemeName("UAE.AccountNumber")
                .schemeName(ACCOUNT_NUMBER)
                .identification(debtorAccountNumber)
                .build();

        final OBWriteInternational3DataInitiationRemittanceInformation inf = OBWriteInternational3DataInitiationRemittanceInformation.builder()
                .reference("PIN-Personal investments")
                .unstructured("Test")
                .build();
        String endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(16);

        return OBWriteInternational3.builder()
                .data(OBWriteInternational3Data.builder()
                        .consentId(consentID)
                        .initiation(OBWriteInternational3DataInitiation.builder()
                                .endToEndIdentification(endToEndReference)
                                .creditor(OBWriteInternational3DataInitiationCreditor.builder()
                                        .name("JHON " + RandomDataGenerator.generateEnglishRandomString(4))
                                        .postalAddress(OBWriteInternational3DataInitiationPostalAddress.builder()
                                                .addressLine1("House No" + RandomDataGenerator.generateRandomBuildingNumber())
                                                .addressLine2(RandomDataGenerator.generateRandomAddressLine())
                                                .addressLine3(RandomDataGenerator.generateRandomCityOfBirth())
                                                .country("IN")
                                                .build())
                                        .build())
                                .debtorAccount(debtorAccount)
                                .remittanceInformation(inf)
                                .creditorAccount(creditorAccount)
                                .instructedAmount(OBWriteInternational3DataInitiationInstructedAmount.builder()
                                        .amount(BigDecimal.valueOf(10.00))
                                        .currency("INR")
                                        .build())
                                .creditorAgent(OBWriteInternational3DataInitiationCreditorAgent.builder()
                                        .identification("CHASUS33")
                                        .name("Test Bank name")
                                        .postalAddress(uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBPostalAddress6.builder()
                                                .country("IN")
                                                .build())
                                        .build())
                                .supplementaryData(OBWriteInternational3DataInitiationSupplementaryData.builder()
                                        .debitCurrency("AED")
                                        .amountInAed(BigDecimal.valueOf(10.00))
                                        .build())
                                .chargeBearer(OBChargeBearerType1Code.BORNE_BY_CREDITOR)
                                .build())
                        .build())
                .build();


    }
}
