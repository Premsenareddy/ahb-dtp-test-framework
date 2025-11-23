package uk.co.deloitte.banking.customer.employment.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.customer.model.employment.*;
import uk.co.deloitte.banking.customer.employment.api.EmploymentApiV2;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmploymentCRMTests {

    private AlphaTestUser alphaTestUser;

    private final String ERROR_MESSAGE_INVALID_VALUES_PROVIDED = "Values provided are not valid";
    private final String ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS = "Customer has invalid employment details";
    private final String ERROR_BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";

    private final String ERROR_CODE_REQUEST_VALIDATION = "REQUEST_VALIDATION";
    private final String ERROR_MESSAGE_STANDARD = "";

    private final String ERROR_CODE_0004 = "0004";
    private final String ERROR_MESSAGE_CUSTOMER_NOT_FOUND = "Customer not found";


    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EmploymentApiV2 employmentApi;

    @Inject
    private EnvUtils envUtils;

    private void setupTestUser() {
        /**
         * TODO :: Ignored in NFT currently
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        //TODO:: Still failing
        //envUtils.ignoreTestInEnv(Environments.ALL);

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    private void setupTestUserFresh() {
        /**
         * TODO :: Ignored in NFT currently
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
    }

    @ParameterizedTest
    @CsvSource({"1, 32", "9, 3", "11, 6", "36, 12", "37, 130", "38, 54", "40, 15", "42, 14", "54, 29", "55, 21", "61, 25",
            "66, 13", "68, 28", "70, 23", "77, 131", "79, 44", "80, 51", "81, 42", "82, 61", "87, 132", "88, 133", "90, 68",
            "91, 43", "100, 134", "101, 135", "102, 99", "103, 43", "104, 134", "105, 135", "106, 99"})
    @Order(1)
    public void happy_path_post_employment_employed_201_created(String designationLapsCode, String professionCode) {
        TEST("AHBDB-4844: AC1 Post Employment - 201 Created");
        TEST("AHBDB-5891: AC1 Positive Test - Happy Path Scenario - Post Employment - EMPLOYED - 201 Created");
        setupTestUser();
        GIVEN("We have received a request from the client to store employment details");
        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName("AHB")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode(designationLapsCode)
                .professionCode(professionCode)
                .build();

        WHEN("We pass the request to CRM to post the relevant information to customer with a valid JWT token and valid field inputs");
        OBWriteEmploymentDetailsResponse1 result = this.employmentApi.createEmploymentDetails(this.alphaTestUser, employment);

        THEN("we will return a 201 response");
        Assertions.assertNotNull(result);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAN", "CRY", "BUT", "BCT", "CHO", "COM", "CON", "ELC", "FIN", "FIB", "FBV", "GTR",
            "HAW", "IMP", "DJP", "LAW", "MAF", "MWP", "NPC", "OIL", "ONL", "OTH", "REB", "RET", "BLD", "TXG", "TOB", "TLI",
            "UCT"})
    @Order(2)
    public void happy_path_post_employment_self_employed_201_created(String businessCode) {
        TEST("AHBDB-4844: AC1 Post Employment - 201 Created");
        TEST("AHBDB-5901: AC1 Positive Test - Happy Path Scenario - Post Employment - SELF_EMPLOYED - 201 Created");
        setupTestUser();
        GIVEN("We have received a request from the client to store employment details");
        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.SELF_EMPLOYED)
                .companyName("AHB")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode(businessCode)
                .designationLAPSCode("36")
                .professionCode("99")
                .build();

        WHEN("We pass the request to CRM to post the relevant information to customer with a valid JWT token and valid field inputs");
        OBWriteEmploymentDetailsResponse1 result = this.employmentApi.createEmploymentDetails(this.alphaTestUser, employment);

        THEN("we will return a 201 response");
        Assertions.assertNotNull(result);

        DONE();
    }

    @Test
    @Order(3)
    public void happy_path_post_employment_other_201_created() {
        TEST("AHBDB-4844: AC1 Post Employment - 201 Created");
        TEST("AHBDB-5902: AC1 Positive Test - Happy Path Scenario - Post Employment - OTHER - 201 Created");
        setupTestUser();
        GIVEN("We have received a request from the client to store employment details");
        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.OTHER)
                .companyName("AHB")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("36")
                .professionCode("99")
                .build();

        WHEN("We pass the request to CRM to post the relevant information to customer with a valid JWT token and valid field inputs");
        OBWriteEmploymentDetailsResponse1 result = this.employmentApi.createEmploymentDetails(this.alphaTestUser, employment);

        THEN("we will return a 201 response");
        Assertions.assertNotNull(result);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Employed", "Self_employed", "!@£$%^&*", "13456567", ""})
    public void invalid_data_EmploymentStatus_400_bad_request(String invalidEmploymentStatus) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5903: AC2 Negative Test - Post - invalid data EmploymentStatus - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("EmploymentStatus", invalidEmploymentStatus);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.employmentApi.createEmploymentDetailsJson(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklas"})
    public void invalid_data_CompanyName_400_bad_request(String invalidCompanyName) {
        TEST("AHBDB-12844 - Tests failing - fixed");

        TEST("AHBDB-4844: AC2 Post - invalid data - 400 bad request");
        TEST("AHBDB-5904: AC2 Negative Test - Post - invalid data CompanyName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("CompanyName", invalidCompanyName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_STANDARD + "companyName");

        DONE();
    }

    @Test
    public void invalid_data_CompanyName_null_400_bad_request() {
        TEST("AHBDB-12977 - Tests failing - fixed");

        TEST("AHBDB-4844: AC2 Post - missing data - 400 bad request");
        TEST("AHBDB-5904: AC2 Negative Test - Post - invalid data CompanyName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("CompanyName", "");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains("Company Name is blank"), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_STANDARD + "companyName");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "!@£$%^&*()", "asdfghjkla", ""})
    public void invalid_data_EmployerCode_400_bad_request(String invalidEmployerCode) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5905: AC2 Negative Test - Post - invalid data EmployerCode - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("EmployerCode", invalidEmployerCode);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "employerCode"), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_STANDARD + "employerCode");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"aed 12345", "AED12345678", "AED !@£$%^", "AED aertgf", ""})
    public void invalid_data_MonthlyIncome_400_bad_request(String invalidMonthlyIncome) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5906: AC2 Negative Test - Post - invalid data MonthlyIncome - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("MonthlyIncome", invalidMonthlyIncome);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "monthlyIncome"), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_STANDARD + "monthlyIncome");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "", "asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklas", "1234567890", "!@£$%&*()"})
    public void invalid_data_IncomeSource_400_bad_request(String invalidIncomeSource) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5907: AC2 Negative Test - Post - invalid data IncomeSource - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("IncomeSource", invalidIncomeSource);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "incomeSource"), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_STANDARD + "incomeSource");

        DONE();
    }

    @Tag("TestTest")
    @ParameterizedTest
    @ValueSource(strings = {"A", "AE", "AESDSX", "1234", "!£$", "", "ae", "abc", "abcd", "abdce"})
    public void invalid_data_BusinessCode_400_bad_request(String invalidBusinessCode) {
        TEST("AHBDB-12222: Empty string passed as businessCode returns 200");
        TEST("AHBDB-5682: Change Business Code from Enum to Text");
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5908: AC2 Negative Test - Post - invalid data BusinessCode - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("BusinessCode", invalidBusinessCode);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.employmentApi.createEmploymentDetailsJson(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "asdfghj", "!@£$%^&", ""})
    public void invalid_data_DesignationLAPSCode_400_bad_request(String invalidDesignationLAPSCode) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5909: AC2 Negative Test - Post - invalid data DesignationLAPSCode - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("DesignationLAPSCode", invalidDesignationLAPSCode);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "designationLAPSCode"), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_STANDARD + "designationLAPSCode");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "asdfghj", "!@£$%^&", ""})
    public void invalid_data_ProfessionCode_400_bad_request(String invalidProfessionCode) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5910: AC2 Negative Test - Post - invalid data ProfessionCode - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("ProfessionCode", invalidProfessionCode);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "professionCode"), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_STANDARD + "professionCode");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"124", "10000", "33344566", "5"})
    public void code_does_not_exist_in_reference_lists_ProfessionCode(String professionCodeNotInList) {
        TEST("AHBDB-4844: AC3 Post - Code doesn't exist in reference lists - 404");
        TEST("AHBDB-5911: AC3 Post - Code doesn't exist in reference lists ProfessionCode - 404 response");
        setupTestUser();
        GIVEN("We have received a request from the client to post a code for profession, LAPS or employer");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("ProfessionCode", professionCodeNotInList);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_INVALID_VALUES_PROVIDED), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_INVALID_VALUES_PROVIDED);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"124", "1003", "33566", "5"})
    public void code_does_not_exist_in_reference_lists_DesignationLAPSCode_404_response(String designationLAPSCodeNotInList) {
        TEST("AHBDB-4844: AC3 Post - Code doesn't exist in reference lists - 404");
        TEST("AHBDB-5912: AC3 Post - Code doesn't exist in reference lists DesignationLAPSCode - 404 response");
        setupTestUser();
        GIVEN("We have received a request from the client to post a code for profession, LAPS or employer");
        JSONObject employmentDetails = this.employmentApi.employmentJSON("DesignationLAPSCode", designationLAPSCodeNotInList);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_INVALID_VALUES_PROVIDED), "Error message was not expected, " +
                "expected: " + ERROR_MESSAGE_INVALID_VALUES_PROVIDED);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"EmploymentStatus", "MonthlyIncome"})
    public void missing_data_EmploymentStatus_and_MonthlyIncome_400_response(String fieldToRemove) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5913: AC2 Post - missing data - EmploymentStatus and MonthlyIncome - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentDetails = this.employmentApi.employmentMissingField(fieldToRemove);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("must not be"), "Error message was not expected, " +
                "expected: " + "must not be");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"EMPLOYED", "SELF_EMPLOYED"})
    public void missing_data_CompanyName_when_Mandatory_400_response(String employmentStatus) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5914: AC2 Post - missing data - CompanyName when Mandatory - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentMissingField = this.employmentApi.employmentMissingFieldAndChangingField("CompanyName", "EmploymentStatus", employmentStatus);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentMissingField, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS), "Error message was not expected," +
                " expected: " + ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS);

        DONE();
    }

    @Test
    public void missing_data_CompanyName_when_Optional_201_response() {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5915: AC2 Post - missing data - CompanyName when Optional - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employment = this.employmentApi.employmentMissingFieldAndChangingField("CompanyName", "EmploymentStatus", "OTHER");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.employmentApi.createEmploymentDetailsJson(this.alphaTestUser, employment, 200);

        THEN("We'll receive a 201 Created");

        DONE();
    }

    @Test
    public void missing_data_IncomeSource_when_Mandatory_400_response() {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5916: AC2 Post - missing data - IncomeSource when Mandatory - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employment = this.employmentApi.employmentMissingFieldAndChangingField("IncomeSource", "EmploymentStatus", "OTHER");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employment, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS), "Error message was not expected," +
                " expected: " + ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"EMPLOYED", "SELF_EMPLOYED"})
    public void missing_data_IncomeSource_when_Optional_201_response(String employmentStatus) {

        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5917: AC2 Post - missing data - IncomeSource when Optional - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentMissingField = this.employmentApi.employmentMissingFieldAndChangingField("IncomeSource", "EmploymentStatus", employmentStatus);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.employmentApi.createEmploymentDetailsJson(this.alphaTestUser, employmentMissingField, 200);

        THEN("We'll receive a 200 Created");

        DONE();
    }

    @Test
    public void missing_data_BusinessCode_when_Mandatory_400_response() {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5918: AC2 Post - missing data - BusinessCode when Mandatory - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employment = this.employmentApi.employmentMissingFieldAndChangingField("BusinessCode", "EmploymentStatus", "SELF_EMPLOYED");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employment, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS), "Error message was not expected," +
                " expected: " + ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"EMPLOYED", "OTHER"})
    public void missing_data_BusinessCode_when_Optional_201_response(String employmentStatus) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5919: AC2 Post - missing data - BusinessCode when Optional - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentMissingField = this.employmentApi.employmentMissingFieldAndChangingField("BusinessCode", "EmploymentStatus", employmentStatus);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.employmentApi.createEmploymentDetailsJson(this.alphaTestUser, employmentMissingField, 200);

        THEN("We'll receive a 200 Created");

        DONE();
    }

    @Test
    public void missing_data_DesignationLAPSCode_when_Mandatory_400_response() {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5920: AC2 Post - missing data - DesignationLAPSCode when Mandatory - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentMissingField = this.employmentApi.employmentMissingField("DesignationLAPSCode");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentMissingField, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS), "Error message was not expected," +
                " expected: " + ERROR_MESSAGE_INVALID_EMPLOYMENT_DETAILS);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELF_EMPLOYED", "OTHER"})
    public void missing_data_DesignationLAPSCode_when_Optional_201_response(String employmentStatus) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5921: AC2 Post - missing data - DesignationLAPSCode when Optional - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employment = this.employmentApi.employmentMissingFieldAndChangingField("DesignationLAPSCode", "EmploymentStatus", employmentStatus);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.employmentApi.createEmploymentDetailsJson(this.alphaTestUser, employment, 200);

        THEN("We'll receive a 201 Created");

        DONE();
    }

    @Test
    public void missing_data_ProfessionCode_when_Mandatory_400_response() {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5922: AC2 Post - missing data - ProfessionCode when Mandatory - 400 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employmentMissingField = this.employmentApi.employmentMissingField("ProfessionCode");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.employmentApi.createEmploymentDetailsJsonErrorResponse(this.alphaTestUser, employmentMissingField, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELF_EMPLOYED", "OTHER"})
    public void missing_data_ProfessionCode_when_Optional_201_response(String employmentStatus) {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5923: AC2 Post - missing data - ProfessionCode when Optional - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");
        JSONObject employment = this.employmentApi.employmentMissingFieldAndChangingField("ProfessionCode", "EmploymentStatus", employmentStatus);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.employmentApi.createEmploymentDetailsJson(this.alphaTestUser, employment, 200);

        THEN("We'll receive a 201 Created");

        DONE();
    }

    @Test
    public void customer_record_does_not_exist_404() {
        TEST("AHBDB-4844: AC5 Customer record doesn't exist - 404");
        TEST("AHBDB-5924: AC5 Customer record doesn't exist - 404");
        GIVEN("We have received a request from the client to get a customer or IDV table with a userID that does not exist");
        setupTestUserFresh();
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        this.employmentApi.getEmploymentInvalid(this.alphaTestUser, 404);

        THEN("The API will return a 404 - not found");

        DONE();
    }

    @Test
    public void get_employment_200_success() {
        TEST("AHBDB-4844: AC4 Get Employment - 200 Success");
        TEST("AHBDB-5925: AC4 Get Employment - 200 Success");
        setupTestUser();
        GIVEN("We have received a request from the client to get a customer with a valid userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBReadEmploymentDetailsResponse1 result = this.employmentApi.getEmploymentDetails(this.alphaTestUser);

        THEN("the platform will return a 200 response");
        AND("the platform will return the customer’s employment information");
        OBEmploymentDetails1 data = result.getData();

        Assertions.assertNotNull(data, "Data was null");

        DONE();
    }

    @Test
    public void missing_optional_employercode_200_response() {
        TEST("AHBDB-4844: AC2 Post - missing or invalid data - 201 response");
        TEST("AHBDB-8226: Positive Test - Post - missing Optional data: EmployerCode - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");

        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName("AHB")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("36")
                .professionCode("99")
                .build();

        OBWriteEmploymentDetailsResponse1 result = this.employmentApi.createEmploymentDetails(this.alphaTestUser, employment);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        THEN("We'll receive a 200 response");
        Assertions.assertNotNull(result);
        DONE();
    }

    @Test
    public void company_with_special_characters_200_response() {

        TEST("AHBDB-12973 - Tests failing - fixed");

        TEST("AHBDB-4395: Company Name field accepts no special characters other than a hyphen/dash");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");

        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName("MERRILL LYNCH BANK S.A.")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("36")
                .professionCode("99")
                .build();

        OBWriteEmploymentDetailsResponse1 result = this.employmentApi.createEmploymentDetails(this.alphaTestUser, employment);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        THEN("We'll receive a 200 response");
        Assertions.assertNotNull(result);
        DONE();
    }

    @Test
    public void self_employed_200_response() {
        TEST("AHBDB-8057: [Employment Details] Error is returned by saving employment details by selecting Self Employed option");
        setupTestUser();
        GIVEN("We have received a request from the client to create Employment");

//        Experience body as noted in ticket AHBDB-8057:
//        {
//            "EmploymentStatus": "SELF_EMPLOYED",
//                "CompanyName": "ROYAL JORDANIAN",
//                "EmployerCode": "0",
//                "MonthlyIncome": "AED 25000",
//                "IncomeSource": null,
//                "BusinessCode": "FBV",
//                "DesignationLAPSCode": null,
//                "ProfessionCode": null
//        }

        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.SELF_EMPLOYED)
                .companyName("ROYAL JORDANIAN")
                .employerCode("0")
                .monthlyIncome("AED 25000")
                .businessCode("FBV")
                .build();

        WHEN("We pass the request to CRM to create the customer with a valid JWT token");
        AND("They are self-employed");
        OBWriteEmploymentDetailsResponse1 result =
                this.employmentApi.createEmploymentDetails(this.alphaTestUser, employment);

        THEN("We'll receive a 200 response");
        Assertions.assertNotNull(result);
        DONE();
    }

}
