package uk.co.deloitte.banking.banking.account.scenarios;

import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.api.RelationshipAccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.common.TransferUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code.BORNE_BY_DEBTOR;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetAccountTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private BankingConfig bankingConfig;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private RelationshipAccountApi relationshipAccountApi;

    @Inject
    TransferUtils transferUtils;

    @Inject
    private CardProtectedApi cardProtectedApi;

    @Inject
    private RelationshipApi relationshipApi;

    private AlphaTestUser alphaTestUser;

    private AlphaTestUser secondAlphaTestUser;
    private AlphaTestUser alphaTestUserChild;


    private static final String FORBIDDEN_ERROR = "UAE.ACCOUNT.FORBIDDEN";
    private static final String ACCOUNT_NOT_USERS = "account doesn't belongs to user";

    private String childId;
    private String connectionId;
    private String fullName = "testUser";
    //run all the tests in order to use the auth tests at the bottom

    public void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);

        }

    }

    public void setupTestUserSecond() {
        if (secondAlphaTestUser == null) {
            secondAlphaTestUser = new AlphaTestUser();
            secondAlphaTestUser = this.alphaTestUserFactory.setupCustomer(this.secondAlphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.secondAlphaTestUser);
        }

    }

    public void setupTestUserChild() {
        if (alphaTestUserChild == null) {
            alphaTestUserChild = new AlphaTestUser();
            childId = alphaTestUserFactory.createChildInForgerock(secondAlphaTestUser, "validtestpassword");
            connectionId = alphaTestUserFactory.createChildInCRM(secondAlphaTestUser,
                    alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
            this.relationshipApi.getRelationships(secondAlphaTestUser);
            alphaTestUserChild =
                    alphaTestUserFactory.createChildCustomer(secondAlphaTestUser, alphaTestUserChild, connectionId, childId);
            this.alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, secondAlphaTestUser, connectionId);
            OBWriteAccount1 request = relationshipAccountApi.createYouthAccountData();
            OBWriteAccountResponse1 account = this.relationshipAccountApi.createDependantCustomerAccount(secondAlphaTestUser, request, connectionId);
            alphaTestUserChild.setAccountNumber(account.getData().getAccountId());

            this.accountApi.executeInternalTransfer(alphaTestUserChild, temenosConfig.getCreditorAccountId(), BigDecimal.valueOf(10));
            createTransaction(alphaTestUserChild);
        }
    }


    @Test
    @Order(1)
    public void positive_a_customer_with_an_account_cif_is_returned_from_cb_adapter_webhook() {
        TEST("AHBDB-8572 Get customer CIF based on Account Number");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        WHEN("I go to get their bank account details");
        THEN("Their details are returned with their CIF in the response");
        OBReadAccount6 obReadAccount6 = this.accountApi.getWebhookAccounts(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(obReadAccount6);
        String cifFromAccounts = obReadAccount6.getData().getAccount().get(0).getAccount().get(2).getIdentification();

        AND("The returned cif matches the one stored in the customer adapter");
        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(alphaTestUser);
        Assertions.assertNotNull(getCustomerResponse);
        String cifFromCustomer = getCustomerResponse.getData().getCustomer().get(0).getCif();
        Assertions.assertEquals(cifFromAccounts, cifFromCustomer);

        DONE();
    }

    @Test
    @Order(1)
    public void positive_get_accountDetails_for_cashDeposit_webhook() {
        TEST("AHBDB-24777 Get Account Details for CashDeposit");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        WHEN("I go to get their bank account details");
        OBReadAccount6 obReadAccount6 = this.accountApi
                .getAccountDetailsForCashDeposit(alphaTestUser, alphaTestUser.getAccountNumber(), OBReadAccount6.class, HttpStatus.OK);
        Assertions.assertNotNull(obReadAccount6);
        Assertions.assertEquals(obReadAccount6.getData().getAccount().get(0).getAccountId(), alphaTestUser.getAccountNumber());

        DONE();
    }

    @Test
    @Order(2)
    public void negative_get_accountDetails_for_cashDeposit_webhook() {
        TEST("AHBDB-24777 Get Account Details for CashDeposit with invalid Account number");
        GIVEN("I have a test user set up with a bank account");
        WHEN("I go to get their bank account details");
        setupTestUser();
        OBReadAccount6 obReadAccount6 = this.accountApi
                .getAccountDetailsForCashDeposit(alphaTestUser, "0141263100332", OBReadAccount6.class, HttpStatus.OK);
        Assertions.assertNull(obReadAccount6.getData().getAccount());
        DONE();
    }

    @Test
    @Order(3)
    public void negative_get_accountDetails_for_cashDeposit_webhook_2() {
        TEST("AHBDB-24777 Get Account Details for CashDeposit without Account number");
        GIVEN("I have a test user set up with a bank account");
        WHEN("I go to get their bank account details");
        this.accountApi
                .getAccountDetailsForCashDeposit(alphaTestUser, "", HttpStatus.FORBIDDEN);
        DONE();
    }

    @Test
    @Order(2)
    public void negative_random_account_results_empty_array_from_cb_adapter_webhook() {
        TEST("AHBDB-8572 Get customer CIF based on Account Number");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        String accountNumber = RandomDataGenerator.generateRandomNumeric(12);
        WHEN("I go to get a random account details : " + accountNumber);
        THEN("an empty list is returned");
        OBReadAccount6 obReadAccount6 = this.accountApi.getWebhookAccounts(alphaTestUser, accountNumber);
        Assertions.assertNull(obReadAccount6.getData().getAccount());

        DONE();
    }

    @Test
    @Order(1)
    public void positive_a_customer_with_an_account_cif_is_returned_from_cb_adapter_protected() {
        TEST("AHBDB-8572 Get customer CIF based on Account Number");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        WHEN("I go to get their bank account details");
        THEN("Their details are returned with their CIF in the response");
        OBReadAccount6 obReadAccount6 = this.accountApi.getProtectedAccounts(alphaTestUser.getAccountNumber(), 200);
        Assertions.assertNotNull(obReadAccount6);
        String cifFromAccounts = obReadAccount6.getData().getAccount().get(0).getAccount().get(2).getIdentification();

        AND("The returned cif matches the one stored in the customer adapter");
        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(alphaTestUser);
        Assertions.assertNotNull(getCustomerResponse);
        String cifFromCustomer = getCustomerResponse.getData().getCustomer().get(0).getCif();
        Assertions.assertEquals(cifFromAccounts, cifFromCustomer);

        DONE();
    }

    @Test
    @Order(2)
    public void negative_random_account_results_empty_array_from_cb_adapter_protected() {
        TEST("AHBDB-13261 - this should return 404");

        TEST("AHBDB-8572 Get customer CIF based on Account Number");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        String accountNumber = RandomDataGenerator.generateRandomNumeric(12);
        WHEN("I go to get a random account details : " + accountNumber);
        THEN("a 404 response is returned to the client");
        OBReadAccount6 obReadAccount6 = this.accountApi.getProtectedAccounts(accountNumber, 404);
        Assertions.assertNull(obReadAccount6.getData().getAccount());

        DONE();
    }

    @Test
    @Order(2)
     void negative_user1_ones_account_cant_be_accessed_by_user2() {
        TEST("AHBDB-13469 access control");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();

        WHEN("User 2 tries to access user 1s account");

        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error1 = this.accountApi.getAccountDetails(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error2 = this.accountApi.getAccountDetails(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error3 = this.accountApi.getAccountDetails(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        DONE();
    }

    @Test
    @Order(2)
    void negative_user1_ones_account_balance_cant_be_accessed_by_user2() {

        TEST("AHBDB-13469 access control");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();

        WHEN("User 2 tries to access user 1s account");

        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error1 = this.accountApi.getAccountBalances(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error2 = this.accountApi.getAccountBalances(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error3 = this.accountApi.getAccountBalances(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        DONE();
    }

    @Test
    @Order(2)
    void negative_user1_ones_account_transactions_cant_be_accessed_by_user2() {
        TEST("AHBDB-13469 access control");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();

        WHEN("User 2 tries to access user 1s account");

        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error1 = this.accountApi.getTransactionDetailsForAccountV2(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error2 = this.accountApi.getTransactionDetailsForAccountV2(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error3 = this.accountApi.getTransactionDetailsForAccountV2(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        DONE();
    }

    @Test
    @Order(2)
    void negative_user1_ones_account_transactions_cant_be_accessed_by_user2_transactionEndpoint() {
        TEST("AHBDB-13469 access control");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();

        WHEN("User 2 tries to access user 1s account");

        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error1 = this.accountApi.accountTransactionsErrorResponse(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error2 = this.accountApi.accountTransactionsErrorResponse(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error3 = this.accountApi.accountTransactionsErrorResponse(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        DONE();
    }

    @Test
    @Order(2)
    void negative_user1_ones_account_locked_amount_cant_be_accessed_by_user2() {
        TEST("AHBDB-13469 access control");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();

        WHEN("User 2 tries to access user 1s account");

        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error1 = this.accountApi.getLockedAmount(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 1, 403);
        Assertions.assertTrue(error1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error2 = this.accountApi.getLockedAmount(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 1,403);
        Assertions.assertTrue(error2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error3 = this.accountApi.getLockedAmount(secondAlphaTestUser, alphaTestUser.getAccountNumber(),1,  403);
        Assertions.assertTrue(error3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        DONE();
    }

    @Test
    @Order(2)
    void negative_user1_ones_account_webhook_cant_be_accessed_by_user2() {

        TEST("AHBDB-13469 access control");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();

        WHEN("User 2 tries to access user 1s account");

        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error1 = this.accountApi.getWebhookAccounts(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error2 = this.accountApi.getWebhookAccounts(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error3 = this.accountApi.getWebhookAccounts(secondAlphaTestUser, alphaTestUser.getAccountNumber(), 403);
        Assertions.assertTrue(error3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        DONE();
    }

    @Test
    @Order(2)
    void negative_user1_ones_account_by_cif_cant_be_accessed_by_user2() {

        TEST("AHBDB-13469 access control");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();

        WHEN("User 2 tries to access user 1s account with user1s cif");
        String cif = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();

        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error1 = this.accountApi.getAccountByCif(secondAlphaTestUser, cif, 403);
        Assertions.assertTrue(error1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error2 = this.accountApi.getAccountByCif(secondAlphaTestUser, cif, 403);
        Assertions.assertTrue(error2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        THEN("a 403 response is returned to the client");
        OBErrorResponse1 error3 = this.accountApi.getAccountByCif(secondAlphaTestUser, cif, 403);
        Assertions.assertTrue(error3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);
        DONE();
    }

    @Test
    @Order(2)
    public void negative_user1_uses_user2_account_to_make_payment() {
        setupTestUser();
        setupTestUserSecond();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer has a valid bank account");
        WHEN("Payment consent is created using a bank account not attached to the customer");
        final OBWriteDomesticConsent4 internalConsent = PaymentRequestUtils.prepareInternalConsent(
                secondAlphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal("11"),
                "AED",
                "reference",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsent4 domesticConsent = PaymentRequestUtils.prepareDomesticConsent(
                secondAlphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "This is additional information",
                BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsent4 legacyConsent = PaymentRequestUtils.prepareLegacyConsent(
                secondAlphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "Reference",
                "This is additional information",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        AND("The payment consent request is submitted");
        final OBErrorResponse1 internalConsentResponse =
                this.transferApiFlows.createInternalPaymentConsentError(alphaTestUser, internalConsent, 403);

        final OBErrorResponse1 domesticConsentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, domesticConsent, 403);

        final OBErrorResponse1 legacyConsentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, legacyConsent, 403);

        THEN("A 403 Unauthorised error will be returned");
        Assertions.assertTrue(internalConsentResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);

        Assertions.assertTrue(domesticConsentResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);

        Assertions.assertTrue(legacyConsentResponse.getMessage().contains(ACCOUNT_NOT_USERS), "Error message was not as expected, " +
                "test expected : " + ACCOUNT_NOT_USERS);

        DONE();
    }

    @Test
    @Order(3)
    public void negative_user1_tries_to_retrieve_user2_child_transactions() {
        envUtils.ignoreTestInEnv("Breaking in NFT with 500", Environments.NFT);

        setupTestUser();
        setupTestUserSecond();
        setupTestUserChild();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer's child has a valid bank account");
        WHEN("Another customer wants to view the child's transactions");
        OBErrorResponse1 error = this.relationshipAccountApi.getAccountTransactions(
                alphaTestUser, alphaTestUserChild.getAccountNumber(), connectionId, 500);

        THEN("The API returns a 500 response - Temenos Response");
        DONE();
    }

    @Test
    @Order(2)
    public void negative_user1_tries_to_create_account_for_user2_child() {
        envUtils.ignoreTestInEnv("Breaking in NFT with 500", Environments.NFT);
        
        setupTestUser();
        setupTestUserSecond();
        setupTestUserChild();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer has a valid bank account");
        OBWriteAccount1 request = relationshipAccountApi.createYouthAccountData();

        AND("They try to create a bank account for a child that isn't theirs");
        OBErrorResponse1 error = this.relationshipAccountApi.createDependantCustomerAccount(alphaTestUser, request, connectionId, 500);

        THEN("The API returns a 500 response - Temenos Response");
        DONE();
    }


    @Test
    @Order(4)
    void get_statement_transaction_details_test_success() {
        TEST("AHBDB-13469 access control");
        envUtils.ignoreTestInEnv("AHBDB-13541 - defect fixed", Environments.NFT);
        TEST("AHBDB-7253 get transaction by transaction reference");
        setupTestUser();
        AND("I have a second user set up");
        setupTestUserSecond();
        BigDecimal PAYMENT_AMOUNT = new BigDecimal("1.00");
        transferUtils.topUpUserAccountWithCardPayment(alphaTestUser, PAYMENT_AMOUNT);

        GIVEN("I have a valid access token and account scope and bank account");
        AND("I create the consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());


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
                        "validReference",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);
        assertEquals(transferResponse.getData().getStatus(),
                OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);

        AND("The test user has a transaction");
        OBReadTransaction6 obReadTransaction6 = this.accountApi.accountTransactions(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(obReadTransaction6);
        String transactionReference = obReadTransaction6.getData().getTransaction().get(0).getTransactionReference();


        WHEN("Calling get transaction details api using the second test users token");
        THEN("A 403 is returned from the service");
        OBErrorResponse1 errorResponse1 = this.accountApi.getAccountStatementTransactions(secondAlphaTestUser, alphaTestUser.getAccountNumber(), transactionReference, 403);
        Assertions.assertTrue(errorResponse1.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);

        OBErrorResponse1 errorResponse2 = this.accountApi.getAccountStatementTransactions(secondAlphaTestUser, alphaTestUser.getAccountNumber(), transactionReference, 403);
        Assertions.assertTrue(errorResponse2.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);

        OBErrorResponse1 errorResponse3 = this.accountApi.getAccountStatementTransactions(secondAlphaTestUser, alphaTestUser.getAccountNumber(), transactionReference, 403);
        Assertions.assertTrue(errorResponse3.getCode().contains(FORBIDDEN_ERROR), "Error message was not as expected," +
                " " +
                "test expected : " + FORBIDDEN_ERROR);

        DONE();
    }

    @Test
    @Order(2)
    public void negative_user1_tries_to_view_user2_specific_transaction() {
        setupTestUser();
        setupTestUserSecond();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer has a valid bank account");
        AND("That account has transactions");
        this.accountApi.executeInternalTransfer(secondAlphaTestUser, temenosConfig.getCreditorAccountId(), BigDecimal.valueOf(10));

        createTransaction(secondAlphaTestUser);

        OBReadTransaction6 obReadTransaction6 = this.accountApi.accountTransactions(secondAlphaTestUser,
                secondAlphaTestUser.getAccountNumber());
        assertNotNull(obReadTransaction6);
        String transactionReference = obReadTransaction6.getData().getTransaction().get(0).getTransactionReference();

        WHEN("User 1 tries to access user 2s account transaction");
        OBErrorResponse1 response = this.accountApi.getAccountStatementTransactions(alphaTestUser, secondAlphaTestUser.getAccountNumber(), transactionReference, 403);

        THEN("a 403 response is returned to the client");
        Assertions.assertEquals(FORBIDDEN_ERROR, response.getCode());

        DONE();
    }

    @Test
    @Order(2)
    public void negative_test_user1_tries_to_access_user2_child_statement_transactions() {
        envUtils.ignoreTestInEnv("AHBDB-13767", Environments.ALL);
        setupTestUser();
        setupTestUserSecond();
        setupTestUserChild();
        TEST("AHBDB-13469 access control");

        GIVEN("A customer has a child with a valid bank account");
        OBReadTransaction6 response = relationshipAccountApi.getAccountTransactions(secondAlphaTestUser,
                alphaTestUserChild.getAccountNumber(), connectionId);

        Assertions.assertNotNull(response);
        String statementId = response.getData().getTransaction().get(0).getTransactionReference();

        WHEN("Another user tries to access the child's transactions");
        OBErrorResponse1 transactionDetailsResponse = relationshipAccountApi.getAccountStatementTransactions(alphaTestUser, alphaTestUserChild.getAccountNumber(),statementId, connectionId, 500);

        THEN("The API will return 500 - Temenos Response");
        DONE();
    }

    @Test
    @Order(3)
    public void negative_test_user1_tries_to_access_user2_child_locked_amount() {
        setupTestUser();
        setupTestUserSecond();
        setupTestUserChild();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer has a child with a bank account");
        WHEN("Another customer tries to access that child's locked amount details");
        OBErrorResponse1 error = this.relationshipAccountApi.getLockedAmount(
                alphaTestUser, alphaTestUserChild.getAccountNumber(), connectionId, 0, 500);

        THEN("The API will return a 500 - Temenos Response");
        DONE();
    }

    @Test
    @Order(3)
    public void negative_test_user1_tries_to_view_user2_child_account_list() {
        setupTestUser();
        setupTestUserSecond();
        setupTestUserChild();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer has a child with a bank account");
        WHEN("Another customer tries to access that child's locked amount details");
        OBErrorResponse1 error = this.relationshipAccountApi.getAccounts(
                alphaTestUser, connectionId, 500);

        THEN("The API will return a 500 - Temenos Response");
        DONE();
    }

    @Test
    @Order(3)
    public void negative_test_user1_tries_to_view_user2_child_account_balances() {
        setupTestUser();
        setupTestUserSecond();
        setupTestUserChild();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer has a child with a bank account");
        WHEN("Another customer tries to access that child's locked amount details");
        OBErrorResponse1 error = this.relationshipAccountApi.getAccountBalances(
                alphaTestUser, alphaTestUserChild.getAccountNumber(), connectionId, 500);

        THEN("The API will return a 500 - Temenos Response");
        DONE();
    }

    @Test
    @Order(3)
    public void negative_test_user1_tries_to_view_user2_child_account_details() {
        setupTestUser();
        setupTestUserSecond();
        setupTestUserChild();
        TEST("AHBDB-13469 access control");
        GIVEN("A customer has a child with a bank account");
        WHEN("Another customer tries to access that child's locked amount details");
        OBErrorResponse1 error = this.relationshipAccountApi.getAccountDetails(
                alphaTestUser, alphaTestUserChild.getAccountNumber(), connectionId, 500);

        THEN("The API will return a 500 - Temenos Response");
        DONE();
    }

    private void createTransaction(AlphaTestUser alphaTestUser) {
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorAccountId(),
                ACCOUNT_NUMBER,
                new BigDecimal(9),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        new BigDecimal("9"), "Transferring $10", "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

    }

    @Test
    @Order(1)
    public void positive_get_accountDetails_for_cashDeposit_cif_webhook() {
        TEST("AHBDB-24777 Get Account Details for CashDeposit with CIF");
        GIVEN("I have a test user set up with a bank account with CIF");
        setupTestUser();
        WHEN("I go to get their bank account details using CIF");
        OBReadAccount6 obReadAccountNumber = this.accountApi.getWebhookAccounts(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(obReadAccountNumber);
        String cifFromAccountNumber = obReadAccountNumber.getData().getAccount().get(0).getAccount().get(2).getIdentification();
        OBReadAccount6 accountDetails = this.accountApi
                .getAccountDetailsForCashDepositCIF(alphaTestUser, cifFromAccountNumber, OBReadAccount6.class, HttpStatus.OK);
        Assertions.assertNotNull(accountDetails);
        Assertions.assertEquals(accountDetails.getData().getAccount().get(0).getAccountId(), alphaTestUser.getAccountNumber());

        DONE();
    }

    @Test
    @Order(2)
    public void negative_get_accountDetails_for_cashDeposit_cif_webhook() {
        TEST("AHBDB-24777 Get Account Details for CashDeposit with CIF");
        GIVEN("I have a test user set up with a bank account with incorrect CIF value");
        setupTestUser();
        WHEN("I go to get their bank account details using CIF");
        OBReadAccount6 obReadAccountNumber = this.accountApi.getWebhookAccounts(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(obReadAccountNumber);
        String cifFromAccountNumber = obReadAccountNumber.getData().getAccount().get(0).getAccount().get(2).getIdentification();
        OBReadAccount6 accountDetails = this.accountApi
                .getAccountDetailsForCashDepositCIF(alphaTestUser, cifFromAccountNumber+0, OBReadAccount6.class, HttpStatus.OK);
        Assertions.assertNull(accountDetails.getData().getAccount());
        DONE();
    }


}
