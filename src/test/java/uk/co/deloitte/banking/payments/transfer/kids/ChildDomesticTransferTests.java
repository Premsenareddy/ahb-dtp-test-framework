package uk.co.deloitte.banking.payments.transfer.kids;


import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildDomesticTransferTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private TemenosConfig temenosConfig;


    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;



    private static final int loginMinWeightExpectedBio = 31;


    private String dependantId = "";
    static String relationshipId;

    private AlphaTestUser alphaTestUserFather;

    private AlphaTestUser alphaTestUserChild;

    private String creditorAccountNumber = null;

    private String debtorAccountNumber = null;



    private void setupFatherTestUser() {
        // envUtils.ignoreTestInEnv(Environments.ALL);
        if (this.alphaTestUserFather == null) {
            this.alphaTestUserFather = new AlphaTestUser();
            this.alphaTestUserFather.setGender(OBGender.MALE);
            this.alphaTestUserFather = alphaTestUserFactory.setupCustomer(alphaTestUserFather);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserFather);
            createUserRelationship(alphaTestUserFather);
            createDependentCustomer(alphaTestUserFather, OBRelationshipRole.FATHER);

        }
    }

    private void setupUserChild() {
        // envUtils.ignoreTestInEnv(Environments.ALL);
        if (this.alphaTestUserChild == null) {
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild.setGender(OBGender.FEMALE);
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserFather, alphaTestUserChild, relationshipId, dependantId);
            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserFather, relationshipId);
            OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                    OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
            assertNotNull(savings);
            OBWriteAccountResponse1Data data = savings.getData();
            assertNotNull(data);
            assertNotNull(data.getAccountId());
            alphaTestUserChild.setAccountNumber(data.getAccountId());
            OBWriteCardDepositResponse1 response =
                    cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                            alphaTestUserChild.getAccountNumber(),
                            BigDecimal.valueOf(20));
            assertNotNull(response);

        }
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
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(obRelationshipRole)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        assertNotNull(response);
        assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();

    }



    private void beneStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUserChild.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHC"})
    public void valid_domestic_payment_create_consent_and_transfer_valid_reference(String validReference) {
        TEST("17624-15376- kids internal transfer transfer");
        TEST("AHBDB-354 / AHBDB-1632 - valid domestic consent with valid reference : " + validReference);
        GIVEN("I have a valid test user set up with a dependent relationship of father");
        setupFatherTestUser();
        AND("I have a child who has been onboarded by the father");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserFather.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());


        WHEN("I create a beneficiary with an valid type and IBAN");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(temenosConfig.getCreditorIban());
        beneficiaryData.setBeneficiaryType("domestic_bank");

        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUserChild, beneficiaryData);


        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1.00");
       /* Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUserChild,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUserChild.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));*/
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
                "AED",
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUserChild, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final WriteDomesticPayment1 transferRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUserChild.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorIban(),
                        ACCOUNT_IBAN,
                        PAYMENT_AMOUNT,
                        validReference,
                        "unstructured",
                        WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                domesticTransferApiFlows.executeRelationshipDomesticPayment(alphaTestUserFather, transferRequest , relationships.getData().getRelationships().get(0).getConnectionId().toString());
        Assertions.assertNotNull(paymentResponse);

        assertEquals(paymentResponse.getData().getStatus(),
                OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        // String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount()
        // .getAmount().replace(".00", "");
        String interimBookedAfterPayment =
                balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        // Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        // commenting assertions due to t24 performance
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        // commenting assertions due to t24 performance
        //   Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);



        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaries(alphaTestUserChild).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertNotNull(GetObBeneficiary5.getLastTransactionDateTime());

        DONE();
    }


}
