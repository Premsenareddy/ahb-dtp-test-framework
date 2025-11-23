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
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry.UtilityInquiryRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry.UtilityInquiryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.CreditorAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.DebtorAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.DueAmount1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.InstructedAmount1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.UtilityPaymentRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.UtilityPaymentRequest1Data;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.UtilityPaymentResponse1;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KidsRelationshipUtilityPaymentsTests {


    @Inject
    private UtilityPaymentsApiFlows utilityPaymentsApiFlows;
    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;
    @Inject
    private EnvUtils envUtils;
    @Inject
    private AuthenticateApiV2 authenticateApi;
    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;
    @Inject
    private AccountApi accountApi;
    @Inject
    private RelationshipApi relationshipApi;


    private AlphaTestUser alphaTestUserFather;
    private AlphaTestUser alphaTestUserChild;

    private static final int loginMinWeightExpectedBio = 31;
    static String relationshipId;
    private String dependantId = "";

    private void setupFatherTestUser() {
        envUtils.ignoreTestInEnv(Environments.SIT, Environments.NFT , Environments.STG);
        if (this.alphaTestUserFather == null) {
            this.alphaTestUserFather = new AlphaTestUser();
            this.alphaTestUserFather = alphaTestUserFactory.setupCustomer(alphaTestUserFather);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserFather);
            createUserRelationship(alphaTestUserFather);
            createDependentCustomer(alphaTestUserFather, OBRelationshipRole.FATHER);

        }
    }

    private void setupUserChild() {
        envUtils.ignoreTestInEnv(Environments.SIT, Environments.NFT , Environments.STG);
        if (this.alphaTestUserChild == null) {
            this.alphaTestUserChild = new AlphaTestUser();
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUserFather, alphaTestUserChild, relationshipId, dependantId);

            alphaTestUserBankingCustomerFactory.setUpChildBankingCustomer(alphaTestUserChild, alphaTestUserFather, relationshipId);
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
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(obRelationshipRole)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
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


    private UtilityInquiryRequest1 KidsUtilityInquiryRequestDataValid() {
        String reference = RandomDataGenerator.generateRandomAlphanumericUpperCase(12);
        return UtilityInquiryRequest1.builder()
                .referenceNum(reference)
                .utilityCompanyCode("02")
                .utilityAccount("0504471040")
                .utilityAccountPin("0")
                .utilityAccountType("01")
                .build();
    }

    private UtilityPaymentRequest1Data kidsUtilityPaymentRequestDataValid() {

        return UtilityPaymentRequest1Data.builder()
                .creditorAccount(CreditorAccount1.builder()
                        .identification("0504471040")
                        .accountType("01")
                        .accountPin("0")
                        .companyCode("02")
                        .build())
                .paymentMode("AC")
                .debtorAccount(DebtorAccount1.builder()
                        .identification("011255008002")
                        .currency("AED")
                        .cardNoFlag("F")
                        .build())
                .instructedAmount(InstructedAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .dueAmount(DueAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .build();
    }



    @Test
    public void positive_case_father_can_pay_service_provider_bills_for_child() {
        TEST("AHBDB-15742- kids can pay valid service provider bills");

        GIVEN("I have a valid test user set up with a dependent relationship of father");
        setupFatherTestUser();
        setupUserChild();
        GIVEN("I have a valid access token and account scope and bank account");

        AND("The customer can see that relationship");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUserFather);
        Assertions.assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(alphaTestUserFather.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());


        WHEN("I send kids the valid inquiry payload and a 200 is returned");
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUserChild, KidsUtilityInquiryRequestDataValid());
        UtilityPaymentRequest1Data request1Data = kidsUtilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());

        UtilityPaymentRequest1 kidsUtilityPaymentRequest = UtilityPaymentRequest1.builder().data(request1Data)
                .build();


        WHEN("I send father the valid relationship payment payload and a 200 is returned");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.kidsUtilityPayments(alphaTestUserFather, kidsUtilityPaymentRequest,
                relationships.getData().getRelationships().get(0).getConnectionId().toString());
        assertNotNull(utilityPaymentResponse1);
        DONE();
    }

}
