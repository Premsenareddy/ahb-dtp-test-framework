package uk.co.deloitte.banking.payments.transfer.internal.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalTransferStepUpLimitsOwnAccounts {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private InternalTransferApiFlows transferApiFlows;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    private AlphaTestUser alphaTestUser;

    private static String SECOND_ACCOUNT_ID = "";

    private void createUserForSetAccountForLogin() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            SECOND_ACCOUNT_ID = accountApi.createCustomerCurrentAccount(alphaTestUser).getData().getAccountId();
        }
       /* else
        {
            this.alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }*/
    }

    private OBWriteDomesticConsent4 obWriteDomesticConsent4Valid(int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalConsent(
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                SECOND_ACCOUNT_ID,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount),
                "AED",
                "validReference",
                "unstructured",
                //RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    private OBWriteDomestic2 obWriteDomestic2Valid(String consentId, int testTransferAmount) {
        return PaymentRequestUtils.prepareInternalTransferRequest(consentId,
                alphaTestUser.getAccountNumber(),
                ACCOUNT_NUMBER,
                SECOND_ACCOUNT_ID,
                ACCOUNT_NUMBER,
                new BigDecimal(testTransferAmount), "validReference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    public void valid_DTP_transfer_users_own_accounts_no_step_up() throws InterruptedException {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxUnauthLimit = paymentConfiguration.getMaxUnauthLimit();
        int testTransferAmount = Integer.parseInt(maxUnauthLimit) + 10;

        AND("I transfer the test money to the created user's account");

OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
        alphaTestUser.getAccountNumber(),
        BigDecimal.valueOf(testTransferAmount));
assertNotNull(response);

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertEquals(Integer.parseInt(interimAvailable), testTransferAmount);
        Assertions.assertEquals(Integer.parseInt(interimBooked), testTransferAmount);

        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount);

        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), testTransferAmount);

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        DONE();
    }

    @Test
    public void valid_DTP_transfer_users_own_accounts_no_transfer_limits() {
        TEST("AHBDB-2519 - Validate DTP Transfer Limits on Consent request");

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        String maxPaymentLimit = paymentConfiguration.getTransferLimit();
        int testTransferAmount = Integer.parseInt(maxPaymentLimit) + 10;

        AND("I transfer the test money to the created user's account");

OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
        alphaTestUser.getAccountNumber(),
        BigDecimal.valueOf(testTransferAmount));
assertNotNull(response);

        AND("The users balance is updated");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertEquals(Integer.parseInt(interimAvailable), testTransferAmount);
        Assertions.assertEquals(Integer.parseInt(interimBooked), testTransferAmount);

        AND("I create the consent payload with a value of : " + testTransferAmount);
        final OBWriteDomesticConsent4 consent4 = obWriteDomesticConsent4Valid(testTransferAmount);

        THEN("I can successfully create consent");
        final OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 =
                this.transferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        assertNotNull(obWriteDomesticConsentResponse5.getData().getConsentId());

        final OBWriteDomestic2 transferRequest = obWriteDomestic2Valid(obWriteDomesticConsentResponse5.getData().getConsentId(), testTransferAmount);

        THEN("I successfully trigger the internal transfer payment");final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        assertNotNull(transferResponse);

        DONE();
    }


}
