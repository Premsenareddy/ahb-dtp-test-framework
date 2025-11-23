package uk.co.deloitte.banking.customer.idnow.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomerId1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetEIDInformation {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private CustomerConfig customerConfig;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());
    }

    @Test
    public void retrieve_applicant_information_200_success_response() {

        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        TEST("AHBDB-1368: AC1 Retrieve Applicant information - 200 success response");
        TEST("AHBDB-3050: AC1 Retrieve Applicant information - 200 success response");
        TEST("AHBDB-4875: The GET IDNow details API giving intermittent issue");
        setupTestUser();
        TokenHolder token = idNowApi.createApplicant(this.alphaTestUser);
        this.alphaTestUser.setApplicantId(token.getApplicantId());
        this.idNowApi.setIdNowAnswer(this.alphaTestUser, "SUCCESS");

        GIVEN("A customer has completed their ID&V process");
        WHEN("The client attempts to retrieve the applicant's full IDNow result information with a valid JWT token");
        ApplicantExtractedDTO applicantDTO = this.idNowApi.getApplicantResult(this.alphaTestUser);

        THEN("The platform will return a 200 response");
        AND("The platform will return the JSON related to the user ID/ transaction ID");
        Assertions.assertEquals(applicantDTO.getIdentificationProcess().get("Id"), this.alphaTestUser.getApplicantId()
                , "Applicant ID did not match expected, expected: " + this.alphaTestUser.getApplicantId());

        DONE();
    }

    @Test
    public void customer_not_found_404_not_found() {
        TEST("AHBDB-1368: AC2 Customer not found - 404 not found");
        TEST("AHBDB-3051: AC2 Customer not found - 404 not found");
        setupTestUser();
        GIVEN("A customer has completed their ID&V process");
        WHEN("The client attempts to retrieve the applicant's full IDNow result information with a user ID / " +
                "transaction ID that does not exist");
        this.customerApi.deleteCustomer(this.alphaTestUser);
        OBErrorResponse1 error = this.idNowApi.getApplicantResultNegativeFlow(this.alphaTestUser, 404);

        THEN("The platform will return a 404 not found response");
        Assertions.assertEquals(error.getCode(), "UAE.ERROR.NOT_FOUND");

        DONE();
    }

    @Test
    public void happy_path_retrieve_customerID_with_valid_EID_DocumentNumber_that_exists_success_response_200() {
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        TEST("AHBDB-4240: AC1 Retrieve customerID with valid EID DocumentNumber that exists - Success Response 200");
        TEST("AHBDB-4706: AC1 Positive Test - Happy Path Scenario - Retrieve customerID with valid EID DocumentNumber that exists - Success Response 200");
        setupTestUser();
        GIVEN("A customer exists with a EID DocumentNumber");
        OBReadCustomerId1 customerID = this.customerApi.getCustomer(this.alphaTestUser);

        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        String documentNumber = this.customerApi.createCustomerIdvDetails(this.alphaTestUser).getData().getDocumentNumber();

        WHEN("The client attempts to get a customer using that DocumentNumber");
        OBReadCustomerId1 idList = this.customerApi.getCustomersByEid(documentNumber);

        THEN("The platform responds with a 200 and a list containing ONLY the CustomerID (BE generated UUID, not CIF)" +
                "of the customer who has that DocumentNumber");

        Assertions.assertEquals(customerID.getData().get(0).getCustomerId(), idList.getData().get(0).getCustomerId(), "Customer ID's did not match," +
                "expected " + customerID);

        DONE();
    }

    @Test
    @Tag("AHBDB-7312")
    public void happy_path_retrieve_customer_with_valid_EID_DocumentNumber_that_does_not_exist_success_response_200() {
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        TEST("AHBDB-4240: AC2 Retrieve customer with a valid EID DocumentNumber that does not exist - Success Response 200");
        TEST("AHBDB-4707: AC2 Positive Test - Happy Path Scenario - Retrieve customer with valid EID DocumentNumber that does not exist - Success Response 200");
        TEST("AHBDB-7312: AC2 Retrieve customer with valid EID DocumentNumber that does not exist - Success Response 200");
        TEST("AHBDB-11911: AC2 Positive Test - Happy Path Scenario - Retrieve customer with valid EID DocumentNumber that does not exist - Success Response 200");

        GIVEN("A customer exists with a EID DocumentNumber");
        WHEN("The client attempts to get a customer using that DocumentNumber");
        OBReadCustomerId1 idList = this.customerApi.getCustomersByEid("000-0000-0000000-0");

        THEN("The platform responds with a 200 and an empty list");
        Assertions.assertTrue(idList.getData().isEmpty(), "IdList was not blank");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123-1234-1234567-12", "asdfgdsvfdsdvfdfvgf", "2345678567876577", "!@#-!@#$-!@#$%%^-!"})
    @Tag("AHBDB-7312")
    public void retrieve_customer_with_invalid_EID_DocumentNumber_400(String invalidDocumentNumber) {
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        TEST("AHBDB-4240: AC3 Retrieve customer with invalid EID DocumentNumber - 400");
        TEST("AHBDB-4708: AC3 Negative Test - Retrieve customer with invalid EID DocumentNumber - 400");
        TEST("AHBDB-7213: AC3 Retrieve customer with invalid EID DocumentNumber - 400");
        TEST("AHBDB-11912: AC3 Negative Test - Retrieve customer with invalid EID DocumentNumber - 400");

        GIVEN("The client wants to check whether a DocumentNumber exists");
        AND("The telephone number does not satisfy the validation in the data table");
        WHEN("The client attempts to retrieve customers using that DocumentNumber");
        uk.co.deloitte.alpha.error.model.OBErrorResponse1 error = this.customerApi.getCustomersByInvalidEid(invalidDocumentNumber, 400);

        THEN("The platform responds with a 400 Bad Request");
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", error.getCode(), "Error codes do not match," +
                "expected Error Code: UAE.ERROR.BAD_REQUEST");
        Assertions.assertEquals("Document Number Invalid Field", error.getMessage(),
                "Error messages do not match");

        DONE();
    }

    @Test
    public void retrieve_customer_without_EID_DocumentNumber_403_response() {
        TEST("AHBDB-4240: EmiratesID duplication check on DTP");
        TEST("AHBDB-5063: Retrieve customer without EID DocumentNumber - 403 response");

        GIVEN("The customer does not send a DocumentNumber");
        WHEN("The client attempts to retrieve customers using that DocumentNumber");
        this.customerApi.getCustomersByInvalidEidVoidResponse("", 403);

        THEN("The platform responds with a 403 Forbidden");

        DONE();
    }

    @Test
    public void happy_path_retrieve_customerID_with_valid_EID_DocumentNumber_that_exists_IDV_REVIEW_REQUIRED_success_response_200() {
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        TEST("AHBDB-4240: AC1 Retrieve customerID with valid EID DocumentNumber that exists - Success Response 200");
        TEST("AHBDB-5207: AC1 Positive Test - Happy Path Scenario - Retrieve customerID with valid EID DocumentNumber that exists - IDV_REVIEW_REQUIRED - Success Response 200");
        setupTestUser();
        GIVEN("A customer exists with a EID DocumentNumber");
        OBReadCustomerId1 customerID = this.customerApi.getCustomer(this.alphaTestUser);

        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_REVIEW_REQUIRED).build()).build(), 200);

        String documentNumber = this.customerApi.createCustomerIdvDetails(this.alphaTestUser).getData().getDocumentNumber();

        WHEN("The client attempts to get a customer using that DocumentNumber");
        OBReadCustomerId1 idList = this.customerApi.getCustomersByEid(documentNumber);

        THEN("The platform responds with a 200 and a list containing ONLY the CustomerID (BE generated UUID, not CIF)" +
                "of the customer who has that DocumentNumber");
        Assertions.assertEquals(customerID.getData().get(0).getCustomerId(), idList.getData().get(0).getCustomerId(), "Customer ID's did not match," +
                "expected " + customerID.getData().get(0).getCustomerId());

        DONE();
    }
}
