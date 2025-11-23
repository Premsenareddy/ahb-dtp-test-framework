package uk.co.deloitte.banking.payments.stepUp;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
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
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StepUpJourney {


    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    private AlphaTestUser alphaTestUser;

    private static final String MAX_OTP = "UAE.OTP.LIMIT_REACHED";

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;


    private void setupTestUser() {
        envUtils.ignoreTestInEnv("AHBDB-13576 - user not being locked with step up failures across actions", Environments.CIT, Environments.SIT, Environments.NFT);
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
    public void negative_case_user_tries_to_step_up_wrong_password_biometrics() {
        TEST("AHBDB-370 - User completing biometrics step up uses wrong password 3 times and is then locked out");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an valid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        THEN("The client tries to create their beneficiary");

        uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");

        AND("The user fails to complete step up auth correctly more than 3 times with a wrong password");

        String password = RandomDataGenerator.generateRandomSHA512enabledPassword();

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(password).scope("accounts").weight(loginMinWeightExpectedBio).build();

        WHEN("They try to step up incorrectly first attempt");
        OBErrorResponse1 stepError1 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError1.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly second attempt");
        OBErrorResponse1 stepError2 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError2.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);


        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        otpCO.setPassword(RandomDataGenerator.generateRandomNumeric(6));
        final StepUpAuthRequest stepUpAuthValidationRequestOTP = StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();

        WHEN("They try to step up incorrectly first attempt OTP");
        OBErrorResponse1 stepErrorOTP1 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequestOTP, 403);
        Assertions.assertTrue(stepErrorOTP1.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        THEN("Their account is locked with a 423 response");
        //the format of these errors will change
        OBErrorResponse1 stepError5 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);

        Assertions.assertTrue(stepError5.getCode().contains(MAX_OTP), "Error message was not as " +
                "expected, test expected : " + MAX_OTP);

        WHEN("They try and create a beneficiary again a 403 is returned");
        uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 obErrorResponse = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse.getCode(), STEP_UP_REQUIRED);

        OBErrorResponse1 stepError6 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);

        AND("Their account is still locked with a 423 response");
        //the format of these errors will change
        Assertions.assertTrue(stepError6.getCode().contains(MAX_OTP), "Error message was not as " +
                "expected, test expected : " + MAX_OTP);

        DONE();
    }


    @Test
    public void negative_case_user_tries_to_step_up_wrong_password_biometrics_adding_bene_then_payment() {
        TEST("AHBDB-370 - User completing biometrics step up uses wrong password 3 times and is then locked out");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an valid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        THEN("The client tries to create their beneficiary");

        uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");

        AND("The user fails to complete step up auth correctly more than 3 times with a wrong password");

        String password = RandomDataGenerator.generateRandomSHA512enabledPassword();

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(password).scope("accounts").weight(loginMinWeightExpectedBio).build();

        WHEN("They try to step up incorrectly first attempt");
        OBErrorResponse1 stepError1 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError1.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly second attempt");
        OBErrorResponse1 stepError2 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError2.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        THEN("They correctly step up on the third attempt");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequestValid = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequestValid);

        THEN("They can created their beneficiary");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);

        WHEN("The user wants to make a large payment above the step up limit");
        ////////////////////////////

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        int testTransferAmount = Integer.parseInt(maxUnauthLimit) + 10;

        AND("I transfer the test money to the created user's account");

        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(), alphaTestUser.getAccountNumber(),
                BigDecimal.valueOf(testTransferAmount));
        assertNotNull(response);

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertEquals(Integer.parseInt(interimAvailable), testTransferAmount);
        Assertions.assertEquals(Integer.parseInt(interimBooked), testTransferAmount);

        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount);

        THEN("I submit the valid payment consent request without step up auth and a 403 is returned");
        final uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);
        Assertions.assertTrue(consentResponse.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They incorrectly step up again");
        OBErrorResponse1 stepError3 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError3.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        //this is returning a 423
        WHEN("They incorrectly step up again");
        OBErrorResponse1 stepError4 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
        AND("Their account is still locked with a 423 response");
        //the format of these errors will change
        Assertions.assertTrue(stepError4.getCode().contains(MAX_OTP), "Error message was not as " +
                "expected, test expected : " + MAX_OTP);

        DONE();

    }
}
