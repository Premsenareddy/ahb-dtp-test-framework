package uk.co.deloitte.banking.payments.beneficiary.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.co.deloitte.banking.payments.beneficiary.BeneficiaryConfig;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomSwift;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdateBeneficiaryTest {

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject BeneficiaryConfig beneficiaryConfig;


    private AlphaTestUser alphaTestUser;

    private static final String ZERO_FIFTY_ERROR = "size must be between 0 and 50";

    private static final String BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";

    private static final String ZERO_ELEVEN_ERROR = "size must be between 0 and 11";

    private static final String NULL_FIELD_ERROR = "must not be null";

    private static final String MUST_BE_THIRTEEN_ERROR = "Validation failed for classes";

    private static final String NO_SPECIAL_CHARS_ERROR = "must not contain special characters";

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;


    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
    }

    private void beneStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
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

    @ParameterizedTest
    @ValueSource(strings = {"احمد سالماحمد سالماحمداحمد لم سالماحمداحم سالمسلم", "ddddderfdertgfdkjhgftyuiolkmnbvftyqwertyuioplkjhg","S&pecia!L", "t", "ا"})
    public void positive_case_beneficiary_update_valid_nickname(String validNickName) {
        TEST("AHBDB-1219 - beneficiary with a valid name length is created with a 201 response");
        TEST("positive_case_beneficiary_update_valid_nickname" + validNickName);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a valid beneficiary set up");

        WHEN("I update the beneficiary with a valid nickname");
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();
        obBeneficiary5.getSupplementaryData().setNickname(validNickName);
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The beneficiary is successfully updated");
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(alphaTestUser, obBeneficiary5);
        Assertions.assertEquals(updatedBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getNickname(), validNickName);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB94BARC10201530093459"})
    public void positive_case_beneficiary_update_account_or_iban(String validAccountOrIban) {
        TEST("AHBDB-1219 - beneficiary with a valid account or iban is created with a 201 response");
        TEST("positive_case_beneficiary_update_account_or_iban" + validAccountOrIban);


        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();

        WHEN("I update the beneficiary IBAN with domestic_flag");
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();
        obBeneficiary5.getCreditorAccount().setIdentification(validAccountOrIban);
        obBeneficiary5.getCreditorAgent().setName(beneficiaryConfig.getDomesticFlag());

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(alphaTestUser, obBeneficiary5);

        THEN("The beneficiary is successfully updated");
        Assertions.assertEquals(updatedBeneficiary.getData().getBeneficiary().get(0).getCreditorAccount().getIdentification(), validAccountOrIban);
        DONE();
    }

    @Test
    public void positive_case_beneficiary_update_mobile() {
        TEST("AHBDB-1219 - beneficiary with a valid account or iban is created with a 201 response");
        TEST("positive_case_beneficiary_update_mobile");

        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();

        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        String newMobile = generateRandomMobile();
        WHEN("I update the beneficiary with a valid mobile number " + newMobile);
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        obBeneficiary5.getSupplementaryData().setMobileNumber(newMobile);
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(alphaTestUser, obBeneficiary5);

        THEN("The beneficiary is successfully updated");
        Assertions.assertEquals(updatedBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getMobileNumber(), newMobile);
        DONE();
    }

    @Test
    public void positive_case_beneficiary_update_swift() {
        TEST("AHBDB-1219 - beneficiary with a valid account or iban is created with a 201 response");
        TEST("positive_case_beneficiary_update_swift");

        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        String swift = generateRandomSwift();
        WHEN("I update the beneficiary with a valid SWIFT : " + swift);

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        obBeneficiary5.getCreditorAgent().setIdentification(swift);
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(alphaTestUser, obBeneficiary5);

        THEN("The beneficiary is successfully updated");
        Assertions.assertEquals(updatedBeneficiary.getData().getBeneficiary().get(0).getCreditorAgent().getIdentification(), swift);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"احمد سالماحمد سالماحمداحمد سالم سالماحمداحم سالمسلم",
            "ddddderfdertgfdkjhgftyuiolkmnbvftyqwertyuioplkjhgfd", "                                                 " +
            "  "})
    public void negative_case_beneficiary_invalid_update_nickname(String invalidNickNameLength) {
        TEST("AHBDB-1219 - beneficiary with a valid nickname length is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_update_nickname " + invalidNickNameLength);


        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with an invalid nickname a 400 response is returned");
        obBeneficiary5.getSupplementaryData().setNickname(invalidNickNameLength);
        OBErrorResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 400);
        Assertions.assertTrue(updatedBeneficiary.getMessage().contains(ZERO_FIFTY_ERROR), "Error message was not as expected, test expected : " + ZERO_FIFTY_ERROR);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"AE07 0331 2345 678956", "AE07 %$^ 2345 6789", "AE07 0331 2345 احمد سا"})
    public void negative_case_beneficiary_invalid_update_iban_accountNumber(String invalidIbanOrAccount) {
        TEST("AHBDB-1219 - beneficiary with an invalid iban or account length is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_update_iban_accountNumber " + invalidIbanOrAccount);

        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        WHEN("I update the beneficiary with an invalid account number or iban and domestic_bank a 404 response is returned");
        obBeneficiary5.getCreditorAgent().setName(beneficiaryConfig.getDomesticFlag());
        obBeneficiary5.getCreditorAccount().setIdentification(invalidIbanOrAccount);
        OBErrorResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 400);
        Assertions.assertTrue(updatedBeneficiary.getMessage().contains(NO_SPECIAL_CHARS_ERROR), "Error message was not as expected, test expected : " + NO_SPECIAL_CHARS_ERROR);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+55550", "+555", "+5555412345671", "+55554123456713"})
    public void negative_case_beneficiary_invalid_update_mobileNumber(String invalidMobile) {
        TEST("AHBDB-1219 - beneficiary with an invalid iban or account length is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_update_mobileNumber" + invalidMobile);

        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with an invalid account number or iban a 400 response is returned");
        obBeneficiary5.getSupplementaryData().setMobileNumber(invalidMobile);
        OBErrorResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 400);
        Assertions.assertTrue(updatedBeneficiary.getMessage().contains(MUST_BE_THIRTEEN_ERROR), "Error message was not as expected, test expected : " + MUST_BE_THIRTEEN_ERROR);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HLALAEAA0011", "HLALAEAA00112233"})
    public void negative_case_beneficiary_invalid_update_swift(String invalidSwift) {
        TEST("AHBDB-1219 - beneficiary with an invalid iban or account length is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_update_swift " + invalidSwift);

        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with an invalid account number or iban a 400 response is returned");
        obBeneficiary5.getCreditorAgent().setIdentification(invalidSwift);
        OBErrorResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 400);

        Assertions.assertTrue(updatedBeneficiary.getMessage().contains(ZERO_ELEVEN_ERROR), "Error message was not as expected, test expected : " + ZERO_ELEVEN_ERROR);

        DONE();
    }

    @Test
    public void negative_case_beneficiary_invalid_name() {
        TEST("AHBDB-1219 - beneficiary with null name is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_null_name");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        AND("I have a valid beneficiary set up");
        String beneName = obBeneficiary5.getCreditorAccount().getName();

        WHEN("I update a beneficiary with an null name");
        obBeneficiary5.getCreditorAccount().setName("Test Name");

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 400);

        Assertions.assertTrue(updatedBeneficiary.getCode().contains(BAD_REQUEST));
        DONE();
    }


    @Test
    public void negative_case_beneficiary_invalid_null_name() {
        TEST("AHBDB-1219 - beneficiary with null name is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_null_name");

        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        String beneName = obBeneficiary5.getCreditorAccount().getName();
        WHEN("I update a beneficiary with an null name");
        obBeneficiary5.getCreditorAccount().setName(null);

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 400);
            Assertions.assertTrue(updatedBeneficiary.getCode().contains(BAD_REQUEST));
        DONE();
    }

    @Test
    public void negative_case_beneficiary_invalid_null_accountNumber() {
        TEST("AHBDB-1219 - beneficiary with null account number is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_null_accountNumber");


        GIVEN("I have a valid access token and account scope");
        AND("I have a valid beneficiary set up");
        setupTestUser();
        OBBeneficiary5 obBeneficiary5 = obBeneficiary5();


        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        WHEN("I update a beneficiary with an null accountNumber");
        obBeneficiary5.getCreditorAccount().setIdentification(null);

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryError(alphaTestUser, obBeneficiary5, 400);
        Assertions.assertTrue(updatedBeneficiary.getMessage().contains(NULL_FIELD_ERROR), "Error message was not as expected, test expected : " + NULL_FIELD_ERROR);
        DONE();
    }

}
