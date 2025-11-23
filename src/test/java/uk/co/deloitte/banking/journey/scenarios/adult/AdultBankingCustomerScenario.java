package uk.co.deloitte.banking.journey.scenarios.adult;

import com.google.common.collect.Lists;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardPaymentResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardWithdrawalResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.CardDeliveryAddress1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.*;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ModificationOperation;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.filters.UpdateCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.TransactionType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUsers;
import uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes.TripleDesUtil;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.authorization.OBCustomerAuthorization1;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.common.TransferUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.util.DomesticPaymentRequestUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNTS_LIMITED_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils.parseLoginResponse;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.*;

@Slf4j
@MicronautTest
@TestMethodOrder(OrderAnnotation.class)
class AdultBankingCustomerScenario extends AdultOnBoardingBase {

    public static final String CUSTOMER_TEST = "customer";
    public static final String SMOKE_TEST = "smoke";
    public static final String ACCOUNT_TEST = "account";
    public static final String PAYMENT_TEST = "payment";
    public static final String CARD_TEST = "card";
    public static final String BENEFICIARY_TEST = "beneficiary";

    @Inject
    TransferUtils transferUtils;

    @Inject
    PaymentConfiguration paymentConfiguration;

    @Inject
    TripleDesUtil tripleDesUtil;

    @BeforeEach
    public void ignore() {
        //Ignore all these test in CIT and SIT until deployed there
//        envUtils.ignoreTestInEnv(Environments.SIT);
    }

    @Tag(CUSTOMER_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(1)
    void marketplace_customer_setup_success_test() {
        this.marketplace_customer_setup_success(true);
    }

    @Tag(CUSTOMER_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(2)
    void reauthenticate_test() {
        this.reauthenticate(ACCOUNT_SCOPE);
    }


    @Tag(CUSTOMER_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(3)
    void generate_customer_cif_test() {
        this.generate_customer_cif();
    }

    @Tag(CUSTOMER_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(4)
    void reauthenticateAgain() {
        this.reauthenticate(ACCOUNT_SCOPE);
    }

    @Tag(CUSTOMER_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(5)
    void verify_eid_status_test() {
        TEST("AHBDB-8292 - Set EID status");
        GIVEN("Customer exists");
        assertNotNull(alphaTestUser.getLoginResponse());

        WHEN("The customer receives the card and client wants to mark it as validated");
        OBWriteEIDStatus1 build = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();
        OBWriteCustomerResponse1 obWriteCustomerResponse1 = customerApiV2.updateCustomerValidations(alphaTestUser,
                build);
        THEN("Status 200 is returned");
        assertNotNull(obWriteCustomerResponse1);
        AND("EID status is set to VALID");
        OBReadCustomer1 currentCustomer = customerApiV2.getCurrentCustomer(alphaTestUser);
        assertEquals(OBEIDStatus.VALID, currentCustomer.getData().getCustomer().get(0).getEidStatus());
    }

    @Tag(ACCOUNT_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(10)
    void create_account_test() {

        this.create_account();
    }

    @Tag(ACCOUNT_TEST)
    @Test
    @Order(11)
    void create_current_account_test() {
        this.create_current_account();
    }


    @Tag(CUSTOMER_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(12)
    void get_customer_authz_test() {
        OBCustomerAuthorization1 customerAuthz = this.get_customer_authz();
        AND("CIF is populated");
        assertNotNull(customerAuthz.getData().getCif());
    }

    @Tag(CUSTOMER_TEST)
    @Test
    @Order(99999)
    void dump() {
        AlphaTestUsers.builder().alphaTestUsers(List.of(this.alphaTestUser))
                .build()
                .writeToFile();
        DONE();
    }

    @Tag(ACCOUNT_TEST)
    @Test
    @Order(100)
    void get_account_details_test_success() {


        TEST("AHBDB-5305-Get Account details positive path");

        WHEN("Calling get account details api");

        OBReadAccount6 response = this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber());
        THEN("Status code 200 is returned");
        OBReadAccount6Data responseData = response.getData();
        assertNotNull(responseData);
        AND("Account details has been returned");

        List<OBAccount6> accountList = responseData.getAccount();
        assertNotNull(accountList);

        OBAccount6 account = accountList.get(0);
        assertNotNull(account);

        assertNotNull(account.getAccountId());
        assertNotNull(account.getCurrency());
        assertNotNull(account.getAccountType());
        assertNotNull(account.getDescription());
        assertNotNull(account.getOpeningDate());

        List<OBAccount4Account> accountAccount = account.getAccount();
        assertNotNull(accountAccount);
        assertEquals(3, accountAccount.size());

        assertNotNull(accountAccount.get(0));
        String schemeName = accountAccount.get(0).getSchemeName();
        assertNotNull(schemeName);
        assertEquals("IBAN.NUMBER", schemeName);
        String iban = accountAccount.get(0).getIdentification();
        assertNotNull(iban);

        assertNotNull(accountAccount.get(1));
        schemeName = accountAccount.get(1).getSchemeName();
        assertNotNull(schemeName);
        assertEquals("SWIFT.CODE", schemeName);
        String identification = accountAccount.get(1).getIdentification();
        assertNotNull(identification);

        assertNotNull(accountAccount.get(2));
        schemeName = accountAccount.get(2).getSchemeName();
        assertNotNull(schemeName);
        assertEquals("AHB.CIF", schemeName);
        String cifIdentification = accountAccount.get(2).getIdentification();
        assertNotNull(cifIdentification);


        DONE();
    }

    @Tag(ACCOUNT_TEST)
    @Test
    @Order(103)
    void get_account_test_success() {


        TEST("AHBDB-5019-Get Account List positive path");

        WHEN("Calling get account api");
        OBReadAccount6 response = this.accountApi.getAccountsV2(alphaTestUser);

        THEN("Status code 200(User Accounts) is returned");
        AND("User Accounts has been returned");

        OBReadAccount6Data data = response.getData();
        assertNotNull(data);

        List<OBAccount6> accountList = response.getData().getAccount();
        assertNotNull(accountList);
        assertTrue(accountList.size() > 0);

        OBAccount6 account = accountList.get(0);
        assertNotNull(account);

        assertNotNull(account.getDescription());
        assertNotNull(account.getAccountId());

        assertNotNull(account.getCurrency());
        assertEquals("AED", account.getCurrency());

        assertNotNull(account.getAccountType());
        assertNotNull(account.getOpeningDate());

        List<OBAccount4Account> accounts = account.getAccount();
        assertNotNull(accounts);
        assertEquals(3, accounts.size());

        OBAccount4Account accountDetails = accounts.get(0);
        assertNotNull(accountDetails.getSchemeName());
        assertEquals("IBAN.NUMBER", accountDetails.getSchemeName());

        accountDetails = accounts.get(1);

        assertNotNull(accountDetails.getSchemeName());
        assertEquals("SWIFT.CODE", accountDetails.getSchemeName());
        assertNotNull(accountDetails.getIdentification());
        DONE();
    }

    @Tag(ACCOUNT_TEST)
    @Test
    @Order(104)
    void get_account_details_by_accountid_test_success() {

        TEST("AHBDB-5019-Get Account List positive path");

        WHEN("Calling get account api");
        this.accountApi.getAccountDetails(alphaTestUser, alphaTestUser.getAccountNumber());

        THEN("Status code 200(User Accounts) is returned");
        AND("User Accounts has been returned");

        AND("When request fired to fetch account details of different user");
        THEN("Should get response of status 403");
        this.accountApi.getAccountDetailsWithError(alphaTestUser, temenosConfig.getCreditorAccountId(), 403);
        DONE();
    }


    //NOTE Ordering of tests are important here for payments to manage limits.
    //NOTE After executing transfer tests repay money from test account to temenos seed account
    //Internal payments with in authorised daily limits tests
    @Tag(PAYMENT_TEST)
    @Test
    @Order(200)
    void create_internal_dtp_transfers_within_all_limits() {

        alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);

        TEST("AHDB-326 :: Test DTP to DTP internal transfers with in all limits");
        final BigDecimal balanceBeforeTransfer =
                new BigDecimal(getBalanceForAccountAndType(alphaTestUser.getAccountNumber(),
                        OBBalanceType1Code.INTERIM_BOOKED).getAmount().getAmount());
        final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(5);
        final BigDecimal DEPOSITED_AMOUNT = BigDecimal.valueOf(15);

        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 12, "0");

        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 12, "0");

        GIVEN("User has account scope");
        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountId);
        //deposit money into account before trigger transfer account
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, DEPOSITED_AMOUNT);

        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        WHEN("User has enough funds for transfer in his account");
        WHEN("Transfer amount is less than authorised limit");
        WHEN("Transfer amount is less than daily limit");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                temenosAccountId,
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "Api tester - internal " +
                        "payments",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Consent should be created");
        final OBWriteDomesticConsentResponse5 consentResponseSuccess =
                internalTransferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        Assertions.assertTrue(StringUtils.isNotBlank(consentResponseSuccess.getData().getConsentId()));

        TEST("Trigger internal transfer payment");
        WHEN("Sufficient funds available");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponseSuccess.getData().getConsentId(),
                        debtorAccountId,
                        ACCOUNT_NUMBER,
                        temenosAccountId,
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        "Api tester - internal " +
                                "payments",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                internalTransferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);
        BigDecimal expectedBalance = balanceBeforeTransfer.add(DEPOSITED_AMOUNT);
        expectedBalance = expectedBalance.subtract(PAYMENT_AMOUNT);
        confirmUserAccountBalance(alphaTestUser.getAccountNumber(),
                expectedBalance,
                OBBalanceType1Code.INTERIM_BOOKED);
        //TODO test transactions api
        DONE();
    }

    //Domestic payments with within authorised limits
    @Tag(PAYMENT_TEST)
    @Test
    @Order(201)
    void create_domestic_dtp_transfers_within_all_limits() {

        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);

        TEST("AHDB-326 :: Test DTP to DTP internal transfers with in all limits");
        final int AMOUNT_LESS_THEN_DAILY_LIMIT = 5;

        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 10, "0");

        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 10, "0");

        GIVEN("User has account scope");
        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountId);
        //deposit money into account before trigger transfer account
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, BigDecimal.valueOf(200));
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
         BigDecimal.valueOf(AMOUNT_LESS_THEN_DAILY_LIMIT), Lists.newArrayList(EQUAL_TO, LESS_THEN),
          alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

//        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00"
//                , "");
        String interimAvailable = balanceResponse.getData().getBalance()
                .stream()
                .filter((b) -> b.getType().toString().equalsIgnoreCase("INTERIM_AVAILABLE"))
                .map((b) -> b.getAmount().getAmount())
                .findFirst()
                .get();

        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        WHEN("User has enough funds for transfer in his account");
        WHEN("Transfer amount is less than authorised limit");
        WHEN("Transfer amount is less than daily limit");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                CREDITOR_ACCOUNT_ID,
                ACCOUNT_IBAN,
                (BigDecimal.valueOf(AMOUNT_LESS_THEN_DAILY_LIMIT)),
                "AED",
                "CHC",
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Consent should be created");
        final OBWriteDomesticConsentResponse5 consentResponseSuccess =
                domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        Assertions.assertTrue(StringUtils.isNotBlank(consentResponseSuccess.getData().getConsentId()));

        TEST("Trigger domestic transfer payment");
        WHEN("Sufficient funds available");
        final WriteDomesticPayment1 transferRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponseSuccess.getData().getConsentId(),
                        debtorAccountId,
                        ACCOUNT_NUMBER,
                        CREDITOR_ACCOUNT_ID,
                        ACCOUNT_IBAN,
                        (BigDecimal.valueOf(AMOUNT_LESS_THEN_DAILY_LIMIT)),
                        "CHC",
                        "unstructured",
                        BORNE_BY_CREDITOR,
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti"
                );

        final OBWriteDomesticResponse5 transferResponse =
                domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, transferRequest);
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

        DONE();
    }

    //NOTE 3 - internal payments with in daily limits ( transfers with in user own accounts )
    @Tag(PAYMENT_TEST)
    @Test
    @Order(202)
    void create_internal_dtp_transfers_within_all_limits_when_high_value_payments_btw_own_accounts_exists() {
        TEST("AHBDB-12936 - Tests failing");
        TEST("AHDB-326 :: Test DTP to DTP internal transfers with in all limits");
        final BigDecimal balanceBeforeTransfer =
                new BigDecimal(getBalanceForAccountAndType(alphaTestUser.getAccountNumber(),
                        OBBalanceType1Code.INTERIM_BOOKED).getAmount().getAmount());
        final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(5);

        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 12, "0");

        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 12, "0");

        GIVEN("User has account scope");
        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountId);
        WHEN("Internal transfer was success between user's current and savings accounts");
        THEN("Internal transfers between user accounts should not be considered while calculating limits");
        executeInternalTransferWithInOwnAccounts(50000);
        WHEN("Internal transfers between users savings and current account is more than unauthorised limit is a " +
                "success");
        THEN("Step up should not be forced");
        //deposit money into account before trigger transfer account
        final BigDecimal DEPOSITED_AMOUNT = BigDecimal.valueOf(15);
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, DEPOSITED_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        WHEN("User has enough funds for transfer in his account");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                temenosAccountId,
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "Api tester - internal " +
                        "payments",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Consent should be created");
        final OBWriteDomesticConsentResponse5 consentResponseSuccess =
                internalTransferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        Assertions.assertTrue(StringUtils.isNotBlank(consentResponseSuccess.getData().getConsentId()));

        TEST("Trigger internal transfer payment");
        WHEN("Sufficient funds available");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponseSuccess.getData().getConsentId(),
                        debtorAccountId,
                        ACCOUNT_NUMBER,
                        temenosAccountId,
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        "Api tester - internal " +
                                "payments",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                internalTransferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);
        assertEquals(transferResponse.getData().getStatus(),
                OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);

        BigDecimal expectedBalance = balanceBeforeTransfer.add(DEPOSITED_AMOUNT);
        expectedBalance = expectedBalance.subtract(PAYMENT_AMOUNT);
        confirmUserAccountBalance(alphaTestUser.getAccountNumber(),
                expectedBalance,
                OBBalanceType1Code.INTERIM_BOOKED);
        //TODO test transactions api
        DONE();
    }


    //TODO 4 - domestic payments with in daily limits ( transfers with in user own accounts )

    //NOTE 5 - internal payments with stepup needed
    @Tag(PAYMENT_TEST)
    @Test
    @Order(203)
    void create_internal_dtp_transfers_when_unauthorised_limit_reached() {
        TEST("AHBDB-12936 - Tests failing");
        TEST("AHDB-326 :: Test DTP to DTP internal transfers with in all limits");
        final BigDecimal balanceBeforeTransfer =
                new BigDecimal(getBalanceForAccountAndType(alphaTestUser.getAccountNumber(),
                        OBBalanceType1Code.INTERIM_BOOKED).getAmount().getAmount());
        //max unauthorised limit is 250000 . Previous payments made through tests should have consent records created
        // with consumed status
        //of total amount = 10 . With 250000 + 10 should force stepup auth for this payment request
        final BigDecimal PAYMENT_AMOUNT = new BigDecimal(paymentConfiguration.getMaxUnauthLimit());
        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 10, "0");
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 10, "0");

        GIVEN("User has account scope");
        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountId);
        //deposit money into account before trigger transfer account
        final BigDecimal DEPOSITED_AMOUNT = PAYMENT_AMOUNT.add(BigDecimal.TEN);
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, DEPOSITED_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
         PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        WHEN("User has enough funds for transfer in his account");
        WHEN("Transfer amount with spend for current day is greater than unauthorised limit");
        WHEN("Transfer amount with spend for current day is less than daily limit");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                temenosAccountId,
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT,
                "AED",
                "Api tester - internal " +
                        "payments",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");


        THEN("Should be forced to perform step up for executing the payment");
        AND("Should receive 403 status code");
        final uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 consentError =
                internalTransferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 403);

        WHEN("User chooses OTP stepup option");
        //User triggers OTP flow
        //1) -> initiate step up authentication
        WHEN("Stepup auth request was initiated");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(32).build());
        THEN("User will receive OTP");

        //2) -> extract otp from dev simulator
        //extract otp from device simulator
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(isNotBlank(otpCO.getPassword()));

        AND("Stepup auth validation will be triggered");
        final StepUpAuthRequest stepUpAuthValidationRequest =
                StepUpAuthRequest.builder().otp(otpCO.getPassword()).weight(32).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
        THEN("OTP validation should success");

        GIVEN("Cache entry was successful for userId");
        THEN("Creating consent should be successful");
        final OBWriteDomesticConsentResponse5 consentResponse5 =
                internalTransferApiFlows.createInternalPaymentConsent(alphaTestUser, consent4);

        TEST("Trigger internal transfer payment");
        WHEN("Sufficient funds available");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse5.getData().getConsentId(),
                        debtorAccountId,
                        ACCOUNT_NUMBER,
                        temenosAccountId,
                        ACCOUNT_NUMBER,
                        PAYMENT_AMOUNT,
                        "Api tester - internal " +
                                "payments",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticResponse5 transferResponse =
                internalTransferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        THEN("Internal payment should be successful");
        Assertions.assertNotNull(transferResponse);
        BigDecimal expectedBalance = balanceBeforeTransfer.add(DEPOSITED_AMOUNT);
        expectedBalance = expectedBalance.subtract(PAYMENT_AMOUNT);
        confirmUserAccountBalance(alphaTestUser.getAccountNumber(),
                expectedBalance,
                OBBalanceType1Code.INTERIM_BOOKED);
        //TODO test transactions api
        DONE();
    }
    //TODO 6 - domestic payments with stepup needed


    //TODO 7 - internal payments with out of daily limits
    @Tag(PAYMENT_TEST)
    @Test
    @Order(204)
    public void create_internal_dtp_transfers_when_limits_reached_daily_limits() {
        TEST("AHBDB-12936 - Tests failing");
        TEST("AHDB-326 :: Test DTP to DTP internal transfers exceeding daily limits");
        //max dialy limits is 1000000. Internal payment more than 1000000 should throw error code of 422
        final BigDecimal PAYMENT_AMOUNT = new BigDecimal(paymentConfiguration.getMaxPaymentLimit());
        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 10, "0");
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 10, "0");

        GIVEN("User has account scope");
        WHEN("Funds transfers to users savings account from T24 funds account :" + debtorAccountId);
        //deposit money into account before trigger transfer account
        final BigDecimal DEPOSIT_AMOUNT = PAYMENT_AMOUNT.add(BigDecimal.TEN);
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, DEPOSIT_AMOUNT);
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        WHEN("User has enough funds for transfer in his account");
        WHEN("Transfer amount is greater then daily limits set which is 1000000 ");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                temenosAccountId,
                ACCOUNT_NUMBER,
                PAYMENT_AMOUNT.add(BigDecimal.ONE),
                "AED",
                "Api tester - internal " +
                        "payments",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("Consent creation should fail with status code 422 Unprocessible Entity");
        AND("Should receive 403 status code");
        final uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1 consentError =
                internalTransferApiFlows.createInternalPaymentConsentError(alphaTestUser, consent4, 422);
        THEN("Consent creation should fail with Unprocessible entity 422 status code");
        DONE();
    }

    //TODO 8 - domestic payments with out of daily limits
    @Tag(PAYMENT_TEST)
    @Test
    @Order(220)
    public void create_domestic_dtp_transfers() {
        TEST("AHDB-354 :: Test DTP domestic transfer consent");
        TEST("AHDB-354/AHDB-1632 :: Test DTP domestic transfer consent & execution");

        final int AMOUNT = 10;

        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 10, "0");
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 10, "0");


        GIVEN("User has account scope");
        WHEN("User has enough funds for transfer in his account");
        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountId);
        //deposit money into account before trigger transfer account
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, BigDecimal.valueOf(100011));
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
         BigDecimal.valueOf(AMOUNT), Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        WHEN("Transfer amount is less than daily limit");
        THEN("Consent should be created");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                CREDITOR_ACCOUNT_ID,
                ACCOUNT_IBAN,
                (BigDecimal.valueOf(AMOUNT)),
                "AED",
                "Api tester - domestic " +
                        "payments",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        final OBWriteDomesticConsentResponse5 consentResponse =
                domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
        assertNotNull(consentResponse);
        assertNotNull(consentResponse.getData().getConsentId());

        WHEN("Transfer is attempted ");
        //transaction from debtor account
        OBWriteDomesticResponse5 obWriteDomesticResponse5 =
                accountApi.executeInternalTransferBetweenAccounts(alphaTestUser, temenosAccountId, debtorAccountId,
                        BigDecimal.valueOf(AMOUNT));

        THEN("Transfer is successful");
        assertNotNull(obWriteDomesticResponse5);

        AND("Balance is updated");
        //TODO: Confirm balance

        AND("Transaction list is updated");
        //TODO: Get transactions and assert

        DONE();
    }

//
//    @Test
//    @Order(210)
//    public void create_domestic_dtp_transfers_with_otp() {
//        //TODO Enable when CB adapter returns back with SUCCESS for fees endpoint
//        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT, Environments.NFT);
//        TEST("AHDB-354 :: Test DTP domestic transfer consent");
//        TEST("AHDB-354/AHDB-1632 :: Test DTP domestic transfer consent & execution");
//
//        final int AMOUNT_LESS_THEN_DAILY_LIMIT = 49000;
//        final int AMOUNT_GREATER_THEN_DAILY_UNAUTHORIZED_LIMIT = 50001;
//
//        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 10, "0");
//
//        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 10, "0");
//        //TODO check with valid external creditor account
//        final String creditorAccountId = "AE850260000674711996301";
//
//        GIVEN("User has account scope");
//        WHEN("User has enough funds for transfer in his account");
//        WHEN("Funds transfers to savings account from T24 funds account :" + debtorAccountId);
//        //deposit money into account before trigger transfer account
//        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, BigDecimal.valueOf(100011));
//        WHEN("Transfer amount is less than daily limit");
//        THEN("Consent should be created");
//        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalDomesticConsent(
//                debtorAccountId,
//                ACCOUNT_NUMBER,
//                creditorAccountId,
//                ACCOUNT_IBAN,
//                (BigDecimal.valueOf(AMOUNT_LESS_THEN_DAILY_LIMIT)),
//                "AED",
//                "Api tester - domestic " +
//                        "payments",
//                "unstructured");
//
//        final OBWriteDomesticConsentResponse5 consentResponse =
//                domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent4);
//        Assertions.assertNotNull(consentResponse);
//        Assertions.assertNotNull(consentResponse.getData().getConsentId());
//
//        WHEN("User's total worth of current day's transaction worth more than daily unauthorized limit");
//        //transaction from debtor account
//        accountApi.executeInternalTransferBetweenAccounts(alphaTestUser, temenosAccountId, debtorAccountId,
//                BigDecimal.valueOf(AMOUNT_GREATER_THEN_DAILY_UNAUTHORIZED_LIMIT + 5));
//        WHEN("Transfer amount is greater than unauthorized daily limit");
//        THEN("Should get FORBIDDEN");
//        final OBWriteDomesticConsent4 consentForbidden = PaymentRequestUtils.prepareInternalDomesticConsent(
//                debtorAccountId,
//                ACCOUNT_NUMBER,
//                creditorAccountId,
//                ACCOUNT_IBAN,
//                (BigDecimal.valueOf(AMOUNT_GREATER_THEN_DAILY_UNAUTHORIZED_LIMIT)),
//                "AED",
//                "Api tester - internal " +
//                        "payments",
//                "unstructured");
//
//        THEN("Should get Forbidden as response");
//        domesticTransferApiFlows.createDomesticPaymentConsentErrorResponse(alphaTestUser, consentForbidden, 403);
//
//        //User triggers OTP flow
//        //1) -> initiate step up authentication
//        WHEN("Stepup auth request was initiated");
//        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(32).build());
//        THEN("User will receive OTP");
//
//        //2) -> extract otp from dev simulator
//        //extract otp from device simulator
//        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
//        assertTrue(isNotBlank(otpCO.getPassword()));
//
//        AND("Stepup auth validation will be triggered");
//        final StepUpAuthRequest stepUpAuthValidationRequest =
//                StepUpAuthRequest.builder().otp(otpCO.getPassword()).weight(32).build();
//        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
//        THEN("Biometrics validation should success");
//        AND("Cache entry was successful");
//
//        GIVEN("Cache entry was successful for userId");
//        THEN("Creating consent should be successful");
//        final OBWriteDomesticConsentResponse5 consentResponseSuccess =
//                domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consentForbidden);
//        Assertions.assertTrue(StringUtils.isNotBlank(consentResponseSuccess.getData().getConsentId()));
//        TEST("Trigger domestic transfer payment");
//        WHEN("Sufficient funds available");
//        final OBWriteDomestic2 transferRequest =
//                PaymentRequestUtils.prepareInternalAndDomesticTransferRequest(consentResponseSuccess.getData()
//                .getConsentId(),
//                        debtorAccountId,
//                        ACCOUNT_NUMBER,
//                        creditorAccountId,
//                        ACCOUNT_IBAN,
//                        (BigDecimal.valueOf(AMOUNT_GREATER_THEN_DAILY_UNAUTHORIZED_LIMIT)),
//                        "Api tester - domestic " +
//                                "payments",
//                        "unstructured");
//        final OBWriteDomesticResponse5 transferResponse =
//                domesticTransferApiFlows.createDomesticPayment(alphaTestUser, transferRequest);
//        Assertions.assertNotNull(transferResponse);
//
//        DONE();
//    }
//
//    @Test
//    @Order(220)
//    void user_crud_beneficiary_biometrics_test() throws InterruptedException {
//
//        TEST("AHBDB-305:: Test Beneficiary Management");
//        TEST("AHBDB-370:: Stepup authentication for Beneficiary Management");
//        GIVEN("User has Account scope and a Valid Account");
//        final int loginMinWeightExpected = 31;
//
//        WHEN("the user calls created Beneficiary");
//
//        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
//        beneficiaryData.setAccountNumber(alphaTestUser.getAccountNumber());
//
//        Response response =
//                this.beneficiaryApiFlows.createBeneficiaryFlexResponse(alphaTestUser
//                        , beneficiaryData);
//
//        THEN("Should fail with FORBIDDEN");
//        //TODO enable error code check after updating ERROR lib
//        Assertions.assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN.getCode());
//        final OBErrorResponse1 errorResponse = response.as(OBErrorResponse1.class);
//        Assertions.assertTrue(Objects.nonNull(errorResponse));
//        THEN("Should return error code: " + errorResponse.getCode());
//        //TODO enable error code check after updating ERROR lib
//        //User triggers OTP flow
//        //1) -> initiate step up authentication
//        WHEN("Stepup auth requets was initiated");
//        authenticateApi.stepUpUserAuthInitiate(alphaTestUser,
//                StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
//
//        AND("Stepup auth validation will be triggered");
//        final StepUpAuthRequest stepUpAuthValidationRequest =
//                StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight
//                (loginMinWeightExpected).build();
//        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
//        THEN("Biometrics validation should success");
//        AND("Cache entry was successful");
//
//        GIVEN("Cache entry was successful for userId");
//        THEN("Creating beneficiary should be successful");
//        OBWriteBeneficiaryResponse1 createdBeneficiary =
//                this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser
//                        , beneficiaryData);
//        Assertions.assertNotNull(createdBeneficiary);
//        Assertions.assertEquals(1, createdBeneficiary.getData().getBeneficiary().size());
//        Assertions.assertNotNull(createdBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());
//
//
//        TEST("AHBDB-305:: Test Beneficiary Management - UPDATE");
//        TEST("AHBDB-370:: Stepup authentication for Beneficiary Management - UPDATE");
//        GIVEN("Create beneficiary already created");
//        THEN("Should return FORBIDDEN status code");
//        OBBeneficiary5 obBeneficiary5 = createdBeneficiary.getData().getBeneficiary().get(0);
//        obBeneficiary5.setReference("Test Ref");
//        Response updatedBeneficiaryResponse = this.beneficiaryApiFlows.updateBeneficiaryResponse(alphaTestUser,
//                obBeneficiary5);
//        Assertions.assertTrue(updatedBeneficiaryResponse.getStatusCode() == HttpStatus.FORBIDDEN.getCode());
//        //1) -> initiate step up authentication
//        WHEN("Stepup auth requets was initiated");
//        authenticateApi.stepUpUserAuthInitiate(alphaTestUser,
//                StepUpAuthInitiateRequest.builder().scope("accounts").weight(loginMinWeightExpected).build());
//
//        AND("Stepup auth validation will be triggered");
//        final StepUpAuthRequest updateStepUpAuthValidationRequest =
//                StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).password(alphaTestUser
//                .getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
//        authenticateApi.validateUserStepUpAuth(alphaTestUser, updateStepUpAuthValidationRequest);
//        THEN("OTP validation should success");
//        AND("Cache entry was successful");
//
//        GIVEN("Cache entry was successful for userId");
//        THEN("update beneficiary request should be successful");
//        THEN("can be be updated by the user");
//        OBBeneficiary5 updatedobBeneficiary5 = createdBeneficiary.getData().getBeneficiary().get(0);
//        obBeneficiary5.setReference("Test Ref");
//        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(alphaTestUser,
//                updatedobBeneficiary5);
//        Assertions.assertEquals("Test Ref", updatedBeneficiary.getData().getBeneficiary().get(0).getReference());
//        Assertions.assertEquals(createdBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId(),
//                updatedBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());
//
//        //TOOO::: FAILED
////        THEN("can be be removed");
////        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, obBeneficiary5.getBeneficiaryId(), 204);
////        OBReadBeneficiary5 deletedBeneficiary = this.beneficiaryApiFlows.getBeneficiaries(alphaTestUser);
////        assertNull(deletedBeneficiary.getData().getBeneficiary());
//        DONE();
//    }
//

    @Tag(ACCOUNT_TEST)
    @Test
    @Order(240)
    void get_user_balances_test_success() {
        TEST("AHBDB-5080 - Get account balances positive path");

        WHEN("Calling account balances api");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        THEN("Status code 200 is returned");

        OBReadBalance1Data balanceResponseData = balanceResponse.getData();
        assertNotNull(balanceResponseData);

        List<OBReadBalance1DataBalance> balanceList = balanceResponseData.getBalance();
        assertNotNull(balanceList);

        balanceList.forEach(this::checkFieldAssertionsForGetUserBalancesTestSuccess);

        assertEquals(3, balanceList.size());
        assertEquals(OBBalanceType1Code.INTERIM_AVAILABLE, balanceList.get(0).getType());
        assertEquals(OBBalanceType1Code.INTERIM_BOOKED, balanceList.get(1).getType());
        assertNotNull(balanceList.get(1).getAmount());
        assertNotNull(balanceList.get(1).getAmount().getAmount());
        assertEquals(OBBalanceType1Code.LOCKED_AMOUNT, balanceList.get(2).getType());
        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(241)
    void get_statement_transaction_details_test_success() {
        TEST("AHBDB-7253 get transaction by transaction reference - defect fixed");

        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);

        alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        OBReadTransaction6 obReadTransaction6 = this.accountApi.accountTransactions(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(obReadTransaction6);
        String transactionReference = obReadTransaction6.getData().getTransaction().get(0).getTransactionReference();


        TEST("AHBDB-5389-Get transaction details positive path");

        WHEN("Calling get transaction details api");
        OBReadTransaction6 response = this.accountApi.getAccountStatementTransactions(alphaTestUser,
         transactionReference);
        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("Transaction details has been returned");
        OBReadDataTransaction6 accountTransactions = response.getData();
        assertNotNull(accountTransactions);

        List<OBTransaction6> transactionList = accountTransactions.getTransaction();
        assertNotNull(transactionList);

        OBTransaction6 transaction = transactionList.get(0);
        assertNotNull(transaction);

        assertNotNull(transaction.getTransactionInformation());
        assertNotNull(transaction.getCreditDebitIndicator());
        assertNotNull(transaction.getBookingDateTime());
        assertNotNull(transaction.getTransactionInformation());

        OBActiveOrHistoricCurrencyAndAmount9 transactionAmount = transaction.getAmount();
        assertNotNull(transactionAmount);

        assertNotNull(transactionAmount.getAmount());
        assertNotNull(transactionAmount.getCurrency());
        assertEquals("AED", transactionAmount.getCurrency());

        assertNotNull(transaction.getBankTransactionCode());

        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(242)
    void get_locked_amount_success_test() {

        final int page = 1;
        TEST("AHBDB-5295- Get locked amount positive path");
        WHEN("Calling get locked amount api");
        OBReadTransaction6 lockedAmount = this.accountApi.getLockedAmount(alphaTestUser,
                alphaTestUser.getAccountNumber(), page);
        THEN("Status code 200 is returned");
        assertNotNull(lockedAmount);

        OBReadDataTransaction6 lockedAmountData = lockedAmount.getData();
        assertNotNull(lockedAmountData);
        AND("Locked Amount Data is not null");
        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(243)
    void get_account_transactions_test_success() {
        TEST("AHBDB-12936 - Tests failing");
        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);

        TEST("AHBDB-5389-Get account transactions positive path");
        WHEN("Credit transfer is executed");
        //deposit money into account before trigger transfer account
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getSeedAccountId(), 12, "0");
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, BigDecimal.valueOf(100011));
        WHEN("Calling get account transactions api");
        OBReadTransaction6 response = this.accountApi.getAccountTransactions(alphaTestUser,
                alphaTestUser.getAccountNumber());
        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("Account transactions has been returned");
        OBReadDataTransaction6 accountTransactions = response.getData();
        assertNotNull(accountTransactions);
        AND("Credit transaction record should be returned");
        accountTransactions.getTransaction().stream()
                .forEach(item -> {
                    if (item.getCreditDebitIndicator() == OBCreditDebitCode1.CREDIT) {
                        assertEquals(temenosAccountId, item.getDebtorAccount().getIdentification());
                        assertEquals(alphaTestUser.getAccountNumber(), item.getCreditorAccount().getIdentification());
                    }
                });
        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(244)
    void create_legacy_dtp_payment_protected_test_success() {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.DEV);
        TEST("AHBDB-7118 Create legacy dtp payment happy path");

        final String debtorAccountId = temenosConfig.getSeedAccountId();
        final String creditorAccountId = alphaTestUser.getAccountNumber();

        WHEN("Calling legacy2dtp payment api");
        OBWriteDomesticResponse5 response = paymentProtectedApi.createLegacyDtpPayment(debtorAccountId,
                creditorAccountId,
                BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Payment response has been returned");
        assertCreateLegacyDtpPaymentPayload(response);
        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(245)
    void create_card_payment_protected_test_success() {
        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);

        TEST("AHBDB-1338 Create card payment happy path");

        final String debtorAccountId = alphaTestUser.getAccountNumber();

        final BigDecimal PAYMENT_AMOUNT = BigDecimal.TEN;
        WHEN("Calling card payment api");
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
         PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        OBWriteCardPaymentResponse1 response = cardProtectedApi.createCardPayment(debtorAccountId, PAYMENT_AMOUNT);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Card payment response has been returned");
        assertCreateCardPaymentPayload(response);

        //TODO assert the transaction shows for the user
        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(246)
    void create_card_deposit_protected_test_success() {
        TEST("AHBDB-1338 Create card deposit happy path");

        final String debtorAccountId = alphaTestUser.getAccountNumber();
        final String creditorAccountId = temenosConfig.getCreditorAccountId();

        final BigDecimal PAYMENT_AMOUNT = BigDecimal.TEN;
        WHEN("Calling card deposit api");
        OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(debtorAccountId,
                creditorAccountId,
                PAYMENT_AMOUNT);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Card deposit response has been returned");
        assertCreateCardDepositPayload(response);

        //TODO assert the transaction shows for the user

        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(247)
    void create_card_withdrawal_protected_test_success() {
        TEST("AHBDB-12936 - Tests failing");
        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);

        TEST("AHBDB-1338 Create card withdrawal happy path");

        final String debtorAccountId = alphaTestUser.getAccountNumber();
        final String creditorAccountId = temenosConfig.getCreditorAccountId();

        BigDecimal PAYMENT_AMOUNT = BigDecimal.TEN;
        WHEN("Calling card withdrawal api");
        Assume.assumeTrue(PAYMENTS_ASSUMPTION_MSG, transferUtils.canProceedTestWithBalance(alphaTestUser,
                PAYMENT_AMOUNT, Lists.newArrayList(EQUAL_TO, LESS_THEN), alphaTestUser.getAccountNumber(),
                OBBalanceType1Code.INTERIM_BOOKED));
        OBWriteCardWithdrawalResponse1 response = cardProtectedApi.createCardWithdrawal(debtorAccountId,
                creditorAccountId,
                PAYMENT_AMOUNT);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Card withdrawal response has been returned");
        assertCreateCardWithdrawalPayload(response);

        //TODO assert the transaction shows for the user

        DONE();
    }

    @Tag(BENEFICIARY_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(251)
    void user_crud_beneficiary_otp_test() throws InterruptedException {

        TEST("AHBDB-305:: Test Beneficiary Management");
        TEST("AHBDB-370:: Stepup authentication for Beneficiary Management");
        GIVEN("User has Account scope and a Valid Account");
        final int otpWeightRequested = 32;

        WHEN("the user calls created Beneficiary");

        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        Response response =
                this.beneficiaryApiFlows.createBeneficiaryFlexResponse(alphaTestUser
                        , beneficiaryData);


        THEN("Should fail with FORBIDDEN");
        response.then().log().all().statusCode(HttpStatus.FORBIDDEN.getCode());
        final OBErrorResponse1 errorResponse = response.as(OBErrorResponse1.class);
        Assertions.assertTrue(Objects.nonNull(errorResponse));
        THEN("Should return error code: " + errorResponse.getCode());
        //TODO enable error code check after updating ERROR lib
        //User triggers OTP flow
        //1) -> initiate step up authentication
        WHEN("Stepup auth requets was initiated");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser,
                StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        THEN("User will receive OTP");

        //2) -> extract otp from dev simulator
        //extract otp from device simulator
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(isNotBlank(otpCO.getPassword()));

        AND("Stepup auth validation will be triggered");
        final StepUpAuthRequest stepUpAuthValidationRequest =
                StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
        THEN("Biometrics validation should success");
        AND("Cache entry was successful");

        GIVEN("Cache entry was successful for userId");
        THEN("Creating beneficiary should be successful");
        OBWriteBeneficiaryResponse1 createdBeneficiary =
                this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser
                        , beneficiaryData);
        assertNotNull(createdBeneficiary);
        Assertions.assertEquals(1, createdBeneficiary.getData().getBeneficiary().size());
        assertNotNull(createdBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());


        TEST("AHBDB-305:: Test Beneficiary Management - UPDATE");
        TEST("AHBDB-370:: Stepup authentication for Beneficiary Management - UPDATE");
        GIVEN("Create beneficiary already created");
        THEN("Should return FORBIDDEN status code");
        OBBeneficiary5 obBeneficiary5 = createdBeneficiary.getData().getBeneficiary().get(0);
        obBeneficiary5.setReference("Test Ref");
        Response updatedBeneficiaryResponse = this.beneficiaryApiFlows.updateBeneficiaryResponse(alphaTestUser,
                obBeneficiary5);
        Assertions.assertTrue(updatedBeneficiaryResponse.getStatusCode() == HttpStatus.FORBIDDEN.getCode());
        //1) -> initiate step up authentication
        WHEN("Stepup auth requets was initiated");
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser,
                StepUpAuthInitiateRequest.builder().scope("accounts").weight(otpWeightRequested).build());
        THEN("User will receive OTP");

        //2) -> extract otp from dev simulator
        //extract otp from device simulator
        OtpCO updateOtpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(isNotBlank(updateOtpCO.getPassword()));

        AND("Stepup auth validation will be triggered");
        final StepUpAuthRequest updateStepUpAuthValidationRequest =
                StepUpAuthRequest.builder().otp(updateOtpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, updateStepUpAuthValidationRequest);
        THEN("OTP validation should success");
        AND("Cache entry was successful");

        GIVEN("Cache entry was successful for userId");
        THEN("update beneficiary request should be successful");
        THEN("can be be updated by the user");
        OBBeneficiary5 updatedobBeneficiary5 = createdBeneficiary.getData().getBeneficiary().get(0);
        obBeneficiary5.setReference("Test Ref");
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(alphaTestUser,
                updatedobBeneficiary5);
        Assertions.assertEquals("Test Ref", updatedBeneficiary.getData().getBeneficiary().get(0).getReference());
        Assertions.assertEquals(createdBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId(),
                updatedBeneficiary.getData().getBeneficiary().get(0).getBeneficiaryId());

        //TOOO::: FAILED
//        THEN("can be be removed");
//        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, obBeneficiary5.getBeneficiaryId(), 204);
//        OBReadBeneficiary5 deletedBeneficiary = this.beneficiaryApiFlows.getBeneficiaries(alphaTestUser);
//        assertNull(deletedBeneficiary.getData().getBeneficiary());
        DONE();
    }


    //card tests should follow customer creation
    @Tag(CARD_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(400)
    void test_create_virtual_debit_card() {

        // refresh token for cards
        alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        TEST("AHBDB-288 - Create virtual debit card");

        GIVEN("A user have registered with HPS Account");
        CreateCard1 createCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUser.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(alphaTestUser.getAccountNumber())
                                .accountType(CardsApiFlows.ACCOUNT_TYPE)
                                .openDate(LocalDateTime.parse("2011-12-03T10:15:30",
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();
        //TODO remove hardcode values upon HPS end to end integration
        final CreateCard1Response createCardResponse = cardsApiFlows.createVirtualDebitCard(alphaTestUser, createCard1);
        THEN("New virtual debit  card should be  created");

        Assertions.assertTrue(!Objects.isNull(createCardResponse));
        Assertions.assertTrue(StringUtils.isNotBlank(createCardResponse.getData().getCardNumber()));

        String createdCardNumber = createCardResponse.getData().getCardNumber();
        ActivateCard1 validActivateCard1 = ActivateCard1.builder()
                .cardNumber(createdCardNumber)
                .lastFourDigits(createdCardNumber.substring(createdCardNumber.length() - 4))
                .cardNumberFlag("M")
                .cardExpiryDate(createCardResponse.getData().getExpiryDate())
                .modificationOperation(ModificationOperation.V)
                .operationReason("Operation reason")
                .build();

        cardsApiFlows.activateDebitCard(alphaTestUser, validActivateCard1, 200);

        AND("Card is activated");
    }

    @Tag(CARD_TEST)
    @Tag(SMOKE_TEST)
    @Test
    @Order(401)
    void test_fetch_cards() {
        TEST("AHBDB-297 - Fetch card details");

        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
    }

    @Tag(CARD_TEST)
    @Test
    @Order(402)
    void test_fetch_card_cvv_details() {
        TEST("AHBDB-297 - Fetch card details");

        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        TEST("AHBDB-3414 when cards available for user");
        WHEN("valid cardId ( first 6 digits of card + last four digits of card is prepared)");
        THEN("Should be able to retrieve card cvv details ");
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(!Objects.isNull(cardCVvDetails.getData()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
    }

    @Tag(CARD_TEST)

    @Test
    @Order(403)
    void test_issue_physical_card_and_activate_card() throws Throwable {
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(!Objects.isNull(cardCVvDetails.getData()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getExpiryDate()));
        GIVEN("User have a valid card");
        THEN("User should be able to request physical card");

        OBReadAccount6 response = this.accountApi.getAccountsV2(alphaTestUser);
        THEN("Status code 200(User Accounts) is returned");
        AND("User Accounts has been returned");
        OBReadAccount6Data data = response.getData();
        assertNotNull(data);

        List<OBAccount6> accountList = response.getData().getAccount();
        OBAccount6 account = accountList.get(0);
        assertNotNull(account);
        List<OBAccount4Account> accounts = account.getAccount();
        assertNotNull(accounts);
        assertEquals(3, accounts.size());
        OBAccount4Account accountDetails = accounts.get(0);
        assertNotNull(accountDetails.getSchemeName());
        assertEquals("IBAN.NUMBER", accountDetails.getSchemeName());
        assertNotNull(accountDetails.getIdentification());
        final WritePhysicalCard1 issuePhysicalCard = WritePhysicalCard1.builder()
                .awbRef("awbRef")
                .dtpReference("DTP" + RandomStringUtils.randomAlphanumeric(7))
                .iban(accountDetails.getIdentification())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .recipientName(alphaTestUser.getName())
                .deliveryAddress(CardDeliveryAddress1.builder()
                        .buildingNumber("123")
                        .country("UAE")
                        .postalCode("wer234")
                        .addressLine(Lists.newArrayList("addressline 1"))
                        .streetName("valley road")
                        .townName("Dubai")
                        .build())
                .build();
        final String plainCardNumber = tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber());
        THEN("Should be able to decrypt cardNumber with 3Des algorithm  :" + plainCardNumber);
        cardsApiFlows.issuePhysicalCard(alphaTestUser, cardId, issuePhysicalCard);
        GIVEN("Physical card has been issued successful");
        THEN("Should be able to activate the card");
        final ActivateCard1 activateCard1 = ActivateCard1.builder()
                .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                .cardNumberFlag("P")
                .cardNumber(plainCardNumber)
                .lastFourDigits(StringUtils.right(cardNumber, 4))
                .modificationOperation(ModificationOperation.A)
                .operationReason("TEST : activate card")
                .build();
        cardsApiFlows.activateDebitCard(alphaTestUser, activateCard1, 200);
    }


    @Tag(CARD_TEST)
    @Test
    @Order(404)
    void test_set_card_pin_and_validate() throws Throwable {
        TEST("AHBDB-12934- Test Failing");

        GIVEN("User have a valid and activated card");
        THEN("User should be able to set pin");

        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(!Objects.isNull(cardCVvDetails.getData()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getExpiryDate()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876",
                tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        final WriteCardPinRequest1 pinSetRequest = WriteCardPinRequest1.builder()
                .cardExpiryDate(cardCVvDetails.getData().getExpiryDate())
                .cardNumber(cardNumber)
                .cardNumberFlag("M")
                .lastFourDigits(StringUtils.right(cardNumber, 4))
                .pinServiceType("C")
                .pinBlock(pinBlock)
                .build();
        THEN("Setting pin is success");
        cardsApiFlows.setDebitCardPin(alphaTestUser, pinSetRequest, 200);
        AND("Should be able to validate the pin");
        final CardPinValidation1 cardPinValidation1 = CardPinValidation1.builder()
                .cardPinValidation1Data(CardPinValidation1Data.builder()
                        .pin(pinBlock)
                        .build())
                .build();
        cardsApiFlows.validateDebitCardPin(alphaTestUser, cardId, cardPinValidation1);
        DONE();
    }


    @Test
    @Order(405)
    void test_fetch_card_transaction_limits_for_spending_type() {

        TEST("AHBDB-3297 - Fetch card limits for withdrawal transaction type");

        GIVEN("A user have registered with HPS Account");
        WHEN("User have a registered card");
        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardLimits1 cardLimits = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser,
                TransactionType.WITHDRAWAL.getLabel(), cardId);
        THEN("Should return withdrawal limits for provided card");
        Assertions.assertTrue(!Objects.isNull(cardLimits));
        Assertions.assertTrue(StringUtils.isNotBlank(cardLimits.getData().getLimits().getDailyTotalAmount()));
    }

    @Tag(CARD_TEST)
    @Test
    @Order(406)
    void test_fetch_card_transaction_limits_for_purchase_type() {
        TEST("AHBDB-3297 - Fetch card limits for purchase transaction type");

        GIVEN("A user have registered with HPS Account");
        WHEN("User have a registered card");
        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        WHEN("User have more than one card listed at ESB / HPS ");
        THEN("Should return list of cards ");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardLimits1 cardLimits = cardsApiFlows.fetchCardLimitsForTransactionType(alphaTestUser,
                TransactionType.PURCHASE.getLabel(), cardId);
        THEN("Should return purchase limits for provided card");
        Assertions.assertTrue(!Objects.isNull(cardLimits));
        Assertions.assertTrue(StringUtils.isNotBlank(cardLimits.getData().getLimits().getDailyTotalAmount()));
    }

    @Tag(CARD_TEST)
    @Test
    @Order(405)
    void test_update_card_update_and_fetch_parameters_request() {
        TEST("AHBDB-295 - Block user debit card");

        GIVEN("A user have registered with HPS Account");
        AND("New virtual debit card has been created");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);

        AND("Block debit card request was fired");
        UpdateCardParameters1 updateCardParameters1 = UpdateCardParameters1.builder()
                .cardNumber(cards.getData().getReadCard1DataCard().get(0).getCardNumber())
                .cardNumberFlag("M")
                .nationalUsage(false)
                .nationalDisATM(false)
                .internationalUsage(false)
                .internetUsage(false)
                .build();
        THEN("Response should be success");
        cardsApiFlows.updateCardParameters(alphaTestUser, updateCardParameters1, 200);

        AND("Card parameters response should be updated");
        final String cardId =
                StringUtils.left(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), 6).concat(StringUtils.right(cards.getData().getReadCard1DataCard().get(0).getCardNumber(), 4));
        final ReadCardParameters1 cardFilter1 = cardsApiFlows.fetchCardFilters(alphaTestUser, cardId);

        Assertions.assertTrue(cardFilter1.getData().getInternetUsage().equalsIgnoreCase("N"));
        Assertions.assertTrue(cardFilter1.getData().getNationalUsage().equalsIgnoreCase("N"));
        Assertions.assertTrue(cardFilter1.getData().getNationalPOS().equalsIgnoreCase("N"));
        Assertions.assertTrue(cardFilter1.getData().getInternetUsage().equalsIgnoreCase("N"));
        Assertions.assertTrue(cardFilter1.getData().getNationalHilalATM().equalsIgnoreCase("N"));
    }

    @Tag(CARD_TEST)
    @Test
    @Order(410)
    void test_update_card_transaction_limits() {
        TEST("AHBDB-4953 - Update card limits");

        GIVEN("A user have registered with HPS Account");
        WHEN("User have a registered card");
        GIVEN("A user have registered with HPS and have cards associated");
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        WHEN("Card limits update requested");
        THEN("Should return success response");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        //TODO remove hardcode values upon HPS end to end integration
        final WriteDailyMonthlyLimits1 cardLimits =
                WriteDailyMonthlyLimits1.builder().data(WriteDailyMonthlyLimits1Data.builder().cardNumber(cardNumber).cardNumberFlag("M").dailyAtmLimit("100").build()).build();
        THEN("Request should be successful");
        cardsApiFlows.updateCardLimits(alphaTestUser, cardLimits, HttpStatus.OK.getCode());
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(440)
    void create_domestic_incoming_payment_protected_test_success() {
        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);

        TEST("AHBDB-7120 Create domestic incoming transfer");

        GIVEN("Domestic request with valid scheme");
        WriteDomesticPayment1 request = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                RandomStringUtils.randomAlphanumeric(11),
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should return success response");
        OBWriteDomesticResponse5 domesticResponse = protectedDomesticTransferApiFlows.createDomesticPayment(request);
        checkDomesticIncomingPaymentAssertions(domesticResponse);
        DONE();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(441)
    void update_domestic_incoming_payment_protected_test_success() {
        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);

        TEST("AHBDB-7120 Update domestic incoming transfer");

        GIVEN("Domestic request with valid scheme");
        WriteDomesticPayment1 request = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                RandomStringUtils.randomAlphanumeric(11),
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should create domestic payment and returns paymentId");
        OBWriteDomesticResponse5 response = protectedDomesticTransferApiFlows.createDomesticPayment(request);

        AND("Authorize created domestic payment");
        GIVEN("Domestic auth request with valid scheme and payment id");
        String domesticPaymentId = response.getData().getDomesticPaymentId();
        WriteDomesticPayment1 authRequest = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                domesticPaymentId,
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should authorize domestic payment");
        OBWriteDomesticResponse5 authResponse = protectedDomesticTransferApiFlows.updateDomesticPayment(authRequest);
        checkDomesticIncomingPaymentAssertions(authResponse);

        DONE();
    }


    @Tag(PAYMENT_TEST)
    @Test
    @Order(442)
    void delete_domestic_incoming_payment_protected_test_success() {
        envUtils.ignoreTestInEnv("AHBDB-12264- Transact timeout", Environments.ALL);
        envUtils.ignoreTestInEnv(Environments.DEV, Environments.CIT);

        TEST("AHBDB-7120 Delete domestic incoming transfer");

        GIVEN("Domestic request with valid scheme");
        WriteDomesticPayment1 request = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                RandomStringUtils.randomAlphanumeric(11),
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should create domestic payment and returns paymentId");
        OBWriteDomesticResponse5 response = protectedDomesticTransferApiFlows.createDomesticPayment(request);

        AND("Delete created domestic payment");
        GIVEN("Domestic auth request with valid scheme and payment id");
        String domesticPaymentId = response.getData().getDomesticPaymentId();
        WriteDomesticPayment1 deleteRequest = DomesticPaymentRequestUtils.prepareDomesticRequest(
                temenosConfig.getSeedAccountId(),
                temenosConfig.getCreditorIban(),
                "DomesticIncoming",
                domesticPaymentId,
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should delete domestic payment");
        OBWriteDomesticResponse5 deleteResponse =
                protectedDomesticTransferApiFlows.deleteDomesticPayment(deleteRequest);
        checkDomesticIncomingPaymentAssertions(deleteResponse);

        DONE();
    }

    @Test
    @Order(443)
    void test_forgot_password_and_card_pin_activation_flow() throws Throwable {

        TEST("AHBDB-1594 - Forgot passcode flow");
        GIVEN("Customer exists and has a bank account");
        // refresh token for re register device and forgot password
        alphaTestUserFactory.refreshAccessToken(alphaTestUser);

        //Fetch cards to generate encrypted pin
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876",
                tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));

        WHEN("I generate the payload");
        CardPinValidationRequest request = CardPinValidationRequest.builder().pin(pinBlock).build();

        AND("I then send a request with a payload certificate can be validated");
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, ob.writeValueAsString(request));

        this.certificateApi.validateCertificate(alphaTestUser,
                ob.writeValueAsString(request),
                signedSignature,
                204);

        WHEN("A customer forgot it's passcode and he initiates the forgot passcode flow using a valid phone number");
        ResetPasswordResponse resetPasswordResponse = authenticateApi.initiateResetPassword(alphaTestUser,
                ResetPasswordRequest.builder().phoneNumber(alphaTestUser.getUserTelephone()).build());
        THEN("Expect an hash to be returned");
        assertTrue(StringUtils.isNotBlank(resetPasswordResponse.getHash()));

        AND("An otp is generated for the user");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(resetPasswordResponse.getHash());
        assertTrue(StringUtils.isNotBlank(otpCO.getPassword()));

        WHEN("A customer received an SMS with the OTP and he enters it");
        ValidateResetPasswordRequest validateResetPasswordRequest = ValidateResetPasswordRequest.builder()
                .hash(resetPasswordResponse.getHash())
                .otp(otpCO.getPassword())
                .build();
        ValidateResetPasswordResponse validateResetPasswordResponse =
                authenticateApi.validateResetPasswordOtp(alphaTestUser, validateResetPasswordRequest);
        THEN("Expect otp to be validated and new hash to be returned");
        assertTrue(StringUtils.isNotBlank(validateResetPasswordResponse.getHash()));

        WHEN("A customer verified the otp and enter a new passcode");
        String newPassword = UUID.randomUUID().toString();
        UpdateForgottenPasswordRequestV1 updateForgottenPasswordRequest = UpdateForgottenPasswordRequestV1.builder()
                .hash(validateResetPasswordResponse.getHash())
                .userPassword(newPassword)
                .build();
        UserDto userDto = authenticateApi.updateForgottenPassword(alphaTestUser, updateForgottenPasswordRequest);
        THEN("The user's password was update and he can login with the new credentials");
        assertNotNull(userDto);
        alphaTestUser.setUserPassword(newPassword);
        UserLoginResponseV2 loginResponse = authenticateApi.loginUser(alphaTestUser);
        assertNotNull(loginResponse);
        assertTrue(StringUtils.isNotBlank(loginResponse.getAccessToken()));

        parseLoginResponse(alphaTestUser, loginResponse);

        TEST("AHBDB-9233 - card pin validation");
        authenticateApi.cardPinValidation(alphaTestUser, request, cardId, signedSignature);
        THEN("Card pin validation successfully");

        loginResponse = authenticateApi.loginUser(alphaTestUser);
        assertNotNull(loginResponse);
        assertTrue(StringUtils.isNotBlank(loginResponse.getAccessToken()));

        parseLoginResponse(alphaTestUser, loginResponse);
        DONE();
    }

    @Test
    @Order(502)
    void test_re_register_device_flow() throws Throwable {
        TEST("AHBDB-7463 Re-register device flow");
        GIVEN("Client is onboarded with ACCOUNTS CUSTOMER Scope");
        WHEN("Client tries register with new device");
        alphaTestUser.setPreviousDeviceId(alphaTestUser.getDeviceId());
        alphaTestUser.setPreviousDeviceHash(alphaTestUser.getDeviceHash());
        alphaTestUser.setPreviousPrivateKeyBase64(alphaTestUser.getPrivateKeyBase64());
        alphaTestUser.setPreviousPublicKeyBase64(alphaTestUser.getPublicKeyBase64());
        alphaTestUser.setDeviceId(UUID.randomUUID().toString());
        alphaTestUser.setDeviceHash(UUID.randomUUID().toString());
        WHEN("User uploads device certificate");

        //Fetch cards to generate pin
        final ReadCard1 cards = cardsApiFlows.fetchCardsForUser(alphaTestUser);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 0);
        final String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        final ReadCardCvv1 cardCVvDetails = cardsApiFlows.fetchCardsCvvForUser(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        final String pinBlock = tripleDesUtil.encryptUserPin("9876",
                tripleDesUtil.decryptUserCardNumber(cardCVvDetails.getData().getCardNumber()));
        CardPinValidationRequest request = CardPinValidationRequest.builder().pin(pinBlock).build();

        TEST("AHBDB-7463 - re register device adult banking user");

        WHEN("The client the client request a registration of a new device");
        LoginResponseV1 loginResponse = this.authenticateApi.registerNewDevice(alphaTestUser);
        THEN("Status code 200(OK) is returned with device scope");
        assertEquals(loginResponse.getScope(), "device");

        parseLoginResponse(alphaTestUser, loginResponse);

        GIVEN("User has a valid access token and wants to validate their telephone number");
        WHEN("The client the client request a otp : " + alphaTestUser.getUserTelephone());
        otpApi.sendDestinationToOTP(alphaTestUser, 204);
        THEN("The client receives a 204 response code");
        TEST("Dev can obtain the otp via the userId");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertNotNull(otpCO);
        final String otp = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otp);
        assertNotNull(otp);
        TEST("Client can validate the OTP");
        otpApi.postOTPCode(alphaTestUser, 200, otp);

        AND("I then send a request with a payload certificate can be validated");
        UserLoginRequestV2 userLoginRequestV2 = UserLoginRequestV2.builder()
                .userId(alphaTestUser.getUserId())
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser,
                ob.writeValueAsString(userLoginRequestV2));

        this.certificateApi.uploadCertificate(alphaTestUser);

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                alphaTestUser.getDeviceId(),
                ob.writeValueAsString(userLoginRequestV2),
                signedSignature,
                204);

        WHEN("Client tries to re-authenticate in order to upgrade the scope");
        assertNotNull(alphaTestUser.getUserId());
        UserLoginResponseV2 userLoginResponseV2 = authenticateApi.loginUser(alphaTestUser, userLoginRequestV2,
                alphaTestUser.getDeviceId(), false);
        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is ACCOUNTS-LIMITED");
        Assertions.assertEquals(ACCOUNTS_LIMITED_SCOPE, userLoginResponseV2.getScope());
        parseLoginResponse(alphaTestUser, userLoginResponseV2);

        authenticateApi.cardPinValidation(alphaTestUser, request, cardId, signedSignature);
        THEN("Card pin validation successfully");

        UserLoginResponseV2 loginResponseV2 = authenticateApi.loginUser(alphaTestUser);
        assertNotNull(loginResponseV2);
        assertTrue(StringUtils.isNotBlank(loginResponse.getAccessToken()));
        parseLoginResponse(alphaTestUser, loginResponseV2);
        DONE();
    }

    private void checkDomesticIncomingPaymentAssertions(OBWriteDomesticResponse5 domesticResponse) {
        Assertions.assertNotNull(domesticResponse);
        Assertions.assertNotNull(domesticResponse.getData().getInitiation());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getSupplementaryData());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getInstructionIdentification());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getEndToEndIdentification());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getInstructedAmount());

        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getInstructedAmount().getAmount());
        //TODO uncomment this after pacs domestic solution
        //Assertions.assertNotNull(domesticResponse.getData().getInitiation().getDebtorAccount());
        //Assertions.assertNotNull(domesticResponse.getData().getInitiation().getDebtorAccount().getName());
        //Assertions.assertNotNull(domesticResponse.getData().getInitiation().getDebtorAccount().getIdentification());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getCreditorAccount());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getCreditorAccount().getName());
        Assertions.assertNotNull(domesticResponse.getData().getInitiation().getCreditorAccount().getIdentification());

        Assertions.assertNotNull(domesticResponse.getData().getStatus());
        Assertions.assertNotNull(domesticResponse.getData().getCreationDateTime());
        Assertions.assertNotNull(domesticResponse.getData().getStatusUpdateDateTime());
        Assertions.assertNotNull(domesticResponse.getData().getExpectedExecutionDateTime());
        Assertions.assertNotNull(domesticResponse.getData().getExpectedSettlementDateTime());
    }

    private void checkFieldAssertionsForGetUserBalancesTestSuccess(OBReadBalance1DataBalance balance) {
        assertNotNull(balance);

        assertNotNull(balance.getAccountId());

        assertNotNull(balance.getType());

        OBReadBalance1DataAmount balanceAmount = balance.getAmount();
        assertNotNull(balanceAmount);

        assertNotNull(balanceAmount.getCurrency());

        assertEquals("AED", balanceAmount.getCurrency());
    }


    private void assertCreateLegacyDtpPaymentPayload(OBWriteDomesticResponse5 response) {
        OBWriteDomesticResponse5Data data = response.getData();
        assertNotNull(data);

        assertEquals(data.getStatus(), OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);
        assertNotNull(data.getStatusUpdateDateTime());
        assertNotNull(data.getCreationDateTime());
        assertNotNull(data.getExpectedExecutionDateTime());
        assertNotNull(data.getExpectedSettlementDateTime());

        OBWriteDomestic2DataInitiation initiation = data.getInitiation();
        assertNotNull(initiation);

        assertNotNull(initiation.getInstructionIdentification());
        assertNotNull(initiation.getEndToEndIdentification());

        assertNotNull(initiation.getInstructedAmount());
        assertNotNull(initiation.getInstructedAmount().getAmount());
        assertNotNull(initiation.getInstructedAmount().getCurrency());

        assertNotNull(initiation.getCreditorAccount());
        assertNotNull(initiation.getCreditorAccount().getIdentification());
        assertNotNull(initiation.getCreditorAccount().getName());

        assertNotNull(initiation.getDebtorAccount());
        assertNotNull(initiation.getDebtorAccount().getIdentification());
        assertNotNull(initiation.getDebtorAccount().getName());

        assertNotNull(initiation.getRemittanceInformation());
        assertNotNull(initiation.getRemittanceInformation().getUnstructured());
    }

    private void assertCreateCardPaymentPayload(OBWriteCardPaymentResponse1 response) {
        assertNotNull(response.getData());
        assertNotNull(response.getData().getCardPaymentId());
        assertNotNull(response.getData().getCreationDateTime());
        assertNotNull(response.getData().getStatusUpdateDateTime());

        assertNotNull(response.getData().getInitiation());
        assertNotNull(response.getData().getInitiation().getInstructedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getLockedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getCurrency());

        assertNotNull(response.getData().getInitiation().getDebtorAccount());
        assertNotNull(response.getData().getInitiation().getDebtorAccount().getIdentification());

        assertNotNull(response.getSupplementaryData());
        assertNotNull(response.getSupplementaryData().getAtUniqueId());
        assertNotNull(response.getSupplementaryData().getCardNumber());
        assertNotNull(response.getSupplementaryData().getAtLocalDate());
        assertNotNull(response.getSupplementaryData().getAtmTermRef());
        assertNotNull(response.getSupplementaryData().getRetrievalReference());
        assertNotNull(response.getSupplementaryData().getAtTransDate());
        assertNotNull(response.getSupplementaryData().getTerminalId());
        assertNotNull(response.getSupplementaryData().getAtTraceNo());
        assertNotNull(response.getSupplementaryData().getAtTxnAmount());
        assertNotNull(response.getSupplementaryData().getAtProcCode());
        assertNotNull(response.getSupplementaryData().getMerchantLoc());
        assertNotNull(response.getSupplementaryData().getTxnAmount());
        assertNotNull(response.getSupplementaryData().getTxnCurrency());
        assertNotNull(response.getSupplementaryData().getAtAuthCode());
    }

    private void assertCreateCardDepositPayload(OBWriteCardDepositResponse1 response) {
        assertNotNull(response.getData());
        assertNotNull(response.getData().getCardPaymentId());
        assertNotNull(response.getData().getCreationDateTime());
        assertNotNull(response.getData().getStatusUpdateDateTime());

        assertNotNull(response.getData().getInitiation());
        assertNotNull(response.getData().getInitiation().getInstructedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getCurrency());

        assertNotNull(response.getData().getInitiation().getCreditorAccount());
        assertNotNull(response.getData().getInitiation().getCreditorAccount().getIdentification());

        assertNotNull(response.getSupplementaryData());
        assertNotNull(response.getSupplementaryData().getAtUniqueId());
        assertNotNull(response.getSupplementaryData().getCardNumber());
        assertNotNull(response.getSupplementaryData().getAtLocalDate());
        assertNotNull(response.getSupplementaryData().getRetrievalReference());
        assertNotNull(response.getSupplementaryData().getAtTransDate());
        assertNotNull(response.getSupplementaryData().getTerminalId());
        assertNotNull(response.getSupplementaryData().getAtTraceNo());
        assertNotNull(response.getSupplementaryData().getAtTxnAmount());
        assertNotNull(response.getSupplementaryData().getAtProcCode());
        assertNotNull(response.getSupplementaryData().getMerchantLoc());
        assertNotNull(response.getSupplementaryData().getTxnAmount());
        assertNotNull(response.getSupplementaryData().getTxnCurrency());
        assertNotNull(response.getSupplementaryData().getAtAuthCode());
    }

    private void assertCreateCardWithdrawalPayload(OBWriteCardWithdrawalResponse1 response) {
        assertNotNull(response.getData());
        assertNotNull(response.getData().getCardPaymentId());
        assertNotNull(response.getData().getCreationDateTime());
        assertNotNull(response.getData().getStatusUpdateDateTime());

        assertNotNull(response.getData().getInitiation());
        assertNotNull(response.getData().getInitiation().getInstructedAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getAmount());
        assertNotNull(response.getData().getInitiation().getInstructedAmount().getCurrency());

        assertNotNull(response.getData().getInitiation().getDebtorAccount());
        assertNotNull(response.getData().getInitiation().getDebtorAccount().getIdentification());

        assertNotNull(response.getSupplementaryData());
        assertNotNull(response.getSupplementaryData().getAtUniqueId());
        assertNotNull(response.getSupplementaryData().getCardNumber());
        assertNotNull(response.getSupplementaryData().getAtLocalDate());
        assertNotNull(response.getSupplementaryData().getRetrievalReference());
        assertNotNull(response.getSupplementaryData().getAtTransDate());
        assertNotNull(response.getSupplementaryData().getTerminalId());
        assertNotNull(response.getSupplementaryData().getAtTraceNo());
        assertNotNull(response.getSupplementaryData().getAtTxnAmount());
        assertNotNull(response.getSupplementaryData().getAtProcCode());
        assertNotNull(response.getSupplementaryData().getMerchantLoc());
        assertNotNull(response.getSupplementaryData().getTxnAmount());
        assertNotNull(response.getSupplementaryData().getTxnCurrency());
        assertNotNull(response.getSupplementaryData().getAtAuthCode());
    }

}
