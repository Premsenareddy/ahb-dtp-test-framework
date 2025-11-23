package uk.co.deloitte.banking.payments.transfer.limitsJourney;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.common.TransferUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransferLimitsJourney {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    TransferUtils transferUtils;

    private AlphaTestUser alphaTestUser;

    private final int transferFromT24Amount = 10;

    private static final String LIMIT_EXCEEDED = "UAE.PAYMENTS.PAYMENT_LIMIT_REACHED";

    private static final int loginMinWeightExpectedBio = 31;


    private void createUserForSetAccountForLogin() {
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
        }

    }

    private void internalPaymentsStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser,
                StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest =
                StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private OBWriteDomesticConsent4 obWriteDomesticConsent4Valid(int testTransferAmount, String debtorAccount,
                                                                 String creditorAccount) {
        return PaymentRequestUtils.prepareInternalConsent(
                debtorAccount,
                ACCOUNT_NUMBER,
                creditorAccount,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private OBWriteDomestic2 obWriteDomestic2Valid(String consentId, int testTransferAmount, String debtorAccount,
                                                   String creditorAccount) {
        return PaymentRequestUtils.prepareInternalTransferRequest(consentId,
                debtorAccount,
                ACCOUNT_NUMBER,
                creditorAccount,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    //These tests need to be run in order

    @Test
    @Order(1)
    public void valid_DTP_transfer_users_own_accounts_no_step_up() {
        TEST("AHBDB-12885:: Test defect fix");

        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        int testTransferAmount = Integer.parseInt(maxUnauthLimit) + 10;

        AND("I transfer the test money to the created user's account");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, BigDecimal.valueOf(testTransferAmount));


        AND("The user has a second account");
        String secondAccountId = accountApi.createCustomerCurrentAccount(alphaTestUser).getData().getAccountId();

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertEquals(Integer.parseInt(interimAvailable), testTransferAmount);
        Assertions.assertEquals(Integer.parseInt(interimBooked), testTransferAmount);

        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount,
                alphaTestUser.getAccountNumber(), secondAccountId);

        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        final OBWriteDomestic2 transferRequest =
                obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), testTransferAmount,
                        alphaTestUser.getAccountNumber(), secondAccountId);

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        DONE();
    }

    @Test
    @Order(2)
    public void valid_DTP_transfer_two_transfers() {
        TEST("AHBDB-12885:: Test defect fix");
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxPaymentLimit = paymentConfiguration.getMaxPaymentLimit();
        int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 10;

        AND("I transfer the test money to the created user's account");


        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, BigDecimal.valueOf(testTransferAmount));

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        int individualTransactionValue = testTransferAmount / 3;

        WHEN("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(individualTransactionValue,
                alphaTestUser.getAccountNumber(), temenosConfig.getCreditorAccountId());

        //First transaction
        AND("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("The consent is created with a 201 response");
        final OBWriteDomesticConsentResponse5 firstConsent =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(firstConsent.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the first transaction");
        final OBWriteDomestic2 transferRequest2 = obWriteDomestic2Valid(firstConsent.getData().getConsentId(),
                individualTransactionValue, alphaTestUser.getAccountNumber(), temenosConfig.getCreditorAccountId());

        THEN("I successfully trigger the internal transfer payment for the first transaction");
        final OBWriteDomesticResponse5 transferResponse2 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest2);
        assertNotNull(transferResponse2);

        //Second transaction
        AND("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("The consent is created with a 201 response");
        final OBWriteDomesticConsentResponse5 secondConsent =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(secondConsent.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the first transaction");
        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(secondConsent.getData().getConsentId(),
                individualTransactionValue, alphaTestUser.getAccountNumber(), temenosConfig.getCreditorAccountId());

        THEN("I successfully trigger the internal transfer payment for the first transaction");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailableAfterPayment =
                balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment =
                balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        DONE();
    }

    @Test
    @Order(3)
    public void valid_domestic_payment_transfer_over_users_limit() {
        TEST("AHBDB-12885:: Test defect fix");

        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxPaymentLimit = paymentConfiguration.getMaxPaymentLimit();
        int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 10;
        int individualTransactionValue = testTransferAmount / 3;

        AND("The user has a balance");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, BigDecimal.valueOf(testTransferAmount));

        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(individualTransactionValue),
                "AED",
                "CHC",
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        AND("I submit the valid payment consent request for the third transaction over the daily limit");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 422);

        THEN("A 422 is returned from the service");
        Assertions.assertTrue(consentResponse.getCode().contains(LIMIT_EXCEEDED), "Error message was not as " +
                "expected, test expected : " + LIMIT_EXCEEDED);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        //AHBDB-12995
        //TODO FIX AHBDB-12995
        //Assertions.assertEquals(response.getData().getTransaction().size(), 3);


        DONE();
    }

}
