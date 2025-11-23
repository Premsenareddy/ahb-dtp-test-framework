package uk.co.deloitte.banking.payments.beneficiary.scenarios.RelationshipBeneficiary;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
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
import uk.co.deloitte.banking.payments.beneficiary.BeneficiaryConfig;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Kids")
public class RelationshipBeneficiaryTest {

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
    private CustomerApi customerApiV1;

    @Inject
    private AccountApi accountApi;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    BeneficiaryConfig beneficiaryConfig;

    private static final int loginMinWeightExpected = 31;

    private AlphaTestUser alphaTestUserFather;

    private AlphaTestUser alphaTestUserMother;

    private AlphaTestUser alphaTestUser;

    private void setupFatherTestUser() {
        envUtils.ignoreTestInEnv("AHBDB-13417", Environments.NFT);
        if (this.alphaTestUserFather == null) {
            this.alphaTestUserFather = new AlphaTestUser();
            this.alphaTestUserFather = alphaTestUserFactory.setupCustomer(alphaTestUserFather);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserFather);
            createUserRelationship(alphaTestUserFather);
            createDependentCustomer(alphaTestUserFather, OBRelationshipRole.FATHER);

        }
    }

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

    private void setupUserNoChild() {
        envUtils.ignoreTestInEnv("AHBDB-13417", Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);

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
    }


    @Test
    @Order(1)
    public void positive_case_create_valid_beneficiary_father_external_beneficiary() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of father");
        setupFatherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserFather.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserFather);

        AND("I have a valid beneficiary set up with an external account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryType(beneficiaryConfig.getDomesticFlag());
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUserFather, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUserFather, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, GetObBeneficiary5);

        DONE();
    }

    @Test
    @Order(2)
    public void positive_case_create_valid_beneficiary_father_internal_dtp_beneficiary() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of father");
        setupFatherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserFather.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserFather);

        AND("I have a valid beneficiary set up with another internal DTP account");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUserFather, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUserFather, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        DONE();
    }

    @Test
    @Order(3)
    public void positive_case_create_valid_beneficiary_father_can_add_themselves() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of father");
        setupFatherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserFather.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserFather);

        AND("I have a valid beneficiary set up with the fathers own account");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(alphaTestUserFather.getAccountNumber());

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUserFather, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUserFather, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        DONE();
    }

    @Test
    @Order(4)
    public void positive_case_create_valid_beneficiary_mother_can_add_herself() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserMother);

        AND("I have a valid beneficiary set up with the mothers own account");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(alphaTestUserMother.getAccountNumber());

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUserMother, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUserMother, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        DONE();
    }

    @Test
    @Order(5)
    public void positive_case_create_valid_beneficiary_mother_can_add_her_second_account() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("The mother has a second bank account created");
        OBWriteAccountResponse1 current = accountApi.createCustomerCurrentAccount(alphaTestUserMother);
        Assertions.assertNotNull(current);
        String secondAccountId = current.getData().getAccountId();

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserMother);

        AND("I have a valid beneficiary set up with the mothers second account");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(secondAccountId);

        WHEN("I send the valid relationship beneficiary payload and a 201 is returned");
        OBBeneficiary5 createdObBeneficiary5 = this.beneficiaryApiFlows.createRelationshipBeneficiaryFlex(alphaTestUserMother, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        THEN("I can get the relationship beneficiary by id with a 200 response");
        OBBeneficiary5 GetObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUserMother, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);


        DONE();
    }

    @Test
    @Order(20)
    public void negative_dependent_beneficiary_mother_can_not_add_another_external_account() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserMother);

        AND("the beneficiary contains an external account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryType(beneficiaryConfig.getDomesticFlag());
        beneficiaryData.setAccountNumber("GB94BARC10201530093459");

        WHEN("I send the relationship beneficiary payload and a 403 is returned");
        this.beneficiaryApiFlows.createRelationshipBeneficiaryVoid(alphaTestUserMother, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString(), 403);


        DONE();
    }

    @Test
    @Order(21)
    public void negative_dependent_beneficiary_mother_can_not_add_another_DTP_account() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupMotherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserMother);

        WHEN("The beneficiary contains another DTP account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setAccountNumber(temenosConfig.getCreditorAccountId());

        THEN("I send the relationship beneficiary payload a 403 is returned");
        this.beneficiaryApiFlows.createRelationshipBeneficiaryVoid(alphaTestUserMother, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString(), 403);

        DONE();
    }

    @Test
    @Order(21)
    public void negative_dependent_beneficiary_parent_cannot_add_bene_someone_else_child() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with no depdent relationship");
        setupUserNoChild();

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserMother);

        AND("The beneficiary contains another DTP account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        WHEN("Another users relationship id is used");

        THEN("I send the relationship beneficiary payload a 403 is returned");
        this.beneficiaryApiFlows.createRelationshipBeneficiaryVoid(alphaTestUser, beneficiaryData, "", 403);

        DONE();
    }

    @Test
    @Order(300)
    public void negative_dependent_beneficiary_invalid_token() {
        TEST("AHBDB-7931 Manage Beneficiary APIs update to allow the parent to add beneficiaries on behalf of the kid");
        GIVEN("I have a valid test user set up with a dependent relationship of father");
        setupMotherTestUser();
        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserMother);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserMother.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");
        beneStepUpAuthOTP(alphaTestUserMother);

        AND("The beneficiary contains another DTP account number");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        AND("The users token is invalid");
        alphaTestUserMother.getLoginResponse().setAccessToken("invalid");

        WHEN("I send the relationship beneficiary payload a 401 is returned");
        this.beneficiaryApiFlows.createRelationshipBeneficiaryVoid(alphaTestUserMother, beneficiaryData, relationships.getData().getRelationships().get(0).getConnectionId().toString(), 401);

        DONE();
    }


}
