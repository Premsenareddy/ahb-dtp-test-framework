package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTermResponse1;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTerm1;

import javax.inject.Inject;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.customer.api.customer.model.OBTermType.MARKETING;
import static uk.co.deloitte.banking.customer.api.customer.model.OBTermType.REGISTRATION;

@Tag("BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdateTermsCRMTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Test
    public void update_terms_successfully_valid_terms_version() {
        TEST("AHBDB-219 user can update their terms with a valid terms version");
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5742: AC1 Positive Test - Happy Path Scenario - Post Terms in CRM - 201 Created");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");
        WHEN("I try to update terms with a valid version");

        OBWriteTerm1 obWriteTerm1 = OBWriteTerm1.builder()
                .termsAccepted(true)
                .termsAcceptedDate(OffsetDateTime.parse("2021-02-12T12:30:24+04:00"))
                .termsVersion(LocalDate.parse("2021-08-12"))
                .type(REGISTRATION)
                .build();

        THEN("The terms are updated and 200 response is returned");

        OBWriteTermResponse1 response = this.customerApi.postCustomerTerms(alphaTestUser, obWriteTerm1);
        assertEquals(true, response.getData().getTermsAccepted());
        assertEquals(LocalDate.parse("2021-08-12"), response.getData().getTermsVersion());
        assertEquals(REGISTRATION.toString(), response.getData().getType().toString());
        DONE();
    }

    @Test
    public void patch_terms_successfully_valid_terms_version() {
        TEST("AHBDB-16562 New consent fields at registration");
        TEST("AHBDB-16561: [CRM] API - Update Terms");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");
        WHEN("I try to patch terms accepted");

        OBWriteTerm1 obWriteTerm1 = OBWriteTerm1.builder()
                .termsAccepted(true)
                .termsAcceptedDate(OffsetDateTime.parse("2021-08-12T12:30:24+04:00"))
                .termsVersion(LocalDate.parse("2021-02-27"))
                .type(MARKETING)
                .build();

        THEN("The terms are updated and 200 response is returned");
        this.customerApi.postCustomerTerms(alphaTestUser, obWriteTerm1);
        obWriteTerm1.setTermsAccepted(false);
        obWriteTerm1.setTermsAcceptedDate(OffsetDateTime.parse("2021-09-30T12:30:24+04:00"));
        obWriteTerm1.setTermsVersion(LocalDate.parse("2021-09-29"));
        OBWriteTermResponse1 response = this.customerApi.patchCustomerTerms(alphaTestUser, obWriteTerm1);
        assertEquals(false, response.getData().getTermsAccepted());
        assertEquals(MARKETING.toString(), response.getData().getType().toString());
        assertEquals(LocalDate.parse("2021-09-29"), response.getData().getTermsVersion());
        DONE();

    }

    @Test
    public void negative_test_update_terms_invalid_terms_accepted() {
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5750: AC3 Negative Test - Post - invalid data TermsAccepted - TermsAccepted: <TermsAccepted> - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs on terms accepted");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", RandomDataGenerator.generateRandomString());
        jsonObject.put("TermsAcceptedDate", "869077230450");
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        jsonObject.put("Type", "REGISTRATION");
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @Test
    public void negative_test_update_terms_terms_accepted_false() {
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5750: AC3 Negative Test - Post - invalid data TermsAccepted - TermsAccepted: <TermsAccepted> - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but with a terms accepted value of false");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", false);
        jsonObject.put("TermsAcceptedDate", "869077230450");
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        jsonObject.put("Type", "REGISTRATION");
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @Test
    public void negative_test_update_terms_invalid_terms_accepted_date_random() {
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5751: AC3 Negative Test - Post - invalid data TermsAcceptedDate - TermsAcceptedDate: <TermsAcceptedDate> - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs on terms accepted date");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", true);
        jsonObject.put("TermsAcceptedDate", RandomDataGenerator.generateRandomString());
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        jsonObject.put("Type", "REGISTRATION");
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"3093720947365", "dhgnrhcosnrb", "", "_+{}[],.<>/?","1996-13-14T20:30:24+04:001",
            "90-05-09T20:30:24+04:00", "1996-08-14T20:30:24+AA:00", "1996-08-14T20:65:24+04:00"})
    public void negative_test_update_terms_invalid_terms_accepted_date_list(String invalidTermsAcceptedDate)
            throws JSONException {
        TEST("AHBDB-219 user tries to update their terms with an invalid time stamp");
        TEST("AHBDB-5930: Field accepts OffSetDateTime and is converted to TimeStamp -- removed valid values from list");
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5751: AC3 Negative Test - Post - invalid data TermsAcceptedDate - TermsAcceptedDate - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs on terms accepted date");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", true);
        jsonObject.put("TermsAcceptedDate", invalidTermsAcceptedDate);
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        jsonObject.put("Type", "REGISTRATION");
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-13-01", "AA-BB-ABCD", "00-00-0000", "09/05/1990", ""})
    public void negative_test_update_terms_invalid_version(String invalidTermsVersion) throws JSONException {
        TEST("AHBDB-219 user tries to update their terms with an invalid version");
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5752: AC3 Negative Test - Post - invalid data TermsVersion - TermsVersion: <TermsVersion> - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs on terms version");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", true);
        jsonObject.put("TermsAcceptedDate", "869077230450");
        jsonObject.put("TermsVersion", invalidTermsVersion);
        jsonObject.put("Type", "REGISTRATION");
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"registration", "banking", "1234455", ""})
    public void negative_test_update_terms_invalid_data_type(String invalidType) throws JSONException {
        TEST("AHBDB-219 user tries to update their terms with an invalid time type");
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5753: AC3 Negative Test - Post - invalid data Type - Type: <Type> - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs on data type");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", true);
        jsonObject.put("TermsAcceptedDate", "869077230450");
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        jsonObject.put("Type", invalidType);
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @Test
    public void negative_test_update_terms_missing_data_type() throws JSONException {
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5754: AC3 Negative Test - Post - Missing Required Parameters - Value: <Value> 400 bad request");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing field inputs for data type");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", true);
        jsonObject.put("TermsAcceptedDate", "869077230450");
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @Test
    public void negative_test_update_terms_missing_terms_version() throws JSONException {
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5754: AC3 Negative Test - Post - Missing Required Parameters - Value: <Value> 400 bad request");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing field inputs for terms version");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", true);
        jsonObject.put("TermsAcceptedDate", "869077230450");
        jsonObject.put("Type", "REGISTRATION");
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @Test
    public void negative_test_update_terms_missing_terms_accepted_date() throws JSONException {
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5754: AC3 Negative Test - Post - Missing Required Parameters - Value: <Value> 400 bad request");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing field inputs for terms accepted date");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAccepted", true);
        jsonObject.put("Type", "REGISTRATION");
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }

    @Test
    public void negative_test_update_terms_missing_terms_accepted() throws JSONException {
        TEST("AHBDB-3688: [CRM] API - Post Terms");
        TEST("AHBDB-5754: AC3 Negative Test - Post - Missing Required Parameters - Value: <Value> 400 bad request");
        setupTestUser();
        GIVEN("We have received a post request from the client to store T&Cs");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing field inputs for terms accepted");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TermsAcceptedDate", "869077230450");
        jsonObject.put("Type", "REGISTRATION");
        jsonObject.put("TermsVersion", LocalDate.parse("2021-04-09"));
        THEN("We’ll receive a 400 bad request ");
        this.customerApi.postCustomerTermsError(alphaTestUser, jsonObject, 400);
        DONE();
    }
}
