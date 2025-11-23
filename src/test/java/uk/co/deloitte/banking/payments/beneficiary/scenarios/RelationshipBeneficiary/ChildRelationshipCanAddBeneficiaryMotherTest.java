package uk.co.deloitte.banking.payments.beneficiary.scenarios.RelationshipBeneficiary;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;
import uk.co.deloitte.banking.payments.beneficiary.BeneficiaryConfig;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.REGISTRATION_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Kids")
public class ChildRelationshipCanAddBeneficiaryMotherTest {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    EnvUtils envUtils;

    private String dependantId = "";

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private PaymentConfiguration paymentConfiguration;

    @Inject
    private CustomerApi customerApiV1;

    @Inject
    BeneficiaryConfig beneficiaryConfig;

    @Inject
    private AccountApi accountApi;

    static String relationshipId;
    static String otpCode;


    private static final int loginMinWeightExpected = 31;

    private AlphaTestUser alphaTestUserChild;

    private final static String TEMPORARY_PASSWORD = "validtestpassword";

    private AlphaTestUser alphaTestUserMother;


    private void setupMotherTestUser() {
        envUtils.ignoreTestInEnv("AHBDB-13417", Environments.NFT);
        if (this.alphaTestUserMother == null) {
            this.alphaTestUserMother = new AlphaTestUser();
            this.alphaTestUserMother.setGender(OBGender.FEMALE);
            this.alphaTestUserMother = alphaTestUserFactory.setupCustomer(alphaTestUserMother);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserMother);
            createUserRelationship(alphaTestUserMother);
            createDependentCustomer(alphaTestUserMother, OBRelationshipRole.MOTHER);

        }
    }

    private void setupUserChild() {
        envUtils.ignoreTestInEnv("AHBDB-13417", Environments.NFT);
        if (this.alphaTestUserChild == null) {
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild.setGender(OBGender.FEMALE);
            OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
            NOTE("getting relationship");
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserMother, alphaTestUserChild, relationshipId, dependantId);

            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserMother, relationshipId);
            OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                    OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
            assertNotNull(savings);
            OBWriteAccountResponse1Data data = savings.getData();
            assertNotNull(data);
            assertNotNull(data.getAccountId());
            alphaTestUserChild.setAccountNumber(data.getAccountId());
        }
    }

    private void beneStepUpAuthOTP(AlphaTestUser alphaTestUser) {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpected).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpected).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }

    private void createUserRelationship(AlphaTestUser alphaTestUser) {
        UserRelationshipWriteRequest request = UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        assertNotNull(response);
        assertNotNull(response.getUserId());
        dependantId = response.getUserId();
    }

    private void createDependentCustomer(AlphaTestUser alphaTestUser, OBRelationshipRole obRelationshipRole) {
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("dependent full name")
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(obRelationshipRole)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        assertNotNull(response);
        assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();

    }


    @Test
    @Order(1)
    public void positive_get_created_valid_beneficiary_add_mother() {
        TEST("AHBDB-7518 Manage Beneficiary API update to restrict Kid onboarded by mother to only add the mother as a beneficiary");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");


        AND("I have a valid beneficiary set up with an external account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(alphaTestUserMother.getAccountNumber());
        beneficiaryData.setBeneficiaryName(alphaTestUserMother.getName());
        beneficiaryData.setMobileNumber(alphaTestUserMother.getUserTelephone());

        AND("The kid has completed step up auth");
        beneStepUpAuthOTP(alphaTestUserChild);

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUserChild, beneficiaryData).getData().getBeneficiary().get(0);

        THEN("The child can get the beneficiary by Id");
        OBBeneficiary5 getObBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUserChild, createdObBeneficiary5.getBeneficiaryId()).getData().getBeneficiary().get(0);
        Assertions.assertNotNull(getObBeneficiary5);

        DONE();
    }

    @Test
    @Order(2)
    public void negative_child_beneficiary_add_another_DTP_account() {
        TEST("AHBDB-7518 Manage Beneficiary API update to restrict Kid onboarded by mother to only add the mother as a beneficiary");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("The kid has completed step up auth");
        beneStepUpAuthOTP(alphaTestUserChild);

        AND("I have a valid beneficiary set up with another DTP account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();


        WHEN("I send the valid relationship beneficiary payload and a 403 is returned");
        this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUserChild, beneficiaryData, 403);
        DONE();
    }

    @Test
    @Order(2)
    public void negative_child_beneficiary_add_external_beneficiary() {
        TEST("AHBDB-7518 Manage Beneficiary API update to restrict Kid onboarded by mother to only add the mother as a beneficiary");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("The kid has completed step up auth");
        beneStepUpAuthOTP(alphaTestUserChild);


        AND("I have a valid beneficiary set up with external account details");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber("AE070331234567890123456");
        beneficiaryData.setBeneficiaryType(beneficiaryConfig.getDomesticFlag());

        WHEN("I send the valid relationship beneficiary payload and a 403 is returned");
        this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUserChild, beneficiaryData, 403);

        DONE();
    }

    @Test
    @Order(200)
    public void negative_child_beneficiary_add_invalid_token() {
        TEST("AHBDB-7518 Manage Beneficiary API update to restrict Kid onboarded by mother to only add the mother as a beneficiary");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());


        AND("I have a valid beneficiary set up with external account details");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        AND("The kid has completed step up auth");
        beneStepUpAuthOTP(alphaTestUserChild);

        alphaTestUserChild.getLoginResponse().setAccessToken("Invalid");

        WHEN("I send the valid relationship beneficiary payload and a 401 is returned");
        this.beneficiaryApiFlows.createBeneErrorResponseVoid(alphaTestUserChild, beneficiaryData, 401);
        DONE();
    }


}
