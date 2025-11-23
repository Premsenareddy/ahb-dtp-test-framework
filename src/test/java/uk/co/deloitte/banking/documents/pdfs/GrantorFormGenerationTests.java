package uk.co.deloitte.banking.documents.pdfs;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.Request;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;
import uk.co.deloitte.banking.http.common.Alt;
import uk.co.deloitte.banking.http.kafka.BaseEvent;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@Tag("Documents")
@Tag("BuildCycle5.1")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GrantorFormGenerationTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private RelationshipApi relationshipApi;

    private AlphaTestUser alphaTestUserChild;
    private AlphaTestUser alphaTestUser;

    @BeforeEach
    public void setupTestUser() {
        //envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUserChild == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            String dependantId = createUserRelationship(alphaTestUser);
            String relationshipId = createDependentCustomer(alphaTestUser, OBRelationshipRole.FATHER, dependantId);
            this.alphaTestUserChild = alphaTestUserFactory.createChildCustomer(alphaTestUser, new AlphaTestUser(), relationshipId, dependantId);
        }
    }

    private String createUserRelationship(AlphaTestUser alphaTestUser) {
        UserRelationshipWriteRequest request = UserRelationshipWriteRequest.builder().tempPassword("validtestpassword").build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        assertNotNull(response);
        assertNotNull(response.getUserId());
        return response.getUserId();
    }

    private String createDependentCustomer(AlphaTestUser alphaTestUser, OBRelationshipRole obRelationshipRole, String dependantId) {
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
        return response.getData().getRelationships().get(0).getConnectionId().toString();
    }

    @Test
    public void happy_path_generate_GRANTOR_form_for_child() {
        TEST("AHBDB-7505: Minor Savings Account - Grantor Form");
        TEST("AHBDB-10960: AC1 - Generate GRANTOR Form");

        GIVEN("A customer exists as a bank account user");
        AND("The customer is a child");

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserChild.setCustomerId(customerId);

        WHEN("The client attempts to generate a GRANTOR form for the customer with all of the mandatory consolidated " +
                "list of fields (using AcroField names) from CRM and the relevant Metadata fields");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getGRANTORDocumentRequest(this.alphaTestUser, this.alphaTestUserChild);

        THEN("We will generate the form against the GRANTOR form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "GRANTOR", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                    String documentId = documentRead1.getData().getDocumentFiles().get(0).getId();
                    byte[] documentBytes = this.documentAdapterApi.getDocumentById(this.alphaTestUser, documentId, 200);
                    assertThat(documentBytes).isNotEmpty();
                });
        DONE();
    }

    @Test
    public void happy_path_generate_GRANTOR_form_for_child_without_eid_metadata_field() {
        TEST("AHBDB-7505: Minor Savings Account - Grantor Form");
        TEST("AHBDB-10961: AC2 - Metadata that has not been previously created");

        GIVEN("A customer exists as a bank account user");
        AND("The customer is a child");

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUserChild).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUserChild.setCustomerId(customerId);

        WHEN("The client attempts to generate a GRANTOR form for the customer with Metadata fields that have not been created ");
        DocumentGenerationRequestEvent documentRequest = this.getGRANTORDocumentRequestWithoutEIDNumberMetadata(this.alphaTestUser, this.alphaTestUserChild);

        THEN("We will generate the form against the GRANTOR form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "GRANTOR", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    private DocumentGenerationRequestEvent getGRANTORDocumentRequestWithoutEIDNumberMetadata(final AlphaTestUser alphaTestUser, final AlphaTestUser alphaTestUserChild) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("GRANTOR")
                        .customerId(alphaTestUserChild.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("SonDauNameENG", "Jim");
                                put("SignatureEn", "Jim Sig");
                                put("Date", "01/01/2020");
                                put("Name", "Bob");
                                put("SavAccNoENG", "12345678");
                            }
                        })
                        .documentMetadata(Map.of(
                                "CIF", "123456789",
                                "testMetadata", "testValue")) // This metadata does not exist in sharepoint. We still expect the doc to be created.
                        .build())
                .build();
    }
}
