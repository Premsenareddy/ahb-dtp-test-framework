package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetEIDInformationChild {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private RelationshipApi relationshipApi;

    private AlphaTestUser alphaTestUserParent;
    private AlphaTestUser alphaTestUserChild;

    private static String childId;
    private static String fullName = "RICHARD ANDREAS KOFER";
    private static String connectionId;

    private void setupTestUsersChild() {
        
        alphaTestUserParent = new AlphaTestUser();
        alphaTestUserChild = new AlphaTestUser();
        alphaTestUserParent = alphaTestUserFactory.setupCustomer(alphaTestUserParent);
        childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, "validtestpassword");
        connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent,
                alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
        alphaTestUserChild =
                alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, connectionId, childId);
    }

    @Test
    @Tag("SmokeTest")
    @Tag("AHBDB-7312")
    public void retrieve_customerId_with_valid_DocumentNumber_200() {
        TEST("AHBDB-7312: AC1 Retrieve customerID with valid EID DocumentNumber that exists - Success Response 200");
        TEST("AHBDB-11910: AC1 Positive Test - Happy Path Scenario - Retrieve customerID with valid EID DocumentNumber that exists - Success Response 200");
        setupTestUsersChild();
        GIVEN("A child exists with a EID DocumentNumber");

        this.customerApi.updateCustomer(this.alphaTestUserChild, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        String documentNumber = this.relationshipApi.createChildIdvDetails(alphaTestUserParent, connectionId).getData().getDocumentNumber();

        WHEN("The client attempts to get a customer using that DocumentNumber");
        String customerIdReturned = this.customerApi.getCustomersByEid(documentNumber)
                .getData().get(0).getCustomerId().toString();

        THEN("The platform responds with a 200 and a list containing ONLY the CustomerId " +
                "(BE generated UUID, not CIF) of the customer who has that DocumentNumber");

        Assertions.assertEquals(childId, customerIdReturned, "Customer ID returned did not match, " +
                "expected: " + childId);

        DONE();
    }
}
