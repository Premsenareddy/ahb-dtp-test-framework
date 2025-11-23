package uk.co.deloitte.banking.payments.transfer.domestic.scenarios;

import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
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

import javax.inject.Inject;
import javax.swing.undo.CannotRedoException;
import java.math.BigDecimal;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DomesticTransferStepUpAuthTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    TransferUtils transferUtils;

    private AlphaTestUser alphaTestUser;

    private final int transferFromT24Amount = 10;

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;

    private void createUserForSetAccountForLogin() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            }
        else {
            this.alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }
    }

    private void internalPaymentsStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private void internalPaymentsStepUpAuthOTP() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(isNotBlank(otpCO.getPassword()));
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private OBWriteDomesticConsent4 obWriteDomesticConsent4Valid(int testTransferAmount) {
        return PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(testTransferAmount),
                "AED",
                "CHC",
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }


    private WriteDomesticPayment1 obWriteDomestic2Valid(String consentId, int testTransferAmount) {
        return PaymentRequestUtils.prepareDomesticTransferRequest(consentId,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(testTransferAmount),
                "CHC",
                "unstructured",
                BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    public void valid_DTP_transfer_over_auth_limit_step_up_required_otp_one_transaction() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        int testTransferAmount = Integer.parseInt(maxUnauthLimit) + 10;
        final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(testTransferAmount);

        AND("I transfer the test money to the created user's account");

        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUser.getAccountNumber(),
                BigDecimal.valueOf(testTransferAmount));
assertNotNull(response);

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertEquals(Integer.parseInt(interimAvailable), testTransferAmount);
        Assertions.assertEquals(Integer.parseInt(interimBooked), testTransferAmount);

        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount);

        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("I complete OTP step up auth");
        internalPaymentsStepUpAuthOTP();

        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        final WriteDomesticPayment1 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), testTransferAmount);

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);

        AND("The users balance is updated after the payment");
        OBReadBalance1 balanceResponseUpdated = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailablePostPayment = balanceResponseUpdated.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedPostPayment = balanceResponseUpdated.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertEquals(Integer.parseInt(interimAvailablePostPayment), 0);
        Assertions.assertEquals(Integer.parseInt(interimBookedPostPayment), 0);

        DONE();
    }

    private void assertNotNull(OBWriteCardDepositResponse1 response) {
    }

    @Test
    public void valid_DTP_transfer_over_auth_limit_step_up_required_biometrics_one_transaction() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        int testTransferAmount = Integer.parseInt(maxUnauthLimit) + 10;
        BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(testTransferAmount);
        AND("I transfer the test money to the created user's account");


        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
        alphaTestUser.getAccountNumber(),
        BigDecimal.valueOf(testTransferAmount));

        assertNotNull(response);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
               OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount);


        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);


        WHEN("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        final WriteDomesticPayment1 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), testTransferAmount);

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);

        DONE();
    }

    @Test
    public void valid_DTP_transfer_over_auth_limit_step_up_required_biometrics_three_transactions() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        int testTransferAmount = Integer.parseInt(maxUnauthLimit) + 10;

        AND("I transfer the test money to the created user's account");

        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUser.getAccountNumber(),
                BigDecimal.valueOf(testTransferAmount));
        assertNotNull(response);

        int singleTransactionsAmount = testTransferAmount / 3;
        BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(singleTransactionsAmount);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
             OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the consent payload with a value of : " + singleTransactionsAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(singleTransactionsAmount);

        //First transaction
        THEN("I can successfully create consent for the first transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the first transaction");
        final WriteDomesticPayment1 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the first transaction");
        final OBWriteDomesticResponse5 transferResponse =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);


        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        //Second transaction
        THEN("I can successfully create consent for the second transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse2 =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(obWriteDomesticConsentResponse2.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the second transaction");
        final WriteDomesticPayment1 transferRequest2 = obWriteDomestic2Valid(obWriteDomesticConsentResponse2.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the second transaction");
        final OBWriteDomesticResponse5 transferResponse2 =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest2);
        Assertions.assertNotNull(transferResponse2);

        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        //Third transaction
        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);


        WHEN("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();


        THEN("I can successfully create consent for the third transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse3 =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(obWriteDomesticConsentResponse3.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the third transaction");
        final WriteDomesticPayment1 transferRequest3 = obWriteDomestic2Valid(obWriteDomesticConsentResponse3.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the third transaction");
        final OBWriteDomesticResponse5 transferResponse3 =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest3);
        Assertions.assertNotNull(transferResponse3);

        AND("The transaction list for the user is updated");
        OBReadTransaction6 obReadTransaction6 = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());

        Assertions.assertEquals(obReadTransaction6.getData().getTransaction().size(), 3);
        DONE();

    }


}
