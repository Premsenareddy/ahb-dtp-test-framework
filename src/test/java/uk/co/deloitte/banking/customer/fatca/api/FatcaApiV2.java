package uk.co.deloitte.banking.customer.fatca.api;

import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBFatcaForm1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBReadFatca1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatca1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatcaDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatcaResponse1;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomInteger;

public class FatcaApiV2 extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    private static final String INTERNAL_V2_CUSTOMERS_FATCA = "/internal/v2/customers/fatca";
    private static final String INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_FATCA = "/internal/v2/relationships/{relationshipId}/customers/fatca";


    public OBWriteFatcaResponse1 createFatcaDetails(final AlphaTestUser alphaTestUser,
                                                    OBWriteFatca1 fatcaDetails) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(fatcaDetails)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteFatcaResponse1.class);
    }
    public OBWriteFatcaResponse1 createFatcaDetailsChild(final AlphaTestUser alphaTestUser,
                                                    OBWriteFatca1 fatcaDetails, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(fatcaDetails)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteFatcaResponse1.class);
    }

    public OBReadFatca1 getFatcaDetails(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadFatca1.class);
    }

    public OBReadFatca1 getFatcaDetailsChild(final AlphaTestUser alphaTestUser, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadFatca1.class);
    }

    public OBErrorResponse1 getFatcaDetailsChildError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                      int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void getFatcaInvalid(final AlphaTestUser alphaTestUser, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat();
    }

    public OBErrorResponse1 getFatcaInvalidErrorResponse(final AlphaTestUser alphaTestUser, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public OBWriteFatca1 obWriteFatca1() {
        return OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .form(OBFatcaForm1.W8)
                        .usCitizenOrResident(Boolean.TRUE)
                        .ssn(String.valueOf(generateRandomInteger(999999999)))
                        .federalTaxClassification(generateEnglishRandomString(20))
                        .build()
                ).build();
    }

    public JSONObject fatcaJSON(String form, String usCitizen, String ssn, String fedTax) throws JSONException {
        JSONObject data = new JSONObject();
        JSONObject fatca = new JSONObject() {
            {
                put("Form", form);
                put("UsCitizenOrResident", usCitizen);
                put("Ssn", ssn);
                put("FederalTaxClassification", fedTax);
            }
        };

        data.put("Data", fatca);
        return data;
    }

    public void createFatcaDetailsJSON(final AlphaTestUser alphaTestUser,
                                       JSONObject fatcaDetails) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(fatcaDetails.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(400).assertThat();
    }

    public Response createFatcaDetailsChildJSON(final AlphaTestUser alphaTestUser, JSONObject fatcaDetails,
                                                String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(fatcaDetails.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    public OBErrorResponse1 createFatcaDetailsJSONErrorResponse(final AlphaTestUser alphaTestUser,
                                                                JSONObject fatcaDetails) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(fatcaDetails.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_FATCA)
                .then()
                .log().all()
                .statusCode(400)
                .extract().as(OBErrorResponse1.class);
    }

    public JSONObject fatcaMissingField(String missingField) throws JSONException {
        JSONObject data = new JSONObject();
        JSONObject fatca = new JSONObject() {
            {
                put("Form", OBFatcaForm1.W8);
                put("UsCitizenOrResident", Boolean.TRUE);
                put("Ssn", generateRandomInteger(999999999));
                put("FederalTaxClassification", generateEnglishRandomString(20));
                remove(missingField);
            }
        };

        data.put("Data", fatca);
        return data;
    }
}
