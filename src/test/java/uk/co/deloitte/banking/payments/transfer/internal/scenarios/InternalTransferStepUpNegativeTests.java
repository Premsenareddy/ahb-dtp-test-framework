package uk.co.deloitte.banking.payments.transfer.internal.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalTransferStepUpNegativeTests {

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

    private static final String MAX_OTP = "UAE.OTP.LIMIT_REACHED";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;

    private void createUserForSetAccountForLogin() {
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

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



    @Test
    public void valid_DTP_transfer_over_auth_limit_user_is_locked_incorrect_otp() {
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

        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount);

        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        AND("The user fails to complete step up auth correctly more than 3 times with a wrong otp code");

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        otpCO.setPassword(RandomDataGenerator.generateRandomNumeric(6));
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();

        WHEN("They try to step up incorrectly first attempt");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError1 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError1.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly second attempt");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError2 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError2.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly third attempt");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError3 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError3.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        THEN("Their account is locked with a 423 response");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError5 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);

        Assertions.assertTrue(stepError5.getCode().contains(MAX_OTP), "Error message was not as " +
                "expected, test expected : " + MAX_OTP);

        DONE();
    }

    @Test
    public void valid_DTP_transfer_over_auth_limit_user_is_locked_incorrect_biometrics() {
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

        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount);

        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        AND("The user fails to complete step up auth correctly more than 3 times with a wrong password");

        String password = RandomDataGenerator.generateRandomSHA512enabledPassword();

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(password).scope("accounts").weight(loginMinWeightExpectedBio).build();

        WHEN("They try to step up incorrectly first attempt");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError1 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError1.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly second attempt");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError2 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError2.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly third attempt");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError3 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError3.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        THEN("Their account is locked with a 423 response");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 stepError5 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);

        Assertions.assertTrue(stepError5.getCode().contains(MAX_OTP), "Error message was not as " +
                "expected, test expected : " + MAX_OTP);

    }

}
