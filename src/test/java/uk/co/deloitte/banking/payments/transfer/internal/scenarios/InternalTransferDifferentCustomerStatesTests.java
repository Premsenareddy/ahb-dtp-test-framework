package uk.co.deloitte.banking.payments.transfer.internal.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
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
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalTransferDifferentCustomerStatesTests {

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

    private AlphaTestUser alphaTestUser;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    protected CardProtectedApi cardProtectedApi;



    private void createUserForSetAccountForLogin() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                    alphaTestUser.getAccountNumber(),
                    BigDecimal.valueOf(30));
            assertNotNull(response);
        }
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @ParameterizedTest
    @ValueSource(strings = {"IDV_COMPLETED",
            "IDV_FAILED",
            "IDV_REVIEW_REQUIRED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_APPROVED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_REJECTED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_IN_PROGRESS",
            "ACCOUNT_CREATION_RISK_REJECTION",
            "ACCOUNT_CREATION_IN_PROGRESS",
            "ACCOUNT_CREATED",
            "ACCOUNT_VERIFIED",
            "ACCOUNT_CREATION_BC_REVIEW_IN_PROGRESS",
            "ACCOUNT_CREATION_BC_REVIEW_APPROVED",
            "ACCOUNT_CREATION_BC_REVIEW_REJECTED",
            "ACCOUNT_CREATION_REVIEW_PARTIALLY_APPROVED",
            "ACCOUNT_CREATION_EMBOSS_NAME_SPECIFIED",
            "ACCOUNT_CREATION_CARD_DELIVERY_IN_PROGRESS",
            "SUSPENDED_UNDER_AGE"})
    public void valid_DTP_transfer_create_consent_and_transfer_valid_reference(String customerState) {
        TEST("AHBDB-326 / AHBDB-3338 - valid consent and payment with valid reference : " + customerState);

        GIVEN("I have a valid access token and account scope and bank account");
        createUserForSetAccountForLogin();

        WHEN("The platform attempts to update the customer in CRM");
        NOTE("Relates to a task for new customer states");
        OBWritePartialCustomer1 data = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.valueOf(customerState))
                        .build())
                .build();
        THEN("The platform returns a 200 OK");
        this.customerApi.updateCustomer(alphaTestUser, data, 200);
        AND("The details in CRM are persisted");
        OBReadCustomer1 getCustomer = this.customerApi.getCurrentCustomer(alphaTestUser);
        Assertions.assertEquals(OBCustomerStateV1.valueOf(customerState),
                getCustomer.getData().getCustomer().get(0).getCustomerState());

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
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        WHEN("Funds transfers to savings account from T24 funds account :" + alphaTestUser.getAccountNumber());

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        THEN("I submit the valid payment consent request");
        final OBWriteDomesticConsentResponse5 consentResponse =
                this.transferApiFlows.createInternalPaymentConsent(alphaTestUser, consent4);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        Assertions.assertEquals(consentResponse.getData().getInitiation().getCreditorAccount().getIdentification(), temenosConfig.getCreditorAccountId());

        AND("I create the valid matching payment transfer request");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponse.getData().getConsentId(),
                        alphaTestUser.getAccountNumber(),
                        ACCOUNT_NUMBER,
                        temenosConfig.getCreditorAccountId(),
                        ACCOUNT_NUMBER,
                        new BigDecimal("1.00"),
                        "validReference",
                        "unstructured",
                        RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("I successfully trigger the internal transfer payment");
        final OBWriteDomesticResponse5 transferResponse =
                this.transferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(transferResponse);

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);

        DONE();
    }

}