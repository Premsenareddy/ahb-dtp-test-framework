package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;

import javax.inject.Inject;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile;

@Tag("BuildCycle2")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UniquePhoneNumberAndEmailTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private OtpApi otpApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    private static final String UAE_ERROR_BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";
    private static final String UAE_ERROR_CONFLICT = "UAE.ERROR.CONFLICT";
    private static final String CUSTOMER_ALREADY_EXISTS = "Customer already exists";
    private static final String EMAIL_ALREADY_EXISTS = "Email already in use";
    private static final String UNIQUE_NUMBER_ERROR_MESSAGE = "Mobile Phone number must be unique across customers.";

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;
    private AlphaTestUser alphaTestUser3;

    private void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    /**
     *  This method sets up a pre-existing customer to use their email/number in 409 negative scenarios
     */
    private void setupTestUser2() {
        if (alphaTestUser2 == null) {
            alphaTestUser2 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    private void setupUser() {
        if (alphaTestUser3 == null) {
            setupTestUser();
            alphaTestUser3 = alphaTestUserFactory.setupUser(new AlphaTestUser());
        }
    }

    @Order(1)
    @Test
    void happy_path_path_new_phone_number_and_email_when_creating_customer() {
        TEST("AHBDB-2456: Unique phone numbers in FR and Customer");
        TEST("AHBDB-3975: AC1 Positive Test - Happy Path Scenario - " +
                "New phone number/email on create customer endpoint - Success Response 200");

        GIVEN("A new customer has a unique phone number");
        setupTestUser();

        WHEN("The client attempts to register a new customer with it");
        THEN("The platform responds with a success response 200");
        DONE();
    }

    @Order(3)
    @Test
    void happy_path_customer_updates_phone_number_with_another_unique_phone_number() {
        TEST("AHBDB-2456: Unique phone numbers in FR and Customer");
        TEST("AHBDB-3977: AC3 Positive Test - Happy Path Scenario - New phone number on update customer endpoint - " +
                "Success Response 200");

        setupTestUser();
        GIVEN("A new customer has a unique phone number");
        String oldNumber = alphaTestUser.getUserTelephone();
        String newNumber = generateRandomMobile();
        alphaTestUser.setUserTelephone(newNumber);

        WHEN("The client attempts to update a customer with it");
        OBWriteCustomerResponse1 response = customerApi
                .patchCustomerSuccess(alphaTestUser, obWritePartialCustomer1(newNumber, null));

        THEN("The platform responds with a 200 success response");
        Assertions.assertNotNull(response);

        AND("The platform will replace the old number with this new number");
        OBReadCustomer1 currentCustomer = customerApi.getCurrentCustomer(alphaTestUser);

        OBCustomer1 details = currentCustomer.getData().getCustomer().get(0);

        assertEquals(alphaTestUser.getUserId(), details.getCustomerId().toString());
        assertNotEquals(oldNumber, details.getMobileNumber());
        assertEquals(newNumber, details.getMobileNumber());
        DONE();
    }

    @Order(3)
    @Test
    void happy_path_customer_updates_email_with_another_unique_email() {
        TEST("AHBDB-2456: Unique phone numbers in FR and Customer");
        TEST("AHBDB-3977: AC3 Positive Test - Happy Path Scenario - New email on update customer endpoint - " +
                "Success Response 200");

        setupTestUser();
        GIVEN("A new customer has a unique phone number");
        String oldEmail = alphaTestUser.getUserEmail();
        String newEmail = generateRandomEmail();
        alphaTestUser.setUserEmail(newEmail);

        WHEN("The client attempts to update a customer with it");
        OBWriteCustomerResponse1 response = customerApi
                .patchCustomerSuccess(alphaTestUser, obWritePartialCustomer1(null, newEmail));

        THEN("The platform responds with a 200 success response");
        Assertions.assertNotNull(response);

        AND("The platform will replace the old number with this new number");
        OBReadCustomer1 currentCustomer = customerApi.getCurrentCustomer(alphaTestUser);

        OBCustomer1 details = currentCustomer.getData().getCustomer().get(0);

        assertEquals(alphaTestUser.getUserId(), details.getCustomerId().toString());
        assertNotEquals(oldEmail, details.getEmail());
        assertEquals(newEmail, details.getEmail());
        DONE();
    }

    @Order(4)
    @Test
    void negative_test_update_customer_with_existing_number() {
        TEST("AHBDB-2456: Unique phone numbers in FR and Customer");
        TEST("AHBDB-3978: AC4 Negative Test - Duplicate phone number on update customer endpoint - Conflict response 409");
        setupTestUser();
        setupTestUser2();
        GIVEN("A customer exists with a phone number");
        OBReadCustomer1 check = customerApi.getCurrentCustomer(alphaTestUser2);
        assertNotNull(check.getData().getCustomer().get(0).getMobileNumber());
        String existingNumber = check.getData().getCustomer().get(0).getMobileNumber();

        WHEN("The client attempts to update another customer with the same number");
        OBErrorResponse1 error = customerApi
                .patchCustomerError(alphaTestUser, obWritePartialCustomer1(existingNumber, null), 400);
        THEN("The platform responds with a Invalid request 409");
        assertEquals(UAE_ERROR_BAD_REQUEST, error.getCode());
        assertEquals(UNIQUE_NUMBER_ERROR_MESSAGE, error.getErrors().get(0).getMessage());
        DONE();
    }

    @Order(4)
    @Test
    void negative_test_update_customer_with_existing_email() {
        envUtils.ignoreTestInEnv("AHBDB-13442", Environments.SIT, Environments.NFT);
        TEST("AHBDB-13442");
        TEST("AHBDB-2456: Unique phone numbers in FR and Customer");
        TEST("AHBDB-3978: AC4 Negative Test - Duplicate phone number on update customer endpoint - Conflict response 409");
        setupTestUser();
        setupTestUser2();
        GIVEN("A customer exists with a phone number");
        OBReadCustomer1 check = customerApi.getCurrentCustomer(alphaTestUser2);
        assertNotNull(check.getData().getCustomer().get(0).getEmail());
        String existingEmail = check.getData().getCustomer().get(0).getEmail();

        WHEN("The client attempts to update another customer with the same number");
        OBErrorResponse1 error = customerApi
                .patchCustomerError(alphaTestUser, obWritePartialCustomer1(null, existingEmail), 409);
        THEN("The platform responds with a Invalid request 409");
        assertEquals(UAE_ERROR_CONFLICT, error.getCode());
        assertEquals(EMAIL_ALREADY_EXISTS, error.getMessage());
        DONE();
    }

    /**
     *   Start of User tests
     */

    @Order(2)
    @Test
    void negative_test_user_creates_customer_using_existing_number() {
        TEST("AHBDB-2456: Unique phone numbers in FR and Customer");
        TEST("AHBDB-3976: AC2 Negative Test - Duplicate phone number on create customer endpoint - Conflict response 409");

        setupTestUser();
        GIVEN("A customer exists with a phone number");
        WHEN("The client attempts to register a new customer with it");
        AlphaTestUser newUser = alphaTestUserFactory.setupUser(new AlphaTestUser());

        OBReadCustomer1 check = customerApi.getCurrentCustomer(alphaTestUser);
        assertNotNull(check.getData().getCustomer().get(0).getMobileNumber());
        String existingNumber = check.getData().getCustomer().get(0).getMobileNumber();

//        alphaTestUser exists in CRM at this point, the test tries to use their details when creating a customer in CRM
        OBWriteCustomer1 customer = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(newUser.getName())
                        .dateOfBirth(newUser.getDateOfBirth())
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(newUser.getUserEmail())
                        .mobileNumber(existingNumber)
                        .language(newUser.getLanguage())
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .build())
                .build();

        THEN("The platform responds with a conflict response 409");
        OBErrorResponse1 error = customerApi.createCustomerError(newUser, customer, 400);
        assertEquals(UAE_ERROR_BAD_REQUEST, error.getCode());
        assertEquals(UNIQUE_NUMBER_ERROR_MESSAGE, error.getErrors().get(0).getMessage());
        DONE();
    }

    @Order(2)
    @Test
    void negative_test_user_creates_customer_using_existing_email() {
        envUtils.ignoreTestInEnv("AHBDB-13441", Environments.SIT, Environments.NFT);
        TEST("AHBDB-13441");
        TEST("AHBDB-2456: Unique phone numbers in FR and Customer");
        TEST("AHBDB-3976: AC2 Negative Test - Duplicate email on create customer endpoint - Conflict response 409");

        setupTestUser();
        GIVEN("A customer exists with a phone number");
        WHEN("The client attempts to register a new customer with it");
        AlphaTestUser newUser = alphaTestUserFactory.setupUser(new AlphaTestUser());

        OBReadCustomer1 check = customerApi.getCurrentCustomer(alphaTestUser);
        assertNotNull(check.getData().getCustomer().get(0).getEmail());
        String existingEmail = check.getData().getCustomer().get(0).getEmail();

//        alphaTestUser exists in CRM at this point, the test tries to use their details when creating a customer in CRM
        OBWriteCustomer1 customer = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(newUser.getName())
                        .dateOfBirth(newUser.getDateOfBirth())
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(existingEmail)
                        .mobileNumber(newUser.getUserTelephone())
                        .language(newUser.getLanguage())
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .build())
                .build();

        THEN("The platform responds with a conflict response 409");
        OBErrorResponse1 error = customerApi.createCustomerError(newUser, customer, 409);
        assertEquals(UAE_ERROR_CONFLICT, error.getCode());
        assertEquals(EMAIL_ALREADY_EXISTS, error.getMessage());
        DONE();
    }

    @Order(5)
    @Test
    void negative_test_update_user_with_existing_number() {
        TEST("AHBDB-2456: Unique phone numbers/email in FR and Customer");
        TEST("AHBDB-3983: AC6 Negative Test - Duplicate phone number on update user endpoint - Conflict response 409");
        AlphaTestUser user = alphaTestUserFactory.setupUser(new AlphaTestUser());
        AlphaTestUser customer = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        GIVEN("A customer exists with a unique phone number");
        WHEN("The client attempts to register a new user with the existing customer's mobile number");
        OBReadCustomer1 check = customerApi.getCurrentCustomer(customer);
        assertNotNull(check.getData().getCustomer().get(0).getMobileNumber());
        String existingNumber = check.getData().getCustomer().get(0).getMobileNumber();

        UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .phoneNumber(existingNumber)
                .build();

        OBErrorResponse1 error =
                authenticateApi.patchUser(user, updateUserRequest).extract().as(OBErrorResponse1.class);
        THEN("The platform responds with a conflict response 409");
        assertEquals(UAE_ERROR_CONFLICT, error.getCode());
        DONE();
    }

    @Order(5)
    @Test
    void negative_test_update_user_with_existing_email() {
        TEST("AHBDB-2456: Unique phone numbers/email in FR and Customer");
        TEST("AHBDB-3983: AC6 Negative Test - Duplicate phone number on update user endpoint - Conflict response 409");
        AlphaTestUser user = alphaTestUserFactory.setupUser(new AlphaTestUser());
        AlphaTestUser customer = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        GIVEN("A customer exists with a unique phone number");
        WHEN("The client attempts to register a new user with the existing customer's mobile number");
        OBReadCustomer1 check = customerApi.getCurrentCustomer(customer);
        assertNotNull(check.getData().getCustomer().get(0).getEmail());
        String existingEmail = check.getData().getCustomer().get(0).getEmail();

        UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .mail(existingEmail)
                .build();

        OBErrorResponse1 error =
                authenticateApi.patchUser(user, updateUserRequest).extract().as(OBErrorResponse1.class);
        THEN("The platform responds with a conflict response 409");
        assertEquals(UAE_ERROR_CONFLICT, error.getCode());
        DONE();
    }

    @Order(6)
    @Test
    void happy_path_user_updates_phone_number_with_another_unique_phone_number() {
        TEST("AHBDB-2456: Unique phone numbers/email in FR and Customer");
        TEST("AHBDB-3981: AC5 Happy Path - Update user with new phone number");
        setupUser();
        GIVEN("A user exists");
        WHEN("They try to update their phone number with another unique one");
        String newNumber = generateRandomMobile();
        alphaTestUser3.setUserTelephone(newNumber);

        authenticateApi.patchUser(alphaTestUser3, updateUserRequestV1(null, newNumber));
        THEN("The platform will return a 200 Response");
        AND("The details will be persisted");
        DONE();
    }

    @Order(6)
    @Test
    void happy_path_user_updates_email_with_another_unique_email() {
        TEST("AHBDB-2456: Unique phone numbers/email in FR and Customer");
        TEST("AHBDB-3981: AC5 Happy Path - Update user with new phone number");
        setupUser();
        GIVEN("A user exists");
        WHEN("They try to update their phone number with another unique one");
        String newEmail = generateRandomEmail();
        alphaTestUser3.setUserEmail(newEmail);

        authenticateApi.patchUser(alphaTestUser3, updateUserRequestV1(newEmail, null));
        THEN("The platform will return a 200 Response");
        AND("The details will be persisted");
        DONE();
    }

    @Test
    void user_updates_their_email_after_customer_updates_their_details() {
        TEST("AHBDB-XXXX");
        AlphaTestUser user = alphaTestUserFactory.setupUser(new AlphaTestUser());
        AlphaTestUser customer = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        GIVEN("A customer has recently changed their email address");
        AND("A user tries to update their email with that same email address");
        String oldNumber = customer.getUserTelephone();
        String newNumber = generateRandomMobile();
        customer.setUserTelephone(newNumber);

        OBWriteCustomerResponse1 response = customerApi
                .patchCustomerSuccess(customer, obWritePartialCustomer1(newNumber, null));
        assertEquals(customer.getUserId(), response.getData().getCustomerId().toString());

        OBReadCustomer1 getResponse = customerApi.getCustomerSuccess(customer);
        assertEquals(customer.getUserId(), getResponse.getData().getCustomer().get(0).getCustomerId().toString());
        assertNotEquals(oldNumber, getResponse.getData().getCustomer().get(0).getMobileNumber());
        WHEN("The platform tries to save the email against that user");
        String existingEmail = getResponse.getData().getCustomer().get(0).getEmail();

        UpdateUserRequestV1 updateUserRequest = UpdateUserRequestV1.builder()
                .mail(existingEmail)
                .build();

        OBErrorResponse1 error =
                authenticateApi.patchUser(user, updateUserRequest).extract().as(OBErrorResponse1.class);
        THEN("The platform responds with a conflict response 409");
        assertEquals(UAE_ERROR_CONFLICT, error.getCode());
        THEN("The platform will return a 400 Response");
        DONE();
    }

    private OBWritePartialCustomer1 obWritePartialCustomer1(String number, String email) {

        return OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .mobileNumber(number)
                        .email(email)
                        .build())
                .build();
    }

    private UpdateUserRequestV1 updateUserRequestV1(String email, String phoneNumber) {

        return UpdateUserRequestV1.builder()
                .phoneNumber(phoneNumber)
                .mail(email)
                .build();
    }
}
