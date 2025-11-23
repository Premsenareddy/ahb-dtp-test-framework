package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;


@Tag("@BuildCycle5.1")
@Tag("@BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateChildInCRMTests {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private CustomerApiV2 customerApi;

    private static final String CREATE_A_CHILD_OVER_18_ERROR = "The customer is not a child, must be under 18";

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserFresh;
    private AlphaTestUser alphaTestUserMale;

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    private void setupTestUserFresh() {
        this.alphaTestUserFresh = new AlphaTestUser();
        this.alphaTestUserFresh = this.alphaTestUserFactory.setupCustomer(this.alphaTestUserFresh);
    }

    private void setupTestUserMale() {
        this.alphaTestUserMale = new AlphaTestUser();
        this.alphaTestUserMale = this.alphaTestUserFactory.setupCustomer(this.alphaTestUserMale);
        OBWritePartialCustomer1 patchDetails = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .gender(OBGender.MALE)
                        .build())
                .build();
        this.customerApi.patchCustomerSuccess(this.alphaTestUserMale, patchDetails);
    }

    @Test
    @Tag("AHBDB-6981")
    public void happy_path_create_child_201_response() {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9166: AC1 Create child in CRM - 201 response");
        TEST("AHBDB-9854: AC1 T&Cs data - 201 response");
        setupTestUserFresh();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");
        AND("The child exists in Forgerock with a UserID");

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUserFresh, request);

        assertNotNull(response.getUserId());
        String dependantId = response.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.MOTHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();
        WHEN("The client attempts to create a child account on CRM with a child userID that has a paired relationship with the parents userID (in Redis)");
        this.relationshipApi.createDependant(this.alphaTestUserFresh, obWriteDependant1);
        THEN("The platform will create the child record and store the relevant information");
        AND("We will return a 201 created response");
        AND("We will create the relationship");
        AND("We will return the customerID");
        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(alphaTestUserFresh);
        assertEquals(dependantId, getResponse.getData().getRelationships().get(0).getCustomerId().toString());
    }

    @ParameterizedTest
    @CsvSource({"MALE, SON", "FEMALE, DAUGHTER"})
    public void mother_child_create_child_CRM_success_201_response(String childGender, String childRole) {
        TEST("AHBDB-6177: Create child in CRM - 201 response");
        TEST("AHBDB-9167: AC1 Create child in CRM - 201 response");
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");
        AND("The child exists in Forgerock with a userID");
        setupTestUserFresh();

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUserFresh, request);

        assertNotNull(response.getUserId());
        String dependantId = response.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.valueOf(childGender))
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.MOTHER)
                        .dependantRole(OBRelationshipRole.valueOf(childRole))
                        .build())
                .build();

        WHEN("The client attempts to create a child account on CRM with a child userID that has a paired relationship with the parents userID (in Redis)");
        THEN("The platform will create the child record and store the relevant information");
        OBReadRelationship1 createDependantResponse =
                this.relationshipApi.createDependant(this.alphaTestUserFresh, obWriteDependant1);
        AND("We will return a 201 created response");
        AND("We will create the relationship");
        AND("We will return the customerID");

        assertEquals(dependantId, createDependantResponse.getData().getRelationships().get(0).getCustomerId().toString(),
                "The ID of child does not match, expected: " + dependantId);
        assertNotNull(createDependantResponse.getData().getRelationships().get(0).getConnectionId().toString(),
                "ConnectionID not created");
    }

    @ParameterizedTest
    @CsvSource({"MALE, SON", "FEMALE, DAUGHTER"})
    public void father_child_create_child_CRM_success_201_response(String childGender, String childRole) {
        TEST("AHBDB-6177: Create child in CRM - 201 response");
        TEST("AHBDB-9168: AC1 Create child in CRM - 201 response");
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");
        AND("The child exists in Forgerock with a userID");
        setupTestUserMale();

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUserMale, request);

        assertNotNull(response.getUserId());
        String dependantId = response.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.valueOf(childGender))
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.FATHER)
                        .dependantRole(OBRelationshipRole.valueOf(childRole))
                        .build())
                .build();

        WHEN("The client attempts to create a child account on CRM with a child userID that has a paired relationship with the parents userID (in Redis)");
        THEN("The platform will create the child record and store the relevant information");
        OBReadRelationship1 createDependantResponse =
                this.relationshipApi.createDependant(this.alphaTestUserMale, obWriteDependant1);
        AND("We will return a 201 created response");
        AND("We will create the relationship");
        AND("We will return the customerID");

        assertEquals(dependantId, createDependantResponse.getData().getRelationships().get(0).getCustomerId().toString(),
                "The ID of child does not match, expected: " + dependantId);
        assertNotNull(createDependantResponse.getData().getRelationships().get(0).getConnectionId().toString(),
                "ConnectionID not created");
    }

    @Test
    public void relationship_not_paired_403_response() {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9170: AC2 Relationship not paired - 403 response");
        setupTestUser();
        setupTestUserFresh();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");
        AND("The child exists in Forgerock with a UserID");

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUserFresh, request);

        assertNotNull(response.getUserId());
        String dependantId = response.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.FATHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();
        WHEN("The client attempts to create a child account on CRM with a child userID that does" +
                " not have a paired relationship with the parents userID (in Redis)");

        this.relationshipApi.createDependantError(this.alphaTestUser, obWriteDependant1, 403);
        THEN("The platform does not create the child's record in CRM");
        AND("The platform will return a 403");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345678", "!@£$%#", "abcvdeAsdaCASKL"})
    public void invalid_childId_400_response(String invalidId) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9172: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("Id", invalidId);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345678", "2007-99-99", "abcvdeAsdaCASKL"})
    public void invalid_dateOfBirth_400_response(String invalidDob) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9173: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("DateOfBirth", invalidDob);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "12345678", "!@£$%#", "test.Test"})
    public void invalid_fullName_400_response(String invalidName) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9189: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody =
                this.relationshipApi.createDependentBodyJsonChange("FullName", invalidName);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @Test
    public void invalid_fullName_max_length_400_response() {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9191: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        String invalidName = RandomStringUtils.randomAlphabetic(201).toLowerCase();

        JSONObject dependantBody =
                this.relationshipApi.createDependentBodyJsonChange("FullName", invalidName);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345678", "!@£$%#", "male", "female"})
    public void invalid_gender_400_response(String invalidGender) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9192: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("Id", invalidGender);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345678", "!@£$%#", "abcvdeAsdaCASKL"})
    public void invalid_language_400_response(String invalidLanguage) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9194: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("Id", invalidLanguage);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @Tag("AHBDB-6981")
    @ValueSource(strings = {"", "12345678", "!@£$%#", "abcvdeAsdaCASKL"})
    public void invalid_termsVersion_400_response(String invalidTermsVersion) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9198: AC3 Create child in CRM - 400 response");
        TEST("AHBDB-9856: AC2 Negative Test - invalid TermsVersion - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("Id", invalidTermsVersion);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @Tag("AHBDB-6981")
    @ValueSource(strings = {"", "12345678", "!@£$%#", "abcvdeAsdaCASKL"})
    public void invalid_termsAccepted_400_response(String invalidTermsAccepted) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9199: AC3 Create child in CRM - 400 response");
        TEST("AHBDB-9855: Negative Test - invalid TermsAccepted - 400 Bad Request");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("Id", invalidTermsAccepted);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345678", "!@£$%#", "abcvdeAsdaCASKL"})
    public void invalid_customerRole_400_response(String invalidCustomerRole) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9200: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("Id", invalidCustomerRole);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345678", "!@£$%#", "abcvdeAsdaCASKL"})
    public void invalid_dependantRole_400_response(String invalidDependantRole) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9201: AC3 Create child in CRM - 400 response");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject dependantBody = this.relationshipApi.createDependentBodyJsonChange("Id", invalidDependantRole);

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, dependantBody, 400);
        THEN("The platform will return a 400 bad request");
    }

    @ParameterizedTest
    @Tag("AHBDB-6981")
    @ValueSource(strings = {"Id", "DateOfBirth", "FullName", "Gender", "Language",
            "TermsVersion", "TermsAccepted", "CustomerRole", "DependantRole"})
    public void missing_mandatory_data_fields_400_response(String fieldToRemove) {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9202: AC3 Create child in CRM - 400 response");
        TEST("AHBDB-9857: AC2 Negative Test - missing TermsAccepted - 400 Bad Request");
        TEST("AHBDB-9858: AC2 Negative Test - missing TermsVersion - 400 Bad Request");
        setupTestUser();
        GIVEN("A parent is onboarded");
        AND("They want to onboard their child as a marketplace customer");

        JSONObject data = new JSONObject() {
            {
                put("Id", UUID.randomUUID());
                put("DateOfBirth", Arrays.asList(LocalDate.now().minusYears(15).getYear(),
                        LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth()));
                put("FullName", "JS TESTNAMEABC");
                put("Gender", "MALE");
                put("Language", "en");
                put("TermsVersion", Arrays.asList(LocalDate.now().getYear(),
                        LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth()));
                put("TermsAccepted", Boolean.TRUE);
                put("CustomerRole", "FATHER");
                put("DependantRole", "SON");
                remove(fieldToRemove);
            }
        };

        JSONObject bodyToSend = new JSONObject() {
            {
                put("Data", data);
            }
        };

        WHEN("The client attempts to create a child account on CRM with invalid/missing information");
        this.relationshipApi.createDependantErrorJson(this.alphaTestUser, bodyToSend, 400);
        THEN("The platform will return a 400 bad request");
    }

    @Test
    public void create_child_twice_403_response() {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9171: AC2 Relationship not paired - 403 response");
        setupTestUserFresh();
        GIVEN("A parent is onboarded");
        AND("The child exists in Forgerock with a UserID");
        AND("The child has already been created in CRM");

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUserFresh, request);

        assertNotNull(response.getUserId());
        String dependantId = response.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.FATHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();
        this.relationshipApi.createDependant(this.alphaTestUserFresh, obWriteDependant1);

        WHEN("The client attempts to create an account for the same child again");
        THEN("The platform will return a 403 Forbidden");
        this.relationshipApi.createDependantError(this.alphaTestUserFresh, obWriteDependant1, 403);

        OBReadRelationship1 getResponse  = this.relationshipApi.getRelationships(this.alphaTestUserFresh);
        assertEquals(dependantId, getResponse.getData().getRelationships().get(0).getCustomerId().toString());
        DONE();
    }

    @Test
    public void negative_test_create_child_older_than_18_400_response() {
        TEST("AHBDB-6177: Create child in CRM");
        TEST("AHBDB-9342: AC3 - Create child in CRM - 400 Bad Request");
        setupTestUserFresh();
        GIVEN("A customer is onboarded");
        AND("They have an existing relationship with a child between 18-20");
        String newUserId = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserFresh.setCustomerId(newUserId);

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();

        LoginResponse response = this.authenticateApi.createRelationshipAndUser(this.alphaTestUserFresh, request);

        WHEN("They try to create an account for that child");

//        Setting details for child
        String childId = response.getUserId();
        String fullName = "JS " + generateEnglishRandomString(10);
        LocalDate dateOfBirth = LocalDate.now().minusYears(19);

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(childId))
                        .dateOfBirth(dateOfBirth)
                        .fullName(fullName)
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.MOTHER)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        THEN("The platform will return a 400 Bad Request");
        Response postResponse =
                this.relationshipApi.createDependantError(this.alphaTestUserFresh, obWriteDependant1, 400);

        assertEquals(CREATE_A_CHILD_OVER_18_ERROR, postResponse.body().as(OBErrorResponse1.class).getMessage());
        DONE();
    }

    @Test
    public void create_two_children_in_forgerock_and_create_them_in_CRM() {
        TEST("AHBDB-11852: Unable to create child in CRM after a parent has already created one");
        setupTestUserFresh();

        GIVEN("A parent tries to onboard two children one after the other");
        String childId1 = alphaTestUserFactory.createChildInForgerock(alphaTestUserFresh, "validtestpassword");
        String childId2 = alphaTestUserFactory.createChildInForgerock(alphaTestUserFresh, "validtestpassword");
        alphaTestUserFactory.createChildInCRM(alphaTestUserFresh, alphaTestUserFactory.generateDependantBody(childId1, 15, "child one", OBGender.MALE, OBRelationshipRole.FATHER));
        WHEN("They attempt to create the second child in CRM");
        alphaTestUserFactory.createChildInCRM(alphaTestUserFresh, alphaTestUserFactory.generateDependantBody(childId2, 15, "child two", OBGender.MALE, OBRelationshipRole.FATHER));
        THEN("The child will be created in CRM");
        DONE();
    }
}
