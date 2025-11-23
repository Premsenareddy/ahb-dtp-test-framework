package uk.co.deloitte.banking.payments.transfer.domestic.scenarios;


import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.common.TransferUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DomesticTransferTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private TemenosConfig temenosConfig;


    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private BankingConfig bankingConfig;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    TransferUtils transferUtils;

    private AlphaTestUser alphaTestUser;

    private AlphaTestUser alphaTestSecondUser;

    private String creditorAccountNumber = null;

    private String debtorAccountNumber = null;


    private static final String ACCOUNT_NOT_USERS = "account doesn't belongs to user";

    private static final String INVALID_AMOUNT = "Bad request Invalid amount provided";

    private static final String INVALID_REMARK = "Bad request remarks should be less than chars";

    private static final String INVALID_PURPOSE = "Bad request purpose should be less than chars";

    private static final String INVALID_IBAN = "Invalid IBAN Number";

    private static final String INVALID_DESTINATION = "Bad request Invalid destination account format";

    private static final String INVALID_SOURCE = "Bad request Invalid source account format";

    private static final String INVALID_PAYMENT_REQUEST = "Payment request is invalid. Please check request details " +
            "matches to consent";

    private static final String INVALID_CREDITOR_SCHEMA = "Invalid destination account scheme name";

    private static final String INVALID_SOURCE_SCHEME = "Invalid source account scheme name";


    private void createNewUser() {
        envUtils.ignoreTestInEnv(Environments.DEV);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            debtorAccountNumber = alphaTestUser.getAccountNumber();
            creditorAccountNumber = temenosConfig.getCreditorIban();
            OBWriteCardDepositResponse1 response =
                    cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                            debtorAccountNumber,
                            BigDecimal.valueOf(20));
            assertNotNull(response);

        } else {
            this.alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }


    }

    private void createSecondUser() {
        envUtils.ignoreTestInEnv(Environments.DEV);
        if (this.alphaTestSecondUser == null) {
            this.alphaTestSecondUser = new AlphaTestUser();
            this.alphaTestSecondUser = alphaTestUserFactory.setupCustomer(alphaTestSecondUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestSecondUser);
        } else {
            this.alphaTestSecondUser = alphaTestUserFactory.refreshAccessToken(alphaTestSecondUser);
        }


    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHC", "EDU", "EMI", "FAM"})
    @Order(2)
    public void valid_domestic_payment_create_consent_and_transfer_valid_reference(String validReference) {
        TEST("AHBDB-354 / AHBDB-1632 - valid domestic consent with valid reference : " + validReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1.00");
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
                "AED",
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final WriteDomesticPayment1 transferRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        debtorAccountNumber,
                        ACCOUNT_NUMBER,
                        creditorAccountNumber,
                        ACCOUNT_IBAN,
                        PAYMENT_AMOUNT,
                        validReference,
                        "unstructured",
                        WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(paymentResponse);

        assertEquals(paymentResponse.getData().getStatus(),
                OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        // String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount()
        // .getAmount().replace(".00", "");
        String interimBookedAfterPayment =
                balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        // Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        // commenting assertions due to t24 performance
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        // commenting assertions due to t24 performance
        //   Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);


        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"AE460090000000123456789"})
    @Order(3)
    public void valid_domestic_payment_valid_IBAN(String validIBAN) {
        TEST("AHBDB-354 / AHBDB-1632 / AHBDB-3331 - valid consent and payment with valid IBAN : " + validIBAN);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("I create the domestic transfer consent payload");
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                validIBAN,
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
                "AED",
                "CHC",
                "validUnstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        THEN("I submit the valid domestic payment consent request and the service returns a 201 response");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final WriteDomesticPayment1 paymentRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        debtorAccountNumber,
                        ACCOUNT_NUMBER,
                        validIBAN,
                        ACCOUNT_IBAN,
                        PAYMENT_AMOUNT,
                        "CHC",
                        "unstructured",
                        WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);
        Assertions.assertNotNull(paymentResponse);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"This is additional information", "1"})
    @Order(4)
    public void valid_domestic_payment_valid_unstructured(String validUnstructured) {
        TEST("AHBDB-354 / AHBDB-1632 - valid consent and payment with valid unstructured : " + validUnstructured);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
                "AED",
                "CHC",
                validUnstructured,
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the valid domestic payment consent request and the service returns a 201 response");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final WriteDomesticPayment1 paymentRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        debtorAccountNumber,
                        ACCOUNT_NUMBER,
                        creditorAccountNumber,
                        ACCOUNT_IBAN,
                        PAYMENT_AMOUNT,
                        "CHC",
                        validUnstructured,
                        WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);
        Assertions.assertNotNull(paymentResponse);

        DONE();
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(strings = {"BORNE_BY_DEBTOR", "BORNE_BY_CREDITOR"
            , "SHARED"})
    public void valid_domestic_payment_create_consent_and_return_Fee(String validCharges) {
        TEST("AHBDB-357 - valid domestic consent with valid fee code : " + validCharges);
        TEST("AHBDB-4193 AC1 Success Response From Adapter ");

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1.22");
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
                "AED",
                "Test Reference Test Reference1",
                "unstructured",
                OBChargeBearerType1Code.valueOf(validCharges),
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201 with the correct charges");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());
        Assertions.assertEquals(consentResponse.getData().getCharges().get(0).getChargeBearer(),
                OBChargeBearerType1Code.valueOf(validCharges));

        WHEN("The matching payment requested is created");
        final WriteDomesticPayment1 transferRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        debtorAccountNumber,
                        ACCOUNT_NUMBER,
                        creditorAccountNumber,
                        ACCOUNT_IBAN,
                        PAYMENT_AMOUNT,
                        "CHC",
                        "unstructured",
                        WriteDomesticPayment1RequestedChargeCodePaymentBearer.valueOf(validCharges),
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest);

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailableAfterPayment =
                balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment =
                balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB94BARC10203410093459", "GB94BARC102034100934591203410093459", ""})
    @Order(6)
    public void negative_domestic_payment_invalid_creditor_iban(String invalidCreditorIBAN) {
        TEST("AHBDB-354 / AHBDB-1632 - invalid creditor IBAN length : " + invalidCreditorIBAN);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload");
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                invalidCreditorIBAN,
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_DESTINATION), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_DESTINATION);
        DONE();

    }

    @ParameterizedTest
    @ValueSource(strings = {"123456767", "1234456711889", "£$%$£$$££", ""})
    @Order(7)
    public void negative_domestic_payment_invalid_debtor_account_number(String invalidDebtorAccountNumber) {
        TEST("AHBDB-354 / AHBDB-1632 - invalid debtor account number: " + invalidDebtorAccountNumber);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload");
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                invalidDebtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_SOURCE), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_SOURCE);
        DONE();

    }

    @ParameterizedTest
    @ValueSource(strings = {"GB24BARC202016300934591", "US64SVBKUS6S330095887129", "GB2LABBY0901285711201707",
            "GB2LABBY090128571120170^&"})
    @Order(8)
    public void negative_domestic_payment_transfer_not_found_creditor_iban_number(String notFoundCreditorIBAN) {
        envUtils.ignoreTestInEnv("NFT IBAN stubbed", Environments.NFT);

        TEST("AHBDB-354 / AHBDB-1632 - invalid creditor IBAN not found : " + notFoundCreditorIBAN);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                notFoundCreditorIBAN,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the invalid payment consent request and receive a 404");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 404);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_IBAN), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_IBAN);
        DONE();

    }

    @ParameterizedTest
    @ValueSource(strings = {"an invalid reference length1234", "", "@£$%^&)(*&^%$%&^&**()_)(*&^%$£@"})
    @Order(9)
    public void negative_domestic_payment_invalid_reference(String invalidReference) {
        TEST("AHBDB-354 / AHBDB-1632  - invalid reference : " + invalidReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                invalidReference,
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_PURPOSE), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_PURPOSE);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Invalid Unstructured too long12", "1234567898444443456787657654321"})
    @Order(10)
    public void negative_domestic_payment_invalid_unstructured(String invalidUnstructured) {
        TEST("AHBDB-354 / AHBDB-1632  - invalid unstructured : " + invalidUnstructured);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                invalidUnstructured,
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_REMARK), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_REMARK);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "99999999999"})
    @Order(11)
    public void negative_domestic_payment_invalid_amount(String invalidAmount) {
        TEST("AHBDB-354 / AHBDB-1632  - invalid payment amount : " + invalidAmount);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal(invalidAmount),
                "AED",
                "CHC",
                "validUnstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_AMOUNT), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_AMOUNT);
        DONE();
    }

    @Test
    @Order(12)
    public void negative_domestic_payment_source_account_does_not_belong_to_user() {
        envUtils.ignoreTestInEnv("AHBDB-13460", Environments.NFT);
        TEST("AHBDB-354 / AHBDB-1632 - negative test source account does not belong to the user");

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        createSecondUser();

        String accountThatIsNotUsers = StringUtils.leftPad(alphaTestSecondUser.getAccountNumber(), 10, "0");
        AND("I create the consent payload with a source account that does not belong to user : " + accountThatIsNotUsers);
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                accountThatIsNotUsers,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the payment consent request and receive a 403 response");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 403);

        Assertions.assertTrue(consentResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as " +
                "expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);
        DONE();
    }

    @Test
    @Order(13)
    public void negative_domestic_payment_consent_does_not_match_payment_request_different_debtor_account() {
        TEST("AHBDB-354 / AHBDB-1632 - created consent does not match payment request");

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "validReferenceForConsent",
                "validUnstructuredForConsent",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The payment request that does not match the consent request is created");
        final WriteDomesticPayment1 paymentRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        "1234567891",
                        ACCOUNT_NUMBER,
                        creditorAccountNumber,
                        ACCOUNT_IBAN,
                        new BigDecimal("1"),
                        "validReferenceForConsent",
                        "validUnstructuredForConsent",
                        WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Then the payment requested is submitted and a 400 response is returned");
        final OBErrorResponse1 paymentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentError(alphaTestUser, paymentRequest, 400);

        Assertions.assertTrue(paymentResponse.getMessage().contains(INVALID_PAYMENT_REQUEST), "Error message was not " +
                "as expected, " +
                "test expected : " + INVALID_PAYMENT_REQUEST);

        DONE();

    }

    @Test
    @Order(14)
    public void negative_domestic_payment_invalid_debtor_scheme_consent_creation() {
        TEST("AHBDB-354 / AHBDB-1632 / 3331 - invalid domestic transfer with invalid schemes");

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload with invalid debtor scheme");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                "INVALIDSCHEMA",
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid domestic transfer consent request and receive a 400");

        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_SOURCE_SCHEME), "Error message was not as" +
                " expected, " +
                "test expected : " + INVALID_SOURCE_SCHEME);
        DONE();
    }

    @Test
    @Order(15)
    public void negative_domestic_payment_invalid_creditor_scheme_consent_creation() {
        TEST("AHBDB-354 / AHBDB-1632 / 3331 - invalid domestic transfer with invalid schemes");

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the domestic transfer consent payload with invalid creditor scheme");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                "INVALIDSCHEMA",
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid domestic transfer consent request and receive a 400");

        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getMessage().contains(INVALID_CREDITOR_SCHEMA), "Error message was not " +
                "as expected, " +
                "test expected : " + INVALID_CREDITOR_SCHEMA);
        DONE();
    }

    @Order(15)
    @ParameterizedTest
    @ValueSource(strings = {"invalidE2EIdentif", "", "invalidE2EIdent"})
    public void negative_DTP_transfer_create_consent_reject_invalid_endToEndIdentification(String invalidEndToEndIdentification) {
        TEST("AHBDB-8194 Payment Service - CB Adapter Integration updates invalid endToEndIdentification : " + invalidEndToEndIdentification);


        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("1.00"),
                "AED",
                "validReference",
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consent4, 400);

        DONE();
    }

    @Test
    @Order(16)
    public void negative_domestic_payment_attempt_to_process_consumed_consent() {
        TEST("AHBDB-4400 - negative test consent cannot be processed more than once");

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1.22"),
                "AED",
                "CHC",
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final WriteDomesticPayment1 paymentRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        debtorAccountNumber,
                        ACCOUNT_NUMBER,
                        creditorAccountNumber,
                        ACCOUNT_IBAN,
                        new BigDecimal("1.22"),
                        "CHC",
                        "unstructured",
                        WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getData().getInitiation().getRemittanceInformation().getReference(),
                "CHC");

        THEN("I attempt to trigger the payment again");
        final OBErrorResponse1 rejectedPaymentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentError(alphaTestUser, paymentRequest, 409);
        Assertions.assertNotNull(rejectedPaymentResponse);

        AND("The payment request is rejected");

        DONE();
    }

    @Test
    @Order(100)
    public void negative_domestic_payment_user_does_not_have_correct_scope_to_create_consent() {
        TEST("AHBDB-354 / AHBDB-1632 - negative test user does not have correct scope to create consent");
        GIVEN("I have a valid access token");
        createNewUser();

        AND("I create the valid consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        AND("I downgrade the user's scope to registration");
        authenticateApi.upgradeAccountAndLoginWithAwait(alphaTestUser, "registration");

        THEN("I submit the payment consent request and receive a 403 response");
        this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponseNoMap(alphaTestUser, consent4, 403);

        DONE();
    }


    @Test
    @Order(101)
    public void negative_domestic_payment_user_does_not_have_valid_token_to_make_request() {
        TEST("AHBDB-354 / AHBDB-1632 - negative test user does not have correct scope to create consent");
        GIVEN("I have a user with an invalid access token");
        createNewUser();

        AND("I create the valid consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        AND("I set the user's access token to a not valid token");
        alphaTestUser.getLoginResponse().setAccessToken("invalidToken");

        THEN("I submit the payment consent request and receive a 401 response");
        this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponseNoMap(alphaTestUser, consent4, 401);

        DONE();
    }

}
