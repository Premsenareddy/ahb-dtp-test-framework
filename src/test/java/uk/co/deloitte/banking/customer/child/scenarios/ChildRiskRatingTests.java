package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.internalrisk.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;

@Tag("AHBDB-6998")
@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildRiskRatingTests {

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
    private IdNowApi idNowApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private SanctionsApi sanctionsApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private String connectionId;
    private String childId;
    private String fullName = "testUser";

    private static final String ERROR_CODE_FOR_BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";
    private static final String ERROR_MESSAGE_NULL_REASON = "Reason cannot be null for this internal risk rating";
    private static final String ERROR_MESSAGE_NULL_MANDATORY_FIELD = "REQUEST_VALIDATION";

    private static final String VALID_RATING = "INCREASED";
    private static final String VALID_REASON = "NATIONALITY";

    @BeforeEach
    void ignore() {
        envUtils.ignoreTestInEnv(Environments.NFT);
    }

    /*
        Tests POST for /internal/v2/relationships/{relationshipId}/customers/internal-risks
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

            CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                    .firstName(generateEnglishRandomString(10))
                    .lastName(generateEnglishRandomString(10))
                    .build();

            TokenHolder childApplicant =
                    this.idNowApi.createChildApplicant(this.alphaTestUser, this.connectionId, applicantRequest);

            assertNotNull(childApplicant.getApplicantId());
            assertNotNull(childApplicant.getSdkToken());
            this.alphaTestUserChild.setApplicantId(childApplicant.getApplicantId());

            assertNotNull(alphaTestUserChild.getApplicantId());
            idNowApi.setIdNowAnswer(this.alphaTestUserChild, "SUCCESS");

            AND("The platform will store the raw IDNow response in the IDNow adapter alongside the userId and applicantId");
            ApplicantExtractedDTO getApplicantResult = idNowApi.getChildApplicantResults(alphaTestUser, this.connectionId);
            assertEquals(this.alphaTestUserChild.getApplicantId(), getApplicantResult.getIdentificationProcess().get("Id"));
            assertEquals("SUCCESS", getApplicantResult.getIdentificationProcess().get("Result"));

            Map<String, Object> userData = getApplicantResult.getUserData();
            String fullName = extractValue(userData, "FullName");
            String firstName = extractValue(userData, "FirstName");
            String lastName = extractValue(userData, "LastName");
            String nationality = extractValue(userData, "Nationality");
            String gender = extractValue(userData, "Gender");

            OBWritePartialCustomer1 updateChildDetails = OBWritePartialCustomer1.builder()
                    .data(OBWritePartialCustomer1Data.builder()
                            .customerState(OBCustomerStateV1.IDV_COMPLETED)
                            .fullName(fullName)
                            .firstName(firstName)
                            .lastName(lastName)
                            .nationality(nationality)
                            .gender(OBGender.valueOf(gender.toUpperCase(Locale.ROOT)))
                            .build())
                    .build();

            this.relationshipApi.createChildIdvDetails(alphaTestUser, connectionId);

            this.customerApi.updateCustomer(alphaTestUserChild, updateChildDetails, 200);

            this.customerApi.getCurrentCustomer(this.alphaTestUserChild);
        }
    }

    @Order(1)
    @ParameterizedTest
    @CsvSource({"PRE_DEFINED_NEUTRAL", "NEUTRAL"})
    public void happy_path_customer_receives_internal_risk_rating_rating_only_201_response(String validRiskRatingNoReason) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11599: Internal Risk Ratings - AC1 - Customer receives an internal risk rating - 201 Response");
        setupTestUsers();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");
        AND("The risk rating is either PRE_DEFINED_NEUTRAL or NEUTRAL");

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(validRiskRatingNoReason))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 response =
                this.customerApi.addInternalRiskRatingChild(alphaTestUser,
                        obWriteInternalRiskRating1, connectionId, 201).as(OBWriteInternalRiskRatingResponse1.class);
        assertEquals(OBInternalRiskRating.valueOf(validRiskRatingNoReason), response.getData().getRating());
        assertEquals(null, response.getData().getReason());
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @CsvSource({"INCREASED, NATIONALITY", "UAE_PEP, E_NAME_CHECKER_HIT", "NON_UAE_PEP, NATURE_OF_BUSINESS", "UNACCEPTABLE, NATIONALITY"})
    public void happy_path_customer_receives_internal_risk_rating_and_reason_201_response(String validRiskRating, String validRiskReason) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11599: Internal Risk Ratings - AC1 - Customer receives an internal risk rating - 201 Response");
        setupTestUsers();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .reason(Set.of(OBRiskReason.valueOf(validRiskReason)))
                        .rating(OBInternalRiskRating.valueOf(validRiskRating))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 response =
                this.customerApi.addInternalRiskRatingChild(alphaTestUser, obWriteInternalRiskRating1,
                        connectionId, 201).as(OBWriteInternalRiskRatingResponse1.class);
        assertEquals(OBInternalRiskRating.valueOf(validRiskRating), response.getData().getRating());
        assertEquals(Set.of(OBRiskReason.valueOf(validRiskReason)), response.getData().getReason());
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @CsvSource({"INCREASED, E_NAME_CHECKER_HIT, NATIONALITY",
                "INCREASED, E_NAME_CHECKER_HIT, NATURE_OF_BUSINESS",
                "INCREASED, NATIONALITY, NATURE_OF_BUSINESS",
                "UAE_PEP, E_NAME_CHECKER_HIT, NATIONALITY",
                "UAE_PEP, E_NAME_CHECKER_HIT, NATURE_OF_BUSINESS",
                "UAE_PEP, NATIONALITY, NATURE_OF_BUSINESS",
                "NON_UAE_PEP, E_NAME_CHECKER_HIT, NATIONALITY",
                "NON_UAE_PEP, E_NAME_CHECKER_HIT, NATURE_OF_BUSINESS",
                "NON_UAE_PEP, NATIONALITY, NATURE_OF_BUSINESS",
                "UNACCEPTABLE, E_NAME_CHECKER_HIT, NATIONALITY",
                "UNACCEPTABLE, E_NAME_CHECKER_HIT, NATURE_OF_BUSINESS",
                "UNACCEPTABLE, NATIONALITY, NATURE_OF_BUSINESS"})
    public void happy_path_customer_receives_internal_risk_rating_2_reasons_201_response(String validRiskRatingTwoReasons, String validRiskReason1, String validRiskReason2) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11599: Internal Risk Ratings - AC1 - Customer receives an internal risk rating - 201 Response");
        setupTestUsers();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(validRiskRatingTwoReasons))
                        .reason(Set.of(OBRiskReason.valueOf(validRiskReason1), OBRiskReason.valueOf(validRiskReason2)))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 response =
                this.customerApi.addInternalRiskRatingChild(alphaTestUser,
                        obWriteInternalRiskRating1, connectionId, 201).as(OBWriteInternalRiskRatingResponse1.class);
        assertEquals(OBInternalRiskRating.valueOf(validRiskRatingTwoReasons), response.getData().getRating());
        assertEquals(Set.of(OBRiskReason.valueOf(validRiskReason1), OBRiskReason.valueOf(validRiskReason2)), response.getData().getReason());
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"INCREASED", "UAE_PEP", "NON_UAE_PEP", "UNACCEPTABLE"})
    public void happy_path_customer_receives_internal_risk_rating_3_reasons_201_response(String riskRatingForThreeReasons) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11599: Internal Risk Ratings - AC1 - Customer receives an internal risk rating - 201 Response");
        setupTestUsers();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");

        String riskReason1 = "E_NAME_CHECKER_HIT";
        String riskReason2 = "NATIONALITY";
        String riskReason3 = "NATURE_OF_BUSINESS";

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(riskRatingForThreeReasons))
                        .reason(Set.of(OBRiskReason.valueOf(riskReason1), OBRiskReason.valueOf(riskReason2), OBRiskReason.valueOf(riskReason3)))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 response =
                this.customerApi.addInternalRiskRatingChild(alphaTestUser,
                        obWriteInternalRiskRating1, connectionId, 201).as(OBWriteInternalRiskRatingResponse1.class);
        assertEquals(OBInternalRiskRating.valueOf(riskRatingForThreeReasons), response.getData().getRating());
        assertEquals(Set.of(OBRiskReason.valueOf(riskReason1), OBRiskReason.valueOf(riskReason2), OBRiskReason.valueOf(riskReason3)), response.getData().getReason());

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"INCREASED", "UAE_PEP", "NON_UAE_PEP", "UNACCEPTABLE"})
    public void negative_test_missing_mandatory_reason_for_certain_ratings(String validRiskRatingButMissingReason) {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11600: Internal Risk Ratings - AC2 - Invalid/missing data fields - 400 Response");
        setupTestUsers();
        GIVEN("We have received a post request from the client to store the Internal Risk Rating");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but with invalid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(validRiskRatingButMissingReason))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 400 bad request");
        OBErrorResponse1 error =
                this.customerApi.addInternalRiskRatingChild(alphaTestUser,
                        obWriteInternalRiskRating1, connectionId, 400).as(OBErrorResponse1.class);
        assertNotNull(error);
        assertEquals(ERROR_CODE_FOR_BAD_REQUEST, error.getCode());
        assertEquals(ERROR_MESSAGE_NULL_REASON, error.getMessage());
        DONE();
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(strings = {"", "increased", "neutral", "!@£$$$"})
    public void negative_test_invalid_risk_rating_400_response(String invalidRiskRating) throws JSONException {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11600: Internal Risk Ratings - AC2 - Invalid/missing data fields - 400 Response");
        setupTestUsers();
        GIVEN("We have received a post request from the client to store the Internal Risk Rating");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but with invalid field inputs");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Rating", invalidRiskRating);
        jsonObject.put("Reason", VALID_REASON);
        jsonObject.put("Timestamp", ZonedDateTime.now());

        JSONObject data = new JSONObject();
        data.put("Data", jsonObject);

        THEN("We'll receive a 400 bad request");

        OBErrorResponse1 error = this.customerApi.addInternalRiskRatingChildJson(alphaTestUser, data, connectionId,400).as(OBErrorResponse1.class);
        assertNotNull(error);
        DONE();
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(strings = {"", "e name checker hit", "neutral", "!@£$$$"})
    public void negative_test_invalid_risk_reason_400_response(String invalidRiskReason) throws JSONException {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11600: Internal Risk Ratings - AC2 - Invalid/missing data fields - 400 Response");
        setupTestUsers();
        GIVEN("We have received a post request from the client to store the Internal Risk Rating");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but with invalid field inputs");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Rating", VALID_RATING);
        jsonObject.put("Reason", invalidRiskReason);
        jsonObject.put("Timestamp", ZonedDateTime.now());

        JSONObject data = new JSONObject();
        data.put("Data", jsonObject);

        THEN("We'll receive a 400 bad request");

        OBErrorResponse1 error = this.customerApi.addInternalRiskRatingChildJson(alphaTestUser, data, connectionId,400).as(OBErrorResponse1.class);
        assertNotNull(error);
        DONE();
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(strings = {"", "2021-02-19T16:18:42", "ABC", "!@£$%^&^", "2021-02-19"})
    public void negative_test_invalid_time_stamp(String invalidTimeStamp) throws JSONException {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11600: Internal Risk Ratings - AC2 - Invalid/missing data fields - 400 Response");
        setupTestUsers();
        GIVEN("We have received a post request from the client to store the Internal Risk Rating");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but with invalid field inputs");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Rating", VALID_RATING);
        jsonObject.put("Reason", VALID_REASON);
        jsonObject.put("Timestamp", invalidTimeStamp);

        JSONObject data = new JSONObject();
        data.put("Data", jsonObject);

        THEN("We'll receive a 400 bad request");

        OBErrorResponse1 error = this.customerApi.addInternalRiskRatingChildJson(alphaTestUser, data, connectionId,400).as(OBErrorResponse1.class);
        assertNotNull(error);
        DONE();
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(strings = {"Rating", "Timestamp"})
    public void missing_mandatory_ratings_field_400_response(String missingMandatoryField) throws JSONException {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11600: Internal Risk Ratings - AC2 - Invalid/missing data fields - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store the internal risk rating");
        AND("There are missing mandatory fields");

        WHEN("The client updates the customer profile");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Rating", VALID_RATING);
        jsonObject.put("Reason", VALID_REASON);
        jsonObject.put("Timestamp", ZonedDateTime.now());
        jsonObject.remove(missingMandatoryField);

        JSONObject data = new JSONObject();
        data.put("Data", jsonObject);

        THEN("We'll receive a 400 bad request");

        OBErrorResponse1 error = this.customerApi.addInternalRiskRatingChildJson(alphaTestUser, data, connectionId,400).as(OBErrorResponse1.class);
        assertNotNull(error);
        assertEquals(ERROR_MESSAGE_NULL_MANDATORY_FIELD, error.getCode());
        DONE();
    }

    @Order(10)
    @Test
    public void negative_test_post_to_child_that_does_not_exist_404_response() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11601: Internal Risk Ratings - AC2 - Invalid/missing data fields - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store the internal risk rating");
        AND("There are missing mandatory fields");
        this.customerApi.deleteCustomer(alphaTestUserChild);
        WHEN("The client updates the customer profile");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.PRE_DEFINED_NEUTRAL)
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        OBErrorResponse1 error =
                this.customerApi.addInternalRiskRatingChild(alphaTestUser, obWriteInternalRiskRating1, connectionId, 404).as(OBErrorResponse1.class);
        THEN("The platform will return a 404 Response");
        assertNotNull(error);
        DONE();
    }

    @Test
    public void negative_test_post_to_child_with_invalid_connection_id() {
        TEST("AHBDB-6998: Banking information capture for child");
        TEST("AHBDB-11602: Internal Risk Ratings - Negative Test - Invalid ConnectionId - 400 Response");
        setupTestUsers();
        GIVEN("The client wants to store the internal risk rating");

        WHEN("The client updates the customer profile");
        AND("The relationship ID used is invalid");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.PRE_DEFINED_NEUTRAL)
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        OBErrorResponse1 error =
                this.customerApi.addInternalRiskRatingChild(alphaTestUser, obWriteInternalRiskRating1, UUID.randomUUID().toString(), 404).as(OBErrorResponse1.class);
        THEN("The platform will return a 404 Response");
        assertNotNull(error);
        DONE();
    }
}
