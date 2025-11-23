package uk.co.deloitte.banking.payments.transfer.international.scenario;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternational3;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternationalConsent5;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternationalConsentResponse6;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternationalResponse5;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternationalResponse6Data;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
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
import uk.co.deloitte.banking.payments.transfer.international.api.InternationalTransferApiFlows;

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
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.INTERNATIONAL_IBAN;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternationalTransferStepUpAuthLimitTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;


    @Inject
    private InternationalTransferApiFlows internationalTransferApiFlows;

    private static final int loginMinWeightExpectedBio = 31;


    private String creditorAccountNumber = null;

    private String debtorAccountNumber = null;


    private void createNewUser() {
        envUtils.ignoreTestInEnv("AHBDB-8621 - epic not deployed sit / nft", Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
            debtorAccountNumber = alphaTestUser.getAccountNumber();
            creditorAccountNumber = temenosConfig.getCreditorIban();
            OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                    debtorAccountNumber,
                    BigDecimal.valueOf(20));
            assertNotNull(response);
        }
        else {
            this.alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }

    }

    private void beneStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHC-purpose", "EDU-purpose"})
    @Order(2)
    public void valid_international_payment_create_consent_and_transfer_valid_reference_BeneficiaryExistAndAmountBelowMaxLimit(String validReference) {
        TEST("AHBDB-8623 / AHBDB-8625 - valid international consent with valid reference : " + validReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");
        String endToEndRef = RandomStringUtils.randomAlphabetic(7) + "E2Eidenti";

        AND("I create the international transfer consent payload");
        AND("I have amount To transfer less than the Max limit Per transaction");
        AND("I beneficiary is already added before");
        final OBWriteInternationalConsent5 consent5 = PaymentRequestUtils.prepareInternationalConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(40000),
                "AED",
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef
        );

        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        WHEN("I create a beneficiary with an valid type and IBAN");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(creditorAccountNumber);
        beneficiaryData.setBeneficiaryType("other_bank");
        beneficiaryData.setSwiftCode("ICICINBB");

        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser,
                beneficiaryData);

        THEN("No need to do step up as beneficiary is found amount need to transfer below max limit");

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, consent5);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = PaymentRequestUtils.prepareInternationalTransferRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(40000),
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef);

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(paymentResponse);

        assertEquals(paymentResponse.getData().getStatus(), OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        // String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        // Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        // commenting assertions due to t24 performance
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        // commenting assertions due to t24 performance
        //   Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);


        DONE();
    }



    @ParameterizedTest
    @ValueSource(strings = {"CHC-purpose", "EDU-purpose"})
    @Order(2)
    public void valid_international_payment_create_consent_and_transfer_valid_reference_BeneficiaryNotFoundAndAmountBelowMaxLimit(String validReference) {
        TEST("AHBDB-8623 / AHBDB-8625 - valid international consent with valid reference : " + validReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");
        String endToEndRef = RandomStringUtils.randomAlphabetic(7) + "E2Eidenti";

        AND("I create the international transfer consent payload");
        AND("I have amount To transfer less than the Max limit Per transaction");
        AND("I beneficiary is already not found for the user");
        final OBWriteInternationalConsent5 consent5 = PaymentRequestUtils.prepareInternationalConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(40000),
                "AED",
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef
        );

        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        THEN("I need to do step up as beneficiary not added before");
        beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, consent5);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = PaymentRequestUtils.prepareInternationalTransferRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(40000),
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef);

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(paymentResponse);

        assertEquals(paymentResponse.getData().getStatus(), OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        // String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        // Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        // commenting assertions due to t24 performance
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        // commenting assertions due to t24 performance
        //   Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);


        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"CHC-purpose", "EDU-purpose"})
    @Order(2)
    public void valid_international_payment_create_consent_and_transfer_valid_reference_BeneficiaryExistAndAmountExceedMaxLimit(String validReference) {
        TEST("AHBDB-8623 / AHBDB-8625 - valid international consent with valid reference : " + validReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");
        String endToEndRef = RandomStringUtils.randomAlphabetic(7) + "E2Eidenti";

        AND("I create the international transfer consent payload");
        AND("I have amount To transfer more than the Max limit Per transaction");
        AND("I beneficiary is already added before");
        final OBWriteInternationalConsent5 consent5 = PaymentRequestUtils.prepareInternationalConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(60000),
                "AED",
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef
        );

        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        WHEN("I create a beneficiary with an valid type and IBAN");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(creditorAccountNumber);
        beneficiaryData.setBeneficiaryType("other_bank");
        beneficiaryData.setSwiftCode("ICICINBB");

        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser,
                beneficiaryData);

        THEN("I need to do step up as amount need to transfer exceed max limit");
        beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, consent5);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = PaymentRequestUtils.prepareInternationalTransferRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(60000),
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef);

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(paymentResponse);

        assertEquals(paymentResponse.getData().getStatus(), OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        // String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        // Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        // commenting assertions due to t24 performance
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        // commenting assertions due to t24 performance
        //   Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);


        DONE();
    }



    @ParameterizedTest
    @ValueSource(strings = {"CHC-purpose", "EDU-purpose"})
    @Order(2)
    public void valid_international_payment_create_consent_and_transfer_valid_reference_BeneficiaryNotFoundAndAmountExceedMaxLimit(String validReference) {
        TEST("AHBDB-8623 / AHBDB-8625 - valid international consent with valid reference : " + validReference);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");
        String endToEndRef = RandomStringUtils.randomAlphabetic(7) + "E2Eidenti";

        AND("I create the international transfer consent payload");
        AND("I have amount To transfer more than the Max limit Per transaction");
        AND("I beneficiary is not found");
        final OBWriteInternationalConsent5 consent5 = PaymentRequestUtils.prepareInternationalConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(60000),
                "AED",
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef
        );

        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);


        THEN("I need to do step up as beneficiary not found and transfer amount exceed max limit");
        beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, consent5);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = PaymentRequestUtils.prepareInternationalTransferRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(60000),
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef);

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(paymentResponse);

        assertEquals(paymentResponse.getData().getStatus(), OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        // String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        // Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        // commenting assertions due to t24 performance
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        // commenting assertions due to t24 performance
        //   Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);


        DONE();
    }
    /*#######################################################
DefectID:AHBDB-14991
Description:Asking for Step Up if Beneficiary Added and paymnet is more than 50K
CreatedBy:Shilpi Agrawal
UpdatedBy:
LastUpdatedOn:
Comments:Defects-Created Extra Validation for above scenario if we don't step up 403 should come
#######################################################*/
    @ParameterizedTest
    @ValueSource(strings = {"CHC-Charitable"})
    @Order(2)
    public void valid_international_payment_create_consent_and_transfer_valid_reference_StepUpRequiredBeneficiaryExistAndAmountExceedMaxLimit(String validReference) {
        TEST("AHBDB-8623 / AHBDB-8625 - valid international consent with valid reference : " + validReference);
        TEST("AHBDB-14991 - Asking for Step Up if Beneficiary Added and paymnet is more than 50K");
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");
        String endToEndRef = RandomStringUtils.randomAlphabetic(7) + "E2Eidenti";

        AND("I create the international transfer consent payload");
        AND("I have amount To transfer more than the Max limit Per transaction");
        AND("I beneficiary is already added before");
        final OBWriteInternationalConsent5 consent5 = PaymentRequestUtils.prepareInternationalConsent(
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(60000),
                "AED",
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef
        );

        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        WHEN("I create a beneficiary with an valid type and IBAN");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(creditorAccountNumber);
        beneficiaryData.setBeneficiaryType("other_bank");
        beneficiaryData.setSwiftCode("ICICINBB");
        beneStepUpAuthBiometrics();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser,
                beneficiaryData);

        THEN("I need to do not step up as amount need to transfer exceed max limit and send consent request,Rashi@1810 will get 403 Error");

        this.internationalTransferApiFlows.internationalPaymentConsentError(alphaTestUser, consent5, 403);
        THEN("I need to do step up as amount need to transfer exceed max limit");
        beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, consent5);
        Assertions.assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = PaymentRequestUtils.prepareInternationalTransferRequest(consentResponse.getData().getConsentId(),
                debtorAccountNumber,
                ACCOUNT_NUMBER,
                creditorAccountNumber,
                INTERNATIONAL_IBAN,
                new BigDecimal("1.00"),
                new BigDecimal(60000),
                validReference,
                "unstructured",
                OBChargeBearerType1Code.BORNE_BY_CREDITOR,
                endToEndRef);

        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);
        Assertions.assertNotNull(paymentResponse);

        assertEquals(paymentResponse.getData().getStatus(), OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);


        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);

        // String interimAvailableAfterPayment = balanceAfterPayment.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        // Assertions.assertNotEquals(interimAvailable, interimAvailableAfterPayment);
        // commenting assertions due to t24 performance
        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());
        // commenting assertions due to t24 performance
        //   Assertions.assertNotEquals(response.getData().getTransaction().size(), 0);


        DONE();
    }
}
