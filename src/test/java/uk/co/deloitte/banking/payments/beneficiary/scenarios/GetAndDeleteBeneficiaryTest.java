package uk.co.deloitte.banking.payments.beneficiary.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetAndDeleteBeneficiaryTest {

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";
    private static final String DOES_NOT_BELONG_MESSAGE = "does not belong to User";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;


    private AlphaTestUser alphaTestUser;
    private AlphaTestUser secondAlphaTestUser;

    private void setupTestUser() {
        if (this.alphaTestUser == null) {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupV2UserAndV2Customer(alphaTestUser, null);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
    }

    private void setupTestUser2() {
        if (this.secondAlphaTestUser == null) {
            this.secondAlphaTestUser = new AlphaTestUser();
            secondAlphaTestUser = this.alphaTestUserFactory.setupCustomer(this.secondAlphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.secondAlphaTestUser);
        }
    }

    private void beneStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private void beneStepUpAuthBiometricsForSecondUser() {
        authenticateApi.stepUpUserAuthInitiate(secondAlphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(secondAlphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(secondAlphaTestUser, stepUpAuthValidationRequest);
    }

    private void beneStepUpAuthOTP() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(isNotBlank(otpCO.getPassword()));
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private OBBeneficiary5 obBeneficiary5() {
        beneStepUpAuthBiometrics();
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        return this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    public void positive_case_beneficiary_get() {
        TEST("AHBDB-350/351 - get beneficiary by id");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a valid beneficiary set up");

        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUser, obBeneficiary5.getBeneficiaryId()).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(obBeneficiary5, GetObBeneficiary5);

        DONE();
    }

    @Test
    public void negative_case_user1_tries_to_get_user2_beneficiaries() {
        TEST("AHBDB-13469 access control");
        setupTestUser();
        setupTestUser2();

        GIVEN("A customer has a beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName("AuthTest");

        beneStepUpAuthBiometricsForSecondUser();
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(secondAlphaTestUser,
                beneficiaryData);
        Assertions.assertEquals("AuthTest", beneOne.getData().getBeneficiary().get(0).getCreditorAccount().getName());

        WHEN("Another customer tries to retrieve that customer's beneficiaries");
        OBErrorResponse1 error = this.beneficiaryApiFlows.getBeneficiaryError(
                alphaTestUser, beneOne.getData().getBeneficiary().get(0).getBeneficiaryId(), 400);

        THEN("The API returns a 400 Bad Request");
        Assertions.assertTrue(error.getMessage().contains(DOES_NOT_BELONG_MESSAGE));

        DONE();
    }

    @Test
    public void positive_case_beneficiary_delete() {
        TEST("AHBDB-350/351 - delete beneficiary by id");


        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a valid beneficiary set up");
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        THEN("The beneficiary can be deleted with a 204 response");
        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, obBeneficiary5.getBeneficiaryId(), 204);

        DONE();
    }

    @Test
    public void negative_case_beneficiary_delete_not_found() {
        TEST("AHBDB-350/351 - get beneficiary by id delete beneficiary by id not found response");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a valid beneficiary set up");
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        THEN("The beneficiary can be deleted with a 204 response");
        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, obBeneficiary5.getBeneficiaryId(), 204);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        THEN("The beneficiary can't be found to be deleted with a 404 response");
        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, obBeneficiary5.getBeneficiaryId(), 404);

        DONE();
    }

    @Test
    public void negative_case_beneficiary_get_not_found() {
        TEST("AHBDB-350/351 - get beneficiary by id delete beneficiary by id not found response");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a valid beneficiary set up");
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        AND("I have a completed step up auth");
        beneStepUpAuthBiometrics();

        THEN("The beneficiary can be deleted with a 204 response");
        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, obBeneficiary5.getBeneficiaryId(), 204);

        THEN("I can get the beneficiary by id with a 404 response");
        AND("I have a completed step up auth");
        beneStepUpAuthBiometrics();

        OBErrorResponse1 response = this.beneficiaryApiFlows.getBeneficiaryError(alphaTestUser, obBeneficiary5.getBeneficiaryId(), 404);
        Assertions.assertTrue(response.getCode().contains("UAE.ERROR.NOT_FOUND"));

        DONE();
    }
}
