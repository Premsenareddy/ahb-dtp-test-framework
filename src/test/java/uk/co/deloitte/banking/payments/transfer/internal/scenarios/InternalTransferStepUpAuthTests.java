package uk.co.deloitte.banking.payments.transfer.internal.scenarios;

import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
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
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalTransferStepUpAuthTests {

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
    protected CardProtectedApi cardProtectedApi;

    @Inject
    protected TransferUtils transferUtils;

    private AlphaTestUser alphaTestUser;


    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";

    private static final String MAX_PAYMENT_LIMIT_REACHED = "UAE.PAYMENTS.PAYMENT_LIMIT_REACHED";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;

    private void createUserForSetAccountForLogin() {

        //envUtils.ignoreTestInEnv("AHBDB-14381:Transact Timeout", Environments.ALL);
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        //this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);

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
        return PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private OBWriteDomestic2 obWriteDomestic2Valid(String consentId, int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalTransferRequest(consentId,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount), "validReference", "unstructured",
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
        BigDecimal PAYMENT_AMOUNT = new BigDecimal(Integer.parseInt(maxUnauthLimit) + 10);

        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        AND("I transfer the test money to the created user's account");

        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(alphaTestUser.getAccountNumber(),
                temenosConfig.getCreditorAccountId(),
                PAYMENT_AMOUNT);

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertTrue(new BigDecimal(interimAvailable).compareTo(PAYMENT_AMOUNT) == 0);
        Assertions.assertTrue(new BigDecimal(interimBooked).compareTo(PAYMENT_AMOUNT) == 0);

        AND("I create the consent payload with a value of : " + PAYMENT_AMOUNT);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(PAYMENT_AMOUNT.intValueExact());

        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("I complete OTP step up auth");
        internalPaymentsStepUpAuthOTP();

        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), PAYMENT_AMOUNT.intValueExact());

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        DONE();
    }


    @Test
    public void valid_DTP_transfer_over_auth_limit_step_up_required_biometrics_one_transaction() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal(Integer.parseInt(maxUnauthLimit) + 10);
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        AND("I transfer the test money to the created user's account");


        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUser.getAccountNumber(),
                PAYMENT_AMOUNT);
        assertNotNull(response);

        AND("I create the consent payload with a value of : " + PAYMENT_AMOUNT);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(PAYMENT_AMOUNT.intValueExact());


        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);


        WHEN("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), PAYMENT_AMOUNT.intValueExact());

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        DONE();
    }

    @Test
    public void valid_DTP_transfer_over_auth_limit_step_up_required_biometrics_three_transactions() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal(Integer.parseInt(maxUnauthLimit) + 10);
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        AND("I transfer the test money to the created user's account");


        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUser.getAccountNumber(),
                PAYMENT_AMOUNT);
        assertNotNull(response);

        int singleTransactionsAmount = PAYMENT_AMOUNT.intValueExact() / 3;

        AND("I create the consent payload with a value of : " + singleTransactionsAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(singleTransactionsAmount);

        //First transaction
        THEN("I can successfully create consent for the first transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the first transaction");
        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the first transaction");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        //refresh token before transaction
        alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        //Second transaction
        THEN("I can successfully create consent for the second transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse2 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse2.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the second transaction");
        final OBWriteDomestic2 transferRequest2 = obWriteDomestic2Valid(obWriteDomesticConsentResponse2.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the second transaction");
        final OBWriteDomesticResponse5 transferResponse2 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest2);
        assertNotNull(transferResponse2);

        //Third transaction
        //refresh token before transaction
        alphaTestUserFactory.refreshAccessToken(alphaTestUser);


        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);


        WHEN("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();


        THEN("I can successfully create consent for the third transaction");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse3 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse3.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the third transaction");
        final OBWriteDomestic2 transferRequest3 = obWriteDomestic2Valid(obWriteDomesticConsentResponse3.getData().getConsentId(), singleTransactionsAmount);

        THEN("I successfully trigger the internal transfer payment for the third transaction");
        final OBWriteDomesticResponse5 transferResponse3 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest3);
        assertNotNull(transferResponse3);

        AND("The transaction list for the user is updated");
        OBReadTransaction6 obReadTransaction6 = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());

        DONE();

    }

    @Test
    public void valid_DTP_transfer_over_max_payment_limit_single_transaction() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxPaymentLimit = paymentConfiguration.getMaxPaymentLimit();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal(Integer.parseInt(maxPaymentLimit) + 10);
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        AND("I transfer the test money to the created user's account");


        /*OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUser.getAccountNumber(),
                PAYMENT_AMOUNT);
        assertNotNull(response);*/

        AND("I create the consent payload with a value of : " + PAYMENT_AMOUNT);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(PAYMENT_AMOUNT.intValueExact());


        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 422);
        Assertions.assertTrue(consentResponse.getCode().contains(MAX_PAYMENT_LIMIT_REACHED), "Payment would exceed daily limit of : 250000");
        DONE();
    }

}
