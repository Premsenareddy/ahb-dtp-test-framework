package uk.co.deloitte.banking.payments.beneficiary.scenarios.RelationshipBeneficiary;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadBeneficiary5;
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
import uk.co.deloitte.banking.payments.beneficiary.BeneficiaryConfig;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Kids")
public class ChildRelationshipBeneficiaryJourneyFatherJourney {

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
    private AccountApi accountApi;

    @Inject
    private BeneficiaryConfig beneficiaryConfig;

    static String relationshipId;


    private AlphaTestUser alphaTestUserFather;

    private static final int loginMinWeightExpected = 31;

    private AlphaTestUser alphaTestUserChild;

    private final static String TEMPORARY_PASSWORD = "validtestpassword";

    private String beneId = "";

    private AlphaTestUser alphaTestUserMother;

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

    private void setupUserChild() {
        envUtils.ignoreTestInEnv("AHBDB-13417", Environments.NFT);
        if (this.alphaTestUserChild == null) {
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserFather, alphaTestUserChild, relationshipId, dependantId);

            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserFather, relationshipId);
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
    public void positive_case_father_can_create_beneficiary_for_child() {
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
        OBBeneficiary5 getObBeneficiary5 = this.beneficiaryApiFlows.getRelationshipBeneficiaries(alphaTestUserFather, relationships.getData().getRelationships().get(0).getConnectionId().toString()).getData().getBeneficiary().get(0);

        beneId = getObBeneficiary5.getBeneficiaryId();
        THEN("Then the returned beneficiary matches the returned");
        Assertions.assertEquals(createdObBeneficiary5, getObBeneficiary5);

        DONE();
    }



    @Test
    @Order(2)
    public void positive_child_can_get_beneficiary_created_by_father() {

        setupUserChild();

        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserFather.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());

        AND("I have a completed step up auth to add the beneficiary");

        OBReadBeneficiary5 obReadBeneficiary5 = this.beneficiaryApiFlows.getBeneficiaryById(alphaTestUserChild, beneId);

        DONE();
    }


}
