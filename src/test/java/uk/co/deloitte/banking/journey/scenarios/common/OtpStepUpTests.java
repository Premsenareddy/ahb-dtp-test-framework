package uk.co.deloitte.banking.journey.scenarios.common;

import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUsers;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;

import java.util.List;
import java.util.Objects;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OtpStepUpTests extends AdultOnBoardingBase {

    public static final String STEP_UP_TESTS = "stepup";
    public static final String SMOKE_TEST = "smoke";

    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(1)
    void marketplace_customer_setup_success_test() {
        this.marketplace_customer_setup_success(false);
    }

    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(2)
    void reauthenticate_test() {
        this.reauthenticate(ACCOUNT_SCOPE);
    }

    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(3)
    void generate_customer_cif_test() {
        this.generate_customer_cif();
    }

    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(4)
    void create_account_test() {
        this.create_account();
    }

    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(5)
    void create_current_account_test() {
        this.create_current_account();
    }


    /**
     * REQUESTED WEIGHT = 32
     * Stepup auth is platform wide singleton state maintained against user irrespective of the multiple scenarios for a given weight.
     * Here are series of scenario tests when spanned multiple scenarios.
     */

    /**
     * OTP Test 1 : When stepup auth is satisfied wile adding beneficiary to a banking customer
     * OTP = correct
     * <p>
     * OLD STATE
     * FAILED_COUNT = 0
     * <p>
     * NEW STATE
     * FAILED_COUNT = 0
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(6)
    void user_create_beneficiary_otp_test_success() throws InterruptedException {
        TEST("Test stepup auth for create beneficiary");
        WHEN("User is doing stepup flow for the first time");
        AND("User have provided the correct OTP");
        user_crud_beneficiary_otp_test(true, false);
    }


    /**
     * OTP Test 2 ( FAILED COUNT = 1 ): When stepup auth is failed wile adding beneficiary to a banking customer
     * OTP = IN-correct
     * <p>
     * OLD STATE
     * FAILED_COUNT = 0
     * <p>
     * NEW STATE
     * FAILED_COUNT = 1
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(7)
    void user_create_beneficiary_otp_test_failed_retry_count_is_zero() throws InterruptedException {
        TEST("Test stepup auth for create beneficiary");
        WHEN("User is doing stepup flow for the first time");
        AND("User have provided the correct OTP");
        user_crud_beneficiary_otp_test(false, false);
    }

    /**
     * OTP Test 3 ( FAILED COUNT = 2 ): When stepup auth is failed wile adding beneficiary to a banking customer
     * OTP = IN-correct
     * <p>
     * OLD STATE
     * FAILED_COUNT = 1
     * <p>
     * NEW STATE
     * FAILED_COUNT = 2
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(8)
    void user_create_beneficiary_otp_test_failed_retry_count_is_one() throws InterruptedException {
        TEST("Test stepup auth for create beneficiary");
        WHEN("User is doing stepup flow for the first time");
        AND("User have provided the correct OTP");
        user_crud_beneficiary_otp_test(false, false);
    }

    /**
     * OTP Test 4 ( FAILED COUNT = 2 ): When stepup auth is satisfied wile adding beneficiary to a banking customer
     * OTP = correct
     * <p>
     * OLD STATE
     * FAILED_COUNT = 2
     * <p>
     * NEW STATE
     * FAILED_COUNT = 2
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(9)
    void user_create_beneficiary_otp_test_success_retry_count_is_two() throws InterruptedException {
        TEST("Test stepup auth for create beneficiary");
        WHEN("User is doing stepup flow for the first time");
        AND("User have provided the correct OTP");
        user_crud_beneficiary_otp_test(true, false);
    }

    /**
     * OTP Test 5 ( FAILED COUNT = 3 ): When stepup auth is failed wile adding beneficiary to a banking customer
     * OTP = IN-correct
     * <p>
     * OLD STATE
     * FAILED_COUNT = 2
     * <p>
     * NEW STATE
     * FAILED_COUNT = 3
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(10)
    void user_create_beneficiary_otp_test_failed_retry_count_is_two() throws InterruptedException {
        TEST("Test stepup auth for create beneficiary");
        WHEN("User is doing stepup flow for the first time");
        AND("User have provided the correct OTP");
        user_crud_beneficiary_otp_test(false, false);
    }


    /**
     * Here max retries are exhaused for banking customer
     * OTP Test 6 ( FAILED COUNT = 3 ): When stepup auth is failed wile adding beneficiary to a banking customer when correct OTP provided
     * OTP = correct
     * <p>
     * OLD STATE
     * FAILED_COUNT = 3
     * <p>
     * NEW STATE
     * FAILED_COUNT = 4
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(11)
    void user_create_beneficiary_otp_test_failed_retry_count_is_three() throws InterruptedException {
        TEST("Test stepup auth for create beneficiary");
        WHEN("User is doing stepup flow for the first time");
        AND("User have provided the correct OTP");
        user_crud_beneficiary_otp_test(false, true);
    }


    /**
     * Here max retries are exhaused for banking customer
     * OTP Test 7 ( FAILED COUNT = 5 ): When stepup auth is failed wile executing payment to banking customer because of OTP retry count exhausted
     * OTP = correct
     * <p>
     * OLD STATE
     * FAILED_COUNT = 4
     * <p>
     * NEW STATE
     * FAILED_COUNT = 5
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(12)
    void user_executing_update_beneficiary_post_retry_exhaust() {
        GIVEN("User has exhausted with stepup retry count");
        WHEN("Update beneficiary request fired");
        OBBeneficiary5 obBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaries(alphaTestUser).getData().getBeneficiary().get(0);
        obBeneficiary5.setReference("Test Ref");
        Response response = this.beneficiaryApiFlows.updateBeneficiaryResponse(alphaTestUser,
                obBeneficiary5);
        WHEN("Then should return 403 ");
        Assertions.assertTrue(response.getStatusCode() == 403);
        final int otpWeightRequested = 32;
        //User triggers OTP flow
        //1) -> initiate step up authentication
        WHEN("Stepup auth request was initiated");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser,
                StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        THEN("User will receive OTP");
        WHEN("Valid OTP entered");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(isNotBlank(otpCO.getPassword()));
        AND("Stepup auth validation will be triggered");
        final StepUpAuthRequest stepUpAuthValidationRequest =
                StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();

        THEN("Should fail with 423 error");
        authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, 423);
    }

    /**
     * REQUESTED WEIGHT = 31
     * Stepup auth is platform wide singleton state maintained against user irrespective of the multiple scenarios for a given weight.
     *
     * User has exhausted stepup for weight and user should be able to login to the platform.
     */
    @Tag(STEP_UP_TESTS)
    @Tag(SMOKE_TEST)
    @Test
    @Order(13)
    void reauthenticate_test_after_otp_retries_exhaust() {
        this.reauthenticate(ACCOUNT_SCOPE);
    }


    @Test
    @Order(99999)
    void dump() {
        AlphaTestUsers.builder().alphaTestUsers(List.of(this.alphaTestUser))
                .build()
                .writeToFile();
        DONE();
    }

    private void user_crud_beneficiary_otp_test(final boolean validateWithCorrectOtp, final boolean isRetryLimitReached) throws InterruptedException {
        GIVEN("User has Account scope and a Valid Account");
        final int otpWeightRequested = 32;
        WHEN("the user calls created Beneficiary");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        Response response =
                this.beneficiaryApiFlows.createBeneficiaryFlexResponse(alphaTestUser
                        , beneficiaryData);

        THEN("Should fail with FORBIDDEN");
        response.then().log().all().statusCode(HttpStatus.FORBIDDEN.getCode());
        final OBErrorResponse1 errorResponse = response.as(OBErrorResponse1.class);
        Assertions.assertTrue(Objects.nonNull(errorResponse));
        THEN("Should return error code: " + errorResponse.getCode());
        //User triggers OTP flow
        //1) -> initiate step up authentication
        WHEN("Stepup auth request was initiated");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser,
                StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        THEN("User will receive OTP");
        String otpValue;
        if (validateWithCorrectOtp) {
            //2) -> extract otp from dev simulator
            //extract otp from device simulator
            WHEN("Valid OTP entered");
            OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
            assertTrue(isNotBlank(otpCO.getPassword()));
            otpValue = otpCO.getPassword();
            AND("Stepup auth validation will be triggered");
            final StepUpAuthRequest stepUpAuthValidationRequest =
                    StepUpAuthRequest.builder().otp(otpValue).scope("accounts").weight(otpWeightRequested).build();
            authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
            THEN("Biometrics validation should success");
            AND("Cache entry was successful");

            GIVEN("Cache entry was successful for userId");
            THEN("Creating beneficiary should be successful");
            OBWriteBeneficiaryResponse1 beneficiary =
                    this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser
                            , beneficiaryData);
            assertNotNull(beneficiary);
            Assertions.assertEquals(1, beneficiary.getData().getBeneficiary().size());
            assertNotNull(beneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());
        } else {
            WHEN("Invalid OTP provided then");
            otpValue = "invalid-otp";
            final StepUpAuthRequest stepUpAuthValidationRequest =
                    StepUpAuthRequest.builder().otp(otpValue).scope("accounts").weight(otpWeightRequested).build();
            THEN("Should fail with FORBIDDEN error");
            authenticateApi.validateUserStepUpAuthError(alphaTestUser, stepUpAuthValidationRequest, isRetryLimitReached ? 423 : 403);
        }
        DONE();
    }
}
