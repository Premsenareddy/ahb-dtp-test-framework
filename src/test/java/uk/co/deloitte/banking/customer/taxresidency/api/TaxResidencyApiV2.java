package uk.co.deloitte.banking.customer.taxresidency.api;

import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.api.customer.model.crs.*;

import javax.inject.Inject;
import java.util.List;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TaxResidencyApiV2 extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    private static final String INTERNAL_V2_CUSTOMERS_CRS = "/internal/v2/customers/crs";
    private static final String INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_CRS = "/internal/v2/relationships/{relationshipId}/customers/crs";

    public void getTaxInformation(AlphaTestUser alphaTestUser, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(statusCode);
    }

    public Response getTaxInformationWithReturn(final AlphaTestUser alphaTestUser, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().response();
    }

    public OBReadCRSResponse2 getTaxInformationChild(final AlphaTestUser alphaTestUser, int statusCode, String relationshipId) {
        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().body().as(OBReadCRSResponse2.class);
    }

    public OBErrorResponse1 getTaxInformationChildError(final AlphaTestUser alphaTestUser, String relationshipId, int statusCode) {
        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBWriteCRSResponse2 addCRSDetailsChild(final AlphaTestUser alphaTestUser,
                                                  final OBWriteCRS2 obWriteCRS1, String relationshipId) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCRS1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCRSResponse2.class);
    }

    public OBErrorResponse1 addCRSDetailsChildError(final AlphaTestUser alphaTestUser, OBWriteCRS2 obWriteCRS1,
                                                    String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCRS1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public Response addCRSDetailsChildJson(final AlphaTestUser alphaTestUser, JSONObject obWriteCRS1,
                                           String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCRS1.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }


    public OBReadCRSResponse1 getCRSResponse(final AlphaTestUser alphaTestUser, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().response().as(OBReadCRSResponse1.class);
    }

    public OBWriteCRS2 getObWriteCRS2() {
        return OBWriteCRS2.builder()
                .data(OBCRSData2.builder()
                        .taxResidencyCountry(List.of(OBTaxResidencyCountry2.builder()
                                .country("AE")
                                .tinNumber("ABC456789")
                                .missingTinReason("Country doesn't issue TINs to its residents")
                                .build()))
                        .uaeResidencyByInvestmentScheme(true)
                        .otherResidencyJurisdictions(true)
                        .personalIncomeTaxJurisdictions(List.of("CN"))
                        .agreedCertification(true)
                        .build())
                .build();
    }

    public JSONObject getJSONForCRS(JSONObject country, String changing, String value) throws JSONException {
        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("TaxResidencyCountry", country);
                        put("UaeResidencyByInvestmentScheme", "true");
                        put("OtherResidencyJurisdictions", "false");
                        put("PersonalIncomeTaxJurisdictions", "CN");
                        put("AgreedCertification", "true");
                        put(changing, value);
                    }
                });
            }
        };
    }

    public JSONObject buildTaxResidencyCountryJSON(String changing, String value) throws JSONException {
        return new JSONObject() {
            {
                put("Country", "AE");
                put("TinNumber", "ABC456789");
                put("MissingTinReason", "Country doesn't issue TINs to its residents");
                put(changing, value);
            }
        };
    }

    public JSONObject getJSONForCRSRemoved(JSONObject country, String toRemove) throws JSONException {
        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("TaxResidencyCountry", country);
                        put("UaeResidencyByInvestmentScheme", "true");
                        put("OtherResidencyJurisdictions", "false");
                        put("PersonalIncomeTaxJurisdictions", "CN");
                        put("AgreedCertification", "true");
                        remove(toRemove);
                    }
                });
            }
        };
    }

    public void postInvalidCRSDetails(final AlphaTestUser alphaTestUser,
                                      final JSONObject jsonObject) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(400).assertThat();
    }

    public OBErrorResponse1 postInvalidCRSDetailsErrorResponse(final AlphaTestUser alphaTestUser,
                                                               final JSONObject jsonObject) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(400)
                .extract().as(OBErrorResponse1.class);
    }

    public OBWriteCRSResponse2 postCRSDetailsJson(final AlphaTestUser alphaTestUser,
                                                  final JSONObject jsonObject) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getJwtToken())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(201)
                .extract().as(OBWriteCRSResponse2.class);
    }

    public void getTaxInformationFromNotFound(AlphaTestUser alphaTestUser) {
        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(404).assertThat();
    }

    public OBWriteCRS2 getObWriteCRS2MultipleTaxResidencies() {
        return OBWriteCRS2.builder()
                .data(OBCRSData2.builder()
                        .taxResidencyCountry(List.of(
                                OBTaxResidencyCountry2.builder()
                                        .country("AE")
                                        .missingTinReason("Country does not issue TINs to its " +
                                                "residents")
                                        .build(),
                                OBTaxResidencyCountry2.builder()
                                        .country("AE")
                                        .missingTinReason("Country does not issue TINs to its " +
                                                "residents")
                                        .build(),
                                OBTaxResidencyCountry2.builder()
                                        .country("AE")
                                        .missingTinReason("Country does not issue TINs to its " +
                                                "residents")
                                        .build()))
                        .uaeResidencyByInvestmentScheme(true)
                        .otherResidencyJurisdictions(true)
                        .personalIncomeTaxJurisdictions(List.of("AE", "CN"))
                        .agreedCertification(true)
                        .build())
                .build();
    }

    public OBWriteCRS2 getObWriteCRS2ValidMultipleTaxResidencies() {
        return OBWriteCRS2.builder()
                .data(OBCRSData2.builder()
                        .taxResidencyCountry(List.of(
                                OBTaxResidencyCountry2.builder()
                                        .country("AE")
                                        .missingTinReason("Country doesn't issue TINs to its " +
                                                "residents")
                                        .build(),
                                OBTaxResidencyCountry2.builder()
                                        .country("AE")
                                        .missingTinReason("Unable to obtain/disclose a TIN")
                                        .build()))
                        .uaeResidencyByInvestmentScheme(true)
                        .otherResidencyJurisdictions(true)

                        .personalIncomeTaxJurisdictions(List.of("AE", "CN"))
                        .agreedCertification(true)
                        .build())
                .build();
    }

    public void addCRSDetailsTooManyTaxResidencies(final AlphaTestUser alphaTestUser,
                                                   final OBWriteCRS2 obWriteCRS1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCRS1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(400);
    }
}
