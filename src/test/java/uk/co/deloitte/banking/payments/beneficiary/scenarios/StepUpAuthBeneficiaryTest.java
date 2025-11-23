package uk.co.deloitte.banking.payments.beneficiary.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StepUpAuthBeneficiaryTest {

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


    private AlphaTestUser alphaTestUser;

    private static final String MAX_OTP = "UAE.OTP.LIMIT_REACHED";

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;


    private void setupTestUser() {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

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

        WHEN("They try to step up incorrectly third attempt");
        OBErrorResponse1 stepError3 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError3.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
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
    public void negative_case_user_tries_to_step_up_wrong_OTP() {
        TEST("AHBDB-370 - User completing biometrics step up uses wrong OTP 3 times and is then locked out");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I want to create a valid beneficiary");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        THEN("The client tries to create their beneficiary");

        uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");

        AND("The user fails to complete step up auth correctly more than 3 times with a wrong otp code");

        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        otpCO.setPassword(RandomDataGenerator.generateRandomNumeric(6));
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();

        WHEN("They try to step up incorrectly first attempt");
        OBErrorResponse1 stepError1 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError1.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly second attempt");
        OBErrorResponse1 stepError2 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError2.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        WHEN("They try to step up incorrectly third attempt");
        OBErrorResponse1 stepError3 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 403);
        Assertions.assertTrue(stepError3.getCode().contains(STEP_UP_REQUIRED), "Error message was not as " +
                "expected, test expected : " + STEP_UP_REQUIRED);

        THEN("Their account is locked with a 423 response");
        OBErrorResponse1 stepError5 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);

        //the format of these errors will change
        Assertions.assertTrue(stepError5.getCode().contains(MAX_OTP), "Error message was not as " +
                "expected, test expected : " + MAX_OTP);

        WHEN("They try and create a beneficiary again a 403 is returned");
        uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 obErrorResponse = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse.getCode(), STEP_UP_REQUIRED);

        AND("Their account is still locked with a 423 response");
        OBErrorResponse1 stepError6 = authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
        //the format of these errors will change
        Assertions.assertTrue(stepError6.getCode().contains(MAX_OTP), "Error message was not as " +
                "expected, test expected : " + MAX_OTP);

        DONE();
    }

}
