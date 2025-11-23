package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventV1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseTypeEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ProcessOriginEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ReasonEnum;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBEIDStatus;
import uk.co.deloitte.banking.customer.api.customer.model.OBInvalidEIDReason;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomerId1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1Data;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBIdType;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.cases.api.CasesApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.UUID;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("@BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddEIDVerificationFlagCRMTests {

    @Inject
    private CasesApi casesApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserFresh;

    private static final String NULL_REASON_WHEN_REQUIRED_ERROR = "Reason is required for invalid EID";
    private static final String INVALID_VALUES_ERROR = "Wrong values provided to update customer validations";

    public void setupTestUser() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(this.alphaTestUser);
            this.customerApi.createCustomerIdvDetails(this.alphaTestUser);
        }
    }

    public AlphaTestUser setupTestUserFresh() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        AlphaTestUser alphaTestUserToSetup = new AlphaTestUser();
        return this.alphaTestUserFactory.setupCustomer(alphaTestUserToSetup);
    }

    @Test
    public void happy_path_retrieve_customerID_200_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9464: AC1 - Client retrieves CustomerID that exists - 200 OK");

        GIVEN("The DocumentNumber of the EID is sent to experience services by Fetchr");
        AND("The DocumentNumber exists against a customerID in CRM");
        this.alphaTestUserFresh = setupTestUserFresh();

        String customerID = this.customerApi.getCustomerSuccess(this.alphaTestUserFresh)
                .getData().getCustomer().get(0).getCustomerId().toString();

        String documentNumber = this.customerApi.createCustomerIdvDetails(this.alphaTestUserFresh)
                .getData().getDocumentNumber();

        WHEN("The client attempts to get the customerID by DocumentNumber");

        OBReadCustomerId1 getResponse = this.customerApi.getCustomersByEid(documentNumber);

        THEN("The platform will return a 200 OK");
        AND("The platform will return the CustomerID that the DocumentNumber belongs to");
        Assertions.assertEquals(customerID, getResponse.getData().get(0).getCustomerId().toString());
        DONE();
    }

    @Test
    public void happy_path_retrieve_customerID_200_response_passport() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9464: AC1 - Client retrieves CustomerID that exists - 200 OK");

        GIVEN("The DocumentNumber of the EID is sent to experience services by Fetchr");
        AND("The DocumentNumber exists against a customerID in CRM");
        this.alphaTestUserFresh = setupTestUserFresh();

        WHEN("The client attempts to create the Passport fields in CRM");
        alphaTestUserFresh.setIdType(OBIdType.PASSPORT);
        OBWriteIdvDetailsResponse1 response = this.customerApi.createCustomerIdvDetails(this.alphaTestUserFresh);


        THEN("The platform will return a 200 OK");
        Assertions.assertNotNull(response.getData().getIssuingAuthorityEn());
        Assertions.assertEquals(response.getData().getIdType(), OBIdType.PASSPORT);
        DONE();
    }


    @Test
    public void happy_path_eid_does_not_exist_200_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9465: AC2 - Client retrieves empty list when using an EID that does not exist - 200 OK");

        GIVEN("The DocumentNumber of the EID is sent to experience services by Fetchr");
        AND("The DocumentNumber does not exist against a customerID in CRM");

        WHEN("The client attempts to get the customerID by DocumentNumber");

        OBReadCustomerId1 getResponse = this.customerApi.getCustomersByEid(RandomDataGenerator.generateRandomEID());

        THEN("The platform will return a 200 OK");
        AND("The platform will return an empty list");
        Assertions.assertNotNull(getResponse);
        DONE();
    }

    @Test
    public void happy_path_get_and_update_eid_status_valid_200_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9467: AC3 - Happy Path - Update EID Status to Valid and omit a reason");

        this.alphaTestUserFresh = setupTestUserFresh();
        GIVEN("The client wants to update the EID Status of a customer in CRM from Pending to Valid");

        WHEN("The client attempts to update the customer profile with a valid EidStatus against a valid CustomerID");
        AND("No InvalidEIDReason is sent");

        OBReadCustomer1Data getCustomerFirst = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();
        Assertions.assertNotNull(getCustomerFirst.getCustomer().get(0).getEidStatus());

        this.customerApi.createCustomerIdvDetails(this.alphaTestUserFresh);

        OBReadCustomer1Data customerPendingEID = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();
        String customerId = customerPendingEID.getCustomer().get(0).getCustomerId().toString();

        Assertions.assertEquals(OBEIDStatus.PENDING.toString(),
                customerPendingEID.getCustomer().get(0).getEidStatus().toString());

        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        OBWriteCustomerResponse1 obWriteCustomerResponse1 =
                this.customerApi.updateCustomerValidations(this.alphaTestUserFresh, eidStatus);

        THEN("The platform will set the EidStatus against the CustomerID sent in the path");
        AND("The platform will push an event to Kafka with the customer's EidStatus, Customer and CIF");
        AND("The platform will return a 200 OK");
        AND("The platform will return the CustomerID of the customer");

        Assertions.assertEquals(customerId, obWriteCustomerResponse1.getData().getCustomerId().toString());

        OBReadCustomer1Data customerData = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();

        Assertions.assertEquals(OBEIDStatus.VALID.toString(),
                customerData.getCustomer().get(0).getEidStatus().toString(),
                "EidStatus expected to be VALID");
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHIP_NOT_READ", "AHB_VALIDATION_FAILED", "EIDA_VALIDATION_FAILED", "OTHERS"})
    public void happy_path_get_and_update_eid_status_invalid_200_response(String reason) {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9468: AC3 - Happy Path - Update EID Status to Invalid and give a valid Reason");

        this.alphaTestUserFresh = setupTestUserFresh();
        GIVEN("The client wants to update the EID Status of a customer in CRM from Pending to Invalid");
        WHEN("The client attempts to update the customer profile with a valid EidStatus against a valid CustomerID");
        AND("A valid InvalidEIDReason is given");

        OBReadCustomer1Data getCustomerFirst = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();
        Assertions.assertNotNull(getCustomerFirst.getCustomer().get(0).getEidStatus());

        OBReadCustomer1Data customerPendingEID = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();
        String customerId = customerPendingEID.getCustomer().get(0).getCustomerId().toString();

        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.INVALID)
                        .invalidEIDReason(OBInvalidEIDReason.valueOf(reason))
                        .build())
                .build();

        OBWriteCustomerResponse1 obWriteCustomerResponse1 =
                this.customerApi.updateCustomerValidations(this.alphaTestUserFresh, eidStatus);

        THEN("The platform will set the EidStatus against the CustomerID sent in the path");
        AND("The platform will push an event to Kafka with the customer's EidStatus, Customer and CIF");
        AND("The platform will return a 200 OK");
        AND("The platform will return the CustomerID of the customer");

        Assertions.assertEquals(customerId, obWriteCustomerResponse1.getData().getCustomerId().toString());

        OBReadCustomer1Data customerData = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();

        Assertions.assertEquals(OBEIDStatus.INVALID.toString(),
                customerData.getCustomer().get(0).getEidStatus().toString(),
                "EidStatus expected to be VALID");
        DONE();
    }

    @Test
    public void negative_test_update_status_to_invalid_and_give_null_reason_400_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9469: AC4 - Update record to Invalid and give a missing reason - 400 Response");

        setupTestUser();
        GIVEN("The client wants to update the EID Status of a customer in CRM from Pending to Invalid");
        WHEN("The client attempts to update the customer profile from PENDING to INVALID");
        AND("The InvalidEIDReason is not given/is null");

        OBReadCustomer1Data getCustomerFirst = this.customerApi.getCurrentCustomer(this.alphaTestUser).getData();
        Assertions.assertNotNull(getCustomerFirst.getCustomer().get(0).getEidStatus());

        OBReadCustomer1Data customerPendingEID = this.customerApi.getCurrentCustomer(this.alphaTestUser).getData();
        String customerId = customerPendingEID.getCustomer().get(0).getCustomerId().toString();

        Assertions.assertEquals(OBEIDStatus.PENDING.toString(),
                customerPendingEID.getCustomer().get(0).getEidStatus().toString());

        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.INVALID)
                        .build())
                .build();

        OBErrorResponse1 error =
                this.customerApi.updateCustomerValidationsError(this.alphaTestUser, eidStatus, 400);

        THEN("The platform will return a 400 Bad Request");
        Assertions.assertNotNull(error);
        Assertions.assertEquals(NULL_REASON_WHEN_REQUIRED_ERROR, error.getMessage());
        DONE();
    }

    @Test
    public void negative_test_customerId_does_not_exist_update_customer_record_400_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9470: AC4 - Update record using a customer ID that does not exist");
        this.alphaTestUserFresh = setupTestUserFresh();
        GIVEN("The client wants to update the EID Status of a customer in CRM");
        WHEN("The client attempts to update the customer profile with an invalid or missing EIDStatus or CustomerId");

        this.alphaTestUserFresh.setUserId(UUID.randomUUID().toString());
        this.alphaTestUserFresh.setCustomerId(UUID.randomUUID().toString());

        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        OBErrorResponse1 updateCustomerEidStatus =
                this.customerApi.updateCustomerValidationsError(this.alphaTestUserFresh, eidStatus, 400);

        THEN("The platform will return a 400 bad request");
        Assertions.assertNotNull(updateCustomerEidStatus);
        Assertions.assertEquals("Invalid Request", updateCustomerEidStatus.getMessage());
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "valid", "!@£$%", "pending", "invalid"})
    public void negative_invalid_eidStatus_update_customer_record_400_response(String invalidEidStatus) {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9471: AC4 - Update Customer Record to an invalid EID status - 400 Bad Request");
        setupTestUser();
        GIVEN("The client wants to update the EID Status of a customer in CRM");
        WHEN("The client attempts to update the customer profile with an invalid or missing EIDStatus or CustomerId");

        JSONObject eidStatus = generateJsonForCustomerValidations(invalidEidStatus, null);

        OBErrorResponse1 updateCustomerEidStatus =
                this.customerApi.updateCustomerValidationsErrorJson(this.alphaTestUser, eidStatus, 400);

        THEN("The platform will return a 400 bad request");
        Assertions.assertNotNull(updateCustomerEidStatus);

        DONE();
    }

    @ParameterizedTest
    @CsvSource({"INVALID, chip not read", "VALID, !@££$£$", "INVALID, "})
    public void negative_test_invalid_reason_but_valid_status_update_customer_record_400_response(String status,
                                                                                                  String invalidReason) {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9472: AC4 - Update Customer Record to a valid EID status but invalid Reason - 400 Bad Request");
        setupTestUser();
        GIVEN("The client wants to update the EID Status of a customer in CRM");
        WHEN("The client attempts to update the customer profile with an invalid or missing EIDStatus or CustomerId");

        JSONObject eidStatus = generateJsonForCustomerValidations(status, invalidReason);

        OBErrorResponse1 updateCustomerEidStatus =
                this.customerApi.updateCustomerValidationsErrorJson(alphaTestUser, eidStatus, 400);

        THEN("The platform will return a 400 bad request");
        Assertions.assertNotNull(updateCustomerEidStatus);

        DONE();
    }

    @ParameterizedTest
    @CsvSource({"invalid, chip not read", " ,!@££$£$", "VALI, alsjdlasldkj"})
    public void negative_test_invalid_reason_and_invalid_status_update_customer_record_400_response(
            String invalidStatus, String invalidReason) {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9473: AC4 - Update Customer Record to an invalid EID status and invalid Reason - 400 Bad Request");
        setupTestUser();

        GIVEN("The client wants to update the EID Status of a customer in CRM");
        WHEN("The client attempts to update the customer profile with an invalid or missing EIDStatus or CustomerId");

        JSONObject eidStatus = generateJsonForCustomerValidations(invalidStatus, invalidReason);

        OBErrorResponse1 updateCustomerEidStatus =
                this.customerApi.updateCustomerValidationsErrorJson(alphaTestUser, eidStatus, 400);

        THEN("The platform will return a 400 bad request");
        Assertions.assertNotNull(updateCustomerEidStatus);

        DONE();
    }

    @Test
    public void negative_test_missing_mandatory_eidStatus_400_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9816: AC4 - Update Customer Record - 400 Bad Request");
        setupTestUser();
        GIVEN("The client wants to update the EID Status of a customer in CRM");
        WHEN("The client attempts to update the customer profile with an invalid or missing EIDStatus or CustomerId");

        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .build())
                .build();

        OBErrorResponse1 updateCustomerEidStatus =
                this.customerApi.updateCustomerValidationsError(alphaTestUser, eidStatus, 400);

        THEN("The platform will return a 400 bad request");
        Assertions.assertNotNull(updateCustomerEidStatus);

        DONE();
    }

    @Test
    public void happy_path_get_customer_after_updating_status_200_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9859: AC5 - Get customer - 200 OK");
        this.alphaTestUserFresh = setupTestUserFresh();
        GIVEN("The client has updated a customer record with EidStatus");

        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        this.customerApi.updateCustomerValidations(alphaTestUserFresh, eidStatus);

        WHEN("The client attempts to retrieve the customer profile with the customerID");
        OBCustomer1 customerData =
                this.customerApi.getCurrentCustomer(alphaTestUserFresh).getData().getCustomer().get(0);
        THEN("The platform will return a 200 OK");
        AND("The platform will return the full customer profile including the EidStatus against the customer");
        Assertions.assertNotNull(customerData);
        Assertions.assertEquals(OBEIDStatus.VALID.toString(), customerData.getEidStatus().toString());
        Assertions.assertEquals(alphaTestUserFresh.getUserId(), customerData.getCustomerId().toString());

        DONE();
    }

    @Test
    public void negative_test_get_customer_does_not_exist_404_response() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9860: AC6 - Customer does not exist - 404 Not Found");
        /**
         * TODO :: Ignoring in ALL due to https://ahbdigitalbank.atlassian.net/browse/AHBDB-10475
         */
        envUtils.ignoreTestInEnv(Environments.ALL);

        AlphaTestUser userNotCustomer = new AlphaTestUser();
        this.alphaTestUserFactory.setupUser(userNotCustomer);
        GIVEN("The client wants to get the EID Status of a customer in CRM");
        WHEN("The client attempts to update the customer profile with an invalid or missing EIDStatus or CustomerId");

        OBErrorResponse1 getCustomer = this.customerApi.getCustomerErrorResponse(userNotCustomer, 404);

        THEN("The platform will return a 400 bad request");
        Assertions.assertNotNull(getCustomer);
        DONE();
    }

    @Test
    public void happy_path_create_case_in_CRM() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9861: AC7 - EID does not match - create case in CRM");

        this.alphaTestUserFresh = setupTestUserFresh();
        String customerId = this.alphaTestUserFresh.getUserId();

        GIVEN("The client wants to create a case due to an EID verification failure");

        CaseEventV1 body = this.casesApi.generateCaseBody(customerId, CaseTypeEnum.EXCEPTION,
                "CAS_TESTING_1152", ProcessOriginEnum.EID_VERIFICATION, ReasonEnum.EID_CANNOT_BE_VERIFIED,
                "High");

        WHEN("The client attempts to push an event with the relevant case information so that an agent can " +
                "investigate");

        this.casesApi.createCaseInCRM(body, 200);

        THEN("The platform will consume this event");
        AND("The platform will create the case on CRM");
        DONE();
    }

    @Test
    public void cycle_through_eid_statuses() {
        TEST("AHBDB-8292: Add EID verification flag in CRM");
        TEST("AHBDB-9862: Cycle through EID statuses");
        this.alphaTestUserFresh = setupTestUserFresh();

        GIVEN("A customer has been onboarded as a customer");

        OBReadCustomer1Data customerPendingEID = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();

        Assertions.assertEquals(OBEIDStatus.PENDING.toString(),
                customerPendingEID.getCustomer().get(0).getEidStatus().toString());

        WHEN("A client attempts to change the EID status from PENDING to VALID");
        THEN("The platform will return a 200 OK");
        AND("The customer record will be updated");
//        Change EID Status to VALID
        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();

        OBWriteCustomerResponse1 obWriteCustomerResponse1 =
                this.customerApi.updateCustomerValidations(this.alphaTestUserFresh, eidStatus);

        OBReadCustomer1Data validEidGet = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();
        Assertions.assertEquals(OBEIDStatus.VALID.toString(),
                validEidGet.getCustomer().get(0).getEidStatus().toString());

        WHEN("A client attempts to change the EID status from VALID to INVALID");
        THEN("The platform will return a 200 OK");
        AND("The customer record will be updated");
//        Change EID Status to INVALID with reason
        OBWriteEIDStatus1 eidStatus2 = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.INVALID)
                        .invalidEIDReason(OBInvalidEIDReason.CHIP_NOT_READ)
                        .build())
                .build();

        OBWriteCustomerResponse1 obWriteCustomerResponse2 =
                this.customerApi.updateCustomerValidations(this.alphaTestUserFresh, eidStatus2);

        OBReadCustomer1Data invalidEidGet = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();
        Assertions.assertEquals(OBEIDStatus.INVALID.toString(),
                invalidEidGet.getCustomer().get(0).getEidStatus().toString());

        WHEN("A client attempts to change the EID status from INVALID to PENDING");
        THEN("The platform will return a 200 OK");
        AND("The customer record will be updated");
//        Change EID Status back to pending
        OBWriteEIDStatus1 eidStatus3 = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.PENDING)
                        .invalidEIDReason(null)
                        .build())
                .build();

        OBWriteCustomerResponse1 obWriteCustomerResponse3 =
                this.customerApi.updateCustomerValidations(this.alphaTestUserFresh, eidStatus3);

        OBReadCustomer1Data pendingEidGet = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh).getData();
        Assertions.assertEquals(OBEIDStatus.PENDING.toString(),
                pendingEidGet.getCustomer().get(0).getEidStatus().toString());
        DONE();
    }

    public JSONObject generateJsonForCustomerValidations(String status, String reason) {
        return new JSONObject() {
            {
                put("Data", new JSONObject(){
                    {
                        put("Status", status);
                        put("InvalidEIDReason", reason);
                    }
                });
            }
        };
    }

}
