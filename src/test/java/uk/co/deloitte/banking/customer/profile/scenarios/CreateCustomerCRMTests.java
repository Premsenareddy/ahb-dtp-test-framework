package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.residentialaddress.api.ResidentialAddressApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomAddressLine;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomBuildingNumber;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomCountrySubDivision;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomPostalCode;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomStreetName;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateCustomerCRMTests {

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private ResidentialAddressApi residentialAddressApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserFresh;

    private final String ERROR_CODE_REQUEST_VALIDATION = "REQUEST_VALIDATION";

    public void setupTestUser() {
        envUtils.ignoreTestInEnv("Email update broken",  Environments.ALL, Environments.NFT);

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupUser(this.alphaTestUser);
        }

        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    public void setupTestUserFresh() {
        envUtils.ignoreTestInEnv("Email update broken",  Environments.ALL, Environments.NFT);

        alphaTestUserFresh = this.alphaTestUserFactory.setupUser(new AlphaTestUser());
    }

    @Test
    @Order(1)
    public void happy_path_create_customer_in_CRM_201_created() {
        TEST("AHBDB-3687: AC1 Create Customer in CRM - 201 Created");
        TEST("AHBDB-5621: AC1 Positive Test - Happy Path Scenario - Create Customer in CRM - 201 Created");
        setupTestUserFresh();
        GIVEN("We have received a request from the client to create a customer");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token and valid field inputs");
        OBWriteCustomerResponse1 response = this.customerApi.createCustomerSuccess(this.alphaTestUserFresh, OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(this.alphaTestUserFresh.getName())
                        .dateOfBirth(this.alphaTestUserFresh.getDateOfBirth())
                        .email(this.alphaTestUserFresh.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .mobileNumber(this.alphaTestUserFresh.getUserTelephone())
                        .language(this.alphaTestUserFresh.getLanguage())
                        .firstName(generateEnglishRandomString(10))
                        .lastName(generateEnglishRandomString(10))
                        .fullName(generateEnglishRandomString(20))
                        .gender(OBGender.FEMALE)
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .termsVersion(LocalDate.of(2020, 12, 20))
                        .termsAccepted(true)
                        .address(OBPostalAddress6.builder()
                                .buildingNumber(generateRandomBuildingNumber())
                                .streetName(generateRandomStreetName())
                                .countrySubDivision(generateRandomCountrySubDivision())
                                .country("AE")
                                .postalCode(generateRandomPostalCode())
                                .addressLine(Collections.singletonList(generateRandomAddressLine()))
                                .build())
                        .build())
                .build());

        THEN("We'll receive a 201 created response");
        AND("We'll receive the UserID (Now same as CustomerID) of the customer");
        AND("We'll receive the CreationDateTime");
        Assertions.assertEquals(alphaTestUserFresh.getUserId(), response.getData().getCustomerId().toString());

        OBReadCustomer1 getResponse = customerApi.getCurrentCustomer(this.alphaTestUserFresh);
        OBCustomer1 details = getResponse.getData().getCustomer().get(0);
        Assertions.assertEquals(alphaTestUserFresh.getUserId(), details.getCustomerId().toString());
        Assertions.assertEquals(alphaTestUserFresh.getName(), details.getPreferredName());
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-13-01", "AA-BB-ABCD", "09/05/1990"})
    public void invalid_data_DateOfBirth_400_bad_request(String invalidDateOfBirth) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5622: AC2: Negative Test - Create - invalid data DateOfBirth - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "DateOfBirth", invalidDateOfBirth);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.createCustomerErrorJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"+55590", "+5555412345678", "+1123456789"})
    public void invalid_data_MobileNumber_400_bad_request(String invalidMobileNumber) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5623: AC2: Negative Test - Create - invalid data MobileNumber - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23", "Country", "AE", "CountrySubDivision", "Dubai"), this.alphaTestUser,
                "MobileNumber", invalidMobileNumber);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        String ERROR_BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected error code: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: " + "Phone number must follow pattern");
    }

    @Test
    public void invalid_data_MobileNumber_null_400_bad_request() throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5624: AC2: Negative Test - Create - invalid data MobileNumber null - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi
                        .createAddressJSON("BuildingNumber", "23", "Country",
                                "AE", "CountrySubDivision", "Dubai"),
                this.alphaTestUser, "MobileNumber", null);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("must not be null"), "Error messages do not match, error message should contain: must not be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"d-onalddonald-Wardd-smith smith", "!@£$%^*^$£", "123456789"})
    public void invalid_data_PreferredName_400_bad_request(String invalidPreferredName) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5625: AC2: Negative Test - Create - invalid data PreferredName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "PreferredName", invalidPreferredName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb", "!@#$%^^%$", "1234567833", "a"})
    public void invalid_data_FirstName_400_bad_request(String invalidFirstName) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5626: AC2: Negative Test - Create - invalid data FirstName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "FirstName", invalidFirstName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb", "!@#$%^^%$", "1234567833", "a"})
    public void invalid_data_LastName_400_bad_request(String invalidLastName) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5627: AC2: Negative Test - Create - invalid data LastName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "LastName", invalidLastName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgbsdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdg", "!@#$%^^%$", "1234567833", "a"})
    public void invalid_data_FullName_400_bad_request(String invalidFullName) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5628: AC2: Negative Test - Create - invalid data FullName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "FullName", invalidFullName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"male", "female", "12323", "!@£$%^&"})
    public void invalid_data_Gender_400_bad_request(String invalidGender) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5629: AC2: Negative Test - Create - invalid data Gender - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "Gender", invalidGender);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.createCustomerErrorJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"aa", "Ae", "uk", "!@", "11"})
    public void invalid_data_Nationality_400_bad_request(String invalidNationality) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5630: AC2: Negative Test - Create - invalid data Nationality - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "Nationality", invalidNationality);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ae", "12", "AEB", "!@"})
    public void invalid_data_Country_of_Birth_400_bad_request(String invalidCountryOfBirth) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5631: AC2: Negative Test - Create - invalid country of birth - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "CountryOfBirth", invalidCountryOfBirth);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgbsdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdg", "12323", "!@£$%^&"})
    public void invalid_data_City_of_Birth_400_bad_request(String invalidCityOfBirth) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5632: AC2: Negative Test - Create - invalid city of birth - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "CityOfBirth", invalidCityOfBirth);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"arabic", "english", "1232", "!@£$"})
    public void invalid_data_Language_400_bad_request(String invalidLanguage) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5633: AC2: Negative Test - Create - invalid data Language - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "Language", invalidLanguage);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@ahb.com"
            , "!##$%^&**(@gmail.com", "12323", "!£$%^&"})
    public void invalid_data_Email_400_bad_request(String invalidEmail) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5634: AC2: Negative Test - Create - invalid data Email - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "Email", invalidEmail);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("must be a well-formed email address"), "Error messages do not match, error message should contain: must be a well-formed email address");
    }

    @Test
    public void invalid_data_Email_null_400_bad_request() throws JSONException {

        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5635: AC2: Negative Test - Create - invalid data Email null - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "Email", null);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Must not be blank"), "Error messages do not match, error message should contain: Must not be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"verified", "not_verified", "12323", "!@£$%^&"})
    public void invalid_data_EmailState_400_bad_request(String invalidEmailState) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5636: AC2: Negative Test - Create - invalid data EmailState - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "EmailState", invalidEmailState);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.createCustomerErrorJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-13-01", "AA-BB-ABCD", "09/05/1990"})
    public void invalid_data_TermsVersion_400_bad_request(String invalidTermsVersion) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5637: AC2: Negative Test - Create - invalid data TermsVersion - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "TermsVersion", invalidTermsVersion);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.createCustomerErrorJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "FALSE", "12323", "!@£$%^&"})
    public void invalid_data_TermsAccepted_400_bad_request(String invalidTermsAccepted) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5638: AC2: Negative Test - Create - invalid data TermsAccepted - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "TermsAccepted", invalidTermsAccepted);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.createCustomerErrorJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdfghjhgfdsdfghj", "12345678901234568"})
    public void invalid_data_BuildingNumber_400_bad_request(String invalidBuildingNumber) throws JSONException {

        TEST("AHBDB-1996: Create, patch, and get customer endpoint - residential address");
        TEST("AHBDB-3069: AC3 Negative Test - Invalid residential address BuildingNumber - 400 response");
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5639: AC2: Negative Test - Create - invalid data BuildingNumber - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(
                this.residentialAddressApi.createAddressJSON("BuildingNumber", invalidBuildingNumber), this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("size must be between 1 and 16"), "Error messages do not match, error message should contain: size must be between 1 and 16");
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdfghjhgfdsdfg334556vjsahvcjhsdvcskdjbckdbcvksjdbvcksjdvbcksjdvbksjaaa", ""})
    public void invalid_data_StreetName_400_bad_request(String invalidStreetName) throws JSONException {
        TEST("AHBDB-1996: AC3 Invalid residential address - 400 response");
        TEST("AHBDB-3361: AC3 Negative Test - Invalid residential address StreetName - 400 response");
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5640: AC2: Negative Test - Create - invalid data StreetName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(
                this.residentialAddressApi.createAddressJSON("StreetName", invalidStreetName), this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("size must be between 1 and 70"), "Error messages do not match, error message should contain: size must be between 1 and 70");
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdfghjhgfdsdfg334556vjsahvcjhsdvcskdjbckdbcvksjdbvcksjdvbcksjdvbksjaaa", "123434544545454563764762578625376576", "qwertyuiopqwertyuiopqwertyuiopqwerty", ""})
    public void invalid_data_CountrySubDivision_400_bad_request(String invalidCountrySubDivision) throws JSONException {
        TEST("AHBDB-1996: AC3 Invalid residential address - 400 response");
        TEST("AHBDB-3363: AC3 Negative Test - Invalid residential address CountrySubDivision - 400 response");
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5641: AC2: Negative Test - Create - invalid data CountrySubDivision - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(
                this.residentialAddressApi.createAddressJSON("CountrySubDivision", invalidCountrySubDivision), this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("size must be between 1 and 35"),
                "Error messages do not match, error message should contain: size must be between 1 and 35");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ae", "12", "@#", "", "qwertyuiop", "AED"})
    public void invalid_data_Country_400_bad_request(String invalidCountry) throws JSONException {
        TEST("AHBDB-1996: AC3 Invalid residential address - 400 response");
        TEST("AHBDB-3364: AC3 Negative Test - Invalid residential address Country - 400 response");
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5642: AC2: Negative Test - Create - invalid data Country - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(
                this.residentialAddressApi.createAddressJSON("Country", invalidCountry), this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"),
                "Error messages do not match, error message should contain: Invalid Field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefghijklmnopq", ""})
    public void invalid_data_PostalCode_400_bad_request(String invalidPostalCode) throws JSONException {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4283: AC7 Invalid PostalCode fields - PostalCode: <PostalCode> - 400");
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5643: AC2: Negative Test - Create - invalid data PostalCode - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(
                this.residentialAddressApi.createAddressJSON("PostalCode", invalidPostalCode), this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("size must be between 1 and 16"),
                "Error messages do not match, error message should contain: size must be between 1 and 16");
    }

    @ParameterizedTest
    @ValueSource(strings = {"IDV COMPLETED", "IDV FAILED", "ASDFGHASDFGH", "idv_failed"})
    public void invalid_data_CustomerState_400_bad_request(String invalidCustomerState) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5644: AC2: Negative Test - Create - invalid data CustomerState - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSON(this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser,
                "CustomerState", invalidCustomerState);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.createCustomerErrorJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
    }

    @Test
    public void invalid_data_AddressLine_400_bad_request() {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5645: AC2: Negative Test - Create - invalid data AddressLine - AddressLine more than 3 - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerError(this.alphaTestUser, OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(this.alphaTestUser.getName())
                        .dateOfBirth(this.alphaTestUser.getDateOfBirth())
                        .email(this.alphaTestUser.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .mobileNumber(this.alphaTestUser.getUserTelephone())
                        .language(this.alphaTestUser.getLanguage())
                        .gender(OBGender.FEMALE)
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .termsVersion(LocalDate.of(2020, 12, 20))
                        .termsAccepted(true)
                        .address(OBPostalAddress6.builder()
                                .addressLine(new ArrayList<>() {
                                    {
                                        add(generateEnglishRandomString(20));
                                        add(generateEnglishRandomString(20));
                                        add(generateEnglishRandomString(20));
                                        add(generateEnglishRandomString(20));
                                    }
                                })
                                .build())
                        .build())
                .build(), 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("size must be between 0 and 3"),
                "Error messages do not match, error message should contain: size must be between 0 and 3");
    }

    @ParameterizedTest
    @ValueSource(strings = {"DateOfBirth", "MobileNumber", "PreferredName", "Language", "Email", "EmailState", "TermsVersion", "TermsAccepted"})
    public void missing_mandatory_data_400_bad_request(String fieldToRemove) throws JSONException {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 400 bad request");
        TEST("AHBDB-5646: AC2: Negative Test - Create - missing mandatory data - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        JSONObject details = this.residentialAddressApi.userProfileJSONRemoveField(
                this.residentialAddressApi.createAddressJSON("BuildingNumber", "23"), this.alphaTestUser, fieldToRemove);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.createCustomerErrorResponseJsonObject(this.alphaTestUser, details, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected error code: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("must not be null") || error.getMessage().contains("Must not be blank"), "Error messages do not match");
    }

    @ParameterizedTest
    @Order(4)
    @ValueSource(strings = {"FirstName", "LastName", "FullName", "Gender", "Nationality", "CountryOfBirth", "CityOfBirth", "Address", "CustomerState"})
    public void missing_optional_data_201_response(String optionalFieldToRemove) {
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 201 response");
        TEST("AHBDB-8218: AC2: Negative Test - Create - missing optional data - Value: <Value> - 201 response");
        setupTestUserFresh();
        GIVEN("We have received a request from the client to create or update a customer");
        JSONObject details = this.customerApi.customerJsonRemoveField(this.alphaTestUserFresh, optionalFieldToRemove);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBWriteCustomerResponse1 createResponse = this.customerApi.createCustomerSuccessJson(this.alphaTestUserFresh, details);
        Assertions.assertNotNull(createResponse.getData().getCustomerId());

        OBReadCustomer1 getResponse = this.customerApi.getCustomerSuccess(this.alphaTestUserFresh);

        OBCustomer1 customerDetails = getResponse.getData().getCustomer().get(0);

        THEN("We'll receive a 201 response");
        AND("The fields are correctly filled out");
        switch (optionalFieldToRemove) {
            case "FirstName":
                Assertions.assertNull(customerDetails.getFirstName());
                break;

            case "LastName":
                Assertions.assertNull(customerDetails.getLastName());
                break;

            case "FullName":
                Assertions.assertNull(customerDetails.getFullName());
                break;

            case "Gender":
                Assertions.assertNull(customerDetails.getGender());
                break;

            case "Nationality":
                Assertions.assertNull(customerDetails.getNationality());
                break;

            case "CountryOfBirth":
                Assertions.assertNull(customerDetails.getCountryOfBirth());
                break;

            case "CityOfBirth":
                Assertions.assertNull(customerDetails.getCityOfBirth());
                break;

            case "Address":
                Assertions.assertNull(customerDetails.getAddress());
                break;

            default:
                break;
        }
        DONE();
    }

    @ParameterizedTest
    @Order(3)
    @ValueSource(strings = {"Department", "SubDepartment", "BuildingNumber", "StreetName", "AddressLine", "TownName", "PostalCode"})
    public void missing_optional_address_fields(String optionalFieldToRemove) {

        TEST("AHBDB-2780: Positive Test - Missing Optional Parameters  - 200 response");
        TEST("AHBDB-8266: Country and CountrySubDivision were made mandatory");
        TEST("AHBDB-3687: AC2 Create - missing or invalid data - 201 response");
        TEST("AHBDB-8219: AC2: Negative Test - Create - missing optional data for address - Value: <Value> - 201 response");
        setupTestUserFresh();
        GIVEN("We have received a request from the client to create or update a customer");
        JSONObject details = this.customerApi.customerJsonAddressRemoveField(alphaTestUserFresh, optionalFieldToRemove);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBWriteCustomerResponse1 createResponse = customerApi.createCustomerSuccessJson(alphaTestUserFresh, details);
        Assertions.assertNotNull(createResponse.getData().getCustomerId());

        OBReadCustomer1 readCustomer = customerApi.getCustomerSuccess(alphaTestUserFresh);
        Assertions.assertEquals(alphaTestUserFresh.getUserId(), readCustomer.getData().getCustomer().get(0).getCustomerId().toString());

        OBPostalAddress6 address = readCustomer.getData().getCustomer().get(0).getAddress();
        switch (optionalFieldToRemove) {
            case "Department":
                Assertions.assertNull(address.getDepartment());
                break;

            case "SubDepartment":
                Assertions.assertNull(address.getSubDepartment());
                break;

            case "BuildingNumber":
                Assertions.assertNull(address.getBuildingNumber());
                break;

            case "StreetName":
                Assertions.assertNull(address.getStreetName());
                break;

            case "AddressLine":
                Assertions.assertNull(address.getAddressLine());
                break;

            case "TownName":
                Assertions.assertNull(address.getTownName());
                break;

            case "PostalCode":
                Assertions.assertNull(address.getPostalCode());
                break;
        }

        THEN("We'll receive a 201 response");
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Country", "CountrySubDivision"})
    @Order(6)
    public void missing_mandatory_address_fields_400_response(String nullMandatoryAddressField) {
        TEST("AHBDB-8266: Country and CountrySubDivision made mandatory");
        setupTestUserFresh();
        GIVEN("A client wants to create a customer in CRM");
        AND("A mandatory field is left out");
        JSONObject details = this.customerApi.customerJsonAddressRemoveField(alphaTestUserFresh, nullMandatoryAddressField);

        WHEN("A client attempts to create them in CRM");
        OBErrorResponse1 error = customerApi.createCustomerErrorResponseJsonObject(alphaTestUserFresh, details, 400);
        THEN("The platform will return a 400 Response");
        Assertions.assertNotNull(error);
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode());
        DONE();
    }

    @Test
    @Order(2)
    public void create_customer_younger_than_21_400_response() {
        GIVEN("We have received a request from the client to create a customer");
        AND("The customer is younger than 21");

        setupTestUserFresh();

        OBWriteCustomer1 obWriteCustomer1 =
                OBWriteCustomer1.builder().data(OBWriteCustomer1Data.builder()
                        .preferredName(this.alphaTestUserFresh.getName())
                        .dateOfBirth(LocalDate.of(2016, 1, 1))
                        .email(this.alphaTestUserFresh.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .mobileNumber(this.alphaTestUserFresh.getUserTelephone())
                        .language(this.alphaTestUserFresh.getLanguage())
                        .gender(OBGender.FEMALE)
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .termsVersion(LocalDate.of(2020, 12, 20))
                        .address(OBPostalAddress6.builder()
                                .buildingNumber(generateRandomBuildingNumber())
                                .streetName(generateRandomStreetName())
                                .countrySubDivision(generateRandomCountrySubDivision())
                                .country("AE")
                                .postalCode(generateRandomPostalCode())
                                .addressLine(Collections.singletonList(generateRandomAddressLine()))
                                .build())
                        .termsAccepted(true)
                        .build()).build();
        WHEN("We pass the request to CRM to create the customer with a valid JWT token");
        customerApi.createCustomerError(this.alphaTestUserFresh, obWriteCustomer1, 400);
        THEN("The platform will respond with a 400 error");
        DONE();
    }

}
