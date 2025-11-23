package uk.co.deloitte.banking.payments.utilitypayments.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
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
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.utilitypayments.api.UtilityPaymentsApiFlows;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KidsStepUpPaymentsTests {

    @Inject
    private UtilityPaymentsApiFlows utilityPaymentsApiFlows;
    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;
    @Inject
    private EnvUtils envUtils;
    @Inject
    private AuthenticateApiV2 authenticateApi;
    @Inject
    private AccountApi accountApi;
    @Inject
    private RelationshipApi relationshipApi;
    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;
    private AlphaTestUser alphaTestUserMother;
    private AlphaTestUser alphaTestUserChild;


    private static final int loginMinWeightExpectedBio = 31;
    static String relationshipId;
    private String dependantId = "";



    private void setupFatherTestUser() {
       // envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT ,Environments.NFT);
        if (this.alphaTestUserMother == null) {
            this.alphaTestUserMother = new AlphaTestUser();
            this.alphaTestUserMother.setGender(OBGender.MALE);
            this.alphaTestUserMother = alphaTestUserFactory.setupCustomer(alphaTestUserMother);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserMother);
            createUserRelationship(alphaTestUserMother);
            createDependentCustomer(alphaTestUserMother, OBRelationshipRole.FATHER);

        }
    }

    private void setupUserChild() {
        envUtils.ignoreTestInEnv(Environments.SIT, Environments.SIT ,Environments.NFT);
        if (this.alphaTestUserChild == null) {
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild.setGender(OBGender.FEMALE);
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserMother, alphaTestUserChild, relationshipId, dependantId);
            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserMother, relationshipId);
            OBWriteAccountResponse1 savings = accountApi.createAccount(alphaTestUserChild,
                    OBExternalAccountType1Code.AHB_YOUTH_SAV, OBExternalAccountSubType1Code.SAVINGS);
            Assertions.assertNotNull(savings);
            OBWriteAccountResponse1Data data = savings.getData();
            Assertions.assertNotNull(data);
            Assertions.assertNotNull(data.getAccountId());
            alphaTestUserChild.setAccountNumber(data.getAccountId());
        }
    }


    private void createUserRelationship(AlphaTestUser alphaTestUser) {
        UserRelationshipWriteRequest request = UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getUserId());
        dependantId = response.getUserId();
    }
    private void createDependentCustomer(AlphaTestUser alphaTestUser, OBRelationshipRole obRelationshipRole) {
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("dependent full name")
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(obRelationshipRole)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();

    }

    private void stepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUserChild, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUserChild.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUserChild, stepUpAuthValidationRequest);
    }


    @Test
    public void positive_case_validate_kids_step_up() {
        TEST("AHBDB-15741- validate kids step up for utility payment");

        GIVEN("I have a valid test user set up with a dependent relationship of mother");
        setupFatherTestUser();
        AND("I have a child who has been onboarded by the mother");
        setupUserChild();
        GIVEN("I have a valid access token and account scope");
        GIVEN("I have a valid access token and account scope and bank account");

        THEN("The client kids tries to validate step for their payment after doing StepUp Authentication");
        stepUpAuthBiometrics();

        THEN("The client kid submits the step up request and receives a 200 response");
        Boolean response = this.utilityPaymentsApiFlows.validateKidsStepUp(alphaTestUserChild);
        assertNotNull(response);
        assertEquals(Boolean.TRUE, response);

        DONE();
    }

}
