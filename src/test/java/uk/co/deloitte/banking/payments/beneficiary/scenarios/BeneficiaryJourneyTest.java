package uk.co.deloitte.banking.payments.beneficiary.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeneficiaryJourneyTest {

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private static final int loginMinWeightExpected = 31;

    private static final String INTERNATIONAL_BENEFICIARY_TYPE = "other_bank";
    private static final String SWIFT_CODE = "EBILAEAD";

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);
        }
    }

    private void beneStepUpAuthOTP() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    @Order(1)
    public void positive_case_create_valid_beneficiary_and_get_beneficiary() {
        TEST("AHBDB-350/351/370 - get beneficiary by id");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUser, createdObBeneficiary5.getBeneficiaryId()).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, GetObBeneficiary5);

        DONE();
    }

    @Test
    public void positive_case_create_valid_beneficiary_and_get_beneficiary_Intl() {
        TEST("AHBDB-7655 : create international beneficiary and verify creation by getting same beneficiary");
        envUtils.ignoreTestInEnv("Feature not deployed yet on NFT, STG", Environments.NFT, Environments.STG);

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setSwiftCode(SWIFT_CODE);
        beneficiaryData.setBeneficiaryType(INTERNATIONAL_BENEFICIARY_TYPE);

        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryInternational(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("I can get the beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUser, createdObBeneficiary5.getBeneficiaryId()).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, GetObBeneficiary5);

        DONE();
    }

    @Test
    @Order(2)
    public void positive_case_beneficiary_update_valid_nickname() {
        TEST("AHBDB-1219 / 370 - beneficiary with a valid name length is created with a 201 response");
        TEST("positive_case_beneficiary_update_valid_nickname");
        String validNickName = "validNickNameUpdated";

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneStepUpAuthOTP();
        OBBeneficiary5 obBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        AND("I have a completed step up auth");
        beneStepUpAuthOTP();

        WHEN("I update the beneficiary with a valid nickname");
        obBeneficiary5.getSupplementaryData().setNickname("validNickNameUpdated");
        OBWriteBeneficiaryResponse1 updatedBeneficiary = this.beneficiaryApiFlows.updateBeneficiary(alphaTestUser, obBeneficiary5);

        THEN("The beneficiary is successfully updated");
        Assertions.assertEquals(updatedBeneficiary.getData().getBeneficiary().get(0).getSupplementaryData().getNickname(), validNickName);
        DONE();
    }

    @Test
    @Order(3)
    public void positive_case_beneficiary_delete() {
        TEST("AHBDB-350/351/370 - delete beneficiary by id");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        AND("I have a completed step up auth");
        beneStepUpAuthOTP();
        AND("I have a valid beneficiary set up");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("The beneficiary can be deleted with a 204 response");
        this.beneficiaryApiFlows.deleteBeneficiary(alphaTestUser, createdObBeneficiary5.getBeneficiaryId(), 204);

        DONE();
    }

}
