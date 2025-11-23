package uk.co.deloitte.banking.payments.beneficiary.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;

import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateBeneficiaryDifferentCustomerStatesTest {

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private CustomerApiV2 customerApi;

    private static final Environments ALL_SKIP_ENV = Environments.NONE;

    private AlphaTestUser alphaTestUser;

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";


    private static final int otpWeightRequested = 32;

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);
        }
    }


    private void beneStepUpAuthOTP() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(otpWeightRequested).scope("accounts").build());
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(alphaTestUser.getUserId());
        assertTrue(isNotBlank(otpCO.getPassword()));
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().otp(otpCO.getPassword()).scope("accounts").weight(otpWeightRequested).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }



    @ParameterizedTest
    @ValueSource(strings = {"IDV_COMPLETED",
            "IDV_FAILED",
            "IDV_REVIEW_REQUIRED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_APPROVED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_REJECTED",
            "ACCOUNT_CREATION_MANUAL_REVIEW_IN_PROGRESS",
            "ACCOUNT_CREATION_RISK_REJECTION",
            "ACCOUNT_CREATION_IN_PROGRESS",
            "ACCOUNT_CREATED",
            "ACCOUNT_VERIFIED",
            "ACCOUNT_CREATION_BC_REVIEW_IN_PROGRESS",
            "ACCOUNT_CREATION_BC_REVIEW_APPROVED",
            "ACCOUNT_CREATION_BC_REVIEW_REJECTED",
            "ACCOUNT_CREATION_REVIEW_PARTIALLY_APPROVED",
            "ACCOUNT_CREATION_EMBOSS_NAME_SPECIFIED",
            "ACCOUNT_CREATION_CARD_DELIVERY_IN_PROGRESS",
            "SUSPENDED_UNDER_AGE"})
    public void positive_case_beneficiary_difference_valid_names(String customerState) {
        TEST("AHBDB-12340 - different customer states can create a bene");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("The platform attempts to update the customer in CRM");
        NOTE("Relates to a task for new customer states");
        OBWritePartialCustomer1 data = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.valueOf(customerState))
                        .build())
                .build();
        THEN("The platform returns a 200 OK");
        this.customerApi.updateCustomer(alphaTestUser, data, 200);
        AND("The details in CRM are persisted");
        OBReadCustomer1 getCustomer = this.customerApi.getCurrentCustomer(alphaTestUser);
        Assertions.assertEquals(OBCustomerStateV1.valueOf(customerState),
                getCustomer.getData().getCustomer().get(0).getCustomerState());

        WHEN("I create a beneficiary with an valid name");
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();

        THEN("The client tries to create their beneficiary");
        OBErrorResponse1 obErrorResponse1 = this.beneficiaryApiFlows.createBeneErrorResponse(alphaTestUser,
                beneficiaryData, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        beneStepUpAuthOTP();

        THEN("They can created their beneficiary");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryFlex(alphaTestUser,
                beneficiaryData);

        DONE();
    }

}
