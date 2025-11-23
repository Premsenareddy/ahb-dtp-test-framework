package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomerId1;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile;


@Tag("BuildCycle1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExistingCustomerCheckApiTest {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"+55550", "+555", "+5555412345671", "+55554123456713", ""})
    void retrieve_customer_invalid_mobile_number_test(String invalidPhoneNumber) {

        TEST("AHBDB-1731- AC6 Retrieve customer with invalid phone number -400");
        GIVEN("The client has a valid api key");
        WHEN("The client calls get on the protected customers endpoint with a invalid mobile phone number");
        OBErrorResponse1 response =
                customerApi.getCustomerProtectedError(invalidPhoneNumber, "mobile", 400);
        THEN("The Get Customer api returns 400 bad request");
        AND("The error message contains that phone number is not valid");
        assertTrue(response.getMessage().contains("Phone Number Invalid Field")
                || response.getMessage().contains("At least one query param is required"));
        DONE();

    }

    @ParameterizedTest
    @ValueSource(strings = {"+22521234567", "A", "@£$%^", "@.com", ""})
    void retrieve_customer_invalid_email_address_test(String invalidEmailAddress) {

        TEST("AHBDB-1732- AC7 Retrieve customer with invalid email address -400");
        GIVEN("The client has a valid api key");
        WHEN("The client calls get on the protected customers endpoint with a invalid email address");
        OBErrorResponse1 response =
                customerApi.getCustomerProtectedError(invalidEmailAddress, "email", 400);
        THEN("The Get Customer api returns 400 bad request");
        AND("The error message contains that email address is invalid");
        assertTrue(response.getMessage().contains("must be a well-formed email address")
                || response.getMessage().contains("At least one query param is required"));
        DONE();
    }

    @Test
    void retrieve_customer_valid_mobile_number_does_not_exist_test() {
        TEST("AC2: Retrieve customer with valid Phone number that does not exist - Success Response 200");
        TEST("AHBDB-1727: AC2 Positive Test - Happy Path Retrieve customer with valid Phone number that does not exist");
        GIVEN("The client has a valid api key");
        WHEN("The client calls on the protected customers endpoint with valid phone number that does not exist");
        OBReadCustomerId1 response = this.customerApi.getCustomersByMobile(generateRandomMobile());
        THEN("The Get Customer api returns 200 OK with empty list");
        assertTrue(isEmpty(response.getData()), "Expected Result is an empty list");
        DONE();
    }

    @Test
    void retrieve_customer_valid_email_does_not_exist_test() {
        TEST("AC4:Retrieve customer with valid email address that does not exist - Success Response 200");
        TEST("AHBDB-1729: AC2 Positive Test - Happy Path Retrieve customer with valid email address that does not exist");
        GIVEN("The client has a valid api key");
        WHEN("The client calls on the protected customers endpoint with valid email address that does not exist");
        OBReadCustomerId1 response = this.customerApi.getCustomersByEmail(generateRandomEmail());
        THEN("The Get Customer api returns 200 OK with empty list");
        assertTrue(isEmpty(response.getData()), "Expected Result is an empty list");
        DONE();
    }

    @Test
    void retrieve_customers_with_valid_email_test(){

        TEST("AC5: Retrieve customer with valid Phone number and email address that both exist -Success Response 200");
        setupTestUser();

        GIVEN("A customer exists with an email address and mobile number");

        WHEN("The client attempts to retrieve customers using that email and telephone number");
        OBReadCustomerId1 getWithEmail = customerApi.getCustomersByEmail(alphaTestUser.getUserEmail());

        THEN("The platform responds with a 200");
        AND ("A list containing the customerId of the customer with the email address");
        assertEquals(alphaTestUser.getUserId(), getWithEmail.getData().get(0).getCustomerId().toString());
        DONE();
    }

    @Test
    void retrieve_customers_with_valid_mobile_number_test(){

        TEST("AC5: Retrieve customer with valid Phone number and email address that both exist -Success Response 200");
        setupTestUser();

        GIVEN("A customer exists with an email address and mobile number");

        WHEN("The client attempts to retrieve customers using that email and telephone number");
        OBReadCustomerId1 getWithMobile = customerApi.getCustomersByMobile(alphaTestUser.getUserTelephone());
        THEN("The platform responds with a 200");
        AND("A list containing the customerId of the customer with the telephone number");
        assertEquals(alphaTestUser.getUserId(), getWithMobile.getData().get(0).getCustomerId().toString());
        DONE();
    }
}
