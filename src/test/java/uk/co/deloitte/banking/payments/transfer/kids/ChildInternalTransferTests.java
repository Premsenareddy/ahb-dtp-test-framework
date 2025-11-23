package uk.co.deloitte.banking.payments.transfer.kids;

import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
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
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.common.TransferUtils;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

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
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.EQUAL_TO;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.LESS_THEN;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.PAYMENTS_ASSUMPTION_MSG;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildInternalTransferTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;


    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    TransferUtils transferUtils;


    private static final int loginMinWeightExpectedBio = 31;

    private String dependantId = "";
    static String relationshipId;

    private AlphaTestUser alphaTestUserFather;
    private AlphaTestUser alphaTestUserChild;

   private void setupFatherTestUser() {
       envUtils.ignoreTestInEnv(Environments.SIT , Environments.NFT , Environments.STG);
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
        envUtils.ignoreTestInEnv(Environments.SIT , Environments.NFT , Environments.STG);
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
    @ValueSource(strings = {"InternalTransferReferenceValid"})
    public void valid_Child_DTP_transfer_create_consent_and_transfer_valid_reference(String validReference) {
        TEST("17624-15376- kids internal transfer transfer");
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
        beneficiaryData.setAccountNumber(temenosConfig.getCreditorAccountId());
        beneficiaryData.setBeneficiaryType("al_hilal");

        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUserChild, beneficiaryData);


        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1.00");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUserChild, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUserChild, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUserChild.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        GIVEN("I have a valid access token and account scope and bank account");
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUserChild.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                validReference,
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUserChild.getAccountNumber());

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);


        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        THEN("I submit the valid payment consent request");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUserChild, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        Assertions.assertEquals(consentResponse.getData().getInitiation().getCreditorAccount().getIdentification(),
                temenosConfig.getCreditorAccountId());

        AND("I create the valid matching payment transfer request");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUserChild.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        validReference,
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createRelationshipInternalTransferPayment(alphaTestUserFather, transferRequest,  relationships.getData().getRelationships().get(0).getConnectionId().toString());
        Assertions.assertNotNull(transferResponse);
        assertEquals(transferResponse.getData().getStatus(),
                OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailableAfterPayment =
                balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment =
                balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

          Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);

        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUserChild,
                alphaTestUserChild.getAccountNumber());
        Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);


        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaries(alphaTestUserChild).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertNotNull(GetObBeneficiary5.getLastTransactionDateTime());
        DONE();
    }


}
