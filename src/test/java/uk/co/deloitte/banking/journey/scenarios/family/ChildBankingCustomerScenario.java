package uk.co.deloitte.banking.journey.scenarios.family;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardPaymentResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardWithdrawalResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1DataAmount;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1DataBalance;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2DataInitiation;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBActiveOrHistoricCurrencyAndAmount9;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBCreditDebitCode1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadDataTransaction6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantRegisterDeviceRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantValidateResetPasswordRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
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
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentFile1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUsers;
import uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes.TripleDesUtil;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.cards.api.CardsRelationshipApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerType1;
import uk.co.deloitte.banking.customer.api.customer.model.OBEIDStatus;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBTermType;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTerm1;
import uk.co.deloitte.banking.customer.api.customer.model.authorization.OBCustomerAuthorization1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBFatcaForm1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatca1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatcaDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;
import uk.co.deloitte.banking.payments.beneficiary.BeneficiaryConfig;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.util.DomesticPaymentRequestUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1.*;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.TEEN_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.REGISTRATION_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomString;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.customer.api.customer.model.OBTermType.BANKING;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildBankingCustomerScenario extends AdultOnBoardingBase {

    @Inject
    private RelationshipApi relationshipApi;

    static String dependantId;
    static String relationshipId;
    static String otpCode;
    static String beneId;
    private static final String CARD_MASK = "000000";
    private String CARD_CREATED_BY_PARENT_NUMBER = null;
    private String CREATE_CARD_EXP_DATE = null;
    private String CARD_CREATED_BY_PARENT_ID = null;

    private static final int loginMinWeightExpected = 31;

    static String childFullName;
    @Inject
    AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    TripleDesUtil tripleDesUtil;

    @Inject
    BeneficiaryConfig beneficiaryConfig;

    @Inject
    private CardsRelationshipApi cardsRelationshipApi;

    private static AlphaTestUser childAlphaTestUser = new AlphaTestUser();

    private final static String TEMPORARY_PASSWORD = "validtestpassword";

    @BeforeEach
    void ignore() {
        envUtils.ignoreTestInEnv(Environments.NONE);
    }


    @Test
    @Order(1)
    void marketplace_customer_setup_success_test() {
        this.marketplace_customer_setup_success(false);
    }

    @Test
    @Order(2)
    void reauthenticate_test() {
        this.reauthenticate(ACCOUNT_SCOPE);
    }

    @Test
    @Order(3)
    void generate_customer_cif_test() {
        this.generate_customer_cif();
    }

    @Test
    @Order(4)
    void dump() {
        AlphaTestUsers.builder().alphaTestUsers(List.of(this.alphaTestUser))
                .build()
                .writeToFile();
        DONE();
    }

    @Test
    @Order(5)
    void create_parent_account_test() {
        this.create_account();
    }

    @Test
    @Order(6)
    void create_user_relationship_test_success() {
        TEST("AHBDB-6178-Create child in Forgerock");
        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword(TEMPORARY_PASSWORD).build();

        WHEN("Calling post relationship from authenticate api with a temporary password");
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("The userId of the newly created user is returned");
        assertNotNull(response.getUserId());

        dependantId = response.getUserId();

        NOTE("Dependant id " + dependantId);

        DONE();
    }

    @Test
    @Order(7)
    void create_dependant_customer_and_relationship() {
        TEST("AHBDB-6177 , AHBDB-6180 - Create dependant and relationship");

        String fullName = "ete" + generateRandomString(5) + " " + generateRandomString(5);
        childFullName = fullName;
        NOTE("Child Name: " + fullName);
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName(fullName)
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.MOTHER)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        WHEN("Calling post to create dependant and relationship");
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        THEN("Status 201(CREATED) is returned");
        assertNotNull(response);
        AND("Relationship contains onboarded by");
        assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();
    }

    @Test
    @Order(8)
    void request_to_ename_checker() {
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerCountry, CustomerDOB and Gender");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(childFullName)
                .country("AE")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("F")
                .build();
        CustomerBlacklistResponseDTO response = sanctionsApi.checkBlacklistedChild(alphaTestUser, customerBlacklistRequestDTO, relationshipId);
        if (response.getResult().equals("HIT")) {
            assertTrue(response.getDetectionId().matches("\\d+"));
            assertEquals("3050", response.getReturnCode());
        } else {
            assertEquals("0000", response.getReturnCode());
        }
        assertTrue(response.getTimestamp().toString().matches("(\\d{4})(-)(\\d{2})(-)(\\d{2})(T)(\\d{2})(:)(\\d{2})(:)(\\d{2})(Z)"));
        assertTrue(response.getReferenceNumber().matches("(\\w{8})(-)(\\w{4})(-)(\\w{4})(-)(\\w{4})(-)(\\w{12})"));
        THEN("The platform will respond with the result with the status code of 200");
        AND("The platform will trigger an event with the result");
    }

    @Test
    @Order(9)
    void send_child_otp() {
        TEST("AHDB-6179 - OTP Generation from Parent’s Device ");
        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUser, 204, relationshipId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);

        assertNotNull(otpCO);

        otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        assertNotNull(otpCode);
    }

    @Test
    @Order(10)
    void register_child_device() {
        TEST("AHDB-6179 - AC8 Initial registration");
        WHEN("A child want to generate an OTP for the child");
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(TEMPORARY_PASSWORD)
                .otp(otpCode)
                .build();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(childAlphaTestUser, request);

        THEN("Status code 201 is returned");
        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(childAlphaTestUser, userLoginResponseV2);

    }


    @Test
    @Order(11)
    void certificate_upload_test() {
        childAlphaTestUser = alphaTestUserFactory.setupUserCerts(childAlphaTestUser);
        assertNotNull(childAlphaTestUser);
    }


    @Test
    @Order(12)
    void child_registration_scope_test() {
        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(childAlphaTestUser);
        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is REGISTRATION");

        Assertions.assertEquals(REGISTRATION_SCOPE, loginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, loginResponse);
    }


    @Test
    @Order(13)
    void child_customer_scope_test() {
        this.authenticateApi.patchUser(childAlphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn("CUSTOMER")
                        .build());


        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(childAlphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(childAlphaTestUser.getUserId())
                        .password(childAlphaTestUser.getUserPassword())
                        .build(),
                childAlphaTestUser.getDeviceId(), true);

        Assertions.assertEquals(CUSTOMER_SCOPE, userLoginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, userLoginResponse);
    }

    @Test
    @Order(14)
    void patch_child_success_test() {
        TEST("AHBDB-5218:: AC1 Patch Customer in CRM");

        GIVEN("Customer is created");
        OBWritePartialCustomer1 patchCustomer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .preferredName("Test" + generateRandomString(5))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language("en")
                        .gender(OBGender.MALE)
                        .build())
                .build();

        WHEN("The client calls patch on the customers endpoint");
        final OBWriteCustomerResponse1 patchResponse = this.customerApiV2.patchChildSuccess(alphaTestUser,
                patchCustomer, relationshipId);

        THEN("we will return a 200 response");
        assertNotNull(patchResponse);

        DONE();
    }

    @Test
    @Order(15)
    void create_child_applicant() {
        TEST("AHDB-6990 - Register Applicant from Parent’s Device ");
        WHEN("A parent want to generate an Applicant for the child");
        final TokenHolder childApplicant = idnowApi.createChildApplicant(alphaTestUser, relationshipId,
                CreateApplicantRequest.builder().firstName(childAlphaTestUser.getName())
                        .lastName(childAlphaTestUser.getName()).build());
        THEN("Status 201(CREATED) is returned");
        assertNotNull(childApplicant);
        assertNotNull(childApplicant.getApplicantId());
        assertNotNull(childApplicant.getSdkToken());
        childAlphaTestUser.setApplicantId(childApplicant.getApplicantId());
    }

    @Test
    @Order(16)
    void send_idnow_webhook_callback() {
        TEST("AHBDB - 6990 - Store IDV status and JSON blob");

        GIVEN("IDNow has finished processing the applicant’s ID");
        AND("the verification has been successful");
        Assertions.assertNotNull(childAlphaTestUser.getApplicantId());


        WHEN("the Ident status value “Success” is returned from IDNow");
        var response = idnowApi.setIdNowAnswer(childAlphaTestUser, "SUCCESS");
        THEN("I will trigger an event saying IDV is completed along with the IDNow response");
        assertTrue(response);
        DONE();
    }

    @Test
    @Order(17)
    void retrieve_child_applicant() {
        TEST("AHBDB - 6990 - Get EID information for a child");
        GIVEN("A customer has completed their ID&V process for the child");
        AND("the verification has been successful");
        Assertions.assertNotNull(childAlphaTestUser.getApplicantId());

        WHEN("the client attempts to retrieve the child applicant’s full IDNow result information with a valid " +
                "JWT token");
        ApplicantExtractedDTO response = idnowApi.getChildApplicantResults(alphaTestUser, relationshipId);

        THEN("the platform will return a 200 response");
        AND("the platform will return the JSON related to the user ID/ transaction ID");
        assertNotNull(response);
        DONE();
    }

    @Test
    @Order(18)
    void persist_child_eid_information_to_customer() throws JsonProcessingException {

        TEST("AHBDB - 6990 - Persist child information from IDV");

        GIVEN("A child exists");
        OBReadCustomer1 currentChild = this.relationshipApi.getChildBasedOnRelationship(alphaTestUser, relationshipId);
        AND("The child has completed his/her ID Check and we have all the information");
        ApplicantExtractedDTO applicantResult = idnowApi.getChildApplicantResults(alphaTestUser, relationshipId);

        WHEN("Client attempt to update child profile with those information");

        Map<String, Object> userData = applicantResult.getUserData();

        OBWritePartialCustomer1 obWriteCustomer1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .build())
                .build();
        obWriteCustomer1.getData().setFullName(extractValue(userData, "FullName"));
        obWriteCustomer1.getData().setFirstName(extractValue(userData, "FirstName"));
        obWriteCustomer1.getData().setLastName(extractValue(userData, "LastName"));
        obWriteCustomer1.getData().setNationality(extractValue(userData, "Nationality"));
        obWriteCustomer1.getData().setGender(OBGender.valueOf(extractValue(userData, "Gender").toUpperCase(Locale.ROOT)));

        obWriteCustomer1.getData().setCustomerState(OBCustomerStateV1.IDV_COMPLETED);

        OBWriteCustomerResponse1 obWriteCustomerResponse1 = this.customerApi.updateCustomer(childAlphaTestUser,
                obWriteCustomer1, 200);
        AND("Expect information to be persisted");

        currentChild = customerApi.getCurrentCustomer(childAlphaTestUser);

        assertEquals(extractValue(userData, "FullName"), currentChild.getData().getCustomer().get(0).getFullName());
        DONE();
    }

    @Test
    @Order(19)
    void create_idv_details_for_child() {
        TEST("AHBDB-6991 - AC2 Customer has completed IDV for child - 200 response");

        GIVEN("The customer provided the ");
        WHEN("Customer enters his child idv details");
        OBWriteIdvDetailsResponse1 customerIdvDetails = relationshipApi.createChildIdvDetails(alphaTestUser, relationshipId);
        THEN("Employment idv are persisted for the customer's child");
        assertNotNull(customerIdvDetails);

        DONE();
    }

    @Test
    @Order(20)
    void child_customer_crs_test() {

        alphaTestUserBankingCustomerFactory.setupEmploymentChild(childAlphaTestUser);
    }

    @Test
    @Order(21)
    void create_fatca_details_for_child() {
        TEST("AHBDB-4848: Create and get endpoint for FATCA declaration in CRM");
        TEST("AC1 Store FATCA details - 201 response");
        GIVEN("the customer has provided their FATCA details");
        var fatca = OBWriteFatca1.builder().data(OBWriteFatcaDetails1.builder()
                        .usCitizenOrResident(true)
                        .ssn("123456789")
                        .form(OBFatcaForm1.W8)
                        .federalTaxClassification("S Corporation")
                        .build())
                .build();

        WHEN("the client updates the customer profile with valid FATCA details");
        var result = this.fatcaApiV2.createFatcaDetailsChild(alphaTestUser, fatca, relationshipId);
        THEN("we will return a 201 response");
        assertNotNull(result);
        DONE();
    }

    @Test
    @Order(22)
    void create_location_details_for_child() {
        TEST("AHBDB-4846: Create and get endpoint for Location declaration");
        TEST("AC1 Create address - 201 response");
        GIVEN("the customer has provided their Location details");
        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("WorkTest")
                .address(OBLocationAddress1.builder()
                        .streetName("Al Saada Street")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 63111")
                        .build())
                .build();

        WHEN("the client updates the customer profile with valid Location details");
        var result = this.locationsApi.createLocationDetailsChild(alphaTestUser, location, relationshipId);
        THEN("we will return a 201 response");
        assertNotNull(result);
        assertNotNull(result.getData().get(0).getId());

        DONE();
    }


    @Test
    @Order(23)
    void child_customer_details_test() {

        //TODO:: NOT ALL THIS INFO SHOULD BE SENT
        //update customer information
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName(generateRandomString(5))
                        .lastName(generateRandomString(10))
                        .preferredName("Test" + generateRandomString(5))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language("en")
                        .cityOfBirth("Dubai")
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .gender(alphaTestUser.getGender())
                        .nationality("AE") //TODO:: Fails when not set
                        .address(OBPartialPostalAddress6.builder()
                                .addressLine(List.of(generateRandomString(10),
                                        generateRandomString(5)))
                                .buildingNumber("101")
                                .country("AE")
                                .countrySubDivision("Dubai")
                                .postalCode("123456") //TODO::Remove
                                .build())
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .build())
                .build();

        OBWriteCustomerResponse1 obWriteCustomerResponse1 = this.customerApi.updateCustomer(childAlphaTestUser,
                customer,
                200);
    }

    // ----- Banking tests begins here -------
    @Test
    @Order(100)
    void test_generate_child_cif() {
        TEST("AHBDB-6999: Generate Child CIF and update CustomerType");
        TEST("AHBDB-4103 - AC1 Put Customer CIF - 200 OK");

        WHEN("Customer tries to generate cif for child");
        final OBCustomer1 childCif =
                this.relationshipApi.putChildCif(alphaTestUser, relationshipId).getData().getCustomer().get(0);

        assertNotNull(childCif);
        assertEquals(childCif.getCustomerType(), OBCustomerType1.BANKING);

        DONE();
    }

    @Test
    @Order(101)
    void verify_relationship_eid_status_test() {
        TEST("AHBDB-8292 - Set EID status");
        GIVEN("Customer exists");
        assertNotNull(childAlphaTestUser.getLoginResponse());

        WHEN("The customer receives the card and client wants to mark it as validated");
        OBWriteEIDStatus1 build = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();
        OBWriteCustomerResponse1 obWriteCustomerResponse1 = customerApiV2.updateCustomerValidations(childAlphaTestUser,
                build);
        THEN("Status 200 is returned");
        assertNotNull(obWriteCustomerResponse1);
        AND("EID status is set to VALID");
        OBReadCustomer1 currentCustomer = customerApiV2.getCurrentCustomer(childAlphaTestUser);
        assertEquals(OBEIDStatus.VALID, currentCustomer.getData().getCustomer().get(0).getEidStatus());
    }

    @Test
    @Order(102)
    void child_account_scope_test() {
        //check scope has updated to accounts automatically
        alphaTestUserBankingCustomerFactory.assertAccountScope(childAlphaTestUser);

        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(childAlphaTestUser);

        Assertions.assertEquals(ACCOUNT_SCOPE, loginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, loginResponse);
    }

    @Test
    @Order(103)
    void test_accept_child_banking_term() {
        TEST("AHBDB-7255: Banking T&Cs for Child");
        TEST("AHBDB-7255 - AC1 T&Cs data - 201 Created");
        var terms = Map.of("TermsAccepted", "true", "TermsAcceptedDate", "2021-02-03T22:42:30+02:00", "TermsVersion", "2021-02-05", "Type", "BANKING");
        OBWriteTerm1 obWriteTerm = OBWriteTerm1.builder()
                .termsAccepted(true)
                .termsAcceptedDate(OffsetDateTime.parse("2021-02-12T12:30:24+04:00"))
                .termsVersion(LocalDate.parse("2021-08-12"))
                .type(BANKING)
                .build();

        final OBWriteTerm1 response =
                this.relationshipApi.createDependantTerm(alphaTestUser, relationshipId, obWriteTerm).getData();

        assertNotNull(response);
        assertEquals(response.getType(), OBTermType.BANKING);

        DONE();
    }

    @Test
    @Order(104)
    void create_account_test() {
        // if you don't want to create kid account uncomment following line
        // envUtils.ignoreTestInEnv(Environments.NONE);

        GIVEN("Status is account creation in progress");
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.ACCOUNT_CREATION_IN_PROGRESS)
                        .build())
                .build();

        WHEN("The client calls post on the customers endpoint");
        final OBWriteCustomerResponse1 response = this.customerApi.updateCustomer(childAlphaTestUser, customer, 200);
        assertNotNull(response);

        OBWriteAccountResponse1 savings = accountApi.createAccount(childAlphaTestUser,
                OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);

        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();

        assertNotNull(data);
        assertNotNull(data.getAccountId());

        childAlphaTestUser.setAccountNumber(data.getAccountId());

        DONE();
    }


    @Test
    @Order(105)
    void child_account_after_new_device_scope_test() {

        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(childAlphaTestUser);
        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is REGISTRATION");

        Assertions.assertEquals(ACCOUNT_SCOPE, loginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, loginResponse);
    }

    @Test
    @Order(106)
    void get_customer_authz_test() {

        TEST("AHBDB-229 - Retrieve customer cif and auths");
        GIVEN("A customer already exists and has generated CIF ");

        WHEN("Client queries for the customers authorizations");

        OBCustomerAuthorization1 customerAuthz = customerApi.getCustomerAuthz(childAlphaTestUser);
        OBCustomerAuthorization1 customerAuthz2 = customerApi.getCustomerAuthz(alphaTestUser);
        THEN("The customer authz properties are returned");
        assertNotNull(customerAuthz);


        AND("CIF is populated");
        assertNotNull(customerAuthz.getData().getCif());
    }

    @Test
    @Order(107)
    void upload_doc_for_child() throws JsonProcessingException {

        TEST("AHBDB-6996 - Upload child's birth certificate ");
        WHEN("A parent wants to upload a doc for a child");
        final DocumentFile1 uploadedDoc = documentRelationshipApi.uploadDocument(alphaTestUser, "BIRTH_CERTIFICATE", relationshipId, 201);

        THEN("Status 201(CREATED) is returned");
        assertNotNull(uploadedDoc);
    }


    @Test()
    @Order(108)
    public void test_onboard_relationship() {
        TEST("AHBDB-AHBDB-9536 relationship onboard");
        GIVEN("Valid parent customer with accounts scope");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        String connectionId = relationships.getData().getRelationships().get(0).getConnectionId().toString();

        AND("Valid kid account request");
        OBWriteAccount1 request = relationshipAccountApi.createYouthAccountData();

        WHEN("Create dependant account");
        OBWriteAccountResponse1 response = relationshipAccountApi.createDependantCustomerAccount(alphaTestUser, request, connectionId);

        THEN("Kid account should be set up correctly");
        Assertions.assertNotNull(response.getData().getAccountId());

        childAlphaTestUser.setAccountNumber(response.getData().getAccountId());
        DONE();
    }

    @Test
    @Order(109)
    void get_account_details_test_success() {
        TEST("AHBDB-5305-Get Account details positive path");

        WHEN("Calling get account details api");
        OBReadAccount6 response = this.accountApi.getAccountDetails(childAlphaTestUser, childAlphaTestUser.getAccountNumber());

        THEN("Status code 200 is returned");
        AND("Account details has been returned");

        assertGetAccountDetailsResponsePayload(response);
        DONE();
    }


    @Test
    @Order(110)
    void get_account_test_success() {

        TEST("AHBDB-5019-Get Account List positive path");

        WHEN("Calling get account api");
        OBReadAccount6 response = this.accountApi.getAccountsV2(childAlphaTestUser);

        THEN("Status code 200(User Accounts) is returned");
        AND("User Accounts has been returned");

        assertGetAccountsResponsePayload(response);
        DONE();
    }

    @Test
    @Order(111)
    public void positive_case_father_can_create_beneficiary_for_child() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of father");
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUser.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);

        AND("I have a valid beneficiary set up with an external account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryType(beneficiaryConfig.getDomesticFlag());
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");


        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUser, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 getObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUser, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        beneId = getObBeneficiary5.getBeneficiaryId();
        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, getObBeneficiary5);

        DONE();
    }

    @Test
    @Order(112)
    public void positive_child_can_get_beneficiary_created_by_father() {

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUser.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");

        OBReadBeneficiary5 obReadBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(childAlphaTestUser, beneId);
        Assertions.assertNotNull(obReadBeneficiary5);

        DONE();
    }


    @Test
    @Order(113)
    public void create_domestic_dtp_transfers() {
        beforePaymentTests();

        //TODO Enable when CB adapter returns back with SUCCESS for fees endpoint
        //envUtils.ignoreTestInEnv(Environments.ALL);
        TEST("AHDB-354 :: Test DTP domestic transfer consent");
        TEST("AHDB-354/AHDB-1632 :: Test DTP domestic transfer consent & execution");

        final int AMOUNT = 10;

        final String debtorAccountId = StringUtils.leftPad(childAlphaTestUser.getAccountNumber(), 10, "0");
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 10, "0");


        GIVEN("User has account scope");
        WHEN("User has enough funds for transfer in his account");
        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountId);
        //deposit money into account before trigger transfer account
        accountApi.executeInternalTransfer(childAlphaTestUser, temenosAccountId, BigDecimal.valueOf(AMOUNT));
        WHEN("Transfer amount is less than daily limit");
        THEN("Consent should be created");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                CREDITOR_ACCOUNT_ID,
                ACCOUNT_IBAN,
                (BigDecimal.valueOf(AMOUNT)),
                "AED",
                "Api tester - domestic " +
                        "payments",
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsentResponse5 consentResponse =
                domesticTransferApiFlows.createDomesticPaymentConsent(childAlphaTestUser, consent4);
        assertNotNull(consentResponse);
        assertNotNull(consentResponse.getData().getConsentId());

        WHEN("Transfer is attempted ");
        //transaction from debtor account
        OBWriteDomesticResponse5 obWriteDomesticResponse5 =
                accountApi.executeInternalTransferBetweenAccounts(childAlphaTestUser, temenosAccountId, debtorAccountId,
                        BigDecimal.valueOf(AMOUNT));

        THEN("Transfer is successful");
        assertNotNull(obWriteDomesticResponse5);

        AND("Balance is updated");
        //TODO: Confirm balance

        AND("Transaction list is updated");
        //TODO: Get transactions and assert

        DONE();
    }

    @Test
    @Order(114)
    void get_user_balances_test_success() {
        beforePaymentTests();

        TEST("AHBDB-5080 - Get account balances positive path");

        WHEN("Calling account balances api");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(childAlphaTestUser,
                childAlphaTestUser.getAccountNumber());
        THEN("Status code 200 is returned");

        assertGetAccountBalancesResponsePayload(balanceResponse);
        DONE();
    }

    @Test
    @Order(115)
    void get_statement_transaction_details_test_success() {
        beforePaymentTests();

        TEST("AHBDB-7253 REMOVE Use of statement id and fix api");

        envUtils.ignoreTestInEnv("AHBDB-15670", Environments.ALL);

        final String statementId = "194631234570595.000001";

        TEST("AHBDB-5389-Get transaction details positive path");

        WHEN("Calling get transaction details api");
        OBReadTransaction6 response = this.accountApi.getAccountStatementTransactions(childAlphaTestUser, statementId);
        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("Transaction details has been returned");
        OBReadDataTransaction6 accountTransactions = response.getData();
        assertNotNull(accountTransactions);

        List<OBTransaction6> transactionList = accountTransactions.getTransaction();
        assertNotNull(transactionList);

        OBTransaction6 transaction = transactionList.get(0);
        assertNotNull(transaction);

        assertNotNull(transaction.getTransactionInformation());
        assertNotNull(transaction.getCreditDebitIndicator());
        assertNotNull(transaction.getBookingDateTime());
        assertNotNull(transaction.getTransactionInformation());

        OBActiveOrHistoricCurrencyAndAmount9 transactionAmount = transaction.getAmount();
        assertNotNull(transactionAmount);

        assertNotNull(transactionAmount.getAmount());
        assertNotNull(transactionAmount.getCurrency());
        assertEquals("AED", transactionAmount.getCurrency());

        assertNotNull(transaction.getBankTransactionCode());

        DONE();
    }

    @Test
    @Order(116)
    void get_locked_amount_success_test() {
        beforePaymentTests();
        final int page = 1;
        TEST("AHBDB-5295- Get locked amount positive path");
        WHEN("Calling get locked amount api");
        OBReadTransaction6 lockedAmount = this.accountApi.getLockedAmount(childAlphaTestUser,
                childAlphaTestUser.getAccountNumber(), page);
        THEN("Status code 200 is returned");
        assertGetLockedAmountResponsePayload(lockedAmount);
        AND("Locked Amount Data is not null");
        DONE();
    }


    @Test
    @Order(117)
    void get_account_transactions_test_success() {
        beforePaymentTests();
        final int AMOUNT = 10;

        TEST("AHBDB-5389-Get account transactions positive path");
        envUtils.ignoreTestInEnv("AHBDB-15670", Environments.ALL);

        WHEN("Credit transfer is executed");
        //deposit money into account before trigger transfer account
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 12, "0");
        accountApi.executeInternalTransfer(childAlphaTestUser, temenosAccountId, BigDecimal.valueOf(AMOUNT));
        WHEN("Calling get account transactions api");
        OBReadTransaction6 response = this.accountApi.getAccountTransactions(childAlphaTestUser,
                childAlphaTestUser.getAccountNumber());
        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("Account transactions has been returned");
        OBReadDataTransaction6 accountTransactions = response.getData();
        assertNotNull(accountTransactions);
        AND("Credit transaction record should be returned");
        accountTransactions.getTransaction().stream()
                .forEach(item -> {
                    if (item.getCreditDebitIndicator() == OBCreditDebitCode1.CREDIT) {
                        assertEquals(temenosAccountId, item.getDebtorAccount().getIdentification());
                        assertEquals(childAlphaTestUser.getAccountNumber(), item.getCreditorAccount().getIdentification());
                    }
                });
        DONE();
    }

    @Test
    @Order(118)
    void create_legacy_dtp_payment_protected_test_success() {
        TEST("AHBDB-7118 Create legacy dtp payment happy path");
        beforePaymentTests();

        final String debtorAccountId = temenosConfig.getSeedAccountId();
        final String creditorAccountId = childAlphaTestUser.getAccountNumber();

        WHEN("Calling legacy2dtp payment api");
        OBWriteDomesticResponse5 response = paymentProtectedApi.createLegacyDtpPayment(debtorAccountId,
                creditorAccountId,
                BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Payment response has been returned");
        assertCreateLegacyDtpPaymentPayload(response);
        DONE();
    }

    @Test
    @Order(119)
    void create_card_payment_protected_test_success() {
        TEST("AHBDB-1338 Create card payment happy path");
        beforePaymentTests();
        envUtils.ignoreTestInEnv("AHBDB-15691", Environments.ALL);
        final String debtorAccountId = temenosConfig.getSeedAccountId();

        WHEN("Calling card payment api");
        OBWriteCardPaymentResponse1 response = cardProtectedApi.createCardPayment(debtorAccountId, BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Card payment response has been returned");
        assertCreateCardPaymentPayload(response);
        DONE();
    }

    @Test
    @Order(120)
    void create_card_deposit_protected_test_success() {
        TEST("AHBDB-1338 Create card deposit happy path");
        beforePaymentTests();

        final String debtorAccountId = temenosConfig.getSeedAccountId();
        final String creditorAccountId = childAlphaTestUser.getAccountNumber();

        WHEN("Calling card deposit api");
        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(debtorAccountId,
                creditorAccountId,
                BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Card deposit response has been returned");
        assertCreateCardDepositPayload(response);
        DONE();
    }

    @Test
    @Order(121)
    void create_card_withdrawal_protected_test_success() {
        TEST("AHBDB-1338 Create card withdrawal happy path");
        beforePaymentTests();

        final String debtorAccountId = temenosConfig.getSeedAccountId();
        final String creditorAccountId = childAlphaTestUser.getAccountNumber();

        WHEN("Calling card withdrawal api");
        OBWriteCardWithdrawalResponse1 response = cardProtectedApi.createCardWithdrawal(debtorAccountId,
                creditorAccountId,
                BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Card withdrawal response has been returned");
        assertCreateCardWithdrawalPayload(response);
        DONE();
    }

    @Test
    @Order(122)
    void user_crud_beneficiary_otp_test() throws InterruptedException {
        envUtils.ignoreTestInEnv("AHBDB-14178", Environments.ALL);
        beforePaymentTests();

        TEST("AHBDB-305:: Test Beneficiary Management");
        TEST("AHBDB-370:: Stepup authentication for Beneficiary Management");
        GIVEN("User has Account scope and a Valid Account");
        final int otpWeightRequested = 32;

        WHEN("the user calls created Beneficiary");

        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        Response response =
                this.beneficiaryApiFlows.createBeneficiaryFlexResponse(childAlphaTestUser
                        , beneficiaryData);


        THEN("Should fail with FORBIDDEN");
        response.then().log().all().statusCode(HttpStatus.FORBIDDEN.getCode());
        final OBErrorResponse1 errorResponse = response.as(OBErrorResponse1.class);
        Assertions.assertTrue(Objects.nonNull(errorResponse));
        THEN("Should return error code: " + errorResponse.getCode());
        //TODO enable error code check after updating ERROR lib
        //User triggers OTP flow
        //1) -> initiate step up authentication
        WHEN("Stepup auth requets was initiated");
        authenticateApi.stepUpUserAuthInitiate(childAlphaTestUser,
                StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        THEN("User will receive OTP");

        //2) -> extract otp from dev simulator
        //extract otp from device simulator
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childAlphaTestUser.getUserId());
        assertTrue(isNotBlank(otpCO.getPassword()));

        AND("Stepup auth validation will be triggered");
        final StepUpAuthRequest stepUpAuthValidationRequest =
                StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(childAlphaTestUser, stepUpAuthValidationRequest);
        THEN("Biometrics validation should success");
        AND("Cache entry was successful");

        GIVEN("Cache entry was successful for userId");
        THEN("Creating beneficiary should be successful");
        OBWriteBeneficiaryResponse1 createdBeneficiary =
                this.beneficiaryApiFlows.createBeneficiaryFlex(childAlphaTestUser
                        , beneficiaryData);
        assertNotNull(createdBeneficiary);
        Assertions.assertEquals(1, createdBeneficiary.getData().getBeneficiary().size());
        assertNotNull(createdBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());


        TEST("AHBDB-305:: Test Beneficiary Management - UPDATE");
        TEST("AHBDB-370:: Stepup authentication for Beneficiary Management - UPDATE");
        GIVEN("Create beneficiary already created");
        THEN("Should return FORBIDDEN status code");
        OBBeneficiary5 obBeneficiary5 = createdBeneficiary.getData().getBeneficiary().get(0);
        obBeneficiary5.setReference("Test Ref");
        Response updatedBeneficiaryResponse = this.beneficiaryApiFlows.updateBeneficiaryResponse(childAlphaTestUser,
                obBeneficiary5);
        Assertions.assertTrue(updatedBeneficiaryResponse.getStatusCode() == HttpStatus.FORBIDDEN.getCode());
        //1) -> initiate step up authentication
        WHEN("Stepup auth requets was initiated");
        authenticateApi.stepUpUserAuthInitiate(childAlphaTestUser,
                StepUpAuthInitiateRequest.builder().scope("accounts").weight(otpWeightRequested).build());
        THEN("User will receive OTP");

        //2) -> extract otp from dev simulator
        //extract otp from device simulator
        OtpCO updateOtpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(childAlphaTestUser.getUserId());
        assertTrue(isNotBlank(updateOtpCO.getPassword()));

        AND("Stepup auth validation will be triggered");
        final StepUpAuthRequest updateStepUpAuthValidationRequest =
                StepUpAuthRequest.builder().otp(updateOtpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(childAlphaTestUser, updateStepUpAuthValidationRequest);
        THEN("OTP validation should success");
        AND("Cache entry was successful");

        GIVEN("Cache entry was successful for userId");
        THEN("update beneficiary request should be successful");
        THEN("can be be updated by the user");
        OBBeneficiary5 updatedobBeneficiary5 = createdBeneficiary.getData().getBeneficiary().get(0);
        obBeneficiary5.setReference("Test Ref");
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(childAlphaTestUser,
                updatedobBeneficiary5);
        Assertions.assertEquals("Test Ref", updatedBeneficiary.getData().getBeneficiary().get(0).getReference());
        Assertions.assertEquals(createdBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId(),
                updatedBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());

        //TOOO::: FAILED
//        THEN("can be be removed");
//        this.beneficiaryApiFlows.deleteBeneficiary(childAlphaTestUser , obBeneficiary5.getBeneficiaryId(), 204);
//        OBReadBeneficiary5 deletedBeneficiary = this.beneficiaryApiFlows.getBeneficiaries(childAlphaTestUser );
//        assertNull(deletedBeneficiary.getData().getBeneficiary());
        DONE();
    }

    @Test
    @Order(123)
    void get_relationship_account_transactions_test_success() {
        envUtils.ignoreTestInEnv("AHBDB-14187", Environments.ALL);
        TEST("AHBDB-9530 Get kids transactions");

        final String debtorAccountId = temenosConfig.getSeedAccountId();
        final String creditorAccountId = childAlphaTestUser.getAccountNumber();

        WHEN("Calling legacy2dtp payment api");
        OBWriteDomesticResponse5 response = paymentProtectedApi.createLegacyDtpPayment(debtorAccountId,
                creditorAccountId,
                BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        WHEN("Calling get account transactions api");
        OBReadTransaction6 accountTransactionResponse = relationshipAccountApi.getAccountTransactions(alphaTestUser,
                childAlphaTestUser.getAccountNumber(), relationshipId);

        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("Account transactions has been returned");
        OBReadDataTransaction6 accountTransactions = accountTransactionResponse.getData();
        assertNotNull(accountTransactions);
        AND("Credit transaction record should be returned");
        accountTransactions.getTransaction().stream()
                .forEach(item -> {
                    if (item.getCreditDebitIndicator() == OBCreditDebitCode1.CREDIT) {
                        assertEquals(debtorAccountId, item.getDebtorAccount().getIdentification());
                        assertEquals(childAlphaTestUser.getAccountNumber(), item.getCreditorAccount().getIdentification());
                    }
                });
        DONE();
    }

    @Test
    @Order(124)
    void get_relationship_statement_transaction_details_test_success() {
        envUtils.ignoreTestInEnv("AHBDB-14187", Environments.ALL);
        TEST("AHBDB-9531 get kids transactions details");

        final String debtorAccountId = temenosConfig.getSeedAccountId();
        final String creditorAccountId = childAlphaTestUser.getAccountNumber();

        WHEN("Calling legacy2dtp payment api");
        OBWriteDomesticResponse5 response = paymentProtectedApi.createLegacyDtpPayment(debtorAccountId,
                creditorAccountId,
                BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Account transactions api returns transaction");
        OBReadTransaction6 transactionResponse = relationshipAccountApi.getAccountTransactions(alphaTestUser,
                childAlphaTestUser.getAccountNumber(), relationshipId);

        Assertions.assertNotNull(transactionResponse);
        Assertions.assertNotNull(transactionResponse.getData());
        Assertions.assertNotNull(transactionResponse.getData().getTransaction());
        Assertions.assertNotNull(transactionResponse.getData().getTransaction().get(0));
        Assertions.assertNotNull(transactionResponse.getData().getTransaction().get(0).getTransactionReference());

        String statementId = transactionResponse.getData().getTransaction().get(0).getTransactionReference();

        AND("Calling get transaction details api");
        OBReadTransaction6 transactionDetailsResponse = relationshipAccountApi.getAccountStatementTransactions(alphaTestUser, childAlphaTestUser.getAccountNumber(), statementId, relationshipId);

        THEN("Api returns transaction details");
        assertNotNull(transactionDetailsResponse);
        assertNotNull(transactionDetailsResponse.getData());
        assertNotNull(transactionDetailsResponse.getData().getTransaction());
        assertNotNull(transactionDetailsResponse.getData().getTransaction().get(0));
        assertNotNull(transactionDetailsResponse.getData().getTransaction().get(0).getTransactionReference());
        assertNotNull(transactionDetailsResponse.getData().getTransaction().get(0).getAmount());
        assertNotNull(transactionDetailsResponse.getData().getTransaction().get(0).getBookingDateTime());
        assertNotNull(transactionDetailsResponse.getData().getTransaction().get(0).getTransactionInformation());
        assertNotNull(transactionDetailsResponse.getData().getTransaction().get(0).getCreditDebitIndicator());

        DONE();
    }

    @Test
    @Order(125)
    void get_relationship_account_details_test_success() {
        TEST("AHBDB-9528 Get Account Details");

        WHEN("Calling get account details api");
        OBReadAccount6 response = relationshipAccountApi.getAccountDetails(alphaTestUser,
                childAlphaTestUser.getAccountNumber(), relationshipId);

        THEN("Status 200 is returned");
        AND("Account details has been returned");

        assertGetAccountDetailsResponsePayload(response);
        DONE();
    }

    @Test
    @Order(126)
    void get_relationship_account_test_success() {
        TEST("AHBDB-9535 Get Account List");

        WHEN("Calling get accounts api");
        OBReadAccount6 response = relationshipAccountApi.getAccounts(alphaTestUser, relationshipId);

        THEN("Status 200 is returned");
        AND("User Accounts has been returned");

        assertGetAccountsResponsePayload(response);
        DONE();
    }

    @Test
    @Order(127)
    void get_relationship_user_balances_test_success() {
        TEST("AHBDB-9529 Get Account Balances");

        WHEN("Calling account balances api");
        OBReadBalance1 balanceResponse = relationshipAccountApi.getAccountBalances(alphaTestUser,
                childAlphaTestUser.getAccountNumber(), relationshipId);

        THEN("Status 200 is returned");
        AND("Account balances have been returned");

        assertGetAccountBalancesResponsePayload(balanceResponse);
        DONE();
    }

    @Test
    @Order(128)
    void get_relationship_locked_amount_success_test() {
        beforePaymentTests();
        final int page = 1;
        TEST("AHBDB-9532 Get Locked Amount");

        WHEN("Calling get locked amount api");
        OBReadTransaction6 lockedAmount = relationshipAccountApi.getLockedAmount(alphaTestUser,
                childAlphaTestUser.getAccountNumber(), relationshipId, page);

        THEN("Status 200 is returned");
        AND("Locked Amount Data is not null");

        assertGetLockedAmountResponsePayload(lockedAmount);
        DONE();
    }

    //card tests should follow customer creation
    @Test
    @Order(129)
    void test_create_virtual_debit_card() {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        TEST("AHBDB-288 - Create virtual debit card");

        GIVEN("A user have registered with HPS Account");
        CreateCard1 createCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .embossedName(childAlphaTestUser.getName())
                        .cardProduct(TEEN_DIGITAL_DC_PLATINUM)
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(childAlphaTestUser.getAccountNumber())
                                .accountType(CardsApiFlows.ACCOUNT_TYPE)
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30",
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
        //TODO remove hardcode values upon HPS end to end integration
        final CreateCard1Response createCardResponse = cardsApiFlows.createVirtualDebitCard(childAlphaTestUser, createCard1);
        THEN("New virtual debit  card should be  created");

        Assertions.assertTrue(!Objects.isNull(createCardResponse));
        Assertions.assertTrue(StringUtils.isNotBlank(createCardResponse.getData().getCardNumber()));


        AND("Card is activated");
    }

    @Test
    @Order(130)
    void test_fetch_cards() {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        TEST("AHBDB-297 - Fetch card details");

        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
    }

    @Test
    @Order(131)
    void test_fetch_card_cvv_details() {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        TEST("AHBDB-297 - Fetch card details");

        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        TEST("AHBDB-3414 when cards available for user");
        WHEN("valid cardId ( first 6 digits of card + last four digits of card is prepared)");
        THEN("Should be able to retrieve card cvv details ");
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(childAlphaTestUser, cardId);
        Assertions.assertTrue(!Objects.isNull(cardCVvDetails.getData()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
    }

    @Test
    @Order(132)
    void test_fetch_card_transaction_limits_for_spending_type() {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        TEST("AHBDB-3297 - Fetch card limits for withdrawal transaction type");

        GIVEN("A user have registered with HPS Account");
        WHEN("User have a registered card");
        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        //TODO remove hardcode values upon HPS end to end integration
        final ReadCardLimits1 cardLimits = cardsApiFlows.fetchCardLimitsForTransactionType(childAlphaTestUser,
                TransactionType.WITHDRAWAL.getLabel(), cardId);
        THEN("Should return withdrawal limits for provided card");
        Assertions.assertTrue(!Objects.isNull(cardLimits));
        Assertions.assertTrue(StringUtils.isNotBlank(cardLimits.getData().getLimits().getDailyTotalAmount()));
    }

    @Test
    @Order(133)
    void test_fetch_card_transaction_limits_for_purchase_type() {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        TEST("AHBDB-3297 - Fetch card limits for purchase transaction type");

        GIVEN("A user have registered with HPS Account");
        WHEN("User have a registered card");
        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        //TODO remove hardcode values upon HPS end to end integration
        final ReadCardLimits1 cardLimits = cardsApiFlows.fetchCardLimitsForTransactionType(childAlphaTestUser,
                TransactionType.PURCHASE.getLabel(), cardId);
        THEN("Should return purchase limits for provided card");
        Assertions.assertTrue(!Objects.isNull(cardLimits));
        Assertions.assertTrue(StringUtils.isNotBlank(cardLimits.getData().getLimits().getDailyTotalAmount()));
    }

    @Test
    @Order(134)
    void test_card_block_request() {
        envUtils.ignoreTestInEnv("AHBDB-14186", Environments.ALL);
        beforePaymentTests();
        TEST("AHBDB-295 - Block user debit card");

        GIVEN("A user have registered with HPS Account");
        AND("New virtual debit card has been created");

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);

        AND("Block debit card request was fired");
        THEN("Response should be success");
        cardsApiFlows.blockCardLimitedApi(childAlphaTestUser, cards.getData().getReadCard1DataCard().get(0).getCardNumber(), 200);
    }

    @Test
    @Order(135)
    void test_card_fetch_parameters() {
        beforePaymentTests();
        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);
        TEST("AHBDB-8419 - Fetch card parameters");

        GIVEN("A user have registered with HPS Account");
        AND("New virtual debit card has been created");
        CreateCard1 createCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(TEEN_DIGITAL_DC_PLATINUM)
                        .embossedName(childAlphaTestUser.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(childAlphaTestUser.getAccountNumber())
                                .accountType(CardsApiFlows.ACCOUNT_TYPE)
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30",
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
        final CreateCard1Response createCardResponse = cardsApiFlows.createVirtualDebitCard(childAlphaTestUser, createCard1);

        AND("Get card parameters request was fired");

        String cardId = createCardResponse.getData().getCardNumber().replace("000000", "");

        THEN("Response should be success");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(childAlphaTestUser, cardId);

        Assertions.assertTrue(cardFilter1.getData().getInternetUsage().equalsIgnoreCase("Y"));
        Assertions.assertTrue(cardFilter1.getData().getNationalUsage().equalsIgnoreCase("Y"));
        Assertions.assertTrue(cardFilter1.getData().getNationalPOS().equalsIgnoreCase("Y"));
        Assertions.assertTrue(cardFilter1.getData().getInternetUsage().equalsIgnoreCase("Y"));
        Assertions.assertTrue(cardFilter1.getData().getNationalHilalATM().equalsIgnoreCase("Y"));
    }

    @Test
    @Order(136)
    void test_card_update_parameters() {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        TEST("AHBDB-3295 - Update card parameters");

        GIVEN("A user have registered with HPS Account");
        AND("New virtuall debit card has been created");

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);

        AND("Block debit card request was fired");
        THEN("Response should be success");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(cards.getData().getReadCard1DataCard().get(0).getCardNumber())
                .cardNumberFlag("M")
                .internetUsage(false)
                .build();

        cardsApiFlows.updateCardParameters(childAlphaTestUser, updateCardParameters1, 200);

        WHEN("Fetch card parameters should reflect update parameters");
        THEN("Response should be success");
        final String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace("000000", "");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(childAlphaTestUser, cardId);

        Assertions.assertTrue(cardFilter1.getData().getInternetUsage().equalsIgnoreCase("N"));
    }

    @Test
    @Order(137)
    void test_update_card_transaction_limits() {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        TEST("AHBDB-4953 - Update card limits");
        GIVEN("A user have registered with HPS Account");
        WHEN("User have a registered card");
        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);
        WHEN("Card limits update requested");
        THEN("Should return success response");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        //TODO remove hardcode values upon HPS end to end integration
        final WriteDailyMonthlyLimits1 cardLimits =
                WriteDailyMonthlyLimits1.builder().data(WriteDailyMonthlyLimits1Data.builder().cardNumber(cardNumber).cardNumberFlag("M").dailyAtmLimit("100").build()).build();
        THEN("Request should be successful");
        cardsApiFlows.updateCardLimits(childAlphaTestUser, cardLimits, HttpStatus.OK.getCode());
    }

    @Test
    @Order(138)
    void test_issue_physical_card_and_activate_card() throws Throwable {
        beforePaymentTests();

        //TODO ignore until AHBDB-12957-not-able-to-create-card-for-child merged into SIT
        envUtils.ignoreTestInEnv(Environments.SIT);

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(childAlphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(childAlphaTestUser, cardId);
        Assertions.assertTrue(!Objects.isNull(cardCVvDetails.getData()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getExpiryDate()));
        GIVEN("User have a valid card");
        THEN("User should be able to request physical card");

        OBReadAccount6 response = this.accountApi.getAccountsV2(childAlphaTestUser);
        THEN("Status code 200(User Accounts) is returned");
        AND("User Accounts has been returned");
        OBReadAccount6Data data = response.getData();
        assertNotNull(data);

        List<OBAccount6> accountList = response.getData().getAccount();
        OBAccount6 account = accountList.get(0);
        assertNotNull(account);
        List<OBAccount4Account> accounts = account.getAccount();
        assertNotNull(accounts);
        assertEquals(3, accounts.size());
        OBAccount4Account accountDetails = accounts.get(0);
        assertNotNull(accountDetails.getSchemeName());
        assertEquals("IBAN.NUMBER", accountDetails.getSchemeName());
        assertNotNull(accountDetails.getIdentification());
        final WritePhysicalCard1 issuePhysicalCard = WritePhysicalCard1.builder()
                .awbRef("awbRef")
                .dtpReference("DTP" + org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(7))
                .iban(accountDetails.getIdentification())
                .phoneNumber(childAlphaTestUser.getUserTelephone())
                .recipientName(childAlphaTestUser.getName())
                .deliveryAddress(CardDeliveryAddress1.builder()
                        .buildingNumber("123")
                        .country("UAE")
                        .postalCode("wer234")
                        .addressLine(Lists.newArrayList("addressline 1"))
                        .streetName("valley road")
                        .townName("Dubai")
                        .build())
                .build();
        final String plainCardNumber = tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber());

        THEN("Should be able to decrypt cardNumber with 3Des algorithm  :" + plainCardNumber);
        cardsApiFlows.issuePhysicalCard(childAlphaTestUser, cardId, issuePhysicalCard);
        GIVEN("Physical card has been issued successful");
        THEN("Should be able to activate the card");
        final ActivateCard1 activateCard1 = ActivateCard1.builder()
                .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                .cardNumberFlag("P")
                .cardNumber(plainCardNumber)
                .lastFourDigits(StringUtils.right(cardNumber, 4))
                .modificationOperation(ModificationOperation.A)
                .operationReason("TEST : activate card")
                .build();
        cardsApiFlows.activateDebitCard(childAlphaTestUser, activateCard1, 200);
    }

    @Test
    @Order(139)
    void create_domestic_incoming_payment_protected_test_success() {
        beforePaymentTests();
        TEST("AHBDB-7120 Create domestic incoming transfer");
        envUtils.ignoreTestInEnv("AHBDB-15691", Environments.ALL);

        GIVEN("Domestic request with valid scheme");
        WriteDomesticPayment1 request = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                RandomStringUtils.randomAlphanumeric(11),
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should return success response");
        OBWriteDomesticResponse5 domesticResponse = protectedDomesticTransferApiFlows.createDomesticPayment(request);
        checkDomesticIncomingPaymentAssertions(domesticResponse);
        DONE();
    }

    @Test
    @Order(140)
    void update_domestic_incoming_payment_protected_test_success() {
        beforePaymentTests();
        TEST("AHBDB-7120 Update domestic incoming transfer");

        GIVEN("Domestic request with valid scheme");
        WriteDomesticPayment1 request = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                RandomStringUtils.randomAlphanumeric(11),
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should create domestic payment and returns paymentId");
        OBWriteDomesticResponse5 response = protectedDomesticTransferApiFlows.createDomesticPayment(request);

        AND("Authorize created domestic payment");
        GIVEN("Domestic auth request with valid scheme and payment id");
        String domesticPaymentId = response.getData().getDomesticPaymentId();
        WriteDomesticPayment1 authRequest = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                domesticPaymentId,
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should authorize domestic payment");
        OBWriteDomesticResponse5 authResponse = protectedDomesticTransferApiFlows.updateDomesticPayment(authRequest);
        checkDomesticIncomingPaymentAssertions(authResponse);

        DONE();
    }


    @Test
    @Order(141)
    void delete_domestic_incoming_payment_protected_test_success() {
        beforePaymentTests();
        TEST("AHBDB-7120 Delete domestic incoming transfer");

        GIVEN("Domestic request with valid scheme");
        WriteDomesticPayment1 request = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                RandomStringUtils.randomAlphanumeric(11),
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should create domestic payment and returns paymentId");
        OBWriteDomesticResponse5 response = protectedDomesticTransferApiFlows.createDomesticPayment(request);

        AND("Delete created domestic payment");
        GIVEN("Domestic auth request with valid scheme and payment id");
        String domesticPaymentId = response.getData().getDomesticPaymentId();
        WriteDomesticPayment1 deleteRequest = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                domesticPaymentId,
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should delete domestic payment");
        OBWriteDomesticResponse5 deleteResponse = protectedDomesticTransferApiFlows.deleteDomesticPayment(deleteRequest);
        checkDomesticIncomingPaymentAssertions(deleteResponse);

        DONE();
    }


    //relationship card tests

    @Order(142)
    @Test
    public void positive_test_relationship_create_virtual_debit_card() {

        TEST("AHBDB-AHBDB-7422 create virtual debit cards for a reltionship");
        GIVEN("I have a valid customer with accounts scope");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();


        WHEN("They create a virtual card with valid values using relationship bank account");
        THEN("A virtual card is created for the user relationship with a 200 response");
        CreateCard1 validCreateCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .embossedName("child Name")
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(childAlphaTestUser.getAccountNumber())
                                .accountType(AccountType.CURRENT.getDtpValue())
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:45", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();

        final CreateCard1Response createCardResponse = this.cardsRelationshipApi.createVirtualDebitCardForRelationship(alphaTestUser, relationshipId.toString(), validCreateCard1);
        Assertions.assertEquals(createCardResponse.getData().getCardNumber().length(), 16);
        Assertions.assertTrue(createCardResponse.getData().getCardNumber().contains(CARD_MASK));

        AND("The card is created for the relationship");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(childAlphaTestUser);
        Assertions.assertEquals(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), createCardResponse.getData().getCardNumber());
        CARD_CREATED_BY_PARENT_NUMBER = createCardResponse.getData().getCardNumber();
        CREATE_CARD_EXP_DATE = createCardResponse.getData().getExpiryDate();
        CARD_CREATED_BY_PARENT_ID = CARD_CREATED_BY_PARENT_NUMBER.replace("000000", "");

        DONE();
    }

    @Order(143)
    @Test
    public void positive_test_relationship_can_retrieve_cards() {
        TEST("AHBDB-AHBDB-7422 create virtual debit cards for a relationship");
        GIVEN("I have a valid customer with accounts scope");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        relationshipId = relationships.getData().getRelationships().get(0).getConnectionId().toString();

        AND("The card is created for the relationship");
        final ReadCard1 cards = this.cardsRelationshipApi.fetchCardForRelationship(alphaTestUser, relationshipId, "debit", 200);
        DONE();
    }

    @Test
    @Order(144)
    void positive_test_relationship_can_issue_physical_card() throws Throwable {

        beforePaymentTests();

        GIVEN("User have a valid card");
        THEN("User should be able to request physical card");

        OBReadAccount6 response = this.accountApi.getAccountsV2(childAlphaTestUser);
        THEN("Status code 200(User Accounts) is returned");
        AND("User Accounts has been returned");
        OBReadAccount6Data data = response.getData();
        assertNotNull(data);

        List<OBAccount6> accountList = response.getData().getAccount();
        OBAccount6 account = accountList.get(0);
        assertNotNull(account);
        List<OBAccount4Account> accounts = account.getAccount();
        assertNotNull(accounts);
        assertEquals(3, accounts.size());
        OBAccount4Account accountDetails = accounts.get(0);
        assertNotNull(accountDetails.getSchemeName());
        assertEquals("IBAN.NUMBER", accountDetails.getSchemeName());
        assertNotNull(accountDetails.getIdentification());

        final WritePhysicalCard1 issuePhysicalCard = WritePhysicalCard1.builder()
                .awbRef("awbRef")
                .dtpReference("DTP" + org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(7))
                .iban(accountDetails.getIdentification())
                .phoneNumber(childAlphaTestUser.getUserTelephone())
                .recipientName(childAlphaTestUser.getName())
                .deliveryAddress(CardDeliveryAddress1.builder()
                        .buildingNumber("123")
                        .country("UAE")
                        .postalCode("wer234")
                        .addressLine(Lists.newArrayList("addressline 1"))
                        .streetName("valley road")
                        .townName("Dubai")
                        .build())
                .build();

        this.cardsRelationshipApi.createPhysicalCardRelationship(alphaTestUser,  issuePhysicalCard, relationshipId, CARD_CREATED_BY_PARENT_ID, 201);

        }

    @Order(145)
    @Test
    public void positive_test_user_can_activate_their_physical_debit_card() {
        beforePaymentTests();
        TEST("AHBDB-AHBDB-7428 activate debit card for relationship");
        GIVEN("I have a valid customer with accounts scope");

        WriteCardActivation1 validActivateCard1 = WriteCardActivation1.builder()
                .modificationOperation(ACTIVATE_PHYSICAL_VIRTUAL)
                .operationReason("Operation reason")
                .build();

        THEN("They receive a 200 response back from the service");

        this.cardsRelationshipApi.activateDebitCardForRelationship(alphaTestUser, validActivateCard1, relationshipId, CARD_CREATED_BY_PARENT_ID, 200);

        AND("relationship card is activated");
        final ReadCard1 card = this.cardsApiFlows.fetchCardsForUser(childAlphaTestUser);

        Assertions.assertTrue(card.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        DONE();
    }

    @Order(146)
    @Test
    public void positive_test_user_sets_pin_for_relationship() throws Throwable {
        beforePaymentTests();
        TEST("AHBDB-296 set debit card pin");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to set their pin");
        final ReadCardCvv1 cardCVvDetails = this.cardsRelationshipApi.fetchCVVForRelationship(alphaTestUser, relationshipId, CARD_CREATED_BY_PARENT_ID, 200);
        final String pinBlock = tripleDesUtil.encryptUserPin("9876", tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        final WriteCardPinRequest1 pinSetRequest = WriteCardPinRequest1.builder()
                .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                .cardNumber(CARD_CREATED_BY_PARENT_NUMBER)
                .cardNumberFlag("M")
                .lastFourDigits(StringUtils.right(CARD_CREATED_BY_PARENT_NUMBER, 4))
                .pinServiceType("C")
                .pinBlock(pinBlock)
                .build();

        THEN("They receive a 200 response back from the service");
        this.cardsRelationshipApi.setDebitCardPinForRelationship(alphaTestUser, pinSetRequest, relationshipId, CARD_CREATED_BY_PARENT_ID, 200);
        DONE();
    }

    @Test
    @Order(147)
    void forgot_passcode_child_success_test() {
        TEST("AHBDB-7818:: AC1 Reset child passcode");

        String newPassword = UUID.randomUUID().toString();

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(newPassword)
                .build();

        WHEN("The client calls put to reset child passcode");
        this.authenticateApi.resetChildPasscode(alphaTestUser, resetPasscodeReq, relationshipId);


        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUser, 204, relationshipId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);

        assertNotNull(otpCO);

        otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        assertNotNull(otpCode);

        GIVEN("Request to validate password is created");
        DependantValidateResetPasswordRequestV2 validatePasscodeResetReq = DependantValidateResetPasswordRequestV2.builder()
                .otp(otpCode)
                .password(newPassword)
                .userId(childAlphaTestUser.getUserId())
                .build();
        UserLoginResponseV2 validatePasswordResponse = this.authenticateApi.validateChildPasscode(childAlphaTestUser, validatePasscodeResetReq, childAlphaTestUser.getDeviceId());

        THEN("we will return a 200 response");
        assertNotNull(validatePasswordResponse);

        DONE();
    }


    private void checkDomesticIncomingPaymentAssertions(OBWriteDomesticResponse5 domesticResponse) {
        Assertions.assertNotNull(domesticResponse);
        Assertions.assertNotNull(domesticResponse.getData().getInitiation());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getSupplementaryData());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getInstructionIdentification());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getEndToEndIdentification());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getInstructedAmount());

        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getInstructedAmount().getAmount());
        //TODO uncomment this after pacs domestic solution
        //Assertions.assertNotNull(domesticResponse.getData().getInitiation().getDebtorAccount());
        //Assertions.assertNotNull(domesticResponse.getData().getInitiation().getDebtorAccount().getName());
        //Assertions.assertNotNull(domesticResponse.getData().getInitiation().getDebtorAccount().getIdentification());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getCreditorAccount());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getCreditorAccount().getName());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getCreditorAccount().getIdentification());

        Assertions.assertNotNull(domesticResponse.getData().getStatus());
        Assertions.assertNotNull(domesticResponse.getData().getCreationDateTime());
        Assertions.assertNotNull(domesticResponse.getData().getStatusUpdateDateTime());
        Assertions.assertNotNull(domesticResponse.getData().getExpectedExecutionDateTime());
        Assertions.assertNotNull(domesticResponse.getData().getExpectedSettlementDateTime());
    }

    private void checkFieldAssertionsForGetUserBalancesTestSuccess(OBReadBalance1DataBalance balance) {
        assertNotNull(balance);

        assertNotNull(balance.getAccountId());

        assertNotNull(balance.getType());

        OBReadBalance1DataAmount balanceAmount = balance.getAmount();
        assertNotNull(balanceAmount);

        assertNotNull(balanceAmount.getCurrency());

        assertEquals("AED", balanceAmount.getCurrency());
    }

    private void assertGetAccountDetailsResponsePayload(OBReadAccount6 response) {
        OBReadAccount6Data responseData = response.getData();
        assertNotNull(responseData);

        List<OBAccount6> accountList = responseData.getAccount();
        assertNotNull(accountList);

        OBAccount6 account = accountList.get(0);
        assertNotNull(account);

        assertNotNull(account.getAccountId());
        assertNotNull(account.getCurrency());
        assertNotNull(account.getAccountType());
        assertNotNull(account.getDescription());
        assertNotNull(account.getOpeningDate());

        List<OBAccount4Account> accountAccount = account.getAccount();
        assertNotNull(accountAccount);
        assertEquals(3, accountAccount.size());

        assertNotNull(accountAccount.get(0));
        String schemeName = accountAccount.get(0).getSchemeName();
        assertNotNull(schemeName);
        assertEquals("IBAN.NUMBER", schemeName);
        String iban = accountAccount.get(0).getIdentification();
        assertNotNull(iban);

        assertNotNull(accountAccount.get(1));
        schemeName = accountAccount.get(1).getSchemeName();
        assertNotNull(schemeName);
        assertEquals("SWIFT.CODE", schemeName);
        String identification = accountAccount.get(1).getIdentification();
        assertNotNull(identification);

        assertNotNull(accountAccount.get(2));
        schemeName = accountAccount.get(2).getSchemeName();
        assertNotNull(schemeName);
        assertEquals("AHB.CIF", schemeName);
        String cifIdentification = accountAccount.get(2).getIdentification();
        assertNotNull(cifIdentification);
    }

    private void assertGetAccountsResponsePayload(OBReadAccount6 response) {
        OBReadAccount6Data data = response.getData();
        assertNotNull(data);

        List<OBAccount6> accountList = response.getData().getAccount();
        assertNotNull(accountList);
        assertTrue(accountList.size() > 0);

        OBAccount6 account = accountList.get(0);
        assertNotNull(account);

        assertNotNull(account.getDescription());
        assertNotNull(account.getAccountId());

        assertNotNull(account.getCurrency());
        assertEquals("AED", account.getCurrency());

        assertNotNull(account.getAccountType());
        assertNotNull(account.getOpeningDate());

        List<OBAccount4Account> accounts = account.getAccount();
        assertNotNull(accounts);
        assertEquals(3, accounts.size());

        OBAccount4Account accountDetails = accounts.get(0);
        assertNotNull(accountDetails.getSchemeName());
        assertEquals("IBAN.NUMBER", accountDetails.getSchemeName());

        accountDetails = accounts.get(1);

        assertNotNull(accountDetails.getSchemeName());
        assertEquals("SWIFT.CODE", accountDetails.getSchemeName());
        assertNotNull(accountDetails.getIdentification());
    }

    private void assertGetAccountBalancesResponsePayload(OBReadBalance1 balanceResponse) {
        OBReadBalance1Data balanceResponseData = balanceResponse.getData();
        assertNotNull(balanceResponseData);

        List<OBReadBalance1DataBalance> balanceList = balanceResponseData.getBalance();
        assertNotNull(balanceList);

        balanceList.forEach(this::checkFieldAssertionsForGetUserBalancesTestSuccess);

        assertEquals(3, balanceList.size());
        assertEquals(OBBalanceType1Code.INTERIM_AVAILABLE, balanceList.get(0).getType());
        assertEquals(OBBalanceType1Code.INTERIM_BOOKED, balanceList.get(1).getType());
        assertNotNull(balanceList.get(1).getAmount());
        assertNotNull(balanceList.get(1).getAmount().getAmount());
        assertEquals(OBBalanceType1Code.LOCKED_AMOUNT, balanceList.get(2).getType());
    }

    private void assertGetLockedAmountResponsePayload(OBReadTransaction6 lockedAmount) {
        assertNotNull(lockedAmount);
        OBReadDataTransaction6 lockedAmountData = lockedAmount.getData();
        assertNotNull(lockedAmountData);
    }

    private void assertCreateLegacyDtpPaymentPayload(OBWriteDomesticResponse5 response) {
        OBWriteDomesticResponse5Data data = response.getData();
        assertNotNull(data);

        assertEquals(data.getStatus(), OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);
        assertNotNull(data.getStatusUpdateDateTime());
        assertNotNull(data.getCreationDateTime());
        assertNotNull(data.getExpectedExecutionDateTime());
        assertNotNull(data.getExpectedSettlementDateTime());

        OBWriteDomestic2DataInitiation initiation = data.getInitiation();
        assertNotNull(initiation);

        assertNotNull(initiation.getInstructionIdentification());
        assertNotNull(initiation.getEndToEndIdentification());

        assertNotNull(initiation.getInstructedAmount());
        assertNotNull(initiation.getInstructedAmount().getAmount());
        assertNotNull(initiation.getInstructedAmount().getCurrency());

        assertNotNull(initiation.getCreditorAccount());
        assertNotNull(initiation.getCreditorAccount().getIdentification());
        assertNotNull(initiation.getCreditorAccount().getName());

        assertNotNull(initiation.getDebtorAccount());
        assertNotNull(initiation.getDebtorAccount().getIdentification());
        assertNotNull(initiation.getDebtorAccount().getName());

        assertNotNull(initiation.getRemittanceInformation());
        assertNotNull(initiation.getRemittanceInformation().getUnstructured());
    }

    private void assertCreateCardPaymentPayload(OBWriteCardPaymentResponse1 response) {
        assertNotNull(response.getData());
        assertNotNull(response.getData().getCardPaymentId());
        assertNotNull(response.getData().getCreationDateTime());
        assertNotNull(response.getData().getStatusUpdateDateTime());

        assertNotNull(response.getData().getInitiation());
        assertNotNull(response.getData().getInitiation().getInstructedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getLockedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getCurrency());

        assertNotNull(response.getData().getInitiation().getDebtorAccount());
        assertNotNull(response.getData().getInitiation().getDebtorAccount().getIdentification());

        assertNotNull(response.getSupplementaryData());
        assertNotNull(response.getSupplementaryData().getAtUniqueId());
        assertNotNull(response.getSupplementaryData().getCardNumber());
        assertNotNull(response.getSupplementaryData().getAtLocalDate());
        assertNotNull(response.getSupplementaryData().getAtmTermRef());
        assertNotNull(response.getSupplementaryData().getRetrievalReference());
        assertNotNull(response.getSupplementaryData().getAtTransDate());
        assertNotNull(response.getSupplementaryData().getTerminalId());
        assertNotNull(response.getSupplementaryData().getAtTraceNo());
        assertNotNull(response.getSupplementaryData().getAtTxnAmount());
        assertNotNull(response.getSupplementaryData().getAtProcCode());
        assertNotNull(response.getSupplementaryData().getMerchantLoc());
        assertNotNull(response.getSupplementaryData().getTxnAmount());
        assertNotNull(response.getSupplementaryData().getTxnCurrency());
        assertNotNull(response.getSupplementaryData().getAtAuthCode());
    }

    private void assertCreateCardDepositPayload(OBWriteCardDepositResponse1 response) {
        assertNotNull(response.getData());
        assertNotNull(response.getData().getCardPaymentId());
        assertNotNull(response.getData().getCreationDateTime());
        assertNotNull(response.getData().getStatusUpdateDateTime());

        assertNotNull(response.getData().getInitiation());
        assertNotNull(response.getData().getInitiation().getInstructedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getCurrency());

        assertNotNull(response.getData().getInitiation().getCreditorAccount());
        assertNotNull(response.getData().getInitiation().getCreditorAccount().getIdentification());

        assertNotNull(response.getSupplementaryData());
        assertNotNull(response.getSupplementaryData().getAtUniqueId());
        assertNotNull(response.getSupplementaryData().getCardNumber());
        assertNotNull(response.getSupplementaryData().getAtLocalDate());
        assertNotNull(response.getSupplementaryData().getRetrievalReference());
        assertNotNull(response.getSupplementaryData().getAtTransDate());
        assertNotNull(response.getSupplementaryData().getTerminalId());
        assertNotNull(response.getSupplementaryData().getAtTraceNo());
        assertNotNull(response.getSupplementaryData().getAtTxnAmount());
        assertNotNull(response.getSupplementaryData().getAtProcCode());
        assertNotNull(response.getSupplementaryData().getMerchantLoc());
        assertNotNull(response.getSupplementaryData().getTxnAmount());
        assertNotNull(response.getSupplementaryData().getTxnCurrency());
        assertNotNull(response.getSupplementaryData().getAtAuthCode());
    }

    private void assertCreateCardWithdrawalPayload(OBWriteCardWithdrawalResponse1 response) {
        assertNotNull(response.getData());
        assertNotNull(response.getData().getCardPaymentId());
        assertNotNull(response.getData().getCreationDateTime());
        assertNotNull(response.getData().getStatusUpdateDateTime());

        assertNotNull(response.getData().getInitiation());
        assertNotNull(response.getData().getInitiation().getInstructedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getCurrency());

        assertNotNull(response.getData().getInitiation().getDebtorAccount());
        assertNotNull(response.getData().getInitiation().getDebtorAccount().getIdentification());

        assertNotNull(response.getSupplementaryData());
        assertNotNull(response.getSupplementaryData().getAtUniqueId());
        assertNotNull(response.getSupplementaryData().getCardNumber());
        assertNotNull(response.getSupplementaryData().getAtLocalDate());
        assertNotNull(response.getSupplementaryData().getRetrievalReference());
        assertNotNull(response.getSupplementaryData().getAtTransDate());
        assertNotNull(response.getSupplementaryData().getTerminalId());
        assertNotNull(response.getSupplementaryData().getAtTraceNo());
        assertNotNull(response.getSupplementaryData().getAtTxnAmount());
        assertNotNull(response.getSupplementaryData().getAtProcCode());
        assertNotNull(response.getSupplementaryData().getMerchantLoc());
        assertNotNull(response.getSupplementaryData().getTxnAmount());
        assertNotNull(response.getSupplementaryData().getTxnCurrency());
        assertNotNull(response.getSupplementaryData().getAtAuthCode());
    }

    private void beforePaymentTests() {
        //envUtils.ignoreTestInEnv(Environments.NONE);
    }
}
