package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBCRSData2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBTaxResidencyCountry2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBReadLocationResponse1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.taxresidency.api.TaxResidencyApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorisationCheckTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private TaxResidencyApiV2 taxResidencyApiV2;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private LocationsApiV2 locationsApiV2;

    @Inject
    private SanctionsApi sanctionsApi;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private FatcaApiV2 fatcaApiV2;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;

    private String validRelationshipId;
    private String validChildId;

    private void setupTestUser() {
        if (alphaTestUser == null || alphaTestUser2 == null) {
            alphaTestUser2 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

            validChildId = alphaTestUserFactory.createChildInForgerock(alphaTestUser);

            validRelationshipId = alphaTestUserFactory.createChildInCRM(alphaTestUser, alphaTestUserFactory
                    .generateDependantBody(validChildId, 15, "Full Name",
                            OBGender.MALE, OBRelationshipRole.FATHER));

        } else {
            alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
            alphaTestUser2 = alphaTestUserFactory.refreshAccessToken(alphaTestUser2);
        }
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_get_child_profile() {
        envUtils.ignoreTestInEnv("AHBDB-13547, will need to update status code to assert on also", Environments.SIT, Environments.NFT);

        TEST("AHBDB-13547");
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();
        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to retrieve the child's details");
        OBErrorResponse1 error =
                relationshipApi.getChildBasedOnRelationshipError(alphaTestUser2, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);
        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_post_fatca() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        JSONObject data = new JSONObject();
        JSONObject fatca = new JSONObject() {
            {
                put("Form", "W9");
                put("UsCitizenOrResident", "false");
                put("Ssn", "123456789");
                put("FederalTaxClassification", "Individual/sole proprietor or single-member LLC");
            }
        };
        data.put("Data", fatca);

        OBErrorResponse1 error = fatcaApiV2.createFatcaDetailsChildJSON(alphaTestUser2, data,
                validRelationshipId, 404).as(OBErrorResponse1.class);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_get_fatca() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        OBErrorResponse1 error =
                fatcaApiV2.getFatcaDetailsChildError(alphaTestUser2, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_post_idv() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(generateEnglishRandomString(10))
                .lastName(generateEnglishRandomString(10))
                .build();

        var error = idNowApi
                .createChildApplicantError(alphaTestUser2, validRelationshipId, applicantRequest, 403);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_get_idv() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        var error =
                idNowApi.getChildApplicantResultsNegativeFlow(alphaTestUser2, validRelationshipId, 403);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_post_internal_risks() {
        envUtils.ignoreTestInEnv("AHBDB-13548", Environments.SIT, Environments.NFT);

        TEST("AHBDB-13548");
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        CustomerBlacklistRequestDTO request = CustomerBlacklistRequestDTO.builder()
                .fullName("Full Name")
                .country("AE")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("F")
                .build();

        OBErrorResponse1 error =
                sanctionsApi.checkBlacklistedChildError(alphaTestUser2, request, validRelationshipId, 403);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_delete_location() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        OBLocationDetails1 validLocation = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Address line 1"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBReadLocationResponse1 response =
                locationsApiV2.createLocationDetailsChild(alphaTestUser, validLocation, validRelationshipId);

        String existingLocationId = response.getData().get(0).getId();

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        OBErrorResponse1 error = locationsApiV2
                .deleteLocationDetailsChildError(alphaTestUser2, existingLocationId, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_post_locations() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Address line 1"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        OBErrorResponse1 error = locationsApiV2
                .createLocationDetailsChildError(alphaTestUser2, location, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_get_locations() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        OBLocationDetails1 validLocation = OBLocationDetails1.builder()
                .name("Work Address")
                .address(OBLocationAddress1.builder()
                        .department("Palm Square")
                        .subDepartment("36a")
                        .buildingNumber("16")
                        .streetName("Sunbay Area")
                        .addressLine(Collections.singletonList("Address line 1"))
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 64235")
                        .build())
                .build();

        locationsApiV2.createLocationDetailsChild(alphaTestUser, validLocation, validRelationshipId);

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to use the child's details");
        OBErrorResponse1 error =
                locationsApiV2.getLocationsDetailsChildError(alphaTestUser2, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }


    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_post_terms() {
        envUtils.ignoreTestInEnv("AHBDB-13548", Environments.SIT, Environments.NFT);

        TEST("AHBDB-13548");
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to retrieve the child's details");
        JSONObject obj = new JSONObject(){
            {
                put("TermsAccepted", true);
                put("TermsAcceptedDate", OffsetDateTime.parse("2021-06-26T12:30:24+04:00"));
                put("TermsVersion", LocalDate.now());
                put("Type", "BANKING");
            }
        };

        OBErrorResponse1 error =
                relationshipApi.createDependantTermError(alphaTestUser2, validRelationshipId, obj, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_get_crs() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to retrieve the child's details");
        OBErrorResponse1 error =
                taxResidencyApiV2.getTaxInformationChildError(alphaTestUser2, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_post_crs() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        WHEN("Another customer tries to use that connection ID to retrieve the child's details");
        OBWriteCRS2 crs = OBWriteCRS2.builder()
                .data(OBCRSData2.builder()
                        .taxResidencyCountry(List.of(OBTaxResidencyCountry2.builder()
                                .country("AE")
                                .missingTinReason("Country doesn't issue TINs to its residents")
                                .build()))
                        .uaeResidencyByInvestmentScheme(true)
                        .otherResidencyJurisdictions(true)
                        .personalIncomeTaxJurisdictions(List.of("AE", "CN"))
                        .agreedCertification(true)
                        .build())
                .build();

        checkUserHasNoRelationships();

        THEN("The platform will not allow them to do so");
        OBErrorResponse1 error =
                this.taxResidencyApiV2.addCRSDetailsChildError(alphaTestUser2, crs, validRelationshipId, 404);

        assertNotNull(error);
        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_patch_child_profile() {
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();

        WHEN("Another customer tries to use that connection ID to retrieve the child's details");
        OBWritePartialCustomer1 patchChild = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName("First")
                        .lastName("Last")
                        .build())
                .build();

        OBErrorResponse1 error =
                customerApiV2.patchChildError(alphaTestUser2, patchChild, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");
        assertNotNull(error);

        DONE();
    }

    @Test
    public void negative_test_a_customer_tries_to_use_another_customers_connection_id_to_create_cif_for_child() {
        envUtils.ignoreTestInEnv("AHBDB-13548", Environments.SIT, Environments.NFT);

        TEST("AHBDB-13548");
        TEST("AHBDB-13514: Authorization on customer adapter");
        setupTestUser();

        GIVEN("A customer has a valid connection ID to an existing child");
        OBReadCustomer1 checkChildExists =
                relationshipApi.getChildBasedOnRelationship(alphaTestUser, validRelationshipId);

        assertEquals(validChildId, checkChildExists.getData().getCustomer().get(0).getCustomerId().toString());

        checkUserHasNoRelationships();
        WHEN("Another customer tries to use that connection ID to retrieve the child's details");
        relationshipApi.putChildCifError(alphaTestUser2, validRelationshipId, 404);

        THEN("The platform will not allow them to do so");

        DONE();
    }

    private void checkUserHasNoRelationships() {
        OBReadRelationship1 response = relationshipApi.getRelationships(alphaTestUser2);
        assertNull(response.getData().getRelationships());
    }
}
