package uk.co.deloitte.banking.customer.idnow.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IdNowEventsTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;

    @Inject
    private CustomerApiV2 customerApi;

    private void setupTestUser() {
        alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

        TokenHolder token = idNowApi.createApplicant(alphaTestUser);

        alphaTestUser.setApplicantId(token.getApplicantId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS", "SUCCESS_DATA_CHANGED"})
    public void happy_path_success_response(String successStatus) {
        TEST("AHBDB-221: AC1 - Success Response");
        TEST("AHBDB-2709: AC1 Positive Test - Happy Path Success Response");
        setupTestUser();
        GIVEN("IDNow has finished processing the applicant's ID");
        AND("The verification has been successful");

        WHEN("The Ident status value " + successStatus + " is returned from IDNow");
        this.idNowApi.setIdNowAnswer(alphaTestUser, successStatus);

        THEN("I will trigger an event saying IDV is completed along with the IDNow response");
        ApplicantExtractedDTO applicantDTO = this.idNowApi.getApplicantResult(this.alphaTestUser);

        assertEquals(alphaTestUser.getApplicantId(), applicantDTO.getIdentificationProcess().get("Id"));
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FRAUD_SUSPICION_PENDING", "REVIEW_PENDING", "CHECK_PENDING"})
    public void positive_test_pending_response(String successStatus) {
        TEST("AHBDB-221: AC2 - Pending Response");
        TEST("AHBDB-2710: AC2 Positive Test - Pending Response");
        TEST("AHBDB-3713: Fetch ID&V result returns null: alpha-idnow-idv-adapter/internal/v1/idvs");
        setupTestUser();

        GIVEN("IDNow has finished processing the applicant's ID");
        AND("The verification is in pending");

        WHEN("The Ident status value " + successStatus + " is returned from IDNow");
        idNowApi.setIdNowAnswer(alphaTestUser, successStatus);

        THEN("I will trigger an event saying IDV review required along with the IDNow response");
        ApplicantExtractedDTO applicantDTO = this.idNowApi.getApplicantResult(this.alphaTestUser);

        assertEquals(alphaTestUser.getApplicantId(), applicantDTO.getIdentificationProcess().get("Id"));
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CANCELED", "FRAUD_SUSPICION_CONFIRMED", "ABORTED"})
    public void negative_test_failed_response(String failedStatus) {
        TEST("AHBDB-221: AC3 - Failed Response");
        TEST("AHBDB-2711: AC3 Negative Test - Failed Response");
        setupTestUser();
        GIVEN("IDNow has finished processing the applicant's ID");
        AND("The verification has failed");

        WHEN("The Ident status value " + failedStatus + " is returned from IDNow");
        this.idNowApi.setIdNowAnswer(alphaTestUser, failedStatus);

        ApplicantExtractedDTO applicantDTO = this.idNowApi.getApplicantResult(alphaTestUser);

        THEN("I will trigger an event saying IDV has failed along with the IDNow response");
        assertEquals(alphaTestUser.getApplicantId(), applicantDTO.getIdentificationProcess().get("Id"));
        assertEquals(failedStatus, applicantDTO.getIdentificationProcess().get("Result"));

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CANCELED", "FRAUD_SUSPICION_CONFIRMED", "ABORTED", "FRAUD_SUSPICION_PENDING", "REVIEW_PENDING", "CHECK_PENDING"})
    public void positive_test_customer_state_cant_be_changed_from_success(String newWebhookResponse) {
        envUtils.ignoreTestInEnv("Not deployed in NFT and above", Environments.NFT, Environments.STG);
        TEST("AHBDB-13703: DTP: Customer state changing automatically from Account_verified to IDV_Failed");
        NOTE("Tests that the IDV webhook will be ignored when the customer status is not in a state awaiting an update");
        if (alphaTestUser2 == null) {
            alphaTestUser2 = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

            TokenHolder token = idNowApi.createApplicant(alphaTestUser2);

            alphaTestUser2.setApplicantId(token.getApplicantId());
            idNowApi.setIdNowAnswer(alphaTestUser2, "SUCCESS");
            patchCustomerWithStatus(alphaTestUser2, OBCustomerStateV1.IDV_COMPLETED); //This will be done by experience
        }

        OBCustomer1 response = customerApiV2.getCurrentCustomer(alphaTestUser2).getData().getCustomer().get(0);

        if (response.getCustomerState().toString().equals("IDV_REVIEW_REQUIRED") ||
                response.getCustomerState().toString().equals("IDV_FAILED")) {

            idNowApi.setIdNowAnswer(alphaTestUser2, "SUCCESS");
            patchCustomerWithStatus(alphaTestUser2, OBCustomerStateV1.IDV_COMPLETED); //This will be done by experience

            ApplicantExtractedDTO result = idNowApi.getApplicantResult(alphaTestUser2);

            assertEquals(alphaTestUser2.getApplicantId(), result.getIdentificationProcess().get("Id"));
            assertEquals("SUCCESS", result.getIdentificationProcess().get("Result"));
        }

        GIVEN("A customer has passed IDV");
        AND("Their results are shown as a success");

        WHEN("The IDV webhook is returned again with their details");
        idNowApi.setIdNowAnswer(alphaTestUser2, newWebhookResponse);

        THEN("The platform will not overwrite the existing results");
        ApplicantExtractedDTO result2 = idNowApi.getApplicantResult(alphaTestUser2);

        assertNotEquals(newWebhookResponse, result2.getIdentificationProcess().get("Result"),
                "A SUCCESS result was expected");
        assertEquals(alphaTestUser2.getApplicantId(), result2.getIdentificationProcess().get("Id"));
        assertEquals("SUCCESS", result2.getIdentificationProcess().get("Result"));

        OBCustomer1 response2 = customerApiV2.getCurrentCustomer(alphaTestUser2).getData().getCustomer().get(0);

        assertNotEquals(OBCustomerStateV1.IDV_FAILED.toString(), response2.getCustomerState().toString());
        assertNotEquals(OBCustomerStateV1.IDV_REVIEW_REQUIRED.toString(), response2.getCustomerState().toString());
        assertEquals(OBCustomerStateV1.IDV_COMPLETED.toString(), response2.getCustomerState().toString());

        DONE();
    }

    private void patchCustomerWithStatus(AlphaTestUser testUser, OBCustomerStateV1 state) {
        OBWritePartialCustomer1 data = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(state)
                        .build())
                .build();
        this.customerApi.updateCustomer(testUser, data, 200);
    }
}
