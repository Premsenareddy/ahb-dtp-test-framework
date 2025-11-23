package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationship1Data;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteRelationship1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;

@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RetrieveRelationships {

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserFresh;
    private AlphaTestUser alphaTestUserChild;


    private void setupTestUser() {
        envUtils.ignoreTestInEnv(Environments.NFT);

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser.setGender(OBGender.FEMALE);
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    private AlphaTestUser setupTestUserFresh() {


        AlphaTestUser alphaTestUserToSetup = new AlphaTestUser();
        alphaTestUserToSetup.setGender(OBGender.FEMALE);
        return this.alphaTestUserFactory.setupCustomer(alphaTestUserToSetup);
    }

    @Test
    public void zero_or_one_relationships_get_200_response() {
        TEST("AHBDB-6988: Retrieve parents relationships when logged in");
        TEST("AHBDB-9203: AC1 - 0 Relationships - Get Existing Relationships");
        TEST("AHBDB-9335: AC1 - 1 Relationship - Get Existing Relationships");

        GIVEN("A customer is onboarded");
        AND("They have no existing relationships");
        this.alphaTestUserFresh = setupTestUserFresh();

        WHEN("A client attempts to retrieve a list of relationships");

        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(this.alphaTestUserFresh);

        THEN("The list of relationships will be returned as empty");

        assertNotNull(getResponse);
        assertNull(getResponse.getData().getRelationships());


        GIVEN("A customer is onboarded");
        AND("They have one existing relationships");

//        Making another customer to form a relationship
        this.alphaTestUserChild = setupTestUserFresh();

        String childId = this.customerApi.getCurrentCustomer(this.alphaTestUserChild)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserChild.setCustomerId(childId);

        OBWriteRelationship1 relationshipBody = OBWriteRelationship1.builder()
                .data(OBWriteRelationship1Data.builder()
                        .relationCustomerId(UUID.fromString(childId))
                        .customerRole(OBRelationshipRole.MOTHER)
                        .relationCustomerRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        OBReadRelationship1 postResponse =
                this.relationshipApi.createRelationship(this.alphaTestUserFresh, relationshipBody, 201);
        String connectionId = postResponse.getData().getRelationships().get(0).getConnectionId().toString();
        assertEquals(OBRelationshipRole.DAUGHTER.toString(), postResponse.getData().getRelationships().get(0).getRole().toString(),
                "ChildRole does not match, expected " + OBRelationshipRole.DAUGHTER);
        assertEquals(childId, postResponse.getData().getRelationships().get(0).getCustomerId().toString(),
                "ChildID does not match, expected " + childId);

        WHEN("A client attempts to retrieve a list of relationships");

        OBReadRelationship1 getResponse2 = this.relationshipApi.getRelationships(this.alphaTestUserFresh);

        THEN("The list of relationships will be returned");
        AND("There is one existing relationship");

        assertNotNull(getResponse2);

        assertEquals(connectionId, getResponse2.getData().getRelationships().get(0).getConnectionId().toString(),
                "Connection id does not match, expected: " + connectionId);

        assertEquals(OBRelationshipRole.DAUGHTER.toString(),
                getResponse2.getData().getRelationships().get(0).getRole().toString(),
                "ChildRole does not match, expected " + OBRelationshipRole.DAUGHTER);

        assertEquals(postResponse.getData().getRelationships().get(0).getFullName(),
                getResponse2.getData().getRelationships().get(0).getFullName());

        assertEquals(OBGender.FEMALE.toString(),
                getResponse2.getData().getRelationships().get(0).getGender().toString());

        assertEquals("ACTIVE",
                getResponse2.getData().getRelationships().get(0).getStatus());

        assertEquals(this.alphaTestUserChild.getCustomerId(),
                getResponse2.getData().getRelationships().get(0).getCustomerId().toString());

        assertEquals("IDV_REVIEW_REQUIRED",
                getResponse2.getData().getRelationships().get(0).getCustomerState().toString());

        DONE();
    }

    @Test
    public void two_relationships_get_200_response() {
        TEST("AHBDB-6988: Retrieve parents relationships when logged in");
        TEST("AHBDB-9337: AC1 - Get Existing Relationships");

        GIVEN("A customer is onboarded");
        AND("They have two existing relationships");

//        newUser = parent, this.alphaTestUserChild=child 1, child2 = child 2
        AlphaTestUser newUser = new AlphaTestUser();

        newUser = this.alphaTestUserFactory.setupCustomer(newUser);
        AlphaTestUser child2 = setupTestUserFresh();

        this.alphaTestUserChild = setupTestUserFresh();

//        Setting customerIDs for the test users
        String newUserId = this.customerApi.getCurrentCustomer(newUser)
                .getData().getCustomer().get(0).getCustomerId().toString();
        newUser.setCustomerId(newUserId);

        String childId = this.customerApi.getCurrentCustomer(this.alphaTestUserChild)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserChild.setCustomerId(childId);

        String child2Id = this.customerApi.getCurrentCustomer(child2)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserChild.setCustomerId(child2Id);

//        Creating relationship bodies for child 1 and child 2
        OBWriteRelationship1 relationshipBody1 = OBWriteRelationship1.builder()
                .data(OBWriteRelationship1Data.builder()
                        .relationCustomerId(UUID.fromString(childId))
                        .customerRole(OBRelationshipRole.MOTHER)
                        .relationCustomerRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        OBWriteRelationship1 relationshipBody2 = OBWriteRelationship1.builder()
                .data(OBWriteRelationship1Data.builder()
                        .relationCustomerId(UUID.fromString(child2Id))
                        .customerRole(OBRelationshipRole.MOTHER)
                        .relationCustomerRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

//        Creating the relationships for both children
        this.relationshipApi.createRelationship(newUser, relationshipBody1, 201);

        OBReadRelationship1 postResponse2 =
                this.relationshipApi.createRelationship(newUser, relationshipBody2, 201);
        String connectionId1 = postResponse2.getData().getRelationships().get(0).getConnectionId().toString();
        String connectionId2 = postResponse2.getData().getRelationships().get(1).getConnectionId().toString();

        WHEN("A client attempts to retrieve a list of relationships");

        OBReadRelationship1 getResponse = this.relationshipApi.getRelationships(newUser);

        List<OBRelationship1Data> listOfRelationships = getResponse.getData().getRelationships();

        List<String> connectionIds = Arrays.asList(listOfRelationships.get(0).getConnectionId().toString(),
                listOfRelationships.get(1).getConnectionId().toString());

        List<String> childIds = Arrays.asList(listOfRelationships.get(0).getCustomerId().toString(),
                listOfRelationships.get(1).getCustomerId().toString());

        THEN("The list of relationships will be returned");
        AND("There are 2 existing relationships");
        AND("The details for the customers are correct");

        assertTrue(connectionIds.contains(connectionId1));
        assertTrue(connectionIds.contains(connectionId2));

        assertTrue(childIds.contains(childId));
        assertTrue(childIds.contains(child2Id));

        assertNotNull(getResponse);

        DONE();
    }

    @Test
    public void happy_path_get_profile_with_connection_id_200_response() {
        TEST("AHBDB-6988: Retrieve parents relationships when logged in");
        TEST("AHBDB-9338: AC2 - Relationship with specified connection ID exist - 200 OK");

        GIVEN("A parent is onboarded as a customer");
        AND("They have onboarded their child");
        AND("Their child exists in CRM");
        AND("A connectionID between the parent and child exists");

        this.alphaTestUserFresh = setupTestUserFresh();

        String newUserId = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserFresh.setCustomerId(newUserId);

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();

        LoginResponse response = this.authenticateApi.createRelationshipAndUser(this.alphaTestUserFresh, request);

//        Setting details for child
        String childId = response.getUserId();
        String fullName = "JS" + generateEnglishRandomString(10);
        LocalDate dateOfBirth = LocalDate.now().minusYears(6);

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

        OBReadRelationship1 postResponse =
                this.relationshipApi.createDependant(this.alphaTestUserFresh, obWriteDependant1);

        String connectionId = postResponse.getData().getRelationships().get(0).getConnectionId().toString();

        WHEN("A client attempts to retrieve the profile of the child using the parent's authorization token");
        THEN("The platform will return a 200 OK");
        AND("The child's profile will be returned");

        OBReadCustomer1 getResponse =
                this.relationshipApi.getChildBasedOnRelationship(this.alphaTestUserFresh, connectionId);

        assertEquals(childId, getResponse.getData().getCustomer().get(0).getCustomerId().toString(),
                "ChildID does not match" + childId);

        assertEquals(fullName, getResponse.getData().getCustomer().get(0).getFullName(),
                "FullName does not match");

        assertEquals(dateOfBirth, getResponse.getData().getCustomer().get(0).getDob(),
                "DateOfBirth does not match the DOB used when creating them in CRM");

        assertEquals("FEMALE", getResponse.getData().getCustomer().get(0).getGender().toString(),
                "Gender of the child does not match the gender assigned when creating them in CRM");

        assertEquals("0-12", getResponse.getData().getCustomer().get(0).getAgeGroup(),
                "AgeGroup of the child does not match their age");

        assertEquals(newUserId, getResponse.getData().getCustomer().get(0).getOnboardedBy(),
                "OnboardedBy ID does not match the parent's ID");

    }

    @Test
    public void happy_path_get_profile_using_connection_id_from_child_perspective() {
        TEST("AHBDB-9394: Test the ability to get parent profile using relationshipID from the child's device");
        GIVEN("A child has been onboarded by a parent");
        AND("The child has a registered device");
        this.alphaTestUserFresh = setupTestUserFresh();
        AlphaTestUser alphaTestUserChild = new AlphaTestUser();

        String childId = this.alphaTestUserFactory.createChildInForgerock(alphaTestUserFresh, "validtestpassword");

        OBWriteDependant1 obWriteDependant1 = this.alphaTestUserFactory.generateDependantBody(childId,
                15, generateEnglishRandomString(10), OBGender.MALE, OBRelationshipRole.FATHER);

        String connectionId = this.alphaTestUserFactory.createChildInCRM(alphaTestUserFresh, obWriteDependant1);

        this.alphaTestUserFactory.createChildCustomer(alphaTestUserFresh, alphaTestUserChild, connectionId, childId);

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserChild);
        OBRelationship1Data obRelationship1Data = relationships.getData().getRelationships().stream().filter(relationship -> alphaTestUserFresh.getUserId().equals(relationship.getCustomerId().toString())).findFirst().get();
        String connectionIdFromChild = obRelationship1Data.getConnectionId().toString();

        WHEN("They want to query for the parent's profile using the existing valid relationship ID between the two");
        THEN("The platform will return the profile of the parent");
        this.customerApi.getCurrentCustomer(alphaTestUserChild);
        OBReadCustomer1 getResponse =
                this.relationshipApi.getChildBasedOnRelationship(alphaTestUserChild, connectionIdFromChild);
        assertNotNull(getResponse);
        DONE();
    }

    @Test
    public void age_group_0_12_relationship_id_200_response() {
        TEST("AHBDB-6988: Retrieve parents relationships when logged in");
        TEST("AHBDB-9339: AC2 - Relationship with specified Connection ID exists - 200 OK");

        GIVEN("A customer is onboarded");
        AND("They have an existing relationship with a child between 0-12");
        this.alphaTestUserFresh = setupTestUserFresh();

        String newUserId = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserFresh.setCustomerId(newUserId);

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();

        LoginResponse response = this.authenticateApi.createRelationshipAndUser(this.alphaTestUserFresh, request);

//        Setting details for child
        String childId = response.getUserId();
        String fullName = "JS" + generateEnglishRandomString(10);
        LocalDate dateOfBirth = LocalDate.of(2016, 1, 4);

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

        OBReadRelationship1 postResponse = this.relationshipApi.createDependant(this.alphaTestUserFresh, obWriteDependant1);

        String connectionId = postResponse.getData().getRelationships().get(0).getConnectionId().toString();

        WHEN("The relationship with specified connection ID exists");
        AND("The relationship is verified by comparing the connection ID list returned from CRM with the provided connection ID");

        OBReadCustomer1 getResponse = this.relationshipApi.getChildBasedOnRelationship(this.alphaTestUserFresh, connectionId);

        THEN("The relation's userID will be extracted");
        AND("It will be used to retrieve the child's profile");
        assertEquals(childId, getResponse.getData().getCustomer().get(0).getCustomerId().toString(),
                "ChildID does not match" + childId);
        assertEquals(fullName, getResponse.getData().getCustomer().get(0).getFullName(),
                "FullName does not match");
        assertEquals(dateOfBirth, getResponse.getData().getCustomer().get(0).getDob());
        assertEquals("FEMALE", getResponse.getData().getCustomer().get(0).getGender().toString());
        assertEquals(this.alphaTestUserFresh.getCustomerId(), getResponse.getData().getCustomer().get(0).getOnboardedBy());
        assertEquals("0-12", getResponse.getData().getCustomer().get(0).getAgeGroup());

        DONE();
    }

    @Test
    public void age_group_13_17_relationship_id_200_response() {
        TEST("AHBDB-6988: Retrieve parents relationships when logged in");
        TEST("AHBDB-9341: AC2 - Relationship with specified Connection ID exists - 200 OK");

        GIVEN("A customer is onboarded");
        AND("They have an existing relationship with a child between 13-17");
        this.alphaTestUserFresh = setupTestUserFresh();

        String newUserId = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh)
                .getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserFresh.setCustomerId(newUserId);

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();

        LoginResponse response = this.authenticateApi.createRelationshipAndUser(this.alphaTestUserFresh, request);

//        Setting details for child
        String childId = response.getUserId();
        String fullName = "JS" + generateEnglishRandomString(10);
        LocalDate dateOfBirth = LocalDate.now().minusYears(15);

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

        OBReadRelationship1 postResponse = this.relationshipApi.createDependant(this.alphaTestUserFresh, obWriteDependant1);

        String connectionId = postResponse.getData().getRelationships().get(0).getConnectionId().toString();

        WHEN("The relationship with specified connection ID exists");
        AND("The relationship is verified by comparing the connection ID list returned from CRM with the provided connection ID");

        OBReadCustomer1 getResponse = this.relationshipApi.getChildBasedOnRelationship(this.alphaTestUserFresh, connectionId);

        THEN("The relation's userID will be extracted");
        AND("It will be used to retrieve the child's profile");
        assertEquals(childId, getResponse.getData().getCustomer().get(0).getCustomerId().toString(),
                "ChildID does not match" + childId);
        assertEquals(fullName, getResponse.getData().getCustomer().get(0).getFullName(),
                "FullName does not match");
        assertEquals(dateOfBirth, getResponse.getData().getCustomer().get(0).getDob());
        assertEquals("FEMALE", getResponse.getData().getCustomer().get(0).getGender().toString());
        assertEquals(this.alphaTestUserFresh.getCustomerId(), getResponse.getData().getCustomer().get(0).getOnboardedBy());
        assertEquals("13-17", getResponse.getData().getCustomer().get(0).getAgeGroup());

        DONE();
    }

    @Test
    public void connectionId_does_not_exist_404_response() {
        TEST("AHBDB-6988: Retrieve parents relationships when logged in");
        TEST("AHBDB-9344: AC3 - Relationship with specified Connection ID does NOT exist - 404 Not Found");
        GIVEN("A customer is onboarded");
        AND("They have no existing relationships");
        setupTestUser();

        WHEN("A client attempts to retrieve a specific relation by using a connection ID that does not exist");
        OBErrorResponse1 errorResponse = this.relationshipApi
                .getChildBasedOnRelationshipError(this.alphaTestUser, UUID.randomUUID().toString(), 404);
        THEN("The platform returns a 404 Not Found");
        assertNotNull(errorResponse);
    }

}
