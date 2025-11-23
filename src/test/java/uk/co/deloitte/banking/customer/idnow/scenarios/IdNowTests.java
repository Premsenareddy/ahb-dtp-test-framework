package uk.co.deloitte.banking.customer.idnow.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("@BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IdNowTests {

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private CustomerApiV2 customerapi;

    @Inject
    private EnvUtils envUtils;

    private static final String CUSTOMER_DOESNT_EXIST = "Customer doesn't exist";

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupV2UserAndV2Customer(new AlphaTestUser(), null);
        }
    }

    @Test
    @Order(1)
    public void successfullyCreateIdNow() {
        TEST("AHBDB-1367 - successfully create IdNow");
        setupTestUser();
        GIVEN("I have a valid access token and customer scope");

        WHEN("I submit a valid payload to the endpoint");

        TokenHolder createApplicantResponse = this.idNowApi.createApplicant(alphaTestUser);
        THEN("Status code 201(CREATED) is returned");
        AND("Response contains a token");
        assertNotNull(createApplicantResponse.getSdkToken());
        assertNotNull(createApplicantResponse.getApplicantId());


        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "aaajhdgvfjdhsvfksjgaflsbflaksjbfflsbflaksjbddddddds", "1234567", "!@#$%^&*", ""})
    @Order(2)
    public void negative_test_create_idNow_invalid_firstname(String invalidFirstName) {

        envUtils.ignoreTestInEnv(Environments.NFT);
        TEST("AHBDB-1367 - negative test create IdNow invalid first name : " + invalidFirstName);
        setupTestUser();
        GIVEN("I have a valid access token and customer scope");

        AND("I create a applicant request with an invalid name");
        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName(invalidFirstName)
                .lastName("Last")
                .build();

        WHEN("I submit an invalid payload to the endpoint");

        OBErrorResponse1 createApplicantErrorResponse = this.idNowApi.createApplicantError(alphaTestUser, applicantRequest, 400);
        THEN("Status code 400 is returned");

        assertEquals("REQUEST_VALIDATION", createApplicantErrorResponse.getCode());
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "aaajhdgvfjdhsvfksjgaflsbflaksjbfflsbflaksjbddddddds", "1234567", "!@#$%^&*", ""})
    @Order(3)
    public void negative_test_create_idNow_invalid_lastname(String invalidLastName) {

        envUtils.ignoreTestInEnv(Environments.NFT);
        TEST("AHBDB-1367 - negative test create IdNow invalid last name : " + invalidLastName);
        setupTestUser();
        GIVEN("I have a valid access token and customer scope");

        AND("I create a applicant request with an invalid last name");
        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName("First")
                .lastName(invalidLastName)
                .build();

        WHEN("I submit an invalid payload to the endpoint");
        OBErrorResponse1 createApplicantErrorResponse = this.idNowApi.createApplicantError(alphaTestUser, applicantRequest, 400);
        THEN("Status code 400 is returned");
        assertEquals("REQUEST_VALIDATION", createApplicantErrorResponse.getCode());
        DONE();
    }


    @Test
    @Order(100)
    public void negative_test_create_idNow_not_found_customer() {
        envUtils.ignoreTestInEnv("AHBDB-14969", Environments.ALL);
        TEST("AHBDB-1367 - negative test create IdNow customer is deleted");
        setupTestUser();
        GIVEN("I have a valid access token and customer scope");

        AND("I create a applicant request with an invalid last name");
        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName("First")
                .lastName("Last")
                .build();

        THEN("the created customer is deleted ");
        this.customerapi.deleteCustomer(alphaTestUser);
        this.customerapi.getCustomerErrorResponse(alphaTestUser, 404);

        WHEN("I submit an invalid payload to the endpoint");


        THEN("Status code 404 is returned");
        OBErrorResponse1 createApplicantErrorResponse = this.idNowApi.createApplicantError(alphaTestUser, applicantRequest, 404);
        Assertions.assertTrue(createApplicantErrorResponse.getCode().equals("UAE.ERROR.NOT_FOUND"), "Error message was not as expected, " +
                "test expected : UAE.ERROR.NOT_FOUND");

        DONE();
    }
}
