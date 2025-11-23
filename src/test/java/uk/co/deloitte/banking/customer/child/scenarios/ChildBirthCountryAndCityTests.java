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
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

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
public class ChildBirthCountryAndCityTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private String connectionId;
    private String childId;
    private String fullName = "testUser";

    /*
        Tests PATCH and GET for /internal/v2/relationships/{relationshipId}/customers
        Specifically for city and country of birth
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

            OBWritePartialCustomer1 patchChild = OBWritePartialCustomer1.builder()
                    .data(OBWritePartialCustomer1Data.builder()
                            .countryOfBirth("IN")
                            .cityOfBirth("Dubai")
                            .build())
                    .build();

            this.customerApi.patchChildSuccess(alphaTestUser, patchChild, connectionId);
        }
    }

    @Order(3)
    @Test
    public void happy_path_patch_country_and_city_of_birth_200_response() {
        TEST("AHBDB-6998: Banking Information Capture for child");
        TEST("AHBDB-11585: Country/City of Birth - AC1 - Patch country/city of birth - 200 Response");
        setupTestUsers();
        GIVEN("The customer has provided their country and city of birth for the child");

        OBReadCustomer1 dataCustomer = this.customerApi.getCurrentCustomer(alphaTestUserChild);
        assertEquals("IN", dataCustomer.getData().getCustomer().get(0).getCountryOfBirth());

        OBWritePartialCustomer1 patchChild = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai Change")
                        .build())
                .build();

        this.customerApi.patchChildSuccess(alphaTestUser, patchChild, connectionId);

        WHEN("The client updates with a valid country and city of birth");

        THEN("We will return a 200 Response");

        OBReadCustomer1 dataCustomer2 = this.customerApi.getCurrentCustomer(alphaTestUserChild);
        assertEquals("AE", dataCustomer2.getData().getCustomer().get(0).getCountryOfBirth());
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"", "!@", "AEE", "21", "ae"})
    public void negative_test_invalid_country(String invalidCountryOfBirth) {
        TEST("AHBDB-6998: Banking Information Capture for child");
        TEST("AHBDB-11586: Country/City of Birth - AC2 - Invalid country/city of birth - 400 Response");
        setupTestUsers();
        GIVEN("The customer has provided their country and city of birth for the child");

        OBReadCustomer1 dataCustomer =
                this.customerApi.getChild(alphaTestUser, connectionId, 200).as(OBReadCustomer1.class);
        assertEquals("IN", dataCustomer.getData().getCustomer().get(0).getCountryOfBirth());

        JSONObject patchChild = generatePartialCustomer("CountryOfBirth", invalidCountryOfBirth);

        OBErrorResponse1 error =
                this.customerApi.patchChildJson(alphaTestUser, patchChild, connectionId, 400).as(OBErrorResponse1.class);
        WHEN("The client updates with a valid country and city of birth");
        THEN("We will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"", "!@", "12343", "!@£$$$$"})
    public void negative_test_invalid_city(String invalidCityOfBirth) {
        TEST("AHBDB-6998: Banking Information Capture for child");
        TEST("AHBDB-11586: Country/City of Birth - AC2 - Invalid country/city of birth - 400 Response");
        setupTestUsers();
        GIVEN("The customer has provided their country and city of birth for the child");

        OBReadCustomer1 dataCustomer = this.customerApi.getCurrentCustomer(alphaTestUserChild);
        assertEquals("IN", dataCustomer.getData().getCustomer().get(0).getCountryOfBirth());

        JSONObject patchChild = generatePartialCustomer("CityOfBirth", invalidCityOfBirth);

        OBErrorResponse1 error =
                this.customerApi.patchChildJson(alphaTestUser, patchChild, connectionId, 400).as(OBErrorResponse1.class);
        WHEN("The client updates with a valid country and city of birth");
        THEN("We will return a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(10)
    @Test
    public void negative_test_patch_country_and_city_of_birth_for_a_customer_that_does_not_exist() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11593: Country/City of birth - Negative Test - Patch child details to a child that doesn't exist");
        setupTestUsers();
        GIVEN("A client wants to add country/city of birth details to a child");
        AND("The child is deleted from CRM");
        this.customerApi.deleteCustomer(alphaTestUserChild);

        WHEN("The client attempts to update the child's details");
        OBWritePartialCustomer1 patchChild = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai Change")
                        .build())
                .build();

        OBErrorResponse1 error = this.customerApi.patchChildError(alphaTestUser, patchChild, connectionId, 404);
        THEN("The platform will return a 404 Response");
        assertNotNull(error);
        DONE();
    }

    private JSONObject generatePartialCustomer(String field, Object value) {
        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("CountryOfBirth", "AE");
                        put("CityOfBirth", "Dubai");
                        put(field, value);
                    }
                });
            }
        };
    }

}
