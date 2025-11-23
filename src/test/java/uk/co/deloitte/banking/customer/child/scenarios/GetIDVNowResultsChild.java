package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ResultEnum;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.idv.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationship1Data;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetIDVNowResultsChild {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private CustomerApiV2 customerApi;

    private static final String REQUEST_VALIDATION = "REQUEST_VALIDATION";

    private String childId;
    private String connectionId;
    private String fullName = "testUser";

    private String childId2;
    private String connectionId2;

    private AlphaTestUser alphaTestUserParent;
    private AlphaTestUser alphaTestUserParent2;
    private AlphaTestUser alphaTestUserChild;

    private void setupTestUsers(boolean rebuild) {
        

        if (this.alphaTestUserParent == null || rebuild) {
            alphaTestUserParent = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            alphaTestUserChild = new AlphaTestUser();

            childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, "validtestpassword");
            connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent,
                    alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
            alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, connectionId, childId);
        }
    }

    private void setupTestUsersNoChildDeviceRegistration(boolean rebuild) {
        

        if (this.alphaTestUserParent2 == null || rebuild) {
            alphaTestUserParent2 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            childId2 = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent2, "validtestpassword");
            connectionId2 = alphaTestUserFactory.createChildInCRM(alphaTestUserParent2,
                    alphaTestUserFactory.generateDependantBody(childId2, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
        }
    }

    private CreateApplicantRequest makeApplicantRequest() {
        return CreateApplicantRequest.builder()
                .firstName(generateEnglishRandomString(10))
                .lastName(generateEnglishRandomString(10))
                .build();
    }

    private void checkRelationship(OBReadRelationship1 relationshipData) {
        OBRelationship1Data data = relationshipData.getData().getRelationships().get(0);
        assertEquals(connectionId, data.getConnectionId().toString());
        assertEquals(childId, data.getCustomerId().toString());
    }

    @Order(1)
    @Test
    public void happy_path_retrieve_applicant_information() {
        TEST("AHBDB-7791: AC1 Retrieve Applicant Information (new API) - 200 Success Response");
        TEST("AHBDB-11785: AC1 Retrieve Applicant information (new API) - 200 success response");
        setupTestUsers(false);

        GIVEN("IDNow has processed a child's application");
        AND("The connectionID exists in the relationship list of the parent");
        OBReadRelationship1 getRelationships = relationshipApi.getRelationships(alphaTestUserParent);
        checkRelationship(getRelationships);

        String applicantId =
                idNowApi.createChildApplicant(alphaTestUserParent, connectionId, makeApplicantRequest()).getApplicantId();
        alphaTestUserChild.setApplicantId(applicantId);

        idNowApi.setIdNowAnswer(alphaTestUserChild, ResultEnum.SUCCESS.toString());

        ApplicantExtractedDTO childApplication = idNowApi.getChildApplicantResults(alphaTestUserParent, connectionId);
        WHEN("The client attempts to retrieve the applicant's full IDNow result information with a valid JWT token and connectionID");

        THEN("The platform will return a 200 response");
        AND("The platform will return the JSON related to the user ID/ transaction ID");
        assertEquals(alphaTestUserChild.getApplicantId(), childApplication.getIdentificationProcess().get("Id"));
        assertEquals("SUCCESS", childApplication.getIdentificationProcess().get("Result"));
        DONE();
    }

    @Order(2)
    @Test
    public void customer_not_found_404() {
        TEST("AHBDB-7791: AC2 Customer not found - 404 not found");
        TEST("AHBDB-11786: AC2 Customer not found - 404 not found");
        setupTestUsers(true);
        GIVEN("IDNow has processed a child's application");
        AND("The connectionID exists in the relationship list of the parent");
        AND("The child has not completed IDV");
        OBReadRelationship1 getRelationships = relationshipApi.getRelationships(alphaTestUserParent);
        checkRelationship(getRelationships);

        WHEN("The client attempts to retrieve the applicant's full IDNow result information with a user ID / transaction ID that does not exist");
        OBErrorResponse1 error =
                idNowApi.getChildApplicantResultsNegativeFlow(alphaTestUserParent, connectionId, 404);

        THEN("The platform will return a 404 not found response");
        Assertions.assertEquals("UAE.ERROR.NOT_FOUND", error.getCode());
        DONE();
    }

    @Order(3)
    @Test
    public void relationship_not_verified_403() {
        TEST("AHBDB-7791: AC3 Relationship not verified - 403 Forbidden");
        TEST("AHBDB-11787: AC3 Relationship not verified - 403 Forbidden");
        setupTestUsers(false);
        GIVEN("The connectionID does not exist in the relationship list of the parent");
        WHEN("The client attempts to retrieve the applicant's full IDNow result information with a valid JWT token and" +
                "the connection ID which does not exist");
        OBErrorResponse1 error = idNowApi.getChildApplicantResultsNegativeFlow(alphaTestUserChild,
                UUID.randomUUID().toString(), 403);

        THEN("The connectionID will not appear in the list of relationships for the parent");
        AND("The platform will be unsuccessful in verifying the relationship");
        assertEquals("UAE.ACCOUNT.FORBIDDEN", error.getCode());

        AND("The platform will return a 403 Forbidden");
        assertEquals("Relationship not verified", error.getMessage());
        DONE();
    }

    @Order(4)
    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS_DATA_CHANGED", "REVIEW_PENDING", "FRAUD_SUSPICION_PENDING"})
    public void change_customer_state_to_IDV_failed_from_IDV_review_required_200(String idvState) {
        TEST("AHBDB-7791: AC4 Change customer state to IDV failed from IDV review required - 200 response");
        TEST("AHBDB-11788: AC4 Change customer state to IDV failed from IDV review required - 200 response");
        setupTestUsers(true);
        GIVEN("The IDNow result was Review Pending or Fraud Suspicion Pending");
        TokenHolder id = this.idNowApi.createChildApplicant(alphaTestUserParent, connectionId, makeApplicantRequest());

        OBReadCustomer1 getChild = this.relationshipApi.getChildBasedOnRelationship(alphaTestUserParent, connectionId);
        assertEquals(OBCustomerStateV1.IDV_REVIEW_REQUIRED, getChild.getData().getCustomer().get(0).getCustomerState());

        alphaTestUserChild.setApplicantId(id.getApplicantId());
        idNowApi.setIdNowAnswer(alphaTestUserChild, idvState);

        ApplicantExtractedDTO getApplicantResult =
                idNowApi.getChildApplicantResults(alphaTestUserParent, this.connectionId);
        assertNotNull(getApplicantResult);
        assertEquals(this.alphaTestUserChild.getApplicantId(), getApplicantResult.getIdentificationProcess().get("Id").toString());
        assertEquals(idvState, getApplicantResult.getIdentificationProcess().get("Result"));

        Map<String, Object> userData = getApplicantResult.getUserData();
        String fullName = extractValue(userData, "FullName");
        String firstName = extractValue(userData, "FirstName");
        String lastName = extractValue(userData, "LastName");
        String nationality = extractValue(userData, "Nationality");
        String gender = extractValue(userData, "Gender");

        AND("The connectionID exists in the relationship list of the parent");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserParent);
        checkRelationship(relationships);

        WHEN("The client updates the customer profile with the customer status IDV Failed using the connection ID");
        relationshipApi.createChildIdvDetails(alphaTestUserParent, connectionId, OBResult.valueOf(idvState));

        OBWritePartialCustomer1 updateChildDetails = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_FAILED)
                        .fullName(fullName)
                        .firstName(firstName)
                        .lastName(lastName)
                        .nationality(nationality)
                        .gender(OBGender.valueOf(gender.toUpperCase(Locale.ROOT)))
                        .build())
                .build();

        customerApi.patchChildSuccess(alphaTestUserParent, updateChildDetails, connectionId);

        THEN("We will return a 200 response");
        AND("The correct details will be returned");
        OBReadCustomer1 getChildAgain = this.relationshipApi.getChildBasedOnRelationship(alphaTestUserParent, connectionId);
        assertEquals(OBCustomerStateV1.IDV_FAILED, getChildAgain.getData().getCustomer().get(0).getCustomerState());
        DONE();
    }

    @Order(5)
    @Test
    public void change_customer_state_to_IDV_completed_from_IDV_review_required_200() {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11789: AC5 Change customer state to IDV completed from IDV review required - 200 response");
        TEST("AHBDB-11790: AC6 Customer has completed IDV - 200 Response");
        setupTestUsers(true);

        TokenHolder id = idNowApi.createChildApplicant(alphaTestUserParent, connectionId, makeApplicantRequest());
        alphaTestUserChild.setApplicantId(id.getApplicantId());
        GIVEN("The IDNow result was Success Data Changed");
        OBReadCustomer1 application = relationshipApi.getChildBasedOnRelationship(alphaTestUserParent, connectionId);
        assertEquals(OBCustomerStateV1.IDV_REVIEW_REQUIRED, application.getData().getCustomer().get(0).getCustomerState());

        idNowApi.setIdNowAnswer(alphaTestUserChild, ResultEnum.SUCCESS.toString());

        ApplicantExtractedDTO getApplicantResult =
                idNowApi.getChildApplicantResults(alphaTestUserParent, this.connectionId);
        assertNotNull(getApplicantResult);
        assertEquals(this.alphaTestUserChild.getApplicantId(), getApplicantResult.getIdentificationProcess().get("Id").toString());
        assertEquals("SUCCESS", getApplicantResult.getIdentificationProcess().get("Result"));

        Map<String, Object> userData = getApplicantResult.getUserData();
        String fullName = extractValue(userData, "FullName");
        String firstName = extractValue(userData, "FirstName");
        String lastName = extractValue(userData, "LastName");
        String nationality = extractValue(userData, "Nationality");
        String gender = extractValue(userData, "Gender");
        AND("The customer has accepted that their DOB and/or Full name can be changed according to the EID information");
        AND("The connectionID exists in the relationship list of the parent");
        OBReadRelationship1 relationships = relationshipApi.getRelationships(alphaTestUserParent);
        checkRelationship(relationships);

        WHEN("The client updates the customer profile with the customer status IDV complete using the connectionID");
        relationshipApi.createChildIdvDetails(alphaTestUserParent, connectionId, OBResult.SUCCESS);

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

        customerApi.patchChildSuccess(alphaTestUserParent, updateChildDetails, connectionId);

        THEN("We will return a 200 response");
        OBReadCustomer1 getChild = relationshipApi.getChildBasedOnRelationship(alphaTestUserParent, connectionId);
        assertEquals(childId, getChild.getData().getCustomer().get(0).getCustomerId().toString());
        assertEquals(OBCustomerStateV1.IDV_COMPLETED, getChild.getData().getCustomer().get(0).getCustomerState());
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"success", "SUCCESS DATA CHANGED", "FRAUD SUSPICION CONFIRMED", "canceled",
            "REVIEW PENDING", "12345678", "!@£$%^&*()", ""})
    public void negative_test_invalid_data_field_result_400_response(String invalidResult) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("Result", invalidResult);
        WHEN("The client updates the customer profile");
        var error = relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"eOZLNV3KF1RcaDyhQhZelLqWIOdYhI6lwAtla"})
    public void negative_test_invalid_data_field_reason_400_response(String invalidReason) throws JSONException {

        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("Reason", invalidReason);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "sdfghjhgfdfghjkjhgfdfghjhgfghjhghjghghgggggggggggggggggggggg",
            "123454345454345654345654567654567654567654567865676567656765", "9999999", "zzzzzzz", "!@#$%^&^&*(*&^*()_+"})
    public void negative_test_invalid_data_field_transaction_number_400_response(String invalidTransactionNumber) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("TransactionNumber", invalidTransactionNumber);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "ABCD-EFGHI", "ABC-DEFGHI", "!@#$%^", "sdfff"})
    public void negative_test_invalid_data_field_ident_id_400_response(String invalidIdentId) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("IdentId", invalidIdentId);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "idcard", "12344", "!@#$%^", "sdfff"})
    public void negative_test_invalid_data_field_id_type_400_response(String invalidIdType) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("IdType", invalidIdType);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "xxx-xxxx-xxxxxxx-x", "123-1234-1234567-12", "23-1234-1234567-1", "3-1234-1234567-1",
            "-1234-1234567-1 ", "123-1-1234567-1", "123-12-1234567-1", "123-123-1234567-1", "123- -1234567-1",
            "123-1234-1234-1", "123-1234- -1", "123-1234-1234567-", "1234-1234-1234567-1", "123-12345-1234567-1",
            "123-1234-12345678-1", "123.1234.1234567.1", "!@#-!@#$-!@#$%%^-!", "asdfgdsvfdsdvfdfvgf",
            "2345678567876577", "@#$%^&*())(*&^%$", "ASDFGHFDSV"})
    public void negative_test_invalid_data_field_document_number_400_response(String invalidDocumentNumber) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("DocumentNumber", invalidDocumentNumber);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "12345678", "1234567890", "123", "12345678909876543", "abAAababa", "!@#$%^&*( "})
    public void negative_test_invalid_data_field_id_number_400_response(String invalidIdNumber) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("IdNumber", invalidIdNumber);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "gaggagag", "agsjdg sjbkdjhs sjhdnk ", "!@#$% ", "123456", "asdfghjklkjhgfdsasdfghjklkjhgfdsdfghjklkjhgfdfghjkljhgfghj"})
    public void negative_test_invalid_data_field_id_country_400_response(String invalidIdCountry) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("IdCountry", invalidIdCountry);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "123123", "agsjdg sjbkdjhs sjhdnk ", "!@#$% ", "123456", "asdfghjklkjhgfdsasdfghjklkjhgfdsdfghjklkjhgfdfghjkljhgfghj"})
    public void negative_test_invalid_data_field_date_of_expiry_400_response(String invalidDateOfExpiry) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("DateOfExpiry", invalidDateOfExpiry);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "1996-13-14T20:30:24+04:00", "1996-08-33T20:30:24+04:00", "ANAA-RE-MET20:30:24+04:00",
            "0000-00-00T20:30:24+04:00", "30-05-2020T20:30:24+04:00", "2020--05--30T20:30:24+04:00",
            "-05-05-2020T20:30:24+04:00", "90-05-09T20:30:24+04:00", "2020/05/30T20:30:24+04:00",
            "2020.05.30 T20:30:24+04:00", "1996-08-14T20:60:24+04:00", "1996-08-14T20:65:24+04:00",
            "1996-08-14T20:30:61+04:00", "1996-08-14T20:30:60+04:00", "1996-08-14T20:30:24*04:00",
            "1996-08-14T20:30:24+30:00", "1996-08-14T20:30:24+04:75", "1996-08-14T20:30:24-30:00",
            "1996-08-14TAA:30:24+04:00", "1996-08-14T20:BB:24+04:00", "1996-08-14T20:30:CC+04:00",
            "1996-08-14T%^:%%:&&+04:00", "1996-08-14T20/30/24+04:00", "1996-08-14T20:30:24+04.00",
            "1996-08-14T20:30:24+AA:00", "1996-08-14T20:30:24+04:AA", "1996-08-14T20:30:24+4:00",
            "1996-08-14T20:30:24+04:0"})
    public void negative_test_invalid_data_field_identification_time_400_response(String invalidIdentificationTime) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("IdentificationTime", invalidIdentificationTime);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "08e95acc-a4d9-3078-b559-9dc9a8b1e11108e95acc-a4d9-3078-b559-9dc9a8b1e11108e95acc-a4d9-3078-b559-9dc9a8b1e111",
            "a ", "!@#$%^^%$", "!", "1234567833", "sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb"})
    public void negative_test_invalid_data_field_gtc_version_400_response(String invalidGtcVersion) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("GtcVersion", invalidGtcVersion);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"", "app", "web", "asdfsdfgfd", "123456 ", "!@#$%^&"})
    public void negative_test_invalid_data_field_type_400_response(String invalidType) throws JSONException {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11791: AC7 - Invalid Data field - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails("Type", invalidType);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        DONE();
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = {"Result", "TransactionNumber", "IdentId", "IdType", "DocumentNumber", "IdNumber",
            "IdCountry", "DateOfExpiry", "IdentificationTime", "Type"})
    public void negative_test_missing_mandatory_fields_400_response(String missingMandatoryField) {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11792: AC8 - Missing mandatory fields - 400 Response");
        setupTestUsers(false);
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONIdvDetails(missingMandatoryField, null);
        WHEN("The client updates the customer profile");
        var error =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, connectionId, jObj, 400);
        THEN("The Status Code is 400");
        assertNotNull(error);
        assertEquals(REQUEST_VALIDATION, error.getCode());
        DONE();
    }

    @Order(8)
    @Test
    public void negative_test_relationship_not_verified_403_response() {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11792: AC9 - Relationship Not Verified - 403 Response");
        setupTestUsers(false);
        GIVEN("The connectionID does not exist in the list of relationships for an adult");
        WHEN("The client attempts to update a customer profile with a valid JWT token and connectionId");
        OBWritePartialCustomer1 updateChildDetails = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_FAILED)
                        .fullName(fullName)
                        .firstName(generateEnglishRandomString(10))
                        .lastName(generateEnglishRandomString(10))
                        .nationality("AE")
                        .gender(OBGender.MALE)
                        .build())
                .build();

        var error =
                customerApi.patchChildError(alphaTestUserParent, updateChildDetails, UUID.randomUUID().toString(), 404);

        JSONObject jObj = customerApi.createJSONIdvDetails("Result", "SUCCESS");

        var error2 =
                relationshipApi.createChildIdvDetailsError(alphaTestUserParent, UUID.randomUUID().toString(), jObj, 403);
        assertNotNull(error2);
        THEN("The connectionID will not appear in the list of relationships");
        AND("The platform will be unsuccessful in verifying the relationship");
        AND("The platform will return a 403 Response");
        assertNotNull(error);
        DONE();
    }

    @Order(10)
    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS", "SUCCESS_DATA_CHANGED", "FRAUD_SUSPICION_CONFIRMED", "CANCELED", "REVIEW_PENDING", "FRAUD_SUSPICION_PENDING"})
    public void positive_test_confirm_idv_states_can_be_store(String validIdvResult) {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11945: Positive Test - Confirm all enum states can be stored");
        setupTestUsersNoChildDeviceRegistration(true);
        GIVEN("A customer wants to update the customer state for a child");
        WHEN("They update the child's details");
        SecureRandom random = new SecureRandom();
        long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);

        OBIdvDetails1 idvDetails = OBIdvDetails1.builder()
                .result(OBResult.valueOf(validIdvResult))
                .reason("TECH_PHOTO")
                .transactionNumber("cb3fe4cb-abd9-4647-841f-35ad8aec6f57")
                .identId("ABC-ABCDE")
                .idType(OBIdType.IDCARD)
                .documentNumber(String.valueOf(n))
                .idNumber("123456789")
                .idCountry("AE")
                .dateOfExpiry(LocalDate.of(2022, 01, 01))
                .identificationTime(OffsetDateTime.of(2021, 01, 01, 10, 10, 0, 0, ZoneOffset.UTC))
                .gtcVersion("GTC-Version")
                .type(OBAppType.APP)
                .build();

        relationshipApi.createChildIdvDetails(alphaTestUserParent2, connectionId2, idvDetails);
        THEN("The platform will respond with a 201");
        AND("The details are persisted ");
        var details = relationshipApi.getIdvDetailsChild(alphaTestUserParent2, connectionId2);
        assertEquals(validIdvResult, details.getData().getResult().toString());
        DONE();
    }

    @Order(10)
    @ParameterizedTest
    @ValueSource(strings = {"IDCARD", "RESIDENCE_PERMIT"})
    public void positive_test_check_valid_id_types(String validIdType) {
        TEST("AHBDB-7791: Child's IDV - Get EID information and Post/Patch result information");
        TEST("AHBDB-11948: Positive Test - Confirm all enum states can be stored");
        setupTestUsersNoChildDeviceRegistration(true);
        GIVEN("A customer wants to update the customer state for a child");
        WHEN("They update the child's details");
        SecureRandom random = new SecureRandom();
        long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);

        OBIdvDetails1 idvDetails = OBIdvDetails1.builder()
                .result(OBResult.SUCCESS)
                .reason("TECH_PHOTO")
                .transactionNumber("cb3fe4cb-abd9-4647-841f-35ad8aec6f57")
                .identId("ABC-ABCDE")
                .idType(OBIdType.valueOf(validIdType))
                .documentNumber(String.valueOf(n))
                .idNumber("123456789")
                .idCountry("AE")
                .dateOfExpiry(LocalDate.of(2022, 01, 01))
                .identificationTime(OffsetDateTime.of(2021, 01, 01, 10, 10, 0, 0, ZoneOffset.UTC))
                .gtcVersion("GTC-Version")
                .type(OBAppType.APP)
                .build();

        relationshipApi.createChildIdvDetails(alphaTestUserParent2, connectionId2, idvDetails);
        THEN("The platform will respond with a 201");
        AND("The details are persisted ");
        var details = relationshipApi.getIdvDetailsChild(alphaTestUserParent2, connectionId2);
        assertEquals(validIdType, details.getData().getIdType().toString());
        DONE();
    }
}
