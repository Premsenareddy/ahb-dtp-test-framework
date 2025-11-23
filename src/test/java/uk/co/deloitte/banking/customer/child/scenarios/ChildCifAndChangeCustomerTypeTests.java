package uk.co.deloitte.banking.customer.child.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerType1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.getRandomIntegerInRange;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildCifAndChangeCustomerTypeTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private RelationshipApi relationshipApi;

    private static final String ERROR_NOT_FOUND = "UAE.ERROR.NOT_FOUND";
    private static final String CONNECTION_ID_NOT_FOUND = "connectionID not found in relationships list";
    private static final String EID_NOT_FOUND = "Customer does not have an existing EID";

    private String childId;
    private String connectionId;
    private String fullName = "Test Name";

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser1;
    private AlphaTestUser alphaTestUser2;
    private AlphaTestUser alphaTestUser3;
    private AlphaTestUser alphaTestUser4;
    private AlphaTestUser alphaTestUser5;
    private AlphaTestUser alphaTestUser6;
    private AlphaTestUser alphaTestUser7;
    private AlphaTestUser alphaTestUser8;
    private AlphaTestUser alphaTestUser9;
    private AlphaTestUser alphaTestUser10;
    private AlphaTestUser alphaTestUser11;

    private void setupTestUsers(boolean rebuild) {
        if (alphaTestUser == null || rebuild) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            childId = alphaTestUserFactory.createChildInForgerock(alphaTestUser, "validtestpassword");
            connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUser,
                    alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));

            patchChildWithFullName(alphaTestUser, connectionId);
        }
    }

    private void patchChildWithFullName(AlphaTestUser alphaTestUserToUse, String connectionIdToUse) {
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName("Test")
                        .lastName("Name")
                        .fullName(fullName)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .nationality("AE")
                        .gender(OBGender.MALE)
                        .onboardedBy(alphaTestUserToUse.getCustomerId())
                        .email(generateRandomEmail())
                        .build())
                .build();

        customerApiV2.patchChildSuccess(alphaTestUserToUse, customer, connectionIdToUse);
    }

    private OBWriteDependant1 generateDependantBodyWithDob(LocalDate dob, String childIdToUse){
        return OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(childIdToUse))
                        .dateOfBirth(dob)
                        .fullName(fullName)
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.FATHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();
    }

    @Test
    public void happy_path_verify_relationship() {
        TEST("AHBDB-6999: Create account for child (CIF and change customerType)");
        TEST("AHBDB-12021: AC1 - Retrieve Relationships from CRM");
        setupTestUsers(false);

        GIVEN("A connectionId exists between a parent and a child");
        WHEN("The client attempts to retrieve the list of relationships for the parent");
        var getRelationship = relationshipApi.getRelationships(alphaTestUser);

        THEN("The platform responds with a 200");
        assertNotNull(getRelationship);

        AND("The correct relationship details are returned");
        assertEquals(connectionId, getRelationship.getData().getRelationships().get(0).getConnectionId().toString());
        assertEquals(childId, getRelationship.getData().getRelationships().get(0).getCustomerId().toString());
        DONE();
    }

    @Test
    public void negative_test_parent_requests_cif_with_wrong_connection_id_relationship_not_found_404_response() {
        TEST("AHBDB-6999: Create account for child (CIF and change customerType)");
        TEST("AHBDB-12023: AC2 - Relationship not found");
        setupTestUsers(false);

        GIVEN("A connectionId exists between a parent and a child");
        WHEN("The client attempts to retrieve the list of relationships for the parent");
        var error = relationshipApi.putChildCifError(alphaTestUser, UUID.randomUUID().toString(), 404);

        THEN("The platform responds with a 200");
        assertNotNull(error);

        AND("The correct relationship details are returned");
        assertEquals(ERROR_NOT_FOUND, error.getCode());
        assertEquals(CONNECTION_ID_NOT_FOUND, error.getMessage());
        DONE();
    }

    @Test
    public void negative_test_idv_does_not_exist_404_response() {

        TEST("AHBDB-6999: Create account for child (CIF and change customerType)");
        TEST("AHBDB-12034: AC3 - IDV does not exist - 404 Response");
        setupTestUsers(true);

        GIVEN("The IDV of the child does not exist");
        WHEN("A client attempts to generate a CIF for the child");
        var error = relationshipApi.putChildCifError(alphaTestUser, connectionId, 404);

        THEN("The platform returns a 404 Response");
        assertNotNull(error);

        AND("A CIF number will not be generated");
        assertEquals(ERROR_NOT_FOUND, error.getCode());
        assertEquals(EID_NOT_FOUND, error.getMessage());
        DONE();
    }

    @Test
    public void happy_path_child_cif_already_exists_200() {
        TEST("AHBDB-6999: Create account for child (CIF and change customerType)");
        TEST("AHBDB-12035: AC4 - Child's CIF already exists");
        setupTestUsers(true);
        GIVEN("The CIF for a child already exists");
        WHEN("A client attempts to generate a CIF for the child");
        relationshipApi.createChildIdvDetails(alphaTestUser, connectionId);

        var response= relationshipApi.putChildCif(alphaTestUser, connectionId);
        String generatedCif = response.getData().getCustomer().get(0).getCif();

        var childProfile= relationshipApi.putChildCif(alphaTestUser, connectionId);

        THEN("The platform returns a 200 Response");
        assertNotNull(childProfile);

        AND("The child's full profile with be returned to the client");
        assertEquals(childId, childProfile.getData().getCustomer().get(0).getCustomerId().toString());
        assertNotNull(childProfile.getData().getCustomer().get(0).getCif());
        assertEquals(generatedCif, childProfile.getData().getCustomer().get(0).getCif());
        DONE();
    }

    @Test
    public void happy_path_generate_unique_cif() {
//        TODO :: Run for regression, takes a long time otherwise
        envUtils.ignoreTestInEnv(Environments.ALL);
        TEST("AHBDB-6999: Create account for child (CIF and change customerType)");
        TEST("AHBDB-12036: AC5, AC6, AC7, AC8 - Generate the unique CIF number");
        NOTE("User 1 is for AC5, Users 2-9 for AC6, User 10 for AC7 and User 11 for AC8");
        int currentYear = LocalDate.now().getYear();
        LocalDate randomDob = LocalDate.of(getRandomIntegerInRange(currentYear - 18, currentYear),
                getRandomIntegerInRange(1, 12), getRandomIntegerInRange(1, 28));
        String sameDateOfBirth = randomDob.format(DateTimeFormatter.ofPattern("ddMMyy"));

        NOTE("Setting up child 1 with a given CIF");
        GIVEN("The platform wants to use an ascending digit (1-9) + the customer's 6 digit birthday to create the CIF");
        AND("The platform starts with digit = 1");
        AND("The CIF does not exist");
        String cifForChild1 = "1" + sameDateOfBirth;

        alphaTestUser1 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId1 = alphaTestUserFactory.createChildInForgerock(alphaTestUser1, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse1 = generateDependantBodyWithDob(randomDob, childId1);
        String connectionId1 = alphaTestUserFactory.createChildInCRM(alphaTestUser1, dependantBodyToUse1);
        patchChildWithFullName(alphaTestUser1, connectionId1);
        relationshipApi.createChildIdvDetails(alphaTestUser1, connectionId1);

        WHEN("The platform attempts to save the CIF");
        THEN("They will be able to save it");
        OBReadCustomer1 childProfile1 = relationshipApi.putChildCif(alphaTestUser1, connectionId1);
        assertEquals(cifForChild1, childProfile1.getData().getCustomer().get(0).getCif());
        DONE();

        NOTE("Setting up child 2 with a given CIF");
        GIVEN("The platform wants to use an ascending digit (1-9) + the customer's 6 digit birthday to create the CIF");
        AND("The CIF does exist");
        String cifForChild2 = "2" + sameDateOfBirth;

        alphaTestUser2 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId2 = alphaTestUserFactory.createChildInForgerock(alphaTestUser2, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse2 = generateDependantBodyWithDob(randomDob, childId2);
        String connectionId2 = alphaTestUserFactory.createChildInCRM(alphaTestUser2, dependantBodyToUse2);
        patchChildWithFullName(alphaTestUser2, connectionId2);
        relationshipApi.createChildIdvDetails(alphaTestUser2, connectionId2);

        WHEN("The platform attempts to save the CIF number");
        THEN("The platform will use the next ascending digit");
        OBReadCustomer1 childProfile2 = relationshipApi.putChildCif(alphaTestUser2, connectionId2);
        assertEquals(cifForChild2, childProfile2.getData().getCustomer().get(0).getCif());

        NOTE("Setting up child 3 with a given CIF");
        String cifForChild3 = "3" + sameDateOfBirth;

        alphaTestUser3 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId3 = alphaTestUserFactory.createChildInForgerock(alphaTestUser3, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse3 = generateDependantBodyWithDob(randomDob, childId3);
        String connectionId3 = alphaTestUserFactory.createChildInCRM(alphaTestUser3, dependantBodyToUse3);
        patchChildWithFullName(alphaTestUser3, connectionId3);
        relationshipApi.createChildIdvDetails(alphaTestUser3, connectionId3);

        OBReadCustomer1 childProfile3 = relationshipApi.putChildCif(alphaTestUser3, connectionId3);
        assertEquals(cifForChild3, childProfile3.getData().getCustomer().get(0).getCif());

        NOTE("Setting up child 4 with a given CIF");
        String cifForChild4 = "4" + sameDateOfBirth;

        alphaTestUser4 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId4 = alphaTestUserFactory.createChildInForgerock(alphaTestUser4, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse4 = generateDependantBodyWithDob(randomDob, childId4);
        String connectionId4 = alphaTestUserFactory.createChildInCRM(alphaTestUser4, dependantBodyToUse4);
        patchChildWithFullName(alphaTestUser4, connectionId4);
        relationshipApi.createChildIdvDetails(alphaTestUser4, connectionId4);

        OBReadCustomer1 childProfile4 = relationshipApi.putChildCif(alphaTestUser4, connectionId4);
        assertEquals(cifForChild4, childProfile4.getData().getCustomer().get(0).getCif());

        NOTE("Setting up child 5 with a given CIF");
        String cifForChild5 = "5" + sameDateOfBirth;

        alphaTestUser5 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId5 = alphaTestUserFactory.createChildInForgerock(alphaTestUser5, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse5 = generateDependantBodyWithDob(randomDob, childId5);
        String connectionId5 = alphaTestUserFactory.createChildInCRM(alphaTestUser5, dependantBodyToUse5);
        patchChildWithFullName(alphaTestUser5, connectionId5);
        relationshipApi.createChildIdvDetails(alphaTestUser5, connectionId5);

        OBReadCustomer1 childProfile5 = relationshipApi.putChildCif(alphaTestUser5, connectionId5);
        assertEquals(cifForChild5, childProfile5.getData().getCustomer().get(0).getCif());

        NOTE("Setting up child 6 with a given CIF");
        String cifForChild6 = "6" + sameDateOfBirth;

        alphaTestUser6 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId6 = alphaTestUserFactory.createChildInForgerock(alphaTestUser6, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse6 = generateDependantBodyWithDob(randomDob, childId6);
        String connectionId6 = alphaTestUserFactory.createChildInCRM(alphaTestUser6, dependantBodyToUse6);
        patchChildWithFullName(alphaTestUser6, connectionId6);
        relationshipApi.createChildIdvDetails(alphaTestUser6, connectionId6);

        OBReadCustomer1 childProfile6 = relationshipApi.putChildCif(alphaTestUser6, connectionId6);
        assertEquals(cifForChild6, childProfile6.getData().getCustomer().get(0).getCif());

        NOTE("Setting up child 7 with a given CIF");
        String cifForChild7 = "7" + sameDateOfBirth;

        alphaTestUser7 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId7 = alphaTestUserFactory.createChildInForgerock(alphaTestUser7, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse7 = generateDependantBodyWithDob(randomDob, childId7);
        String connectionId7 = alphaTestUserFactory.createChildInCRM(alphaTestUser7, dependantBodyToUse7);
        patchChildWithFullName(alphaTestUser7, connectionId7);
        relationshipApi.createChildIdvDetails(alphaTestUser7, connectionId7);

        OBReadCustomer1 childProfile7 = relationshipApi.putChildCif(alphaTestUser7, connectionId7);
        assertEquals(cifForChild7, childProfile7.getData().getCustomer().get(0).getCif());

        NOTE("Setting up child 8 with a given CIF");
        String cifForChild8 = "8" + sameDateOfBirth;

        alphaTestUser8 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId8 = alphaTestUserFactory.createChildInForgerock(alphaTestUser8, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse8 = generateDependantBodyWithDob(randomDob, childId8);
        String connectionId8 = alphaTestUserFactory.createChildInCRM(alphaTestUser8, dependantBodyToUse8);
        patchChildWithFullName(alphaTestUser8, connectionId8);
        relationshipApi.createChildIdvDetails(alphaTestUser8, connectionId8);

        OBReadCustomer1 childProfile8 = relationshipApi.putChildCif(alphaTestUser8, connectionId8);
        assertEquals(cifForChild8, childProfile8.getData().getCustomer().get(0).getCif());

        NOTE("Setting up child 9 with a given CIF");
        String cifForChild9 = "9" + sameDateOfBirth;

        alphaTestUser9 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId9 = alphaTestUserFactory.createChildInForgerock(alphaTestUser9, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse9 = generateDependantBodyWithDob(randomDob, childId9);
        String connectionId9 = alphaTestUserFactory.createChildInCRM(alphaTestUser9, dependantBodyToUse9);
        patchChildWithFullName(alphaTestUser9, connectionId9);
        relationshipApi.createChildIdvDetails(alphaTestUser9, connectionId9);

        OBReadCustomer1 childProfile9 = relationshipApi.putChildCif(alphaTestUser9, connectionId9);
        assertEquals(cifForChild9, childProfile9.getData().getCustomer().get(0).getCif());
        DONE();

        NOTE("Setting up child 10 with a CIF -- this should be a random CIF");
        GIVEN("All the possible combinations for CIF using the child's date of birth are taken");
        AND("The platform wants to use 7 random digits to create the CIF");
        AND("The CIF does not previously exist");
        alphaTestUser10 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId10 = alphaTestUserFactory.createChildInForgerock(alphaTestUser10, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse10 = generateDependantBodyWithDob(randomDob, childId10);
        String connectionId10 = alphaTestUserFactory.createChildInCRM(alphaTestUser10, dependantBodyToUse10);
        patchChildWithFullName(alphaTestUser10, connectionId10);
        relationshipApi.createChildIdvDetails(alphaTestUser10, connectionId10);

        WHEN("The platform attempts to generate the CIF number");
        OBReadCustomer1 childProfile10 = relationshipApi.putChildCif(alphaTestUser10, connectionId10);
        String randomCifGenerated = childProfile10.getData().getCustomer().get(0).getCif();
        THEN("They will be able to save it");
        assertNotEquals(cifForChild1, randomCifGenerated);
        assertNotEquals(cifForChild2, randomCifGenerated);
        assertNotEquals(cifForChild3, randomCifGenerated);
        assertNotEquals(cifForChild4, randomCifGenerated);
        assertNotEquals(cifForChild5, randomCifGenerated);
        assertNotEquals(cifForChild6, randomCifGenerated);
        assertNotEquals(cifForChild7, randomCifGenerated);
        assertNotEquals(cifForChild8, randomCifGenerated);
        assertNotEquals(cifForChild9, randomCifGenerated);
        DONE();

        NOTE("Setting up child 11 with a CIF -- this should also be a random CIF");
        GIVEN("The customer's digit + 6 digit DOB CIF is taken");
        AND("The platform wants to use 7 random digits to create the CIF");
        AND("The CIF does exist");
        alphaTestUser11 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        String childId11 = alphaTestUserFactory.createChildInForgerock(alphaTestUser11, "validtestpassword");
        OBWriteDependant1 dependantBodyToUse11= generateDependantBodyWithDob(randomDob, childId11);
        String connectionId11 = alphaTestUserFactory.createChildInCRM(alphaTestUser11, dependantBodyToUse11);
        patchChildWithFullName(alphaTestUser11, connectionId11);
        relationshipApi.createChildIdvDetails(alphaTestUser11, connectionId11);

        WHEN("The platform tries to generate the CIF number");
        OBReadCustomer1 childProfile11 = relationshipApi.putChildCif(alphaTestUser11, connectionId11);
        String randomCifGenerated2 = childProfile11.getData().getCustomer().get(0).getCif();
        assertNotEquals(cifForChild1, randomCifGenerated2);
        assertNotEquals(cifForChild2, randomCifGenerated2);
        assertNotEquals(cifForChild3, randomCifGenerated2);
        assertNotEquals(cifForChild4, randomCifGenerated2);
        assertNotEquals(cifForChild5, randomCifGenerated2);
        assertNotEquals(cifForChild6, randomCifGenerated2);
        assertNotEquals(cifForChild7, randomCifGenerated2);
        assertNotEquals(cifForChild8, randomCifGenerated2);
        assertNotEquals(cifForChild9, randomCifGenerated2);
        assertNotEquals(randomCifGenerated, randomCifGenerated2);
        THEN("The platform will generate a random 7-digit CIF number until a unique one is found");
        DONE();

        deleteAllCustomers();
    }

    @Test
    public void happy_path_change_customer_type_to_banking() {
        TEST("AHBDB-6999: Create account for child (CIF and change customerType)");
        TEST("AHBDB-12037: AC9 - Put Customer CIF and change customer type to banking");
        setupTestUsers(true);
        GIVEN("A customer has met all of the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV");
        WHEN("A client attempts to generate a CIF for the child");
        relationshipApi.createChildIdvDetails(alphaTestUser, connectionId);

        OBReadCustomer1 beforeCif = relationshipApi.getChildBasedOnRelationship(alphaTestUser, connectionId);
        assertEquals(OBCustomerType1.MARKETPLACE, beforeCif.getData().getCustomer().get(0).getCustomerType());

        OBReadCustomer1 putCifResponse = relationshipApi.putChildCif(alphaTestUser, connectionId);
        THEN("The platform returns a 200 Response");
        assertNotNull(putCifResponse);
        AND("It will return the generated CIF");
        assertNotNull(putCifResponse.getData().getCustomer().get(0).getCif());
        AND("The CustomerType will be Banking");
        OBReadCustomer1 getChild = relationshipApi.getChildBasedOnRelationship(alphaTestUser, connectionId);
        assertEquals(OBCustomerType1.BANKING, getChild.getData().getCustomer().get(0).getCustomerType());
        DONE();
    }

    private void deleteAllCustomers() {
        customerApiV2.deleteCustomer(alphaTestUser1);
        customerApiV2.deleteCustomer(alphaTestUser2);
        customerApiV2.deleteCustomer(alphaTestUser3);
        customerApiV2.deleteCustomer(alphaTestUser4);
        customerApiV2.deleteCustomer(alphaTestUser5);
        customerApiV2.deleteCustomer(alphaTestUser6);
        customerApiV2.deleteCustomer(alphaTestUser7);
        customerApiV2.deleteCustomer(alphaTestUser8);
        customerApiV2.deleteCustomer(alphaTestUser9);
        customerApiV2.deleteCustomer(alphaTestUser10);
        customerApiV2.deleteCustomer(alphaTestUser11);
    }
}
