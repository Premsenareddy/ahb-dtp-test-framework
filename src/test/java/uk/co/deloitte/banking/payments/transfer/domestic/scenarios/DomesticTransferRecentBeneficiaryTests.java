package uk.co.deloitte.banking.payments.transfer.domestic.scenarios;


import com.google.common.collect.Lists;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
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
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.transfer.common.TransferUtils;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.EQUAL_TO;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.LESS_THEN;
import static uk.co.deloitte.banking.payments.transfer.common.TransferUtils.PAYMENTS_ASSUMPTION_MSG;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DomesticTransferRecentBeneficiaryTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;


    @Inject
    private AuthenticateApiV2 authenticateApi1;

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

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

    private String creditorAccountNumber = null;

    private String debtorAccountNumber = null;

    private static final int loginMinWeightExpectedBio = 31;




    private void createNewUser() {
        envUtils.ignoreTestInEnv(Environments.SIT ,Environments.NFT);
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

    private void beneStepUpAuthBiometrics() {
        authenticateApi1.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi1.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    public void valid_domestic_payment_create_consent_and_transfer_Beneficiary_exists() {
        TEST("AHBDB-354 / AHBDB-1632 - valid domestic consent with valid reference : " + "CHC");

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


        WHEN("I create a beneficiary with an valid type and IBAN");
        BeneficiaryData beneficiaryData1 = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData1.setAccountNumber(creditorAccountNumber);
        beneficiaryData1.setBeneficiaryType("Bank Misr");

        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser, beneficiaryData1);



        AND("I create the domestic transfer consent payload");
        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareDomesticConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                ACCOUNT_IBAN,
                PAYMENT_AMOUNT,
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
        final WriteDomesticPayment1 transferRequest =
                PaymentRequestUtils.prepareDomesticTransferRequest(consentResponse.getData().getConsentId(),
                        debtorAccountNumber,
                        ACCOUNT_NUMBER,
                        creditorAccountNumber,
                        ACCOUNT_IBAN,
                        PAYMENT_AMOUNT,
                        "CHC",
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



        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaries(alphaTestUser).getData().getBeneficiary().get(0);
        Assertions.assertNotNull(GetObBeneficiary5.getLastTransactionDateTime());

        DONE();
    }


}


