package uk.co.deloitte.banking.payments.transfer.international.scenario;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBRisk1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBPostalAddress6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.account.api.payment.model.international.*;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.internationalCharges.InternationalChargesResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.payment.charges.api.InternationalPaymentChargesApiFlows;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;
import uk.co.deloitte.banking.payments.transfer.international.api.InternationalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.INTERNATIONAL_IBAN;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternationalTransferTests {

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

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private InternationalPaymentChargesApiFlows internationalPaymentChargesApiFlows;

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
            //this.alphaTestUser.setUserEmail("shilpiagr"+ RandomDataGenerator.generateRandomAlphanumericUpperCase(3)+"@test.com");
            OBWriteCardDepositResponse1 response = cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),
                    debtorAccountNumber,
                    BigDecimal.valueOf(5000.00));
            assertNotNull(response);
        }
        refreshToken();

    }
    public void refreshToken() {
        this.alphaTestUserFactory.refreshAccessToken(this.alphaTestUser);
    }

    public OBWriteInternationalConsent5 internationalPaymentConsentRequestDataValidWithAccount(String iBan) {
        final OBWriteInternational3DataInitiationCreditorAccount creditorAccount;
        if ((iBan.isBlank()))
        {
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .identification(creditorAccountNumber)
                    .name("Test Account Owner Name"+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    //.name("Test Account Owner")
                    //.secondaryIdentification("ICI0000012")
                    .secondaryIdentification("CHAS0INBX01")
                    .build();

        }
        else if(StringUtils.isNumeric(iBan)) {
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"))
                    .identification(temenosConfig.getCreditorAccountId())
                    .name("Test Account Owner Name" + RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .secondaryIdentification("CHAS0INBX01")
                    .build();
        }
        else{
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .identification(iBan)
                    .name("Test Account Owner Name" + RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .build();
        }
        final OBWriteInternational3DataInitiationDebtorAccount debtorAccount = OBWriteInternational3DataInitiationDebtorAccount.builder()
                .schemeName("UAE.AccountNumber")
                .identification(debtorAccountNumber)
                .build();
        String reference = RandomDataGenerator.generateRandomAlphanumericUpperCase(12);
        final OBWriteInternational3DataInitiationRemittanceInformation inf = OBWriteInternational3DataInitiationRemittanceInformation.builder()
                .reference("PIN-Personal investments")
                .unstructured("Remarks "+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                .build();
        String endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(16);
        return OBWriteInternationalConsent5.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteInternationalConsent5Data.builder()
                        .initiation(OBWriteInternational3DataInitiation.builder()
                                .endToEndIdentification(endToEndReference)
                                .creditor(OBWriteInternational3DataInitiationCreditor.builder()
                                        .name("JHON " + RandomDataGenerator.generateEnglishRandomString(4))
                                        .postalAddress(OBWriteInternational3DataInitiationPostalAddress.builder()
                                                .addressLine1("House No" + RandomDataGenerator.generateRandomBuildingNumber())
                                                .addressLine2(RandomDataGenerator.generateRandomAddressLine())
                                                .addressLine3(RandomDataGenerator.generateRandomCityOfBirth())
                                                .country("IN")
                                                .build())
                                        .build())
                                .creditorAgent(OBWriteInternational3DataInitiationCreditorAgent.builder()
                                        .identification("CHASUS33")
                                        .name("Test Bank Name "+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                                        .postalAddress(OBPostalAddress6.builder()
                                                .country("IN")
                                                .build())
                                        .build())
                                .creditorAccount(creditorAccount)
                                .debtorAccount(debtorAccount)
                                .remittanceInformation(inf)
                                .chargeBearer(OBChargeBearerType1Code.BORNE_BY_CREDITOR)
                                .instructedAmount(OBWriteInternational3DataInitiationInstructedAmount.builder()
                                        .amount(BigDecimal.valueOf(10.00))
                                        .currency("INR").build())
                                .supplementaryData(OBWriteInternational3DataInitiationSupplementaryData.builder()
                                        .debitCurrency("AED")
                                        .amountInAed(BigDecimal.valueOf(10.00))
                                        .beneficiaryNickName(RandomDataGenerator.generateEnglishRandomString(3))
                                        .build())
                                .build())
                        .build())
                .build();

    }

    public OBWriteInternational3 internationalTransferRequestDataValid(String consentID, String iBan) {

        final OBWriteInternational3DataInitiationCreditorAccount creditorAccount;
        if ((iBan.isBlank()))
        {
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .identification(creditorAccountNumber)
                    .name("Test Account Owner Name"+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .secondaryIdentification("CHAS0INBX01")
                    .build();

        }
        else if(StringUtils.isNumeric(iBan)) {
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"))
                    .identification(temenosConfig.getCreditorAccountId())
                    .name("Test Account Owner Name" + RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .secondaryIdentification("CHAS0INBX01")
                    .build();
        }
        else{
            creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                    .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("IBAN"))
                    .identification(iBan)
                    .name("Test Account Owner Name" + RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                    .build();
        }
        final OBWriteInternational3DataInitiationDebtorAccount debtorAccount = OBWriteInternational3DataInitiationDebtorAccount.builder()
                .schemeName(ACCOUNT_NUMBER)
                .identification(debtorAccountNumber)
                .build();

        final OBWriteInternational3DataInitiationRemittanceInformation inf = OBWriteInternational3DataInitiationRemittanceInformation.builder()
                .reference("PIN-Personal investments")
                .unstructured("Remarks "+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                .build();
        String endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(16);

        return OBWriteInternational3.builder()
                .data(OBWriteInternational3Data.builder()
                        .consentId(consentID)
                        .initiation(OBWriteInternational3DataInitiation.builder()
                                .endToEndIdentification(endToEndReference)
                                .creditor(OBWriteInternational3DataInitiationCreditor.builder()
                                        .name("JHON " + RandomDataGenerator.generateEnglishRandomString(4))
                                        .postalAddress(OBWriteInternational3DataInitiationPostalAddress.builder()
                                                .addressLine1("House No" + RandomDataGenerator.generateRandomBuildingNumber())
                                                .addressLine2(RandomDataGenerator.generateRandomAddressLine())
                                                .addressLine3(RandomDataGenerator.generateRandomCityOfBirth())
                                                .country("IN")
                                                .build())
                                        .build())
                                .debtorAccount(debtorAccount)
                                .remittanceInformation(inf)
                                .creditorAccount(creditorAccount)
                                .instructedAmount(OBWriteInternational3DataInitiationInstructedAmount.builder()
                                        .amount(BigDecimal.valueOf(10.00))
                                        .currency("INR")
                                        .build())
                                .creditorAgent(OBWriteInternational3DataInitiationCreditorAgent.builder()
                                        .identification("CHASUS33")
                                        //.name("Test Bank name")
                                        .name("Test Bank Name "+RandomDataGenerator.generateRandomAlphanumericUpperCase(3))
                                        .postalAddress(OBPostalAddress6.builder()
                                                .country("IN")
                                                .build())
                                        .build())
                                .supplementaryData(OBWriteInternational3DataInitiationSupplementaryData.builder()
                                        .debitCurrency("AED")
                                        .amountInAed(BigDecimal.valueOf(10.00))
                                        .build())
                                .chargeBearer(OBChargeBearerType1Code.BORNE_BY_CREDITOR)
                                .build())
                        .build())
                .build();


    }


    public void beneStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHC-purpose", "EDU-purpose", "EMI-purpose", "FAM-purpose"})
    @Order(2)
    public void valid_international_payment_create_consent_and_transfer_valid_reference(String validReference) {
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
                new BigDecimal(50000),
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
                new BigDecimal("1.00"),
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
       TestCaseID:AHBDB-14978
       Description:[POST internal/v1/international-payment-consents] Positive flow 201 Response code when user provides a valid local IBAN number existing in the core banking using IBAN.com or a valid Swift code available in new core Banking
       CreatedBy:Shilpi Agrawal
       UpdatedBy:
       LastUpdatedOn:
       Comments:
    #######################################################*/
    @ParameterizedTest

    @CsvSource({
            "10,'', CHASUS33,IN, INR, 12345678901, BORNE_BY_CREDITOR, CHC-Charitable contributions",
            "1, '',ROYCCAT2VIC,CA, CAD, 010, SHARED, EMI-Equated monthly instalments",
            "1, IE64IRCE92050112345678,'IRCEIE2D',IE, EUR, 981234, BORNE_BY_DEBTOR, OAT-Own Account Transfer",
            "2,AT483200000012345864,'RLNWATWW',AU,AUD,032732,BORNE_BY_DEBTOR, RNT-Rent Payments",
            "3,JO71CBJO0000000000001234567890,'CBJOJOAX',JO,JOD,'',BORNE_BY_DEBTOR, RNT-Rent Payments",
            "4,'','IIIGGB22',GB,GBP,200415,BORNE_BY_DEBTOR, EDU-purpose",
            "4,'GB33BUKB20201555555555','BUKBGB22',GB,GBP,'202678',BORNE_BY_DEBTOR, EDU-purpose",
            "5,'','IZZBOMRU',OM,OMR,'',BORNE_BY_DEBTOR, EDU-purpose",
    })

    public void valid_international_create_consent(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment) {
        TEST("AHBDB-8623 / AHBDB-8625 - valid international consent with valid reference : ");
        TEST("AHBDB-14978- [POST internal/v1/international-payment-consents] Positive flow 201 Response code when user provides a valid local IBAN number existing in the core banking using IBAN.com or a valid Swift code available in new core Banking for following Test Data" + " Payment Amount" + paymentAmount + " IBan " + iBan + " swift code" + swiftCode + " Country & " + countryCode + " Currency" + currencyCode + "  charge Bearer Type " + chargeBearerType + "pupose Of payment " + puposeOfpayment);
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the international transfer consent payload");
        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);
        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        internationPaymentConsentRequest.getData().getInitiation().getSupplementaryData().setBeneficiaryNickName("Nic");
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2("Address Line 2");
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("Test International Payment");
        internationPaymentConsentRequest.getData().getInitiation().getSupplementaryData().setDebitCurrency("AED");
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);
        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);
        beneStepUpAuthBiometrics();
        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());
        assertEquals(consentResponse.getData().getInitiation().getCreditorAgent().getIdentification(),swiftCode);
        assertEquals(consentResponse.getData().getInitiation().getRemittanceInformation().getReference(),puposeOfpayment);
        DONE();
    }

    /*#######################################################
       TestCaseID:AHBDB-14978
       Description:[POST internal/v1/international-payment-consents] Positive flow 201 Response code when user provides a valid local IBAN number existing in the core banking using IBAN.com or a valid Swift code available in new core Banking
       CreatedBy:Shilpi Agrawal
       UpdatedBy:
       LastUpdatedOn:
       Comments:
    #######################################################*/
    @ParameterizedTest
    @CsvSource({
            "3,JO71CBJO0000000000001234567890,'CBJOJOAX',JO,JOD,'',BORNE_BY_DEBTOR, RNT-Rent Payments",
            "5,'','IZZBOMRU',OM,OMR,'',BORNE_BY_DEBTOR, EDU-purpose",
            "6,'','FEBKUS6LXXX',US,USD,'',BORNE_BY_DEBTOR, RNT-Rent Payments"
    })

    public void valid_international_create_consentWithoutOptionalParameters(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment) {
        TEST("AHBDB-8623 / AHBDB-8625 - valid international consent with valid reference : ");
        TEST("AHBDB-14978- [POST internal/v1/international-payment-consents] Positive flow 201 Response code when user provides a valid local IBAN number existing in the core banking using IBAN.com or a valid Swift code available in new core Banking for following Test Data" + " Payment Amount" + paymentAmount + " IBan " + iBan + " swift code" + swiftCode + " Country & " + countryCode + " Currency" + currencyCode + "  charge Bearer Type " + chargeBearerType + "pupose Of payment " + puposeOfpayment);
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();
        AND("I create the international transfer consent payload without Optional parameters");
        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);
        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        internationPaymentConsentRequest.getData().getInitiation().getSupplementaryData().setBeneficiaryNickName("");
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2("");
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        internationPaymentConsentRequest.getData().getInitiation().getSupplementaryData().setDebitCurrency("");
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);

        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);
        beneStepUpAuthBiometrics();
        THEN("I submit the valid payment consent request and the service returns a 201 and verified response");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());
        assertEquals(consentResponse.getData().getInitiation().getCreditorAgent().getIdentification(),swiftCode);
        assertEquals(consentResponse.getData().getInitiation().getRemittanceInformation().getReference(),puposeOfpayment);
        DONE();
    }

    /*#######################################################
       TestCaseID:AHBDB-14465,AHBDB-14469
       Description:[POST internal/v1/international-payment-consents] Negative flow 400 Response code: if invalid or mandatory field is missing, [POST internal/v1/international-payment-consents] Negative flow 400 Response code: "SecondaryIdentification": field is mandatory in case of England or Ireland, India, Australia, Canada
       CreatedBy:Shilpi Agrawal
       UpdatedBy:
       LastUpdatedOn:
       Comments:Defects fixed
    #######################################################*/
    @ParameterizedTest
    @Tag("DefectAHBDB-15368")
    @ValueSource(strings = {"ReferenceNum",
            "DebitAccountNumber",
            "SourceAccountSchemeName",
            "TransactionAmount",
            "TransactionCurrency",
            "BenAccountNumberOrIBAN",
            "BenAccountSchemeName",
            "BenSWIFTCode",
            "BenBankCountry",
            //"BenName",
            "BenBankName",
            "BenAddress1",
            "BenAddress3",
            "ChargeIndicator",
            "PurposeReasonReference",
            "CreditorSecondaryIdentification",
            "InvalidReferenceNumWithSpecialCharChk",
            "InvalidReferenceNumMaxLengthChk",
            "InvalidDebitAccountNumberMaxLengthChk",
            "InvalidDebitAccountNumberMinLengthChk",
            //"InvalidSourceAccountSchemeName",
            "ZeroTransactionAmount",
            "NegativeTransactionAmount",
            "InvalidTransactionCurrencyMaxLengthChk",
            "InvalidBenAccountNumberOrIBANMaxLengthChk",
            "InvalidBenAddress1MaxLengthChk",
            "InvalidBenAddress2MaxLengthChk",
            "InvalidBenAddress3MaxLengthChk",
            "InvalidRemarksMaxLengthChk",
            "InvalidBenSWIFTCodeMaxLengthChk",
            "InvalidBenSWIFTCodeMinLengthChk"

    })

    public void negative_international_MandatoryCheck_create_consent(String mandatoryFieldName) {
        TEST("AHBDB-8621 / AHBDB-8623 - valid international consent with valid reference : ");
        TEST("AHBDB-14465/AHBDB-14469:[POST internal/v1/international-payment-consents] Negative flow 400 Response code: if invalid or mandatory field is missing.");
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();


        AND("I create the international transfer consent payload with missing mandatory field or invalid value and missing following "+ mandatoryFieldName);

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount("");

        String endToEndReference;
        switch (mandatoryFieldName) {
            case "ReferenceNum":
                //internationPaymentConsentRequest.getData().getInitiation().setEndToEndIdentification(null);
                internationPaymentConsentRequest.getData().getInitiation().setEndToEndIdentification("");
                break;
            case "DebitAccountNumber":
                //internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setIdentification(null);
                internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setIdentification("");
                break;
            case "SourceAccountSchemeName":
                //internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setSchemeName(null);
                internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setSchemeName("");
                break;
            case "TransactionAmount":
                //internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(null);
                internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(BigDecimal.valueOf(0));
                break;
            case "TransactionCurrency":
                //internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(null);
                internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency("");
                break;
            case "BenAccountNumberOrIBAN":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification(null);
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification("");
                break;
            case "BenAccountSchemeName":
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSchemeName(null);

                break;
            case "BenSWIFTCode":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(null);
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification("");
                break;
            case "BenBankCountry":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(null);
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry("");
                break;
            /*case "BenName":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setName("");
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setName(null);
                break;*/
            case "BenBankName":
                internationPaymentConsentRequest.getData().getInitiation().getCreditor().setName(null);
                //internationPaymentConsentRequest.getData().getInitiation().getCreditor().setName("");
                break;
            case "BenAddress1":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine1(null);
                internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine1("");
                break;
            case "BenAddress3":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine3(null);
                internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine3("");
                break;
            case "ChargeIndicator":
                internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(null);
                break;
            case "PurposeReasonReference":
                //internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(null);
                internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference("");
                break;
            case "CreditorSecondaryIdentification":
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSchemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"));
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification(temenosConfig.getCreditorAccountId());
                //internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(null);
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification("");
                break;

            case "InvalidReferenceNumWithSpecialCharChk":
                endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(15);
                internationPaymentConsentRequest.getData().getInitiation().setEndToEndIdentification(endToEndReference + "#");
                break;
            case "InvalidReferenceNumMaxLengthChk":
                endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(17);
                internationPaymentConsentRequest.getData().getInitiation().setEndToEndIdentification(endToEndReference);
                break;
            case "InvalidDebitAccountNumberMaxLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setIdentification("0123456789012");
                break;
            case "InvalidDebitAccountNumberMinLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setIdentification("012345678");
                break;
            /*case "InvalidSourceAccountSchemeName":
                internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setSchemeName("IBAN");
                break;*/
            case "ZeroTransactionAmount":
                internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(BigDecimal.valueOf(0));
                break;
            case "NegativeTransactionAmount":
                internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(BigDecimal.valueOf(-5));
                break;
            case "InvalidTransactionCurrencyMaxLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency("AEDR");
                break;

            case "InvalidBenAccountNumberOrIBANMaxLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;

            case "InvalidBenSWIFTCodeMaxLengthChk":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification("123456789012345");
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(RandomDataGenerator.generateRandomAlphanumericUpperCase(12));
                break;
            case "InvalidBenSWIFTCodeMinLengthChk":
                //internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification("123456789012345");
                internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(RandomDataGenerator.generateRandomAlphanumericUpperCase(7));
                break;
            case "InvalidBenAddress1MaxLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine1(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
            case "InvalidBenAddress2MaxLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
            case "InvalidBenAddress3MaxLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine3(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
            case "InvalidRemarksMaxLengthChk":
                internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
        }

        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        THEN("Error response 400 is returned for bad request with the validation error");

        final OBErrorResponse1 consentResponse =
                this.internationalTransferApiFlows.internationalPaymentConsentError(alphaTestUser, internationPaymentConsentRequest, 400);

        AND("Actual Error Message displayed in Response " + consentResponse.getMessage());
        AND("Actual Error Code displayed in Response " + consentResponse.getCode());
        DONE();
    }

    /*#######################################################
   TestCaseID:AHBDB-15296
   Description:[POST internal/v1/international-payments] Negative flow 400 Response code: if invalid or mandatory field is missing
   CreatedBy:Shilpi Agrawal
   UpdatedBy:
   LastUpdatedOn:
   Comments:Defects-
   AHBDB-15027	ABle to do International payment Consent without entering CreditorAccount.SecondaryIdentiffication that signifies SORT CODE/IFSC Code/BSB Code/Transit number
   https://ahbdigitalbank.atlassian.net/browse/AHBDB-15368
#######################################################*/

    @ParameterizedTest
    @Tag("DefectAHBDB-15368")
    @ValueSource(strings = {
            "ConsentId",
            "ReferenceNum",
            "DebitAccountNumber",
            "SourceAccountSchemeName",
            "TransactionAmount",
            "TransactionCurrency",
            "BenAccountNumberOrIBAN",
            "BenAccountSchemeName",
            "BenSWIFTCode",
            "BenBankCountry",
            //"BenName",
            "BenBankName",
            "BenAddress1",
            "BenAddress3",
            "ChargeIndicator",
            "PurposeReasonReference",
            "CreditorSecondaryIdentification",
            "InvalidReferenceNumWithSpecialCharChk",
            "InvalidReferenceNumMaxLengthChk",
            "InvalidDebitAccountNumberMaxLengthChk",
            "InvalidDebitAccountNumberMinLengthChk",
            //"InvalidSourceAccountSchemeName",
            "ZeroTransactionAmount",
            "NegativeTransactionAmount",
            "InvalidTransactionCurrencyMaxLengthChk",
            "InvalidBenAccountNumberOrIBANMaxLengthChk",
            "InvalidBenAddress1MaxLengthChk",
            "InvalidBenAddress2MaxLengthChk",
            "InvalidBenAddress3MaxLengthChk",
            "InvalidRemarksMaxLengthChk",
            "InvalidBenSWIFTCodeMaxLengthChk",
            "InvalidBenSWIFTCodeMinLengthChk"

    })


    public void negative_international_MandatoryCheck_create_Payment(String mandatoryFieldName) {
        TEST("AHBDB-8621 / AHBDB-8625 - Make International Transfer request ");

        TEST("AHBDB-15296:[POST internal/v1/international-payment] [POST internal/v1/international-payments] Negative flow 400 Response code: if invalid or mandatory field is missing");
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();


        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount("");
        beneStepUpAuthBiometrics();
        // this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);

        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());


        WHEN("The matching payment requested is created missing any of the mandatory input or entered invalid value for following input "+mandatoryFieldName);
        final OBWriteInternational3 transferRequest = internationalTransferRequestDataValid(consentResponse.getData().getConsentId(), "");

        transferRequest.getData().getInitiation().setEndToEndIdentification(consentResponse.getData().getInitiation().getEndToEndIdentification());

        THEN("Then the payment requested is submitted and a 400 response is returned");


        String endToEndReference;
        switch (mandatoryFieldName) {
            case "ConsentId":
                //transferRequest.getData().setConsentId(null);
                transferRequest.getData().setConsentId("");
                break;
            case "ReferenceNum":
                transferRequest.getData().getInitiation().setEndToEndIdentification(null);
                //transferRequest.getData().getInitiation().setEndToEndIdentification("");
                break;

            case "DebitAccountNumber":
                transferRequest.getData().getInitiation().getDebtorAccount().setIdentification(null);
                //transferRequest.getData().getInitiation().getDebtorAccount().setIdentification("");
                break;

            case "SourceAccountSchemeName":
                transferRequest.getData().getInitiation().getDebtorAccount().setSchemeName(null);
                break;
            case "TransactionAmount":
                transferRequest.getData().getInitiation().getInstructedAmount().setAmount(null);
                //transferRequest.getData().getInitiation().getInstructedAmount().setAmount("");
                //transferRequest.getData().getInitiation().getInstructedAmount().setAmount(BigDecimal.valueOf(0));
                break;
            case "TransactionCurrency":
                transferRequest.getData().getInitiation().getInstructedAmount().setCurrency(null);
                //transferRequest.getData().getInitiation().getInstructedAmount().setCurrency("");
                break;
            case "BenAccountNumberOrIBAN":
                transferRequest.getData().getInitiation().getCreditorAccount().setIdentification(null);
                //transferRequest.getData().getInitiation().getCreditorAccount().setIdentification("");
                break;
            case "BenAccountSchemeName":
                transferRequest.getData().getInitiation().getCreditorAccount().setSchemeName(null);
                break;
            case "BenSWIFTCode":
                transferRequest.getData().getInitiation().getCreditorAgent().setIdentification(null);
                //transferRequest.getData().getInitiation().getCreditorAgent().setIdentification("");
                break;
            case "BenBankCountry":
                transferRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(null);
                //transferRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry("");
                break;
            /*case "BenName":
                transferRequest.getData().getInitiation().getCreditorAccount().setName(null);
                //transferRequest.getData().getInitiation().getCreditorAccount().setName("");
                break;*/
            case "BenBankName":
                transferRequest.getData().getInitiation().getCreditor().setName(null);
                //transferRequest.getData().getInitiation().getCreditor().setName("");
                break;
            case "BenAddress1":
                transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine1(null);
                //transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine1("");
                break;
            case "BenAddress3":
                transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine3(null);
                //transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine3("");
                break;
            case "ChargeIndicator":
                transferRequest.getData().getInitiation().setChargeBearer(null);
                break;
            case "PurposeReasonReference":
                transferRequest.getData().getInitiation().getRemittanceInformation().setReference(null);
                //transferRequest.getData().getInitiation().getRemittanceInformation().setReference("");
                break;
            case "CreditorSecondaryIdentification":
                transferRequest.getData().getInitiation().getCreditorAccount().setSchemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"));
                transferRequest.getData().getInitiation().getCreditorAccount().setIdentification(temenosConfig.getCreditorAccountId());
                transferRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(null);
                //transferRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification("");
                break;
            case "InvalidReferenceNumWithSpecialCharChk":
                endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(15);
                transferRequest.getData().getInitiation().setEndToEndIdentification(endToEndReference + "#");
                break;
            case "InvalidReferenceNumMaxLengthChk":
                endToEndReference = RandomDataGenerator.generateRandomAlphanumericUpperCase(17);
                transferRequest.getData().getInitiation().setEndToEndIdentification(endToEndReference);
                break;
            case "InvalidDebitAccountNumberMaxLengthChk":
                transferRequest.getData().getInitiation().getDebtorAccount().setIdentification("0123456789012");
                break;
            case "InvalidDebitAccountNumberMinLengthChk":
                transferRequest.getData().getInitiation().getDebtorAccount().setIdentification("012345678");
                break;
               /* Removed as Requirement changed
           case "InvalidSourceAccountSchemeName":
                transferRequest.getData().getInitiation().getDebtorAccount().setSchemeName("IBAN");
                break;
                */
            case "ZeroTransactionAmount":
                transferRequest.getData().getInitiation().getInstructedAmount().setAmount(BigDecimal.valueOf(0));
                break;
            case "NegativeTransactionAmount":
                transferRequest.getData().getInitiation().getInstructedAmount().setAmount(BigDecimal.valueOf(-5));
                break;
            case "InvalidTransactionCurrencyMaxLengthChk":
                transferRequest.getData().getInitiation().getInstructedAmount().setCurrency("AEDR");
                break;

            case "InvalidBenAccountNumberOrIBANMaxLengthChk":
                transferRequest.getData().getInitiation().getCreditorAccount().setIdentification(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;

            case "InvalidBenSWIFTCodeMaxLengthChk":
                //transferRequest.getData().getInitiation().getCreditorAccount().setIdentification("123456789012345");
                transferRequest.getData().getInitiation().getCreditorAgent().setIdentification(RandomDataGenerator.generateRandomAlphanumericUpperCase(12));
                break;
            case "InvalidBenSWIFTCodeMinLengthChk":
                //transferRequest.getData().getInitiation().getCreditorAccount().setIdentification("123456789012345");
                transferRequest.getData().getInitiation().getCreditorAgent().setIdentification(RandomDataGenerator.generateRandomAlphanumericUpperCase(7));
                break;
            case "InvalidBenAddress1MaxLengthChk":
                transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine1(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
            case "InvalidBenAddress2MaxLengthChk":
                transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
            case "InvalidBenAddress3MaxLengthChk":
                transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine3(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
            case "InvalidRemarksMaxLengthChk":
                transferRequest.getData().getInitiation().getRemittanceInformation().setUnstructured(RandomDataGenerator.generateRandomAlphanumericUpperCase(36));
                break;
        }

        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber);


        THEN("Error response 400 is returned for bad request with the validation error");


        final OBErrorResponse1 paymentResponse =
                internationalTransferApiFlows.internationalPaymentError(alphaTestUser, transferRequest, 400);
        AND("Actual Error Message displayed in Response " + paymentResponse.getMessage());
        AND("Actual Error Code displayed in Response " + paymentResponse.getCode());
        DONE();
    }
    /*#######################################################
      TestCaseID:AHBDB-14469
      Description:AHBDB-14469:[POST internal/v1/international-payment-consents] Negative flow 400 Response code: SecondaryIdentification: field is mandatory in case of England or Ireland, India, Australia, Canada
      CreatedBy:Shilpi Agrawal
      UpdatedBy:
      LastUpdatedOn:
      Comments:Commented Test due to Defects-
      AHBDB-15027	ABle to do International payment Consent without entering CreditorAccount.SecondaryIdentiffication that signifies SORT CODE/IFSC Code/BSB Code/Transit number
      https://ahbdigitalbank.atlassian.net/browse/AHBDB-15368
   #######################################################*/
    @ParameterizedTest
    @CsvSource({
            "10.00,'',ICICINBB012,IN, INR, , BORNE_BY_CREDITOR, CHC-Charitable",
            "1, '',ROYCCAT2VIC,CA, CAD, , SHARED, EMI-Equated",
            "1, IE64IRCE92050112345678,'IRCEIE2D',IE, EUR, , BORNE_BY_DEBTOR, OAT-Own Account",
            "2,AT483200000012345864,'RLNWATWW',AU,AUD,,BORNE_BY_DEBTOR, RNT-Rent Payments",
            "4,'','IIIGGB22',GB,GBP,,BORNE_BY_DEBTOR, EDU-purpose",
            "4,'GB33BUKB20201555555555','BUKBGB22',GB,GBP,,BORNE_BY_DEBTOR, EDU-purpose"
    })

    public void negative_international_MandatoryCheckforSecondaryIdentification_create_consent(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment){
        TEST("AHBDB-8621 / AHBDB-8623 - Validate International Transfer - International payment consent ");

        TEST("AHBDB-14469:[POST internal/v1/international-payment-consents] Negative flow 400 Response code: SecondaryIdentification: field is mandatory in case of England or Ireland, India, Australia, Canada");
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();


        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);

        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSchemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification(temenosConfig.getCreditorAccountId());
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        THEN("Then the payment requested is submitted and a 400 response is returned");


        final OBErrorResponse1 consentResponse =
                this.internationalTransferApiFlows.internationalPaymentConsentError(alphaTestUser, internationPaymentConsentRequest, 400);

        AND("Actual Error Message displayed in Response " + consentResponse.getMessage());
        AND("Actual Error Code displayed in Response " + consentResponse.getCode());

        DONE();
    }
    /*#######################################################
      TestCaseID:AHBDB-15468
      Description:[POST internal/v1/international-payments] Negative flow 400 Response code: "SecondaryIdentification": field is mandatory in case of England or Ireland, India, Australia, Canada
      CreatedBy:Shilpi Agrawal
      UpdatedBy:
      LastUpdatedOn:
      Comments:Defects-
      AHBDB-15027	ABle to do International payment Consent without entering CreditorAccount.SecondaryIdentiffication that signifies SORT CODE/IFSC Code/BSB Code/Transit number
      https://ahbdigitalbank.atlassian.net/browse/AHBDB-15368
   #######################################################*/
    @ParameterizedTest

    @CsvSource({
           "10.00,'',ICICINBB012,IN, INR, 'ICI0000012', BORNE_BY_CREDITOR, CHC-Charitable",
            "1, '',ROYCCAT2VIC,CA, CAD, 010, SHARED, EMI-Equated",
            "1, IE64IRCE92050112345678,'IRCEIE2D',IE, EUR, 981234, BORNE_BY_DEBTOR, OAT-Own Account",
            "2,AT483200000012345864,'RLNWATWW',AU,AUD,032732,BORNE_BY_DEBTOR, RNT-Rent Payments",
            "4,'','IIIGGB22',GB,GBP,200415,BORNE_BY_DEBTOR, EDU-purpose",
            "4,'GB33BUKB20201555555555','BUKBGB22',GB,GBP,202678,BORNE_BY_DEBTOR, EDU-purpose"

    })

    public void negative_international_MandatoryCheckforSecondaryIdentification_create_Payment(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment){
        TEST("AHBDB-8621 / AHBDB-8625 - Make International Transfer request ");

        TEST("AHBDB-15468:[POST internal/v1/international-payment-consents] Negative flow 400 Response code: SecondaryIdentification: field is mandatory in case of England or Ireland, India, Australia, Canada");
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();


        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);
        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSchemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setIdentification(temenosConfig.getCreditorAccountId());
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");

       final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created and Secondary Identification as blank or null");
        final OBWriteInternational3 transferRequest = internationalTransferRequestDataValid(consentResponse.getData().getConsentId(), iBan);
        transferRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        transferRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        transferRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        transferRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        transferRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        transferRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(null);
        transferRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        transferRequest.getData().getInitiation().setEndToEndIdentification(consentResponse.getData().getInitiation().getEndToEndIdentification());
        transferRequest.getData().getInitiation().getCreditorAccount().setSchemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf("ACCOUNT_NUMBER"));
        transferRequest.getData().getInitiation().getCreditorAccount().setIdentification(temenosConfig.getCreditorAccountId());
        transferRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);

        THEN("Then the payment requested is submitted and a 400 response is returned");

        final OBErrorResponse1 paymentResponse =
                internationalTransferApiFlows.internationalPaymentError(alphaTestUser, transferRequest, 400);
                AND("Actual Error Message displayed in Response " + paymentResponse.getMessage());
        AND("Actual Error Code displayed in Response " + paymentResponse.getCode());



        DONE();
    }
    /*#######################################################
   TestCaseID:AHBDB-14466
   Description:[POST internal/v1/international-payment-consents] Negative flow 404 Response code: Destination IBAN not found
   CreatedBy:Shilpi Agrawal
   UpdatedBy:
   LastUpdatedOn:
   Comments:
#######################################################*/
    @ParameterizedTest
    @CsvSource({
            "1, 'IM64IRCE92050112345678','IRCEIE2D',IE, EUR, 981234, SHARED, EMI-Equated monthly instalments"
    })


    public void negative_international_create_consent_404(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment) {
        TEST("AHBDB-8623 / AHBDB-8623 - valid international consent with valid reference");
        TEST("AHBDB-14466: [POST internal/v1/international-payment-consents] Negative flow 404 Response code: Destination IBAN not found With Following Test Data" + " Payment Amount" + paymentAmount + " IBan " + iBan + " charge Bearer Type " + chargeBearerType + "pupose Of payment " + puposeOfpayment);
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);
        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);

        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        THEN("Error response 404  is returned for bad request with the validation error that is Invalid IBAN Number" );

        final OBErrorResponse1 consentResponse =
                this.internationalTransferApiFlows.internationalPaymentConsentError(alphaTestUser, internationPaymentConsentRequest, 404);

        AND("Actual Error Message displayed in Response " + consentResponse.getMessage());
        AND("Actual Error Code displayed in Response " + consentResponse.getCode());
        assertEquals("Invalid IBAN Number", consentResponse.getMessage());

        DONE();
    }
    /*#######################################################
  TestCaseID:AHBDB-14468
  Description:[POST internal/v1/international-payment-consents] Negative flow 422 Response code: Insufficient funds in Source Account
  CreatedBy:Shilpi Agrawal
  UpdatedBy:
  LastUpdatedOn:
  Comments:
#######################################################*/
    @ParameterizedTest
    @CsvSource({
            "'IE64IRCE92050112345678','IRCEIE2D',IE, EUR, 981234, SHARED, EMI-Equated monthly instalments"})

    public void negative_international_create_consent_422( String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment) {
        TEST("AHBDB-8623 / AHBDB-8623 - valid international consent with valid reference");
        TEST("AHBDB-14468 -[POST internal/v1/international-payment-consents] Negative flow 422 Response code: Insufficient funds in Source Account',422,'UAE.PAYMENTS.INSUFFICIENT_FUNDS With Following Test Data  IBan " + iBan + " charge Bearer Type " + chargeBearerType + "pupose Of payment " + puposeOfpayment);
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("I create the international transfer consent payload");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        double paymentAmount = Double.valueOf(interimAvailable)+1.00;
        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);

        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(BigDecimal.valueOf(paymentAmount));
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);

        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber +"and paying more than available balance "+paymentAmount);

        beneStepUpAuthBiometrics();

        THEN("Error response 422 is returned for bad request with the validation error that is  'You do not have sufficient balance in account'" );

        final OBErrorResponse1 consentResponse =
                this.internationalTransferApiFlows.internationalPaymentConsentError(alphaTestUser, internationPaymentConsentRequest, 422);

        AND("Actual Error Message displayed in Response " + consentResponse.getMessage());
        AND("Actual Error Code displayed in Response " + consentResponse.getCode());

        assertEquals("UAE.PAYMENTS.INSUFFICIENT_FUNDS", consentResponse.getCode());

        DONE();
    }

    /*#######################################################
   TestCaseID:AHBDB-14467
   Description:[POST internal/v1/international-payment-consents] Negative flow 403 Response code: Source Account does not belong to logged in user
   CreatedBy:Shilpi Agrawal
   UpdatedBy:
   LastUpdatedOn:
   Comments:
#######################################################*/
    @Test

    public void negative_international_create_consent_WithInvalidAct() {
        TEST("AHBDB-8621 / AHBDB-8623 - valid international consent with valid reference");
        TEST("AHBDB-14467 - [POST internal/v1/international-payment-consents] Negative flow 403 Response code: Source Account does not belong to logged in user");
        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount("");


        internationPaymentConsentRequest.getData().getInitiation().getDebtorAccount().setIdentification(RandomStringUtils.randomAlphanumeric(12));


        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        THEN("Error response 404 is returned for invalid account request with the validation error");

        final OBErrorResponse1 consentResponse =
                this.internationalTransferApiFlows.internationalPaymentConsentError(alphaTestUser, internationPaymentConsentRequest, 403);

        AND("Actual Error Message displayed in Response " + consentResponse.getMessage());
        AND("Actual Error Code displayed in Response " + consentResponse.getCode());

        DONE();
    }


    /*#######################################################
       TestCaseID:AHBDB-15297
       Description:	[POST internal/v1/international-payments] Positive flow 200 Response code when user provides a valid test data and with optional parameters
       CreatedBy:Shilpi Agrawal
       UpdatedBy:
       LastUpdatedOn:
       Comments:
    #######################################################*/
    @ParameterizedTest

    @CsvSource({
            "10.00,'',ICICINBB012,IN, INR, 'ICI0000012', BORNE_BY_CREDITOR, CHC-Charitable contributions",
           "1, '',ROYCCAT2VIC,CA, CAD, 010, SHARED, EMI-Equated monthly instalments",
            "1, IE64IRCE92050112345678,'IRCEIE2D',IE, EUR, 981234, BORNE_BY_DEBTOR, OAT-Own Account Transfer",
            "2,AT483200000012345864,'RLNWATWW',AU,AUD,032732,BORNE_BY_DEBTOR, RNT-Rent Payments",
            "3,JO71CBJO0000000000001234567890,'CBJOJOAX',JO,JOD,'',BORNE_BY_DEBTOR, EMI-Equated monthly instalments",
            "4,'','IIIGGB22',GB,GBP,200415,BORNE_BY_DEBTOR, EDU-purpose",
            "4,'GB33BUKB20201555555555','BUKBGB22',GB,GBP,202678,BORNE_BY_DEBTOR, EDU-Educational Support",
            "5,'','IZZBOMRU',OM,OMR,'',BORNE_BY_DEBTOR, EDU-Educational Support",
            "6,'','IBOCUS44',US,USD,'',BORNE_BY_DEBTOR, RNT-Rent Payments"
    })


    public void valid_international_payment_with_optionalParameters(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment) {
        TEST("AHBDB-8621 / AHBDB-8625 - Make International Transfer request ");
        TEST("AHBDB-15297 - [POST internal/v1/international-payments] Positive flow 200 Response code when user provides a valid test data and with optional parameters With Following Test Data" + " Payment Amount" + paymentAmount + " IBan " + iBan + "swift Code as"+swiftCode+"currency Code as "+currencyCode+"Currency Code as "+currencyCode+"Secondary Identification(SortCode/BSB/TransitCode/IFSC) as"+scIfscBsbTransitNum+" charge Bearer Type " + chargeBearerType + "pupose Of payment " + puposeOfpayment);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimAvailable = balanceResponse.getData().getBalance().get(0).getAmount().getAmount().replace(".00", "");
        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);

        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());


        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = internationalTransferRequestDataValid(consentResponse.getData().getConsentId(), iBan);
        transferRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        transferRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        transferRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        transferRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        transferRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        transferRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        transferRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        transferRequest.getData().getInitiation().setEndToEndIdentification(consentResponse.getData().getInitiation().getEndToEndIdentification());
        /** TODO Need to Make Random */
        transferRequest.getData().getInitiation().getSupplementaryData().setDebitCurrency("AED");
        transferRequest.getData().getInitiation().getSupplementaryData().setBeneficiaryNickName(RandomDataGenerator.generateEnglishRandomString(5));
        transferRequest.getData().getInitiation().getSupplementaryData().setOrgRefNumber(RandomDataGenerator.generateRandomNumeric(7));
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2(RandomDataGenerator.generateRandomAddressLine());
        transferRequest.getData().getInitiation().getRemittanceInformation().setUnstructured(RandomDataGenerator.generateEnglishRandomString(15));
        transferRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);

        assertEquals(OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED,paymentResponse.getData().getStatus());

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);


        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());

        DONE();
    }

    /*#######################################################
       TestCaseID:AHBDB-15298
       Description:	[POST internal/v1/international-payments] Positive flow 200 Response code and user should be able to do international payment when user provides a valid data without optional parameters also
       CreatedBy:Shilpi Agrawal
       UpdatedBy:
       LastUpdatedOn:
       Comments:Due to defect Can not pass full description in Purpose
       AHBDB-15316-

    #######################################################*/
    @ParameterizedTest
    //Test Data for Sanctioned country
    /*@CsvSource({
            "10.00,'IR710570029971601460641001',BKBPIRTH,IR, AED, '', BORNE_BY_CREDITOR, CHC-Charitable",
            "11.00,'',BOELKPP1XXX,KP, AED, '', BORNE_BY_CREDITOR, CHC-Charitable",
            "1, '','ARBXSYDA',SY, AED,, BORNE_BY_DEBTOR, OAT-Own Account",
            "4,'','BCCUCUHHSLB',CU,AED,,BORNE_BY_DEBTOR, EDU-purpose"
    })*/

    @CsvSource({
            "10.00,'',ICICINBB012,IN, INR, 'ICI0000012', BORNE_BY_CREDITOR, CHC-Charitable contributions",
            "1, '',ROYCCAT2VIC,CA, CAD, 010, SHARED, EMI-Equated monthly instalments",
            "1, IE64IRCE92050112345678,'IRCEIE2D',IE, EUR, 981234, BORNE_BY_DEBTOR, OAT-Own Account Transfer",
            "2,AT483200000012345864,'RLNWATWW',AU,AUD,032732,BORNE_BY_DEBTOR, RNT-Rent Payments",
            "3,JO71CBJO0000000000001234567890,'CBJOJOAX',JO,JOD,'',BORNE_BY_DEBTOR, RNT-Rent Payments",
            "4,'','IIIGGB22',GB,GBP,200415,BORNE_BY_DEBTOR, EDU-Educational Support",
            "4,GB33BUKB20201555555555,'BUKBGB22',GB,GBP,202678,BORNE_BY_DEBTOR, EDU-Educational Support",
            "5,'','IZZBOMRU',OM,OMR,'',BORNE_BY_DEBTOR, EDU-purpose",
            "6,'','IBOCUS44',US,USD,'',BORNE_BY_DEBTOR, RNT-Rent Payments"
    })

    public void valid_international_payment_without_optionalParameters(BigDecimal paymentAmount, String iBan, String swiftCode, String countryCode, String currencyCode, String scIfscBsbTransitNum, String chargeBearerType, String puposeOfpayment) {
        TEST("AHBDB-8621 / AHBDB-8625 - Make International Transfer request:: ");
        TEST("AHBDB-15298 - [POST internal/v1/international-payments] Positive flow 200 Response code and user should be able to do international payment when user provides a valid data without optional parameters also With Following Test Data" + " Payment Amount" + paymentAmount + " IBan " + iBan + "swift Code as"+swiftCode+"currency Code as "+currencyCode+"Currency Code as "+currencyCode+"Secondary Identification(SortCode/BSB/TransitCode/IFSC) as"+scIfscBsbTransitNum+" charge Bearer Type " + chargeBearerType + "pupose Of payment " + puposeOfpayment);

        GIVEN("I have a valid access token and account scope and bank account");
        createNewUser();

        AND("The user has a balance");
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        assertNotNull(balanceResponse);

        String interimBooked = balanceResponse.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");


        AND("I create the international transfer consent payload");

        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount(iBan);
        internationPaymentConsentRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        internationPaymentConsentRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        internationPaymentConsentRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        //internationPaymentConsentRequest.getData().getInitiation().getRemittanceInformation().setUnstructured(null);
        internationPaymentConsentRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);
        WHEN("Funds transfers from T24 funds account :" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        THEN("I submit the valid payment consent request and the service returns a 201");
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());

        WHEN("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = internationalTransferRequestDataValid(consentResponse.getData().getConsentId(), iBan);
        transferRequest.getData().getInitiation().setChargeBearer(OBChargeBearerType1Code.valueOf(chargeBearerType));
        transferRequest.getData().getInitiation().getCreditorAgent().setIdentification(swiftCode);
        transferRequest.getData().getInitiation().setDestinationCountryCode(countryCode);
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setCountry(countryCode);
        transferRequest.getData().getInitiation().getInstructedAmount().setAmount(paymentAmount);
        transferRequest.getData().getInitiation().getInstructedAmount().setCurrency(currencyCode);
        transferRequest.getData().getInitiation().getCreditorAccount().setSecondaryIdentification(scIfscBsbTransitNum);
        transferRequest.getData().getInitiation().getRemittanceInformation().setReference(puposeOfpayment);
        transferRequest.getData().getInitiation().setEndToEndIdentification(consentResponse.getData().getInitiation().getEndToEndIdentification());
        transferRequest.getData().getInitiation().getSupplementaryData().setDebitCurrency("");
        transferRequest.getData().getInitiation().getSupplementaryData().setBeneficiaryNickName("");
        transferRequest.getData().getInitiation().getSupplementaryData().setOrgRefNumber("");
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2("");
        transferRequest.getData().getInitiation().getCreditor().getPostalAddress().setAddressLine2("");
        transferRequest.getData().getInitiation().getRemittanceInformation().setUnstructured("");
        transferRequest.getData().getInitiation().getCreditorAgent().getPostalAddress().setCountry(countryCode);


        THEN("Then the payment requested is submitted and a 201 response is returned");
        final OBWriteInternationalResponse5 paymentResponse =
                internationalTransferApiFlows.executeInternationalPayment(alphaTestUser, transferRequest);

        assertEquals(OBWriteInternationalResponse6Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED,paymentResponse.getData().getStatus());

        AND("The users balance is reduced");
        OBReadBalance1 balanceAfterPayment = this.accountApi.getAccountBalances(alphaTestUser,
                alphaTestUser.getAccountNumber());
        Assertions.assertNotNull(balanceResponse);


        String interimBookedAfterPayment = balanceAfterPayment.getData().getBalance().get(1).getAmount().getAmount().replace(".00", "");

        Assertions.assertNotEquals(interimBooked, interimBookedAfterPayment);

        AND("The users transaction list contains the transactions");
        OBReadTransaction6 response = this.accountApi.getTransactionDetailsForAccountV2(alphaTestUser,
                alphaTestUser.getAccountNumber());

        DONE();
    }
    /*#######################################################
      TestCaseID:AHBDB-14472
      Description:[POST internal/v1/international-payment-consents] Negative flow 401 Response code: In case of Disallowed or missing customer tokens
      CreatedBy:Shilpi Agrawal
      UpdatedBy:
      LastUpdatedOn:
      Comments:Commented Test As after running it All Testcases Getting failed
      #######################################################*/
 /** TODO Need to explore how to include in incorporate it in BuildKite */
    @Order(900)
    //@Test
    public void negative_test_international_paymentConsent_has_invalid_token() {
        TEST("AHBDB-8621 / AHBDB-8623 - valid international consent with valid reference : ");
        TEST("AHBDB-14472 -[POST internal/v1/international-payment-consents] Negative flow 401 Response code: In case of Disallowed or missing customer tokens");
        GIVEN("I have a valid customer with an invalid token");

        createNewUser();

        WHEN("Post call is initiated to API endpoint ' /internal/v1/utility-bills/payments with invalid inputs' with invalid bearer token");
        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount("");


        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber);

        beneStepUpAuthBiometrics();
        String Jws = alphaKeyService.validateCertForPayments(alphaTestUser, internationPaymentConsentRequest.toString());

        this.alphaTestUser.getLoginResponse().setAccessToken("Invalid");
        THEN("Error response 401 is returned for bad request with the validation error");
        this.internationalTransferApiFlows.internationalPaymentConsentErrorWithoutJws(this.alphaTestUser, internationPaymentConsentRequest, Jws, 401);

        DONE();
    }

    /*#######################################################
      TestCaseID:AHBDB-15393
      Description:Negative flow 401 Response code: In case of Disallowed or invalid Jwt tokens is passed in Payment consent
      CreatedBy:Shilpi Agrawal
      UpdatedBy:
      LastUpdatedOn:
      Comments:
      #######################################################*/

    @Test
    @Tag("US:AHBDB-12654")
    public void negative_test_international_paymentConsent_has_invalid_jwt() {
        TEST("AHBDB-12654 - Enforce JWS Signature on Payment endpoints");
        TEST("AAHBDB-15393 -Negative flow 401 Response code: In case of Disallowed or invalid Jwt tokens is passed in Payment consent");
        GIVEN("I have a valid customer with an invalid token");

        createNewUser();

        WHEN("Post call is initiated to API endpoint ' /internal/v1/utility-bills/payments with invalid inputs' with invalid bearer token");
        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount("");


        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber);

        beneStepUpAuthBiometrics();

        AND("JWS is passed as blank");
        String Jws = "";

        THEN("Error response 401 is returned for bad request with the validation error");
        this.internationalTransferApiFlows.internationalPaymentConsentErrorWithoutJws(this.alphaTestUser, internationPaymentConsentRequest, Jws, 401);

        DONE();
    }

    /*#######################################################
         TestCaseID:AHBDB-15394
         Description:Negative flow 401 Response code: In case of Disallowed or invalid Jwt tokens is passed in international Payment Request
         CreatedBy:Shilpi Agrawal
         UpdatedBy:
         LastUpdatedOn:
         Comments:
         #######################################################*/

    @Test
    @Tag("US:AHBDB-12654")
    public void negative_test_international_payment_has_invalid_jwt() {
        TEST("AHBDB-12654 - Enforce JWS Signature on Payment endpoints");
        TEST("AHBDB-15394-Negative flow 401 Response code: In case of Disallowed or invalid Jwt tokens is passed in international Payment Request");
        GIVEN("I have a valid customer with an invalid token");

        createNewUser();

        WHEN("Post call is initiated to API endpoint ' /internal/v1/utility-bills/payments with invalid inputs' with invalid bearer token");
        OBWriteInternationalConsent5 internationPaymentConsentRequest = internationalPaymentConsentRequestDataValidWithAccount("");


        WHEN("the client attempts a call to the payment consent service:" + debtorAccountNumber);

        beneStepUpAuthBiometrics();
        final OBWriteInternationalConsentResponse6 consentResponse =
                this.internationalTransferApiFlows.createInternationalPaymentConsent(alphaTestUser, internationPaymentConsentRequest);
        assertNotNull(consentResponse.getData().getConsentId());


        AND("The matching payment requested is created");
        final OBWriteInternational3 transferRequest = internationalTransferRequestDataValid(consentResponse.getData().getConsentId(), "");
        transferRequest.getData().getInitiation().setEndToEndIdentification(consentResponse.getData().getInitiation().getEndToEndIdentification());
        AND("JWS is passed as blank");
        String Jws = "";

        THEN("Error response 401 is returned for bad request with the validation error");
        this.internationalTransferApiFlows.internationalPaymentErrorWithoutJws(this.alphaTestUser, transferRequest, Jws, 401);

        DONE();
    }

}
