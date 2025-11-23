package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.residentialaddress.api.ResidentialAddressApi;
import uk.co.deloitte.banking.customer.taxresidency.api.TaxResidencyApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;

@Tag("AHBDB-6998")
@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildResidentialAddressTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private FatcaApiV2 fatcaApiV2;

    @Inject
    private OtpApi otpApi;

    @Inject
    private LocationsApiV2 locationsApiV2;

    @Inject
    private ResidentialAddressApi residentialAddressApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private TaxResidencyApiV2 taxResidencyApiV2;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private String connectionId;
    private String childId;
    private String fullName = "testUser";

    @BeforeEach
    void ignore() {
        envUtils.ignoreTestInEnv(Environments.NFT);
    }

    /*
        Tests GET and PATCH for /internal/v2/relationships/{relationshipId}/customers
        Specifically for residential address
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
    public void happy_path_patch_residential_address_200_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11594: Residential Address - AC1 - Patch residential address - 200 Response");
        setupTestUsers();
        GIVEN("The customer has provided their residential address for the child");

        this.customerApi.getCurrentCustomer(alphaTestUserChild);
        this.customerApi.getCurrentCustomer(alphaTestUser);
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertEquals(childId, relationships.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(connectionId, relationships.getData().getRelationships().get(0).getConnectionId().toString());

        OBPartialPostalAddress6 address = OBPartialPostalAddress6.builder()
                .department("Palm Square Test")
                .subDepartment("Flat No 108")
                .buildingNumber("166")
                .streetName("Sunny Bay Area")
                .addressLine(Collections.singletonList("Dubai Mall Test"))
                .townName("Dubai Test")
                .countrySubDivision("Abu Dhabi")
                .country("AE")
                .postalCode("P.O. Box 64235")
                .build();

        OBWritePartialCustomer1 patchData = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(address)
                        .build())
                .build();

        OBWriteCustomerResponse1 patchResponse =
                this.customerApi.patchChildSuccess(alphaTestUser, patchData, connectionId);
        WHEN("The client updates the customer profile with a valid residential address");
        assertNotNull(patchResponse);
        THEN("The platform will return a 200 OK");
        AND("The details will be persisted");
        OBReadCustomer1 data =
                this.customerApi.getChild(alphaTestUser, connectionId, 200).as(OBReadCustomer1.class);
        OBCustomer1 details = data.getData().getCustomer().get(0);
        Assertions.assertEquals(address.getCountry(), details.getAddress().getCountry());
        Assertions.assertEquals(address.getBuildingNumber(), details.getAddress().getBuildingNumber());
        Assertions.assertEquals(address.getPostalCode(), details.getAddress().getPostalCode());
        Assertions.assertEquals(address.getDepartment(), details.getAddress().getDepartment());
        Assertions.assertEquals(address.getCountrySubDivision(), details.getAddress().getCountrySubDivision());
        Assertions.assertEquals(address.getTownName(), details.getAddress().getTownName());
        assertEquals(childId, details.getCustomerId().toString());
        assertEquals(alphaTestUser.getUserId(), details.getOnboardedBy());
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "12345678901234567"})
    public void negative_test_invalid_building_number(String invalidBuildingNumber) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11595: Residential Address - AC2 - Invalid Residential Address - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store a residential address against the child");
        AND("The address format does not satisfy the validation in the data table");

        JSONObject address = generateAddress("BuildingNumber", invalidBuildingNumber);

        OBErrorResponse1 patchResponse =
                this.customerApi.patchChildJson(alphaTestUser, address, connectionId, 400).as(OBErrorResponse1.class);
        WHEN("The client updates the customer profile with this address");
        assertNotNull(patchResponse);
        THEN("The platform will return a 400 Bad Request");
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "vtqmjfgzewyoqzycuyakoqnynlgylniydyjnknnsznzlkjkakazdqmojuzmlqrbcqswipya"})
    public void negative_test_invalid_street_name(String invalidStreetName) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11595: Residential Address - AC2 - Invalid Residential Address - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store a residential address against the child");
        AND("The address format does not satisfy the validation in the data table");

        JSONObject address = generateAddress("StreetName", invalidStreetName);

        OBErrorResponse1 patchResponse =
                this.customerApi.patchChildJson(alphaTestUser, address, connectionId, 400).as(OBErrorResponse1.class);
        WHEN("The client updates the customer profile with this address");
        assertNotNull(patchResponse);
        THEN("The platform will return a 400 Bad Request");
        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_invalid_address_line() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11595: Residential Address - AC2 - Invalid Residential Address - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store a residential address against the child");
        AND("The address format does not satisfy the validation in the data table");

        List addressLine = new ArrayList() {
            {
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
            }
        };

        OBPartialPostalAddress6 address = OBPartialPostalAddress6.builder()
                .department("Palm Square Test")
                .subDepartment("Flat No 108")
                .buildingNumber("166")
                .streetName("Sunny Bay Area")
                .addressLine(addressLine)
                .townName("Dubai Test")
                .countrySubDivision("Abu Dhabi Test")
                .country("AE")
                .postalCode("P.O. Box 64235")
                .build();

        OBWritePartialCustomer1 patchData = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(address)
                        .build())
                .build();

        OBErrorResponse1 patchResponse =
                this.customerApi.patchChildError(alphaTestUser, patchData, connectionId, 400);
        WHEN("The client updates the customer profile with this address");
        assertNotNull(patchResponse);
        THEN("The platform will return a 400 Bad Request");
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "vtqmjfgzewyoqzycuyakoqnynlgylniydyjnknnsznzlkjkakazdqmojuzmlqrbcqswipya"})
    public void negative_test_invalid_country_sub_division(String invalidCountrySubDivision) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11595: Residential Address - AC2 - Invalid Residential Address - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store a residential address against the child");
        AND("The address format does not satisfy the validation in the data table");

        JSONObject address = generateAddress("CountrySubDivision", invalidCountrySubDivision);

        OBErrorResponse1 patchResponse =
                this.customerApi.patchChildJson(alphaTestUser, address, connectionId, 400).as(OBErrorResponse1.class);
        WHEN("The client updates the customer profile with this address");
        assertNotNull(patchResponse);
        THEN("The platform will return a 400 Bad Request");
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "vtqmjfgzewyoqzycuyakoqnynlgylniydyjnknnsznzlkjkakazdqmojuzmlqrbcqswipya"})
    public void negative_test_invalid_country(String invalidCountry) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11595: Residential Address - AC2 - Invalid Residential Address - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store a residential address against the child");
        AND("The address format does not satisfy the validation in the data table");

        JSONObject address = generateAddress("Country", invalidCountry);

        OBErrorResponse1 patchResponse =
                this.customerApi.patchChildJson(alphaTestUser, address, connectionId, 400).as(OBErrorResponse1.class);
        WHEN("The client updates the customer profile with this address");
        assertNotNull(patchResponse);
        THEN("The platform will return a 400 Bad Request");
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"Department", "SubDepartment", "BuildingNumber", "StreetName", "AddressLine", "TownName", "PostalCode"})
    public void positive_test_missing_optional_fields(String missingOptionalField) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11597: Residential Address - Positive Test - Missing Optional Fields");
        setupTestUsers();
        GIVEN("The client wants to store a residential address against the child");
        AND("The address format does not satisfy the validation in the data table");

        JSONObject address = generateAddress(missingOptionalField, null);

        OBWriteCustomerResponse1 patchResponse =
                this.customerApi.patchChildJson(alphaTestUser, address, connectionId, 200).as(OBWriteCustomerResponse1.class);
        WHEN("The client updates the customer profile with this address");
        assertNotNull(patchResponse);
        THEN("The platform will return a 201");
        DONE();
    }

    @Order(100)
    @Test
    public void negative_test_patch_child_that_does_not_exist() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11596: Negative Test - Customer Not Found - 404 Response");
        setupTestUsers();
        GIVEN("A child has been deleted from CRM");
        this.customerApi.deleteCustomer(alphaTestUserChild);

        this.customerApi.getCurrentCustomerError(alphaTestUserChild, 404);
        WHEN("A parent attempts to update the residential address of that child");
        OBPartialPostalAddress6 address = OBPartialPostalAddress6.builder()
                .department("Palm Square Test")
                .subDepartment("Flat No 108")
                .buildingNumber("166")
                .streetName("Sunny Bay Area")
                .addressLine(Collections.singletonList("Dubai Mall Test"))
                .townName("Dubai Test")
                .countrySubDivision("Abu Dhabi")
                .country("AE")
                .postalCode("P.O. Box 64235")
                .build();

        OBWritePartialCustomer1 patchData = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(address)
                        .build())
                .build();

        OBErrorResponse1 error =
                this.customerApi.patchChildError(alphaTestUser, patchData, connectionId, 404);
        THEN("The platform will respond with a 404 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_patch_child_invalid_connection_id() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11598: Residential Address - Negative Test - Invalid ConnectionID - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to update the child details");
        WHEN("The client updates the customer profile");
        AND("The relationshipId supplied does not match");

        OBPartialPostalAddress6 address = OBPartialPostalAddress6.builder()
                .department("Palm Square Test")
                .subDepartment("Flat No 108")
                .buildingNumber("166")
                .streetName("Sunny Bay Area")
                .addressLine(Collections.singletonList("Dubai Mall Test"))
                .townName("Dubai Test")
                .countrySubDivision("Abu Dhabi")
                .country("AE")
                .postalCode("P.O. Box 64235")
                .build();

        OBWritePartialCustomer1 patchData = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(address)
                        .build())
                .build();

        OBErrorResponse1 error =
                this.customerApi.patchChildError(alphaTestUser, patchData, UUID.randomUUID().toString(), 404);
        THEN("The platform will return a 404 Response");
        assertNotNull(error);
        DONE();
    }

    private JSONObject generateAddress(String field, Object value) {
        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("Address", new JSONObject() {
                            {
                                put("Department", "Palm Square");
                                put("SubDepartment", "36a");
                                put("BuildingNumber", "16");
                                put("StreetName", "Sunbay Area");
                                put("AddressLine", Collections.singletonList("Dubai Mall"));
                                put("TownName", "Dubai");
                                put("CountrySubDivision", "Abu Dhabi");
                                put("Country", "AE");
                                put("PostalCode", "P.O. Box 64235");
                                put(field, value);
                            }
                        });
                    }
                });
            }
        };
    }
}
