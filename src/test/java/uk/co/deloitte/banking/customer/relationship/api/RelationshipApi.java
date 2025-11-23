package uk.co.deloitte.banking.customer.relationship.api;

import io.micronaut.http.HttpStatus;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTerm1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTermResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteRelationship1;

import javax.inject.Inject;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;


public class RelationshipApi extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    @Inject
    TemenosConfig temenosConfig;

    private static final String INTERNAL_RELATIONSHIPS = "/internal/v2/relationships";
    private static final String INTERNAL_RELATIONSHIPS_CUSTOMERS = "/internal/v2/relationships/customers";
    private static final String INTERNAL_RELATIONSHIPS_ID_CUSTOMERS = "/internal/v2/relationships/{relationshipId}/customers";
    private static final String INTERNAL_RELATIONSHIPS_ID_CUSTOMERS_CIF = "/internal/v2/relationships/{relationshipId}/customers/cif";
    private static final String INTERNAL_RELATIONSHIP_TERM="/internal/v2/relationships/{relationshipId}/customers/terms";
    private static final String INTERNAL_RELATIONSHIP_IDVS="/internal/v2/relationships/{relationshipId}/customers/idvs";
    private static final String INTERNAL_REALTIONSHIP_TRANSACTIONS_V2 = "/internal/v2/relationships/{relationshipId}/accounts/{accountId}/transactions";

    public OBReadRelationship1 createDependant(final AlphaTestUser alphaTestUser,
                                               OBWriteDependant1 obWriteDependant1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteDependant1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBReadRelationship1.class);
    }

    public Response createDependantError(final AlphaTestUser alphaTestUser,
                                         OBWriteDependant1 obWriteDependant1, Integer statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteDependant1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    public Response createDependantErrorJson(final AlphaTestUser alphaTestUser,
                                             JSONObject jsonObject, Integer statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    public JSONObject createDependentBodyJsonChange(String fieldToChange, String fieldValue) {

        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("Id", UUID.randomUUID());
                        put("DateOfBirth", Arrays.asList(LocalDate.now().minusYears(15).getYear(),
                                LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth()));
                        put("FullName", "js" + generateEnglishRandomString(10));
                        put("Gender", "MALE");
                        put("Language", "en");
                        put("TermsVersion", Arrays.asList(LocalDate.now().getYear(),
                                LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth()));
                        put("TermsAccepted", Boolean.TRUE);
                        put("CustomerRole", "FATHER");
                        put("DependantRole", "SON");
                        put(fieldToChange, fieldValue);
                    }
                });
            }
        };
    }

    public OBReadRelationship1 createRelationship(final AlphaTestUser alphaTestUser,
                                                  OBWriteRelationship1 obWriteRelationship1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteRelationship1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBReadRelationship1.class);
    }

    public ValidatableResponse createRelationshipUnsupported(final AlphaTestUser alphaTestUser,
                                                             OBWriteRelationship1 obWriteRelationship1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteRelationship1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS)
                .then()
                .log().all().statusCode(501).assertThat();
    }

    public JSONObject createRelationshipBodyJson(String relationCustomerId, String customerRole,
                                                 String relationCustomerRole) {
        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("RelationCustomerId", relationCustomerId);
                        put("CustomerRole", customerRole);
                        put("RelationCustomerRole", relationCustomerRole);
                    }
                });
            }
        };
    }

    public OBErrorResponse1 createRelationshipError(final AlphaTestUser alphaTestUser,
                                                    JSONObject createRelationBodyJson, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(createRelationBodyJson.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public OBReadRelationship1 getRelationships(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());


        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadRelationship1.class);
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public OBReadCustomer1 getChildBasedOnRelationship(final AlphaTestUser alphaTestUser, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());


        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS_ID_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomer1.class);
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public OBReadCustomer1 putChildCif(final AlphaTestUser alphaTestUser, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .put(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS_ID_CUSTOMERS_CIF)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomer1.class);
    }

    public OBErrorResponse1 putChildCifError(final AlphaTestUser alphaTestUser, String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .put(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS_ID_CUSTOMERS_CIF)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBWriteTermResponse1 createDependantTerm(final AlphaTestUser alphaTestUser, String relationshipId, OBWriteTerm1 data){
        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(data)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIP_TERM)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteTermResponse1.class);
    }

    public OBWriteTermResponse1 patchDependantTerm(final AlphaTestUser alphaTestUser, String relationshipId, OBWriteTerm1 data){
        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(data)
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_RELATIONSHIP_TERM)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteTermResponse1.class);
    }


    public OBWriteIdvDetailsResponse1 createChildIdvDetails(final AlphaTestUser alphaTestUser, String relationshipId, OBIdvDetails1 idvDetails1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .body(idvDetails1)
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIP_IDVS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteIdvDetailsResponse1.class);
    }

    public OBWriteIdvDetailsResponse1 createChildIdvDetails(final AlphaTestUser alphaTestUser, String relationshipId) {

        SecureRandom random = new SecureRandom();
        long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);

        OBIdvDetails1 idvDetails = OBIdvDetails1.builder()
                .result(OBResult.SUCCESS)
                .reason("TECH_PHOTO")
                .transactionNumber("cb3fe4cb-abd9-4647-841f-35ad8aec6f57")
                .identId("ABC-ABCDE")
                .idType(OBIdType.IDCARD)
                .documentNumber(String.valueOf(n))
                .idNumber("123456789")
                .idCountry("AE")
                .dateOfExpiry(LocalDate.of(2022, 01, 01))
                .identificationTime(OffsetDateTime.of(2021, 01, 01, 10, 10, 0, 0, ZoneOffset.UTC))
                .gtcVersion("GTC-Version")
                .type(OBAppType.APP)
                .build();

        return createChildIdvDetails(alphaTestUser, relationshipId, idvDetails);
    }

    public OBErrorResponse1 createDependantTermError(final AlphaTestUser alphaTestUser,
                                                     String relationshipId, JSONObject jsonObject) {

        return createDependantTermError(alphaTestUser, relationshipId, jsonObject, 400);
    }

    public OBErrorResponse1 createDependantTermError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                     JSONObject jsonObject, int statusCode){

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIP_TERM)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }


    /**
     * @param alphaTestUser
     * @return
     */
    public OBWriteIdvDetailsResponse1 createChildIdvDetails(final AlphaTestUser alphaTestUser, String relationshipId, OBResult result) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        SecureRandom random = new SecureRandom();
        long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);
        OBIdvDetails1 idvDetails = OBIdvDetails1.builder()
            .result(result)
            .reason("TECH_PHOTO")
            .transactionNumber("cb3fe4cb-abd9-4647-841f-35ad8aec6f57")
            .identId("ABC-ABCDE")
            .idType(OBIdType.IDCARD)
            .documentNumber(String.valueOf(n))
            .idNumber("123456789")
            .idCountry("AE")
            .dateOfExpiry(LocalDate.of(2022, 01, 01))
            .identificationTime(OffsetDateTime.of(2021, 01, 01, 10, 10, 0, 0, ZoneOffset.UTC))
            .gtcVersion("GTC-Version")
            .type(OBAppType.APP)
            .build();

        return given()
            .config(config)
            .pathParam("relationshipId", relationshipId)
            .log().all()
            .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
            .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
            .contentType(JSON)
            .when()
            .body(idvDetails)
            .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIP_IDVS)
            .then()
            .log().all()
            .statusCode(201).assertThat()
            .extract().body().as(OBWriteIdvDetailsResponse1.class);
    }

    public OBErrorResponse1 createChildIdvDetailsError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                                 JSONObject jsonObject, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .body(jsonObject.toString())
                .post(customerConfig.getBasePath() + INTERNAL_RELATIONSHIP_IDVS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public OBReadIdvDetailsResponse1 getIdvDetailsChild(final AlphaTestUser alphaTestUser, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
            .config(config)
            .pathParam("relationshipId", relationshipId)
            .log().all()
            .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
            .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
            .contentType(JSON)
            .when()
            .get(customerConfig.getBasePath() + INTERNAL_RELATIONSHIP_IDVS)
            .then()
            .log().all()
            .statusCode(200).assertThat()
            .extract().body().as(OBReadIdvDetailsResponse1.class);
    }

    public OBErrorResponse1 getChildBasedOnRelationshipError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                             int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());


        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_RELATIONSHIPS_ID_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTransactionsOnRelationshipId(final AlphaTestUser alphaTestUser, String relationshipId, String accountId, Map<String, String> queryParams
                                                              , Class classType, HttpStatus statusCode) {

        return (T) given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .pathParam("accountId", accountId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .queryParams(queryParams)
                .contentType(JSON)
                .when()
                .get(temenosConfig.getPath() + INTERNAL_REALTIONSHIP_TRANSACTIONS_V2)
                .then()
                .log().all()
                .statusCode(statusCode.getCode()).assertThat()
                .extract().body().as(classType);
    }
}
