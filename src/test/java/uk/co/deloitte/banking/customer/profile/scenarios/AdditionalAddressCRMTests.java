package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBReadLocationResponse1;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdditionalAddressCRMTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private LocationsApiV2 locationsApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private final String REQUEST_VALIDATION_ERROR_CODE = "REQUEST_VALIDATION";
    private final String NOT_FOUND_ERROR_CODE = "UAE.ERROR.NOT_FOUND";
    private final String NOT_FOUND_MESSAGE_CODE = "Customer locations not found";
    private final String ERROR_MESSAGE_STANDARD = "createLocation.locationDetails.address.";

    private String id;

    private void setupTestUser() {
        envUtils.ignoreTestInEnv(Environments.NFT);

        if (this.alphaTestUser == null) {
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Test
    @Order(1)
    public void happy_path_create_address_201_and_get_addresses_200_success_one_address() {
        TEST("AHBDB-4846: AC1 Post Addresses - 201 Created");
        TEST("AHBDB-4846: AC4 Get Addresses - 200 Success");
        TEST("AHBDB-6103: AC1 Positive Test - Happy Path Scenario - Create address - 201 response and AC4 Get " +
                "addresses - 200 success response - One Address");
        setupTestUser();
        GIVEN("We have received a request from the client to store the customer's Additional Addresses");
        OBLocationDetails1 locations = OBLocationDetails1.builder()
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank Head Ofice")
                        .subDepartment("Al Bahr Towers")
                        .buildingNumber("16")
                        .streetName("Al Saada Street, Al Nayhan Camp Area")
                        .addressLine(List.of("Sector E3"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 63111").build()).build();

        WHEN("We pass the request to CRM to create the Additional Addresses with a valid JWT token and valid field " +
                "inputs");
        OBReadLocationResponse1 response = this.locationsApi.createLocationDetails(this.alphaTestUser, locations);


        THEN("We'll receive a 201 created response");
        AND("We'll receive the full list of Additional Addresses related to that userID");
        this.id = response.getData().get(0).getId();

        GIVEN("We have received a request from the client to get a customer's Additional Addresses with a valid " +
                "userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBReadLocationResponse1 getResponse = this.locationsApi.getLocationsDetails(this.alphaTestUser);

        THEN("We'll receive a 200 response");
        AND("We will receive the full list of Additional Addresses related to that userID");
        Assertions.assertEquals(this.id, getResponse.getData().get(0).getId(), "Location ID returned does not match " +
                "ID sent");

        DONE();
    }

    @Test
    public void happy_path_create_address_201_and_get_addresses_200_success_multiple_addresses() {
        TEST("AHBDB-4846: AC1 Post Addresses - 201 Created");
        TEST("AHBDB-4846: AC4 Get Addresses - 200 Success");
        TEST("AHBDB-6104: AC1 Positive Test - Happy Path Scenario - Create address - 201 response and AC4 Get " +
                "addresses - 200 success response - Multiple Addresses");
        setupTestUser();
        GIVEN("We have received a request from the client to store the customer's Additional Addresses");
        OBLocationDetails1 location1 = OBLocationDetails1.builder()
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank Head Office")
                        .subDepartment("Al Bahr Towers")
                        .buildingNumber("16")
                        .streetName("Al Saada Street, Al Nayhan Camp Area")
                        .addressLine(List.of("Sector E3"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 63111").build()).build();

        OBLocationDetails1 location2 = OBLocationDetails1.builder()
                .name("Home")
                .address(OBLocationAddress1.builder()
                        .department("Sultan Nasser Al Su Suwaidi Building")
                        .subDepartment("Flat No 108")
                        .buildingNumber("Plot No C96")
                        .streetName("Al Marakaziva")
                        .addressLine(List.of("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 64235").build()).build();

        WHEN("We pass the request to CRM to create the Additional Addresses with a valid JWT token and valid field " +
                "inputs");
        OBReadLocationResponse1 response1 = this.locationsApi.createLocationDetails(this.alphaTestUser, location1);
        OBReadLocationResponse1 response2 = this.locationsApi.createLocationDetails(this.alphaTestUser, location2);

        THEN("We'll receive a 201 created response");
        AND("We'll receive the full list of Additional Addresses related to that userID");
        String locationId1 = response1.getData().get(0).getId();
        String locationId2 = response2.getData().get(1).getId();

        GIVEN("We have received a request from the client to get a customer's Additional Addresses with a valid " +
                "userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBReadLocationResponse1 getResponse = this.locationsApi.getLocationsDetails(this.alphaTestUser);

        THEN("We'll receive a 200 response");
        AND("We will receive the full list of Additional Addresses related to that userID");
        Assertions.assertEquals(4, getResponse.getData().stream().count(), "Expected 4 locations");
    }

    @Test
    @Order(2)
    public void happy_path_put_additional_address_200_response() {
        TEST("AHBDB-4846: AC3 Update Addresses - 200 Success");
        TEST("AHBDB-6105: AC3 Positive Test - Happy Path Scenario - Put additional address - 200 response");
        GIVEN("We have received a request from the client to update an address in the list of Additional Addresses");

        OBLocationDetails1 locationToUpdateTo = OBLocationDetails1.builder()
                .id(this.id)
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("Sultan Nasser Al Su Suwaidi Building")
                        .subDepartment("Flat No 108")
                        .buildingNumber("Plot No C96")
                        .streetName("Al Marakaziva")
                        .addressLine(List.of("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 64235").build()).build();

        WHEN("We pass the request to CRM to update the customer with a valid JWT token and the relevant updated " +
                "fields");
        OBReadLocationResponse1 response = this.locationsApi.updateLocationDetails(this.alphaTestUser,
                locationToUpdateTo);

        THEN("We'll receive a 200 Success");
        AND("We'll receive the full list of Additional Addresses with the updated fields");
        Assertions.assertNotEquals("Home", response.getData().get(0).getName(), "Name was not updated");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"",
            "fbdddddddddfgjbfhdvdhfbvkhdfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbfbfgjbfhdvdhfbvkhdfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbffdfgvdfbgfbvfgbfbvdfbh"})
    public void invalid_residential_address_Name_400_response(String invalidName) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6106: AC2 Negative Test - Invalid residential address Name - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("TownName", "Dubai");
        location.put("Name", invalidName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_ERROR_CODE, error.getCode(), "Error code was not expected, " +
                "expected: " + REQUEST_VALIDATION_ERROR_CODE);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"",
            "fbfgjbfhdvdhfbvkhddddddddddfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbfbfgjbfhdvdhfbvkhdfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbffdfgvdfbgfbvfgbfbvdfbh"})
    public void invalid_residential_address_Department_400_response(String invalidDepartment) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6107: AC2 Negative Test - Invalid residential address Department - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("Department", invalidDepartment);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "department: size must be between " +
                "1 and 255"), "Error message not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"",
            "fbfgjbfhdvdhfbvkhddddddddddfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbfbfgjbfhdvdhfbvkhdfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbffdfgvdfbgfbvfgbfbvdfbh"})
    public void invalid_residential_address_SubDepartment_400_response(String invalidSubDepartment) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6109: AC2 Negative Test - Invalid residential address SubDepartment - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("SubDepartment", invalidSubDepartment);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "subDepartment: size must be " +
                "between 1 and 255"), "Error message not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdfghjhgfdsdfghj", "12345678901234568", ""})
    public void invalid_residential_address_BuildingNumber_400_response(String invalidBuildingNumber) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6110: AC2 Negative Test - Invalid residential address BuildingNumber - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("BuildingNumber", invalidBuildingNumber);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "buildingNumber: size must be " +
                "between 1 and 16"), "Error message not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"qwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopqwertyuiopq", ""})
    public void invalid_residential_address_StreetName_400_response(String invalidStreetName) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6111: AC2 Negative Test - Invalid residential address StreetName - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("StreetName", invalidStreetName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        try {
            Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "streetName: size must be " +
                    "between 1 and 70"), "Error message not as expected");
        } catch (AssertionFailedError err) {
            Assertions.assertTrue(error.getMessage().contains("must not be blank"), "Error message not as expected");
        }

        DONE();
    }

    @Test
    public void invalid_residential_address_AddressLine_more_than_3_400_response() {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6112: AC2 Negative Test - Invalid residential address AddressLine - more than 3 - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        OBLocationDetails1 location = OBLocationDetails1.builder()
                .id(this.id)
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("Sultan Nasser Al Su Suwaidi Building")
                        .subDepartment("Flat No 108")
                        .buildingNumber("Plot No C96")
                        .streetName("Al Marakaziva")
                        .addressLine(List.of("Dubai Mall", "Dubai Mall", "Dubai Mall", "Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 64235").build()).build();

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsError(this.alphaTestUser, location);

        THEN("We'll receive a 400 bad request");
        Assertions.assertTrue(error.getMessage().contains("addressLine: size must be between 0 and 3"), "Error " +
                "message not as expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"",
            "fbfgjbfhdvdhfbvkhddddddddddfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbfbfgjbfhdvdhfbvkhdfbvhjdfxbvjhjcjkbvkjcbvkhbvkdbvkjhdbvjkchbvkfygbkfhbvlfhbvkfhbvkcbvkjbkkjbkjbkgvjbnklvgjnbkjvbffdfgvdfbgfbvfgbfbvdfbh"})
    public void invalid_residential_address_TownName_400_response(String invalidTownName) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6113: AC2 Negative Test - Invalid residential address TownName - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("TownName", invalidTownName);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        try {
            Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "townName: size must be " +
                    "between 1 and 255"), "Error message not as expected");
        } catch (AssertionFailedError err) {
            Assertions.assertTrue(error.getMessage().contains("must not be blank"), "Error message not as expected");
        }

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "dkgskshdgfkshdfhcjkjbkfgjbgkjgwjhfrr", "123434544545454563764762578625376576"})
    public void invalid_residential_address_CountrySubDivision_400_response(String invalidCountrySubDivision) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6114: AC2 Negative Test - Invalid residential address CountrySubDivision - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("CountrySubDivision", invalidCountrySubDivision);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        try {
            Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "countrySubDivision: size must" +
                    " be between 1 and 35"), "Error message not as expected");
        } catch (AssertionFailedError err) {
            Assertions.assertTrue(error.getMessage().contains("must not be blank"), "Error message not as expected");
        }

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12", "@£", "ae", "AEV"})
    public void invalid_residential_address_Country_400_response(String invalidCountry) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6115: AC2 Negative Test - Invalid residential address Country - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("Country", invalidCountry);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        try {
            Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "country: must match"), "Error" +
                    " message not as expected");
        } catch (AssertionFailedError err) {
            Assertions.assertTrue(error.getMessage().contains("must not be blank"), "Error message not as expected");
        }

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ashgvdsjhvdjshvcf"})
    public void invalid_residential_address_PostalCode_400_response(String invalidPostalCode) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6116: AC2 Negative Test - Invalid residential address PostalCode - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Addresses");
        JSONObject location = this.locationsApi.locationBuilder("PostalCode", invalidPostalCode);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertTrue(error.getMessage().contains(ERROR_MESSAGE_STANDARD + "postalCode: size must be between " +
                "1 and 16"), "Error message not as expected");

        DONE();
    }

    @Test
    public void happy_path_delete_address_200_response() {
        TEST("AHBDB-4846: AC5 Delete Addresses - 200 Success");
        TEST("AHBDB-6117: AC5 Positive Test - Happy Path Scenario - Delete address - 200 response");
        setupTestUser();
        GIVEN("We have received a request from the client to delete an Additional Address with a valid userID that " +
                "exists");
        OBLocationDetails1 locations = OBLocationDetails1.builder()
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank Head Ofice")
                        .subDepartment("Al Bahr Towers")
                        .buildingNumber("16")
                        .streetName("Al Saada Street, Al Nayhan Camp Area")
                        .addressLine(List.of("Sector E3"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 63111").build()).build();

        this.locationsApi.createLocationDetails(this.alphaTestUser, locations);
        OBReadLocationResponse1 getResponse = this.locationsApi.getLocationsDetails(this.alphaTestUser);

        for (OBLocationDetails1 location : getResponse.getData()) {
            this.locationsApi.deleteLocationDetails(this.alphaTestUser, location.getId());
        }

        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBErrorResponse1 error = this.locationsApi.getLocationsDetailsError(this.alphaTestUser, 404);

        THEN("We'll receive a 200 response");
        AND("We will receive the full list of Additional Addresses related to that userID less the one that was just " +
                "deleted");
        Assertions.assertEquals(NOT_FOUND_ERROR_CODE, error.getCode(), "Error Codes do not match. Expected " + NOT_FOUND_ERROR_CODE +
                " but found " + error.getCode());
        Assertions.assertEquals(NOT_FOUND_MESSAGE_CODE, error.getMessage(), "Error Messages do not match. Expected " + NOT_FOUND_MESSAGE_CODE +
                " but found " + error.getMessage());

        DONE();
    }

    //TODO:: Fix
//    @Test
//    public void customer_not_found_404_not_found() {
//        TEST("AHBDB-4846: AC6 Get/Delete Customer record doesn't exist - 404");
//        TEST("AHBDB-6118: AC6 Negative Test - Customer not found - 404 not found");
//        setupTestUser();
//        GIVEN("We have received a request from the client to get/delete an address with a userID that does not exist");
//        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
//        OBErrorResponse1 error = this.locationsApi.getLocationsDetailsError(this.alphaTestUser, 404);
//
//        THEN("The CRM will return a 404 - not found");
//        Assertions.assertTrue(error.getMessage().contains("Customer locations not found"), "Error Message not as " +
//                "expected");
//
//        DONE();
//    }

    @Test
    public void missing_mandatory_field_Name_400_response() {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6119: AC2 Negative Test - Missing Mandatory Field - Name - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Address");
        JSONObject location = this.locationsApi.locationBuilder("BuildingNumber", "16");
        location.remove("Name");

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_ERROR_CODE, error.getCode(), "Error Code was not as expected");
        Assertions.assertTrue(error.getMessage().contains("name: must not be blank"), "Error Message was not as " +
                "expected");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"StreetName", "TownName", "CountrySubDivision", "Country"})
    public void missing_mandatory_field_400_response(String fieldToRemove) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 400 bad request");
        TEST("AHBDB-6120: AC2 Negative Test - Missing Mandatory Field - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Additional Address");
        JSONObject location = locationBuilderAndRemove(fieldToRemove);

        WHEN("We pass the request to CRM to create the customer with a valid JWT token but invalid field inputs");
        OBErrorResponse1 error = this.locationsApi.createLocationDetailsJSONError(this.alphaTestUser, location, 400);

        THEN("We'll receive a 400 bad request");
        Assertions.assertEquals(REQUEST_VALIDATION_ERROR_CODE, error.getCode(), "Error Code was not as expected");
        Assertions.assertTrue(error.getMessage().toLowerCase().contains(fieldToRemove.toLowerCase() + ": must not be " +
                "blank"), "Error Message was not as expected");

        DONE();
    }

    @Test
    public void happy_path_delete_address_from_multiple_addresses_200_response() {
        TEST("AHBDB-4846: AC5 Delete Addresses - 200 Success");
        TEST("AHBDB-6339: AC5 Positive Test - Happy Path Scenario - Delete address from multiple addresses - 200 " +
                "response");
        setupTestUser();
        GIVEN("We have received a request from the client to delete an Additional Address with a valid userID that " +
                "exists");
        OBLocationDetails1 location1 = OBLocationDetails1.builder()
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("Al Hilal Bank Head Office")
                        .subDepartment("Al Bahr Towers")
                        .buildingNumber("16")
                        .streetName("Al Saada Street, Al Nayhan Camp Area")
                        .addressLine(List.of("Sector E3"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 63111").build()).build();

        OBLocationDetails1 location2 = OBLocationDetails1.builder()
                .name("Home")
                .address(OBLocationAddress1.builder()
                        .department("Sultan Nasser Al Su Suwaidi Building")
                        .subDepartment("Flat No 108")
                        .buildingNumber("Plot No C96")
                        .streetName("Al Marakaziva")
                        .addressLine(List.of("Dubai Mall"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("PO Box 64235").build()).build();

        String location1Id =
                this.locationsApi.createLocationDetails(this.alphaTestUser, location1).getData().get(0).getId();
        this.locationsApi.createLocationDetails(this.alphaTestUser, location2);

        this.locationsApi.getLocationsDetails(this.alphaTestUser);
        this.locationsApi.deleteLocationDetails(this.alphaTestUser, location1Id);

        WHEN("We pass the request to CRM to get that customer with a valid JWT token");
        OBReadLocationResponse1 getResponse = this.locationsApi.getLocationsDetails(this.alphaTestUser);

        THEN("We'll receive a 200 response");
        AND("We will receive the full list of Additional Addresses related to that userID less the one that was just " +
                "deleted");
        Assertions.assertNotEquals(location1Id, getResponse.getData().get(0).getId(), "ID's did not match");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Department", "SubDepartment", "BuildingNumber", "AddressLine", "PostalCode"})
    public void missing_optional_fields(String fieldToRemove) {
        TEST("AHBDB-4846: AC2 Post/Update - missing or invalid data - 201 response ");
        TEST("AHBDB-8220: Positive Test - Missing Optional Field - Value: <Value> - 201 response");
        setupTestUser();
        GIVEN("We have received a post or update request from the client to store the Additional Address(es)");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing field inputs");

        JSONObject location = locationBuilderAndRemove(fieldToRemove);

        OBReadLocationResponse1 response =
                this.locationsApi.createLocationDetailsJSONSuccess(this.alphaTestUser, location, 201);
        THEN("We'll receive a 201 response");
        Assertions.assertNotNull(response);
    }

    public JSONObject locationBuilderAndRemove(String fieldToRemove) {
        return new JSONObject() {
            {
                put("Name", "Work");
                put("Address", new JSONObject() {
                    {
                        put("Department", "Al Hilal Bank Head Office");
                        put("SubDepartment", "Al Bahr Towers");
                        put("BuildingNumber", "16");
                        put("StreetName", "Al Saada Street, Al Nayhan Camp Area");
                        put("AddressLine", List.of("Sector E3"));
                        put("TownName", "Dubai");
                        put("CountrySubDivision", "Abu Dhabi");
                        put("Country", "AE");
                        put("PostalCode", "PO Box 63111");
                        remove(fieldToRemove);
                    }
                });
            }
        };
    }
}
