package uk.co.deloitte.banking.payments.beneficiary.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.BeneficiaryConfig;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateBeneficiaryTest {

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
    private BeneficiaryConfig beneficiaryConfig;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private static final Environments ALL_SKIP_ENV = Environments.NONE;

    private AlphaTestUser alphaTestUser;

    private static final String ZERO_FIFTY_ERROR = "size must be between 0 and 50";

    private static final String NO_SPECIAL_CHARS_ERROR = "must not contain special characters";

    private static final String CREDITOR_NAME_SPEC_CHARS = "must not be null, contain special characters or numbers";

    private static final String ZERO_ELEVEN_ERROR = "size must be between 0 and 11";

    private static final String NULL_FIELD_ERROR = "must not be null";

    private static final String MUST_BE_THIRTEEN_ERROR = "size must be between 13 and 13";

    private static final String ACCOUNT_NOT_FOUND = "UAE.ERROR.NOT_FOUND";

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";

    private static final String CANNOT_BE_OWN_ACCOUNT = "Own account cannot be added as beneficiary";

    private static final int loginMinWeightExpectedBio = 31;

    private static final int otpWeightRequested = 32;


    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
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

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @ParameterizedTest
    @ValueSource(strings = {"احمد سالماحمد سالماحمداحمد لم سالماحمداحم سالمسلم",
            "positive test case valid name of fifty chars benef", "t", "ا"})
    public void positive_case_beneficiary_difference_valid_names(String validName) {

        TEST("AHBDB-305 - beneficiary with a valid name length is created with a 201 response");
        TEST("positive_case_beneficiary_difference_valid_names " + validName);


        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an valid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName(validName);

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("They can created their beneficiary");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);
        Assertions.assertEquals(beneOne.getData().getBeneficiary().get(0).getCreditorAccount().getName(), validName);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"احمد سالماحمد سالماحمداحمد لم سالماحمداحم سالمسلم",
            "long nickname of thirty charac", "t", "ا"})
    public void positive_case_beneficiary_valid_nicknames(String validNickname) {

        TEST("AHBDB-305 - beneficiary with a valid nickname is created with a 201 response");
        TEST("positive_case_beneficiary_valid_nicknames " + validNickname);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an valid nickname");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setNickName(validNickname);

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);
        Assertions.assertEquals(beneOne.getData().getBeneficiary().get(0).getSupplementaryData().getNickname(), validNickname);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"domestic_bank"})
    public void positive_case_beneficiary_difference_types(String validType) {
        TEST("AHBDB-305 - beneficiary with a valid name length is created with a 201 response");
        TEST("positive_case_beneficiary_difference_types " + validType);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an valid type");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryType(validType);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);
        Assertions.assertEquals(beneOne.getData().getBeneficiary().get(0).getCreditorAgent().getName(), validType);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB94BARC10201530093459", "LC14BOSL123456789012345678901234", "NO8330001234567"})
    public void positive_case_beneficiary_different_iban(String validAccountOrIban) {
        TEST("AHBDB-305 - beneficiary with a valid account or iban is created with a 201 response");
        TEST("positive_case_beneficiary_difference_account_or_iban " + validAccountOrIban);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an valid type and IBAN");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(validAccountOrIban);
        beneficiaryData.setBeneficiaryType(beneficiaryConfig.getDomesticFlag());

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);
        Assertions.assertEquals(beneOne.getData().getBeneficiary().get(0).getCreditorAccount().getIdentification(),
                validAccountOrIban);
        DONE();
    }

    @Test
    public void positive_case_beneficiary_different_valid_DTP_account_number() {
        TEST("AHBDB-242 - beneficiary with a valid account or iban is created with a 201 response");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an valid type and account number");
        String beneAccountNumber = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 10, "0");

        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(beneAccountNumber);

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);
        Assertions.assertEquals(beneOne.getData().getBeneficiary().get(0).getCreditorAccount().getIdentification(),
                beneAccountNumber);
        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"احمد سالماحمد سالماحمداحمد سالم سالماحمداحم سالمسلم",
            "ddddderfdertgfdkjhgftyuiolkmnbvftyqwertyuioplkjhgfd", "                                                 " +
            "  "})
    public void negative_case_beneficiary_invalid_name(String invalidNameLength) {
        TEST("AHBDB-305 - beneficiary with invalid name length is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_name " + invalidNameLength);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName(invalidNameLength);

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(ZERO_FIFTY_ERROR), "Error message was not as expected, " +
                "test expected : " + ZERO_FIFTY_ERROR);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678908765432", "OI*&^TRFVBNKO(*&^YTFGBN", "d!@£$%^&*()"})
    public void negative_case_beneficiary_invalid_name_characters(String invalidNameCharacters) {
        TEST("AHBDB-12586 test defect");
        TEST("AHBDB-305 - beneficiary with invalid name characters is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_name_characters " + invalidNameCharacters);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName(invalidNameCharacters);


        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(CREDITOR_NAME_SPEC_CHARS), "Error message was not as " +
                "expected, test expected : " + CREDITOR_NAME_SPEC_CHARS);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"احمد سالماحمد سالماحمداحمد سالم سالماحمداحم سالمسلم",
            "ddddderfdertgfdkjhgftyuiolkmnbvftyqwertyuioplkjhgfd", "                                                 " +
            "  "})
    public void negative_case_beneficiary_invalid_nickname_length(String invalidNickNameLength) {
        TEST("AHBDB-305 - beneficiary with invalid nickname length is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_nickname_length " + invalidNickNameLength);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setNickName(invalidNickNameLength);
        THEN("The client tries to create their beneficiary");

        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(ZERO_FIFTY_ERROR), "Error message was not as expected, " +
                "test expected : " + ZERO_FIFTY_ERROR);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11234567654343", "(*&^%$%^&*", "*&^TFVBNKO(*&^T"})
    public void positive_case_beneficiary_nick_name_allowed_specialCharacters(String validNickNameCharacters) {
        TEST("AHBDB-305 - beneficiary with invalid nickname characters is create with a 201 response");
        TEST("negative_case_beneficiary_invalid_nickname_characters " + validNickNameCharacters);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setNickName(validNickNameCharacters);

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("They can created their beneficiary");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);
        Assertions.assertEquals(beneOne.getData().getBeneficiary().get(0).getSupplementaryData().getNickname(), validNickNameCharacters);

        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"GB94BARC20201530093459", "GB24BARC20201630093459"})
    public void negative_case_beneficiary_invalid_IBAN_number(String invalidIban) {
        envUtils.ignoreTestInEnv("NFT IBAN stubbed", Environments.NFT);
        TEST("AHBDB-305 - beneficiary with invalid IBAN number is rejected with a 404 response");
        TEST("negative_case_beneficiary_invalid_IBAN_number " + invalidIban);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid iban");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(invalidIban);
        beneficiaryData.setBeneficiaryType(beneficiaryConfig.getDomesticFlag());

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 404 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 404);
        Assertions.assertTrue(response.getCode().contains(ACCOUNT_NOT_FOUND), "Error message was not as expected," +
                " test expected : " + ACCOUNT_NOT_FOUND);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"AE07 0331 2345 678956", "AE07 %$^ 2345 6789", "AE07 0331 2345 احمد سا"})
    public void negative_case_beneficiary_invalid_IBAN_number_characters(String invalidIban) {
        TEST("AHBDB-305 - beneficiary with invalid IBAN number is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_IBAN_number " + invalidIban);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid iban");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(invalidIban);
        beneficiaryData.setBeneficiaryType(beneficiaryConfig.getDomesticFlag());

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(NO_SPECIAL_CHARS_ERROR), "Error message was not as expected," +
                " test expected : " + NO_SPECIAL_CHARS_ERROR);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HLALAEAA0011", "HLALAEAA00112233"})
    public void negative_case_beneficiary_invalid_SWIFT_number(String invalidSwift) {
        TEST("AHBDB-305 - beneficiary with invalid SWIFT number is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_SWIFT_number " + invalidSwift);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid swift");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(invalidSwift);

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(ZERO_ELEVEN_ERROR), "Error message was not as expected, " +
                "test expected : " + ZERO_ELEVEN_ERROR);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+55550", "+555", "+5555412345671", "+55554123456713"})
    public void negative_case_beneficiary_invalid_mobile_number_length(String invalidMobileNumber) {
        TEST("AHBDB-305 - beneficiary with invalid mobile number length is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_mobile_number_length " + invalidMobileNumber);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an invalid number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setMobileNumber(invalidMobileNumber);

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(MUST_BE_THIRTEEN_ERROR), "Error message was not as " +
                "expected, test expected : " + MUST_BE_THIRTEEN_ERROR);

        DONE();
    }

    @Test
    public void negative_case_beneficiary_invalid_null_name() {
        TEST("AHBDB-305 - beneficiary with null name is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_null_name");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an null name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName(null);


        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(NULL_FIELD_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_FIELD_ERROR);

        DONE();
    }

    @Test
    public void negative_case_beneficiary_invalid_null_type() {
        TEST("AHBDB-305 - beneficiary with null type is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_null_type");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an null type");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryType(null);

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(NULL_FIELD_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_FIELD_ERROR);
        DONE();
    }

    @Test
    public void negative_case_beneficiary_invalid_null_accountNumber() {
        TEST("AHBDB-305 - beneficiary with null account number is rejected with a 400 response");
        TEST("negative_case_beneficiary_invalid_null_accountNumber");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with an null accountNumber");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(null);

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(NULL_FIELD_ERROR), "Error message was not as expected, " +
                "test expected : " + NULL_FIELD_ERROR);
        DONE();
    }

    @Test
    public void negative_case_beneficiary_invalid_accountNumber_users_own() {
        TEST("AHBDB-12884 - test defect - user cannot user own account number as bene account number");
        TEST("AHBDB-7679 - test defect - user cannot user own account number as bene account number");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with own account accountNumber");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(alphaTestUser.getAccountNumber());

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains(CANNOT_BE_OWN_ACCOUNT), "Error message was not as expected, " +
                "test expected : " + CANNOT_BE_OWN_ACCOUNT);
        DONE();
    }

    @Test
    public void negative_case_beneficiary_not_found_accountNumber() {
        TEST("AHBDB-242 - beneficiary with not found account number is rejected with a 404 response");
        TEST("negative_case_beneficiary_not_found_accountNumber");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a beneficiary with a not found accountNumber");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber("1234567890");

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 404 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser, beneficiaryData, 404);
        Assertions.assertTrue(response.getCode().contains(ACCOUNT_NOT_FOUND), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_FOUND);
        DONE();
    }

}
