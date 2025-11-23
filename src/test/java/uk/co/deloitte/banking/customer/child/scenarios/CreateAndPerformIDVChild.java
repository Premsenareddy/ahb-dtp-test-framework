package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateAndPerformIDVChild {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private RelationshipApi relationshipApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private AlphaTestUser alphaTestUserNegative;
    private AlphaTestUser alphaTestUserChildNegative;

    private String childId;
    private String connectionId;

    private String childIdNegative;
    private String connectionIdNegative;

    private String TEMPORARY_PASSWORD = "temporary_password";
    private String NEW_PASSWORD = "newvalidtestpassword";

    private final String FORBIDDEN_403_RESPONSE = "UAE.ACCOUNT.FORBIDDEN";

    @BeforeEach
    void ignore() {
        
    }

    private void setupTestUser() {
        if (this.alphaTestUser == null || this.alphaTestUserChild == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);

            this.childId = this.alphaTestUserFactory.createChildInForgerock(alphaTestUser, TEMPORARY_PASSWORD);

            this.alphaTestUserChild = new AlphaTestUser();

            OBWriteDependant1 obWriteDependant1 = this.alphaTestUserFactory.generateDependantBody(childId,
                    5,generateEnglishRandomString(10), OBGender.MALE, OBRelationshipRole.FATHER);

            this.connectionId = this.alphaTestUserFactory.createChildInCRM(alphaTestUser, obWriteDependant1);

            this.alphaTestUserChild.setUserPassword(TEMPORARY_PASSWORD);
            this.alphaTestUserChild = this.alphaTestUserFactory
                    .createChildCustomer(this.alphaTestUser, this.alphaTestUserChild,
                            connectionId, childId, TEMPORARY_PASSWORD, NEW_PASSWORD);

            CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                    .firstName(generateEnglishRandomString(10))
                    .lastName(generateEnglishRandomString(10))
                    .build();

            final TokenHolder childApplicant =
                    this.idNowApi.createChildApplicant(this.alphaTestUser, this.connectionId, applicantRequest);

            assertNotNull(childApplicant.getApplicantId());
            assertNotNull(childApplicant.getSdkToken());
            this.alphaTestUserChild.setApplicantId(childApplicant.getApplicantId());
        }
    }

    private void setupTestUserNegative() {
        if (this.alphaTestUserNegative == null || this.alphaTestUserChildNegative == null) {
            this.alphaTestUserNegative = new AlphaTestUser();
            this.alphaTestUserChildNegative = new AlphaTestUser();
            this.alphaTestUserNegative = this.alphaTestUserFactory.setupCustomer(this.alphaTestUserNegative);

            this.childIdNegative = this.alphaTestUserFactory.createChildInForgerock(alphaTestUserNegative, TEMPORARY_PASSWORD);

            OBWriteDependant1 obWriteDependant1 = this.alphaTestUserFactory.generateDependantBody(childIdNegative,
                    15,generateEnglishRandomString(10), OBGender.MALE, OBRelationshipRole.FATHER);

            this.connectionIdNegative = this.alphaTestUserFactory.createChildInCRM(alphaTestUserNegative, obWriteDependant1);

            this.alphaTestUserChildNegative.setUserPassword(TEMPORARY_PASSWORD);
            this.alphaTestUserChildNegative = this.alphaTestUserFactory
                    .createChildCustomer(this.alphaTestUserNegative, this.alphaTestUserChildNegative,
                            connectionIdNegative, childIdNegative, TEMPORARY_PASSWORD, NEW_PASSWORD);
        }
    }

    @Tag("SmokeTest")
    @Order(1)
    @Test
    public void happy_path_parent_requests_IDV_and_creates_applicant() {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10777: AC1, AC3 - Request IDV and Create Applicant - 200 Response");
        AlphaTestUser alphaTestUserHappyPath = new AlphaTestUser();
        AlphaTestUser alphaTestUserChildHappyPath = new AlphaTestUser();
        alphaTestUserHappyPath = this.alphaTestUserFactory.setupCustomer(alphaTestUserHappyPath);
        GIVEN("The connectionID exists in the relationship list of the parent");

        String childIdHP = this.alphaTestUserFactory.createChildInForgerock(alphaTestUserHappyPath, TEMPORARY_PASSWORD);

        OBGender childGender = alphaTestUserChildHappyPath.getGender();

        String connectionIdHP = this.alphaTestUserFactory.createChildInCRM(alphaTestUserHappyPath,
                alphaTestUserFactory.generateDependantBody(childIdHP, 15, alphaTestUserChildHappyPath.getName(), childGender, OBRelationshipRole.FATHER));

        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(alphaTestUserHappyPath);

        assertEquals(childIdHP,
                getRelationships.getData().getRelationships().get(0).getCustomerId().toString());

        assertEquals(connectionIdHP,
                getRelationships.getData().getRelationships().get(0).getConnectionId().toString());

        WHEN("Experience attempts to initiate the IDV process with the connectionID (new API)");
        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(generateEnglishRandomString(10))
                .lastName(generateEnglishRandomString(10))
                .build();

        TokenHolder childApplicant =
                this.idNowApi.createChildApplicant(alphaTestUserHappyPath, connectionIdHP, applicantRequest);
        THEN("The platform will be able to verify the relationship by matching the ConnectionID to the one which exists in the list of relationships returned for the parent");
        assertNotNull(childApplicant.getApplicantId());
        assertNotNull(childApplicant.getSdkToken());
        DONE();
    }

    @Order(1)
    @Test
    public void negative_test_relationship_not_verified_403_response() {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10778: AC2 - Relationship not verified - 403 Forbidden");
        setupTestUser();
        GIVEN("The connectionID does not exist in the relationship list of the parent");
        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(generateEnglishRandomString(10))
                .lastName(generateEnglishRandomString(10))
                .build();

        String randomRelationshipId = UUID.randomUUID().toString();

        OBErrorResponse1 error =
                this.idNowApi.createChildApplicantError(alphaTestUser, randomRelationshipId, applicantRequest, 403);
        WHEN("Experience attempts to initiate the IDV process with the connectionID (new API)");
        THEN("The connectionID will not appear in the list of relationships for the parent");
        AND("The platform will be unsuccessful in verifying the relationship");
        AND("The platform will return a 403 Forbidden");
        assertNotNull(error);
        assertEquals(FORBIDDEN_403_RESPONSE, error.getCode());

        DONE();
    }

    @Order(1)
    @Test
    @Tag("SmokeTest")
    public void happy_path_retrieve_idv_success() {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10779: AC4 - Success Response");

        setupTestUser();
        GIVEN("IDNow has finished processing the child's ID");
        AND("The verification has been successful");
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(this.alphaTestUser);
        assertEquals(this.childId, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(this.connectionId, getRelationships.getData().getRelationships().get(0).getConnectionId().toString());

        this.customerApi.getCurrentCustomer(this.alphaTestUserChild);

        WHEN("The ident status value Success is returned from IDNow");
        THEN("I will trigger an event saying IDV is completed");
        idNowApi.setIdNowAnswer(this.alphaTestUserChild, "SUCCESS");

        AND("The platform will store the raw IDNow response in the IDNow adapter alongside the userId and applicantId");
        ApplicantExtractedDTO getApplicantResult = idNowApi.getChildApplicantResults(alphaTestUser, this.connectionId);
        assertNotNull(getApplicantResult);
        assertEquals(this.alphaTestUserChild.getApplicantId(), getApplicantResult.getIdentificationProcess().get("Id"));
        assertEquals("SUCCESS", getApplicantResult.getIdentificationProcess().get("Result"));

        Map<String, Object> userData = getApplicantResult.getUserData();
        String fullName = extractValue(userData, "FullName");
        String firstName = extractValue(userData, "FirstName");
        String lastName = extractValue(userData, "LastName");
        String nationality = extractValue(userData, "Nationality");
        String gender = extractValue(userData, "Gender");

        AND("The platform will store the customer state in CRM as IDV Completed");

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

        this.customerApi.createCustomerIdvDetails(alphaTestUserChild);

        this.customerApi.updateCustomer(this.alphaTestUserChild, updateChildDetails, 200);

        OBReadCustomer1 getChildAgain = this.customerApi.getCurrentCustomer(this.alphaTestUserChild);
        OBCustomer1 childData = getChildAgain.getData().getCustomer().get(0);

        assertEquals(OBCustomerStateV1.IDV_COMPLETED, childData.getCustomerState());
        assertEquals(fullName, childData.getFullName());
        assertEquals(firstName, childData.getFirstName());
        assertEquals(lastName, childData.getLastName());
        assertEquals(nationality, childData.getNationality());
        assertEquals(OBGender.valueOf(gender.toUpperCase(Locale.ROOT)), childData.getGender());
        assertEquals(childId, childData.getCustomerId().toString());

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS_DATA_CHANGED", "REVIEW_PENDING", "FRAUD_SUSPICION_PENDING"})
    public void happy_path_retrieve_idv_pending(String idvState) {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10780: AC5 - Pending Response");
        setupTestUser();

        GIVEN("IDNow has finished processing the child's ID");
        AND("The verification is pending");

        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(this.alphaTestUser);
        assertEquals(this.childId, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(this.connectionId, getRelationships.getData().getRelationships().get(0).getConnectionId().toString());

        this.customerApi.getCurrentCustomer(this.alphaTestUserChild);

        WHEN("The ident status value 'Success Data Changed', 'Review Pending' or 'Fraud Suspicion Pending' is returned from IDNow");
        THEN("I will trigger an event saying IDV review required");
        idNowApi.setIdNowAnswer(this.alphaTestUserChild, idvState);

        ApplicantExtractedDTO getApplicantResult =
                idNowApi.getChildApplicantResults(alphaTestUser, this.connectionId);
        assertNotNull(getApplicantResult);
        assertEquals(this.alphaTestUserChild.getApplicantId(), getApplicantResult.getIdentificationProcess().get("Id").toString());
        assertEquals(idvState, getApplicantResult.getIdentificationProcess().get("Result"));

        Map<String, Object> userData = getApplicantResult.getUserData();
        String fullName = extractValue(userData, "FullName");
        String firstName = extractValue(userData, "FirstName");
        String lastName = extractValue(userData, "LastName");
        String nationality = extractValue(userData, "Nationality");
        String gender = extractValue(userData, "Gender");

        AND("The platform will store the customer state in CRM as IDV Review Required");

        OBWritePartialCustomer1 updateChildDetails = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_REVIEW_REQUIRED)
                        .fullName(fullName)
                        .firstName(firstName)
                        .lastName(lastName)
                        .nationality(nationality)
                        .gender(OBGender.valueOf(gender.toUpperCase(Locale.ROOT)))
                        .build())
                .build();

        this.customerApi.updateCustomer(this.alphaTestUserChild, updateChildDetails, 200);

        OBReadCustomer1 getChildAgain = this.customerApi.getCurrentCustomer(this.alphaTestUserChild);
        OBCustomer1 childData = getChildAgain.getData().getCustomer().get(0);

        assertEquals(OBCustomerStateV1.IDV_REVIEW_REQUIRED, childData.getCustomerState());
        assertEquals(fullName, childData.getFullName());
        assertEquals(firstName, childData.getFirstName());
        assertEquals(lastName, childData.getLastName());
        assertEquals(nationality, childData.getNationality());
        assertEquals(OBGender.valueOf(gender.toUpperCase(Locale.ROOT)), childData.getGender());
        assertEquals(childId, childData.getCustomerId().toString());

        AND("The platform will store the raw IDNow response in the IDNow adapter alongside the userId and applicantId");
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"CANCELED", "FRAUD_SUSPICION_CONFIRMED", "ABORTED"})
    public void happy_path_retrieve_idv_failed(String idvState) {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10781: AC6 - Failed Response");
        setupTestUser();

        GIVEN("IDNow has finished processing the child's ID");
        AND("The verification is failed");
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(this.alphaTestUser);
        assertEquals(this.childId, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(this.connectionId, getRelationships.getData().getRelationships().get(0).getConnectionId().toString());
        WHEN("Experience attempts to initiate the IDV process with the connectionID (new API)");

        this.customerApi.getCurrentCustomer(this.alphaTestUserChild);

        WHEN("The ident status value 'Canceled', 'Fraud Suspicion Confirmed' or 'Aborted' is returned from IDNow");
        THEN("I will trigger an event saying IDV has failed");
        idNowApi.setIdNowAnswer(this.alphaTestUserChild, idvState);

        ApplicantExtractedDTO getApplicantResult =
                idNowApi.getChildApplicantResults(alphaTestUser, this.connectionId);
        assertNotNull(getApplicantResult);
        assertEquals(this.alphaTestUserChild.getApplicantId(), getApplicantResult.getIdentificationProcess().get("Id"));
        assertEquals(idvState, getApplicantResult.getIdentificationProcess().get("Result"));

        Map<String, Object> userData = getApplicantResult.getUserData();
        String fullName = extractValue(userData, "FullName");
        String firstName = extractValue(userData, "FirstName");
        String lastName = extractValue(userData, "LastName");
        String nationality = extractValue(userData, "Nationality");
        String gender = extractValue(userData, "Gender");

        AND("The platform will store the customer state in CRM as IDV Review Required");

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

        this.customerApi.updateCustomer(this.alphaTestUserChild, updateChildDetails, 200);

        OBReadCustomer1 getChildAgain = this.customerApi.getCurrentCustomer(this.alphaTestUserChild);
        OBCustomer1 childData = getChildAgain.getData().getCustomer().get(0);

        assertEquals(OBCustomerStateV1.IDV_FAILED, childData.getCustomerState());
        assertEquals(fullName, childData.getFullName());
        assertEquals(firstName, childData.getFirstName());
        assertEquals(lastName, childData.getLastName());
        assertEquals(nationality, childData.getNationality());
        assertEquals(OBGender.valueOf(gender.toUpperCase(Locale.ROOT)), childData.getGender());
        assertEquals(childId, childData.getCustomerId().toString());


        AND("The platform will store the raw IDNow response in the IDNow adapter alongside the userId and applicantId");
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"a", "aaajhdgvfjdhsvfksjgaflsbflaksjbfflsbflaksjbddddddds", ""})
    public void negative_test_create_applicant_invalid_first_name(String invalidFirstName) {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10784: Negative Test - Create Applicant with invalid last name");
        GIVEN("A relationship ID between a parent and child exists");
        setupTestUserNegative();
        WHEN("The platform receives a request to create a child applicant");
        AND("An invalid first name is sent in the request");
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(this.alphaTestUserNegative);
        assertEquals(this.childIdNegative, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(this.connectionIdNegative, getRelationships.getData().getRelationships().get(0).getConnectionId().toString());
        THEN("The platform will return a 400 Bad Request");

        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(invalidFirstName)
                .lastName(generateEnglishRandomString(10))
                .build();

        this.idNowApi.createChildApplicantError(alphaTestUserNegative, connectionIdNegative, applicantRequest, 400);
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"a", "aaajhdgvfjdhsvfksjgaflsbflaksjbfflsbflaksjbddddddds", ""})
    public void negative_test_create_applicant_invalid_last_name(String invalidLastName) {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10785: Negative Test - Create Applicant with invalid last name");
        GIVEN("A relationship ID between a parent and child exists");
        setupTestUserNegative();
        WHEN("The platform receives a request to create a child applicant");
        AND("An invalid first name is sent in the request");
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(this.alphaTestUserNegative);
        assertEquals(this.childIdNegative, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());
        assertEquals(this.connectionIdNegative, getRelationships.getData().getRelationships().get(0).getConnectionId().toString());

        THEN("The platform will return a 400 Bad Request");

        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(generateEnglishRandomString(10))
                .lastName(invalidLastName)
                .build();

        this.idNowApi.createChildApplicantError(alphaTestUserNegative, connectionIdNegative, applicantRequest, 400);
        DONE();
    }

    @Test
    @Order(999)
    public void negative_test_create_applicant_with_deleted_child_customer() {
        TEST("AHBDB-6990: Child's IDV - Create Applicant and Perform IDV");
        TEST("AHBDB-10821: Negative Test - Create Applicant with invalid last name");
        GIVEN("A relationship ID between a parent and child exists");
        setupTestUserNegative();
        WHEN("The platform attempts to create an IDV applicant for the child");
        AND("The child does not exist");

        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(generateEnglishRandomString(10))
                .lastName(generateEnglishRandomString(10))
                .build();

        this.customerApi.deleteCustomer(alphaTestUserChildNegative);
        relationshipApi.getRelationships(alphaTestUserNegative);
        OBErrorResponse1 error = this.idNowApi.createChildApplicantError(alphaTestUserNegative,
                connectionIdNegative, applicantRequest, 403);
        THEN("The platform will respond with a 403");
        assertNotNull(error);
        DONE();
    }

}
