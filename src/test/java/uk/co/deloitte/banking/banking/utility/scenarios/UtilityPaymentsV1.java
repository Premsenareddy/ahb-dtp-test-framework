package uk.co.deloitte.banking.banking.utility.scenarios;

import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.cbAdaptor.CBAdaptorResponseError;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupInstructedAmount;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupRequestDataV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model.UtilityPaymentAmount;
import uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model.UtilityPaymentRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model.UtilityPaymentRequest1Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model.UtilityPaymentResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.topup.api.TopupApi;
import uk.co.deloitte.banking.banking.utility.api.UtilityPaymentApiFlows;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("payments")
public class UtilityPaymentsV1 extends AdultOnBoardingBase {

    private static AlphaTestUser alphaTestUser;
    private static final String REMITTANCE_INFORMATION = "Etisalat - 0553019466";
    private static final String CREDIT_ACCOUNT_ID = "AED1756800040002";
    private static final String CURRENCY_ID = "AED";
    private static final String REQUEST_TIME = "20210101160102123";
    public static final String PAYMENT_ORDER_PRODUCT_BY_MONEY = "AHBUTIL";
    public static final String AMOUNT = "100";
    @Inject
    TopupApi topupApi;

    @Inject
    AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    UtilityPaymentApiFlows utilityPaymentApiFlows;

    private void setupTestUser() {
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);
    }

    @Order(1)
    @Test()
    public void positive_utility_payment() throws Throwable {
        setupTestUser();
        TEST("AHBDB-10787 - Utility payment");

        GIVEN("I have a valid account and I want to pay utility bills");
        AND("Money is added to user's account");
        addMoneyToAccount(alphaTestUser);
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request and receives a 200 response");

        final UtilityPaymentResponse1 response = utilityPaymentApiFlows
                .createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .build())
                        .build(), UtilityPaymentResponse1.class, HttpStatus.OK);

        Assertions.assertEquals("success", response.getUtilityPaymentResponseData().getStatus());
        Assertions.assertNotNull(response.getUtilityPaymentResponseData().getPaymentSystemId());
        DONE();
    }

    @Test
    @Order(2)
    public void negative_utility_payment_insufficient_bal() {

        TEST("Negative Flow: " +
                "Verify 422 Response when user post request with Account having Insufficient Balance");

        GIVEN("I have a valid account will nill balance and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request and receives a 422 response");
        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .amount(UtilityPaymentAmount.builder().amount(new BigDecimal("10000")).build())
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.UNPROCESSABLE_ENTITY);

        Assertions.assertEquals("UAE.PAYMENTS.INSUFFICIENT_FUNDS", response.getCode());
        Assertions.assertEquals("Transact server error - Bad Request :: utilityPayment", response.getMessage());
        DONE();
    }

    @Test
    @Order(3)
    public void negative_utility_Payment_missing_credit_Account() {

        TEST("Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing credit account number and receives a 400 response");

        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .creditAccountId("").build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_E2E_reference() {

        TEST("Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing E2E reference and receives a 400 response");
        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .endToEndReference("")
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_debit_Account() {

        TEST("Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing debit account number and receives a 400 response");

        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId("")
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_currency_type() {

        TEST("Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing currency type and receives a 400 response");

        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .amount(UtilityPaymentAmount.builder().paymentCurrencyId("").build())
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_remittance_info() {

        TEST("Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing data and receives a 400 response");

        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .remittanceInformation(List.of(""))
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_request_time() {

        TEST("Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing request time and receives a 400 response");

        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .requestTimes(List.of(""))
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_paymentOrderProductId() {

        TEST("Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing paymentOrderProductId and receives a 400 response");

        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId(alphaTestUser.getAccountNumber())
                                .paymentOrderProduct("")
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_incorrect_Account() {

        TEST("Negative Flow: Verify 404 Response when user post request with Account that Does not Exist ");

        GIVEN("I don't have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with incorrect account number and receives a 404 response");

        CBAdaptorResponseError response = utilityPaymentApiFlows.createUtilityPaymentV1(alphaTestUser, UtilityPaymentRequest1.builder()
                        .utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                                .debitAccountId("1110000000")
                                .build())
                        .build()
                , CBAdaptorResponseError.class, HttpStatus.NOT_FOUND);
        Assertions.assertEquals("UAE.ACCOUNT.NOT_FOUND", response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    private AccountTopupResponseV1 addMoneyToAccount(AlphaTestUser testUser) {
        return topupApi.doTopup(testUser, getValidTopupRequest());
    }

    private AccountTopupRequestV1 getValidTopupRequest() {
        return AccountTopupRequestV1.builder()
                .accountTopupRequestData(AccountTopupRequestDataV1.builder()
                        .instructedAmount(AccountTopupInstructedAmount.builder()
                                .amount(BigDecimal.valueOf(1000L))
                                .build())
                        .endToEndReference(org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(12))
                        .remittanceInformation(List.of(REMITTANCE_INFORMATION))
                        .build()
                )
                .build();
    }
}
