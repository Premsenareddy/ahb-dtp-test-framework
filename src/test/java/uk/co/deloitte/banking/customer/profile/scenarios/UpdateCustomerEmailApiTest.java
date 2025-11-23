package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.EmailVerification;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.email.api.EmailVerificationApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;

import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;

/**
 * Created by Bharat Gatty on 16/02/2021
 */

@Tag("@BuildCycle1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdateCustomerEmailApiTest {
    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApi customerapi;

    @Inject
    private OtpApi otpApi;

    @Inject
    private EmailVerificationApi emailVerificationApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;


    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.createCustomerWithValidatedEmail();
        }
    }


    @Test
    void update_customer_email_address_test_happy_path() {
        // Pre-Requisites
        // Customer is onboarded and has a validated email address
        // Customer now wants to update their email address

        TEST("AHBDB-214: AC1 Update customer email");
        TEST("AHBDB-2394:AC1 Positive - Update customer email");
        setupTestUser();
        GIVEN("the customer has specified and validated a different email in bank account onboarding than they had specified in marketplace registration");
        WHEN("the client updates the customer profile with the new email");
        String updateEmailAddress = generateRandomEmail();
        OBWriteCustomerResponse1 response = this.customerapi.updateCustomerEmail(this.alphaTestUser, updateEmailAddressRequest(this.alphaTestUser, updateEmailAddress), 200);
        THEN("the api will return a 200 response");
        AND("the api will replace the old email with this new email");
        Assertions.assertNotNull(response);
        final OBReadCustomer1 currentCustomer = this.customerapi.getCurrentCustomer(this.alphaTestUser);
        OBCustomer1 obCustomerResponse = currentCustomer.getData().getCustomer().get(0);
        assertNotNull(obCustomerResponse.getCustomerId());

        assertEquals(updateEmailAddress,
                obCustomerResponse.getEmail());

    }

    @Test
    void update_customer_email_verification_status_test_happy_path() {

        //AHBDB-12767  -Test failing - Passed

        TEST("AHBDB-214: AC2 Update customer email verification status");
        TEST("AHBDB-2395:AC2 Positive - Update customer email verification status");
        AlphaTestUser alphaTestUserNew = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

        var current = customerapi.getCurrentCustomer(alphaTestUserNew);
        assertEquals(OBWriteEmailState1.NOT_VERIFIED.toString(), current.getData().getCustomer().get(0).getEmailState().toString());

        emailVerificationApi.generateEmailVerificationLink(alphaTestUserNew, alphaTestUserNew.getUserEmail(), 204);
        EmailVerification emailVerification = emailVerificationApi.getEmailVerificationLink(alphaTestUserNew);
        emailVerificationApi.verifyEmailLink(alphaTestUserNew, emailVerification, 200);
        GIVEN("the customer has validated an email in bank account onboarding");

        WHEN("the client updates the customer profile with this validation status");
        AND(" the api will return a 200 response");

        await().atMost(10, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    final OBReadCustomer1 currentCustomer = this.customerapi.getCurrentCustomer(alphaTestUserNew);
                    OBCustomer1 obCustomerResponse = currentCustomer.getData().getCustomer().get(0);
                    assertNotNull(obCustomerResponse.getCustomerId());
                    assertEquals("VERIFIED",
                            obCustomerResponse.getEmailState().toString());
                });
    }

    @Test
    void update_customer_idv_status_test_happy_path() {

        TEST("AHBDB-1887: AC1 Change customer state to IDV completed from IDV review required - 200 response");
        TEST("AHBDB-2713: AC1 Change customer state to IDV completed from IDV review required - 200 response");
        setupTestUser();
        GIVEN("the customer has validated an email in bank account onboarding");

        WHEN("the client updates the customer profile with this validation status");
        OBWriteCustomerResponse1 response = this.customerapi.updateCustomerEmail(this.alphaTestUser, updateIdvStateVerificationStatusRequest(this.alphaTestUser), 200);
        AND(" the api will return a 200 response");
        Assertions.assertNotNull(response);
        final OBReadCustomer1 currentCustomer = this.customerapi.getCurrentCustomer(this.alphaTestUser);
        OBCustomer1 obCustomerResponse = currentCustomer.getData().getCustomer().get(0);
        assertNotNull(obCustomerResponse.getCustomerId());
        assertEquals("IDV_COMPLETED", obCustomerResponse.getCustomerState().toString());

    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678909827", " ", "+555126309931", "@bcg.com", "a", "null", "(*&^%$£)"})
    void update_customer_invalid_email_address_test_negative_path(String invalidEmailAddress) {
        TEST("AC3 Invalid Email Address");
        TEST("AHBDB-2396:AC3 Negative - Invalid Email Address");
        setupTestUser();
        GIVEN("the client wants to store an email address against a user AND the email address does not satisfy the validation in the data table");
        WHEN("The client updates the user with this email address");
        OBErrorResponse1 response = this.customerapi.updateCustomerEmailErrorScenarios(this.alphaTestUser, updateEmailAddressInvalidRequest(this.alphaTestUser, invalidEmailAddress), 400);
        THEN("The platform responds with a 400 Bad request");

    }

    private OBWritePartialCustomer1 updateEmailAddressRequest(AlphaTestUser newCustomer, String updateEmailAddress) {
        return OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .email(updateEmailAddress)
                        .build())
                .build();
    }

    private OBWritePartialCustomer1 updateEmailVerificationStatusRequest(AlphaTestUser newCustomer) {
        return OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .email(generateRandomEmail())
                        .build())
                .build();
    }

    private OBWritePartialCustomer1 updateIdvStateVerificationStatusRequest(AlphaTestUser newCustomer) {
        return OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .email(generateRandomEmail())
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .build())
                .build();
    }

    private OBWritePartialCustomer1 updateEmailAddressInvalidRequest(AlphaTestUser newCustomer, String invalidEmailAddress) {
        return OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .email(invalidEmailAddress)
                        .build())
                .build();
    }
}
