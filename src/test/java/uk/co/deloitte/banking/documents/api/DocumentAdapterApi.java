package uk.co.deloitte.banking.documents.api;


import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.documents.config.DocumentConfig;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.AddMetadataRequest;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

@Slf4j
public class DocumentAdapterApi extends BaseApi {

    @Inject
    private DocumentConfig documentConfig;

    static final String INTERNAL_V1_DOCUMENTS = "/internal/v1/documents";
    static final String INTERNAL_V1_DOCUMENTS_ID = "/internal/v1/documents/{id}";
    static final String PROTECTED_V1_ID_DOCUMENTS_ID = "/protected/v1/{customerId}/documents/{documentId}";

    public DocumentRead1 getDocumentList(AlphaTestUser alphaTestUser, String docType, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("docType", docType)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_DOCUMENTS)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(DocumentRead1.class);
    }

    public DocumentRead1 getDocumentList(AlphaTestUser alphaTestUser, String docType, String accountId,
                                         String fromDate, String toDate, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("docType", docType)
                .queryParam("accountId", accountId)
                .queryParam("fromDate", fromDate)
                .queryParam("toDate", toDate)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_DOCUMENTS)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(DocumentRead1.class);
    }

    public OBErrorResponse1 getDocumentListError(AlphaTestUser alphaTestUser, String docType, String accountId,
                                                 String fromDate, String toDate, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("docType", docType)
                .queryParam("accountId", accountId)
                .queryParam("fromDate", fromDate)
                .queryParam("toDate", toDate)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_DOCUMENTS)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public void getDocumentListVoid(AlphaTestUser alphaTestUser, String docType, String accountId,
                                    String fromDate, String toDate, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("docType", docType)
                .queryParam("accountId", accountId)
                .queryParam("fromDate", fromDate)
                .queryParam("toDate", toDate)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_DOCUMENTS)
                .then().log().all().statusCode(statusCode);
    }

    public byte[] getDocumentById(AlphaTestUser alphaTestUser, String docId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .pathParams("id", docId)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_DOCUMENTS_ID)
                .then()
                .statusCode(statusCode).extract().asByteArray();

    }

    public OBErrorResponse1 getDocumentByIdError(AlphaTestUser alphaTestUser, String docId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .pathParams("id", docId)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_DOCUMENTS_ID)
                .then()
                .statusCode(statusCode).extract().as(OBErrorResponse1.class);

    }

    public void getDocumentByIdVoid(AlphaTestUser alphaTestUser, String docId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .pathParams("id", docId)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(documentConfig.getBasePath() + INTERNAL_V1_DOCUMENTS_ID)
                .then()
                .statusCode(statusCode);
    }

    public Response createMetadata(AddMetadataRequest request, String customerId, String documentId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .pathParams("customerId", customerId, "documentId", documentId)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_API_KEY, documentConfig.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(documentConfig.getBasePath() + PROTECTED_V1_ID_DOCUMENTS_ID)
                .then().log().all().statusCode(statusCode)
                .extract().response();

    }

}
