package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import io.restassured.response.Response;
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
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBReadLocationResponse1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;

@Tag("AHBDB-6998")
@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildAddressTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private LocationsApiV2 locationsApiV2;

    private static final String CONNECTION_ID_NOT_FOUND = "UAE.ERROR.NOT_FOUND";

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserFresh;
    private AlphaTestUser alphaTestUserNegative;

    private String randomUUID = UUID.randomUUID().toString();
    private String childId;
    private String connectionId;
    private String validLocationId = null;

    private String childIdFresh;
    private String connectionIdFresh;

    private String childIdNegative;
    private String connectionIdNegative;

    /*
        Tests GET, POST and PUT for /internal/v2/relationships/{relationshipId}/customers/locations
        Tests DELETE for /internal/v2/relationships/{relationshipId}/customers/locations/{locationId}
     */

    private void setupTestUsers() {
        if (this.alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

            childId = alphaTestUserFactory.createChildInForgerock(alphaTestUser);
            connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUser,
                    alphaTestUserFactory.generateDependantBody(childId, 15, "testUser", OBGender.MALE, OBRelationshipRole.FATHER));
        }
    }

    private void setupTestUsersFresh() {
        alphaTestUserFresh = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

        childIdFresh = alphaTestUserFactory.createChildInForgerock(alphaTestUserFresh);

        connectionIdFresh = alphaTestUserFactory.createChildInCRM(alphaTestUserFresh, alphaTestUserFactory
                .generateDependantBody(childIdFresh, 15, "testUser", OBGender.MALE, OBRelationshipRole.FATHER));
    }

    private void setupTestUsersNegative() {
        if (this.alphaTestUserNegative == null) {
            alphaTestUserNegative = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

            childIdNegative = alphaTestUserFactory.createChildInForgerock(alphaTestUserNegative);

            connectionIdNegative = alphaTestUserFactory.createChildInCRM(alphaTestUserNegative, alphaTestUserFactory
                    .generateDependantBody(childIdNegative, 15, "testUser", OBGender.MALE, OBRelationshipRole.FATHER));

            OBLocationDetails1 location = OBLocationDetails1.builder()
                    .name("Work Address")
                    .address(OBLocationAddress1.builder()
                            .department("Palm Square")
                            .subDepartment("36a")
                            .buildingNumber("16")
                            .streetName("Sunbay Area")
                            .addressLine(Collections.singletonList("Dubai Mall"))
                            .townName("Dubai")
                            .countrySubDivision("Abu Dhabi")
                            .country("AE")
                            .postalCode("P.O. Box 64235")
                            .build())
                    .build();

            this.locationsApiV2.createLocationDetailsChild(alphaTestUserNegative, location, connectionIdNegative);
        }
    }

    @Order(1)
    @Test
    public void happy_path_create_retrieve_update_delete_additional_addresses() {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11548: ADDRESS - AC1, AC2, AC4, AC5 - Create, Retrieve, Update and Delete Additional Address");
        setupTestUsers();
        GIVEN("The customer has provided an additional address for the child");

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        WHEN("The client sends a valid additional address");
        OBReadLocationResponse1 postLocationResponse =
                this.locationsApiV2.createLocationDetailsChild(alphaTestUser, location, connectionId);

        THEN("The platform will return a 201 Response");
        assertNotNull(postLocationResponse);
        AND("Will return the list of addresses");
        assertFalse(postLocationResponse.getData().isEmpty());

        OBReadLocationResponse1 getResult1 =
                this.locationsApiV2.getLocationsDetailsChild(alphaTestUser, connectionId);

        OBLocationDetails1 locationDetails1 = getResult1.getData().get(0);

        assertNotNull(locationDetails1);
        assertEquals("Work Address", locationDetails1.getName());

        assertEquals(Collections.singletonList("Dubai Mall"), locationDetails1.getAddress().getAddressLine());
        assertEquals("Abu Dhabi", locationDetails1.getAddress().getCountrySubDivision());
        assertEquals("P.O. Box 64235", locationDetails1.getAddress().getPostalCode());
        assertEquals("Sunbay Area", locationDetails1.getAddress().getStreetName());
        assertEquals("Palm Square", locationDetails1.getAddress().getDepartment());
        assertEquals("36a", locationDetails1.getAddress().getSubDepartment());
        assertEquals("16", locationDetails1.getAddress().getBuildingNumber());
        assertEquals("Dubai", locationDetails1.getAddress().getTownName());
        assertEquals("AE", locationDetails1.getAddress().getCountry());

        String locationsId = locationDetails1.getId();
        DONE();

//        Update additional addresses
        GIVEN("The customer has provided an additional address for the child");
        AND("Wants to update the address");

        OBLocationDetails1 updateLocation = OBLocationDetails1.builder()
                .id(locationsId)
                .name("Work Address1")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square1")
                        .subDepartment("36a1")
                        .buildingNumber("161")
                        .streetName("Sunbay Area1")
                        .addressLine(Collections.singletonList("Dubai Mall1"))
                        .townName("Dubai1")
                        .countrySubDivision("Umm Al Quwain")
                        .country("AE")
                        .postalCode("P.O. Box 642351")
                        .build())
                .build();

        WHEN("The client updates the customer profile with a valid additional address");
        OBReadLocationResponse1 updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChild(alphaTestUser, updateLocation, connectionId);

        THEN("The platform will return a 200 Response");
        assertNotNull(updateLocationResponse);
        AND("Will return the list of addresses");
        assertFalse(updateLocationResponse.getData().isEmpty());
        DONE();

//        Retrieve additional addresses
        GIVEN("A child exists with a list of addresses");
        OBReadLocationResponse1 getResult2 = this.locationsApiV2.getLocationsDetailsChild(alphaTestUser, connectionId);
        OBLocationDetails1 locationDetails2 = getResult2.getData().get(0);
        WHEN("The client attempts to retrieve the customer's address list with a valid JWT token");
        THEN("The platform will return a 200 Response");

        assertNotNull(getResult2);
        assertNotNull(locationDetails1);
        assertEquals("Work Address1", locationDetails2.getName());

        assertEquals(Collections.singletonList("Dubai Mall1"), locationDetails2.getAddress().getAddressLine());
        assertEquals("Umm Al Quwain", locationDetails2.getAddress().getCountrySubDivision());
        assertEquals("P.O. Box 642351", locationDetails2.getAddress().getPostalCode());
        assertEquals("Sunbay Area1", locationDetails2.getAddress().getStreetName());
        assertEquals("Palm Square1", locationDetails2.getAddress().getDepartment());
        assertEquals("36a1", locationDetails2.getAddress().getSubDepartment());
        assertEquals("161", locationDetails2.getAddress().getBuildingNumber());
        assertEquals("Dubai1", locationDetails2.getAddress().getTownName());
        assertEquals("AE", locationDetails2.getAddress().getCountry());

        AND("Will return the list of addresses");
        assertFalse(getResult2.getData().isEmpty());
        DONE();

//        Delete additional addresses
        GIVEN("A child exists with an additional address");
        OBReadLocationResponse1 getResult3 = this.locationsApiV2.getLocationsDetailsChild(alphaTestUser, connectionId);
        assertFalse(getResult3.getData().isEmpty());

        OBLocationDetails1 newLocationToAdd = OBLocationDetails1.builder()
                .name("Country House")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank")
                        .subDepartment("50b")
                        .buildingNumber("10")
                        .streetName("Al Marakazi ya")
                        .townName("Dubai")
                        .countrySubDivision("Sharjah")
                        .country("AE")
                        .build())
                .build();

        OBReadLocationResponse1 secondAddressResponse =
                this.locationsApiV2.createLocationDetailsChild(alphaTestUser, newLocationToAdd, connectionId);
        assertNotNull(secondAddressResponse);

        OBReadLocationResponse1 getResult4 = this.locationsApiV2.getLocationsDetailsChild(alphaTestUser, connectionId);
        assertTrue(getResult4.getData().size() == 2);

        String locationId1 = getResult4.getData().get(0).getId();
        String locationId2 = getResult4.getData().get(1).getId();
        validLocationId = locationId2;

        WHEN("The client calls delete on the address with a valid JWT token");
        OBReadLocationResponse1 deleteResponse =
                this.locationsApiV2.deleteLocationDetailsChild(alphaTestUser, locationId1, connectionId);
        assertTrue(deleteResponse.getData().size() == 1);
        assertEquals(locationId2, deleteResponse.getData().get(0).getId());
        THEN("The platform will respond with a 200");
        AND("The address is deleted from the address list");
        AND("The remaining list of addresses is returned");
        DONE();
    }

    @Order(3)
    @Test
    public void positive_test_post_and_put_multiple_additional_addresses() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11548: Addresses - Positive Test - Post Multiple additional addresses");
        TEST("AHBDB-11548: Addresses - Positive Test - Put with Multiple additional addresses");
        setupTestUsersFresh();
        GIVEN("A child has an existing address in their list of addresses");

        OBLocationDetails1 location1 = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBReadLocationResponse1 postLocationResponse =
                this.locationsApiV2.createLocationDetailsChild(alphaTestUserFresh, location1, connectionIdFresh);
        assertEquals(1, postLocationResponse.getData().size());

        WHEN("A client wants to create another address in the list");
        OBLocationDetails1 location2 = OBLocationDetails1.builder()
                .name("Country House")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank")
                        .subDepartment("50b")
                        .buildingNumber("10")
                        .streetName("Al Marakazi ya")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .build())
                .build();

        OBReadLocationResponse1 postLocationResponse2 =
                this.locationsApiV2.createLocationDetailsChild(alphaTestUserFresh, location2, connectionIdFresh);
        assertEquals(2, postLocationResponse2.getData().size());

        String locationId = postLocationResponse2.getData().get(0).getId();

        THEN("The platform will respond with a 201");
        AND("The new location details are persisted");
        DONE();

        GIVEN("A child has 2 or more additional addresses");
        OBLocationDetails1 location3 = OBLocationDetails1.builder()
                .id(locationId)
                .name("Country House")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank")
                        .subDepartment("50b")
                        .buildingNumber("10")
                        .streetName("Al Marakazi ya")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .build())
                .build();
        WHEN("A client attempts to PUT an additional address");
        OBReadLocationResponse1 putResponse =
                this.locationsApiV2.updateLocationDetailsChild(alphaTestUserFresh, location3, connectionIdFresh);
        assertEquals(2, postLocationResponse2.getData().size());
        THEN("The platform will respond with a 200 Response");
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"Name", "Address"})
    public void negative_test_post_missing_mandatory_data_fields_400_response(String missingMandatoryDataFieldPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11552: ADDRESS - Negative Test - Missing Mandatory Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBody(missingMandatoryDataFieldPost, null);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);

        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"StreetName", "TownName", "CountrySubDivision", "Country"})
    public void negative_test_post_missing_mandatory_address_fields_400_response(String missingMandatoryAddressFieldPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11552: ADDRESS - AC3 - Negative Test - Missing Mandatory Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress(missingMandatoryAddressFieldPost, null);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"Department", "SubDepartment", "BuildingNumber", "AddressLine", "PostalCode"})
    public void positive_test_post_missing_optional_address_fields_201_response(String missingOptionalAddressFieldPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11556: ADDRESS - AC3 - Positive Test - Missing Optional Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress(missingOptionalAddressFieldPost, null);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 201);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 201 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "N4Xe6Ffc9GgyEE1Z9hkV7mGNjemsOobwGbah8EmJlyu4lp4vbxxXfUAhsKp0KmDuQm0UJzjG7hMnar1UlofzJ3XA1QUCg7Y1RgWYitn24dDvvbnXJs8WV3aarlA67xRD6MGYPxDC06dWv1wPcA3QjKUroQ7sDHH7gYzrMqXNM0lBe24weAAKr3CdhoVX8GcrbxapEodlbS1tu3ybOBGK7yXbY3xs37cFpSduZrSWn4MZ0KbH6R6TivWdDRrO4C5m"})
    public void negative_test_post_invalid_name_400_response(String invalidAddressLabelNamePost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBody("Name", invalidAddressLabelNamePost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "kgktnaoljhqxegnvysxrbyfshbaqftcnfdfpyopqjwalxjsplqomjwvhnsbtgirruitsjuaxsrptniqjxjhvwywzszoqdfkndwzmrhgflbucmknflprxqqaeaoqvqggjlmhuyrokurbzcywcwrjpiormkjbmkyndmgnspppptwofhcuhkzcveucfjgwfmpjptfvvcrkuzqjuoydelxepkswxanacyhbnzdpliklgoztmtwdsdlqsjrpefpearja"})
    public void negative_test_post_invalid_department_400_response(String invalidDepartmentPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("Department", invalidDepartmentPost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "kgktnaoljhqxegnvysxrbyfshbaqftcnfdfpyopqjwalxjsplqomjwvhnsbtgirruitsjuaxsrptniqjxjhvwywzszoqdfkndwzmrhgflbucmknflprxqqaeaoqvqggjlmhuyrokurbzcywcwrjpiormkjbmkyndmgnspppptwofhcuhkzcveucfjgwfmpjptfvvcrkuzqjuoydelxepkswxanacyhbnzdpliklgoztmtwdsdlqsjrpefpearja"})
    public void negative_test_post_invalid_sub_department_400_response(String invalidSubDepartmentPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("SubDepartment", invalidSubDepartmentPost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "yjlusfjevgesiarqo"})
    public void negative_test_post_invalid_building_number_400_response(String invalidBuildingNumberPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("BuildingNumber", invalidBuildingNumberPost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "henekqwmnkpqkejopmocewmfqjkihzbkzejoramwhwyxmaixagcvotvyfqytrjdfuyobybr"})
    public void negative_test_post_invalid_street_name_400_response(String invalidStreetNamePost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("StreetName", invalidStreetNamePost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_post_invalid_address_line_400_response() {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");
        List addressLine = new ArrayList() {
            {
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
            }
        };

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(addressLine)
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBErrorResponse1 error =
                this.locationsApiV2.createLocationDetailsChildError(alphaTestUser, location, connectionId, 400);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "kgktnaoljhqxegnvysxrbyfshbaqftcnfdfpyopqjwalxjsplqomjwvhnsbtgirruitsjuaxsrptniqjxjhvwywzszoqdfkndwzmrhgflbucmknflprxqqaeaoqvqggjlmhuyrokurbzcywcwrjpiormkjbmkyndmgnspppptwofhcuhkzcveucfjgwfmpjptfvvcrkuzqjuoydelxepkswxanacyhbnzdpliklgoztmtwdsdlqsjrpefpearja"})
    public void negative_test_post_invalid_town_name_400_response(String invalidTownNamePost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("TownName", invalidTownNamePost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "muktjdkwskokzgkyqssdnnawnlmvffwdoxag"})
    public void negative_test_post_invalid_country_sub_division_400_response(String invalidCountrySubDivisionPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("CountrySubDivision", invalidCountrySubDivisionPost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "AEE", "!@", "ae"})
    public void negative_test_post_invalid_country_400_response(String invalidCountryPost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("Country", invalidCountryPost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "ijaruuwidpvcblbdq"})
    public void negative_test_post_invalid_postal_code_400_response(String invalidPostalCodePost) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11549: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("PostalCode", invalidPostalCodePost);
        Response updateLocationResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

//    Negative Put tests

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"Id", "Name", "Address"})
    public void negative_test_put_missing_mandatory_data_fields_400_response(String missingMandatoryDataFieldPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11553: ADDRESS - AC3 - Negative Test - Missing Mandatory Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBody(missingMandatoryDataFieldPut, null);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"StreetName", "TownName", "CountrySubDivision", "Country"})
    public void negative_test_put_missing_mandatory_address_fields_400_response(String missingMandatoryAddressFieldPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11553: ADDRESS - AC3 - Negative Test - Missing Mandatory Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress(missingMandatoryAddressFieldPut, null);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"Department", "SubDepartment", "BuildingNumber", "AddressLine", "PostalCode"})
    public void positive_test_put_missing_optional_address_fields_201_response(String missingOptionalAddressFieldPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Positive Test - Missing Optional Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");

        if (validLocationId == null) {
            OBLocationDetails1 location = OBLocationDetails1.builder()
                    .name("Work Address")
                    .address(OBLocationAddress1.builder()
                            .department("Palm Square")
                            .subDepartment("36a")
                            .buildingNumber("16")
                            .streetName("Sunbay Area")
                            .addressLine(Collections.singletonList("Dubai Mall"))
                            .townName("Dubai")
                            .countrySubDivision("Abu Dhabi")
                            .country("AE")
                            .postalCode("P.O. Box 64235")
                            .build())
                    .build();

            validLocationId =
                    this.locationsApiV2.createLocationDetailsChild(alphaTestUser, location, connectionId).getData().get(0).getId();
        }
        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress(missingOptionalAddressFieldPut, null);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 200);
        THEN("The platform will return a 201 Response");
        OBReadCustomer1 get =
                this.customerApiV2.getChild(alphaTestUser, connectionId, 200).as(OBReadCustomer1.class);
        assertNotNull(get);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "N4Xe6Ffc9GgyEE1Z9hkV7mGNjemsOobwGbah8EmJlyu4lp4vbxxXfUAhsKp0KmDuQm0UJzjG7hMnar1UlofzJ3XA1QUCg7Y1RgWYitn24dDvvbnXJs8WV3aarlA67xRD6MGYPxDC06dWv1wPcA3QjKUroQ7sDHH7gYzrMqXNM0lBe24weAAKr3CdhoVX8GcrbxapEodlbS1tu3ybOBGK7yXbY3xs37cFpSduZrSWn4MZ0KbH6R6TivWdDRrO4C5m"})
    public void negative_test_put_invalid_name_400_response(String invalidAddressLabelNamePut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsersNegative();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBody("Name", invalidAddressLabelNamePut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUserNegative, location, connectionIdNegative, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "kgktnaoljhqxegnvysxrbyfshbaqftcnfdfpyopqjwalxjsplqomjwvhnsbtgirruitsjuaxsrptniqjxjhvwywzszoqdfkndwzmrhgflbucmknflprxqqaeaoqvqggjlmhuyrokurbzcywcwrjpiormkjbmkyndmgnspppptwofhcuhkzcveucfjgwfmpjptfvvcrkuzqjuoydelxepkswxanacyhbnzdpliklgoztmtwdsdlqsjrpefpearja"})
    public void negative_test_put_invalid_department_400_response(String invalidDepartmentPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsersNegative();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("Department", invalidDepartmentPut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUserNegative, location, connectionIdNegative, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "N4Xe6Ffc9GgyEE1Z9hkV7mGNjemsOobwGbah8EmJlyu4lp4vbxxXfUAhsKp0KmDuQm0UJzjG7hMnar1UlofzJ3XA1QUCg7Y1RgWYitn24dDvvbnXJs8WV3aarlA67xRD6MGYPxDC06dWv1wPcA3QjKUroQ7sDHH7gYzrMqXNM0lBe24weAAKr3CdhoVX8GcrbxapEodlbS1tu3ybOBGK7yXbY3xs37cFpSduZrSWn4MZ0KbH6R6TivWdDRrO4C5m"})
    public void negative_test_put_invalid_sub_department_400_response(String invalidSubDepartmentPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsersNegative();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("SubDepartment", invalidSubDepartmentPut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUserNegative, location, connectionIdNegative, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "yjlusfjevgesiarqo"})
    public void negative_test_put_invalid_building_number_400_response(String invalidBuildingNumberPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("BuildingNumber", invalidBuildingNumberPut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "henekqwmnkpqkejopmocewmfqjkihzbkzejoramwhwyxmaixagcvotvyfqytrjdfuyobybr"})
    public void negative_test_put_invalid_street_name_400_response(String invalidStreetNamePut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("StreetName", invalidStreetNamePut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_put_invalid_address_line_400_response() {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");
        List addressLine = new ArrayList() {
            {
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
            }
        };

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(addressLine)
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBErrorResponse1 error =
                this.locationsApiV2.updateLocationDetailsChildError(alphaTestUser, location, connectionId, 400);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "kgktnaoljhqxegnvysxrbyfshbaqftcnfdfpyopqjwalxjsplqomjwvhnsbtgirruitsjuaxsrptniqjxjhvwywzszoqdfkndwzmrhgflbucmknflprxqqaeaoqvqggjlmhuyrokurbzcywcwrjpiormkjbmkyndmgnspppptwofhcuhkzcveucfjgwfmpjptfvvcrkuzqjuoydelxepkswxanacyhbnzdpliklgoztmtwdsdlqsjrpefpearja"})
    public void negative_test_put_invalid_town_name_400_response(String invalidTownNamePut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsersNegative();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("TownName", invalidTownNamePut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUserNegative, location, connectionIdNegative, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "muktjdkwskokzgkyqssdnnawnlmvffwdoxag"})
    public void negative_test_put_invalid_country_sub_division_400_response(String invalidCountrySubDivisionPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("CountrySubDivision", invalidCountrySubDivisionPut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "AEE", "!@", "ae"})
    public void negative_test_put_invalid_country_400_response(String invalidCountryPut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("Country", invalidCountryPut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "ijaruuwidpvcblbdq"})
    public void negative_test_put_invalid_postal_code_400_response(String invalidPostalCodePut) {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11550: ADDRESS - AC3 - Negative Test - Invalid Fields");
        setupTestUsers();
        GIVEN("A client wants to store an additional address against the child");
        AND("The address request does not match the data validation table");

        WHEN("The client sends the address");

        JSONObject location = generateJsonBodyChangeAddress("PostalCode", invalidPostalCodePut);
        Response updateLocationResponse =
                this.locationsApiV2.updateLocationDetailsChildJson(alphaTestUser, location, connectionId, 400);
        OBErrorResponse1 error = updateLocationResponse.as(OBErrorResponse1.class);
        THEN("The platform will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_delete_location_with_invalid_location_id() {
        TEST("AHBDB-6998: Banking Information Capture For Child");
        TEST("AHBDB-11554: ADDRESS - Negative Test - Location ID does not exist");
        setupTestUsers();
        GIVEN("A client wants to delete a child's additional address details with a location ID that does not exist");
        OBReadRelationship1 validateConnectionId = this.relationshipApi.getRelationships(alphaTestUser);
        assertEquals(childId, validateConnectionId.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(connectionId, validateConnectionId.getData().getRelationships().get(0).getConnectionId().toString());

        OBErrorResponse1 error =
                this.locationsApiV2.deleteLocationDetailsChildError(alphaTestUser, randomUUID, connectionId, 404);
        WHEN("The client calls delete on that ID with a valid JWT token");
        THEN("The platform will return a 404");
        assertNotNull(error);

        DONE();
    }

    @Order(5)
    @Test
    public void negative_test_post_with_random_connection_id_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11555: Addresses - AC6 - Customer Not Found");
        setupTestUsers();
        GIVEN("A client wants to add additional address details for a child");
        AND("They use an invalid connection ID");

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBErrorResponse1 error =
                this.locationsApiV2.createLocationDetailsChildError(alphaTestUser, location, randomUUID, 404);
        WHEN("The client attempts to CRUD the address with a valid JWT token");
        assertNotNull(error);
        assertEquals(CONNECTION_ID_NOT_FOUND, error.getCode());
        THEN("The platform will return a 404 Response");
        DONE();
    }

    @Order(5)
    @Test
    public void negative_test_put_and_delete_with_random_connection_id_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11551: Addresses - AC6 - Customer Not Found");
        setupTestUsers();
        GIVEN("A client wants to change the details for an additional address for a child");
        AND("They use an invalid connection ID");

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBReadLocationResponse1 response =
                this.locationsApiV2.createLocationDetailsChild(alphaTestUser, location, connectionId);
        WHEN("The client attempts to CRUD the address with a valid JWT token");
        assertNotNull(response);
        String locationId = response.getData().get(0).getId();

        OBLocationDetails1 location2 = OBLocationDetails1.builder()
                .id(locationId)
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBErrorResponse1 error =
                this.locationsApiV2.updateLocationDetailsChildError(alphaTestUser, location2, randomUUID, 404) ;
        THEN("The platform will return a 404 Response");
        assertNotNull(error);
        assertEquals(CONNECTION_ID_NOT_FOUND, error.getCode());
        DONE();

        WHEN("A client attempts to delete the details for an address of a child");
        AND("The connectionID they supply does not match");
        OBErrorResponse1 errorDelete =
                this.locationsApiV2.deleteLocationDetailsChildError(alphaTestUser, locationId, randomUUID, 404);
        THEN("The platform will return a 404 Response");
        assertNotNull(errorDelete);
        assertEquals(CONNECTION_ID_NOT_FOUND, errorDelete.getCode());
        DONE();
    }

    @Order(5)
    @Test
    public void negative_test_get_with_random_connection_id_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11551: Addresses - AC6 - Customer Not Found");
        setupTestUsers();
        GIVEN("A client wants to see the additional address details for a child");
        AND("They use an invalid connection ID");

        OBErrorResponse1 error =
                this.locationsApiV2.getLocationsDetailsChildError(alphaTestUser, UUID.randomUUID().toString(), 404);

        THEN("The platform will return a 404 Response");
        assertNotNull(error);
        assertEquals(CONNECTION_ID_NOT_FOUND, error.getCode());
        DONE();
    }

    @Order(5)
    @Test
    public void negative_test_put_and_delete_address_random_location_id() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11554: Addresses - Negative Test - Put address with a random LocationID and valid ConnectionID");
        setupTestUsers();
        GIVEN("A child has at least one additional address");
        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBReadLocationResponse1 response = this.locationsApiV2.createLocationDetailsChild(alphaTestUser, location, connectionId);
        WHEN("A client attempts to PUT or DELETE that additional address");
        AND("The locationID supplied in the request does not match");

        OBLocationDetails1 location2 = OBLocationDetails1.builder()
                .id(randomUUID)
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBErrorResponse1 putError =
                this.locationsApiV2.updateLocationDetailsChildError(alphaTestUser, location2, connectionId, 404);
        OBErrorResponse1 deleteError =
                this.locationsApiV2.deleteLocationDetailsChildError(alphaTestUser, randomUUID, connectionId, 404);
        THEN("The platform will return a 404 Response");
        DONE();
    }

    @Order(100)
    @Test
    public void negative_test_crud_with_child_that_does_not_exist() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11551: AC6 - Customer not found - 404 Response");
        setupTestUsersFresh();
        AlphaTestUser child = new AlphaTestUser();
        child.setUserId(childIdFresh);

        child = this.alphaTestUserFactory.createChildCustomer(alphaTestUserFresh, child, connectionIdFresh, childIdFresh);
        GIVEN("A child exists with a valid connection ID");
        OBLocationDetails1 postResponse =
                this.locationsApiV2.createLocationDetailsChildJson(alphaTestUserFresh, generateJsonBody("Id", null), connectionIdFresh, 201).as(OBLocationDetails1.class);

        String locationId = postResponse.getId();
        System.out.println(locationId);
        this.customerApiV2.deleteCustomer(child);

        this.customerApiV2.getCurrentCustomerError(child, 404);
        OBReadRelationship1 relationship1 = this.relationshipApi.getRelationships(alphaTestUserFresh);
        assertTrue(relationship1.getData().getRelationships() == null);
        WHEN("They are deleted from CRM");
        AND("A client tries to CRUD their address details");

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

//        Create
        OBErrorResponse1 errorCreate = this.locationsApiV2.createLocationDetailsChildError(alphaTestUserFresh, location, connectionIdFresh, 404);
        assertNotNull(errorCreate);

//        Retrieve
        OBErrorResponse1 errorRetrieve = this.locationsApiV2.getLocationsDetailsChildError(alphaTestUserFresh, connectionIdFresh, 404);
        assertNotNull(errorRetrieve);

        OBLocationDetails1 locationForPut = OBLocationDetails1.builder()
                .id(locationId)
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

//        Update
        OBErrorResponse1 errorUpdate =
                this.locationsApiV2.updateLocationDetailsChildError(alphaTestUserFresh, locationForPut, connectionIdFresh, 404);
        assertNotNull(errorUpdate);

//        Delete
        OBErrorResponse1 errorDelete =
                this.locationsApiV2.deleteLocationDetailsChildError(alphaTestUserFresh, locationId, connectionIdFresh, 404);
        assertNotNull(errorDelete);
        THEN("The platform will respond with a 404 Not Found");
        DONE();
    }

    private JSONObject generateJsonBody(String field, String changing) {
        return new JSONObject() {
            {
                put("Id", randomUUID);
                put("Name", "Test Name");
                put("Address", new JSONObject() {
                    {
                        put("Department", "Palm Square");
                        put("SubDepartment", "36a");
                        put("BuildingNumber", "16");
                        put("StreetName", "Sunbay Area");
                        put("AddressLine", "Dubai Mall");
                        put("TownName", "Dubai");
                        put("CountrySubDivision", "Abu Dhabi");
                        put("Country", "AE");
                        put("PostalCode", "P.O. Box 64235");
                    }
                });
                put(field, changing);
            }
        };
    }

    private JSONObject generateJsonBodyChangeAddress(String field, String changing) {
        return new JSONObject() {
            {
                put("Id", validLocationId);
                put("Name", "Test Name");
                put("Address", new JSONObject() {
                    {
                        put("Department", "Palm Square");
                        put("SubDepartment", "36a");
                        put("BuildingNumber", "16");
                        put("StreetName", "Sunbay Area");
                        put("AddressLine", "Dubai Mall");
                        put("TownName", "Dubai");
                        put("CountrySubDivision", "Abu Dhabi");
                        put("Country", "AE");
                        put("PostalCode", "P.O. Box 64235");
                        put(field, changing);
                    }
                });
            }
        };
    }
}
