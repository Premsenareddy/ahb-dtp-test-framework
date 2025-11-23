package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.AddMetadataRequest;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.AddMetadataResponse;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentFile1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;
import uk.co.deloitte.banking.documents.api.DocumentRelationshipAdapterApi;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class UploadBirthCertificateTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private DocumentRelationshipAdapterApi documentRelationshipApi;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

    private AlphaTestUser alphaTestUser;

    private String TEMPORARY_PASSWORD = "temporary_password";

    private String childId;
    private String connectionId;

    private static final String ACCOUNT_FORBIDDEN_CODE = "UAE.ACCOUNT.FORBIDDEN";
    private static final String ACCOUNT_FORBIDDEN_MESSAGE = "Relationship not verified";

    private void setupTestUsers() {
        
        if (alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
            alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);

            childId = this.alphaTestUserFactory.createChildInForgerock(alphaTestUser, TEMPORARY_PASSWORD);

            connectionId = this.alphaTestUserFactory.createChildInCRM(alphaTestUser,
                    alphaTestUserFactory.generateDependantBody(childId, 5, "Test Name", OBGender.MALE, OBRelationshipRole.FATHER));
        }
    }

    @Tag("SmokeTest")
    @Test
    public void happy_path_upload_birth_certificate_for_child() {
        TEST("AHBDB-6996: Upload Birth Certificate for child");
        TEST("AHBDB-11164: AC1, AC2 - Store birth certificate - 200 Response");

        GIVEN("The connectionID exists in the relationship list of the parent");
        setupTestUsers();
        relationshipApi.getRelationships(alphaTestUser);

        WHEN("A call is made from Document Adapter to SharePoint");
        documentRelationshipApi.uploadDocument(alphaTestUser, "BIRTH_CERTIFICATE", connectionId, 201);

        THEN("The birth certificate is saved in the Child's User folder in SharePoint");
        DONE();
    }

    @Test
    public void relationship_not_verified_for_child_403() {
        TEST("AHBDB-6996: Upload Birth Certificate for child");
        TEST("AHBDB-11158: AC3 Relationship not verified - 403 Forbidden");

        GIVEN("A Get Relationship request is sent");
        setupTestUsers();
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertEquals(alphaTestUser.getUserId(), getRelationships.getData().getCustomerId().toString());
        assertEquals(childId, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());

        WHEN("The provided connectionID does not exist in the relationship list of the parent");
        OBErrorResponse1 uploadedDoc = documentRelationshipApi.uploadDocumentError(alphaTestUser, "BIRTH_CERTIFICATE", UUID.randomUUID().toString(), 403);
        assertEquals(ACCOUNT_FORBIDDEN_CODE, uploadedDoc.getCode());
        assertEquals(ACCOUNT_FORBIDDEN_MESSAGE, uploadedDoc.getMessage());
        THEN("The platform will be unsuccessful in verifying the relationship");
        AND("The platform will return a 403 Forbidden and log");
        DONE();
    }

    @Test
    public void create_metadata_for_child_birth_certificate() {
        TEST("AHBDB-6996: Upload Birth Certificate for child");
        TEST("AHBDB-11158: AC4 Metadata that has not been previously created");

        GIVEN("A Metadata field has not been created in Sharepoint");
        WHEN("the client attempts to save the Birth Certificate with Metadata fields that have not been created");
        setupTestUsers();
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertEquals(alphaTestUser.getUserId(), getRelationships.getData().getCustomerId().toString());
        assertEquals(childId, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());
        Map<String, String> metaDataBody = new HashMap<>();
        metaDataBody.put("ParentCIF", "1234567");
        metaDataBody.put("EIDNumber", "123123412345671");

        DocumentFile1 uploadedDoc = documentRelationshipApi.uploadDocument(alphaTestUser, "BIRTH_CERTIFICATE", connectionId, 201);
        String documentId = uploadedDoc.getId();
        AddMetadataRequest request = AddMetadataRequest.builder()
                .metadata(metaDataBody)
                .build();
        Response metadata = documentAdapterApi.createMetadata(request, childId, documentId, 200);
        AddMetadataResponse response = metadata.as(AddMetadataResponse.class);
        assertEquals(true, response.getResult());

        THEN("We will save the Birth Certificate in the Child’s user folder in Sharepoint");
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"CIF, 12345678,EID, 1111111"})
    public void invalid_metadata_for_child_birth_certificate(String cifKey, String cifValue, String eidkey, String eidValue) {
        TEST("AHBDB-6996: Upload Birth Certificate for child");
        TEST("AHBDB-11162: AC4 Metadata that has not been previously created - Negative metadata");

        GIVEN("A Metadata field has not been created in Sharepoint");
        WHEN("the client attempts to save the Birth Certificate with Metadata fields that have not been created");
        setupTestUsers();
        OBReadRelationship1 getRelationships = this.relationshipApi.getRelationships(alphaTestUser);
        assertEquals(alphaTestUser.getUserId(), getRelationships.getData().getCustomerId().toString());
        assertEquals(childId, getRelationships.getData().getRelationships().get(0).getCustomerId().toString());
        Map<String, String> metaDataBody = new HashMap<>();
        metaDataBody.put(cifKey, cifValue);
        metaDataBody.put(eidkey, eidValue);

        DocumentFile1 uploadedDoc = documentRelationshipApi.uploadDocument(alphaTestUser, "BIRTH_CERTIFICATE", connectionId, 201);
        String documentId = uploadedDoc.getId();
        AddMetadataRequest request = AddMetadataRequest.builder()
                .metadata(metaDataBody)
                .build();
        Response metadata = documentAdapterApi.createMetadata(request, childId, documentId, 200);
        AddMetadataResponse response = metadata.as(AddMetadataResponse.class);
        assertEquals(false, response.getResult());

        THEN("We will save the Birth Certificate in the Child’s user folder in Sharepoint");
        AND("The metadata fields that had not been created will not be saved with the drive item");
        DONE();
    }
}


