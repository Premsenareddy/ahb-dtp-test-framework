package uk.co.deloitte.banking.payments.transfer.legacy.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.temenos.api.TemenosApi;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code.BORNE_BY_DEBTOR;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LegacyTransferTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private TemenosApi temenosApi;

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

    private AlphaTestUser alphaTestUser;

    private String creditorAccountNumber = "null";

    private String debtorAccountNumber = "null";

    private final int transferFromT24Amount = 15;

    private static final String INSUFFICIENT_FUNDS = "You do not have sufficient balance in account";

    private static final String INVALID_AMOUNT = "Bad request Invalid amount provided";

    private static final String INVALID_REMARK = "Bad request remarks should be less than chars";

    private static final String INVALID_PURPOSE = "Bad request purpose should be less than chars";

    private static final String CREDITOR_NOT_FOUND = "Creditor account not found";

    private static final String INVALID_IBAN = "Invalid IBAN Number";

    private static final String INVALID_DESTINATION = "Bad request Invalid destination account format";

    private static final String INVALID_SOURCE = "Bad request Invalid source account format";

    private static final String INVALID_PAYMENT_REQUEST = "Payment request is invalid. Please check request details " +
            "matches to consent";


    private void createUserForSetAccountForLogin() {
        envUtils.ignoreTestInEnv("AHBDB-14401", Environments.NFT);
        envUtils.ignoreTestInEnv(Environments.DEV);
        if (this.alphaTestUser == null) {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            debtorAccountNumber = alphaTestUser.getAccountNumber();
            creditorAccountNumber = temenosConfig.getLegacyIban();
            OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(
                    temenosConfig.getCreditorAccountId(), alphaTestUser.getAccountNumber(),
                    BigDecimal.valueOf(transferFromT24Amount));
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

            debtorAccountNumber = alphaTestUser.getAccountNumber();
            creditorAccountNumber = temenosConfig.getLegacyIban();

            OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(alphaTestUser.getAccountNumber(),
                    temenosConfig.getCreditorAccountId(),
                    BigDecimal.valueOf(transferFromT24Amount));
        }
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    //legacy account transfer use the same endpoint and validation as domestic transfers

    @ParameterizedTest
    @ValueSource(strings = {"CRP", "EOS", "SAL", "ALW", "LIP"})
    @Order(1)
    public void valid_legacy_payment_create_consent_and_transfer_valid_reference(String validReference) {
        //AHBDB-12972

        TEST("AHBDB-2402 / AHBDB-1632 / AHBDB-357 - valid domestic consent with valid CHC and no charges : " + validReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
                , "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1.22"),
                "AED",
                validReference,
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        AND("There are no charges and the instrument is set to ahb_legacy");
        Assertions.assertNotNull(consentResponse);
        Assertions.assertNull(consentResponse.getData().getCharges());
        Assertions.assertEquals(consentResponse.getData().getInitiation().getLocalInstrument(), "DtpLegacy");

        WHEN("The matching payment requested is created");
        final OBWriteDomestic2 paymentRequest =
                PaymentRequestUtils.prepareLegacyRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1.22"),
                validReference,
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);
        Assertions.assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getData().getStatus(),
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
        ;
        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"This is additional information", "", "1"})
    @Order(3)
    public void valid_legacy_payment_valid_unstructured(String validUnstructured) {
        TEST("AHBDB-2402 / AHBDB-1632 / AHBDB-357 - valid consent and payment with valid unstructured : " + validUnstructured);
        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                validUnstructured,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);


        THEN("I submit the valid domestic payment consent request and the service returns a 201 response");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        AND("There are no charges");
        Assertions.assertNotNull(consentResponse);
        Assertions.assertNull(consentResponse.getData().getCharges());

        WHEN("The matching payment requested is created");
        final OBWriteDomestic2 paymentRequest =
                PaymentRequestUtils.prepareLegacyRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "CHC",
                validUnstructured,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");
        AND("There are no charges");

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);
        Assertions.assertNotNull(paymentResponse);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB94BARC10203410093459", "GB94BARC102034100934591203410093459", ""})
    @Order(4)
    public void negative_legacy_payment_invalid_creditor_iban(String invalidCreditorIBAN) {
        TEST("AHBDB-2402 / AHBDB-1632 / - invalid creditor IBAN length : " + invalidCreditorIBAN);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                invalidCreditorIBAN,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                BORNE_BY_DEBTOR,
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
    @ValueSource(strings = {"1", "£$%$£$$££", ""})
    @Order(5)
    public void negative_legacy_payment_invalid_debtor_account_number(String invalidDebtorAccountNumber) {
        TEST("AHBDB-2402 / AHBDB-1632 - invalid debtor account number: " + invalidDebtorAccountNumber);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                invalidDebtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

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
    @ValueSource(strings = {"GB24BARC202016300934591", "US64SVBKUS6S33009588792"})
    @Order(6)
    public void negative_legacy_payment_transfer_not_found_creditor_iban_number(String notFoundCreditorIBAN) {
        envUtils.ignoreTestInEnv("NFT IBAN stubbed", Environments.NFT);
        TEST("AHBDB-2402 / AHBDB-1632 - invalid creditor IBAN not found : " + notFoundCreditorIBAN);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                notFoundCreditorIBAN,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                "Unstructured",
                BORNE_BY_DEBTOR,
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
    @ValueSource(strings = {"an invalid invalidReference length1234", "", "@£$%^&)(*&^%$%&^&**()_)(*&^%$£@"})
    @Order(7)
    public void negative_legacy_payment_invalid_CHC(String invalidReference) {
        TEST("AHBDB-2402 / AHBDB-1632  - invalid CHC : " + invalidReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                invalidReference,
                "Unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

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
    @Order(8)
    public void negative_legacy_payment_invalid_unstructured(String invalidUnstructured) {
        TEST("AHBDB-2402 / AHBDB-1632  - invalid unstructured : " + invalidUnstructured);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "CHC",
                invalidUnstructured,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

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
    @ValueSource(strings = {"-2", "-1"})
    @Order(9)
    public void negative_legacy_payment_invalid_amount(String invalidAmount) {
        TEST("AHBDB-2402 / AHBDB-1632 6 - invalid payment amount : " + invalidAmount);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal(invalidAmount),
                "AED",
                "CHC",
                "validUnstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

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
    @Order(10)
    public void negative_legacy_payment_consent_does_not_match_payment_request_different_creditor() {
        TEST("AHBDB-2402 / AHBDB-1632  - created consent does not match payment request different creditor IBAN");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "validCHCForConsent",
                "validUnstructuredForConsent",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The payment request the does not match the consent request is created");
        final OBWriteDomestic2 paymentRequest =
                PaymentRequestUtils.prepareLegacyRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                "TL380010012345678910106",
                ACCOUNT_IBAN,
                new BigDecimal("1"), "validCHCForConsent", "validUnstructuredForConsent",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");


        THEN("Then the payment requested is submitted and a 400 response is returned");
        final OBErrorResponse1 paymentResponse = domesticTransferApiFlows.createDomesticPaymentError(alphaTestUser,
                paymentRequest, 400);

        Assertions.assertTrue(paymentResponse.getMessage().contains(INVALID_PAYMENT_REQUEST), "Error message was not " +
                "as expected, " +
                "test expected : " + INVALID_PAYMENT_REQUEST);

        DONE();
    }

    @Test
    @Order(100)
    public void negative_legacy_payment_user_does_not_have_correct_scope_for_payment() {
        TEST("AHBDB-2402 / AHBDB-1632  - user does not have correct scope for payment");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "validCHCForConsent",
                "validUnstructuredForConsent",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment request is created");
        final OBWriteDomestic2 paymentRequest =
                PaymentRequestUtils.prepareLegacyRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"), "validCHCForConsent", "validUnstructuredForConsent",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");


        AND("I downgrade the scope of the token for the user");
        authenticateApi.upgradeAccountAndLoginWithAwait(alphaTestUser, "registration");

        THEN("Then the payment requested is submitted and a 403 response is returned");
        this.domesticTransferApiFlows.createDomesticPaymentErrorNoMap(alphaTestUser, paymentRequest, 403);

        DONE();

    }


    @Test
    @Order(101)
    public void negative_legacy_payment_user_does_not_have_a_valid_token_for_payment() {
        TEST("AHBDB-2402 / AHBDB-1632  - user does not have a valid token for payment");
        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();
        authenticateApi.upgradeAccountAndLoginWithAwait(alphaTestUser, ScopeConstants.ACCOUNT_SCOPE);

        AND("I create the legacy transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "AED",
                "validCHCForConsent",
                "validUnstructuredForConsent",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountNumber);

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment request is created");
        final OBWriteDomestic2 paymentRequest =
                PaymentRequestUtils.prepareLegacyRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                new BigDecimal("1"),
                "validCHCForConsent",
                "validUnstructuredForConsent",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");


        AND("I set the token as invalid for the user");
        alphaTestUser.getLoginResponse().setAccessToken("invalidToken");

        THEN("Then the payment requested is submitted and a 401 response is returned");
        this.domesticTransferApiFlows.createDomesticPaymentErrorNoMap(alphaTestUser, paymentRequest, 401);

        DONE();

    }
}
