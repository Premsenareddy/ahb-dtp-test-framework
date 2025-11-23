package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBCRSData2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBReadCRSResponse2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBTaxResidencyCountry2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRSResponse2;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.taxresidency.api.TaxResidencyApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class ChildCRSTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private TaxResidencyApiV2 taxResidencyApiV2;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private String connectionId;
    private String childId;
    private String fullName = "testUser";

    /*
        Tests GET and POST for /internal/v2/relationships/{relationshipId}/customers/crs
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
    public void happy_path_create_and_get_crs_details() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11461: CRS - AC1, AC3 - Create and Get CRS details");
        setupTestUsers();

        GIVEN("The customer has provided their CRS details");
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

        OBWriteCRSResponse2 response = this.taxResidencyApiV2.addCRSDetailsChild(alphaTestUser, crs, connectionId);
        OBCRSData2 postResponse = response.getData();

        assertNotNull(response);
        assertEquals(crs.getData().getUaeResidencyByInvestmentScheme(), postResponse.getUaeResidencyByInvestmentScheme());
        assertEquals(crs.getData().getOtherResidencyJurisdictions(), postResponse.getOtherResidencyJurisdictions());
        assertEquals(crs.getData().getPersonalIncomeTaxJurisdictions(), postResponse.getPersonalIncomeTaxJurisdictions());
        assertEquals(crs.getData().getAgreedCertification(), postResponse.getAgreedCertification());
        assertEquals(crs.getData().getTaxResidencyCountry().get(0), postResponse.getTaxResidencyCountry().get(0));
        WHEN("The client sends the CRS information");

        OBReadCRSResponse2 readCrs =
                this.taxResidencyApiV2.getTaxInformationChild(alphaTestUser, 200, connectionId);

        OBCRSData2 data = readCrs.getData().get(0);

        assertEquals(crs.getData().getUaeResidencyByInvestmentScheme(), data.getUaeResidencyByInvestmentScheme());
        assertEquals(crs.getData().getOtherResidencyJurisdictions(), data.getOtherResidencyJurisdictions());
        assertEquals(crs.getData().getPersonalIncomeTaxJurisdictions(), data.getPersonalIncomeTaxJurisdictions());
        assertEquals(crs.getData().getAgreedCertification(), data.getAgreedCertification());
        assertEquals(crs.getData().getTaxResidencyCountry().get(0), data.getTaxResidencyCountry().get(0));

        THEN("The CRS information will be stored in CRM");
        AND("The platform will return a 201 Created");
        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "ae", "AEUK", "!@", "33"})
    public void negative_test_invalid_country_details_400_response(String invalidCountry) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11462: CRS - AC2 - Negative Test - Invalid Country - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs = generateCRSDetails("Country", invalidCountry, "AgreedCertification", "true");

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400);
        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "a", "hjsdvcjshdvcsjhdbcvkj"})
    public void negative_test_invalid_tin_details_400_response(String invalidTinNumber) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11462: CRS - AC2 - Negative Test - Invalid TinNumber - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs = generateCRSDetails("TinNumber", invalidTinNumber, "AgreedCertification", "true");

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "1234", "@£$%^&"})
    public void negative_test_invalid_missing_reason_details_400_response(String invalidMissingTinReason) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11462: CRS - AC2 - Negative Test - Invalid MissingTinReason - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs = generateCRSDetails("MissingTinReason", invalidMissingTinReason, "AgreedCertification", "true");

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400);


        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"test", "2132132", "TRUE", "FALSE", "asdfgh", "12345"})
    public void negative_test_invalid_investment_scheme_details_400_response(String invalidUaeResidencyByInvestmentScheme) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11462: CRS - AC2 - Negative Test - Invalid UaeResidencyByInvestmentScheme - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs = generateCRSDetails("Country", "AE", "UaeResidencyByInvestmentScheme", invalidUaeResidencyByInvestmentScheme);

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"f", "!@£$$$", "12344556", "TRUE", "FALSE", "asdfgh", "12345"})
    public void negative_test_invalid_other_residency_jurisdictions_details_400_response(String invalidOtherResidencyJurisdictions) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11462: CRS - AC2 - Negative Test - Invalid OtherResidencyJurisdictions - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs = generateCRSDetails("Country", "AE", "OtherResidencyJurisdictions", invalidOtherResidencyJurisdictions);

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "A", "ae", "!@", "33"})
    public void negative_test_invalid_personal_income_tax_jurisdictions_details_400_response(String invalidPersonalIncomeTaxJurisdictions) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11462: CRS - AC2 - Negative Test - Invalid PersonalIncomeTaxJurisdictions - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs = generateCRSDetails("Country", "AE", "PersonalIncomeTaxJurisdictions", invalidPersonalIncomeTaxJurisdictions);

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"", "@!£$£", "jasd;klas", "TRUE", "FALSE", "asdfgh", "12345"})
    public void negative_test_invalid_certification_details_400_response(String invalidAgreedCertification) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11462: CRS - AC2 - Negative Test - Invalid AgreedCertification - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs =
                generateCRSDetails("Country", "AE", "AgreedCertification", invalidAgreedCertification);

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"MissingTinReason", "TinNumber"})
    public void positive_test_missing_optional_tax_residency_country_fields_201_response(String missingOptionalTaxResidencyCountryFields) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11544: CRS - AC2 - Negative Test - Missing OptionalTaxResidencyCountryFields - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crs = generateCRSDetails(missingOptionalTaxResidencyCountryFields, null, "AgreedCertification", "true");
        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 201);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"UaeResidencyByInvestmentScheme", "OtherResidencyJurisdictions", "PersonalIncomeTaxJurisdictions"})
    public void positive_test_missing_optional_data_fields_201_response(String missingOptionalDataFields) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11547: CRS - Positive Test - Missing Optional Data Fields");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject taxResidencyCountry =
                this.taxResidencyApiV2.buildTaxResidencyCountryJSON("Country", "AE");
        JSONObject crsMissingOptionalFields =
                this.taxResidencyApiV2.getJSONForCRSRemoved(taxResidencyCountry, missingOptionalDataFields);

        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crsMissingOptionalFields, connectionId, 201);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 201");

        DONE();
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"TaxResidencyCountry", "AgreedCertification"})
    public void negative_test_missing_mandatory_fields_400_response(String mandatoryField) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11544: CRS - Negative Test - Missing Mandatory Fields - 400 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");
        AND("The details do not satisfy the data validation");

        JSONObject crsMissingMandatoryField =
                generateCRSDetails("Country", "AE", mandatoryField, null);
        this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crsMissingMandatoryField, connectionId, 400);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Bad Request");

        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_post_connection_id_does_not_exist_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11463: CRS - AC4 - ConnectionId does not exist - 404 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");

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

        String randomId = UUID.randomUUID().toString();

        OBErrorResponse1 response =
                this.taxResidencyApiV2.addCRSDetailsChildError(alphaTestUser, crs, randomId, 404);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 404 Not Found");
        assertNotNull(response);
        DONE();
    }

    @Order(3)
    @Test
    public void negative_test_get_connection_id_does_not_exist_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11463: CRS - AC4 - ConnectionId does not exist - 404 Response");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");

        String randomId = UUID.randomUUID().toString();

        OBErrorResponse1 response =
                this.taxResidencyApiV2.getTaxInformationChildError(alphaTestUser,randomId, 404);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 404 Not Found");
        assertNotNull(response);
        DONE();
    }

    @Order(100)
    @Test
    public void negative_test_post_and_get_with_child_that_does_not_exist() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11463: CRS - AC4 - Customer not found - 404 Not Found");
        AlphaTestUser parent = new AlphaTestUser();
        AlphaTestUser child = new AlphaTestUser();

        parent = this.alphaTestUserFactory.setupCustomer(parent);
        String childIdNew = this.alphaTestUserFactory.createChildInForgerock(parent);
        String relationshipId = this.alphaTestUserFactory.createChildInCRM(parent,
                this.alphaTestUserFactory.generateDependantBody(childIdNew, 15, "Test User", OBGender.MALE, OBRelationshipRole.FATHER));
        child.setUserId(childIdNew);
        child = this.alphaTestUserFactory.createChildCustomer(parent, child, relationshipId, childIdNew);

        GIVEN("A child is deleted from CRM");
        AND("The connectionId is still valid");
        OBReadRelationship1 check = this.relationshipApi.getRelationships(parent);
        assertEquals(childIdNew, check.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(relationshipId, check.getData().getRelationships().get(0).getConnectionId().toString());

        this.customerApiV2.deleteCustomer(child);
        WHEN("A client attempts to POST or GET CRS details");

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

        OBErrorResponse1 errorPost =
                this.taxResidencyApiV2.addCRSDetailsChildError(parent, crs, relationshipId, 404);
        OBErrorResponse1 errorGet =
                this.taxResidencyApiV2.getTaxInformationChildError(parent, relationshipId, 404);
        assertNotNull(errorPost);
        assertNotNull(errorGet);
        THEN("The platform will return a 404");
        DONE();
    }

    @Test
    public void negative_test_post_crs_details_null_data_400_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11546: CRS - Negative Test - Post CRS details with missing data field");
        setupTestUsers();

        GIVEN("The client wants to store the CRS details against the child");

        JSONObject crs =
                generateCRSDetails("Country", "AE", "AgreedCertification", "true");
        crs.remove("Data");

        OBErrorResponse1 response =
                this.taxResidencyApiV2.addCRSDetailsChildJson(alphaTestUser, crs, connectionId, 400).as(OBErrorResponse1.class);

        WHEN("The client sends the CRS information");
        THEN("The platform will return a 400 Response");
        assertNotNull(response);
        DONE();
    }

    private JSONObject generateCRSDetails(String countryField, String countryChange, String dataField, String dataChange) {

        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("TaxResidencyCountry", new JSONObject() {
                            {
                                put("Country", "AE");
                                put("TinNumber", "ABC456789");
                                put("MissingTinReason", "Country doesn't issue TINs to its residents");
                                put(countryField, countryChange);
                            }
                        });
                        put("UaeResidencyByInvestmentScheme", "true");
                        put("OtherResidencyJurisdictions", "false");
                        put("PersonalIncomeTaxJurisdictions", List.of("CN"));
                        put("AgreedCertification", "true");
                        put(dataField, dataChange);
                    }
                });
            }
        };
    }

}
