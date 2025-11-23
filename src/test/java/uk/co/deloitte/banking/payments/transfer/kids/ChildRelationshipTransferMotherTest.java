package uk.co.deloitte.banking.payments.transfer.kids;

import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.common.TransferUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code.BORNE_BY_DEBTOR;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.*;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Kids")
public class ChildRelationshipTransferMotherTest {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    EnvUtils envUtils;

    private String dependantId = "";

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private CustomerApi customerApiV1;

    @Inject
    private AccountApi accountApi;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    TransferUtils transferUtils;

    static String relationshipId;
    static String otpCode;


    private static final int loginMinWeightExpected = 31;

    private AlphaTestUser alphaTestUserChild;

    private final static String TEMPORARY_PASSWORD = "validtestpassword";

    private AlphaTestUser alphaTestUserMother;
    private static final int otpWeightRequested = 31;
    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";
    private static final String LIMIT_EXCEEDED = "UAE.PAYMENTS.PAYMENT_LIMIT_REACHED";


    private void internalPaymentsStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUserChild.getUserPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest);
    }

    private void setupMotherTestUser() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        /**TODO need to create user every testcase as to verify limit and step up testcases**/
            this.alphaTestUserMother = new AlphaTestUser();
            this.alphaTestUserMother.setGender(OBGender.FEMALE);
            this.alphaTestUserMother = alphaTestUserFactory.setupCustomer(alphaTestUserMother);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserMother);
            createUserRelationship(alphaTestUserMother);
            createDependentCustomer(alphaTestUserMother, OBRelationshipRole.MOTHER);
    }

    private void setupUserChild() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        /**TODO need to create user every testcase as to verify limit and step up testcases**/
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild.setGender(OBGender.FEMALE);
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserMother, alphaTestUserChild, relationshipId, dependantId);
            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserMother, relationshipId);
            OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                    OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
            assertNotNull(savings);
            OBWriteAccountResponse1Data data = savings.getData();
            assertNotNull(data);
            assertNotNull(data.getAccountId());
            alphaTestUserChild.setAccountNumber(data.getAccountId());
    }


    private void createUserRelationship(AlphaTestUser alphaTestUser) {
        UserRelationshipWriteRequest request = UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        assertNotNull(response);
        assertNotNull(response.getUserId());
        dependantId = response.getUserId();
    }

    private void createDependentCustomer(AlphaTestUser alphaTestUser, OBRelationshipRole obRelationshipRole) {
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("dependent full name")
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(obRelationshipRole)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        assertNotNull(response);
        assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();

    }

    private OBWriteDomesticConsent4 obWriteDomesticConsent4Valid(int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalConsent(
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUserMother.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

    }

    private OBWriteDomestic2 obWriteDomestic2Valid(String consentId, int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalTransferRequest(consentId,
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUserMother.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    @Test
    @Order(1)
    public void positive_kid_can_transfer_to_mother() {
        TEST("AHBDB-15376- kids transfer, No maker checker required if kid is transferring to onboarded by person account");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("The child's account has a balance");
        cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUserChild.getAccountNumber(),
                BigDecimal.valueOf(10));
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        AND("I create the consent payload");
            final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                    alphaTestUserChild.getAccountNumber(),
                    ACCOUNT_NUMBER,
                    alphaTestUserMother.getAccountNumber(),
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

            Assertions.assertEquals(consentResponse.getData().getInitiation().getCreditorAccount().getIdentification(), alphaTestUserMother.getAccountNumber());

            AND("I create the valid matching payment transfer request");
            final OBWriteDomestic2 transferRequest =
                    PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                            alphaTestUserChild.getAccountNumber(),
                            ACCOUNT_NUMBER,
                            alphaTestUserMother.getAccountNumber(),
                            ACCOUNT_NUMBER,
                            new BigDecimal("10"), "validReference", "unstructured",
                            RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

            THEN("I successfully trigger the internal transfer payment");
            final OBWriteDomesticResponse5 transferResponse =
                    this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest);
            Assertions.assertNotNull(transferResponse);

            AND("The users balance is reduced");
            OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUserChild,
                    alphaTestUserChild.getAccountNumber());
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

    /**
     TestCaseID: AHBDB-16711
     Description:User should get 403 response if kids does transfer to other than Mother account and kid is onboarded by Mother
     */
    @Test
    @Order(2)
    public void negative_kid_cant_transfer_to_another_DTP_account() {
        TEST("AHBDB-15376- kids transfer, kid can't transfer to another dtp account if onboarded by mother");
        TEST("AHBDB-15385 -Payments Service - Skip max transfer limit check on internal and domestic transfer consent for kid where maker checker needed.");
        TEST("AHBDB-16711-User should get 403 response if kids does transfer to other than Mother account and kid is onboarded by Mother");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("The child's account has a balance");
        cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUserChild.getAccountNumber(),
                BigDecimal.valueOf(10));
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        AND("I create the consent payload with another users DTP account number");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("10"),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("I submit the valid payment consent request then a 403 is returned");
        this.transferApiFlows.createInternalPaymentConsentError(alphaTestUserChild, consent4, 403);


        DONE();
    }

    /**
     TestCaseID: AHBDB-16716
     Description:User should get 403 code, Kid to internal payment more than 50K Step up should required and if kid don't do step up if kid is onboarded by mother
     **/
    @Test
    public void valid_DTP_kids_transfer_over_auth_limit_step_up_required_otp_one_transaction() {
        TEST("AHBDB-15376/AHBDB-15385 -Payments Service - Skip max transfer limit check on internal and domestic transfer consent for kid where maker checker needed.");
        TEST("AHBDB-16716-User should get 403 code, Kid to internal payment more than 50K Step up should required and if kid don't do step up if kid is onboarded by mother");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());
        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal(Integer.parseInt(maxUnauthLimit) + 10);


        AND("The child's account has a balance");


        transferUtils.topUpUserAccountWithCardPayment(alphaTestUserChild, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUserChild, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUserChild.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        AND("I transfer the test money to the created user's account");


        cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUserChild.getAccountNumber(),
                PAYMENT_AMOUNT);

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");


        AND("I create the consent payload with a value of : " + PAYMENT_AMOUNT);



        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUserMother.getAccountNumber(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUserChild, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("I complete OTP step up auth");
        //internalPaymentsByKidStepUpAuthOTP();
        internalPaymentsStepUpAuthBiometrics();
        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 consentResponse2 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUserChild, consent4, 201);
        assertNotNull(consentResponse2.getData().getConsentId());

        AND("I create the valid matching payment transfer request");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse2.getData().getConsentId(),
                        alphaTestUserChild.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        alphaTestUserMother.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        //new BigDecimal("10"),
                        "validReference",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest);
        Assertions.assertNotNull(transferResponse);

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
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
    /**
          TestCaseID: AHBDB-17595
          Description:User should get 422 success response if kids does transfer more than max limit to Mother account then maker checker should not happened and API should not allow transaction happened
     **/
    @Test
    public void valid_DTP_kids_transfer_Mother_over_max_limit_required_one_transaction() {
        TEST("AHBDB-15376 / AHBDB-15385- Payments Service - Skip max transfer limit check on internal and domestic transfer consent for kid where maker checker needed. ");
        TEST("AHBDB-17595- User should get 422 success response if kids does transfer more than max limit to Mother account then maker checker should not happened and API should not allow transaction happened" );
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());
        String maxLimit = paymentConfiguration.getMaxPaymentLimit();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal(Integer.parseInt(maxLimit) + 10);


        AND("The child's account has a balance");
     /*cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
            alphaTestUserChild.getAccountNumber(),
             PAYMENT_AMOUNT);*/


        transferUtils.topUpUserAccountWithCardPayment(alphaTestUserChild, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUserChild, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUserChild.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        AND("I transfer the test money to the created user's account");


        cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUserChild.getAccountNumber(),
                PAYMENT_AMOUNT);

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");


        AND("I create the consent payload with a value of : " + PAYMENT_AMOUNT);



        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUserMother.getAccountNumber(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        internalPaymentsStepUpAuthBiometrics();

        WHEN("I step up auth");

        THEN("I can successfully create consent");
        final OBErrorResponse1 consentResponse2 =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUserChild, consent4, 422);


        DONE();
    }
    /**
     TestCaseID: AHBDB-17659
     Description:User should get 403 code, Kid to multiple internal payment and sum of transactions is more than 50K Step up should required and if kid don't do step up if kid is onboarded by mother
     **/
    @Test
    public void valid_DTP_kids_transfer_Mother_over_auth_limit_step_up_required_otp_multiple_transaction() {
        TEST("AHBDB-15376/AHBDB-15385 -Payments Service - Skip max transfer limit check on internal and domestic transfer consent for kid where maker checker needed.");
        TEST("AHBDB-17659-User should get 403 code, Kid to multiple internal payment and sum of transactions is more than 50K Step up should required and if kid don't do step up if kid is onboarded by mother");

        GIVEN("I have a valid access token and account scope and bank account");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal(Integer.parseInt(maxUnauthLimit) + 10);
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUserChild, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUserChild, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUserChild.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        AND("I transfer the test money to the created user's account");


        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(alphaTestUserChild.getAccountNumber(),
                temenosConfig.getCreditorAccountId(),
                PAYMENT_AMOUNT);
        assertNotNull(response);

        int singleTransactionsAmount = PAYMENT_AMOUNT.intValueExact() / 3;

        AND("I create the consent payload with a value of : " + singleTransactionsAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(singleTransactionsAmount);

        //First transaction
        THEN("I can successfully create consent for the first transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse1 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUserChild, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse1.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the first transaction");
        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse1.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the first transaction");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest);
        assertNotNull(transferResponse);

        //refresh token before transaction
        alphaTestUserFactory.refreshAccessToken(alphaTestUserChild);
        //Second transaction
        THEN("I can successfully create consent for the second transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse2 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUserChild, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse2.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the second transaction");
        final OBWriteDomestic2 transferRequest2 = obWriteDomestic2Valid(obWriteDomesticConsentResponse2.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the second transaction");
        final OBWriteDomesticResponse5 transferResponse2 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest2);
        assertNotNull(transferResponse2);

        //Third transaction
        //refresh token before transaction
        alphaTestUserFactory.refreshAccessToken(alphaTestUserChild);

        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUserChild, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);


        WHEN("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();


        THEN("I can successfully create consent for the third transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse3 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUserChild, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse3.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the third transaction");
        final OBWriteDomestic2 transferRequest3 = obWriteDomestic2Valid(obWriteDomesticConsentResponse3.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the third transaction");
        final OBWriteDomesticResponse5 transferResponse3 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest3);
        assertNotNull(transferResponse3);

        AND("The transaction list for the user is updated");
        OBReadTransaction6 obReadTransaction6 = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());

        DONE();
    }

    /**
     TestCaseID: AHBDB-17664
     Description:User should get 422 code, if Kid does multiple internal payment and sum of transactions is more than max limit to Mother account then maker checker should not happened and API should not allow transaction happened
     **/
    @Test
    public void valid_DTP_kids_transfer_mother_over_max_limit_required_multiple_transaction() {
        TEST("AHBDB-15376/AHBDB-15385: Payments Service - Skip max transfer limit check on internal and domestic transfer consent for kid where maker checker needed.");
        TEST("AHBDB-17664-User should get 422 code, if Kid does multiple internal payment and sum of transactions is more than max limit to Mother account then maker checker should not happened and API should not allow transaction happened");

        GIVEN("I have a valid access token and account scope and bank account");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        String maxPaymentLimit = paymentConfiguration.getMaxPaymentLimit();
        int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 10;

        AND("I transfer the test money to the created user's account");


        this.cardProtectedApi.createCardDepositWebhook(alphaTestUserChild.getAccountNumber(), BigDecimal.valueOf(testTransferAmount));


        int individualTransactionValue = testTransferAmount / 3;

        WHEN("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(individualTransactionValue);

        //First transaction
        AND("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("The consent is created with a 201 response");
        final OBWriteDomesticConsentResponse5 firstConsent =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUserChild, consent4, 201);
        assertNotNull(firstConsent.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the first transaction");
        final OBWriteDomestic2 transferRequest2 = obWriteDomestic2Valid(firstConsent.getData().getConsentId(), individualTransactionValue);

        THEN("I successfully trigger the internal transfer payment for the second transaction");
        final OBWriteDomesticResponse5 transferResponse2 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest2);
        assertNotNull(transferResponse2);

        //Second transaction
        AND("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("The second consent is created with a 201 response");
        final OBWriteDomesticConsentResponse5 secondConsent =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUserChild, consent4, 201);
        assertNotNull(secondConsent.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the second transaction");
        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(secondConsent.getData().getConsentId(), individualTransactionValue);

        THEN("I successfully trigger the internal transfer payment for the second transaction");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUserChild, transferRequest);
        assertNotNull(transferResponse);

        //third transaction
        AND("I submit the valid payment consent request for the third transaction over the daily limit");
        internalPaymentsStepUpAuthBiometrics();
        final OBErrorResponse1 thirdConsent =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUserChild, consent4, 422);

        THEN("A 422 is returned from the service");
        Assertions.assertTrue(thirdConsent.getCode().contains(LIMIT_EXCEEDED), "Error message was not as " +
                "expected, test expected : " + LIMIT_EXCEEDED);

        AND("The transaction list for the user is updated with 2 successful transactions");
        OBReadTransaction6 obReadTransaction6 = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertEquals(obReadTransaction6.getData().getTransaction().size(), 2);
        DONE();
    }
}
