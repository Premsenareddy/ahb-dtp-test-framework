package uk.co.deloitte.banking.customer.cases.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventRequest;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventV1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseTypeEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ProcessOriginEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ReasonEnum;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.cases.api.CasesApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.http.common.Alt;
import uk.co.deloitte.banking.http.kafka.BaseEvent;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("@BuildCycle4")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateCaseTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CasesApi casesApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
//        TODO :: Ignored in NFT
        envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    @ParameterizedTest
    @CsvSource({"CAS_Testing_1152, NAME_SCREENING, E_NAME_CHECKER_HIT, High",
            "CAS_TESTING_1162, BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION, VERIFY_RELATIONSHIP, Normal",
            "CAS_TESTING_1172, IDV, IDV_REVIEW_NEEDED, Low",
            "CAS_TESTING_1182, EID_VERIFICATION, EID_CANNOT_BE_VERIFIED, Normal"})
    public void happy_path_create_case_exception_200(String title, String process, String reason, String priority) {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7431: Positive Test - Happy Path Scenario - Create Case - Exception");
        setupTestUser();
        GIVEN("A customer falls into one of the exception cases above");
        String customerId = this.alphaTestUser.getUserId();

        WHEN("The client attempts to push an event with the relevant case information for a customer");

        CaseEventV1 casesBody = this.casesApi.generateCaseBody(customerId, CaseTypeEnum.EXCEPTION, title,
                ProcessOriginEnum.valueOf(process), ReasonEnum.valueOf(reason), priority);

        THEN("The platform will consume this event");

        this.casesApi.createCaseInCRM(casesBody, 200);

        AND("The platform will create the case on CRM");
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"CAS_Testing_1152, NAME_SCREENING, E_NAME_CHECKER_HIT, High",
            "CAS_TESTING_1162, BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION, VERIFY_RELATIONSHIP, Normal",
            "CAS_TESTING_1172, IDV, IDV_REVIEW_NEEDED, Low"})
    public void happy_path_create_case_complaint_200(String title, String process, String reason, String priority) {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7432: Positive Test - Happy Path Scenario - Create Case - Complaint");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        CaseEventV1 casesBody = this.casesApi.generateCaseBody(customerId, CaseTypeEnum.EXCEPTION, title,
                ProcessOriginEnum.valueOf(process), ReasonEnum.valueOf(reason), priority);

        this.casesApi.createCaseInCRM(casesBody, 200);

        AND("The platform will create the case on CRM");
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"CAS_Testing_1152, NAME_SCREENING, E_NAME_CHECKER_HIT, High",
            "CAS_TESTING_1162, BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION, VERIFY_RELATIONSHIP, Normal",
            "CAS_TESTING_1172, IDV, IDV_REVIEW_NEEDED, Low"})
    public void happy_path_create_case_general_200(String title, String process, String reason, String priority) {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7433: Positive Test - Happy Path Scenario - Create Case - General");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");

        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        CaseEventV1 casesBody = this.casesApi.generateCaseBody(customerId, CaseTypeEnum.GENERAL, title,
                ProcessOriginEnum.valueOf(process), ReasonEnum.valueOf(reason), priority);

        this.casesApi.createCaseInCRM(casesBody, 200);

        AND("The platform will create the case on CRM");
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"CAS_Testing_1152, NAME_SCREENING, E_NAME_CHECKER_HIT, High",
            "CAS_TESTING_1162, BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION, VERIFY_RELATIONSHIP, Normal",
            "CAS_TESTING_1172, IDV, IDV_REVIEW_NEEDED, Low"})
    public void happy_path_create_case_operation_200(String title, String process, String reason, String priority) {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7434: Positive Test - Happy Path Scenario - Create Case - Operation");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        CaseEventV1 casesBody = this.casesApi.generateCaseBody(customerId, CaseTypeEnum.OPERATION, title,
                ProcessOriginEnum.valueOf(process), ReasonEnum.valueOf(reason), priority);

        this.casesApi.createCaseInCRM(casesBody, 200);

        AND("The platform will create the case on CRM");
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"CAS_Testing_1153, NAME_SCREENIN, E_NAME_CHECKER_HIT, COMPLAINT, High",
            "CAS_Testing_1153, 1234, E_NAME_CHECKER_HIT, EXCEPTION, Medium",
            "CAS_Testing_1153, ' ' , IDV_REVIEW_NEEDED, GENERAL, Low"})
    public void negative_test_invalid_process_field_400_response(String title, String invalidProcess, String reason,
                                                                 String caseType, String priority) {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7435: Negative Test - Invalid Field - Process: <Process>");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");

        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        JSONObject casesBody = this.casesApi.generateCaseBodyJson(customerId, caseType, title,
                invalidProcess, reason, priority);

        AND("The platform will return 400");
        this.casesApi.createCase(alphaTestUser, casesBody, 400);
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"CAS_Testing_1153, NAME_SCREENING, E_NAME_CHECKER, COMPLAINT, High",
            "CAS_Testing_1153, BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION, VERIFY, EXCEPTION, Medium",
            "CAS_Testing_1153, IDV, !@#$%^&*()_+, GENERAL, Low"})
    public void negative_invalid_reason_field_400_response(String title, String process, String invalidReason,
                                                           String caseType, String priority) {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7436: Negative Test - Invalid Field - Reason: <Reason>");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        JSONObject casesBody = this.casesApi.generateCaseBodyJson(customerId, caseType, title,
                process, invalidReason, priority);

        AND("The platform will return 400");
        this.casesApi.createCase(alphaTestUser, casesBody, 400);
        DONE();
    }


    @ParameterizedTest
    @CsvSource({"CAS_Testing_1153, NAME_SCREENING, E_NAME_CHECKER_HIT, COMPLAIN, High",
            "CAS_Testing_1153, BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION, VERIFY_RELATIONSHIP, general, Medium",
            "CAS_Testing_1153, IDV, IDV_REVIEW_NEEDED, !@#$%^&*()_+, Low"})
    public void negative_invalid_caseType_field_400_response(String title, String process, String reason,
                                                             String invalidCaseType, String priority) {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7437: Negative Test - Invalid Field - CaseType: <CaseType>");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        JSONObject casesBody = this.casesApi.generateCaseBodyJson(customerId, invalidCaseType, title,
                process, reason, priority);

        AND("The platform will return 400");
        this.casesApi.createCase(alphaTestUser, casesBody, 400);
        DONE();
    }

    @Test
    public void missing_mandatory_caseType_400_response() {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7439: Missing Mandatory Fields - Value: <Value>");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        JSONObject casesBody = this.casesApi.generateCaseBodyJson(customerId, null,
                "CAS_Testing_1153", "NAME_SCREENING", "E_NAME_CHECKER_HIT", "High");

        AND("The platform will return 400");
        this.casesApi.createCase(alphaTestUser, casesBody, 400);
        DONE();
    }

    @Test
    public void missing_mandatory_customerId_400_response() {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7439: Missing Mandatory Fields - Value: <Value>");
        setupTestUser();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        CaseEventV1 casesBody = CaseEventV1.builder()
                .metadata(BaseEvent.builder()
                        .alt(Alt.builder()
                                .userId(null)
                                .userIdLabel(null)
                                .correlationIdLabel("")
                                .startTime(0)
                                .build())
                        .version(0)
                        .build())
                .eventData(CaseEventRequest.builder()
                        .caseType(CaseTypeEnum.EXCEPTION)
                        .customerId(null)
                        .title("CAS_TESTING_1165")
                        .processOrigin(ProcessOriginEnum.BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION)
                        .responsibleContactID(null)
                        .reason(ReasonEnum.VERIFY_RELATIONSHIP)
                        .priority("High")
                        .description("test description")
                        .additionalDetails(null)
                        .build())
                .build();

        AND("The platform will return 400");
        this.casesApi.createCaseInCRM(casesBody, 400);

        DONE();
    }

    @Test
    public void missing_metadata_400_response() {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7440: Missing Mandatory Fields - Metadata");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        CaseEventV1 casesBody = CaseEventV1.builder()
                .metadata(null)
                .eventData(CaseEventRequest.builder()
                        .caseType(CaseTypeEnum.EXCEPTION)
                        .customerId(customerId)
                        .title("CAS_TESTING_1154")
                        .processOrigin(ProcessOriginEnum.IDV)
                        .responsibleContactID(customerId)
                        .reason(ReasonEnum.E_NAME_CHECKER_HIT)
                        .priority("High")
                        .description("test description")
                        .build())
                .build();

        Response errorResponse = this.casesApi.createCaseInCRM(casesBody, 400);

        AND("The platform will return 400");
        DONE();
    }

    @Test
    public void missing_optional_process_200_response() {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7441: Missing Optional Field - postCase.EventData.Process");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        JSONObject casesBody = this.casesApi.generateCaseBodyJson(customerId, "GENERAL",
                "CAS_Testing_1153", null, "E_NAME_CHECKER_HIT", "High");

        AND("The platform will return 200");
        this.casesApi.createCase(alphaTestUser, casesBody, 200);
        DONE();
    }

    @Test
    public void missing_optional_reason_200_response() {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7441: Missing Optional Field - postCase.EventData.Reason");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        JSONObject casesBody = this.casesApi.generateCaseBodyJson(customerId, "GENERAL",
                "CAS_Testing_1153", "NAME_SCREENING", "E_NAME_CHECKER_HIT", "High");

        AND("The platform will return 200");
        this.casesApi.createCase(alphaTestUser, casesBody, 200);
        DONE();
    }

    @Test
    public void missing_optional_additional_details_200_response() {
        TEST("AHBDB-5309: AC1 Create Case");
        TEST("AHBDB-7441: Missing Optional Field - postCase.EventData.AdditionalDetails");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        String userIdLabel = "|u-" + customerId.substring(0, 8);

        GIVEN("A customer falls into one of the exception cases above");
        WHEN("The client attempts to push an event with the relevant case information for a customer");
        THEN("The platform will consume this event");

        CaseEventV1 casesBody = CaseEventV1.builder()
                .metadata(BaseEvent.builder()
                        .alt(Alt.builder()
                                .userId(customerId)
                                .userIdLabel(userIdLabel)
                                .correlationIdLabel("")
                                .startTime(0)
                                .build())
                        .version(0)
                        .build())
                .eventData(CaseEventRequest.builder()
                        .caseType(CaseTypeEnum.EXCEPTION)
                        .customerId(customerId)
                        .title("CAS_TESTING_1165")
                        .processOrigin(ProcessOriginEnum.BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION)
                        .responsibleContactID(customerId)
                        .reason(ReasonEnum.VERIFY_RELATIONSHIP)
                        .priority("High")
                        .description("test description")
                        .additionalDetails(null)
                        .build())
                .build();

        AND("The platform will return 200");
        this.casesApi.createCaseInCRM(casesBody, 200);
        DONE();
    }

    @Test
    public void crm_manual_exception_case_name_checker_hit() {
        TEST("AHBDB-10121: Not able to create CRM manual exception case, when name check is hit in CIT");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        String userIdLabel = "|u-" + customerId.substring(0, 8);

        CaseEventV1 casesBody = CaseEventV1.builder()
                .metadata(BaseEvent.builder()
                        .alt(Alt.builder()
                                .userId(customerId)
                                .userIdLabel(userIdLabel)
                                .correlationIdLabel("")
                                .startTime(0)
                                .build())
                        .version(0)
                        .build())
                .eventData(CaseEventRequest.builder()
                        .caseType(CaseTypeEnum.EXCEPTION)
                        .customerId(customerId)
                        .processOrigin(ProcessOriginEnum.NAME_SCREENING)
                        .responsibleContactID(customerId)
                        .reason(ReasonEnum.E_NAME_CHECKER_HIT)
                        .build())
                .build();

        AND("The platform will return 200");
        this.casesApi.createCaseInCRM(casesBody, 200);
        DONE();
    }

    @Test
    public void crm_manual_changing_relationship() {
        TEST("AHBDB-9068: Create case for changing relationship due to gender mismatch");
        setupTestUser();
        String customerId = this.alphaTestUser.getUserId();
        String userIdLabel = "|u-" + customerId.substring(0, 8);

        CaseEventV1 casesBody = CaseEventV1.builder()
                .metadata(BaseEvent.builder()
                        .alt(Alt.builder()
                                .userId(customerId)
                                .userIdLabel(userIdLabel)
                                .correlationIdLabel("")
                                .startTime(0)
                                .build())
                        .version(0)
                        .build())
                .eventData(CaseEventRequest.builder()
                        .caseType(CaseTypeEnum.EXCEPTION)
                        .customerId(customerId)
                        .processOrigin(ProcessOriginEnum.BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION)
                        .responsibleContactID(customerId)
                        .reason(ReasonEnum.VERIFY_RELATIONSHIP)
                        .additionalDetails("Gender of the parent is M, gender of the child is M")
                        .build())
                .build();

        AND("The platform will return 200");
        this.casesApi.createCaseInCRM(casesBody, 200);
        DONE();
    }

}
