package uk.co.deloitte.banking.customer.cases.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.cases.api.CasesApi;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.http.common.Alt;
import uk.co.deloitte.banking.http.kafka.BaseEvent;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;

@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CasesIncludeRelationshipContactField {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CasesApi casesApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private RelationshipApi relationshipApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private String connectionId;

    private void setupTestUser() {

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(this.alphaTestUser);
            setupTestUserChild(this.alphaTestUser);
        }
    }

    @ParameterizedTest
    @CsvSource({"BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION, VERIFY_RELATIONSHIP",
                "NAME_SCREENING, E_NAME_CHECKER_HIT",
                "IDV, IDV_DOCUMENT_ERROR"})
    public void happy_path_create_case_with_new_contact_field_kids(String process, String reason) {
        TEST("AHBDB-7007: Include relationship contact field during crease creation");
        TEST("AHBDB-10535: AC1 Create Case");
        setupTestUser();
        GIVEN("A create case request has been raised");
        WHEN("The client attempts to push an event with the relevant case information for the child");
        String customerId = this.alphaTestUserChild.getUserId();
        String userIdLabel = "|u-" + customerId.substring(0,8);

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
                        .responsibleContactID(this.alphaTestUser.getUserId())
                        .processOrigin(ProcessOriginEnum.valueOf(process))
                        .reason(ReasonEnum.valueOf(reason))
                        .build())
                .build();

        THEN("The responsibleContactID is expected in the request");
        AND("The platform will consume this event");
        AND("The platform will create the case on CRM");
        this.casesApi.createCaseInCRM(casesBody, 200);
        DONE();
    }



    @Test
    public void negative_test_create_case_missing_responsible_contact_id() {
        TEST("AHBDB-7007: Include relationship contact field during crease creation");
        TEST("AHBDB-10536: Negative Test - Missing responsible contact ID");
        setupTestUser();
        GIVEN("A create case request has been raised");
        WHEN("The client attempts to push an event with the relevant case information for the child");
        AND("The responsible contact ID field is missing");
        String customerId = this.alphaTestUserChild.getUserId();
        String userIdLabel = "|u-" + customerId.substring(0,8);

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
                        .reason(ReasonEnum.E_NAME_CHECKER_HIT)
                        .build())
                .build();

        THEN("The platform will return a 400");
        this.casesApi.createCaseInCRM(casesBody, 400);
        DONE();
    }

    public void setupTestUserChild(AlphaTestUser atuParent) {
        String tempPassword = "validtestpassword";

        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword(tempPassword).build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(atuParent, request);
        assertNotNull(response.getUserId());
        String childId = response.getUserId();

        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(childId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName("js " + generateEnglishRandomString(10))
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.FATHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();

        OBReadRelationship1 createResponse =
                this.relationshipApi.createDependant(atuParent, obWriteDependant1);

        this.connectionId = createResponse.getData().getRelationships().get(0).getConnectionId().toString();

        this.alphaTestUserChild = new AlphaTestUser();
        this.alphaTestUserChild.setUserId(childId);
        this.alphaTestUserChild.setUserPassword(tempPassword);

        alphaTestUserChild = this.alphaTestUserFactory.createChildCustomer(this.alphaTestUser,
                this.alphaTestUserChild, this.connectionId, this.alphaTestUserChild.getUserId());
    }
}
