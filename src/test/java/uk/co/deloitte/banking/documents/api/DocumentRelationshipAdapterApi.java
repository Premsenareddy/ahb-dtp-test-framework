package uk.co.deloitte.banking.documents.api;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.documents.config.DocumentConfig;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentFile1;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.io.File;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

@Slf4j
public class DocumentRelationshipAdapterApi extends BaseApi {

    @Inject
    private DocumentConfig documentConfig;

    static final String INTERNAL_V1_RELATIONSHIPS_ID_DOCUMENTS_GET = "/internal/v1/relationships/{relationshipId}/documents";
    static final String INTERNAL_V1_RELATIONSHIPS_ID_DOCUMENTS_POST = "/internal/v1/relationships/{relationshipId}/documents/{documentType}";
    static final String UPLOAD_FILE_NAME = "upload_doc.txt";

    public DocumentFile1 uploadDocument(AlphaTestUser alphaTestUser, String docType, String relationshipId, int statusCode) {
        File targetFile = new File(getClass().getClassLoader().getResource(UPLOAD_FILE_NAME).getFile());
        assertNotNull(targetFile);

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .pathParam("documentType", docType)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .multiPart(new MultiPartSpecBuilder(targetFile).fileName(UPLOAD_FILE_NAME).controlName("file").mimeType("application/txt").build())
                .when()
                .post(documentConfig.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_DOCUMENTS_POST)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(DocumentFile1.class);
    }

    public DocumentRead1 getDocumentList(AlphaTestUser alphaTestUser, String docType, String relationshipId, String fromDate, String toDate, int statusCode) {
        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("docType", docType)
                .queryParam("fromDate", fromDate)
                .queryParam("toDate", toDate)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_DOCUMENTS_GET)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(DocumentRead1.class);
    }

    public OBErrorResponse1 uploadDocumentError(AlphaTestUser alphaTestUser, String docType, String relationshipId, int statusCode) {
        File targetFile = new File(getClass().getClassLoader().getResource(UPLOAD_FILE_NAME).getFile());
        assertNotNull(targetFile);

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .pathParam("documentType", docType)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .multiPart(new MultiPartSpecBuilder(targetFile).fileName(UPLOAD_FILE_NAME).controlName("file").mimeType("application/txt").build())
                .when()
                .post(documentConfig.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_DOCUMENTS_POST)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

}
