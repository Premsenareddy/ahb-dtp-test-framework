package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

@Tag("AHBDB-6998")
@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildFATCATests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private FatcaApiV2 fatcaApiV2;

    @Inject
    private RelationshipApi relationshipApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private String connectionId;
    private String childId;
    private String fullName = "testUser";

    /*
        Tests GET and POST for /internal/v2/relationships/{relationshipId}/customers/fatca
     */

    private void setupTestUsers() {
        if (this.alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
            alphaTestUserChild = new AlphaTestUser();

            alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            childId = alphaTestUserFactory.createChildInForgerock(alphaTestUser);
            connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUser,
                    alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));

            alphaTestUserChild =
                    alphaTestUserFactory.createChildCustomer(alphaTestUser, alphaTestUserChild, connectionId, childId);
        }
    }

    @Order(1)
    @Test
    public void get_existing_relationships_from_CRM() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11450: AC1 - Get existing relationships from CRM");
        setupTestUsers();
        GIVEN("A client wants to check the list of relationships for a parent");
        WHEN("The client sends a request to retrieve the list of relationships");

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);

        THEN("The platform returns a 200 OK");
        AND("The existing customer relationships for that parent is returned");

        assertNotNull(relationships);
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(childId, relationships.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        DONE();
    }

    @Order(1)
    @Test
    public void happy_path_create_and_get_fatca_details_201_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11451: FATCA - AC1, AC3 - Create and Get FATCA details");
        setupTestUsers();
        String ssn = "987654321";
        String federalTaxClassification = "Individual/sole proprietor or single-member LLC";

        GIVEN("The customer has provided their child's FATCA details");
        WHEN("The client sends the valid FATCA details");
        OBWriteFatca1 fatca = OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .usCitizenOrResident(false)
                        .ssn(ssn)
                        .form(OBFatcaForm1.W9)
                        .federalTaxClassification(federalTaxClassification)
                        .build())
                .build();

        THEN("The details will be stored in CRM");
        AND("The platform returns a 201 Created");
        OBWriteFatcaResponse1 fatcaResponse = this.fatcaApiV2.createFatcaDetailsChild(alphaTestUser, fatca, connectionId);
        OBWriteFatcaDetails1 postResponse = fatcaResponse.getData();
        assertNotNull(fatcaResponse);
        assertEquals(false, postResponse.getUsCitizenOrResident());
        assertEquals(OBFatcaForm1.W9, postResponse.getForm());
        assertEquals(ssn, postResponse.getSsn());
        assertEquals(federalTaxClassification, postResponse.getFederalTaxClassification());
        DONE();

        GIVEN("A child exists in CRM with existing FATCA details");
        WHEN("The client attempts to retrieve the FATCA details for the child");
        OBReadFatca1 getResponse =
                this.fatcaApiV2.getFatcaDetails(alphaTestUserChild);
        this.fatcaApiV2.getFatcaDetailsChild(alphaTestUser, connectionId);

        assertNotNull(getResponse);

        THEN("The platform will return a 200 OK");
        AND("The correct FATCA details are returned");
        OBWriteFatcaDetails1 data = getResponse.getData();

        assertEquals(false, data.getUsCitizenOrResident());
        assertEquals(OBFatcaForm1.W9, data.getForm());
        assertEquals(ssn, data.getSsn());
        assertEquals(federalTaxClassification, data.getFederalTaxClassification());
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"Ssn", "FederalTaxClassification"})
    public void positive_test_missing_optional_fields_200_response(String removeOptionalField) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11460: FATCA - Positive Test - Missing optional fields");
        setupTestUsers();

        GIVEN("The client wants to store FATCA details against a child customer");
        AND("The details have missing optional fields");

        JSONObject data = new JSONObject();
        JSONObject fatca = new JSONObject() {
            {
                put("Form", "W9");
                put("UsCitizenOrResident", "false");
                put("Ssn", "123456789");
                put("FederalTaxClassification", "Individual/sole proprietor or single-member LLC");
            }
        };
        fatca.remove(removeOptionalField);
        data.put("Data", fatca);

        WHEN("The client sends the FATCA details");

        this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, data, connectionId, 201);

        THEN("The platform will return a 201");
        AND("The details are persisted");
        this.fatcaApiV2.getFatcaDetailsChild(alphaTestUser, connectionId);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"Form", "UsCitizenOrResident"})
    public void negative_test_invalid_fatca_details_missing_mandatory_fields_400_response(String removeMandatoryField) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11452: FATCA - AC2 - Missing mandatory fields");
        setupTestUsers();

        GIVEN("The client wants to store FATCA details against a child customer");
        AND("The details do not satisfy the validation in the data table");

        JSONObject data = new JSONObject();
        JSONObject fatca = new JSONObject() {
            {
                put("Form", "W9");
                put("UsCitizenOrResident", "false");
                put("Ssn", "123456789");
                put("FederalTaxClassification", "Individual/sole proprietor or single-member LLC");
            }
        };
        fatca.remove(removeMandatoryField);
        data.put("Data", fatca);

        WHEN("The client sends the invalid FATCA details");

        OBErrorResponse1 error =
                this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, data, connectionId, 400).as(OBErrorResponse1.class);

        THEN("The platform will return a 400 Bad Request");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "asdasd"})
    public void negative_test_invalid_fatca_details_invalid_form_400_response(String invalidForm) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11453: FATCA - AC2 - Invalid Form details");
        setupTestUsers();

        GIVEN("The client wants to store FATCA details against a child customer");
        AND("The details do not satisfy the validation in the data table");

        JSONObject fatca = this.fatcaApiV2.fatcaJSON(invalidForm, "false",
                "123456789", "Individual/sole proprietor or single-member LLC");

        WHEN("The client sends the invalid FATCA details");

        OBErrorResponse1 error =
                this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, fatca, connectionId, 400).as(OBErrorResponse1.class);

        THEN("The platform will return a 400 Bad Request");

        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "asdasd"})
    public void negative_test_invalid_fatca_details_invalid_us_citizen_or_resident_400_response(String invalidCitizenBoolean) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11453: FATCA - AC2 - Invalid USCitizenOrResident details");
        setupTestUsers();

        GIVEN("The client wants to store FATCA details against a child customer");
        AND("The details do not satisfy the validation in the data table");

        JSONObject fatca = this.fatcaApiV2.fatcaJSON("W9", invalidCitizenBoolean,
                "123456789", "Individual/sole proprietor or single-member LLC");

        WHEN("The client sends the invalid FATCA details");

        OBErrorResponse1 error =
                this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, fatca, connectionId, 400).as(OBErrorResponse1.class);

        THEN("The platform will return a 400 Bad Request");

        assertNotNull(error);

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "asdasd", "12345678", "1234567890", "!@£$%^&*("})
    public void negative_test_invalid_fatca_details_invalid_ssn_400_response(String invalidSsn) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11453: FATCA - AC2 - Invalid SSN details");
        setupTestUsers();

        GIVEN("The client wants to store FATCA details against a child customer");
        AND("The details do not satisfy the validation in the data table");

        JSONObject fatca = this.fatcaApiV2.fatcaJSON("W9", "false",
                invalidSsn, "Individual/sole proprietor or single-member LLC");

        WHEN("The client sends the invalid FATCA details");

        OBErrorResponse1 error =
                this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, fatca, connectionId, 400).as(OBErrorResponse1.class);

        THEN("The platform will return a 400 Bad Request");

        assertNotNull(error);

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "asdasd", "12345678", "!@£$%^&*(", "fievwoknehogsefbjrgkstdnvcfycauebbsyuqxjqjvslnidyyeumnohttxxtyqonpnmwkgrrzjrracsmdpvjbgbdycyssksoguoy"})
    public void negative_test_invalid_fatca_details_invalid_tax_classification_400_response(String invalidTaxClassification) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11453: FATCA - AC2 - Invalid Tax Classification details");
        setupTestUsers();

        GIVEN("The client wants to store FATCA details against a child customer");
        AND("The details do not satisfy the validation in the data table");

        JSONObject fatca = this.fatcaApiV2
                .fatcaJSON("W9", "false", "123456789", invalidTaxClassification);

        WHEN("The client sends the invalid FATCA details");

        OBErrorResponse1 error =
                this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, fatca, connectionId, 400).as(OBErrorResponse1.class);

        THEN("The platform will return a 400 Bad Request");

        assertNotNull(error);

        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_post_missing_data_400_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11458: FATCA - Negative Test - Null Body - 400 Response");
        setupTestUsers();
        GIVEN("A client wants to update their FATCA details");
        JSONObject fatca = this.fatcaApiV2.fatcaJSON("W9", "false",
                "123456789", "Individual/sole proprietor or single-member LLC");
        fatca.remove("Data");
        WHEN("The client sends a request");
        AND("The body is missing");
        this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, fatca, connectionId, 400);
        THEN("The platform will respond with a 400 Response");
        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_get_invalid_connection_id_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11454: FATCA - AC4 - Customer not found - 404 Response");
        setupTestUsers();

        GIVEN("A client wants to query for FATCA details");

        WHEN("The client attempts to retrieve the applicant's FATCA information with a connectionId that does not exist");

        OBErrorResponse1 error =
                this.fatcaApiV2.getFatcaDetailsChildError(alphaTestUser, UUID.randomUUID().toString(), 404);

        THEN("The platform will return a 404 Not Found");

        assertNotNull(error);

        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_post_invalid_connection_id_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11454: FATCA - AC4 - Customer not found - 404 Response");
        setupTestUsers();

        GIVEN("A client wants to query for FATCA details");

        WHEN("The client attempts to retrieve the applicant's FATCA information with a connectionId that does not exist");

        JSONObject fatca = this.fatcaApiV2
                .fatcaJSON("W9", "false", "123456789", "Individual/sole proprietor or single-member LLC");

        OBErrorResponse1 error =
                this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, fatca, UUID.randomUUID().toString(), 404).as(OBErrorResponse1.class);

        THEN("The platform will return a 404 Not Found");

        assertNotNull(error);

        DONE();
    }

    @Order(100)
    @Test
    public void negative_test_get_and_post_customer_does_not_exist_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11454: FATCA - AC4 - Customer Not Found - 404 Response");
        setupTestUsers();

        GIVEN("A client wants to query for FATCA details");
        WHEN("The client attempts to retrieve the applicant's FATCA information with a connectionId that does not exist");

        this.customerApiV2.deleteCustomer(alphaTestUserChild);
        JSONObject fatca = this.fatcaApiV2
                .fatcaJSON("W9", "false", "123456789", "Individual/sole proprietor or single-member LLC");

        this.fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser, fatca, connectionId, 404);
        this.fatcaApiV2.getFatcaDetailsChildError(alphaTestUser, connectionId, 404);

        THEN("The platform will return a 404 Not Found");
        DONE();
    }
}
