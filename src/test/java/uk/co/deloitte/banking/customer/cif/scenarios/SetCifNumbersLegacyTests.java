package uk.co.deloitte.banking.customer.cif.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBReadIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.cif.CifsApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomDateOfBirth;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEID;

@Tag("@BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SetCifNumbersLegacyTests {

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private CifsApi cifsApi;

    @Inject
    private EnvUtils envUtils;

    private final String CODE_ERROR_0003 = "UAE.ERROR.CONFLICT";
    private final String CODE_ERROR_0002 = "UAE.ERROR.BAD_REQUEST";
    private final String CONFLICT_CIF_MESSAGE = "Cif already in use";
    private final String INVALID_DOCUMENT_NUMBER_MESSAGE = "Invalid document number";
    private final String INVALID_CIF_NUMBER_MESSAGE = "Invalid cif";

    private void setupTestUser() {
        NOTE("Registering and creating a customer");
        envUtils.ignoreTestInEnv(Environments.NFT);
        AlphaTestUser alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
    }

    private AlphaTestUser setupTestUserPhone(String phone, AlphaTestUser alphaTestUserToSetUp) {
        NOTE("Registering and creating a customer");

        alphaTestUserToSetUp = new AlphaTestUser();
        alphaTestUserToSetUp.setUserTelephone(phone);

        alphaTestUserToSetUp = alphaTestUserFactory.setupCustomer(alphaTestUserToSetUp);
        return alphaTestUserToSetUp;
    }


    @Test
    public void happy_path_EID_not_matched_store_unique_CIF_200_OK() {
        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");
        TEST("AHBDB-9326: AC1 - Happy Path Scenario - EID does not exist in CIF service - Store unique CIF - 200 OK");

        GIVEN("A customer’s EID is not matched to a CIF in the CIF service");
        WHEN("The client attempts to reserve a UNIQUE CIF chosen by him.");
        final CifResponse cif = this.cifsApi.submitCif();

        THEN("The CIF service will store the chosen CIF ");
        AND("The CIF service will return a 200 OK");
        AND("CIF is populated");
        Assertions.assertTrue(StringUtils.isNotBlank(cif.getCifNumber()));

        DONE();
    }

    @Test
    public void happy_path_EID_matched_store_unique_CIF_200_OK() {
        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");
        TEST("AHBDB-9327: AC2 - Happy Path Scenario - EID exists in CIF service - Store unique CIF - 200 OK");

        GIVEN("A customer has met all the requirements to become a banking customer");
        setupTestUser();
        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);

        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
        OBReadCustomer1 response = this.customerApi.putCustomerCif(this.alphaTestUser);

        AND("The CIF exists in the database with SystemOfOrigin == DTP");

        OBReadIdvDetailsResponse1 responseIdv = this.customerApi.getCustomerIdvDetails(alphaTestUser);
        String emiratesId = responseIdv.getData().getDocumentNumber();

        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        WHEN("The client attempts to reserve a UNIQUE CIF chosen by the customer");
        String cifNumber = randomNumeric(7).replace("0","1");
        final CifResponse cifResponse = this.cifsApi.submitCifNumber(emiratesId, cifNumber);

        THEN("The CIF service will store the chosen CIF");
        AND("The CIF service will return a 200 OK");
        AND("The CIF service will return the newly stored CIF");
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse.getCifNumber()));
        Assertions.assertEquals(cifNumber, cifResponse.getCifNumber());

        DONE();
    }

    @Test
    public void happy_path_EID_matched_not_storing_CIF() {
        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");
        TEST("AHBDB-9328: AC3 EID exists in CIF service - Do not store CIF - 200 OK ");

        envUtils.ignoreTestInEnv("Ignore in SIT as the DB is not refreshed and chances of conflict high", Environments.SIT);

        GIVEN("A customer’s EID is matched to a CIF in the CIF service");
        String emiratesId = generateRandomEID();
        String cifNumber1 = 1 + randomNumeric(6);
        String cifNumber2 = 2 + randomNumeric(6);

        final CifResponse cifResponse1 = this.cifsApi.submitCifNumber(emiratesId, cifNumber1);

        AND("The CIF exists in the database with SystemOfOrigin == Legacy");
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse1.getCifNumber()));
        Assertions.assertEquals(cifNumber1, cifResponse1.getCifNumber());

        WHEN("The client attempts to reserve the CIF chosen by the customer");
        final CifResponse cifResponse2 = this.cifsApi.submitCifNumber(emiratesId, cifNumber2);

        THEN("the CIF service will return the CIF value of the customer who exists with that Emirates ID number ");
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse2.getCifNumber()));
        Assertions.assertEquals(cifNumber1, cifResponse2.getCifNumber());
        Assertions.assertNotEquals(cifNumber2, cifResponse2.getCifNumber());

        DONE();
    }

    @Test
    public void CIF_already_exists_409_conflict() {
        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");
        TEST("AHBDB-9329: AC4 CIF already exists - 409 Conflict  ");

        GIVEN("A customer has chosen a CIF which in the CIF service.");
        String cifNumber1 = randomNumeric(7).replace("0","1");
        final CifResponse cifResponse1 = this.cifsApi.submitCifNumber(randomNumeric(15).replaceFirst("0", "1"), cifNumber1);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse1.getCifNumber()));
        Assertions.assertEquals(cifNumber1, cifResponse1.getCifNumber());

        WHEN("Another client attempts to reserve the same CIF in the CIF service.");
        OBErrorResponse1 cifResponseError = this.cifsApi.submitCifNumberError(randomNumeric(15).replaceFirst("0", "1"), cifNumber1, 409);
        THEN("The CIF service will not store the CIF");
        AND("The CIF service will return a 409 Conflict");
        Assertions.assertTrue(cifResponseError.getCode().contains(CODE_ERROR_0003), "Error Code was not as expected, " +
        "test expected : " + CODE_ERROR_0003);
        Assertions.assertTrue(cifResponseError.getMessage().contains(CONFLICT_CIF_MESSAGE), "Error Message was not as expected, " +
                "test expected : " + CONFLICT_CIF_MESSAGE);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "12345678", "abcdef", "!@#$%^"})
    public void invalid_cif_number_400_response(String invalidCifNumber) {
        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");
        TEST("AHBDB-9331: Negative Test - Invalid CIF Number - 400 response");

        GIVEN("A customer’s EID is not matched to a CIF in the CIF service");
        WHEN("The client attempts to reserve an invalid UNIQUE CIF chosen by him.");

        OBErrorResponse1 cifResponseError = this.cifsApi.submitCifNumberError("123123412345671", invalidCifNumber, 400);

        THEN("The CIF service will not store the chosen CIF.");
        AND("The CIF service will return a 400 response");

        Assertions.assertTrue(cifResponseError.getCode().contains(CODE_ERROR_0002), "Error Code was not as expected, " +
                "test expected : " + CODE_ERROR_0002);
        Assertions.assertTrue(cifResponseError.getMessage().contains(INVALID_CIF_NUMBER_MESSAGE), "Error Message was not as expected, " +
                "test expected : " + INVALID_CIF_NUMBER_MESSAGE);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12312341234567", "1231234123456712", "abcabcagaggagaa", "!@#$%^&*()_+_)("})
    public void invalid_emirates_id_400_response(String invalidEmiratesId) {
        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");
        TEST("AHBDB-9330: Negative Test - Invalid Emirates Id - 400 response");

        GIVEN("A customer’s EID is not matched to a CIF in the CIF service");
        WHEN("The client attempts to reserve a UNIQUE CIF chosen by him with an invalid Document Number.");

        OBErrorResponse1 cifResponseError = this.cifsApi.submitCifNumberError(invalidEmiratesId, "4561238", 400);

        THEN("The CIF service will not store the chosen CIF ");
        AND("The CIF service will return a 400 response.");

        Assertions.assertTrue(cifResponseError.getCode().contains(CODE_ERROR_0002), "Error Code was not as expected, " +
                "test expected : " + CODE_ERROR_0003);
        Assertions.assertTrue(cifResponseError.getMessage().contains(INVALID_DOCUMENT_NUMBER_MESSAGE), "Error Message was not as expected, " +
                "test expected : " + INVALID_DOCUMENT_NUMBER_MESSAGE);

        DONE();
    }

    @Test
    public void null_cif_number_401_response() {
        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");
        TEST("AHBDB-9332: Negative Test - null CIF Number - 400 response");

        GIVEN("A customer’s EID is not matched to a CIF in the CIF service");
        WHEN("The client attempts to reserve an null UNIQUE CIF chosen by him.");

        this.cifsApi.submitCifNumberVoid("123123412345671", "", 401);

        THEN("The CIF service will not store the chosen CIF.");
        AND("The CIF service will return a 401 response");

        DONE();
    }

    @Test
    public void happy_path_null_emirates_id_200_response() {
        envUtils.ignoreTestInEnv("AHBDB-13552", Environments.NFT);

        TEST("AHBDB-8204: AHB Legacy Platform to set CIF numbers in the CIF Service");

        GIVEN("A customer’s EID is not matched to a CIF in the CIF service");
        WHEN("The client attempts to reserve a UNIQUE CIF chosen by him with a null Document Number.");

        String cif = randomNumeric(7).replace("0", "1");

        CifResponse response = this.cifsApi.submitCifNumber(null, cif);

        THEN("The CIF service will store the chosen CIF");
        assertEquals(cif, response.getCifNumber());
        DONE();
    }

    @Test
    @Tag("AHBDB-10476")
    @Tag("SmokeTest")
    public void customer_tries_to_generate_CIF_with_0_at_start_from_phone_number() {
        TEST("AHBDB-10476: CIF changes to avoid leading 0s - Customer tries to generate CIF with 0 at the start ");

        GIVEN("A customer has met all the requirements to become a banking customer");
        setupTestUser();
        String cifNumber = "0" + randomNumeric(6);
        String phoneNumber = "+55552" + cifNumber;

        this.alphaTestUser = setupTestUserPhone(phoneNumber, this.alphaTestUser);

        LocalDate dateOfBirth = generateRandomDateOfBirth();
        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(dateOfBirth)
                        .mobileNumber(phoneNumber)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        AND("The customer has populated EID and IDV complete");
        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);

        WHEN("The customer adapter attempts to generate a CIF with a 0 at the beginning");
        this.cifsApi.generateCifLegacy(dateOfBirth, this.alphaTestUser.getUserTelephone());

        THEN("The platform will return a 200 and change the CIF Number");
        DONE();
    }

    @Test
    @Tag("AHBDB-10476")
    public void put_cif_number_that_starts_with_0() {
        TEST("AHBDB-10476: CIF changes to avoid leading 0s - Customer tries to generate CIF with 0 at the start ");

        GIVEN("A customer’s EID is not matched to a CIF in the CIF service");
        String cifNumber = "0" + randomNumeric(6);
        String emiratesId = randomNumeric(15);

        WHEN("The client attempts to reserve a UNIQUE CIF chosen by him that starts with 0.");
        OBErrorResponse1 cifError = this.cifsApi.submitCifNumberError(emiratesId, cifNumber, 400);
        assertEquals(CODE_ERROR_0002, cifError.getCode(), "Code error is not matching, expected " + CODE_ERROR_0002 +
                        " but received " + cifError.getCode());
        assertEquals(INVALID_CIF_NUMBER_MESSAGE, cifError.getMessage(), "Code error is not matching, expected " + INVALID_CIF_NUMBER_MESSAGE +
                " but received " + cifError.getMessage());

        THEN("The CIF service will not store the chosen CIF");
        AND("The CIF service will return a 400");

        DONE();
    }
}
