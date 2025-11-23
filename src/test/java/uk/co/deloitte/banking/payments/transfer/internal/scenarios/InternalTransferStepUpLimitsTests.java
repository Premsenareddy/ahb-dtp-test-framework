package uk.co.deloitte.banking.payments.transfer.internal.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
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
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalTransferStepUpLimitsTests {

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

    private AlphaTestUser alphaTestUser;

    private final int transferFromT24Amount = 10;

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";

    private static final String LIMIT_EXCEEDED = "UAE.PAYMENTS.PAYMENT_LIMIT_REACHED";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;

    private void createUserForSetAccountForLogin() {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

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
    @Tag("Maintenance")
    @Test
    public void valid_DTP_transfer_over_max_payment_limit_single_transaction() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxPaymentLimit = paymentConfiguration.getMaxPaymentLimit();
       // int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 10;
        BigDecimal testTransferAmount = new BigDecimal(Integer.parseInt(maxPaymentLimit) + 10);
        AND("I transfer the test money to the created user's account");
        //this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(testTransferAmount));
        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                alphaTestUser.getAccountNumber(),
                testTransferAmount);
        assertNotNull(response);

        WHEN("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount.intValueExact());


        AND("I submit the valid payment consent request with a value greater than the daily allowance");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 422);

        THEN("A 422 is returned from the service"+consentResponse.getCode());
        Assertions.assertTrue(consentResponse.getCode().contains(LIMIT_EXCEEDED), "Error message was not as " +
                "expected, test expected : " + LIMIT_EXCEEDED);


        WHEN("I create the consent payload with a value of the daily limit : " + maxPaymentLimit);
        final OBWriteDomesticConsent4 obWriteDomesticConsent4 = obWriteDomesticConsent4Valid(Integer.parseInt(maxPaymentLimit));

        AND("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("The consent is created with a 201 response");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, obWriteDomesticConsent4, 201);
        assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the transaction");
        final OBWriteDomestic2 transferRequest2 = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), Integer.parseInt(maxPaymentLimit));

        THEN("I successfully trigger the internal transfer payment for the transaction");
        final OBWriteDomesticResponse5 transferResponse2 =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest2);
        assertNotNull(transferResponse2);

        AND("The users balance is reduced");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(Integer.parseInt(interimAvailable), testTransferAmount);
        Assertions.assertNotEquals(Integer.parseInt(interimBooked), testTransferAmount);

        DONE();
    }

    @Test
    public void valid_DTP_transfer_over_max_payment_limit_three_transactions() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxPaymentLimit = paymentConfiguration.getMaxPaymentLimit();
        int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 10;

        AND("I transfer the test money to the created user's account");


        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(testTransferAmount));


        int individualTransactionValue = testTransferAmount / 3;

        WHEN("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(individualTransactionValue);

        //First transaction
        AND("I complete OTP step up auth");
        internalPaymentsStepUpAuthBiometrics();

        THEN("The consent is created with a 201 response");
        final OBWriteDomesticConsentResponse5 firstConsent =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(firstConsent.getData().getConsentId());

        AND("I create the valid matching payment transfer request for the first transaction");
        final OBWriteDomestic2 transferRequest2 = obWriteDomestic2Valid(firstConsent.getData().getConsentId(), individualTransactionValue);

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
        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(secondConsent.getData().getConsentId(), individualTransactionValue);

        THEN("I successfully trigger the internal transfer payment for the first transaction");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        //third transaction
        AND("I submit the valid payment consent request for the third transaction over the daily limit");
        final OBErrorResponse1 thirdConsent =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 422);

        THEN("A 422 is returned from the service");
        Assertions.assertTrue(thirdConsent.getCode().contains(LIMIT_EXCEEDED), "Error message was not as " +
                "expected, test expected : " + LIMIT_EXCEEDED);


        AND("The transaction list for the user is updated with 2 successful transactions");
        OBReadTransaction6 obReadTransaction6 = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertEquals(obReadTransaction6.getData().getTransaction().size(), 2);
        ;

        DONE();
    }

}