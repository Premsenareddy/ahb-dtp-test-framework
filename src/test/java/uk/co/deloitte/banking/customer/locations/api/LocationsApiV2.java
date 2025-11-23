package uk.co.deloitte.banking.customer.locations.api;

import io.restassured.response.Response;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBReadLocationResponse1;

import javax.inject.Inject;
import java.util.List;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

public class LocationsApiV2 extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    private static final String INTERNAL_V2_CUSTOMERS_LOCATIONS = "/internal/v2/customers/locations";

    public JSONObject locationBuilder(String changing, String value) {
        return new JSONObject() {
            {
                put("Name", "Work");
                put("Address", new JSONObject() {
                    {
                        put("Department", "Al Hilal Bank Head Office");
                        put("SubDepartment", "Al Bahr Towers");
                        put("BuildingNumber", "16");
                        put("StreetName", "Al Saada Street, Al Nayhan Camp Area");
                        put("AddressLine", List.of("Sector E3"));
                        put("TownName", "Dubai");
                        put("CountrySubDivision", "Abu Dhabi");
                        put("Country", "AE");
                        put("PostalCode", "PO Box 63111");
                        put(changing, value);
                    }
                });
            }
        };
    }

    public OBReadLocationResponse1 createLocationDetails(final AlphaTestUser alphaTestUser,
                                                         OBLocationDetails1 locationDetails1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_LOCATIONS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBReadLocationResponse1 createLocationDetailsChild(final AlphaTestUser alphaTestUser,
                                                              OBLocationDetails1 locationDetails1, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1)
                .when()
                .post(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBErrorResponse1 createLocationDetailsChildError(final AlphaTestUser alphaTestUser,
                                                            OBLocationDetails1 locationDetails1,
                                                            String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1)
                .when()
                .post(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public Response createLocationDetailsChildJson(final AlphaTestUser alphaTestUser, JSONObject locationDetails1,
                                                   String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1.toString())
                .when()
                .post(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }


    public OBErrorResponse1 createLocationDetailsError(final AlphaTestUser alphaTestUser,
                                                       OBLocationDetails1 locationDetails1) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_LOCATIONS)
                .then()
                .log().all()
                .statusCode(400).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 createLocationDetailsJSONError(final AlphaTestUser alphaTestUser,
                                                           JSONObject locationDetails1, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_LOCATIONS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }
    public OBReadLocationResponse1 createLocationDetailsJSONSuccess(final AlphaTestUser alphaTestUser,
                                                                    JSONObject locationDetails1, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_LOCATIONS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().body().as(OBReadLocationResponse1.class);
    }


    public OBReadLocationResponse1 updateLocationDetails(final AlphaTestUser alphaTestUser,
                                                         OBLocationDetails1 locationDetails1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1)
                .when()
                .put(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_LOCATIONS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBReadLocationResponse1 updateLocationDetailsChild(final AlphaTestUser alphaTestUser,
                                                              OBLocationDetails1 locationDetails1, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1)
                .when()
                .put(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBErrorResponse1 updateLocationDetailsChildError(final AlphaTestUser alphaTestUser,
                                                            OBLocationDetails1 locationDetails1,
                                                            String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1)
                .when()
                .put(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public Response updateLocationDetailsChildJson(final AlphaTestUser alphaTestUser,
                                                   JSONObject locationDetails1, String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(locationDetails1.toString())
                .when()
                .put(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    public OBReadLocationResponse1 deleteLocationDetails(final AlphaTestUser alphaTestUser,
                                                         String locationId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .delete(customerConfig.getBasePath() + "/internal/v2/customers/locations/" + locationId)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBReadLocationResponse1 deleteLocationDetailsChild(final AlphaTestUser alphaTestUser,
                                                              String locationId, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .delete(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations/" + locationId)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBErrorResponse1 deleteLocationDetailsChildError(final AlphaTestUser alphaTestUser, String locationId,
                                                            String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .delete(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations/" + locationId)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 deleteLocationDetailsError(final AlphaTestUser alphaTestUser,
                                                       String locationId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .delete(customerConfig.getBasePath() + "/internal/v2/customers/locations/" + locationId)
                .then()
                .log().all()
                .statusCode(404).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadLocationResponse1 getLocationsDetails(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_LOCATIONS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBReadLocationResponse1 getLocationsDetailsChild(final AlphaTestUser alphaTestUser, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadLocationResponse1.class);
    }

    public OBErrorResponse1 getLocationsDetailsChildError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                          int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + "/internal/v2/relationships/" + relationshipId + "/customers/locations")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 getLocationsDetailsError(final AlphaTestUser alphaTestUser, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_LOCATIONS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }
}
