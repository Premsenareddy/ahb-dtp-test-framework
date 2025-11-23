package uk.co.deloitte.banking.journey.scenarios.adult;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.api.RelationshipAccountApi;
import uk.co.deloitte.banking.banking.payment.api.PaymentProtectedApi;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.authorization.OBCustomerAuthorization1;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBCRSData2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBTaxResidencyCountry2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentStatus;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBWriteEmploymentDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.locations.api.LocationsApiV2;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.taxresidency.api.TaxResidencyApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentRelationshipAdapterApi;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;
import uk.co.deloitte.banking.payments.transfer.domestic.api.DomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.domestic.api.ProtectedDomesticTransferApiFlows;
import uk.co.deloitte.banking.payments.transfer.internal.api.InternalTransferApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomCountryCode;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomString;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_NUMBER;

@Slf4j
@MicronautTest
@TestMethodOrder(OrderAnnotation.class)
public class AdultOnBoardingBase {


    @Inject
    protected EnvUtils envUtils;

    @Inject
    protected AuthenticateApiV2 authenticateApi;

    @Inject
    protected CustomerApiV2 customerApi;

    @Inject
    protected OtpApi otpApi;

    @Inject
    protected IdNowApi idnowApi;

    @Inject
    protected SanctionsApi sanctionsApi;

    @Inject
    protected FatcaApiV2 fatcaApiV2;

    @Inject
    protected TaxResidencyApiV2 taxResidencyApi;

    @Inject
    protected LocationsApiV2 locationsApi;

    @Inject
    protected CustomerApiV2 customerApiV2;

    @Inject
    protected DocumentRelationshipAdapterApi documentRelationshipApi;

    @Inject
    protected AlphaKeyService alphaKeyService;

    protected ObjectMapper ob = new ObjectMapper();

    @Inject
    protected BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    protected DomesticTransferApiFlows domesticTransferApiFlows;

    @Inject
    protected InternalTransferApiFlows internalTransferApiFlows;

    @Inject
    protected ProtectedDomesticTransferApiFlows protectedDomesticTransferApiFlows;

    @Inject
    protected PaymentProtectedApi paymentProtectedApi;

    @Inject
    protected TemenosConfig temenosConfig;

    @Inject
    protected AccountApi accountApi;

    @Inject
    RelationshipApi relationshipApi;

    @Inject
    protected RelationshipAccountApi relationshipAccountApi;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    @Inject
    protected CardsApiFlows cardsApiFlows;

    @Inject
    protected DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    protected CertificateProtectedApi certificateProtectedApi;

    @Inject
    protected CertificateApi certificateApi;

    @Inject
    protected BeneficiaryDataFactory beneficiaryDataFactory;

    protected static AlphaTestUser alphaTestUser = new AlphaTestUser();
    protected static boolean alphaTestUserLoadedFromFile = false;

    @Inject
    protected AlphaTestUserFactory alphaTestUserFactory;
    protected final String CREDITOR_ACCOUNT_ID = "AE850260000674711996301";


    protected void marketplace_customer_setup_success(boolean useUsersFile) {
        TEST("Create marketplace customer");

        Random random = new Random();

        if (useUsersFile) {
            this.alphaTestUser = alphaTestUserFactory.loadFromFile();
            if (alphaTestUser != null) {
                NOTE("User loaded from file");
                alphaTestUserLoadedFromFile = true;
                return;
            }
        }


        this.alphaTestUser = alphaTestUserFactory.setupV2UserAndV2Customer(new AlphaTestUser(), null);

        AND("Completes the customer profile with valid date");
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .preferredName("Test" + generateRandomString(5))
                        .dateOfBirth(LocalDate.now().minusYears(30)) // <<--T24 expects over 21
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language("en")
                        .gender(OBGender.MALE)
                        //.mobileNumber(alphaTestUser.generateUserTelephone()) <-- Andrei investigate when changing
                        // the phone number fails tests
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .address(OBPartialPostalAddress6.builder()
                                .addressLine(List.of(generateRandomString(10),
                                        generateRandomString(5)))
                                .buildingNumber("101")
                                .streetName("Street")
                                .countrySubDivision("Dubai")
                                // AE
                                .country(generateRandomCountryCode())
                                .postalCode("1234") // <<--T24 expects numbers //TODO fix
                                .build())
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .build())
                .build();

        WHEN("The client calls post on the customers endpoint");
        final OBWriteCustomerResponse1 response = this.customerApi.updateCustomer(alphaTestUser, customer, 200);
        assertNotNull(response);

        OBReadCustomer1 currentCustomer = customerApi.getCurrentCustomer(alphaTestUser);
        assertNotNull(currentCustomer);
        assertNotNull(currentCustomer.getData());
        assertNotNull(currentCustomer.getData().getCustomer().get(0).getAddress());

        WHEN("The client confirms their IDV");
        final OBWriteIdvDetailsResponse1 customerIdvDetails = customerApi.createCustomerIdvDetails(alphaTestUser);
        THEN("IDV details are persisted for the customer");
        assertNotNull(customerIdvDetails);

        OBReadCustomer1 currentCustomerAfterIdv = customerApi.getCurrentCustomer(alphaTestUser);

        WHEN("The client perform Ename checker");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .country(currentCustomer.getData().getCustomer().get(0).getCountryOfBirth())
                .dateOfBirth(currentCustomer.getData().getCustomer().get(0).getDob())
                .gender(currentCustomer.getData().getCustomer().get(0).getGender().name().toUpperCase(Locale.ROOT).substring(0, 1))
                .fullName(currentCustomer.getData().getCustomer().get(0).getFullName())
                .build();
        CustomerBlacklistResponseDTO customerBlacklistResponseDTO = this.sanctionsApi.checkBlacklistedCustomer(alphaTestUser, customerBlacklistRequestDTO);
        THEN("Response is received for that customer");
        assertNotNull(customerBlacklistResponseDTO);


        var employmentDetails1 = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName("AHB")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("36")
                .professionCode("99")
                .build();
        OBWriteEmploymentDetailsResponse1 customerEmploymentDetails =
                customerApi.createCustomerEmploymentDetails(alphaTestUser, employmentDetails1);


        assertNotNull(customerEmploymentDetails);

        var crsResponse1 =
                this.customerApi.addCRSDetails(alphaTestUser,
                        OBWriteCRS2.builder()
                                .data(OBCRSData2.builder()
                                        .taxResidencyCountry(List.of(OBTaxResidencyCountry2.builder()
                                                .country("AE")
                                                .missingTinReason("Country doesn't issue TINs to its " +
                                                        "residents")
                                                .build()))
                                        .uaeResidencyByInvestmentScheme(true)
                                        .otherResidencyJurisdictions(true)
                                        .personalIncomeTaxJurisdictions(List.of("AE", "CN"))
                                        .agreedCertification(true)
                                        .build())
                                .build());

        THEN("CRS details are persisted for the customer");
        assertNotNull(crsResponse1);

    }

    protected void reauthenticate(String expectedScope) {
        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(alphaTestUser);
        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is REGISTRATION");

        Assertions.assertEquals(expectedScope, loginResponse.getScope());
        parseLoginResponse(alphaTestUser, loginResponse);
    }

    protected String generate_customer_cif() {
        return generate_customer_cif(alphaTestUser);
    }

    protected String generate_customer_cif(AlphaTestUser alphaTestUser) {
        TEST("AHBDB-4103: Generate Customer CIF and update CustomerType");
        TEST("AHBDB-4103 - AC1 Put Customer CIF - 200 OK");

        if (alphaTestUserLoadedFromFile) {
            NOTE("Skipping as user loaded from file");
            return null;
        }


        GIVEN(" a customer exists with a valid phone number and has ");

        WHEN("the client calls get on the protected customers endpoint with a valid mobile phone number and has idv " +
                "details persisted");

        final OBReadCustomer1 currentCustomer = this.customerApi.getCurrentCustomer(alphaTestUser);
        assertNotNull(currentCustomer);

        WHEN("Customer tries to generate cif");
        final OBCustomer1 customerUpdatedWithCif =
                this.customerApi.putCustomerCif(alphaTestUser).getData().getCustomer().get(0);
        assertNotNull(customerUpdatedWithCif);


        assertEquals(customerUpdatedWithCif.getCustomerType(), OBCustomerType1.BANKING);
        assertNotNull(customerUpdatedWithCif.getCif());

        DONE();

        return customerUpdatedWithCif.getCif();
    }

    protected void create_current_account() {
        if (alphaTestUserLoadedFromFile) {
            NOTE("Skipping as user loaded from file");
            return;
        }
        OBWriteAccountResponse1 savings = accountApi.createCustomerCurrentAccount(alphaTestUser);

        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data);
        assertNotNull(data.getAccountId());

        alphaTestUser.setCurrentAccountNumber(data.getAccountId());
    }


    protected void create_account() {

        if (alphaTestUserLoadedFromFile) {
            NOTE("Skipping as user loaded from file");
            return;
        }


        TEST("AHBDB-8108: Support new states for account creation");

        GIVEN("Status is account creation in progress");
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.ACCOUNT_CREATION_IN_PROGRESS)
                        .build())
                .build();


        WHEN("The client calls post on the customers endpoint");
        final OBWriteCustomerResponse1 response = this.customerApi.updateCustomer(alphaTestUser, customer, 200);
        assertNotNull(response);




        OBWriteAccountResponse1 savings = accountApi.createCustomerSavingsAccount(alphaTestUser);

        assertNotNull(savings);
        OBWriteAccountResponse1Data data = savings.getData();
        assertNotNull(data);
        assertNotNull(data.getAccountId());

        alphaTestUser.setAccountNumber(data.getAccountId());


        THEN("The client calls update on the customers endpoint for state");
        final OBWriteCustomerResponse1 responseStateUpdate = this.customerApi.updateCustomer(alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.ACCOUNT_CREATED)
                        .build())
                .build(), 200);

        AND("AHBDB-12962: User has accounts");
        OBReadAccount6 accounts2 = accountApi.getAccounts(alphaTestUser);

        DONE();
    }

    protected void confirmUserAccountBalance(final String accountNumber, final BigDecimal expectedBalance, final OBBalanceType1Code balanceType) {
        WHEN("Balance request is fired");
        final OBReadBalance1DataBalance requestedBalance = getBalanceForAccountAndType(accountNumber, balanceType);

        final BigDecimal returnedBalance = new BigDecimal(requestedBalance.getAmount().getAmount());
        assertTrue(returnedBalance.compareTo(expectedBalance) == 0);
    }

    protected OBReadBalance1DataBalance getBalanceForAccountAndType(String accountNumber, OBBalanceType1Code balanceType) {
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                accountNumber);
        THEN("Status code 200 is returned");

        OBReadBalance1Data balanceResponseData = balanceResponse.getData();
        assertNotNull(balanceResponseData);

        List<OBReadBalance1DataBalance> balanceList = balanceResponseData.getBalance();
        assertNotNull(balanceList);

        balanceList.forEach(this::checkFieldAssertionsForGetUserBalancesTestSuccess);

        AND("Need to confirm expected balance is returned");
        assertEquals(3, balanceList.size());
        final OBReadBalance1DataBalance requestedBalance = balanceList.stream()
                .filter(balance -> balance.getType() == balanceType)
                .findFirst()
                .get();
        Assertions.assertNotNull(requestedBalance);
        Assertions.assertNotNull(requestedBalance.getAmount());
        return requestedBalance;
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

    protected void executeInternalTransferWithInOwnAccounts(final int amount) {
        final int AMOUNT_GREAT_THEN_UNAUTHORISED_LIMIT = amount;

        final String debtorAccountId = StringUtils.leftPad(alphaTestUser.getAccountNumber(), 12, "0");
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 12, "0");
        final String creditorAccountId = StringUtils.leftPad(alphaTestUser.getCurrentAccountNumber(), 12, "0");

        GIVEN("User has account scope");
        WHEN("Funds transfers to users from T24 funds account :" + temenosAccountId);
        WHEN("Funds transfers to users account :" + debtorAccountId);
        WHEN("Funds transfers to users account of amount :" + amount);
        //deposit money into user savings account before trigger transfer account
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, BigDecimal.valueOf(AMOUNT_GREAT_THEN_UNAUTHORISED_LIMIT));

        final OBWriteDomesticConsent4 consent4 = PaymentRequestUtils.prepareInternalConsent(
                debtorAccountId,
                ACCOUNT_NUMBER,
                creditorAccountId,
                ACCOUNT_NUMBER,
                (BigDecimal.valueOf(AMOUNT_GREAT_THEN_UNAUTHORISED_LIMIT)),
                "AED",
                "Api tester - internal " +
                        "payments",
                "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");

        THEN("Consent should be created");
        final OBWriteDomesticConsentResponse5 consentResponseSuccess =
                internalTransferApiFlows.createInternalPaymentConsentWithStatus(alphaTestUser, consent4, 201);
        Assertions.assertTrue(StringUtils.isNotBlank(consentResponseSuccess.getData().getConsentId()));

        TEST("Trigger internal transfer payment between users savings and current accounts");
        WHEN("Sufficient funds available");
        final OBWriteDomestic2 transferRequest =
                PaymentRequestUtils.prepareInternalTransferRequest(consentResponseSuccess.getData().getConsentId(),
                        debtorAccountId,
                        ACCOUNT_NUMBER,
                        creditorAccountId,
                        ACCOUNT_NUMBER,
                        (BigDecimal.valueOf(AMOUNT_GREAT_THEN_UNAUTHORISED_LIMIT)),
                        "Api tester - internal " +
                                "payments",
                        "unstructured"
                        , RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
        final OBWriteDomesticResponse5 transferResponse =
                internalTransferApiFlows.createInternalTransferPayment(alphaTestUser, transferRequest);
        THEN("Internal transfer between users savings and current accounts should be successful");
        Assertions.assertNotNull(transferResponse);
        DONE();
    }

    public void transferMoneyBackFromTestAccountToTemenosSeedAccount(final int amount) {
        final String temenosAccountId = StringUtils.leftPad(temenosConfig.getCreditorAccountId(), 12, "0");
        //deposit money into account before trigger transfer account
        accountApi.executeInternalTransfer(alphaTestUser, temenosAccountId, BigDecimal.valueOf(amount));
    }


    protected OBCustomerAuthorization1 get_customer_authz() {

        TEST("AHBDB-229 - Retrieve customer cif and auths");
        GIVEN("A customer already exists and has generated CIF ");

        WHEN("Client queries for the customers authorizations");
        OBCustomerAuthorization1 customerAuthz = customerApi.getCustomerAuthz(alphaTestUser);
        THEN("The customer authz properties are returned");
        assertNotNull(customerAuthz);


        DONE();

        return customerAuthz;
    }
}
