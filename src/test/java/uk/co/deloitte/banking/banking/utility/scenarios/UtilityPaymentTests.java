package uk.co.deloitte.banking.banking.utility.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.cbAdaptor.CBAdaptorResponseError;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupInstructedAmount;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupRequestDataV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.topup.api.TopupApi;
import uk.co.deloitte.banking.banking.utility.api.UtilityPaymentApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
public class UtilityPaymentTests {

    private static final String CREDIT_ACCOUNT_ID = "AED1756800040002";
    private static final String CURRENCY_ID = "AED";
    private static final String REMITTANCE_INFORMATION = "Etisalat - 0553019466";
    private static final String REQUEST_TIME = "20210101160102123";
    public static final String PAYMENT_ORDER_PRODUCT_BY_MONEY = "AHBUTIL";
    public static final String AMOUNT = "100";

    @Inject
    private UtilityPaymentApiFlows utilityPaymentApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    @Inject
    private TopupApi topupApi;

    @BeforeEach
    private void setupTestUser() {
        envUtils.ignoreTestInEnv("Cannot run tests which add money using topup as this messes with calculations in " +
                "Transact", Environments.ALL);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);
        }
    }

    @Test
    public void positive_utility_payment() {
        envUtils.ignoreTestInEnv("Cannot run tests which add money using topup as this messes with calculations in " +
                "Transact", Environments.ALL);

        TEST("AHBDB-10787 - Utility payment");

        GIVEN("I have a valid account and I want to pay utility bills");
        AND("Money is added to user's account");
        addMoneyToAccount(alphaTestUser);
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request and receives a 200 response");
        UtilityPaymentResponse1 response = this.utilityPaymentApi.doUtilityPayment(
                getValidRequest());
        Assertions.assertEquals("success", response.getUtilityPaymentResponseData().getStatus());
        Assertions.assertNotNull(response.getUtilityPaymentResponseData().getPaymentSystemId());
        DONE();
    }

    @Test
    public void negative_utility_payment_insufficient_bal() {

        TEST("AHBDB-12405 - [POST] Negative Flow: " +
                "Verify 422 Response when user post request with Account having Insufficient Balance");

        GIVEN("I have a valid account will nill balance and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request and receives a 422 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest(CREDIT_ACCOUNT_ID,RandomStringUtils.randomAlphanumeric(12),  alphaTestUser.getAccountNumber(),  CURRENCY_ID,
                        REMITTANCE_INFORMATION,  REQUEST_TIME,  PAYMENT_ORDER_PRODUCT_BY_MONEY, "10000"), 422);
        Assertions.assertEquals("UAE.PAYMENTS.INSUFFICIENT_FUNDS", response.getCode());
        Assertions.assertEquals("Transact server error - Bad Request :: utilityPayment", response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_credit_Account() {

        TEST("AHBDB-12399 - [POST] Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing credit account number and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( "",RandomStringUtils.randomAlphanumeric(12),  alphaTestUser.getAccountNumber(),  CURRENCY_ID,
                         REMITTANCE_INFORMATION,  REQUEST_TIME,  PAYMENT_ORDER_PRODUCT_BY_MONEY, AMOUNT), 400);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_E2E_reference() {

        TEST("AHBDB-12399 - [POST] Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing E2E reference and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( CREDIT_ACCOUNT_ID,"",  alphaTestUser.getAccountNumber(),  CURRENCY_ID,
                         REMITTANCE_INFORMATION,  REQUEST_TIME,  PAYMENT_ORDER_PRODUCT_BY_MONEY, AMOUNT), 400);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_debit_Account() {

        TEST("AHBDB-12399 - [POST] Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing debit account number and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( CREDIT_ACCOUNT_ID,RandomStringUtils.randomAlphanumeric(12),  "",  CURRENCY_ID,
                         REMITTANCE_INFORMATION,  REQUEST_TIME,  PAYMENT_ORDER_PRODUCT_BY_MONEY, AMOUNT), 400);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_currency_type() {

        TEST("AHBDB-12399 - [POST] Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing currency type and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( CREDIT_ACCOUNT_ID,RandomStringUtils.randomAlphanumeric(12),  alphaTestUser.getAccountNumber(),  "",
                         REMITTANCE_INFORMATION,  REQUEST_TIME,  PAYMENT_ORDER_PRODUCT_BY_MONEY, AMOUNT), 400);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_remittance_info() {

        TEST("AHBDB-12399 - [POST] Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing data and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( CREDIT_ACCOUNT_ID,RandomStringUtils.randomAlphanumeric(12),  alphaTestUser.getAccountNumber(),  CURRENCY_ID,
                         "",  REQUEST_TIME,  PAYMENT_ORDER_PRODUCT_BY_MONEY, AMOUNT), 400);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_request_time() {

        TEST("AHBDB-12399 - [POST] Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing request time and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( CREDIT_ACCOUNT_ID,RandomStringUtils.randomAlphanumeric(12),  alphaTestUser.getAccountNumber(),  CURRENCY_ID,
                         REMITTANCE_INFORMATION,  "",  PAYMENT_ORDER_PRODUCT_BY_MONEY, AMOUNT), 400);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_missing_paymentOrderProductId() {

        TEST("AHBDB-12399 - [POST] Negative Verify 400 Response when user post request with missing mandatory details.");

        GIVEN("I have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with missing paymentOrderProductId and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( CREDIT_ACCOUNT_ID,RandomStringUtils.randomAlphanumeric(12),  alphaTestUser.getAccountNumber(),  CURRENCY_ID,
                         REMITTANCE_INFORMATION,  REQUEST_TIME,  "", AMOUNT), 400);
        Assertions.assertNotNull(response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }

    @Test
    public void negative_utility_Payment_incorrect_Account() {

        TEST("AHBDB-12403 - [POST] Negative Flow: Verify 404 Response when user post request with Account that Does not Exist ");

        GIVEN("I don't have a valid account and I want to pay utility bills");
        WHEN("I create a request for utility bill payment");
        THEN("The client submits the payment request with incorrect account number and receives a 404 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.doUtilityPaymentNegative(
                getRequest( CREDIT_ACCOUNT_ID,RandomStringUtils.randomAlphanumeric(12),  "1110000000",  CURRENCY_ID,
                         REMITTANCE_INFORMATION,  REQUEST_TIME,  PAYMENT_ORDER_PRODUCT_BY_MONEY, AMOUNT), 404);
        Assertions.assertEquals("UAE.ACCOUNT.NOT_FOUND", response.getCode());
        Assertions.assertNotNull(response.getMessage());
        DONE();
    }


    private UtilityPaymentRequest1 getValidRequest() {

        return UtilityPaymentRequest1.builder().utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                .creditAccountId(CREDIT_ACCOUNT_ID)
                .endToEndReference(RandomStringUtils.randomAlphanumeric(12))
                .debitAccountId(alphaTestUser.getAccountNumber())
                .amount(UtilityPaymentAmount.builder().amount(new BigDecimal("100"))
                        .paymentCurrencyId(CURRENCY_ID).build())
                .remittanceInformation(List.of(REMITTANCE_INFORMATION))
                .requestTimes(List.of(REQUEST_TIME))
                .paymentOrderProduct(PAYMENT_ORDER_PRODUCT_BY_MONEY)
                .build()).build();
    }

    private UtilityPaymentRequest1 getRequest(String creditAccountID, String reffNumber, String accountNumber, String currencyType,
                                                   String remittanceInfo, String requestTime, String paymentOrder, String amount ) {

        return UtilityPaymentRequest1.builder().utilityPaymentRequestData(UtilityPaymentRequest1Data.builder()
                .creditAccountId(creditAccountID)
                .endToEndReference(reffNumber)//RandomStringUtils.randomAlphanumeric(12))
                .debitAccountId(accountNumber)
                .amount(UtilityPaymentAmount.builder().amount(new BigDecimal(amount))
                        .paymentCurrencyId(currencyType).build())
                .remittanceInformation(List.of(remittanceInfo))
                .requestTimes(List.of(requestTime))
                .paymentOrderProduct(paymentOrder)
                .build()).build();
    }

    private void addMoneyToAccount(AlphaTestUser testUser) {
        AccountTopupResponseV1 response = this.topupApi.doTopup(testUser,
                getValidTopupRequest());
    }

    private AccountTopupRequestV1 getValidTopupRequest() {
        return AccountTopupRequestV1.builder()
                .accountTopupRequestData(AccountTopupRequestDataV1.builder()
                        .instructedAmount(AccountTopupInstructedAmount.builder()
                                .amount(BigDecimal.valueOf(1000L))
                                .build())
                        .endToEndReference(org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(12))
                        .remittanceInformation(
                                List.of(REMITTANCE_INFORMATION)
                        )
                        .build()
                )
                .build();
    }
}
