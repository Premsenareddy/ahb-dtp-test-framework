package uk.co.deloitte.banking.payments.serviceProviderBeneficiary.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBCashAccount50;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.config.UtilityPaymentsConfig;
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
public class GetAndDeleteBeneTests {

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

    @Inject
    private UtilityPaymentsConfig utilityPaymentsConfig;

    private AlphaTestUser alphaTestUser;

    private static final String NOT_USERS_BENE = "does not belong to User";


    private void setupTestUser() {
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
                        .name("creditor name")
                        .identification(temenosConfig.getCreditorAccountId())
                        .secondaryIdentification("secondary identification")
                        .build()
                ).build();
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }


    @Test
    @Order(1)
    public void positive_case_create_and_get_valid_service_beneficiary() {
        TEST("AHBDB-7334 / AHBDB-7642 - user can create a valid service provider beneficiary and the make a get request to retrieve it");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);

        AND("The beneficiary can be got and it matches the data that was used to create");
        ReadBeneficiaryResponse1 getBene = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);
        Assertions.assertEquals(getBene.getData().getBeneficiaryList().get(0), beneficiaryResponse1Data.getData());

        DONE();
    }

    @Test
    @Order(1)
    public void positive_valid_beneficiary_can_be_deleted_by_id() {
        TEST("AHBDB-7334 / AHBDB-7642- user can create a valid service provider beneficiary and delete it by using the created id");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);

        AND("The beneficiary can be deleted by the id");
        String beneId = beneficiaryResponse1Data.getData().getBeneficiaryId();
        this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiary(alphaTestUser, beneId, 204);

        ReadBeneficiaryResponse1 getBene = this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser);

        THEN("The beneficiary is removed from the list");
        Assertions.assertNull(getBene.getData().getBeneficiaryList());

        DONE();
    }

    @Test
    @Order(2)
    public void negative_valid_beneficiary_can_be_deleted_by_id_and_deleted_again_404() {
        TEST("AHBDB-7334 / AHBDB-7642 - user can create a valid service provider beneficiary and delete it 404 response");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);

        AND("The beneficiary can be deleted by the id with a 204");
        String beneId = beneficiaryResponse1Data.getData().getBeneficiaryId();
        this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiary(alphaTestUser, beneId, 204);

        WHEN("The user attempts to delete again and gets a 404");
        this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiary(alphaTestUser, beneId, 404);

        DONE();
    }

    @Test
    @Order(2)
    public void negative_user_tries_to_delete_another_users_beneficiary() {
        TEST("AHBDB-7334 / AHBDB-7642 - user can get and delete");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);

        AND("The user tries to retrieve another user's beneficiary");
        String beneId = utilityPaymentsConfig.getCreatedUtilityBene();
        THEN("A 400 is returned from the service");
        OBErrorResponse1 obErrorResponse1 = this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiaryError(alphaTestUser, beneId, 400);

        Assertions.assertTrue(obErrorResponse1.getMessage().contains(NOT_USERS_BENE), "Error message was not as expected, " +
                "test expected : " + NOT_USERS_BENE);

        DONE();
    }

    @Test
    @Order(101)
    public void negative_valid_beneficiary_cant_be_delete_invalid_token() {
        TEST("AHBDB-7334 / AHBDB-7642 - user can get and delete");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);

        AND("Their token is set to an invalid value");
        String token = alphaTestUser.getLoginResponse().getAccessToken();
        alphaTestUser.getLoginResponse().setAccessToken("invalid");

        AND("Then a 401 is returned from the service");
        String beneId = beneficiaryResponse1Data.getData().getBeneficiaryId();
        this.serviceProviderBeneficiaryApiFlows.deleteServiceBeneficiary(alphaTestUser, beneId, 401);

        alphaTestUser.getLoginResponse().setAccessToken(token);

        DONE();
    }

    @Test
    @Order(102)
    public void negative_valid_beneficiary_cant_be_got_invalid_token() {
        TEST("AHBDB-7334 / AHBDB-7642 - user can get and delete");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I create a service beneficiary");

        WriteBeneficiary1 writeBeneficiary1 = WriteBeneficiary1.builder().data(writeBeneficiary1DataValid())
                .build();

        THEN("The client submits the beneficiary payload and receives a 201 response");
        BeneficiaryResponse1 beneficiaryResponse1Data = this.serviceProviderBeneficiaryApiFlows.createServiceBeneficiary(alphaTestUser, writeBeneficiary1);

        AND("Their token is set to an invalid value");
        String token = alphaTestUser.getLoginResponse().getAccessToken();
        alphaTestUser.getLoginResponse().setAccessToken("invalid");

        AND("Then a 401 is returned from the service");
        this.serviceProviderBeneficiaryApiFlows.getServiceBeneficiaries(alphaTestUser, 401);

        alphaTestUser.getLoginResponse().setAccessToken(token);

        DONE();
    }

}
