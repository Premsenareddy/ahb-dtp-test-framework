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
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateChildInForgerockTest {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    private AlphaTestUser alphaTestUser;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory createCustomerFlow;

    @Inject
    private RelationshipApi relationshipApi;

    private final String REQUEST_VALIDATION = "REQUEST_VALIDATION";
    private final String SIZE_ERROR = "size must be between 8 and 2147483647";
    private final String NULL_MESSAGE = "must not be blank";

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.createCustomerFlow.setupCustomer(this.alphaTestUser);
        }
    }

    @Test
    void create_user_relationship_test_success() {
        TEST("AHBDB-6178 - Create child in Forgerock");
        TEST("AHBDB-8712 : AC1 Positive Test - Happy Path Scenario - Create child in Forgerock - 201 response");
        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword("temporary_password").build();

        GIVEN("A customer exists.");
        setupTestUser();

        WHEN("Calling post relationship from authenticate api with a temporary password.");
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        THEN("Status code 201 is returned.");
        assertNotNull(response);

        AND("The userId of the newly created user is returned.");
        assertNotNull(response.getUserId());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1234567", "abcdefg", "!@#$%^&"})
    void invalid_password_400_bad_request(String invalidPassword) throws JSONException {
        TEST("AHBDB-6178 - Create child in Forgerock");
        TEST("AHBDB-8713 : AC2 Negative Test - Create child in Forgerock - 400 response");

        GIVEN(String.format("A customer exists."));
        setupTestUser();

        JSONObject jsonObject = createRelationshipAndUserBodyJson(invalidPassword);

        WHEN("Calling post relationship from authenticate api with an invalid temporary password.");
        OBErrorResponse1 result =
                this.authenticateApi.createRelationshipError(alphaTestUser, jsonObject, 400);

        THEN("Status Code is 400");
        assertEquals(REQUEST_VALIDATION, result.getCode(), "Error code not as expected, expected: " + REQUEST_VALIDATION);
        assertTrue(result.getMessage().contains(SIZE_ERROR), "Error message not as expected, expected: " + SIZE_ERROR);

        DONE();
    }

    @Test
    void null_password_400_response() {
        TEST("AHBDB-6178: Create child in Forgerock");
        TEST("AHBDB-8714 : AC2 Create child in Forgerock - 400 response");

        GIVEN("A parent is onboarded as a marketplace customer");
        AND("They want to onboard their child as a marketplace customer");
        setupTestUser();

        JSONObject nullPassword = new JSONObject() {
            {
                put("Password", JSONObject.NULL);
            }
        };

        WHEN("The client attempts to create a child account on Forgerock with a invalid temporary password");
        THEN("The platform will return a 400 bad request");
        OBErrorResponse1 response =
                this.authenticateApi.createRelationshipError(alphaTestUser, nullPassword, 400);
        assertNotNull(response);
        assertTrue(response.getMessage().contains(NULL_MESSAGE));
    }

    public JSONObject createRelationshipAndUserBodyJson(String password) {
        return new JSONObject() {
            {
                put("Password", password);
            }
        };
    }
}
