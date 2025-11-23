package uk.co.deloitte.banking.banking.account.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardPaymentResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1RequestedChargeCodePaymentBearer;
import uk.co.deloitte.banking.ahb.dtp.test.banking.account.model.AccountBalance;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBEIDStatus;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1Data;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDataSetup {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private CardProtectedApi cardProtectedApi;

    @Inject
    private DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    private AlphaTestUser alphaTestUser;

    private String currentAccountNumber;

    @Test
    public void validateAccountBalanceJourneyWithChargeAppliedFalse() {
        GIVEN("I have a marketplace customer");
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

        AND("I update the customer EIDStatus to valid");
        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        this.customerApiV2.updateCustomerValidations(alphaTestUser, eidStatus);

        WHEN("I create bank accounts for the customer");
        this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
        OBWriteAccountResponse1 response = this.accountApi.createCustomerCurrentAccount(this.alphaTestUser);
        currentAccountNumber = response.getData().getAccountId();

        int testTransferAmount = 300;

        THEN("The customer can create transactions");
        // Testdata verification required for dev and CIT. SIT flow is working.
//        NOTE("Creating an internal transfer to enable other transfers");
//        this.accountApi.executeInternalTransfer(alphaTestUser, temenosConfig.getCreditorAccountId(), BigDecimal.valueOf(testTransferAmount));

        NOTE("Creating deposits into account");
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(11));
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(12));
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(13));

//        NOTE("ATM Withdrawal");
//        OBWriteCardWithdrawalResponse1 atmWithdrawalResponse1 = cardProtectedApi.createCardWithdrawal(alphaTestUser.getAccountNumber(),
//                temenosConfig.getCreditorAccountId(),
//                BigDecimal.valueOf(11));
//
//        OBWriteCardWithdrawalResponse1 atmWithdrawalResponse2 = cardProtectedApi.createCardWithdrawal(alphaTestUser.getAccountNumber(),
//                temenosConfig.getCreditorAccountId(),
//                BigDecimal.valueOf(12));
//
//        OBWriteCardWithdrawalResponse1 atmWithdrawalResponse3 = cardProtectedApi.createCardWithdrawal(alphaTestUser.getAccountNumber(),
//                temenosConfig.getCreditorAccountId(),
//                BigDecimal.valueOf(13));

        NOTE("Outgoing Domestic transfer");
        WriteDomesticPayment1 domesticPaymentRequest1 = domesticTransferRequest();
        this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, domesticPaymentRequest1);

        NOTE("DTP-DTP transfer - Different Account Holders");
//        OBWriteDomestic2 internalPaymentRequest11 = internalTransferRequest(temenosConfig.getCreditorAccountId());
//        OBWriteDomesticResponse5 internalPaymentResponse11 = this.transferApiFlows.createInternalTransferPayment(alphaTestUser, internalPaymentRequest11);

        NOTE("DTP-Legacy transfer");
        OBWriteDomestic2 legacyPaymentRequest1 = legacyTransferRequest();
        domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, legacyPaymentRequest1);

        NOTE("DTP-DTP transfer - Same account holder");
        OBWriteDomestic2 internalPaymentRequest21 = internalTransferRequest(currentAccountNumber);
        this.transferApiFlows.createInternalTransferPayment(alphaTestUser, internalPaymentRequest21);

        NOTE("POS transaction");
        OBWriteCardPaymentResponse1 posResponse1 = cardProtectedApi.createCardPayment(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(11));
//        OBWriteCardPaymentResponse1 posResponse2 = cardProtectedApi.createCardPayment(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(12));
//        OBWriteCardPaymentResponse1 posResponse3 = cardProtectedApi.createCardPayment(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(13));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("charge", "false");

        NOTE("Balance Enquiry");
        OBReadBalance1 balances = this.accountApi.getAccountBalancesATM(alphaTestUser.getAccountNumber(), queryParams);
        String interimAvailable = getBalanceBy(balances, "INTERIM_AVAILABLE");
        String interimBooked = getBalanceBy(balances, "INTERIM_BOOKED");
        Double lockedAmount = Double.parseDouble(interimAvailable) - Double.parseDouble(interimBooked);

        NOTE("Account balance call for source of truth");
        AccountBalance accountBalance = this.accountApi.getAccountBalance(alphaTestUser.getAccountNumber());

        NOTE("Assert Locked Amounts");
        Assertions.assertEquals(interimAvailable, accountBalance.body.get(0).workingBalance);
        Assertions.assertEquals(interimBooked, accountBalance.body.get(0).useableBalance);
        Assertions.assertEquals(lockedAmount, Double.parseDouble(accountBalance.body.get(0).lockedAmount));

        System.err.println("Customer details: ");
        System.out.println();
        System.err.println("Customer CIF: " + currentAccountNumber.substring(2, 9));
        System.err.println("Savings account number: " + this.alphaTestUser.getAccountNumber());
        System.err.println("Current account number: " + currentAccountNumber);

        DONE();
    }

    @Test
    public void validateAccountBalanceJourneyWithChargeAppliedTrue() {
        GIVEN("I have a marketplace customer");
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

        AND("I update the customer EIDStatus to valid");
        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        this.customerApiV2.updateCustomerValidations(alphaTestUser, eidStatus);

        WHEN("I create bank accounts for the customer");
        this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
        OBWriteAccountResponse1 response = this.accountApi.createCustomerCurrentAccount(this.alphaTestUser);
        currentAccountNumber = response.getData().getAccountId();

        THEN("The customer can create transactions");

        NOTE("Creating deposits into account");
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(11));
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(12));
        this.cardProtectedApi.createCardDepositWebhook(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(13));

        NOTE("Outgoing Domestic transfer");
        WriteDomesticPayment1 domesticPaymentRequest1 = domesticTransferRequest();
        this.domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, domesticPaymentRequest1);

        NOTE("DTP-Legacy transfer");
        OBWriteDomestic2 legacyPaymentRequest1 = legacyTransferRequest();
        domesticTransferApiFlows.executeDomesticPayment(alphaTestUser, legacyPaymentRequest1);

        NOTE("DTP-DTP transfer - Same account holder");
        OBWriteDomestic2 internalPaymentRequest21 = internalTransferRequest(currentAccountNumber);
        this.transferApiFlows.createInternalTransferPayment(alphaTestUser, internalPaymentRequest21);

        NOTE("POS transaction");
        cardProtectedApi.createCardPayment(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(11));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("charge", "true");

        NOTE("Balance Enquiry");
        OBReadBalance1 balances = this.accountApi.getAccountBalancesATM(alphaTestUser.getAccountNumber(), queryParams);

        NOTE("Account balance call for source of truth");
        AccountBalance accountBalance = this.accountApi.getAccountBalance(alphaTestUser.getAccountNumber());

        NOTE("Assert Locked Amounts");
        Assertions.assertEquals(getBalanceBy(balances, "LOCKED_AMOUNT"), accountBalance.body.get(0).lockedAmount);

        System.err.println("Customer details: ");
        System.out.println();
        System.err.println("Customer CIF: " + currentAccountNumber.substring(2, 9));
        System.err.println("Savings account number: " + this.alphaTestUser.getAccountNumber());
        System.err.println("Current account number: " + currentAccountNumber);

        DONE();
    }

    private String getBalanceBy(OBReadBalance1 balances, String interim_available) {
        return balances.getData().getBalance()
                .stream()
                .filter((b) -> b.getType().toString().equalsIgnoreCase(interim_available))
                .map((b) -> b.getAmount().getAmount())
                .findFirst()
                .get();
    }

    private OBWriteDomestic2 internalTransferRequest(String creditorAccount) {
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                creditorAccount,
                ACCOUNT_NUMBER,
                new BigDecimal(10),
                "AED",
                "validReference",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        OBWriteDomesticConsentResponse5 consentResponse = this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent, 201);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 transferRequest = PaymentRequestUtils.prepareInternalTransferRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                creditorAccount,
                ACCOUNT_NUMBER,
                new BigDecimal(10), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
        return transferRequest;
    }

    private OBWriteDomestic2 legacyTransferRequest() {
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareLegacyConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getLegacyIban(),
                ACCOUNT_IBAN,
                new BigDecimal(10),
                "AED",
                "EDU",
                "1",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti",
                RandomStringUtils.randomAlphabetic(7) + "Instruct");

        OBWriteDomesticConsentResponse5 consentResponse = this.domesticTransferApiFlows.createDomesticPaymentConsent(alphaTestUser, consent);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        OBWriteDomestic2 paymentRequest = PaymentRequestUtils.prepareLegacyRequest(
                consentResponse.getData().getConsentId(),
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getLegacyIban(),
                ACCOUNT_IBAN,
                new BigDecimal("10"),
                consentResponse.getData().getInitiation().getRemittanceInformation().getReference(),
                "1",
                consentResponse.getData().getInitiation().getEndToEndIdentification(),
                RandomStringUtils.randomAlphabetic(7) + "Instruct");
        return paymentRequest;
    }

    private WriteDomesticPayment1 domesticTransferRequest() {
        OBWriteDomesticConsent4 consent = PaymentRequestUtils.prepareDomesticConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                temenosConfig.getCreditorIban(),
                ACCOUNT_IBAN,
                new BigDecimal(10),
                "AED",
                "EDU",
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_DEBTOR,
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

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
        return paymentRequest;
    }
}
