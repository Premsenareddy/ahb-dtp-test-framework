package uk.co.deloitte.banking.journey.scenarios.adult;


import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.util.DomesticPaymentRequestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_CREDITOR;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdultBankingCustomerScenarioThin extends AdultOnBoardingBase {
    public static final String CUSTOMER_TEST = "customer";
    public static final String SMOKE_TEST = "smoke";
    public static final String ACCOUNT_TEST = "account";
    public static final String PAYMENT_TEST = "payment";
    public static final String DOMESTIC_INCOMING_CRED_IBAN = "AE050530000013465753001";

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
        //SAVINGS
        this.create_account();
    }

    @Tag(ACCOUNT_TEST)
    @Test
    @Order(11)
    void create_current_account_test() {
        //CURRENT
        this.create_current_account();
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(12)
    void atm_deposit_savings() {
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(100000));
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(13)
    void atm_deposit_current() {
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getCurrentAccountNumber(), BigDecimal.valueOf(100000));
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(14)
    void atm_withdrawal_gcc_savings() {
        //AtmFee D00000600
        // "AtmTermRef": "UAHB",
        this.cardProtectedApi.createCardWithdrawalWebhooks(alphaTestUser.getAccountNumber(), new BigDecimal("200.00"), "D00000600", "UAHB");
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(15)
    void atm_withdrawal_gcc_current() {
        //AtmFee D00000600
        // "AtmTermRef": "UAHB",
        this.cardProtectedApi.createCardWithdrawalWebhooks(alphaTestUser.getCurrentAccountNumber(), new BigDecimal("100.00"), "D00000600", "UAHB");
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(16)
    void atm_withdrawal_savings() {
        this.cardProtectedApi.createCardWithdrawalWebhooks(alphaTestUser.getAccountNumber(),  new BigDecimal("100.00"), null, "AHBD");
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(17)
    void atm_withdrawal_savings_current() {
        this.cardProtectedApi.createCardWithdrawalWebhooks(alphaTestUser.getCurrentAccountNumber(),  new BigDecimal("200.00"), null, "AHBD");
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(18)
    void atm_withdrawal_non_AHB() {
        //AtmFee D00000200
        // "AtmTermRef": "UAHB",
        this.cardProtectedApi.createCardWithdrawalWebhooks(alphaTestUser.getAccountNumber(), new BigDecimal("100.00"), "D00000200", "UAHB");
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(19)
    void atm_withdrawal_non_AHB_current() {
        //AtmFee D00000200
        // "AtmTermRef": "UAHB",
        this.cardProtectedApi.createCardWithdrawalWebhooks(alphaTestUser.getCurrentAccountNumber(), new BigDecimal("102.00"), "D00000200", "UAHB");
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(20)
    void card_payment_savings() {
        this.cardProtectedApi.createCardPayment(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(100));
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(21)
    void card_payment_current() {
        this.cardProtectedApi.createCardPayment(alphaTestUser.getCurrentAccountNumber(), BigDecimal.valueOf(100));
    }


    @Tag(PAYMENT_TEST)
    @Test
    @Order(22)
    void domestic_payment_domestic_outgoing_savings() {
        domesticOutgoing(alphaTestUser.getAccountNumber());
    }
//
    @Tag(PAYMENT_TEST)
    @Test
    @Order(23)
    void domestic_payment_domestic_outgoing_current() {
        domesticOutgoing(alphaTestUser.getCurrentAccountNumber());
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(24)
    void domestic_payment_dtp_legacy_savings() {
        dtp2Legacy(alphaTestUser.getAccountNumber());

    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(25)
    void domestic_payment_dtp_legacy_current() {
        dtp2Legacy(alphaTestUser.getCurrentAccountNumber());
    }

    @Test
    @Order(26)
    void create_domestic_incoming_payment_protected_test_success_savings() {

        domesticIncoming(alphaTestUser.getAccountNumber());
    }

    @Test
    @Order(27)
    void create_domestic_incoming_payment_protected_test_success_current() {
        domesticIncoming(alphaTestUser.getCurrentAccountNumber());
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(28)
    void domestic_payment_legacy_dtp() {
        OBWriteDomesticResponse5 response = paymentProtectedApi.createLegacyDtpPayment( "bla",
                alphaTestUser.getAccountNumber(),
                BigDecimal.TEN);
        THEN("Status code 201 is returned");
        assertNotNull(response);

        AND("Payment response has been returned");
        OBWriteDomesticResponse5Data data = response.getData();
        assertEquals(data.getStatus(), OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(29)
    void balance_charge_savings() {
        WHEN("Calling account balances api");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalancesCharge(alphaTestUser.getAccountNumber());
        THEN("Status code 200 is returned");

        OBReadBalance1Data balanceResponseData = balanceResponse.getData();
        assertNotNull(balanceResponseData);
    }

    @Tag(PAYMENT_TEST)
    @Test
    @Order(30)
    void balance_charge_current() {
        WHEN("Calling account balances api");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalancesCharge(alphaTestUser.getCurrentAccountNumber());
        THEN("Status code 200 is returned");

        OBReadBalance1Data balanceResponseData = balanceResponse.getData();
        assertNotNull(balanceResponseData);
    }

    private void domesticIncoming(String accountNumber) {
        WriteDomesticPayment1 request = DomesticPaymentRequestUtils.prepareDomesticRequest(
                accountNumber,
                DOMESTIC_INCOMING_CRED_IBAN,
                "DomesticIncoming",
                org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(11),
                BigDecimal.TEN
        );

        WHEN("Calling domestic incoming payment api");
        THEN("Should return success response");
        OBWriteDomesticResponse5 domesticResponse = protectedDomesticTransferApiFlows.createDomesticPayment(request);

        OBWriteDomesticResponse5Data data = domesticResponse.getData();
        assertEquals(data.getStatus(), OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_WITHOUT_POSTING);
        DONE();
    }

    private void dtp2Legacy(String accountNumber1) {
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareLegacyConsent(
                accountNumber1,
                ACCOUNT_NUMBER,
                temenosConfig.getLegacyIban(),
                ACCOUNT_IBAN,
                new BigDecimal("1.22"),
                "AED",
                "EOS",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        WHEN("Funds transfers to savings account from T24 funds account :" + accountNumber1);

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
                        accountNumber1,
                        ACCOUNT_NUMBER,
                        temenosConfig.getLegacyIban(),
                        ACCOUNT_IBAN,
                        new BigDecimal("1.22"),
                        "EOS",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                        RandomStringUtils.randomAlphabetic(7) + "Instruct");


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteDomesticResponse5 paymentResponse =
                domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, paymentRequest);
        Assertions.assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getData().getStatus(),
                OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);
    }


    private void domesticOutgoing(String accountId) {
        final String debtorAccountId = StringUtils.leftPad(accountId, 10, "0");

        WHEN("User has enough funds for transfer in his account");
        WHEN("Transfer amount is less than authorised limit");
        WHEN("Transfer amount is less than daily limit");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                CREDITOR_ACCOUNT_ID,
                ACCOUNT_IBAN,
                new BigDecimal("105.00"),
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
                        new BigDecimal("105.00"),
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

        DONE();
    }

}
