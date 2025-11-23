package uk.co.deloitte.banking.customer.aml;

import io.restassured.http.ContentType;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.config.AmlConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SanctionsApi extends BaseApi {

    private static final String INTERNAL_V1_CUSTOMER_BLACKLISTS = "/internal/v1/customer-blacklists";
    private static final String INTERNAL_V1_RELATIONSHIPS_ID_CUSTOMER_BLACKLISTS = "/internal/v1/relationships/{relationshipId}/customer-blacklists";

    @Inject
    AmlConfiguration amlConfiguration;

    public CustomerBlacklistResponseDTO checkBlacklistedCustomer(final AlphaTestUser alphaTestUser,
                                                                 CustomerBlacklistRequestDTO requestDTO) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post(amlConfiguration.getBasePath() + INTERNAL_V1_CUSTOMER_BLACKLISTS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(CustomerBlacklistResponseDTO.class);
    }

    public CustomerBlacklistResponseDTO checkBlacklistedChild(final AlphaTestUser alphaTestUser,
                                                              CustomerBlacklistRequestDTO requestDTO,
                                                              String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(relationshipId);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post(amlConfiguration.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_CUSTOMER_BLACKLISTS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(CustomerBlacklistResponseDTO.class);
    }

    public OBErrorResponse1 checkBlacklistedChildError(final AlphaTestUser alphaTestUser,
                                                       CustomerBlacklistRequestDTO requestDTO,
                                                       String relationshipId,
                                                       int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(relationshipId);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post(amlConfiguration.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_CUSTOMER_BLACKLISTS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 checkBlacklistedChildErrorJson(final AlphaTestUser alphaTestUser,
                                                           JSONObject requestDTO,
                                                           String relationshipId,
                                                           int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(relationshipId);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(requestDTO.toString())
                .when()
                .post(amlConfiguration.getBasePath() + INTERNAL_V1_RELATIONSHIPS_ID_CUSTOMER_BLACKLISTS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 checkBlackListedCustomerError(final AlphaTestUser alphaTestUser,
                                                          CustomerBlacklistRequestDTO requestDTO) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post(amlConfiguration.getBasePath() + INTERNAL_V1_CUSTOMER_BLACKLISTS)
                .then()
                .log().all()
                .statusCode(400).extract().body().as(OBErrorResponse1.class);
    }
}
