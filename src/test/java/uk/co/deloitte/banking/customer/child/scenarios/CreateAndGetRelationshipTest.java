package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.relationship.model.*;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateAndGetRelationshipTest {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;
    private AlphaTestUser alphaTestUser3;
    private AlphaTestUser alphaTestUser4;
    private AlphaTestUser alphaTestUser5;
    private AlphaTestUser alphaTestUser6;
    private AlphaTestUser alphaTestUser7;
    private AlphaTestUser alphaTestUser8;

    private final String CONFLICT_MESSAGE = "Only one parent relationship allowed";
    private final String CONFLICT_CODE = "0003";
    private final String NULL_MESSAGE = "must not be null";
    private final String REQUEST_VALIDATION = "REQUEST_VALIDATION";
    static String childId;
    static String connectionId;

    private void setupTestUser() {
        //ignoring in SIT and NFT as the story is not deployed

        
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    private void setupTestUser2() {
        
        if (this.alphaTestUser2 == null) {
            this.alphaTestUser2 = new AlphaTestUser();
            this.alphaTestUser2 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser2);
        }
    }

    private void setupTestUser3() {
        
        if (this.alphaTestUser3 == null) {
            this.alphaTestUser3 = new AlphaTestUser();
            this.alphaTestUser3 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser3);
        }
    }

    private void setupTestUser4() {
        
        if (this.alphaTestUser4 == null) {
            this.alphaTestUser4 = new AlphaTestUser();
            this.alphaTestUser4 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser4);
        }
    }

    private void setupTestUser5() {
        
        if (this.alphaTestUser5 == null) {
            this.alphaTestUser5 = new AlphaTestUser();
            this.alphaTestUser5 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser5);
            OBWritePartialCustomer1 patchDetails = OBWritePartialCustomer1.builder()
                    .data(OBWritePartialCustomer1Data.builder()
                            .gender(OBGender.MALE)
                            .build())
                    .build();
            this.customerApi.patchCustomerSuccess(this.alphaTestUser5, patchDetails);
        }
    }

    private void setupTestUser6() {
        
        if (this.alphaTestUser6 == null) {
            this.alphaTestUser6 = new AlphaTestUser();
            this.alphaTestUser6 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser6);
        }
    }

    private void setupTestUser7() {
        
        if (this.alphaTestUser7 == null) {
            this.alphaTestUser7 = new AlphaTestUser();
            this.alphaTestUser7 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser7);
        }
    }

    private void setupTestUser8() {
        
        if (this.alphaTestUser8 == null) {
            this.alphaTestUser8 = new AlphaTestUser();
            this.alphaTestUser8 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser8);
        }
    }

    @Test
    @Order(2)
    void create_relationship_and_get_relationship_success() {
        TEST("AHBDB-6180: DTP API - Create and Get relationship");
        TEST("AHBDB-9129: AC3 - 201 Created Response ");
        TEST("AHBDB-9129: AC6 - GET request for an existing relationship");

        GIVEN("A customer has been onboarded as a marketplace/banking customer");
        AND("Their daughter has also been set up");
//        User1 = Parent, User2 = Child
        setupTestUser();
        setupTestUser2();

//        Set customer IDs for both alphaTestUsers
        String customerId1 = this.customerApi.getCurrentCustomer(alphaTestUser)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId1);

        String customerId2 = this.customerApi.getCurrentCustomer(alphaTestUser2)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser2.setCustomerId(customerId2);

        WHEN("We want to create a new relationship with an existing customer");
        OBWriteRelationship1 obWriteRelationship1 = OBWriteRelationship1.builder()
                .data(OBWriteRelationship1Data.builder()
                                .relationCustomerId(UUID.fromString(customerId2))
                                .customerRole(OBRelationshipRole.MOTHER)
                                .relationCustomerRole(OBRelationshipRole.DAUGHTER)
                                .build())
                .build();

        OBReadRelationship1 postResponse = this.relationshipApi.createRelationship(alphaTestUser, obWriteRelationship1, 201);
        String relationshipId = postResponse.getData().getRelationships().get(0).getConnectionId().toString();
        THEN("We receive a 201 created response");

//        Start of GET ACs
//        This one relates to getting a specific relationship
        GIVEN("A relationship exists between two customers");
        WHEN("A client wants to retrieve the relationship between the two customers");
        THEN("A 200 response is returned");
        OBReadCustomer1 getRelationshipBetweenTwoCustomers = this.relationshipApi.getChildBasedOnRelationship(alphaTestUser, relationshipId);
        AND("The existing relationship between the two customers is returned");

//        This one retrieves all relationships for a customer
        GIVEN("A customer has an existing relationship");
        WHEN("A client wants to retrieve the relationships associated with that customer");
        THEN("The platform will return a 200 response");

        NOTE("Checking relationships for the parent");
        OBReadRelationship1 getAllRelationshipsParent = this.relationshipApi.getRelationships(alphaTestUser);

        AND("All existing relationships will be returned");
        assertEquals(customerId1, getAllRelationshipsParent.getData().getCustomerId().toString(),
                "Customer ID of parent does not match");
        assertEquals(customerId2, getAllRelationshipsParent.getData().getRelationships().get(0).getCustomerId().toString(),
                "Customer ID of the child does not match");
        assertEquals(OBRelationshipRole.DAUGHTER, getAllRelationshipsParent.getData().getRelationships().get(0).getRole(),
                "Customer Role does not match");

        NOTE("Checking relationships for the child");
        OBReadRelationship1 getAllRelationshipsChild = this.relationshipApi.getRelationships(alphaTestUser2);
        assertEquals(customerId2, getAllRelationshipsChild.getData().getCustomerId().toString(),
                "Customer ID of child does not match");
        assertEquals(customerId1, getAllRelationshipsChild.getData().getRelationships().get(0).getCustomerId().toString(),
                "Customer ID of the parent does not match");
        assertEquals(OBRelationshipRole.MOTHER, getAllRelationshipsChild.getData().getRelationships().get(0).getRole(),
                "Customer Role does not match");

        DONE();
    }

    @Test
    void create_multiple_relationships_success() {
        TEST("AHBDB-6180: DTP API - Create and Get relationship");
        TEST("AHBDB-9132: AC3 - 201 Created Response and AC 6 - 201 Created Response");

        GIVEN("Three customers have been onboarded as a marketplace/banking customers");
        setupTestUser6(); //child
        setupTestUser7(); //child
        setupTestUser8(); //parent

        String customerId6 = this.customerApi.getCurrentCustomer(alphaTestUser6)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser6.setCustomerId(customerId6);

        String customerId7 = this.customerApi.getCurrentCustomer(alphaTestUser7)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser7.setCustomerId(customerId7);

        WHEN("We want to create a new relationship between MOTHER and a DAUGHTER.");
        OBWriteRelationship1 obWriteRelationship1 = OBWriteRelationship1.builder()
                .data(OBWriteRelationship1Data.builder()
                        .relationCustomerId(UUID.fromString(customerId6))
                        .customerRole(OBRelationshipRole.MOTHER)
                        .relationCustomerRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        OBReadRelationship1 postResponse = this.relationshipApi.createRelationship(alphaTestUser8, obWriteRelationship1, 201);
        String relationshipId = postResponse.getData().getRelationships().get(0).getConnectionId().toString();
        THEN("We receive a 201 created response");

        WHEN("We want to create a new relationship between MOTHER and another DAUGHTER.");
        OBWriteRelationship1 obWriteRelationship2 = OBWriteRelationship1.builder()
                .data(OBWriteRelationship1Data.builder()
                        .relationCustomerId(UUID.fromString(customerId7))
                        .customerRole(OBRelationshipRole.MOTHER)
                        .relationCustomerRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        OBReadRelationship1 postResponse2 = this.relationshipApi.createRelationship(alphaTestUser8, obWriteRelationship2, 201);
        String relationshipId2 = postResponse2.getData().getRelationships().get(0).getConnectionId().toString();
        THEN("We receive a 201 created response");

        NOTE("Checking relationships for the parent");
        OBReadRelationship1 getAllRelationshipsParent = this.relationshipApi.getRelationships(alphaTestUser8);

        AND("All existing relationships will be returned");

        assertEquals(alphaTestUser8.getUserId(), getAllRelationshipsParent.getData().getCustomerId().toString(),
                "Customer ID of parent does not match");

        ArrayList<String> listOfIds = new ArrayList<>();
        ArrayList<String> listOfRelationshipIds= new ArrayList<>();

        // store children Ids from GET
        String daughterId1 = getAllRelationshipsParent.getData().getRelationships().get(0).getCustomerId().toString();
        String daughterId2 = getAllRelationshipsParent.getData().getRelationships().get(1).getCustomerId().toString();
        Collections.addAll(listOfIds, daughterId1, daughterId2);

        //store relationship Ids from GET
        String relationshipIdChild1 = getAllRelationshipsParent.getData().getRelationships().get(0).getConnectionId().toString();
        String relationshipIdChild2 = getAllRelationshipsParent.getData().getRelationships().get(1).getConnectionId().toString();
        Collections.addAll(listOfRelationshipIds, relationshipIdChild1, relationshipIdChild2);

        //assert with actual child and relationship Ids
        Assertions.assertTrue(listOfIds.contains(customerId6));
        Assertions.assertTrue(listOfIds.contains(customerId7));

        Assertions.assertTrue(listOfRelationshipIds.contains(relationshipId));
        Assertions.assertTrue(listOfRelationshipIds.contains(relationshipId2));

        assertEquals(OBRelationshipRole.DAUGHTER, getAllRelationshipsParent.getData().getRelationships().get(0).getRole(),
                "Customer Role does not match");
        assertEquals(OBRelationshipRole.DAUGHTER, getAllRelationshipsParent.getData().getRelationships().get(0).getRole() ,
                "Customer Role does not match");

        DONE();
    }

    @Test
    void create_child_and_relationship_success() {
        TEST("AHBDB-6180: DTP API - Create and Get relationship");
        TEST("AHBDB-9131: AC1 - POST Request from Client to Customer Adapter ");

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();
        GIVEN("A parent exists.");
        setupTestUser5();

        AND("A child is created in Forgerock.");
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser5, request);
        assertNotNull(response);

        assertNotNull(response.getUserId());
        childId = response.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(childId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("Fullname")
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.FATHER)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        WHEN("Calling post to create dependant and relationship");
        OBReadRelationship1 response2 = this.relationshipApi.createDependant(alphaTestUser5, obWriteDependant1);

        THEN("Status 201(CREATED) is returned");
        assertNotNull(response2);

        AND("Relationship contains onboarded by");
        assertEquals(alphaTestUser5.getUserId(), response2.getData().getRelationships().get(0).getOnboardedBy());
        connectionId = response2.getData().getRelationships().get(0).getConnectionId().toString();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser5);
        THEN("Status 200(OK) is returned");
        assertNotNull(relationships);

        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(OBGender.MALE, relationships.getData().getGender());
        assertEquals(alphaTestUser5.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());
    }

    @Test
    public void create_relationship_that_already_exists_409_response() {
        TEST("AHBDB-6180: Create and Get relationship");
        TEST("AHBDB-9133: AC4 - Customer Relationship Already Exists - 409 Conflict Response");

        GIVEN("Three customers are onboarded");
        setupTestUser3(); //MOTHER
        setupTestUser4(); //child
        setupTestUser5(); //FATHER

        String customerId4 = this.customerApi.getCurrentCustomer(alphaTestUser4)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser4.setCustomerId(customerId4);

        AND("The Mother has relationship with the child.");
        OBWriteRelationship1 obWriteRelationship1 = OBWriteRelationship1.builder()
                .data(OBWriteRelationship1Data.builder()
                        .relationCustomerId(UUID.fromString(customerId4))
                        .customerRole(OBRelationshipRole.MOTHER)
                        .relationCustomerRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        OBReadRelationship1 postResponse = this.relationshipApi.createRelationship(alphaTestUser3, obWriteRelationship1, 201);

        WHEN("We want to create the same relationship again.");

        JSONObject jsonObject =
                this.relationshipApi.createRelationshipBodyJson(customerId4,"MOTHER", "DAUGHTER");

        OBErrorResponse1 postResponse2 = this.relationshipApi.createRelationshipError(alphaTestUser3, jsonObject, 409);
        THEN("A 409 Conflict is returned");
        assertEquals("UAE.ERROR.CONFLICT", postResponse2.getCode(), "Error code not as expected, expected: " + "UAE.ERROR.CONFLICT");
        assertEquals(CONFLICT_MESSAGE, postResponse2.getMessage(), "Error message not as expected, expected: " + CONFLICT_MESSAGE);

        WHEN("The Father wants to create the relationship with the same child. ");
        JSONObject jsonObject2 =
                this.relationshipApi.createRelationshipBodyJson(customerId4,"FATHER", "DAUGHTER");

        THEN("A 409 Conflict is returned");
        OBErrorResponse1 postResponse3 = this.relationshipApi.createRelationshipError(alphaTestUser5, jsonObject2, 409);
        assertEquals("UAE.ERROR.CONFLICT", postResponse2.getCode(), "Error code not as expected, expected: " + "UAE.ERROR.CONFLICT");
        assertEquals(CONFLICT_MESSAGE, postResponse2.getMessage(), "Error message not as expected, expected: " + CONFLICT_MESSAGE);

        DONE();
    }

    @Test
    public void get_with_no_existing_relationships_200_response() {
        
        TEST("AHBDB-6180: AC8 - 200 Success Response");
        TEST("AHBDB-9134: AC8 - Get existing relations/Customer Relationship Does NOT Exist");

        GIVEN("A customer is onboarded");
        AND("They have no pre-existing relationships");
        AlphaTestUser freshUser = new AlphaTestUser();
        freshUser = this.alphaTestUserFactory.setupCustomer(freshUser);
        WHEN("A client retrieves the list of relationships for the customer");
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(freshUser);
        THEN("The platform returns a 200 response");
        AND("The list of relationships is empty");
        assertNotNull(getRelationships);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "father", "mother", "son", "daughter", "123456789", "!@#$%^&*()"})
    void invalid_customer_role_400_bad_request(String invalidCustomerRole) throws JSONException {
        TEST("AHBDB-6180 - Create and Get relationship");
        TEST("AHBDB-9136 : Invalid Customer Role - 400 response");

        GIVEN("A customer exists.");
        setupTestUser();

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        JSONObject jsonObject = this.relationshipApi.createRelationshipBodyJson(customerId, invalidCustomerRole, "MOTHER");

        WHEN("Sending the Request with invalid customer role.");
        OBErrorResponse1 obErrorResponse1 = this.relationshipApi.createRelationshipError(alphaTestUser, jsonObject, 400);

        THEN("Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "father", "mother", "son", "daughter", "123456789", "!@#$%^&*()"})
    void invalid_relation_customer_role_400_bad_request(String invalidRelationCustomerRole) throws JSONException {
        TEST("AHBDB-6180 - Create and Get relationship");
        TEST("AHBDB-9130 : Invalid Relation Customer Role - 400 response");

        GIVEN("A customer exists.");
        setupTestUser();

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        JSONObject jsonObject =
                this.relationshipApi.createRelationshipBodyJson(customerId,"MOTHER", invalidRelationCustomerRole);

        WHEN("Sending the Request with invalid relation customer role.");
        OBErrorResponse1 obErrorResponse1 =
                this.relationshipApi.createRelationshipError(alphaTestUser, jsonObject, 400);

        THEN("Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abcdfghsk", "123456789", "!@#$%^&*()"})
    void invalid_relation_customer_id_400_bad_request(String invalidRelationCustomerId) throws JSONException {
        TEST("AHBDB-6180 - Create and Get relationship");
        TEST("AHBDB-9137 : Invalid Customer Id - 400 response");

        GIVEN("A customer exists.");
        setupTestUser();

        JSONObject jsonObject = this.relationshipApi.createRelationshipBodyJson(invalidRelationCustomerId,"MOTHER", "SON");

        WHEN("Sending the Request with invalid customer id.");
        OBErrorResponse1 obErrorResponse1 = this.relationshipApi.createRelationshipError(alphaTestUser, jsonObject, 400);

        THEN("Status Code is 400");

        DONE();
    }

    @Test
    void null_relation_customer_id_400_bad_request() throws JSONException {
        TEST("AHBDB-6180 - Create and Get relationship");
        TEST("AHBDB-9138 : Null Customer Id - 400 response");

        GIVEN("A customer exists.");
        setupTestUser();

        JSONObject jsonObject = this.relationshipApi.createRelationshipBodyJson(null,"MOTHER", "SON");

        WHEN("Sending the Request with null customer id.");
        OBErrorResponse1 obErrorResponse1 = this.relationshipApi.createRelationshipError(alphaTestUser, jsonObject, 400);

        THEN("Status Code is 400");
        assertEquals(REQUEST_VALIDATION, obErrorResponse1.getCode(), "Error code not as expected, expected: " + REQUEST_VALIDATION);
        assertTrue(obErrorResponse1.getMessage().contains(NULL_MESSAGE));

        DONE();
    }

    @Test
    void null_customer_role_400_bad_request() throws JSONException {
        TEST("AHBDB-6180 - Create and Get relationship");
        TEST("AHBDB-9139 : Null Customer Role - 400 response");

        GIVEN("A customer exists.");
        setupTestUser();
        setupTestUser2();

        String customerId2 = this.customerApi.getCurrentCustomer(alphaTestUser2)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser2.setCustomerId(customerId2);

        JSONObject jsonObject = this.relationshipApi.createRelationshipBodyJson(customerId2,null, "SON");

        WHEN("Sending the Request with null customer role.");
        OBErrorResponse1 obErrorResponse1 = this.relationshipApi.createRelationshipError(alphaTestUser, jsonObject, 400);

        THEN("Status Code is 400");
        assertEquals(REQUEST_VALIDATION, obErrorResponse1.getCode(), "Error code not as expected, expected: " + REQUEST_VALIDATION);
        assertTrue(obErrorResponse1.getMessage().contains(NULL_MESSAGE));

        DONE();
    }

    @Test
    void null_relation_customer_role_400_bad_request() throws JSONException {
        TEST("AHBDB-6180 - Create and Get relationship");
        TEST("AHBDB-9135 :  Null Relation Customer Role - 400 response");

        GIVEN("A customer exists.");
        setupTestUser();
        setupTestUser2();

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        JSONObject jsonObject = this.relationshipApi.createRelationshipBodyJson( customerId,"MOTHER", null);

        WHEN("Sending the Request with null relation customer role.");
        OBErrorResponse1 obErrorResponse1 = this.relationshipApi.createRelationshipError(alphaTestUser, jsonObject, 400);

        THEN("Status Code is 400");
        assertEquals(REQUEST_VALIDATION, obErrorResponse1.getCode(), "Error code not as expected, expected: " + REQUEST_VALIDATION);
        assertTrue(obErrorResponse1.getMessage().contains(NULL_MESSAGE));

        DONE();
    }
}
