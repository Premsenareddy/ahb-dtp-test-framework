package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.EmailVerification;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.email.api.EmailVerificationApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.Collections;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;

/**
 * Created by Bharat Gatty on 18/02/2021
 */
@Tag("@BuildCycle1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailValidationUsingVerificationLinkApiTest {

    @Inject
    private EmailVerificationApi emailApi;

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private EnvUtils envUtils;

    private static final String ERROR_CODE_1001 = "UAE.OTP.INVALID";
    private static final String ERROR_CODE_1002 = "UAE.OTP.EXPIRED";

    private static final String BAD_REQUEST = "UAE.ERROR.BAD_REQUEST";
    private static final String EMAIL_TOO_LONG = "Email must not be longer than 40 characters.";

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Test
    @Order(1)
    void email_verification_link_generation_test() {
        TEST("AHBDB-215: AC1 Generation of email with verification link (Post request) - 204 response");
        TEST("AHBDB-2500: AC1 Generation of email with verification link (Post request) - 204 response");
        setupTestUser();
        GIVEN("the client wants to validate a valid email address");
        WHEN("the client attempts to request a verification link via email");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        THEN("The platform will generate a verification link with a unique hash and send it to the customer’s email address");
        AND("204 response will be returned");
        DONE();
    }

    @Test
    @Order(2)
    void email_verification_link_access_success_test() {
        TEST("AHBDB-215: AC5 Verification link accessed successfully (POST request)  - 200 response");
        TEST("AHBDB-2505:AC5&6  Verification link accessed successfully (POST request)  - 200 response");
        setupTestUser();
        GIVEN("a verification link has been sent via email to the customer");
        WHEN("the client returns the correct combination of hash and email ");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        THEN("THEN the platform will return a 200 response");
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        this.emailApi.verifyEmailLink(alphaTestUser, emailVerification, 200);
        AND("store validated status against email");
        DONE();
    }

    @Test
    @Order(3)
    void verification_link_accessed_again_negative_path() {

        TEST("AHBDB-215: AC9 Expiry after use - 400 response, 1002 body");
        TEST("AHBDB-2507: AC8 Expiry after use - 400 response, 1002 body");
        setupTestUser();
        GIVEN("a verification link has been been used by a customer to validate their email");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        this.emailApi.verifyEmailLink(alphaTestUser, emailVerification, 200);
        WHEN("the verification link is accessed again");
        final OBErrorResponse1 response = this.emailApi.verifyEmailLinkError(alphaTestUser, emailVerification, 400);
        THEN("the platform will return a 400 response with 1002 body");
        Assertions.assertTrue(response.getCode().contains(ERROR_CODE_1002), "Error message was not as " +
                "expected, test expected : " + ERROR_CODE_1002 + ". However, error message was " + response.getCode());

        DONE();
    }


    @Order(4)
    @ParameterizedTest
    @ValueSource(strings = {"(*&^%$£)", "12345678909827", " ", "+555126309931", "@bcg.com", "a", "null"})
    void invalid_email_address_400_test_negative_path(String invalidEmailAddress) {
        TEST("AHBDB-215:AC2 Invalid Email Address - 400 response");
        TEST("AHBDB-2502: AC2 Negative Invalid Email Address - 400");
        setupTestUser();
        GIVEN("The client wants to validate an Email address");
        AND("the email address does not satisfy the validation in the data table");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, invalidEmailAddress, 400);
        WHEN("The client attempts to request a verification link via email");
        THEN("The platform responds with a 400 Bad request");
        DONE();
    }

    @Test
    @Order(5)
    void old_email_link_expiry_when_new_link_request_test() {
        TEST("AHBDB-13374 - Test fix");

        TEST("AHBDB-215: AC4 Expiry of old verification link if new link is requested - 400 response, 1002 body");
        TEST("AHBDB-2504:AC4 Expiry of old verification link if new link is requested - 400 response, 1002 body - TBC");
        setupTestUser();
        GIVEN("the client attempts to request a verification link via email");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        AND("a verification link has has already been sent to that email within 24 hours");
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        WHEN("the customer accesses the original link");
        final OBErrorResponse1 response = this.emailApi.verifyEmailLinkError(alphaTestUser, emailVerification, 400);
        THEN("the platform will return a 400 response with 1002 body");
        Assertions.assertTrue(response.getCode().contains(ERROR_CODE_1001), "Error message was not as " +
                "expected, test expected : " + ERROR_CODE_1002 + ". However, the error message was " + response.getCode());

        DONE();

    }

    @Test
    @Order(6)
    void client_returns_invalid_hash_test_negative_path() {
        TEST("AHBDB-215: AC7 Verification link accessed unsuccessfully - 400 response, 1001 body");
        TEST("AHBDB-2506: AC7 Negative - Verification link accessed unsuccessfully - 400 response, 1001 body");
        setupTestUser();
        GIVEN("a verification link has been sent via email to the customer");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        WHEN("the client returns an invalid hash");
        emailVerification.setToken("@^*£((1123abdhc");
        THEN("the platform will return a 400 response with 1001 body");
        final OBErrorResponse1 response = this.emailApi.verifyEmailLinkError(alphaTestUser, emailVerification, 400);
        Assertions.assertTrue(response.getCode().contains(ERROR_CODE_1001), "Error message was not as " +
                "expected, test expected : " + ERROR_CODE_1001);
        DONE();

    }

    @Test
    @Order(7)
    void client_returns_invalid_email_test_negative_path() {
        TEST("AHBDB-215: AC8 Verification link accessed unsuccessfully - 400 response, 1002 body");
        TEST("AHBDB-2518: AC8 Verification link accessed unsuccessfully - 400 response, 1002 body");
        setupTestUser();
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        GIVEN("a verification link has been sent via email to the customer");
        WHEN("the client returns an invalid email");
        emailVerification.setEmail("invalidemail@test.com");
        THEN("the platform will return a 400 response with 1002 body");
        this.emailApi.verifyEmailLink(alphaTestUser, emailVerification, 400);
        final OBErrorResponse1 response = this.emailApi.verifyEmailLinkError(alphaTestUser, emailVerification, 400);
        Assertions.assertTrue(response.getCode().contains(ERROR_CODE_1002), "Error message was not as " +
                "expected, test expected : " + ERROR_CODE_1002);
        DONE();

    }

    @Test
    @Order(8)
    void client_returns_missing_parameters_test_negative_path() {
        TEST("AHBDB-215: AC10 Missing parameters - 400 response");
        TEST("AHBDB-2508: Missing parameters - 400 response");
        setupTestUser();
        GIVEN("a verification link has been sent via email to the customer");
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        EmailVerification emailVerification = this.emailApi.getEmailVerificationLink(alphaTestUser);
        WHEN("the client returns an a response which is missing the email address");
        emailVerification.setEmail("");
        THEN("the platform will return a 400 response");
        this.emailApi.verifyEmailLink(alphaTestUser, emailVerification, 400);
        this.emailApi.generateEmailVerificationLink(alphaTestUser, alphaTestUser.getUserEmail(), 204);
        EmailVerification emailVerification2 = this.emailApi.getEmailVerificationLink(alphaTestUser);
        WHEN("the client returns an a response which is missing the hash");
        emailVerification2.setToken("");
        THEN("the platform will return a 400 response");
        this.emailApi.verifyEmailLink(alphaTestUser, emailVerification2, 400);
        DONE();
    }

    @Test
    public void email_cannot_be_over_40_characters_customer_creation() {
        envUtils.ignoreTestInEnv("13772", Environments.NFT);
        TEST("AHBDB-13772: Email is restricted to 40 character length");
        GIVEN("I have a new customer");
        AlphaTestUser user = new AlphaTestUser();

        AND("That customer's email length is over the specified limit");
        WHEN("I try to create the customer");
        user = this.alphaTestUserFactory.setupUser(user);
        user.setUserEmail(generateEnglishRandomString(40) + "@ahb.com");
        OBErrorResponse1 error = this.customerApiV2.createCustomerError(user, generateCustomerInvalidEmailLength(user), 400);

        THEN("The API will return a 400 bad request");
        Assertions.assertEquals(BAD_REQUEST, error.getCode());
        Assertions.assertEquals(EMAIL_TOO_LONG, error.getMessage());

        DONE();
    }

    @Test
    public void email_cannot_be_over_40_characters_customer_update() {
        envUtils.ignoreTestInEnv("13772", Environments.NFT);
        TEST("AHBDB-13772: Email is restricted to 40 character length");
        GIVEN("I have an existing customer");
        setupTestUser();

        AND("The customer wants to update their email to be over length 40 characters");
        WHEN("I attempt to update the customer's email");
        OBErrorResponse1 error = this.customerApiV2.patchCustomerError(alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .email(generateEnglishRandomString(33) + "@ahb.com")
                        .build())
                .build(), 400);



        THEN("The API will return a 400 bad request");
        Assertions.assertEquals(BAD_REQUEST, error.getCode());
        Assertions.assertEquals(EMAIL_TOO_LONG, error.getMessage());

        DONE();
    }

    private OBWriteCustomer1 generateCustomerInvalidEmailLength(AlphaTestUser alphaTestUser) {
        return OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(generateEnglishRandomString(10))
                        .dateOfBirth(LocalDate.now().minusYears(25))
                        .email(alphaTestUser.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language(alphaTestUser.getLanguage())
                        .firstName(generateEnglishRandomString(10))
                        .lastName(generateEnglishRandomString(10))
                        .fullName(generateEnglishRandomString(20))
                        .gender(alphaTestUser.getGender())
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .termsVersion(LocalDate.of(2020, 12, 20))
                        .termsAccepted(true)
                        .address(OBPostalAddress6.builder()
                                .buildingNumber(generateRandomBuildingNumber())
                                .streetName(generateRandomStreetName())
                                .countrySubDivision(generateRandomCountrySubDivision())
                                .country("AE")
                                .postalCode(generateRandomPostalCode())
                                .addressLine(Collections.singletonList(generateRandomAddressLine()))
                                .build())
                        .build()).build();
    }
}
