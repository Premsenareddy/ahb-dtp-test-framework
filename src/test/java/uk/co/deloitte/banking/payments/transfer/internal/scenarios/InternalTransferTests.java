package uk.co.deloitte.banking.payments.transfer.internal.scenarios;

import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
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
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalTransferTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    private BankingConfig bankingConfig;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    TransferUtils transferUtils;

    private AlphaTestUser alphaTestUser;

    private AlphaTestUser alphaTestSecondUser;


    private static final String ACCOUNT_NOT_USERS_ERROR_MSG = "account doesn't belongs to user";
    private static final String ACCOUNT_NOT_USERS = "UAE.AUTH.INSUFFICIENT_PERMISSIONS";

    private static final String INSUFFICIENT_FUNDS = "UAE.PAYMENTS.INSUFFICIENT_FUNDS";

    private static final String INVALID_AMOUNT = "UAE.ERROR.BAD_REQUEST";

    private static final String INVALID_REMARK = "UAE.ERROR.BAD_REQUEST";

    private static final String INVALID_PURPOSE = "UAE.ERROR.BAD_REQUEST";

    private static final String SOURCE_DESTINATION_SAME = "UAE.ERROR.BAD_REQUEST";

    private static final String DEBTOR_NOT_FOUND = "cannot be found or does not belong to user";

    private static final String CREDITOR_NOT_FOUND = "UAE.ERROR.NOT_FOUND";

    private static final String INVALID_DESTINATION = "UAE.ERROR.BAD_REQUEST";

    private static final String INVALID_SOURCE = "UAE.ERROR.BAD_REQUEST";

    private static final String REQUEST_DETAILS_DONT_MATCH = "UAE.ERROR.BAD_REQUEST";

    private static final String INVALID_CONSENT_ID = "Invalid consentId";

    private static final String INVALID_DESTINATION_SCHEME = "UAE.ERROR.BAD_REQUEST";

    private static final String INVALID_SOURCE_SCHEME = "UAE.ERROR.BAD_REQUEST";

    private static BigDecimal balanceAmt= BigDecimal.valueOf(20);

    private void createUserForSetAccountForLogin() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            //transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, BigDecimal.valueOf(20));
            transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, balanceAmt);

        }
        else{
            alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }
    }

    private void createSecondUser() {
        if (this.alphaTestSecondUser == null) {
            this.alphaTestSecondUser = new AlphaTestUser();
            this.alphaTestSecondUser = alphaTestUserFactory.setupCustomer(alphaTestSecondUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestSecondUser);
        } else {
            this.alphaTestSecondUser = alphaTestUserFactory.refreshAccessToken(alphaTestSecondUser);
        }


    }

    // user hardcoded existing user that doesnt create a new account
    private void existingUserLogin() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            alphaTestUser.setUserId(bankingConfig.getBankingUserUserId());
            alphaTestUser.setUserPassword(bankingConfig.getBankingUserPassword());
            alphaTestUser.setAccountNumber(bankingConfig.getBankingUserAccountNumber());
            alphaTestUser.setDeviceId(bankingConfig.getBankingUserDeviceId());
            alphaTestUser.setPrivateKeyBase64(bankingConfig.getBankingUserPrivateKey());
            alphaTestUser.setPublicKeyBase64(bankingConfig.getBankingUserPublicKey());
            UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                    .userId(alphaTestUser.getUserId())
                    .password(alphaTestUser.getUserPassword())
                    .build();
            UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginExistingUserProtected(userLoginRequestV2,
                    alphaTestUser);
            parseLoginResponse(alphaTestUser, userLoginResponseV2);
            OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(alphaTestUser.getAccountNumber(),
                    temenosConfig.getCreditorAccountId(),
                    BigDecimal.valueOf(20));
            assertNotNull(response);
        }
    }

    /*@BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }*/


    @Test
    @Order(1)
    public void negative_DTP_insufficient_funds() {
        envUtils.ignoreTestInEnv("AHBDB-13429 - user is asked to step up before balance is checked", Environments.NFT);
        TEST("AHBDB-12987 - Tests failing");

        TEST("AHBDB-326 - invalid consent and payment with insufficient funds");
        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("75000"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());
        alphaTestUserFactory.refreshAccessToken(alphaTestUser);

        THEN("I submit the valid payment consent request and receive a 422");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 422);

        Assertions.assertTrue(consentResponse.getCode().contains(INSUFFICIENT_FUNDS), "Error message was not as " +
                "expected, " +
                "test expected : " + INSUFFICIENT_FUNDS);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"InternalTransferReferenceValid", "a", "b", "c"})
    public void valid_DTP_transfer_create_consent_and_transfer_valid_reference(String validReference) {
        TEST("AHBDB-13224 & AHBDB-12995- Test failing - Passed");

        TEST("AHBDB-326 / AHBDB-3338 - valid consent and payment with valid reference : " + validReference);
        createUserForSetAccountForLogin();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1.00");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));

        GIVEN("I have a valid access token and account scope and bank account");
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                validReference,
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        THEN("I submit the valid payment consent request");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        Assertions.assertEquals(consentResponse.getData().getInitiation().getCreditorAccount().getIdentification(),
                temenosConfig.getCreditorAccountId());

        AND("I create the valid matching payment transfer request");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        validReference,
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);
        assertEquals(transferResponse.getData().getStatus(),
                OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);

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
    @ValueSource(strings = {"InternalValidUnstructuredData1", ""})
    public void valid_DTP_transfer_valid_unstructured(String validUnstructured) {
        TEST("AHBDB-326 - valid consent and payment with valid unstructured : " + validUnstructured);
        createUserForSetAccountForLogin();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        GIVEN("I have a valid access token and account scope and bank account");
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "Reference",
                validUnstructured,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the valid payment consent request");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        AND("I create the valid matching payment transfer request");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        "Reference",
                        validUnstructured,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);

        THEN("I can view the transactions for the account and assert that they are not null");
        final OBReadTransaction6 obReadTransaction6 = this.accountApi.accountTransactions(alphaTestUser,
                alphaTestUser.getAccountNumber());

        // commenting assertions due to t24 performance
//        Assertions.assertTrue(obReadTransaction6.getMeta().getPageSize() != 0);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456767", "12344567889"})
    public void negative_DTP_transfer_invalid_creditor_account_number(String invalidCreditorAccountNumber) {
        TEST("AHBDB-13224- Test failing");
        TEST("AHBDB-326 - invalid creditor account number length : " + invalidCreditorAccountNumber);
        GIVEN("I have a valid access token and account scope and bank account");

        createUserForSetAccountForLogin();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                invalidCreditorAccountNumber,
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "Reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(INVALID_DESTINATION), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_DESTINATION);
        DONE();

    }

    @ParameterizedTest
    @ValueSource(strings = {"12345611767", "1234456788911"})
    public void negative_DTP_transfer_invalid_debtor_account_number(String invalidDebtorAccountNumber) {
        TEST("AHBDB-326 - invalid debtor account number length : " + invalidDebtorAccountNumber);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                invalidDebtorAccountNumber,
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(INVALID_SOURCE), "Error message was not as expected," +
                " " +
                "test expected : " + INVALID_SOURCE);
        DONE();

    }

    @ParameterizedTest
    @ValueSource(strings = {"123456781191", "111111111111"})
    public void negative_DTP_transfer_transfer_not_found_creditor_account_number(String notFoundCreditorAccountNumber) {
        TEST("AHBDB-13230: defect fix");

        TEST("AHBDB-326 - invalid creditor account number not found : " + notFoundCreditorAccountNumber);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                notFoundCreditorAccountNumber,
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "Reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 404");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 404);

        Assertions.assertTrue(consentResponse.getCode().contains(CREDITOR_NOT_FOUND), "Error message was not as " +
                "expected, " +
                "test expected : " + CREDITOR_NOT_FOUND);
        DONE();

    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789112", "111111111111"})
    public void negative_DTP_transfer_not_found_debtor_account_number(String notFoundDebtorAccountNumber) {
        envUtils.ignoreTestInEnv("update not deployed to NFT", Environments.NFT);

        TEST("AHBDB-326 - invalid debtor account number not found  : " + notFoundDebtorAccountNumber);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                notFoundDebtorAccountNumber,
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 404");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);

        Assertions.assertTrue(consentResponse.getMessage().contains(ACCOUNT_NOT_USERS_ERROR_MSG), "Error message was not as " +
                "expected, " +
                "test expected : " + ACCOUNT_NOT_USERS_ERROR_MSG);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"qwertyuioplkjfffffuytrdxcvbnjki", ""})
    public void negative_DTP_transfer_invalid_reference(String invalidReference) {
        TEST("AHBDB-326 - invalid reference : " + invalidReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                invalidReference,
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(INVALID_PURPOSE), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_PURPOSE);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"qwertyuioplkjfffffuytrdxcvbnjki", "1234567898444443456787657654321"})
    public void negative_DTP_transfer_invalid_unstructured(String invalidUnstructured) {
        TEST("AHBDB-13224- Test failing");
        TEST("AHBDB-326 - invalid unstructured : " + invalidUnstructured);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "reference",
                invalidUnstructured,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(INVALID_REMARK), "Error message was not as expected," +
                " " +
                "test expected : " + INVALID_REMARK);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "22222222222", "-1", "-1000000000"})
    public void negative_DTP_transfer_invalid_amount(String invalidAmount) {
        TEST("AHBDB-326 - invalid amount : " + invalidAmount);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(invalidAmount),
                "AED",
                "Reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(INVALID_AMOUNT), "Error message was not as expected," +
                " " +
                "test expected : " + INVALID_AMOUNT);
        DONE();
    }

    @Test
    public void negative_DTP_transfer_source_account_same_as_destination() {
        TEST("AHBDB-326 - negative test source account cannot be the same as destination account");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(SOURCE_DESTINATION_SAME), "Error message was not as " +
                "expected, " +
                "test expected : " + SOURCE_DESTINATION_SAME);
        DONE();
    }

    @Test
    public void negative_DTP_transfer_consent_does_not_match_transfer_request_amount() {
        TEST("AHBDB-13224- Test failing");

        TEST("AHBDB-326 - negative test consent request does not match the transfer request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the valid payment consent request");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        AND("I create the invalid not matching payment transfer request with a different transfer amount");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        new BigDecimal("2"),
                        "validReference",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBErrorResponse1 transferResponse =
                this.transferApiFlows.createInternalTransferPaymentError(alphaTestUser, transferRequest, 400);

        Assertions.assertTrue(transferResponse.getCode().contains(REQUEST_DETAILS_DONT_MATCH), "Error message was not" +
                " as expected, " +
                "test expected : " + REQUEST_DETAILS_DONT_MATCH);
        DONE();
    }

    @Test
    public void negative_DTP_transfer_consent_does_not_match_transfer_incorrect_consent_id() {

        TEST("AHBDB-13224- Test failing");


        TEST("AHBDB-326 - negative test consent request does not match the transfer request");
        TEST("AHBDB-11562 - Enforce JWS Signature on Payment endpoints");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser, PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the valid payment consent request");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        AND("I create the invalid not matching payment transfer request with an invalid consentId");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(UUID.randomUUID().toString(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        "validReference",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBErrorResponse1 transferResponse =
                this.transferApiFlows.createInternalTransferPaymentError(alphaTestUser, transferRequest, 400);

        //waiting for error message stability
//        Assertions.assertTrue(transferResponse.getCode().contains(REQUEST_DETAILS_DONT_MATCH), "Error message was
//        not as expected, " +
//                "test expected : " + REQUEST_DETAILS_DONT_MATCH);
        DONE();
    }

    @Test
    public void negative_DTP_transfer_source_account_does_not_belong_to_user() {
        envUtils.ignoreTestInEnv("AHBDB-13460", Environments.NFT);

        TEST("AHBDB-326 - negative test source account does not belong to the user");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        createSecondUser();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestSecondUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);

        Assertions.assertTrue(consentResponse.getCode().contains(ACCOUNT_NOT_USERS), "Error message was not as " +
                "expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);
        DONE();
    }

    @Test
    public void negative_DTP_DTP_transfer_invalid_debtor_schema_consent_creation() {
        TEST("AHBDB-326 /AHBDB-3338 - invalid consent with invalid debtor scheme");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload within invalid scheme");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                "INVALIDSCHEMA",
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("1"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(INVALID_SOURCE_SCHEME), "Error message was not as " +
                "expected, " +
                "test expected : " + INVALID_SOURCE_SCHEME);
        DONE();
    }

    @Test
    public void negative_DTP_DTP_transfer_invalid_creditor_schema_consent_creation() {
        TEST("AHBDB-326 / AHBDB-3338 - invalid consent with invalid creditor scheme");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload within invalid scheme");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                "INVALIDSCHEMA",
                new BigDecimal("1"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


        THEN("I submit the valid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        Assertions.assertTrue(consentResponse.getCode().contains(INVALID_DESTINATION_SCHEME), "Error message was not " +
                "as expected, " +
                "test expected : " + INVALID_DESTINATION_SCHEME);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidE2EIdentif", "", "invalidE2EIdent"})
    public void negative_DTP_transfer_create_consent_reject_invalid_endToEndIdentification(String invalidEndToEndIdentification) {
        TEST("AHBDB-8194 Payment Service - CB Adapter Integration updates invalid endToEndIdentification : " + invalidEndToEndIdentification);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal("1.00"),
                "AED",
                "validReference",
                "unstructured",
                invalidEndToEndIdentification);

        THEN("I submit the invalid payment consent request and receive a 400");
        final OBErrorResponse1 consentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 400);

        DONE();
    }
    @Order(1)
    @Test
    public void AHBDB_14844_checking_balance_is_reduced() {
        TEST("AHBDB-14844: Balance not being reduced");
        GIVEN("A customer has a valid bank account with balance");
        createUserForSetAccountForLogin();
        OBReadBalance1 balance =
                this.accountApi.getAccountBalances(this.alphaTestUser,
                        this.alphaTestUser.getAccountNumber());

        //Assertions.assertTrue(balance.getData().getBalance().get(0).getAmount().getAmount().equals("20"));
        AND("Balance is "+balanceAmt);

        AND("Balance returned from API is "+balance.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , ""));
        String actualBalance= balance.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        Assertions.assertTrue(actualBalance.equals(balanceAmt.toString()));
        Assertions.assertNotNull(balance.getData().getBalance().get(0).getAmount());
        AND("They create a DTP-DTP transfer");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                BigDecimal.TEN,
                "AED",
                "EDU",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        new BigDecimal("10"), "Transferring $10", "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

        WHEN("They check their balance again");
        OBReadBalance1 balance2 =
                this.accountApi.getAccountBalances(this.alphaTestUser,
                        this.alphaTestUser.getAccountNumber());
        THEN("The balance will be reduced");
        //Assertions.assertFalse(balance2.getData().getBalance().get(0).getAmount().getAmount().equals("20"));
        Assertions.assertFalse(balance2.getData().getBalance().get(0).getAmount().getAmount().equals(balanceAmt.toString()));
        Assertions.assertNotNull(transferResponse);

        DONE();
    }

}
