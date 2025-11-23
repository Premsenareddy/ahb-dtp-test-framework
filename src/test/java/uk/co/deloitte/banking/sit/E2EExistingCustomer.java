package uk.co.deloitte.banking.sit;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardPaymentResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.card.model.activation.CardModificationOperation1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadDataTransaction6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.filters.UpdateCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.TransactionType;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifResponse;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.BeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.ReadBeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.WriteBeneficiary1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.WriteBeneficiary1Data;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBReadIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.cif.CifsApi;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.serviceProviderBeneficiary.api.ServiceProviderBeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;

import java.math.BigDecimal;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Tag("@SITRegression")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EExistingCustomer {

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private AccountApi accountApi;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private CustomerApi customerApiV1;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

    @Inject
    private CifsApi cifsApi;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private ServiceProviderBeneficiaryApiFlows serviceProviderBeneficiaryApiFlows;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private BeneficiaryApiFlows beneficiaryApi;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    private CardProtectedApi cardProtectedApi;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private BankingConfig bankingConfig;

    private String EMIRATES_ID = null;

    private static final String CARD_MASK = "000000";

    private String CREATED_CARD_NUMBER = null;

    private static final int loginMinWeightExpected = 31;

    private AlphaTestUser alphaTestUser;

    private OBBeneficiary5 createdBeneficiary;

    private String currentAccountNumber = "0";

    public void loginExistingUser() {
        /**
         * TODO :: Ignoring as these are modular and cannot be run together in BuildKite
         */
        envUtils.ignoreTestInEnv(Environments.ALL);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser.setUserId("27ab54d4-dc39-33b1-a013-3bfe24372e33");
            this.alphaTestUser.setUserPassword("e6d887461a21bce595d157330125cc40f8ce4adfa1b7c696df71b2c930fe996105e9043e9188a2580b457cb4b9307d394c40616bb50629a56361e3b106c1d119");
            this.alphaTestUser.setAccountNumber("019945849001");
            this.alphaTestUser.setDeviceId("13c973df-5855-4c1d-a494-90c49e505c69");
            this.alphaTestUser.setPrivateKeyBase64("AKgSTx0UoOuiaDgLNHFDkg3Ohga4fykVB8GuUNbXyeyh");
            this.alphaTestUser.setPublicKeyBase64("A6k3dxPr05mUX5KeVo+gPdw8XAn0Fh24tfF5hD2SR4TO");
            currentAccountNumber = "014125894001";
            UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                    .userId(this.alphaTestUser.getUserId())
                    .password(this.alphaTestUser.getUserPassword())
                    .build();
            UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginExistingUserProtected(userLoginRequestV2, this.alphaTestUser);
            parseLoginResponse(this.alphaTestUser, userLoginResponseV2);

            OBReadIdvDetailsResponse1 responseIdv = this.customerApiV2.getCustomerIdvDetails(this.alphaTestUser);
            EMIRATES_ID = responseIdv.getData().getDocumentNumber();
        }
    }

    @Test
    public void customer_posts_KYC_form() {
        loginExistingUser();
        TEST("AHBDB-8141: AC9 - Document Generation - KYC - Success");
        TEST("AHBDB-8302: AC9 - Document Generation - KYC - Success");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        WHEN("The client attempts to generate a KYC form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getKYCDocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the KYC form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "KYC", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    public void customer_posts_Account_Opening_form() {
        loginExistingUser();
        TEST("AHBDB-8141: AC9 - Document Generation - Account Opening - Success");
        TEST("AHBDB-8297: AC9 - Document Generation - Account Opening - Success");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        WHEN("The client attempts to generate an Account Opening form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getAccountOpeningDocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the Account Opening form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "AC_OPEN", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    public void customer_posts_IBAN_form() {
        loginExistingUser();
        TEST("AHBDB-8141: AC9 - Document Generation - IBAN - Success");
        TEST("AHBDB-8298: AC9 - Document Generation - IBAN - Success");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        WHEN("The client attempts to generate a IBAN form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getIBANDocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the IBAN form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "IBAN", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    public void customer_posts_CRS_form() {
        loginExistingUser();
        TEST("AHBDB-8141: AC9 - Document Generation - CRS - Success");
        TEST("AHBDB-8296: AC9 - Document Generation - CRS - Success");
        TEST("The customer posts their CRS form");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        alphaTestUser.setCustomerId(customerId);
        WHEN("The client attempts to generate a CRS form for the customer with all of the mandatory consolidated" +
                "list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent CRSdocumentRequest = this.developmentSimulatorService.getCRSDocumentRequest(alphaTestUser);

        THEN("We will generate the form against the CRS form existing template");
        this.developmentSimulatorService.generateDocument(CRSdocumentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "CRS", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });

        DONE();
    }

    @Test
    public void customer_posts_FATCA_form() {
        loginExistingUser();
        TEST("AHBDB-8141: AC9 - Document Generation - FATCA - Success");
        TEST("AHBDB-8294: AC9 - Document Generation - FATCA - Success");
        TEST("The customer posts the FATCA Form");
        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApiV2.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        alphaTestUser.setCustomerId(customerId);

        AND("The customer's 'Form' field is set to W8");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the " +
                "mandatory consolidated list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getW9DocumentRequest(alphaTestUser);

        THEN("We will generate the form against the W8 form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "W9", alphaTestUser.getAccountNumber(),
                            "2021-01", "2021-10", 200);
                    Assertions.assertNotNull(documentRead1.getData().getDocumentFiles());
                    int numberOfStatements = documentRead1.getData().getDocumentFiles().size();
                    Assertions.assertEquals(numberOfStatements, 1);
                });
    }

    @Test
    public void retrieve_account_details() {
        loginExistingUser();
        TEST("AHBDB-7060: AC7 - Get Account Details - Success");
        TEST("AHBDB-7332: AC7 - Retrieve Customer Account Details - Success");
        GIVEN("A customer has created an account");
        AND("I want to retrieve the account details");
        THEN("Their account details can be retrieved from the core banking adapter");
        OBReadAccount6 response = this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(response.getData().getAccount().get(0).getOpeningDate());
        Assertions.assertNotNull(response.getData().getAccount().get(0).getDescription());

        WHEN("I check Transact's User Interface");
        THEN("The account details match those retrieved by the API");
        //Check the API call returns the correct information

        DONE();
    }

    @Test
    public void customer_retrieves_all_accounts() {
        loginExistingUser();
        TEST("AHBDB-9814: AC1 - Customer retrieves list of accounts - Success");
        TEST("AHBDB-10528: AC1 - Happy Path Scenario - Customer retrieves list of accounts");
        GIVEN("The customer has a banking account");
        CifResponse cifResponse1 = this.cifsApi.generateCifNumber(alphaTestUser, EMIRATES_ID);

        AND("They want to retrieve a list of their accounts");
        WHEN("The client sends a request to get the customer's accounts");
        OBReadAccount6 accountList = this.accountApi.getAccountByCif(alphaTestUser, cifResponse1.getCifNumber());

        THEN("The platform will respond with the list of the customer's accounts");
        Assertions.assertTrue(accountList.getData().getAccount().get(0).getAccountSubType().equals(OBExternalAccountSubType1Code.SAVINGS)
                || accountList.getData().getAccount().get(0).getAccountSubType().equals(OBExternalAccountSubType1Code.CURRENT_ACCOUNT));

        if (accountList.getData().getAccount().get(0).getAccountSubType().equals(OBExternalAccountSubType1Code.CURRENT_ACCOUNT)) {
            currentAccountNumber = accountList.getData().getAccount().get(0).getAccountId();
        }
        else {
            currentAccountNumber = accountList.getData().getAccount().get(1).getAccountId();
        }

        DONE();

    }

    @Test
    public void get_account_transactions_from_transact() {
        loginExistingUser();
        TEST("AHBDB-7060: AC2 - Get Account Transactions - Success");
        TEST("AHBDB-7053: AC2 Happy Path Scenario - Get Account Transactions - Success");
        GIVEN("A customer has created a transaction to another customer");
        //Done using other tests in set

        WHEN("I call Transact to get the customer's transactions");
        OBReadTransaction6 transactions =
                this.accountApi.getAccountTransactions(this.alphaTestUser,
                        this.alphaTestUser.getAccountNumber());

        THEN("The API will return 200 OK");
        AND("I receive a list of the customer's transactions");
        Assertions.assertNotNull(transactions.getData(), "No transactions were found");

        WHEN("I check Transact's User Interface");
        THEN("The transactions will match the list returned by the API");
        //Check the API call returns the correct information

        DONE();
    }

    @Test
    void get_locked_amount_from_transact() {
        loginExistingUser();
        TEST("AHBDB-7060: AC3 Get Account Balances - Success");
        TEST("AHBDB-8011: AC3 Get Account Balances - Locked Amount - Success");
        GIVEN("A customer has created an account");
        WHEN("Calling get locked amount api");
        OBReadTransaction6 lockedAmount = this.accountApi.getLockedAmount(alphaTestUser,
                alphaTestUser.getAccountNumber(), 1);

        THEN("Status code 200 is returned");
        assertNotNull(lockedAmount);

        AND("Locked Amount Data is not null");
        OBReadDataTransaction6 lockedAmountData = lockedAmount.getData();
        assertNotNull(lockedAmountData);

        DONE();
    }

    @Test
    public void get_account_balances_from_transact() {
        loginExistingUser();
        TEST("AHBDB-7060: AC3 - Get Account Balances - Success");
        TEST("AHBDB-7054: AC3 Happy Path Scenario - Get Account Balances - Success");
        GIVEN("The client wants a list of a customer's account balances");
        WHEN("I call transact to get the customer's account balances");
        OBReadBalance1 balance =
                this.accountApi.getAccountBalances(this.alphaTestUser,
                        this.alphaTestUser.getAccountNumber());

        THEN("The API will return 200 OK");
        AND("I receive a list of the customer's account balances");
        Assertions.assertNotNull(balance);

        WHEN("I check Transact's User Interface");
        THEN("The account balances match the list returned by the API");
        //Check the API call returns the correct information

        DONE();
    }

    @Test
    public void retrieve_card_details() {
        loginExistingUser();
        TEST("AHBDB-7060: AC9 - Retrieve user's cards - Success");
        TEST("AHBDB-7682: AC9 - Retrieve user's cards - Success");
        GIVEN("A customer has created an account with virtual card");
        AND("I want to retrieve their virtual cards details");
        WHEN("I call the API to retrieve virtual cards details");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertNotNull(cards.getData().getReadCard1DataCard().get(0).getCardNumber());

        THEN("The API returns a 200 OK");
        AND("Their virtual card details can be retrieved from the HPS adapter");

        DONE();
    }

    @Test
    public void positive_test_get_cardCVV() {
        loginExistingUser();
        TEST("AHBDB-7060: AC9 - Get user's cards - Success");
        TEST("AHBDB-7759: AC9 - Retrieve user's cards - CVV - Success");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        final ReadCardCvv1 cardCVvDetails = this.cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        String cvv = cardCVvDetails.getData().getCvv();
        Assertions.assertNotNull(cvv);
        Assertions.assertNotNull(cardCVvDetails.getData().getCardNumber());
        THEN("The encrypted CVV can be retrieved using the created cardId : " + cvv);

        DONE();
    }

    @Test
    public void positive_test_user_updates_spending_limits() {
        loginExistingUser();
        TEST("AHBDB-7060: AC11 - Debit Card Management - Success");
        TEST("AHBDB-7881: AC11 - Debit Card Management - User updates spending limits - Success");
        GIVEN("I have a test user with a created card");
        final ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));

        String cardId = cards.getData().getReadCard1DataCard().get(0).getCardNumber().replace(CARD_MASK, "");
        AND("A valid cardId is created using the unmasked digits of the card number : " + cardId);

        String dailyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(999));
        String monthlyLimit = String.valueOf(RandomDataGenerator.generateRandomInteger(1000000));

        AND(String.format("They update their daily limits to %s and monthly limits to %s ", dailyLimit, monthlyLimit));
        WriteDailyMonthlyLimits1Data writeDailyMonthlyLimits1DataValid = WriteDailyMonthlyLimits1Data.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .dailyAtmLimit(dailyLimit)
                .dailyPosLimit(dailyLimit)
                .monthlyAtmLimit(monthlyLimit)
                .monthlyPosLimit(monthlyLimit)
                .dailyEcommLimit(dailyLimit)
                .monthlyEcommLimit(monthlyLimit)
                .build();

        WriteDailyMonthlyLimits1 writeDailyMonthlyLimits1Valid = WriteDailyMonthlyLimits1.builder().data(writeDailyMonthlyLimits1DataValid).build();

        THEN("They receive a 200 back from the service");
        cardsApiFlows.updateCardLimits(alphaTestUser, writeDailyMonthlyLimits1Valid, 200);

        AND("Their withdrawal limits are updated");
        final ReadCardLimits1 cardLimitsWithdrawal = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.WITHDRAWAL.getLabel(), cardId);

        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsWithdrawal.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        AND("Their purchase limits are updated");
        final ReadCardLimits1 cardLimitsPurchase = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser, TransactionType.PURCHASE.getLabel(), cardId);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyNationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyInternationalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyTotalAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getDailyOnusAmount(), dailyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicOnusAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicNationalAmount(), monthlyLimit);
        Assertions.assertEquals(cardLimitsPurchase.getData().getLimits().getPeriodicTotalAmount(), monthlyLimit);

        DONE();
    }

    @Test
    public void positive_test_user_updates_online_and_abroad_payments() {
        loginExistingUser();
        TEST("AHBDB-7060: AC11 - Debit Card Management - Success");
        TEST("AHBDB-7886: AC11 - Debit Card Management - User updates online and abroad payments - Success");
        GIVEN("I have a valid customer with accounts scope");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to switch off online payments debit card");

        UpdateCardParameters1 validUpdateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internationalUsage(false)
                .internetUsage(false)
                .build();

        THEN("A 200 is returned from the service");
        cardsApiFlows.updateCardParameters(alphaTestUser, validUpdateCardParameters1, 200);

        AND("The users card is shown as having online payments blocked");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");


        AND("The user can turn back on online payments");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internetUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("online payments is turned back on");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");

        DONE();
    }

    @Test()
    public void positive_test_user_blocks_their_card() {
        loginExistingUser();
        TEST("AHBDB-7060: AC11 - Debit Card Management - Success");
        TEST("AHBDB-7889: AC11 - Debit Card Management - User blocks their card - Success");
        GIVEN("I have a valid customer with accounts scope");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("The user has a valid card created with card number : " + CREATED_CARD_NUMBER);
        AND("They make a valid request to block debit card");


        THEN("A 200 is returned from the service");
        cardsApiFlows.blockCardLimitedApi(alphaTestUser, cardId, 200);


        AND("The users card is shown as blocked");
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter1.getData().getInternationalUsage(), "N");
        assertEquals(cardFilter1.getData().getInternetUsage(), "N");
        assertEquals(cardFilter1.getData().getNationalUsage(), "N");

        AND("The user can unblock their card");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(CREATED_CARD_NUMBER)
                .cardNumberFlag("M")
                .internetUsage(true)
                .nationalUsage(true)
                .internationalUsage(true)
                .build();

        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        THEN("their card is unblocked");
        final ReadCardParameters1 cardFilter2 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);
        assertEquals(cardFilter2.getData().getInternationalUsage(), "Y");
        assertEquals(cardFilter2.getData().getInternetUsage(), "Y");
        assertEquals(cardFilter2.getData().getNationalUsage(), "Y");
        DONE();
    }

    @Test
    public void positive_test_customer_deactivates_their_card() {
        loginExistingUser();
        TEST("AHBDB-8141: R4 SIT Integration Tests");
        GIVEN("I have a valid customer with accounts scope");
        String cif = this.customerApiV1.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCif();

        AND("The user has a created card");
        String cardId = CREATED_CARD_NUMBER.replace(CARD_MASK, "");

        WHEN("The user makes a call to deactivate their debit card");
        WriteCardActivation1 cardActivation = WriteCardActivation1.builder()
                .modificationOperation(CardModificationOperation1.DEACTIVATE_PHYSICAL_VIRTUAL)
                .operationReason("Operation reason")
                .build();

        THEN("They receive a 200 response back from the service");
        this.cardsApiFlows.activateDebitCardProtected(cardActivation, cardId, cif, 200);

        AND("The customer's card is deactivated");
        ReadCard1 cards = this.cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertFalse(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated());

        DONE();
    }

    @Test
    public void happy_path_customer_creates_service_beneficiary() {
        loginExistingUser();
        TEST("AHBDB-8141: AC11 Manage Service Beneficiaries");
        TEST("AHBDB-9398: AC11 - Happy Path - Create a valid service beneficiary - Success");
        GIVEN("A customer wants to create or manage their service beneficiaries");
        WriteBeneficiary1 beneficiaryData = createValidBeneData();

        WHEN("The customer sends a request to create or manage their service beneficiaries");
        BeneficiaryResponse1 beneficiaryResponseData = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, beneficiaryData);

        THEN("The client will respond with a list of the customer's service beneficiaries");
        Assertions.assertNotNull(beneficiaryResponseData.getData());

        DONE();
    }

    @Test
    public void happy_path_retrieve_service_beneficiaries() {
        loginExistingUser();
        TEST("AHBDB-8141: AC11 Manage Service Beneficiaries");
        TEST("AHBDB-9401: AC11 - Happy Path - Customer retrieves service beneficiaries");
        GIVEN("A customer wants to retrieve their service beneficiaries");
        WHEN("The customer requests a list of service beneficiaries");
        ReadBeneficiaryResponse1 beneficiaryResponse = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("The client will respond with the list of the customer's service beneficiaries");
        Assertions.assertNotNull(beneficiaryResponse.getData().getBeneficiaryList());

        DONE();
    }

    @Test
    public void happy_path_customer_deletes_service_beneficiary() {
        loginExistingUser();
        TEST("AHBDB-8141: AC11 - Manage Service Beneficiaries");
        TEST("AHBDB-9407: AC11 - Happy Path - Customer deletes service beneficiary by ID");
        GIVEN("A customer wants to delete a service beneficiary from their account");
        ReadBeneficiaryResponse1 response1 = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        WHEN("The customer requests to delete that service beneficiary");
        String beneId = response1.getData().getBeneficiaryList().get(0).getBeneficiaryId();

        THEN("The beneficiary will be deleted from the customer's list");
        this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiary(alphaTestUser, beneId, 204);

        AND("The client will return a list of the customer's service beneficiaries less the deleted one");
        ReadBeneficiaryResponse1 getBenes = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);
        Assertions.assertNull(getBenes.getData().getBeneficiaryList());

        DONE();
    }

    @Test
    public void create_beneficiary_for_user_in_payments() {
        loginExistingUser();
        TEST("AHBDB-8141: AC10 - Step Up Authentication for Beneficiaries");
        TEST("AHBDB-8320: AC10 - Step Up Authentication - Create Beneficiary - Success");
        TEST("AHBDB-7060: AC4 - Create Beneficiaries - Success");
        TEST("AHBDB-7061: AC4 Create Beneficiaries - Success");
        GIVEN("The customer has an Account with no beneficiaries");
        OBReadBeneficiary5 getBeneficiaries = this.beneficiaryApi.getBeneficiaries(this.alphaTestUser);
        Assertions.assertNull(getBeneficiaries.getData().getBeneficiary());

        beneStepUpAuthOTP();
        WHEN("The user tries to create a beneficiary");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName("Test Beneficiary");

        createdBeneficiary = this.beneficiaryApi.createBeneficiaryFlex(this.alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("A beneficiary is created successfully");
        Assertions.assertNotNull(createdBeneficiary.getBeneficiaryId());
        Assertions.assertEquals(beneficiaryData.getNickName(), createdBeneficiary.getSupplementaryData().getNickname());

        DONE();
    }

    @Test
    public void retrieve_beneficiary_from_account() {
        loginExistingUser();
        TEST("AHBDB-7060: AC5 - Get Beneficiaries - Success");
        TEST("AHBDB-7325: AC5 Retrieve Beneficiaries - Success");
        GIVEN("The customer has an account with beneficiaries");
        WHEN("I try to retrieve a list of the beneficiaries");
        OBReadBeneficiary5 getAllBeneficiaries = this.beneficiaryApi.getBeneficiaries(this.alphaTestUser);
        Assertions.assertEquals(1, getAllBeneficiaries.getData().getBeneficiary().size());

        THEN("The API will return 200 OK");
        AND("I will receive a list of the customer's beneficiaries");
        Assertions.assertNotNull(getAllBeneficiaries.getData().getBeneficiary());
        AND("The beneficiary returned by the payments service matches the one the user submitted");
        Assertions.assertEquals(getAllBeneficiaries.getData().getBeneficiary().get(0), createdBeneficiary);

        //Check the API call returns the correct information

        DONE();
    }

    @Test
    public void retrieve_beneficiary_from_account_using_BeneficiaryId() {
        loginExistingUser();
        TEST("AHBDB-7060: AC5 - Get Beneficiaries - Success");
        TEST("AHBDB-7329: AC5 Retrieve Beneficiaries using BeneficiaryID - Success");
        GIVEN("The customer has an account with beneficiaries");
        WHEN("I try to retrieve a list of the beneficiaries");
        OBReadBeneficiary5 getAllBeneficiaries = this.beneficiaryApi.getBeneficiaryById(this.alphaTestUser,
                createdBeneficiary.getBeneficiaryId());

        THEN("The API will return 200 OK");
        AND("I will receive the beneficiary requested");
        Assertions.assertNotNull(getAllBeneficiaries.getData().getBeneficiary());
        System.out.println();
        System.out.println("Nickname: " + getAllBeneficiaries.getData().getBeneficiary().get(0).getSupplementaryData().getNickname());
        System.out.println("Beneficiary ID: " + getAllBeneficiaries.getData().getBeneficiary().get(0).getBeneficiaryId());
        System.out.println("Mobile Number: " + getAllBeneficiaries.getData().getBeneficiary().get(0).getSupplementaryData().getMobileNumber());
        System.out.println();

        WHEN("I check Transact's User Interface");
        THEN("The beneficiary will match the received");
        //Check the API call returns the correct information

        DONE();
    }

    @Test
    public void update_beneficiary_in_account() {
        loginExistingUser();
        TEST("AHBDB-8141: AC10 - Step Up Authentication for Beneficiaries");
        TEST("AHBDB-8321: Step Up Authentication - Update Beneficiary - Success");
        TEST("AHBDB-7060: AC6 - Update Beneficiaries - Success");
        TEST("AHBDB-7328: AC6 Update Beneficiaries - Success");
        GIVEN("The customer has an account with beneficiaries");
        AND("They wish to update a beneficiary's details");
        OBBeneficiary5 getBeneficiary = this.beneficiaryApi.getBeneficiaryById(this.alphaTestUser,
                createdBeneficiary.getBeneficiaryId())
                .getData().getBeneficiary().get(0);
        getBeneficiary.setReference("Test Ref");

        WHEN("I call the API to update the beneficiary");
        beneStepUpAuthOTP();
        THEN("The API will return 200 OK");
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApi.updateBeneficiary(this.alphaTestUser, getBeneficiary);

        AND("I will receive a list of the customer's beneficiaries with updated information");
        Assertions.assertEquals("Test Ref", updatedBeneficiary.getData().getBeneficiary().get(0).getReference());
        System.out.println();
        System.out.println("Nickname: " + updatedBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getNickname());
        System.out.println("Beneficiary ID: " + updatedBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());
        System.out.println("Mobile Number: " + updatedBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getMobileNumber());
        System.out.println();

        WHEN("I check Transact's User Interface");
        THEN("The beneficiaries will match those in the received list");
        //Check the update has been made in Transact

        DONE();
    }

    @Test
    @Order(1)
    @Tag("Individual")
    public void customer_transfers_money_to_their_account() {
        loginExistingUser();
        GIVEN("I have a banking customer");
        int testTransferAmount = 500;

        WHEN("I transfer the test money to the created user's account");

        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(alphaTestUser.getAccountNumber(),
        temenosConfig.getCreditorAccountId(),
        BigDecimal.valueOf(testTransferAmount));
        assertNotNull(response);

        THEN("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        DONE();
    }

    @Test
    @Order(1)
    @Tag("Individual")
    public void customer_deposits_money_into_account() {
        loginExistingUser();
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(500));
    }

    @Test
    @Tag("Individual")
    public void happy_path_internal_transfer_different_account_holders() {
        loginExistingUser();
        TEST("AHBDB-8141: AC3 - Make an internal transfer - Success");
        TEST("AHBDB-8532: AC3 - Happy Path - Make an Internal Transfer - Different Account Holders");
        GIVEN("A customer has a valid bank account");
        WHEN("An internal transfer is initiated");
        internalPaymentsStepUpAuthBiometrics();

        OBWriteDomesticConsent4 consent = createInternalConsentToDifferentAccount(10);

        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        bankingConfig.getBankingUserAccountNumber(),
                        ACCOUNT_NUMBER,
                        new BigDecimal("10"), "Transferring $10", "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(transferResponse);

        DONE();
    }

    @Test
    @Tag("Individual")
    public void happy_path_internal_transfer_same_account_holder() {
        loginExistingUser();
        TEST("AHBDB-8141: AC3 - Make an internal transfer - Success");
        TEST("AHBDB-8144: AC3 - Happy Path - Make an Internal Transfer - Same Account Holder");
        GIVEN("A customer has a valid savings account");
        AND("The customer has a valid current account");
        WHEN("An internal transfer is initiated");
        OBWriteDomesticConsent4 consent = createInternalConsentBetweenSavingsAndCurrentAccount(10);

        OBWriteDomesticConsentResponse5 consentResponse = this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 transferRequest = PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                currentAccountNumber,
                ACCOUNT_NUMBER,
                new BigDecimal(10), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

        THEN("Their account balances will be modified appropriately in T24");
        Assertions.assertNotNull(transferResponse);

        DONE();
    }

    @Test
    public void no_limits_transferring_between_accounts_belonging_to_same_customer() {
        loginExistingUser();
        TEST("AHBDB-8141: AC3 - Make an internal transfer - Success");
        TEST("AHBDB-8665: AC3 - Happy Path - There are no transfer limits when transferring between accounts belonging to the same customer");
        String maxPaymentLimit = paymentConfiguration.getTransferLimit();
        int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 100;


OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(alphaTestUser.getAccountNumber(),
        temenosConfig.getCreditorAccountId(),
        BigDecimal.valueOf(testTransferAmount));
assertNotNull(response);

        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser, alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);
        GIVEN("A customer has a valid current account");
        AND("The customer has a valid savings account");

        WHEN("An internal transfer is initiated between those two accounts with amount over the maximum transfer limit");
        OBWriteDomesticConsent4 consent = createInternalConsentBetweenSavingsAndCurrentAccount(testTransferAmount);

        OBWriteDomesticConsentResponse5 consentResponse = this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 transferRequest = PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                currentAccountNumber,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);

        THEN("Both account balances will be modified appropriately in T24");
        Assertions.assertNotNull(transferResponse);

        DONE();
    }

    @Test
    @Tag("Individual")
    public void happy_path_domestic_transfer() {
        loginExistingUser();
        TEST("AHBDB-8141: AC5 - Make a domestic transfer to a non-AHB Account - Success");
        TEST("AHBDB-8146: AC5 - Happy Path - Make a Domestic Transfer to a non-AHB Account");
        GIVEN("A customer has a valid bank account");
        WHEN("A domestic transfer to a non-AHB Account is initiated");
        OBWriteDomesticConsent4 consent = createDomesticConsent(10, "BORNE_BY_DEBTOR");
        OBWriteDomesticConsentResponse5 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WriteDomesticPayment1 paymentRequest = PaymentRequestUtils.prepareDomesticTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("10"),
                "EDU",
                "unstructured",
                WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_DEBTOR,
                consentResponse.getData().getInitiation().getEndToEndIdentification());

        OBWriteDomesticResponse5 paymentResponse = this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);

        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(paymentResponse);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"BORNE_BY_DEBTOR", "BORNE_BY_CREDITOR", "FOLLOWING_SERVICE_LEVEL", "SHARED"})
    public void happy_path_domestic_transfer_different_charges(String fee) {
        loginExistingUser();
        TEST("AHBDB-8141: AC5 - Make a domestic transfer to a non-AHB Account - Success");
        TEST("AHBDB-8564: AC5 - Happy Path - Make a Domestic Transfer to a non-AHB Account with Different Charges");
        GIVEN("A customer has a valid bank account");
        internalPaymentsStepUpAuthBiometrics();
        AND("The payment consent has a valid charges field");
        OBWriteDomesticConsent4 consent = createDomesticConsent(1, fee);
        OBWriteDomesticConsentResponse5 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());
        Assertions.assertEquals(OBChargeBearerType1Code.valueOf(fee), consentResponse.getData().getCharges().get(0).getChargeBearer());

        WHEN("A domestic transfer to a non-AHB Account is initiated");
        WriteDomesticPayment1 paymentRequest = PaymentRequestUtils.prepareDomesticTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal("10"),
                "Reference",
                "unstructured",
                WriteDomesticPayment1RequestedChargeCodePaymentBearer.valueOf(fee),
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        AND("Their fee matches that given by the charges field in the consent");
        OBWriteDomesticResponse5 paymentResponse = this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);

        THEN("Their account balance and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(paymentResponse);

        DONE();
    }

    @Test
    public void happy_path_legacy_transfer() {
        loginExistingUser();
        TEST("AHBDB-8141: AC4 - Make a domestic transfer to an AHB Account - Success");
        TEST("AHBDB-8145: AC4 - Happy Path - Make a Legacy Transfer");
        GIVEN("A customer has a valid bank account");
        WHEN("A domestic transfer to an AHB Account is initiated");
        OBWriteDomesticConsent4 consent = createLegacyConsent(10);

        OBWriteDomesticConsentResponse5 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        AND("There are no charges");
        Assertions.assertNotNull(consentResponse);
        Assertions.assertNull(consentResponse.getData().getCharges());

        OBWriteDomestic2 paymentRequest = PaymentRequestUtils.prepareLegacyRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                BigDecimal.valueOf(10),
                "EDU",
                "1",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        OBWriteDomesticResponse5 paymentResponse = domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);

        THEN("Their account and their beneficiary's will be modified appropriately in T24");
        Assertions.assertNotNull(paymentResponse);

        DONE();
    }

    @Test
    @Tag("Individual")
    public void happy_path_customer_makes_POS_transaction() {
        loginExistingUser();
        TEST("AHBDB-9814: AC1 - Customer makes a POS Transaction - Success");
        TEST("AHBDB-9513: AC1 - Happy Path Scenario - Customer makes a POS Transaction - Success");
        GIVEN("A customer has a valid bank account with card and PIN details");
        AND("The customer wants to perform a POS transaction");
        WHEN("ESB sends POS Transaction to the Core Banking adapter");
        OBWriteCardPaymentResponse1 response = cardProtectedApi.createCardPayment(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(40));

        THEN("The Core Banking adapter passes the request to Transact and receives a success response");
        assertNotNull(response);

        AND("The request can be viewed in T24");
        DONE();
    }

    private void beneStepUpAuthOTP() {
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private void internalPaymentsStepUpAuthBiometrics() {
        authenticateApiV2.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApiV2.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private OBWriteDomesticConsent4 createInternalConsentBetweenSavingsAndCurrentAccount(int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                currentAccountNumber,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount), "AED", "Transferring $10", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private OBWriteDomesticConsent4 createInternalConsentToDifferentAccount(int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                bankingConfig.getBankingUserAccountNumber(),
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private OBWriteDomesticConsent4 createLegacyConsent(int testTransferAmount) {
        return PaymentRequestUtils.prepareLegacyConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(testTransferAmount),
                "AED",
                "EDU",
                "1",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");
    }

    private OBWriteDomesticConsent4 createDomesticConsent(int testTransferAmount, String fee) {
        return PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(testTransferAmount),
                "AED",
                "EDU",
                "unstructured",
                OBChargeBearerType1Code.valueOf(fee),
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private WriteBeneficiary1 createValidBeneData() {
        return WriteBeneficiary1.builder()
                .data(WriteBeneficiary1Data.builder()
                        .serviceCode("01")
                        .serviceProvider("Estisalat")
                        .serviceTypeCode("04")
                        .serviceType("Mobile(GSM)")
                        .premiseNumber("123456789")
                        .consumerPin("123456789")
                        .phoneNumber(alphaTestUser.getUserTelephone())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .creditor(OBCashAccount50.builder()
                                .schemeName("schemePlaceholder")
                                .name("Service Bene Nickname")
                                .identification(temenosConfig.getCreditorAccountId())
                                .secondaryIdentification("secondary identification")
                                .build())
                        .build())
                .build();
    }
}
