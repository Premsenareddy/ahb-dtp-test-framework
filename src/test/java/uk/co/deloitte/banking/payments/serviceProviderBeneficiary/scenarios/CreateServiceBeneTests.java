package uk.co.deloitte.banking.payments.serviceProviderBeneficiary.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBCashAccount50;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.BeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.ReadBeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.WriteBeneficiary1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.WriteBeneficiary1Data;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.serviceProviderBeneficiary.api.ServiceProviderBeneficiaryApiFlows;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateServiceBeneTests {

    @Inject
    private ServiceProviderBeneficiaryApiFlows serviceProviderBeneficiaryApiFlows;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private TemenosConfig temenosConfig;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        envUtils.ignoreTestInEnv( Environments.NFT);

        if (this.alphaTestUser == null) {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
    }

    private void setupTestUserNegative() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
    }

    private WriteBeneficiary1Data writeBeneficiary1DataValid() {
        return WriteBeneficiary1Data.builder()
                .serviceCode("01")
                .serviceProvider("Estisalat")
                .serviceTypeCode("04")
                .serviceType("Mobile(GSM)")
                .premiseNumber("123456789")
                .consumerPin(RandomDataGenerator.generateRandomNumeric(128))
                .phoneNumber(alphaTestUser.getUserTelephone())
                .mobileNumber(alphaTestUser.getUserTelephone())
                .creditor(OBCashAccount50.builder()
                        .schemeName("schemePlaceholder")
                        .name("Service Bene Nickname field of fifty characterssss")
                        .identification(temenosConfig.getCreditorAccountId())
                        .secondaryIdentification("secondary identification")
                        .build()
                ).build();
    }


    @Test
    @Order(1)
    public void positive_case_create_valid_service_beneficiary() {
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);


        DONE();
    }

    @BeforeEach
    void setUpTest() {
        if (alphaTestUser != null) {
            alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"du", "Abu Dhabi Distribution Company (ADDC)", "Al Ain Distribution Company (AADC)",
            "Dubai Electricity and Water Authority (DEWA)", "Sharjah Electricity and Water Authority (SEWA)", "Salik"
            , "General Charities", "Building Masjid", "Zakat"})
    public void positive_case_create_valid_service_beneficiary_serviceProvider(String serviceProvider) {
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary serviceProvider : " + serviceProvider);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceProvider(serviceProvider);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getServiceProvider(), serviceProvider);

        AND("Then a list of beneficiaries is returned the the user");
        String beneId = beneficiaryResponse1Data.getData().getBeneficiaryId();
        ReadBeneficiaryResponse1 getBene =
                this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("It contains their beneficiary");
        boolean beneCreated = getBene.getData().getBeneficiaryList().stream()
                .anyMatch(t -> t.getServiceProvider().equals(serviceProvider));
        Assertions.assertTrue(beneCreated);

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"Mobile (GSM)", "Wasel Recharge ", "E-Vision", "Landline/Elife", "Al Shamil (Broadband)",
            "Internet (Dial-up)", "Postpaid", "Prepaid - More Credit", "Prepaid - More Time", "Landline", "Broadband"
            , "TV", "ADDC", "DEWA", "SEWA", "Salik", "Charity"})
    public void positive_case_create_valid_service_beneficiary_serviceType(String serviceType) {
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary serviceType : " + serviceType);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceType(serviceType);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getServiceType(), serviceType);

        AND("Then a list of beneficiaries is returned the the user");
        ReadBeneficiaryResponse1 getBene =
                this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("It contains their beneficiary");
        boolean beneCreated = getBene.getData().getBeneficiaryList().stream()
                .anyMatch(t -> t.getServiceType().equals(serviceType));
        Assertions.assertTrue(beneCreated);

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"01", "02", "03", "04", "05", "06", "07", "08", "09"})
    public void positive_case_create_valid_service_beneficiary_serviceProviderID(String serviceProviderID) {
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary serviceProviderID : " + serviceProviderID);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceCode(serviceProviderID);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getServiceCode(), serviceProviderID);

        AND("Then a list of beneficiaries is returned the the user");
        ReadBeneficiaryResponse1 getBene =
                this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("It contains their beneficiary");
        boolean beneCreated = getBene.getData().getBeneficiaryList().stream()
                .anyMatch(t -> t.getServiceCode().equals(serviceProviderID));
        Assertions.assertTrue(beneCreated);

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "99"})
    public void positive_case_create_valid_service_beneficiary_serviceTypeID(String serviceTypeID) {
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary serviceTypeID : " + serviceTypeID);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceTypeCode(serviceTypeID);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getServiceTypeCode(), serviceTypeID);

        AND("Then a list of beneficiaries is returned the the user");
        ReadBeneficiaryResponse1 getBene =
                this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("It contains their beneficiary");
        boolean beneCreated = getBene.getData().getBeneficiaryList().stream()
                .anyMatch(t -> t.getServiceTypeCode().equals(serviceTypeID));
        Assertions.assertTrue(beneCreated);

        DONE();
    }

    @Test
    @Order(1)
    public void positive_case_create_valid_service_beneficiary_premiseNumber() {
        String premiseNumber = Integer.toString(RandomDataGenerator.generateRandomIntegerInRange(100000000, 999999999));
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary premiseNumber : " + premiseNumber);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setPremiseNumber(premiseNumber);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getPremiseNumber(), premiseNumber);

        AND("Then a list of beneficiaries is returned the the user");
        ReadBeneficiaryResponse1 getBene =
                this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("It contains their beneficiary");
        boolean beneCreated = getBene.getData().getBeneficiaryList().stream()
                .anyMatch(t -> t.getPremiseNumber().equals(premiseNumber));
        Assertions.assertTrue(beneCreated);

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(ints = {128})
    public void positive_case_create_valid_service_beneficiary_consumerPIN(int pinLength) {
        String consumerPIN = RandomDataGenerator.generateRandomNumeric(pinLength);
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary consumerPIN : " + consumerPIN);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setConsumerPin(consumerPIN);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getConsumerPin(), consumerPIN);

        DONE();
    }

    @Test
    @Order(1)
    public void positive_case_create_valid_service_beneficiary_nicknameAndAccountNumber() {
        String nickname = RandomDataGenerator.generateRandomString(50);
        String accountNumber = RandomDataGenerator.generateRandomNumeric(12);
        OBCashAccount50 creditorAccount =
                OBCashAccount50.builder().identification(accountNumber).name(nickname).build();

        TEST("AHBDB-7333 - user can create a valid service provider beneficiary nickname : " + nickname);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setCreditor(creditorAccount);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getCreditor(), creditorAccount);

        AND("Then a list of beneficiaries is returned the the user");
        ReadBeneficiaryResponse1 getBene =
                this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("It contains their beneficiary");
        boolean beneCreated = getBene.getData().getBeneficiaryList().stream()
                .anyMatch(t -> t.getCreditor().equals(creditorAccount));
        Assertions.assertTrue(beneCreated);

        DONE();
    }


    @Test
    @Order(1)
    public void positive_case_create_valid_service_beneficiary_nicknameContainsSpecialCharAndNumbers() {
        String nickname = "Sagay *&^%$£ Zakat 393";
        String accountNumber = RandomDataGenerator.generateRandomNumeric(12);
        OBCashAccount50 creditorAccount =
                OBCashAccount50.builder().identification(accountNumber).name(nickname).build();

        TEST("AHBDB-7333 - user can create a valid service provider beneficiary nickname : " + nickname);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setCreditor(creditorAccount);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        Assertions.assertEquals(beneficiaryResponse1Data.getData().getCreditor(), creditorAccount);

        AND("Then a list of beneficiaries is returned the the user");
        ReadBeneficiaryResponse1 getBene =
                this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("It contains their beneficiary");
        boolean beneCreated = getBene.getData().getBeneficiaryList().stream()
                .anyMatch(t -> t.getCreditor().equals(creditorAccount));
        Assertions.assertTrue(beneCreated);

        DONE();
    }

    @Order(1)
    @Test
    public void positive_case_create_valid_service_beneficiary_minimal_require_fields() {
        TEST("AHBDB-7333 - user can create a valid service provider beneficiary minimum required fields");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setConsumerPin(null);
        writeBeneficiary1Data.setMobileNumber(null);
        writeBeneficiary1Data.setPremiseNumber(null);
        writeBeneficiary1Data.setPhoneNumber(null);


        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data =
                this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);
        String beneId = beneficiaryResponse1Data.getData().getBeneficiaryId();

        AND("Then a list of beneficiaries is returned the the user");
        this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"", "97273084", "5557395", "555739583"})
    public void negative_case_create_invalid_service_beneficiary_phoneNumber(String invalidPhoneNumber) {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary invalid phoneNumber : " + invalidPhoneNumber);

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setPhoneNumber(invalidPhoneNumber);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"", "97273084", "5557395", "555739583"})
    public void negative_case_create_invalid_service_beneficiary_mobileNumber(String invalidMobileNumber) {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary invalid mobileNumber : " + invalidMobileNumber);

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setMobileNumber(invalidMobileNumber);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {127, 126})
    public void negative_case_create_invalid_consumerPin_length(int invalidConsumerPinLength) {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary invalid consumerPin length : " + invalidConsumerPinLength);
        String consumerPIN = RandomDataGenerator.generateRandomNumeric(invalidConsumerPinLength);
        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setConsumerPin(consumerPIN);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"97273084", "5557395121"})
    public void negative_case_create_invalid_premisesNumber_length(String invalidPremisesNumber) {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary invalid consumerPin : " + invalidPremisesNumber);

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setConsumerPin(invalidPremisesNumber);

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);

        DONE();
    }


    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"Service Bene Nickname field of more than fifty char"})
    public void negative_case_create_invalid_nickName(String invalidNickName) {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary invalid consumerPin : " + invalidNickName);

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setCreditor(OBCashAccount50.builder()
                .schemeName("schemePlaceholder")
                .name(invalidNickName)
                .identification(temenosConfig.getCreditorAccountId())
                .secondaryIdentification("secondary identification")
                .build()
        );

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);

        DONE();
    }

    @Order(2)
    @Test
    public void negative_case_create_invalid_service_beneficiary_missing_serviceProvider() {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary null serviceProvider");

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceProvider(null);


        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);
        DONE();
    }

    @Order(2)
    @Test
    public void negative_case_create_invalid_service_beneficiary_missing_serviceType() {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary null serviceType");

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceType(null);


        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);
        DONE();
    }

    @Order(2)
    @Test
    public void negative_case_create_invalid_service_beneficiary_missing_serviceCode() {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary null serviceCode");

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceCode(null);


        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);
        DONE();
    }

    @Order(2)
    @Test
    public void negative_case_create_invalid_service_beneficiary_missing_serviceTypeCode() {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary null serviceTypeCode");

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1Data writeBeneficiary1Data = writeBeneficiary1DataValid();
        writeBeneficiary1Data.setServiceTypeCode(null);


        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1Data)
                .build();

        THEN("The client submits the beneficiary payload and receives a 400 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 400);
        DONE();
    }


    @Order(102)
    @Test
    public void negative_case_missing_customer_token() {
        TEST("AHBDB-7333 - negative test user tries to create beneficiary invalid token");

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I create a service beneficiary with an invalid token");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();
        alphaTestUser.getLoginResponse().setAccessToken("");

        THEN("The client submits the beneficiary payload and receives a 401 response");
        this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiaryError(alphaTestUser, writeBeneficiary1, 401);


        DONE();
    }
}
