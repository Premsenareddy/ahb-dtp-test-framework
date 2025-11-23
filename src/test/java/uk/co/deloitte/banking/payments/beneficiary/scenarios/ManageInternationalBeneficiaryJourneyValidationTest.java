package uk.co.deloitte.banking.payments.beneficiary.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ManageInternationalBeneficiaryJourneyValidationTest {

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


    private AlphaTestUser alphaTestUser;

    private static final int loginMinWeightExpected = 31;

    private static final String INTERNATIONAL_BENEFICIARY_TYPE = "other_bank";
    private static final String SWIFT_CODE = "EBILAEAD";

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);
        }
    }

    private void beneStepUpAuthOTP() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    @Order(1)
    public void positive_case_create_valid_international_beneficiary_and_get_beneficiary() {
        TEST("AHBDB-7655 : create international beneficiary and verify creation by getting same beneficiary");
        envUtils.ignoreTestInEnv("Feature not deployed yet on NFT, STG", Environments.NFT, Environments.STG);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);

        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUser, createdObBeneficiary5.getBeneficiaryId()).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, GetObBeneficiary5);

        DONE();
    }

    @Test
    public void negative_case_create_international_beneficiary_null_address_fields() {
        TEST("AHBDB-7655 - create international beneficiary with null address fields is rejected with a 400 response");
        TEST("negative_case_create_international_beneficiary_invalid_null_address_fields");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I create a beneficiary with null values for mandatory address fields");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setAddressLine1(null);
        beneficiaryData.setAddressLine2("sample address");
        beneficiaryData.setAddressLine3(null);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 404 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponseInternational(alphaTestUser, beneficiaryData, 404);
        Assertions.assertEquals(obErrorResponse1.getCode(), "UAE.ERROR.NOT_FOUND");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"address line one address line 1 address line", "address line two address line 2 address line"})
    public void negative_case_create_international_beneficiary_invalid_address_length(String invalidAddressLine) {
        TEST("AHBDB-7655 - create international beneficiary with invalid address length is rejected with a 400 response");
        TEST("negative_case_create_international_beneficiary_invalid_address_length");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I create a beneficiary with invalid address fields");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setAddressLine1(invalidAddressLine);
        beneficiaryData.setAddressLine2("sample address");
        beneficiaryData.setAddressLine3(invalidAddressLine);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponseInternational(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains("address line1 can be a maximum of 35 characters long"), "Error message was not as expected, " +
                "test expected : " + "address line1 can be a maximum of 35 characters long");
        Assertions.assertTrue(response.getMessage().contains("address line3 can be a maximum of 35 characters long"), "Error message was not as expected, " +
                "test expected : " + "address line3 can be a maximum of 35 characters long");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USPD", "INRT", "UAED"})
    public void negative_case_create_international_beneficiary_invalid_currency(String invalidcurrency) {
        TEST("AHBDB-7655 - create international beneficiary with invalid currency length is rejected with a 400 response");
        TEST("negative_case_create_international_beneficiary_invalid_currency_length");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I create a beneficiary with ninvalid currency");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setCreditorAccountCurrency(invalidcurrency);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        OBErrorResponse1 response = this.beneficiaryApiFlows.createBeneErrorResponseInternational(alphaTestUser, beneficiaryData, 400);
        Assertions.assertTrue(response.getMessage().contains("creditor account currency can be a maximum of 3 characters"), "Error message was not as expected, " +
                "test expected : " + "creditor account currency can be a maximum of 3 characters");

        DONE();

    }
    @Test
    public void negative_case_create_international_beneficiary_invalid_swift() {
        TEST("AHBDB-7655 :international beneficiary with invalid swift rejcted with 400 response");
        TEST("negative_case_international_beneficiary_invalid_swift");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode("invalidswift");
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);

        THEN("The client submits the international beneficiary payload and receives a 400 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 404);
        Assertions.assertEquals(obErrorResponse1.getCode(), "UAE.ERROR.NOT_FOUND");

        DONE();
    }

    @Test
    public void negative_case_create_international_beneficiary_invalid_beneficiary_parameters() {
        TEST("AHBDB-7655 : international_beneficiary with invalid beneficiary params rejected with 400 response");
        TEST("negative_case_international_beneficiary_invalid_beneficiary_params");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setBeneficiaryType(null);

        THEN("The client submits the international beneficiary payload and receives a 400 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 400);
        Assertions.assertEquals(obErrorResponse1.getCode(), "REQUEST_VALIDATION");
        Assertions.assertTrue(obErrorResponse1.getMessage().contains("must not be null"), "Error message was not as expected, " +
                "test expected : " + "must not be null");
        DONE();
    }

    @Test
    public void negative_case_create_international_beneficiary_null_swift() {
        TEST("AHBDB-7655 : international_beneficiary with null swift rejected with 404 response");
        TEST("negative_case_international_beneficiary_null_swift");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(null);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);

        THEN("The client submits the international beneficiary payload and receives a 404 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 404);
        Assertions.assertEquals(obErrorResponse1.getCode(), "UAE.ERROR.NOT_FOUND");
        DONE();
    }


    @Test
    @Order(2)
    public void positive_case_update_valid_international_beneficiary() {
        TEST("AHBDB-7687 : update international beneficiary and verify updation by getting same beneficiary");
        envUtils.ignoreTestInEnv("Feature not deployed yet on NFT, STG", Environments.NFT, Environments.STG);

        String validSwift = "ICICINBB";

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a valid swiftcode");
        beneficiaryData.setSwiftCode(validSwift);
        OBWriteBeneficiaryResponse1 updatedIntlBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryInternational(alphaTestUser, beneficiaryData, createdObBeneficiary5.getBeneficiaryId());

        THEN("The beneficiary is successfully updated");
        Assertions.assertEquals(validSwift, updatedIntlBeneficiary.getData().getBeneficiary().get(0).getCreditorAgent().getIdentification());
        Assertions.assertEquals("ICICI BANK LIMITED", updatedIntlBeneficiary.getData().getBeneficiary().get(0).getCreditorAgent().getName());
        DONE();
    }

    @Test
    public void negative_case_update_invalid_swift_international_beneficiary() {
        TEST("AHBDB-7687 : international beneficiary update with invalid swift rejected with 404 response");
        TEST("negative_case_international_beneficiary_invalid_swift");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a invalid swiftcode");
        beneficiaryData.setSwiftCode("invalidSwift");

        THEN("The client submits the international beneficiary payload and receives a 400 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryInternationalError(alphaTestUser, beneficiaryData,
                createdObBeneficiary5.getBeneficiaryId(), 404);
        Assertions.assertEquals(obErrorResponse1.getCode(), "UAE.ERROR.NOT_FOUND");
        DONE();
    }

    @Test
    public void negative_case_update_invalid_params_international_beneficiary() {
        TEST("AHBDB-7687 : international beneficiary update with invalid beneficiary params rejected with 400 response");
        TEST("negative_case_international_beneficiary_invalid_beneficiary_params");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a valid swiftcode and null beneficiary type");
        beneficiaryData.setSwiftCode("ICICINBB");
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setBeneficiaryType(null);

        THEN("The client submits the international beneficiary payload and receives a 400 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryInternationalError(alphaTestUser, beneficiaryData,
                createdObBeneficiary5.getBeneficiaryId(), 400);
        Assertions.assertEquals(obErrorResponse1.getCode(), "REQUEST_VALIDATION");
        Assertions.assertTrue(obErrorResponse1.getMessage().contains("must not be null"), "Error message was not as expected, " +
                "test expected : " + "must not be null");
        DONE();
    }

    @Test
    public void unauthorized_case_update_international_beneficiary() {
        TEST("AHBDB-7687 : international beneficiary update with unauthorized case and rejected with 403 response");
        TEST("negative_case_international_beneficiary_unauthorized case");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        WHEN("I update the beneficiary with a valid swiftcode and null beneficiary type");
        beneficiaryData.setSwiftCode("ICICINBB");
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);

        THEN("The client submits the international beneficiary payload and receives a 403 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryInternationalError(alphaTestUser, beneficiaryData,
                createdObBeneficiary5.getBeneficiaryId(), 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), "UAE.AUTH.STEP_UP_AUTH_REQUIRED");
        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"address line one address line 1 address line", "address line two address line 2 address line"})
    public void negative_case_update_international_beneficiary_invalid_address_length(String invalidAddressLine) {
        TEST("AHBDB-7687 : update international beneficiary with invalid address length is rejected with a 400 response");
        TEST("negative_case_update_international_beneficiary_invalid_address_length");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a invalid addressline");
        beneficiaryData.setAddressLine1(invalidAddressLine);
        beneficiaryData.setAddressLine2("sample address");
        beneficiaryData.setAddressLine3(invalidAddressLine);

        THEN("The client submits the international beneficiary payload and receives a 400 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryInternationalError(alphaTestUser, beneficiaryData,
                createdObBeneficiary5.getBeneficiaryId(), 400);
        Assertions.assertTrue(obErrorResponse1.getMessage().contains("address line1 can be a maximum of 35 characters long"), "Error message was not as expected, " +
                "test expected : " + "address line1 can be a maximum of 35 characters long");
        Assertions.assertTrue(obErrorResponse1.getMessage().contains("address line3 can be a maximum of 35 characters long"), "Error message was not as expected, " +
                "test expected : " + "address line3 can be a maximum of 35 characters long");
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USPD", "INRT", "UAED"})
    public void negative_case_update_international_beneficiary_invalid_creditor_currency(String invalidCurrency) {
        TEST("AHBDB-7687 : update international beneficiary with invalid currency length is rejected with a 400 response");
        TEST("negative_case_update_international_beneficiary_invalid_currency_length");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a invalid currency");
        beneficiaryData.setCreditorAccountCurrency(invalidCurrency);

        THEN("The client submits the international beneficiary payload and receives a 400 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryInternationalError(alphaTestUser, beneficiaryData,
                createdObBeneficiary5.getBeneficiaryId(), 400);
        Assertions.assertTrue(obErrorResponse1.getMessage().contains("creditor account currency can be a maximum of 3 characters"), "Error message was not as expected, " +
                "test expected : " + "creditor account currency can be a maximum of 3 characters");
        DONE();
    }
    @Test
    public void negative_case_update_international_beneficiary_null_swift() {
        TEST("AHBDB-7687 : international beneficiary update with null swift rejected with 404 response");
        TEST("negative_case_update_international_beneficiary_null_swift");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a null swift");
        beneficiaryData.setSwiftCode(null);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);

        THEN("The client submits the international beneficiary payload and receives a 404 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryInternationalError(alphaTestUser, beneficiaryData,
                createdObBeneficiary5.getBeneficiaryId(), 404);
        Assertions.assertEquals(obErrorResponse1.getCode(), "UAE.ERROR.NOT_FOUND");
        DONE();
    }

    @Test
    public void negative_case_update_international_beneficiary_null_address_fields() {
        TEST("AHBDB-7687 - update international beneficiary with null address fields is rejected with a 400 response");
        TEST("negative_case_update_international_beneficiary_invalid_null_address_fields");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a null address fields");
        beneficiaryData.setAddressLine1(null);
        beneficiaryData.setAddressLine2("sample address");
        beneficiaryData.setAddressLine3(null);

        THEN("The client submits the beneficiary payload and receives a 404 response");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.updateBeneficiaryInternationalError(alphaTestUser, beneficiaryData, createdObBeneficiary5.getBeneficiaryId(),404);
        Assertions.assertEquals(obErrorResponse1.getCode(), "UAE.ERROR.NOT_FOUND");

        DONE();
    }

    @Test
    @Order(3)
    public void positive_case_international_beneficiary_delete() {
        TEST("AHBDB-7691 - delete beneficiary by id");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a completed step up auth");
        beneStepUpAuthOTP();
        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);
        THEN("The beneficiary can be deleted with a 204 response");
        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, createdObBeneficiary5.getBeneficiaryId(), 204);
        DONE();
    }
    @ParameterizedTest
    @Order(4)
    @ValueSource(strings = {"fkjeifj wd", "1829128491", "^&$%^&*()_","nick@123nam_()"})
    public void positive_case_create_international_beneficiary_valid_nickname(String validnickname) {
        TEST("AHBDB-12584 - create international beneficiary with Special characters and numbers for NickNames");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a completed step up auth");
        beneStepUpAuthOTP();
        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setNickName(validnickname);
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);
        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUser, createdObBeneficiary5.getBeneficiaryId()).getData().getBeneficiary().get(0);
        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, GetObBeneficiary5);
    }
    @ParameterizedTest
    @Order(5)
    @ValueSource(strings = {"fkjeifj wd", "1829128491", "^&$%^&*()_","nick@123nam_()"})
    public void positive_case_update_international_beneficiary_valid_nickname(String validnickname) {
        TEST("AHBDB-12584 - create international beneficiary with Special characters and numbers for NickNames");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a completed step up auth");
        beneStepUpAuthOTP();
        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setNickName("Test");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);
        THEN("I can get the beneficiary by id with a 200 response");
        beneficiaryData.setNickName(validnickname);
        beneStepUpAuthOTP();
        OBWriteBeneficiaryResponse1 updatedIntlBeneficiary = this.beneficiaryApiFlows.updateBeneficiaryInternational(alphaTestUser, beneficiaryData, createdObBeneficiary5.getBeneficiaryId());
        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertTrue(updatedIntlBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getNickname().contains(validnickname));
    }

    @Test
    public void negative_case_create_international_beneficiary_with_invalid_auth() {
        TEST("AHBDB-7655 : create international beneficiary with invalid auth ");
        envUtils.ignoreTestInEnv("Feature not deployed yet on NFT, STG", Environments.NFT, Environments.STG);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);

        THEN("I expect the api to send a 401 response on passing invalid bearer token");
        this.beneficiaryApiFlows.createBeneficiaryInternationalInvalidBearer(alphaTestUser, beneficiaryData, 401);

        DONE();
    }

    @Test
    public void negative_case_update_international_beneficiary_with_invalid_auth() {
        TEST("AHBDB-7687 : update international beneficiary with invalid auth ");
        envUtils.ignoreTestInEnv("Feature not deployed yet on NFT, STG", Environments.NFT, Environments.STG);
        String validSwift = "ICICINBB";

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        THEN("I expect the update api to send a 401 response on passing invalid bearer token");
        beneficiaryData.setSwiftCode(validSwift);
        this.beneficiaryApiFlows.updateBeneficiaryInternationalInvalidBearer(alphaTestUser, beneficiaryData, createdObBeneficiary5.getBeneficiaryId(), 401);

        DONE();
    }

    @Test
    public void negative_case_delete_beneficiary_with_invalid_auth() {
        TEST("AHBDB-7691 : delete international beneficiary with invalid auth");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a completed step up auth");
        beneStepUpAuthOTP();
        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);
        THEN("I expect the delete api to send a 401 response on passing invalid bearer token");
        this.beneficiaryApiFlows.deleteBeneficiaryInvalidBearer(alphaTestUser, createdObBeneficiary5.getBeneficiaryId(), 401);
        DONE();
    }


}

