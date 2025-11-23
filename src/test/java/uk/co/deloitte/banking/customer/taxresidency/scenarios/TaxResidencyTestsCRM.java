package uk.co.deloitte.banking.customer.taxresidency.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.crs.*;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.taxresidency.api.TaxResidencyApiV2;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaxResidencyTestsCRM {

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserForNotFound;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private TaxResidencyApiV2 taxResidencyApi;

    private final String REQUEST_VALIDATION_ERROR_CODE = "REQUEST_VALIDATION";
    private final String STANDARD_ERROR_MESSAGE = "createCRSDetails.crsDetails.data.";
    private final String ERROR_CODE_0005 = "0005";
    private final String ERROR_MESSAGE_NATIONALITY = "Nationality must match existing ISO-";
    private final String ERROR_VALIDATION = "UAE.ERROR.VALIDATION";

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    public void setupTestUserForNotFoundInCRS() {
        this.alphaTestUserForNotFound = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());
    }

    @Test
    public void happy_path_post_CRS_201_created_and_get_CRS_200_success() {

        TEST("AHBDB-4847: AC1 Post CRS - 201 Created");
        TEST("AHBDB-4847: AC3 Get CRS - 200 Success");
        TEST("AHBDB-5872: AC1 Positive Test - Happy Path Scenario - Post CRS - 201 Created and AC3 Get CRS - 200 Success");
        setupTestUser();
        GIVEN("We have received a request from the client to store the CRS details");
        OBWriteCRS2 crs = this.taxResidencyApi.getObWriteCRS2();

        WHEN("We pass the request to CRM to post the relevant information to customer with a valid" +
                "JWT token and valid field inputs");
        OBWriteCRSResponse2 postCrsResponse = this.customerApi.postCRSDetails(this.alphaTestUser, crs);

        THEN("We'll receive a 201 created response");

        GIVEN("We have received a request from the client to get a customer with a valid userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        this.taxResidencyApi.getTaxInformation(this.alphaTestUser, 200);

        THEN("We'll receive a 200 response");
        AND("We will receive all of the CRS fields related to that userID");

        DONE();
    }

    @Test
    public void happy_path_post_CRS_multiple_TaxResidencyCountry_201_created_and_get_CRS_200_success() {

        TEST("AHBDB-4847: AC1 Post CRS - 201 Created");
        TEST("AHBDB-4847: AC3 Get CRS - 200 Success");
        TEST("AHBDB-5055: AC1 Positive Test - Happy Path Scenario - Post CRS  Multiple TaxResidencyCountry - 201 Created " +
                "and AC3 Get CRS - 200 Success");
        setupTestUser();
        GIVEN("We have received a request from the client to store the CRS details");
        OBWriteCRS2 crs = this.taxResidencyApi.getObWriteCRS2ValidMultipleTaxResidencies();

        WHEN("We pass the request to CRM to post the relevant information to customer with a valid" +
                "JWT token and valid field inputs");
        OBWriteCRSResponse2 postCrsResponse = this.customerApi.postCRSDetails(this.alphaTestUser, crs);

        THEN("We'll receive a 201 created response");

        GIVEN("We have received a request from the client to get a customer with a valid userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        this.taxResidencyApi.getTaxInformation(this.alphaTestUser, 200);

        THEN("We'll receive a 200 response");
        AND("We will receive all of the CRS fields related to that userID");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ae", "AEUK", "!@", "33"})
    public void invalid_data_Country_400_response(String invalidCountry) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-3063: AC2 Negative Test - Post - invalid data Country - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "Country", invalidCountry
                ),
                "AgreedCertification",
                "true"
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.taxResidencyApi.postInvalidCRSDetailsErrorResponse(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_VALIDATION, error.getCode(), "Error codes do not match");
        Assertions.assertEquals(ERROR_MESSAGE_NATIONALITY, error.getMessage(), "Error messages do not match");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "hjsdvcjshdvcsjhdbcvkj"})
    public void invalid_data_TinNumber_400_response(String invalidTin) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-2716: AC2 Negative Test - Post - invalid data TinNumber - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "TinNumber", invalidTin
                ),
                "AgreedCertification",
                "true"
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.taxResidencyApi.postInvalidCRSDetailsErrorResponse(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_ERROR_CODE, error.getCode(), "Error codes do not match");
        Assertions.assertTrue(error.getMessage().contains(STANDARD_ERROR_MESSAGE + "taxResidencyCountry[0].tinNumber"), "Error messages do not match");

        DONE();
    }

    @ParameterizedTest                          //Commented due to AHBDB-6399
    @ValueSource(strings = {"1234", "@£$%^&"/*, "Country   does    not    issue   TINs to its      residents "*/})
    public void invalid_data_MissingTinReason_400_response(String invalidTinReason) {

        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-2715: AC2 Negative Test - Post - invalid data MissingTinReason - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "MissingTinReason", invalidTinReason
                ),
                "AgreedCertification",
                "true"
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.taxResidencyApi.postInvalidCRSDetailsErrorResponse(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "FALSE", "asdfgh", "12345"})
    public void invalid_data_UaeResidencyByInvestmentScheme_400_response(String invalidUaeResidencyByInvestmentScheme) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-5879: AC2 Negative Test - Post - invalid data UaeResidencyByInvestmentScheme - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "Country", "AE"
                ),
                "UaeResidencyByInvestmentScheme",
                invalidUaeResidencyByInvestmentScheme
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.taxResidencyApi.postInvalidCRSDetails(this.alphaTestUser, crs);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "FALSE", "asdfgh", "12345"})
    public void invalid_data_OtherResidencyJurisdictions_400_response(String invalidOtherResidencyJurisdictions) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-5880: AC2 Negative Test - Post - invalid data OtherResidencyJurisdictions - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "Country", "AE"
                ),
                "OtherResidencyJurisdictions",
                invalidOtherResidencyJurisdictions
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.taxResidencyApi.postInvalidCRSDetails(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "ae", "!@", "33"})
    public void invalid_data_PersonalIncomeTaxJurisdictions_400_response(String invalidPersonalIncomeTaxJurisdictions) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-5881: AC2 Negative Test - Post - invalid data PersonalIncomeTaxJurisdictions - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "Country", "AE"
                ),
                "UaeResidencyByInvestmentScheme",
                invalidPersonalIncomeTaxJurisdictions
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.taxResidencyApi.postInvalidCRSDetails(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "FALSE", "asdfgh", "12345"})
    public void invalid_data_AgreedCertification_400_response(String invalidAgreedCertification) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-5882: AC2 Negative Test - Post - invalid data AgreedCertification - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "Country", "AE"
                ),
                "AgreedCertification",
                invalidAgreedCertification
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.taxResidencyApi.postInvalidCRSDetails(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @Test
    public void customer_record_does_not_exist_404() {

        TEST("AHBDB-13312- Test fix");

        TEST("AHBDB-4847: AC4 Customer record doesn't exist - 404");
        TEST("AHBDB-5883: AC4 Negative Test - Customer record doesn't exist - 404");
        GIVEN("We have received a request from the client to get a customer or IDV table with a userID that does not exist");
        setupTestUserForNotFoundInCRS();

        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        this.taxResidencyApi.getTaxInformationFromNotFound(this.alphaTestUserForNotFound);

        THEN("The API will return a 404 - not found");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TaxResidencyCountry", "AgreedCertification"})
    public void missing_data_400_response(String fieldToRemove) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-5884: AC2 Negative Test - Post - missing data - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRSRemoved(
                this.taxResidencyApi.buildTaxResidencyCountryJSON(
                        "Country", "AE"
                ),
                fieldToRemove);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.taxResidencyApi.postInvalidCRSDetailsErrorResponse(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_ERROR_CODE, error.getCode(), "Error codes do not match");
        Assertions.assertTrue(error.getMessage().contains(STANDARD_ERROR_MESSAGE), "Error messages do not match");

        DONE();
    }

    @Test
    public void missing_data_Country_400_response() {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-5885: AC2 Negative Test - Post - missing data Country - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject crs = this.taxResidencyApi.getJSONForCRS(
                buildTaxResidencyCountryJSONRemoved(),
                "AgreedCertification",
                "true"
        );

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.taxResidencyApi.postInvalidCRSDetailsErrorResponse(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_ERROR_CODE, error.getCode(), "Error codes do not match");
        Assertions.assertTrue(error.getMessage().contains(STANDARD_ERROR_MESSAGE + "taxResidencyCountry[0].country"), "Error messages do not match");

        DONE();
    }

    @Test
    public void invalid_data_exceeding_2_objects_of_TaxResidencyCountry_400_response() {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data = 400 bad request");
        TEST("AHBDB-5886: AC2 Negative Test - Post - invalid data - Exceeding 2 Objects of TaxResidencyCountry - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        OBWriteCRS2 crs = this.taxResidencyApi.getObWriteCRS2MultipleTaxResidencies();

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.taxResidencyApi.addCRSDetailsTooManyTaxResidencies(this.alphaTestUser, crs);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"UaeResidencyByInvestmentScheme", "OtherResidencyJurisdictions", "PersonalIncomeTaxJurisdictions"})
    public void missing_optional_data_201_response(String fieldToRemove) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data - 201 response");
        TEST("AHBDB-8224: Positive Test - Post - missing optional data - Value: <Value>");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject taxResidencyCountry =
                this.taxResidencyApi.buildTaxResidencyCountryJSON("Country", "AE");
        JSONObject crs =
                this.taxResidencyApi.getJSONForCRSRemoved(taxResidencyCountry, fieldToRemove);

        OBWriteCRSResponse2 postCrsResponse = this.taxResidencyApi.postCRSDetailsJson(this.alphaTestUser, crs);
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing field inputs");
        THEN("We'll receive a 201 response");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TinNumber", "MissingTinReason"})
    public void missing_optional_tax_data_201_response(String fieldToRemove) {
        TEST("AHBDB-4847: AC2 Post - missing or invalid data - 201 response");
        TEST("AHBDB-8225: Positive Test - Post - missing optional TaxResidencyCountry data - Value: <Value>");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject taxResidencyCountry = new JSONObject() {
            {
                put("Country", "AE");
                put("TinNumber", "ABC456789");
                put("MissingTinReason", "Country doesn't issue TINs to its residents");
                remove(fieldToRemove);
            }
        };

        JSONObject crs =
                this.taxResidencyApi.getJSONForCRS(taxResidencyCountry, "AgreedCertification", "true");

        OBWriteCRSResponse2 postCrsResponse = this.taxResidencyApi.postCRSDetailsJson(this.alphaTestUser, crs);
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing field inputs");
        THEN("We'll receive a 201 response");
    }

    @Test
    public void otherResidencyJurisdictions_false_201_response() {
        TEST("AHBDB-5502: CRS API giving error");
        setupTestUser();
        GIVEN("We have received a request from the client to create CRS");
        JSONObject taxResidencyCountry =
                this.taxResidencyApi.buildTaxResidencyCountryJSON("Country", "AE");

        JSONObject crs = new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("TaxResidencyCountry", taxResidencyCountry);
                        put("UaeResidencyByInvestmentScheme", "true");
                        put("OtherResidencyJurisdictions", "false");
                        put("PersonalIncomeTaxJurisdictions", NULL);
                        put("AgreedCertification", "true");
                    }
                });
            }
        };

        WHEN("We pass the request to CRM to create the customer with a valid JWT token");
        AND("data.OtherResidencyJurisdictions is set to False");
        AND("data.personalIncomeTaxJurisdictions is missing/null");
        OBWriteCRSResponse2 postCrsResponse = this.taxResidencyApi.postCRSDetailsJson(this.alphaTestUser, crs);
        THEN("We'll receive a 201 response");
    }

    @Test
    public void missing_tin_reason_201_response() {
        TEST("AHBDB-4417: Save CRS API giving error for Missing TIN Reason");
        setupTestUser();
        GIVEN("We have received a request from the client to store the CRS details");
        OBWriteCRS2 crs = OBWriteCRS2.builder()
                .data(OBCRSData2.builder()
                        .taxResidencyCountry(List.of(OBTaxResidencyCountry2.builder()
                                .country("AE")
                                .missingTinReason("Country doesn't issue TINs to its residents")
                                .build()))
                        .uaeResidencyByInvestmentScheme(true)
                        .otherResidencyJurisdictions(true)
                        .personalIncomeTaxJurisdictions(List.of("US"))
                        .agreedCertification(true)
                        .build())
                .build();

        WHEN("We pass the request with the given tin reason");
        OBWriteCRSResponse2 postCrsResponse = this.customerApi.postCRSDetails(this.alphaTestUser, crs);
        Assertions.assertNotNull(postCrsResponse);

        THEN("We'll receive a 201 created response");
        AND("The customers details are saved correctly");
        this.taxResidencyApi.getTaxInformation(this.alphaTestUser, 200);

        DONE();
    }

    @Test
    public void get_crs_with_null_personalIncomeTaxJurisdictions_posted_200_response() {
        TEST("AHBDB-4418: Get CRS API giving error");
        setupTestUser();
        GIVEN("We have received a request from the client to store the CRS details");
        WHEN("We pass the request with missing personalIncomeTaxJurisdictions");
        OBWriteCRS2 crs = OBWriteCRS2.builder()
                .data(OBCRSData2.builder()
                        .taxResidencyCountry(List.of(OBTaxResidencyCountry2.builder()
                                .country("AE")
                                .missingTinReason("Country doesn't issue TINs to its residents")
                                .build()))
                        .uaeResidencyByInvestmentScheme(true)
                        .otherResidencyJurisdictions(true)
                        .agreedCertification(true)
                        .build())
                .build();

        OBWriteCRSResponse2 postCrsResponse = this.customerApi.postCRSDetails(this.alphaTestUser, crs);
        Assertions.assertNotNull(postCrsResponse);

        THEN("We'll receive a 201 created response");
        AND("When we retrieve the details from the client");
        THEN("We get a 200 response");
        this.taxResidencyApi.getTaxInformation(this.alphaTestUser, 200);
        DONE();
    }

    @Test
    public void missingTinReason_returned_in_get_crs() {
        TEST("AHBDB-7973: Not getting MissingTinReason in CRS response");
        TEST("AHBDB-7491: Country(postCRS) are returned as uuid instead of actual values");
        setupTestUser();
        GIVEN("We have stored the CRS details with missingTinReason given");
        OBWriteCRS2 crs = OBWriteCRS2.builder()
                .data(OBCRSData2.builder()
                        .taxResidencyCountry(List.of(
                                OBTaxResidencyCountry2.builder()
                                .country("AE")
                                .missingTinReason("Country doesn't issue TINs to its residents")
                                .build(),

                                OBTaxResidencyCountry2.builder()
                                        .country("NG")
                                        .missingTinReason("Unable to obtain/disclose a TIN")
                                        .build()))
                        .uaeResidencyByInvestmentScheme(true)
                        .otherResidencyJurisdictions(false)
                        .agreedCertification(true)
                        .build())
                .build();

        OBWriteCRSResponse2 postCrsResponse = this.customerApi.postCRSDetails(this.alphaTestUser, crs);
        Assertions.assertNotNull(postCrsResponse.getData()
                .getTaxResidencyCountry().get(0).getMissingTinReason());

        WHEN("We get the CRS details for a customer");
        THEN("The missingTinReason will be given in the response");
        Response response = this.taxResidencyApi.getTaxInformationWithReturn(this.alphaTestUser, 200);
        Assertions.assertNotNull(postCrsResponse.getData()
                .getTaxResidencyCountry().get(0).getMissingTinReason());
        Assertions.assertNotNull(postCrsResponse.getData()
                .getTaxResidencyCountry().get(1).getMissingTinReason());

        String country1 = response.jsonPath().getString("Data[0].TaxResidencyCountry[0].Country");
        String country2 = response.jsonPath().getString("Data[0].TaxResidencyCountry[1].Country");

        ArrayList<String> listOfCountries = new ArrayList<>();
        Collections.addAll(listOfCountries, country1, country2);

        System.out.println(listOfCountries);
        Assertions.assertTrue(listOfCountries.contains("AE"));
        Assertions.assertTrue(listOfCountries.contains("NG"));
        DONE();
    }

    public JSONObject buildTaxResidencyCountryJSONRemoved() throws JSONException {
        return new JSONObject() {
            {
                put("TinNumber", "ABC456789");
                put("MissingTinReason", "Country doesn't issue TINs to its residents");
            }
        };
    }
}
