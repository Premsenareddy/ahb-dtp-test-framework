package uk.co.deloitte.banking.customer.cif;

import io.micronaut.http.HttpStatus;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cif.CifConfig;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifRequest;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifResponse;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.SetCifRequest;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Map;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

public class CifsApi extends BaseApi {

    @Inject
    CifConfig cifConfig;

    @Inject
    CardsConfiguration cardsConfiguration;

    @Inject
    AuthConfiguration authConfiguration;

    private final String INTERNAL_CIFS_PATH = "/internal/v1/cifs";
    private final String WEBHOOK_CIFS_PATH = "/webhooks/v1/cifs";
    private final String CUSTOMER_CIF_PATH = "/protected/v1/legacy/customer";

    public <T> T getLegacyCIFByEmiratesId(Map<String, String> queryParams, final Class <T> classType, HttpStatus status) {
        return (T) given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .queryParams(queryParams)
                .headers("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CUSTOMER_CIF_PATH)
                .then().log().all().statusCode(status.getCode())
                .extract().body().as(classType);

    }

    public CifResponse generateCif(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(CifRequest.builder()
                        .emiratesId(randomNumeric(15).replaceFirst("0", "1"))
                        .dateOfBirth(alphaTestUser.getDateOfBirth())
                        .phoneNumber(alphaTestUser.getUserTelephone()).build())
                .when()
                .post(cifConfig.getBasePath() + INTERNAL_CIFS_PATH)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(CifResponse.class);
    }

    public CifResponse generateCifNumber(final AlphaTestUser alphaTestUser, String emiratesId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(CifRequest.builder()
                        .emiratesId(emiratesId)
                        .dateOfBirth(alphaTestUser.getDateOfBirth())
                        .phoneNumber(alphaTestUser.getUserTelephone()).build())
                .when()
                .post(cifConfig.getBasePath() + INTERNAL_CIFS_PATH)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(CifResponse.class);
    }

    public CifResponse generateCifLegacy(String eid, LocalDate dateOfBirth, String phoneNumber) {
        return given()
                .config(config)
                .log().all()
                .contentType(JSON)
                .body(CifRequest.builder()
                        .emiratesId(eid)
                        .dateOfBirth(dateOfBirth)
                        .phoneNumber(phoneNumber).build())
                .when()
                .post(cifConfig.getBasePath() + WEBHOOK_CIFS_PATH)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(CifResponse.class);
    }

    public CifResponse generateCifLegacy(LocalDate dateOfBirth, String phoneNumber) {
        return generateCifLegacy(randomNumeric(15).replaceFirst("0", "1"), dateOfBirth, phoneNumber);
    }

    public JSONObject createCifBodyJson(String phoneNumber, String emiratesId, String dateOfBirth) {
        return new JSONObject() {
            {
                put("PhoneNumber", phoneNumber);
                put("EmiratesId", emiratesId);
                put("DateOfBirth", dateOfBirth);
            }
        };
    }

    public CifResponse generateCifNumberJson(final AlphaTestUser alphaTestUser, JSONObject createCifBodyJson) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(createCifBodyJson.toString())
                .when()
                .post(cifConfig.getBasePath() + INTERNAL_CIFS_PATH)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().as(CifResponse.class);
    }

    public OBErrorResponse1 generateCifError(final AlphaTestUser alphaTestUser, JSONObject createCifBodyJson, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(createCifBodyJson.toString())
                .when()
                .post(cifConfig.getBasePath() + INTERNAL_CIFS_PATH)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public CifResponse submitCif() {
        return given()
                .config(config)
                .log().all()
                .contentType(JSON)
                .body(SetCifRequest.builder()
                        .emiratesId(randomNumeric(15).replaceFirst("0", "1")).build())
                .when()
                .put(cifConfig.getBasePath() + WEBHOOK_CIFS_PATH + "/" + randomNumeric(7).replace("0", "1"))
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(CifResponse.class);
    }

    public CifResponse submitCifNumber(String emiratesId, String cifNumber) {
        return given()
                .config(config)
                .log().all()
                .contentType(JSON)
                .body(SetCifRequest.builder().emiratesId(emiratesId).build())
                .when()
                .put(cifConfig.getBasePath() + WEBHOOK_CIFS_PATH + "/" + cifNumber)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(CifResponse.class);
    }

    public OBErrorResponse1 submitCifNumberError(String emiratesId, String cifNumber, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .contentType(JSON)
                .body(SetCifRequest.builder().emiratesId(emiratesId).build())
                .when()
                .put(cifConfig.getBasePath() + WEBHOOK_CIFS_PATH + "/" + cifNumber)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public void submitCifNumberVoid(String emiratesId, String cifNumber, int statusCode) {

        given()
                .config(config)
                .log().all()
                .contentType(JSON)
                .body(SetCifRequest.builder().emiratesId(emiratesId).build())
                .when()
                .put(cifConfig.getBasePath() + WEBHOOK_CIFS_PATH + "/" + cifNumber)
                .then()
                .log().all()
                .statusCode(statusCode);
    }
}