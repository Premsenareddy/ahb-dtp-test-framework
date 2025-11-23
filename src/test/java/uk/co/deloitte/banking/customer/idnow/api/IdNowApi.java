package uk.co.deloitte.banking.customer.idnow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpStatus;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.json.JSONObject;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.config.IdNowConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.DocumentType;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.GetApplicantResponse;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.GetApplicantListResponse;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IDNowValue;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IdentificationProcess;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IdentificationDocument;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.WebhookEvent;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

public class IdNowApi extends BaseApi {

    public static final String BEARER = "Bearer ";
    public static final String SCOPE = "Scope";
    public static final String TOKEN_TYPE = "TokenType";
    public static final String ACCESS_TOKEN = "AccessToken";

    public static final String V1_IDV_EVENTS = "/v1/idv/events";
    public static final String WEBHOOKS_IDV_EVENTS = "/webhooks/idv/events";
    public static final String INTERNAL_V1_RELATIONSHIPS_ID_IDVS_APPLICANTS = "/internal/v1/relationships/{relationshipId}/idvs/applicants";
    public static final String INTERNAL_V1_RELATIONSHIPS_ID_IDVS = "/internal/v1/relationships/{relationshipId}/idvs";
    public static final String INTERNAL_V1_IDVS_APPLICANTS = "/internal/v1/idvs/applicants";
    public static final String INTERNAL_V1_IDVS = "/internal/v1/idvs";
    public static final String INTERNAL_V2_IDVS = "/internal/v2/idvs";
    public static final String PROTECTED_V2_IDVS = "/protected/v2/{customerId}/idvs";
    public static final String CUSTOMER_IDVS_PATH = "/internal/v2/customers/idvs";

    @Inject
    IdNowConfiguration idNowConfiguration;

    @Inject
    CardsConfiguration cardsConfiguration;

    @Inject
    private ObjectMapper objectMapper;

    public <T> T getCustomerDetailsByDoc(final AlphaTestUser alphaTestUser, Map<String, String> queryParams, final Class <T> classType, HttpStatus status) {
        return (T) given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .queryParams(queryParams)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CUSTOMER_IDVS_PATH)
                .then().log().all().statusCode(status.getCode())
                .extract().body().as(classType);

    }

    /**
     * @param alphaTestUser
     * @return
     */
    public TokenHolder createApplicant(final AlphaTestUser alphaTestUser) {

        final CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder().build();

        return createApplicant(alphaTestUser, applicantRequest);
    }

    public TokenHolder createApplicantBasedOnDocumentType(final AlphaTestUser alphaTestUser, DocumentType documentType) {

        final CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder().build();

        return createApplicantBasedOnDocumentType(alphaTestUser, applicantRequest, documentType);
    }


    public TokenHolder createApplicant(final AlphaTestUser alphaTestUser,
                                       CreateApplicantRequest createApplicantRequest) {

        return given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(createApplicantRequest)
                .when()
                .post(idNowConfiguration.getBasePath() + INTERNAL_V1_IDVS_APPLICANTS)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.getCode())
                .extract().body().as(TokenHolder.class);
    }

    public TokenHolder createApplicantBasedOnDocumentType(final AlphaTestUser alphaTestUser,
                                       CreateApplicantRequest createApplicantRequest, DocumentType documentType) {

        return given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .queryParam("documentType", documentType)
                .body(createApplicantRequest)
                .when()
                .post(idNowConfiguration.getBasePath() + INTERNAL_V1_IDVS_APPLICANTS)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.getCode())
                .extract().body().as(TokenHolder.class);
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public TokenHolder createChildApplicant(final AlphaTestUser alphaTestUser, String relationshipId,
                                            CreateApplicantRequest applicantRequest) {

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(applicantRequest)
                .when()
                .post(idNowConfiguration.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_IDVS_APPLICANTS)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.getCode())
                .extract().body().as(TokenHolder.class);
    }

    public OBErrorResponse1 createChildApplicantError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                      CreateApplicantRequest applicantRequest, int statusCode) {

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(applicantRequest)
                .when()
                .post(idNowConfiguration.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_IDVS_APPLICANTS)
                .then().log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 createApplicantError(final AlphaTestUser alphaTestUser,
                                                 final CreateApplicantRequest applicantRequest, int statusCode) {
        return given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(applicantRequest)
                .when()
                .post(idNowConfiguration.getBasePath() + INTERNAL_V1_IDVS_APPLICANTS)
                .then().log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public Boolean setIdNowAnswer(final AlphaTestUser alphaTestUser, String status) {
        var webhookEvent = WebhookEvent.builder()
                .identificationprocess(IdentificationProcess.builder()
                        .result(status)
                        .companyId("demosecurityfeatures")
                        .filename("demosecurityfeatures__20190528_154257__061e65c8ad110.zip")
                        .identificationTime("2019-05-28T17:47:12+02:00")
                        .id(alphaTestUser.getApplicantId())
                        .build())
                .customdata(Map.of("custom", "value"))
                .contactdata(Map.of("mobilephone", "+555521234567"))
                .userdata(Map.of(
                        "birthday", IDNowValue.builder().status("NEW").value("1977-04-27").build(),
                        "firstname", IDNowValue.builder().status("NEW").value("RICHARD ANDREAS").build(),
                        "lastname", IDNowValue.builder().status("NEW").value("KOFER").build(),
                        "nationality", IDNowValue.builder().status("NEW").value("DE").build(),
                        "gender", IDNowValue.builder().status("NEW").value("MALE").build(),
                        "fullname", IDNowValue.builder().status("NEW").value("RICHARD ANDREAS KOFER").build()
                ))
                .build();
        return given().config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .body(webhookEvent)
                .when()
                .post(idNowConfiguration.getWebhookPath() + WEBHOOKS_IDV_EVENTS)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(Boolean.class);

    }

    public Boolean setIdNowAnswer(final AlphaTestUser alphaTestUser, String status, DocumentType documentType) {
        var webhookEvent = WebhookEvent.builder()
                .identificationprocess(IdentificationProcess.builder()
                        .result(status)
                        .companyId("demosecurityfeatures")
                        .filename("demosecurityfeatures__20190528_154257__061e65c8ad110.zip")
                        .identificationTime("2019-05-28T17:47:12+02:00")
                        .id(alphaTestUser.getApplicantId())
                        .build())
                .identificationdocument(IdentificationDocument.builder()
                        .type(new IDNowValue("NEW",
                                documentType == DocumentType.EID ? "IDCARD" : DocumentType.PASSPORT.toString())).build())
                .customdata(Map.of("custom", "value"))
                .contactdata(Map.of("mobilephone", "+555521234567"))
                .userdata(Map.of(
                        "birthday", IDNowValue.builder().status("NEW").value("1977-04-27").build(),
                        "firstname", IDNowValue.builder().status("NEW").value("RICHARD ANDREAS").build(),
                        "lastname", IDNowValue.builder().status("NEW").value("KOFER").build(),
                        "nationality", IDNowValue.builder().status("NEW").value("DE").build(),
                        "gender", IDNowValue.builder().status("NEW").value("MALE").build(),
                        "fullname", IDNowValue.builder().status("NEW").value("RICHARD ANDREAS KOFER").build()
                ))
                .build();
        return given().config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .body(webhookEvent)
                .when()
                .post(idNowConfiguration.getWebhookPath() + WEBHOOKS_IDV_EVENTS)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(Boolean.class);

    }

    public ValidatableResponse setIdNowAnswerNotFound(final AlphaTestUser alphaTestUser, String status) {
        var webhookEvent = WebhookEvent.builder()
                .identificationprocess(IdentificationProcess.builder()
                        .result(status)
                        .companyId("demosecurityfeatures")
                        .filename("demosecurityfeatures__20190528_154257__061e65c8ad110.zip")
                        .identificationTime("2019-05-28T17:47:12+02:00")
                        .id(alphaTestUser.getApplicantId())
                        .build())
                .customdata(Map.of("custom", "value"))
                .contactdata(Map.of("mobilephone", "+555521234567"))
                .userdata(Map.of(
                        "birthday", IDNowValue.builder().status("NEW").value("1977-04-27").build(),
                        "firstname", IDNowValue.builder().status("NEW").value("RICHARD ANDREAS").build(),
                        "lastname", IDNowValue.builder().status("NEW").value("KOFER").build(),
                        "nationality", IDNowValue.builder().status("NEW").value("DE").build(),
                        "gender", IDNowValue.builder().status("NEW").value("MALE").build(),
                        "fullname", IDNowValue.builder().status("NEW").value("RICHARD ANDREAS KOFER").build()
                ))
                .build();
        return given().config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .body(webhookEvent)
                .when()
                .post(idNowConfiguration.getBasePath() + V1_IDV_EVENTS)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.getCode());

    }



    public ApplicantExtractedDTO getApplicantResult(final AlphaTestUser alphaTestUser) {

        GetApplicantResponse response = given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(idNowConfiguration.getBasePath() + INTERNAL_V1_IDVS)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(GetApplicantResponse.class);

        return response.getResult();
    }

    public GetApplicantListResponse getApplicantResultV2(final AlphaTestUser alphaTestUser) {

        return given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(idNowConfiguration.getBasePath() + INTERNAL_V2_IDVS)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(GetApplicantListResponse.class);
    }


    public ApplicantExtractedDTO getChildApplicantResults(final AlphaTestUser alphaTestUser, String relationshipId) {

        GetApplicantResponse response = given().config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(idNowConfiguration.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_IDVS)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(GetApplicantResponse.class);

        return response.getResult();
    }

    public OBErrorResponse1 getChildApplicantResultsNegativeFlow(final AlphaTestUser alphaTestUser,
                                                                 String relationshipId, int statusCode) {

        return given().config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(idNowConfiguration.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_IDVS)
                .then().log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 getApplicantResultNegativeFlow(AlphaTestUser alphaTestUser, int statusCode) {

        return given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(idNowConfiguration.getBasePath() + INTERNAL_V1_IDVS)
                .then().log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public WebhookEvent postIDVEvents(final AlphaTestUser alphaTestUser, WebhookEvent webhookEvent) {
        return given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(webhookEvent)
                .post(idNowConfiguration.getWebhookPath() + WEBHOOKS_IDV_EVENTS)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(WebhookEvent.class);
    }

    public OBErrorResponse1 setIdAnswerJSON(JSONObject jsonObject, int statusCode) {
        return given().config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .body(jsonObject.toString())
                .when()
                .post(idNowConfiguration.getBasePath() + V1_IDV_EVENTS)
                .then().log().all()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public ApplicantExtractedDTO getApplicantResultsForCustomerId(final AlphaTestUser alphaTestUser, String customerId) {

        GetApplicantResponse response = given().config(config)
                .pathParam("customerId", customerId)
                .queryParam("documentType", "EID")
                .log().all()
                .header(HEADER_X_API_KEY, idNowConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(idNowConfiguration.getBasePath() + PROTECTED_V2_IDVS)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(GetApplicantListResponse.class).getIdNowDetails().get(0);

        return response.getResult();
    }

    public OBErrorResponse1 getApplicantResultsForCustomerIdNegativeFlow(final AlphaTestUser alphaTestUser,
                                                                 String customerId, int statusCode) {

        return given().config(config)
                .pathParam("customerId", customerId)
                .log().all()
                .header(HEADER_X_API_KEY, idNowConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(idNowConfiguration.getBasePath() + PROTECTED_V2_IDVS)
                .then().log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }
}
