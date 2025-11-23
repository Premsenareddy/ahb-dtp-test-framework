package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.EmailVerification;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.email.api.EmailVerificationApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.residentialaddress.api.ResidentialAddressApi;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomTownName;

@Tag("BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PatchCustomerCRMTests {

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private ResidentialAddressApi residentialAddressApi;

    @Inject
    private EmailVerificationApi emailVerificationApi;

    @Inject
    private EnvUtils envUtils;

    private final String ERROR_CODE_0002 = "0002";
    private final String ERROR_BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";
    private final String ERROR_NOT_FOUND = "UAE.ERROR.NOT_FOUND";
    private final String ERROR_CODE_REQUEST_VALIDATION = "REQUEST_VALIDATION";
    private final String INVALID_FIELD = "Invalid Field";
    private final String PATCH_UNDER_18_ERROR_MESSAGE = "The customer is an an adult, age must be 18+";

    private void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Test
    public void happy_path_get_customer_200_success() {
        TEST("AHBDB-5218: AC3 Get Customer - 200 Success");
        TEST("AHBDB-6341: AC3 Positive Test - Happy Path Scenario - Get Customer - 200 Success");
        setupTestUser();
        GIVEN("We have received a request from the client to get a customer with a valid userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBReadCustomer1 response = this.customerApi.getCustomerSuccess(this.alphaTestUser);

        THEN("We'll receive a 200 response");
        AND("We will receive the full customer profile related to that userID");
        Assertions.assertNotNull(response);

        DONE();
    }

    @Test
    public void happy_path_customer_record_does_not_exist_404() {
        TEST("AHBDB-5218: AC4 Customer record doesn't exist - 404");
        TEST("AHBDB-6342: AC4 Positive Test - Happy Path Scenario - Customer record doesn't exist - 404");
        AlphaTestUser testUser = alphaTestUserFactory.setupUser(new AlphaTestUser());
        GIVEN("We have received a request from the client to get a customer with a userID that does not exist");

        WHEN("We pass the request to CRM to get the customer with a valid JWT token");
        OBErrorResponse1 error = this.customerApi.getCustomerErrorResponse(testUser, 404);

        THEN("The API will return a 404 - not found");
        Assertions.assertEquals(ERROR_NOT_FOUND, error.getCode());
        Assertions.assertTrue(error.getMessage().contains("Customer not found"));
        DONE();
    }

    @Test
    public void happy_path_scenario_patch_customer_200_success() {
        TEST("AHBDB-5218: AC1 Patch Customer - 200 Success");
        TEST("AHBDB-6343: AC1 Positive Test - Happy Path Scenario - Patch Customer - 200 Success");
        setupTestUser();
        GIVEN("We have received a request from the client to update a field in the customer profile");
        OBWritePartialCustomer1 patchDetails = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .preferredName(generateRandomString(10))
                        .dateOfBirth(LocalDate.now().minusYears(22))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language("en")
                        .gender(OBGender.MALE)
                        .build())
                .build();

        WHEN("We pass the request to CRM to update the customer with a valid JWT token and the relevant updated fields");
        OBWriteCustomerResponse1 patchResponse =
                this.customerApi.patchCustomerSuccess(this.alphaTestUser, patchDetails);

        THEN("We'll receive a 200 Success");
        Assertions.assertNotNull(patchResponse);

        DONE();
    }

    @Test
    public void update_email_verification_status_if_new_email_is_sent() {

        TEST("AHBDB-7968: Update Email Verified flag to 'Not Verified' if a new email has not been verified");
        AlphaTestUser testUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

        emailVerificationApi.generateEmailVerificationLink(testUser, testUser.getUserEmail(), 204);
        EmailVerification emailVerification = emailVerificationApi.getEmailVerificationLink(testUser);
        emailVerificationApi.verifyEmailLink(testUser, emailVerification, 200);

        await().atMost(10, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    OBReadCustomer1 getCustomer = customerApi.getCurrentCustomer(testUser);
                    Assertions.assertEquals("VERIFIED", getCustomer.getData().getCustomer().get(0).getEmailState().toString());
                });

        OBWritePartialCustomer1 updateEmail = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .email(generateRandomEmail())
                        .build())
                .build();

        customerApi.patchCustomerSuccess(testUser, updateEmail);

        OBReadCustomer1 getCustomerNewEmail = customerApi.getCurrentCustomer(testUser);
        Assertions.assertEquals("NOT_VERIFIED", getCustomerNewEmail.getData().getCustomer().get(0).getEmailState().toString());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-13-01", "AA-BB-ABCD", "09/05/1990"})
    public void invalid_data_DateOfBirth_400_bad_request(String invalidDateOfBirth) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6344: AC2 Negative Test - Patch - invalid data DateOfBirth - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("DateOfBirth", invalidDateOfBirth);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+55590", "+5555412345678", "+1123456789"})
    public void invalid_data_MobileNumber_400_bad_request(String invalidMobileNumber) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6345: AC2 Negative Test - Patch - invalid data MobileNumber - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("MobileNumber", invalidMobileNumber);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_BAD_REQUEST);
        Assertions.assertTrue(error.getMessage().contains("Phone Number Invalid Field"), "Error message was not as expected");

        DONE();
    }

    @Test
    public void invalid_data_MobileNumber_null_400_bad_request() {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6346: AC2 Negative Test - Patch - invalid data MobileNumber - null - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("MobileNumber", "");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);

        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains("mobileNumber"), "Error message was not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"d-onalddonald-Wardd-smith smith", "!@£$%^*^$£", "123456789"})
    public void invalid_data_PreferredName_400_bad_request(String invalidPreferredName) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6347: AC2 Negative Test - Patch - invalid data PreferredName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("PreferredName", invalidPreferredName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains("preferredName"), "Error message was not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb", "!@#$%^^%$", "1234567833", "a"})
    public void invalid_data_FirstName_400_bad_request(String invalidFirstName) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6348: AC2 Negative Test - Patch - invalid data FirstName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("FirstName", invalidFirstName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains("firstName"), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");


        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb", "!@#$%^^%$", "1234567833", "a"})
    public void invalid_data_LastName_400_bad_request(String invalidLastName) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6349: AC2 Negative Test - Patch - invalid data LastName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("LastName", invalidLastName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains("lastName"), "Error message was not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgbsdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdg", "!@#$%^^%$", "1234567833", "a"})
    public void invalid_data_FullName_400_bad_request(String invalidFullName) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6350: AC2 Negative Test - Patch - invalid data FullName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("FullName", invalidFullName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains("fullName"), "Error message was not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"male", "female", "12323", "!@£$%^&"})
    public void invalid_data_Gender_400_bad_request(String invalidGender) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6351: AC2 Negative Test - Patch - invalid data Gender - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("Gender", invalidGender);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"aa", "Ae", "uk", "!@", "11"})
    public void invalid_data_Nationality_400_bad_request(String invalidNationality) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6352: AC2 Negative Test - Patch - invalid data Nationality - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("Nationality", invalidNationality);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains("nationality"), "Error message was not as expected");


        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ae", "12", "AEB", "!@"})
    public void invalid_country_of_birth_400_bad_request(String invalidCountryOfBirth) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6353: AC2 Negative Test - Patch - invalid country of birth - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("CountryOfBirth", invalidCountryOfBirth);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains("countryOfBirth"), "Error message was not as expected");


        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgbsdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdg", "12323", "!@£$%^&"})
    public void invalid_city_of_birth_400_bad_request(String invalidCityOfBirth) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6354: AC2 Negative Test - Patch - invalid city of birth - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("CityOfBirth", invalidCityOfBirth);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");

        assertEquals("REQUEST_VALIDATION", error.getCode());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"arabic", "english", "1232", "!@£$"})
    public void invalid_data_Language_400_bad_request(String invalidLanguage) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6355: AC2 Negative Test - Patch - invalid data Language - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("Language", invalidLanguage);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains(INVALID_FIELD), "Error message was not as expected");
        Assertions.assertTrue(error.getMessage().contains("language"), "Error message was not as expected");


        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgffddfdfdfadssfsdgdfjgghkjhgfdsdfghjkjhgfdsdfghjhgfdsdfgaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@ahb.com"
            , "!##$%^&**(@gmail.com", "12323", "!£$%^&"})
    public void invalid_data_Email_400_bad_request(String invalidEmail) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6356: AC2 Negative Test - Patch - invalid data Email - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("Email", invalidEmail);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains("email: must be a well-formed email address"), "Error message was not as expected");


        DONE();
    }

    @Test
    public void invalid_data_Email_null_400_bad_request() {

        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6357: AC2 Negative Test - Patch - invalid data Email - null - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("Email", "");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_BAD_REQUEST, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_0002);
        Assertions.assertTrue(error.getMessage().contains("Email must not be blank"), "Error message was not as expected");


        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"verified", "not_verified", "12323", "!@£$%^&"})
    public void invalid_data_EmailState_400_bad_request(String invalidEmailState) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6358: AC2 Negative Test - Patch - invalid data EmailState - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("EmailState", invalidEmailState);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-13-01", "AA-BB-ABCD", "09/05/1990"})
    public void invalid_data_TermsVersion_400_bad_request(String invalidTermsVersion) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6359: AC2 Negative Test - Patch - invalid data TermsVersion - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("TermsVersion", invalidTermsVersion);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "FALSE", "12323", "!@£$%^&"})
    public void invalid_data_TermsAccepted_400_bad_request(String invalidTermsAccepted) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6360: AC2 Negative Test - Patch - invalid data TermsAccepted - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("TermsAccepted", invalidTermsAccepted);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdfghjhgfdsdfghj", "12345678901234568"})
    public void invalid_data_BuildingNumber_400_bad_request(String invalidBuildingNumber) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6361: AC2 Negative Test - Patch - invalid data BuildingNumber - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject address = this.residentialAddressApi.createAddressJSON("BuildingNumber", invalidBuildingNumber);
        JSONObject customerDetails = this.residentialAddressApi.userProfileJSON(address, this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("buildingNumber: size must be between 1 and 16"), "Error message was not expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdfghjhgfdsdfg334556vjsahvcjhsdvcskdjbckdbcvksjdbvcksjdvbcksjdvbksjaaa", ""})
    public void invalid_data_StreetName_400_bad_request(String invalidStreetName) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6362: AC2 Negative Test - Patch - invalid data StreetName - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject address = this.residentialAddressApi.createAddressJSON("StreetName", invalidStreetName);
        JSONObject customerDetails = this.residentialAddressApi.userProfileJSON(address, this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("streetName: size must be between 1 and 70"), "Error message was not expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdfghjhgfdsdfg334556vjsahvcjhsdvcskdjbckdbcvksjdbvcksjdvbcksjdvbksjaaa", "123434544545454563764762578625376576"})
    public void invalid_data_CountrySubDivision_400_bad_request(String invalidCountrySubDivision) {

        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6363: AC2 Negative Test - Patch - invalid data CountrySubDivision - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject address = this.residentialAddressApi.createAddressJSON("CountrySubDivision", invalidCountrySubDivision);
        JSONObject customerDetails = this.residentialAddressApi.userProfileJSON(address, this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("countrySubDivision: size must be between 1 and 35"), "Error message was not expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ae", "12", "@#"})
    public void invalid_data_Country_400_bad_request(String invalidCountry) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6364: AC2 Negative Test - Patch - invalid data Country - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject address = this.residentialAddressApi.createAddressJSON("Country", invalidCountry);
        JSONObject customerDetails = this.residentialAddressApi.userProfileJSON(address, this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("Invalid Field"), "Error message was not expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefghijklmnopq", ""})
    public void invalid_data_PostalCode_400_bad_request(String invalidPostalCode) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6365: AC2 Negative Test - Patch - invalid data PostalCode - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject address = this.residentialAddressApi.createAddressJSON("PostalCode", invalidPostalCode);
        JSONObject customerDetails = this.residentialAddressApi.userProfileJSON(address, this.alphaTestUser);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("postalCode: size must be between 1 and 16"), "Error message was not expected");


        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"IDV COMPLETED", "IDV FAILED", "ASDFGHASDFGH", "idv_failed"})
    public void invalid_data_CustomerState_400_bad_request(String invalidCustomerState) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6366: AC2 Negative Test - Patch - invalid data CustomerState - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = this.customerApi.createJSONForUpdateCustomer("CustomerState", invalidCustomerState);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");

        DONE();
    }

    @Test
    public void invalid_data_AddressLine_400_bad_request() {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6367: AC2 Negative Test - Patch - invalid data AddressLine - 400 bad request");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.patchCustomerError(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(OBPartialPostalAddress6.builder()
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
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("addressLine: size must be between 0 and 3"), "Error message was not expected");

        DONE();
    }

    @Test
    public void missing_mandatory_data() {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 400 bad request");
        TEST("AHBDB-6368: AC2 Negative Test - Patch - missing mandatory data");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject customerDetails = new JSONObject();
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, customerDetails, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(ERROR_CODE_REQUEST_VALIDATION, error.getCode(), "Error codes do not match, expected: " + ERROR_CODE_REQUEST_VALIDATION);
        Assertions.assertTrue(error.getMessage().contains("data: must not be null"), "Error message was not expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"DateOfBirth", "PreferredName", "FirstName", "LastName", "FullName", "Gender",
            "Nationality", "CountryOfBirth", "CityOfBirth", "Language", "EmailState", "TermsVersion",
            "TermsAccepted", "Address", "CustomerState", "MobileNumber", "Email"})
    public void missing_optional_data(String fieldToRemove) {
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 200 response");
        TEST("AHBDB-8221: Positive Test - Patch - Missing optional data - Value: <Value> - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject details = this.customerApi.customerJsonRemoveField(this.alphaTestUser, fieldToRemove);
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, details, 200);
        THEN("We'll receive a 200 response");
        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"Department", "SubDepartment", "BuildingNumber", "StreetName", "AddressLine",
            "TownName", "PostalCode"})
    public void missing_optional_address_data(String fieldToRemove) {
        TEST("AHBDB-8266: Country and CountrySubDivision made mandatory");
        TEST("AHBDB-5218: AC2 Patch - missing or invalid data - 200 response");
        TEST("AHBDB-8222: Positive Test - Patch - Missing optional data for address - Value: <Value> - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to update a customer");
        JSONObject details = this.customerApi.customerJsonAddressRemoveField(this.alphaTestUser, fieldToRemove);
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, details, 200);
        THEN("We'll receive a 200 response");
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"IDV_COMPLETED",
            "IDV_FAILED",
            "IDV_REVIEW_REQUIRED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_APPROVED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_REJECTED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_IN_PROGRESS",
            "ACCOUNT_CREATION_RISK_REJECTION",
            "ACCOUNT_CREATION_IN_PROGRESS",
            "ACCOUNT_CREATED",
            "ACCOUNT_VERIFIED",
            "ACCOUNT_CREATION_BC_REVIEW_IN_PROGRESS",
            "ACCOUNT_CREATION_BC_REVIEW_APPROVED",
            "ACCOUNT_CREATION_BC_REVIEW_REJECTED",
            "ACCOUNT_CREATION_REVIEW_PARTIALLY_APPROVED",
            "ACCOUNT_CREATION_EMBOSS_NAME_SPECIFIED",
            "ACCOUNT_CREATION_CARD_DELIVERY_IN_PROGRESS",
            "SUSPENDED_UNDER_AGE",
            "IDV_REVIEW_REJECTED",
            "IDV_MANUAL_REVIEW_REQUIRED"})
    public void update_customer_state(String customerState) {
        envUtils.ignoreTestInEnv("Not available in NFT and above", Environments.NFT, Environments.STG);
        TEST("AHBDB-8653: New field to be added to the MobileAppState");
        setupTestUser();
        GIVEN("I want to update the customer state of a customer in CRM");
        WHEN("The platform attempts to update the customer in CRM");
        NOTE("Relates to a task for new customer states");
        OBWritePartialCustomer1 data = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.valueOf(customerState))
                        .build())
                .build();
        THEN("The platform returns a 200 OK");
        this.customerApi.updateCustomer(alphaTestUser, data, 200);
        AND("The details in CRM are persisted");
        OBReadCustomer1 getCustomer = this.customerApi.getCurrentCustomer(alphaTestUser);
        Assertions.assertEquals(OBCustomerStateV1.valueOf(customerState),
                getCustomer.getData().getCustomer().get(0).getCustomerState());
        DONE();
    }


    @Test
    public void patch_adult_to_less_then_18_years() {
        TEST("DEFECT AHBDB-10066: Unable to PATCH Customer");

        GIVEN("An adult is onboarded");
        setupTestUser();

        OBReadCustomer1 getResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
        assertEquals("21+", getResponse.getData().getCustomer().get(0).getAgeGroup());

        WHEN("PATCH with null values");
        OBWritePartialCustomer1 patchDetails = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .build())
                .build();

        THEN("A 200 response is returned");
        OBWriteCustomerResponse1 patchResponse = this.customerApi.patchCustomerSuccess(this.alphaTestUser, patchDetails);
        assertNotNull(patchResponse);

        WHEN("PATCH an adult to be less then 18 years old");
        JSONObject patchDetails2 = this.customerApi.createJSONForUpdateCustomer("DateOfBirth", LocalDate.now().minusYears(15).toString());

        OBErrorResponse1 patchResponse2 = this.customerApi.updateCustomerUsingJSONErrorResponse(this.alphaTestUser, patchDetails2, 400);
        THEN("A 400 response is returned");
        assertEquals(ERROR_BAD_REQUEST, patchResponse2.getCode());
        assertEquals(PATCH_UNDER_18_ERROR_MESSAGE, patchResponse2.getMessage());

        DONE();
    }

    @Test
    public void patch_address_with_missing_optional_address_fields() {

        TEST("AHBDB-8266: POST /internal/v2/customers return 400 when Optional Fields are null");
        NOTE("Country and CountrySubDivision were made mandatory in the POST call but not mandatory in the PATCH for a customer");
        setupTestUser();
        GIVEN("A customer has their Country and CountrySubDivision fields populated");
        OBReadCustomer1 getDetails = customerApi.getCurrentCustomer(alphaTestUser);
        assertNotNull(getDetails.getData().getCustomer().get(0).getAddress().getCountry());
        assertNotNull(getDetails.getData().getCustomer().get(0).getAddress().getCountrySubDivision());
        WHEN("A client attempts to update the address details of a customer");
        AND("They do not include these two fields");
        String randomTownName = generateRandomTownName();

        OBWritePartialCustomer1 update = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(OBPartialPostalAddress6.builder().townName(randomTownName).build())
                        .build())
                .build();

        customerApi.patchCustomerSuccess(alphaTestUser, update);
        THEN("The platform will return a 200 Response");
        AND("The details will be persisted");
        OBReadCustomer1 getAgain = customerApi.getCurrentCustomer(alphaTestUser);

        assertEquals(randomTownName, getAgain.getData().getCustomer().get(0).getAddress().getTownName());
        DONE();
    }

    @Test
    public void positive_test_user_can_update_city_of_birth_with_a_single_character() {
        TEST("AHBDB-13255: SIT | Update Customer failing for field CityOfBirth with values as single character");
        setupTestUser();

        GIVEN("The client wants to update the customer's details");
        OBWritePartialCustomer1 obWritePartialCustomer1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .cityOfBirth("s")
                        .countryOfBirth("BB")
                        .build())
                .build();

        WHEN("They want to update cityOfBirth to a single character");
        OBWriteCustomerResponse1 success = customerApi.patchCustomerSuccess(alphaTestUser, obWritePartialCustomer1);
        assertEquals(alphaTestUser.getUserId(), success.getData().getCustomerId().toString());

        THEN("The platform will save the details");
        OBCustomer1 getCustomer = customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0);

        assertEquals("s", getCustomer.getCityOfBirth());
        assertEquals("BB", getCustomer.getCountryOfBirth());

        DONE();
    }

    @Test
    public void positive_test_user_can_update_their_details_with_their_own_email() {
        envUtils.ignoreTestInEnv("AHBDB-13442", Environments.SIT, Environments.NFT);
        TEST("AHBDB-13442");
        setupTestUser();
        GIVEN("A client wants to update the details of customer");
        OBCustomer1 customerDetails = customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0);

        String email = customerDetails.getEmail();

        OBWritePartialCustomer1 obWritePartialCustomer1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .email(email)
                        .build())
                .build();
        WHEN("They update the email field with their existing email");
        OBWriteCustomerResponse1 success = customerApi.patchCustomerSuccess(alphaTestUser, obWritePartialCustomer1);
        assertEquals(alphaTestUser.getUserId(), success.getData().getCustomerId().toString());

        THEN("The platform will return a 200 Response");

        DONE();
    }
}
