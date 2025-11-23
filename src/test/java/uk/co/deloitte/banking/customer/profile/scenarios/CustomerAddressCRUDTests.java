package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.shaded.org.apache.commons.lang.math.NumberUtils;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBReadLocationResponse1;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.residentialaddress.api.ResidentialAddressApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerAddressCRUDTests {
    // TODO: Add "Dubai Bypass Rd" when bypass rejection bug fixed --- https://ahbdigitalbank.atlassian.net/browse/AHBDB-4705


    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private ResidentialAddressApi residentialAddressApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private LocationsApiV2 locationsApi;

    private AlphaTestUser alphaTestUser;

    @Inject
    EnvUtils envUtils;


    private void setupTestUser() {

        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupV2UserAndV2Customer(new AlphaTestUser(), null);
        }
    }

    private void forceRebuildUser() {
        this.alphaTestUser = alphaTestUserFactory.setupV2UserAndV2Customer(new AlphaTestUser(), null);
    }

    @ParameterizedTest
    @CsvSource({"Department," + 256, "SubDepartment," + 256, "BuildingNumber," + 17, "StreetName," + 71, "StreetName,", "TownName," + 256, "TownName,", "PostalCode," + 17, "CountrySubDivision," + 36, "CountrySubDivision,", "AddressLine, line1/line2/line3/line4", "Country, AAA", "Country,"})
    void set_invalid_residential_address_category(String line, String value) throws JSONException {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4267 to 4275 and 4279: Invalid residential address: " + line);
        setupTestUser();
        GIVEN("The client wants to store an additional address against the customer");
        AND("The address format does not satisfy the validation in the data table");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .name("Office")
                .address(addressEdit(line, value))
                .build();
        THEN("The platform responds with a 400 Bad request");
        OBErrorResponse1 result = locationsApi.createLocationDetailsError(alphaTestUser,request);
    }

    @Test
    void set_invalid_residential_address_name() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4266 AC3 Negative Test - Invalid residential address Name - Name: <Name> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store an additional address against the customer");
        AND("The address format does not satisfy the validation in the data table");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .name(RandomDataGenerator.generateRandomString(256))
                .address(createDefaultAddress())
                .build();
        THEN("The platform responds with a 400 Bad request");
        OBErrorResponse1 result = locationsApi.createLocationDetailsError(alphaTestUser,request);
    }

    @Test
    void set_null_residential_address_name() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4278 Negative Test - Missing Mandatory Field - Name - 400 response");
        setupTestUser();
        GIVEN("The client wants to store an additional address against the customer");
        AND("The address format does not satisfy the validation in the data table");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .name("")
                .address(createDefaultAddress())
                .build();
        THEN("The platform responds with a 400 Bad request");
        OBErrorResponse1 result = locationsApi.createLocationDetailsError(alphaTestUser,request);
    }

    @Test
    void set_valid_residential_address() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4263: AC1 Positive Test - Happy Path Scenario - Create address - 201 response and AC4 Get addresses - 200 success response - One Address - Fully Populated");
        forceRebuildUser();
        GIVEN("The customer has provided an additional address (on top of the initial residential address)");
        WHEN("The client updates the customer profile with a valid additional address");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .name("Office")
                .address(createDefaultAddress())
                .build();
        THEN("We will return a 201 response");
        OBReadLocationResponse1 result = locationsApi.createLocationDetails(alphaTestUser,request);
        AND("We will return the list of addresses");
        OBReadLocationResponse1 getResult = locationsApi.getLocationsDetails(alphaTestUser);
        assertFalse(getResult.getData().get(0).getAddress().getBuildingNumber().isBlank());

    }

    @Test
    void set_minimal_valid_residential_address() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4263: AC1 Positive Test - Happy Path Scenario - Create address - 201 response and AC4 Get addresses - 200 success response - One Address - Minimally Populated");
        forceRebuildUser();
        GIVEN("The customer has provided an additional address (on top of the initial residential address)");
        WHEN("The client updates the customer profile with a valid additional address");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .name("Office")
                .address(createMinimalAddress())
                .build();
        THEN("We will return a 201 response");

        locationsApi.createLocationDetails(alphaTestUser,request);
        AND("We will return the list of addresses");
        OBReadLocationResponse1 getResult = locationsApi.getLocationsDetails(alphaTestUser);
        assertNull(getResult.getData().get(0).getAddress().getBuildingNumber());
        assertFalse(getResult.getData().get(0).getAddress().getTownName().isBlank());
    }


    @Test
    void edit_valid_residential_address() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4265: AC2 Positive Test - Happy Path Scenario - Put additional address - 200 response");
        forceRebuildUser();
        GIVEN("The customer has provided an additional address");
        OBLocationDetails1 minimalRequest = OBLocationDetails1.builder()
                .name("Office")
                .address(createMinimalAddress())
                .build();
        locationsApi.createLocationDetails(alphaTestUser,minimalRequest);

        OBReadLocationResponse1 getMinimalResult = locationsApi.getLocationsDetails(alphaTestUser);
        assertNull(getMinimalResult.getData().get(0).getAddress().getBuildingNumber());
        String id = getMinimalResult.getData().get(0).getId();
        AND("Wants to update the address");
        WHEN("The client updates the customer profile with a valid additional address");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .id(id)
                .name("Office")
                .address(createDefaultAddress())
                .build();
        THEN("We will return a 200 response");
        locationsApi.updateLocationDetails(alphaTestUser,request);
        AND("We will return the list of addresses");
        OBReadLocationResponse1 getResult = locationsApi.getLocationsDetails(alphaTestUser);
        assertFalse(getResult.getData().get(0).getAddress().getBuildingNumber().isBlank());

    }

    @Test
    void set_multiple_valid_residential_addresses() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4264 AC1 Positive Test - Happy Path Scenario - Create address - 201 response and AC4 Get addresses - 200 success response - Multiple Addresses");
        forceRebuildUser();
        GIVEN("The customer has provided additional addresses (on top of the initial residential address)");
        WHEN("The client updates the customer profile with valid additional addresses");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .name("Office")
                .address(addressEdit("BuildingNumber", "literal 16"))
                .build();
        locationsApi.createLocationDetails(alphaTestUser,request);
        request = OBLocationDetails1.builder()
                .name("Address 2")
                .address(addressEdit("BuildingNumber", "literal 7"))
                .build();
        THEN("We will return a 201 response");
        locationsApi.createLocationDetails(alphaTestUser,request);
        AND("We will return the list of addresses");
        OBReadLocationResponse1 getResult = locationsApi.getLocationsDetails(alphaTestUser);
        assertTrue(getResult.getData().get(0).getAddress().getBuildingNumber().equals("16")||getResult.getData().get(0).getAddress().getBuildingNumber().equals("7"));
        assertTrue(getResult.getData().get(1).getAddress().getBuildingNumber().equals("16")||getResult.getData().get(1).getAddress().getBuildingNumber().equals("7"));
    }

    @Test
    void delete_residential_address() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4276 AC5 Positive Test - Happy Path Scenario - Delete address - 200 response");
        setupTestUser();
        GIVEN("A customer exists with an address");
        OBLocationDetails1 request = OBLocationDetails1.builder()
                .name("Office")
                .address(createDefaultAddress())
                .build();
        WHEN("The client calls delete on the address with a valid JWT token");
        THEN("The API will return a 200");
        OBReadLocationResponse1 result = locationsApi.createLocationDetails(alphaTestUser,request);
        AND("The address will be deleted from the address list in the customer profile");
        AND("The list of remaining addresses will be returned");
        OBReadLocationResponse1 getResult = locationsApi.getLocationsDetails(alphaTestUser);
        locationsApi.deleteLocationDetails(alphaTestUser, getResult.getData().get(0).getId());
        locationsApi.getLocationsDetails(alphaTestUser);
    }

    @Test
    void delete_residential_address_error() {
        TEST("AHBDB-3737: CRUD Additional Addresses");
        TEST("AHBDB-4277 AC6 Negative Test - Customer not found - 404 not found");
        setupTestUser();
        GIVEN("A customer exists with a customer profile");
        WHEN("The client attempts to retrieve or delete on the address with a valid JWT token");
        THEN("The platform will return a 404 not found response");
        customerApi.deleteCustomer(alphaTestUser);
        locationsApi.deleteLocationDetailsError(alphaTestUser, "351d9477-fcf3-4d54-9365-984f0d7d3840");
        this.alphaTestUser = alphaTestUserFactory.setupV2UserAndV2Customer(new AlphaTestUser(), null);
    }

    private OBLocationAddress1 createDefaultAddress() {
        ArrayList<String> addressLine = new ArrayList<>();
        addressLine.add(RandomDataGenerator.generateRandomAddressLine());
        return OBLocationAddress1.builder()
                .addressLine(addressLine)
                .buildingNumber(RandomDataGenerator.generateRandomBuildingNumber())
                .department(RandomDataGenerator.generateRandomSubDepartment())
                .subDepartment(RandomDataGenerator.generateRandomDepartment())
                .townName(RandomDataGenerator.generateRandomTownName())
                .country("AE")
                .countrySubDivision(RandomDataGenerator.generateRandomCountrySubDivision())
                .postalCode(RandomDataGenerator.generateRandomPostalCode())
                .streetName(RandomDataGenerator.generateRandomStreetName())
                .build();
    }
    private OBLocationAddress1 createMinimalAddress() {
        ArrayList<String> addressLine = new ArrayList<>();
        return OBLocationAddress1.builder()
                .addressLine(addressLine)
                .townName(RandomDataGenerator.generateRandomTownName())
                .country("AE")
                .countrySubDivision(RandomDataGenerator.generateRandomCountrySubDivision())
                .streetName(RandomDataGenerator.generateRandomStreetName())
                .build();
    }

    private OBLocationAddress1 addressEdit(String line, String value) {
        if (value != null && NumberUtils.isNumber(value)) {
            value = RandomDataGenerator.generateRandomString(Integer.parseInt(value));
        }
        if (value != null && value.contains("literal ")) {
            value = value.replace("literal ", "");
        }
        OBLocationAddress1 address = createDefaultAddress();
        switch (line) {
            case "BuildingNumber":
                address.setBuildingNumber(value);
                break;
            case "Department":
                address.setDepartment(value);
                break;
            case "SubDepartment":
                address.setSubDepartment(value);
                break;
            case "TownName":
                address.setTownName(value);
                break;
            case "Country":
                address.setCountry(value);
                break;
            case "CountrySubDivision":
                address.setCountrySubDivision(value);
                break;
            case "PostalCode":
                address.setPostalCode(value);
                break;
            case "StreetName":
                address.setStreetName(value);
                break;
            case "AddressLine":
                List<String> addressLine = Arrays.asList(value.split("/"));
                address.setAddressLine(addressLine);
                break;
        }
        return address;
    }

}
