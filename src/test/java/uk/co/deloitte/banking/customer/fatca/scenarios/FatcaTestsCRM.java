package uk.co.deloitte.banking.customer.fatca.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.*;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FatcaTestsCRM {

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private FatcaApiV2 fatcaApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private CustomerApiV2 customerApi;

    private final String REQUEST_VALIDATION_CODE = "REQUEST_VALIDATION";
    private final String STANDARD_ERROR_MESSAGE = "createCustomerFatcaDetails.fatcaDetails.data.";

    private void setupTestUser() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    @Test
    public void happy_path_post_FATCA_201_created_and_get_FATCA_200_success() {
        TEST("AHBDB-4848: AC1 Post FATCA - 201 Created");
        TEST("AHBDB-4848: AC3 Get FATCA - 200 Success");
        TEST("AHBDB-5705: AC1 Positive Test - Happy Path Scenario - Post FATCA - 201 Created " +
                "and AC3 Get FATCA - 200 Success");
        setupTestUser();

        GIVEN("We have received a request from the client to store the FATCA declaration");
        WHEN("We pass the request to CRM to post the relevant information to customer with a valid JWT token " +
                "and valid field inputs");
        OBWriteFatcaDetails1 postFatcaHappyPathResponse = this.fatcaApi.createFatcaDetails(this.alphaTestUser, OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .usCitizenOrResident(true)
                        .form(OBFatcaForm1.W9)
                        .ssn("654532258")
                        .federalTaxClassification("Individual/sole proprietor or single-member LLC").build()).build()).getData();

        THEN("We'll receive a 201 created response");

        GIVEN("We have received a request from the client to get a customer with a valid userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBReadFatca1 response = this.fatcaApi.getFatcaDetails(this.alphaTestUser);

        THEN("We'll receive a 200 response");
        AND("We will receive all of the FATCA fields related to that userID");
        Assertions.assertEquals(postFatcaHappyPathResponse, response.getData(), "Response does not match");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"w8", "W6", "12"})
    public void invalid_data_Form_400_response(String invalidForm) {
        TEST("AHBDB-4848: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5707: AC2 - Negative Scenario - Post - invalid data Form - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create FATCA");
        JSONObject invalidData = this.fatcaApi.fatcaJSON(invalidForm, "false", "654532258", "S Corporation");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.fatcaApi.createFatcaDetailsJSON(this.alphaTestUser, invalidData);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "TRUE", "FALSE", "!@£$%^"})
    public void invalid_data_USCitizenOrResident_400_response(String invalidUSCitizenOrResident) {
        TEST("AHBDB-4848: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5708: AC2 - Negative Scenario - Post - invalid data USCitizenOrResident - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create FATCA");
        JSONObject invalidData = this.fatcaApi.fatcaJSON("W8", invalidUSCitizenOrResident, "654532258", "S Corporation");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.fatcaApi.createFatcaDetailsJSON(this.alphaTestUser, invalidData);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567b9", "1234567890", "12345678"})
    public void invalid_data_Ssn_400_response(String invalidSsn) {
        TEST("AHBDB-4848: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5709: AC2 - Negative Scenario - Post - invalid data Ssn - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create FATCA");
        JSONObject invalidData = this.fatcaApi.fatcaJSON("W8", "false", invalidSsn, "S Corporation");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.fatcaApi.createFatcaDetailsJSONErrorResponse(this.alphaTestUser, invalidData);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_CODE, error.getCode(), "Error codes do not match, expected: " + REQUEST_VALIDATION_CODE);
        Assertions.assertTrue(error.getMessage().contains(STANDARD_ERROR_MESSAGE + "ssn"), "Error messages do not match");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hdbfjhsdbfkjsdbfcksdjbcvkdfhbvkdhbvkdjbfvkjsbdckjsdbc76tr65r8w7tgdjwhvdbksjfbckdjscdssssssdddddssdeww", "agsvsgfsf", ""})
    public void invalid_data_FederalTaxClassification_400_response(String invalidFederalTaxClassification) {
        TEST("AHBDB-4848: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5710: AC2 - Negative Scenario - Post - invalid data FederalTaxClassification - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create FATCA");
        JSONObject invalidData = this.fatcaApi.fatcaJSON("W8", "false", "654532258", invalidFederalTaxClassification);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.fatcaApi.createFatcaDetailsJSONErrorResponse(this.alphaTestUser, invalidData);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_CODE, error.getCode(), "Error codes do not match, expected: " + REQUEST_VALIDATION_CODE);
        Assertions.assertTrue(error.getMessage().contains(STANDARD_ERROR_MESSAGE + "federalTaxClassification"), "Error messages do not match");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Form:form", "UsCitizenOrResident:usCitizenOrResident"})
    public void missing_mandatory_parameters_400_bad_request(String toRemove) {
        TEST("AHBDB-4848: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5711: AC2 - Negative Scenario - Post - Missing Mandatory Parameters - 400 bad request");
        setupTestUser();
        String[] toRemoveArr = toRemove.split(":");

        GIVEN("We have received a request from the client to create FATCA");
        JSONObject invalidData = this.fatcaApi.fatcaMissingField(toRemoveArr[0]);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.fatcaApi.createFatcaDetailsJSONErrorResponse(this.alphaTestUser, invalidData);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_CODE, error.getCode(), "Error codes do not match, expected: " + REQUEST_VALIDATION_CODE);
        Assertions.assertTrue(error.getMessage().contains(STANDARD_ERROR_MESSAGE + toRemoveArr[1]), "Error messages do not match");

        DONE();
    }

    @Test
    @Order(1)
    public void customer_record_does_not_exist_404() {
        TEST("AHBDB-4848: AC4 Customer record doesn't exist - 404");
        TEST("AHBDB-5712: AC4 - Negative Scenario - Customer record doesn't exist - 404");
        setupTestUser();
        GIVEN("We have received a request from the client to get a customer or IDV table with a userID that does not exist");

        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBErrorResponse1 error = this.fatcaApi.getFatcaInvalidErrorResponse(this.alphaTestUser, 404);

        THEN("The API will return a 404 - not found");
        Assertions.assertEquals("UAE.ERROR.NOT_FOUND", error.getCode(), "Error Codes do not match, expected: UAE.ERROR.NOT_FOUND");
        Assertions.assertEquals("No FATCAs found for customer", error.getMessage(), "Error Messages do not match");
    }
}
