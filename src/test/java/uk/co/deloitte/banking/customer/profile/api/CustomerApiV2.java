package uk.co.deloitte.banking.customer.profile.api;

import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBDocumentSource;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomerId1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTerm1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTermResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.authorization.OBCustomerAuthorization1;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRSResponse2;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBWriteEmploymentDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBAppType;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBIdType;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBIdvDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBReadIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBResult;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.internalrisk.OBWriteInternalRiskRating1;
import uk.co.deloitte.banking.customer.api.customer.model.internalrisk.OBWriteInternalRiskRatingResponse1;

import javax.inject.Inject;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.PhoneNumberUtils.sanitize;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomAddressLine;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomBuildingNumber;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomCountrySubDivision;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomPostalCode;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomStreetName;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

public class CustomerApiV2 extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    private static final String PROTECTED_V2_CUSTOMERS = "/protected/v2/customers";
    private static final String PROTECTED_V2_CUSTOMERS_ID = "/protected/v2/customers/{customerId}";
    private static final String PROTECTED_V2_CUSTOMERS_NUMBER_CIFS = "/protected/v2/cifs/{cifNumber}/customers";
    private static final String PROTECTED_V2_CUSTOMERS_EID_ID = "/protected/v2/customers/eid/{emiratesId}";
    private static final String PROTECTED_V2_CUSTOMERS_ID_VALIDATIONS = "/protected/v2/customers/{customerId}/validations";

    private static final String INTERNAL_RISKS = "/internal/v2/customers/internal-risks";
    private static final String INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_INTERNAL_RISKS = "/internal/v2/relationships/{relationshipId}/customers/internal-risks";
    private static final String INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS = "/internal/v2/relationships/{relationshipId}/customers";
    private static final String INTERNAL_V2_CUSTOMERS = "/internal/v2/customers";
    private static final String INTERNAL_V2_CUSTOMERS_AUTHORIZATIONS = "/internal/v2/customers/authorizations";
    private static final String INTERNAL_V2_CUSTOMERS_CRS = "/internal/v2/customers/crs";
    private static final String INTERNAL_V2_CUSTOMERS_TERMS = "/internal/v2/customers/terms";
    private static final String INTERNAL_V2_CUSTOMERS_CIF = "/internal/v2/customers/cif";
    private static final String INTERNAL_V2_CUSTOMERS_EMPLOYMENTS = "/internal/v2/customers/employments";
    private static final String INTERNAL_V2_CUSTOMERS_IDVS = "/internal/v2/customers/idvs";

    /**
     * @param alphaTestUser
     * @param obWriteCustomer1
     * @return
     */
    public OBWriteCustomerResponse1 createCustomerSuccess(final AlphaTestUser alphaTestUser,
                                                          OBWriteCustomer1 obWriteCustomer1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }

    public OBWriteCustomerResponse1 createCustomerSuccessJson(final AlphaTestUser alphaTestUser,
                                                              JSONObject jsonObject) {
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
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }

    public JSONObject customerJsonRemoveField(final AlphaTestUser alphaTestUser, String customerFieldToRemove) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        JSONObject address = new JSONObject() {
            {
                put("BuildingNumber", generateRandomBuildingNumber());
                put("StreetName", generateRandomStreetName());
                put("AddressLine", generateRandomAddressLine());
                put("CountrySubDivision", generateRandomCountrySubDivision());
                put("Country", "AE");
                put("PostalCode", generateRandomPostalCode());
            }
        };

        JSONObject data = new JSONObject();
        JSONObject profile = new JSONObject() {
            {
                put("DateOfBirth", LocalDate.of(1996, 8, 12));
                put("MobileNumber", sanitize(alphaTestUser.generateUserTelephone()));
                put("FirstName", generateEnglishRandomString(20));
                put("LastName", generateEnglishRandomString(20));
                put("PreferredName", alphaTestUser.getName());
                put("Nationality", "AE");
                put("Gender", OBGender.MALE);
                put("CountryOfBirth", "AE");
                put("CustomerState", OBCustomerStateV1.IDV_COMPLETED);
                put("Language", alphaTestUser.getLanguage());
                put("Email", generateRandomEmail());
                put("EmailState", OBWriteEmailState1.VERIFIED);
                put("TermsVersion", LocalDate.of(2020, 12, 12));
                put("TermsAccepted", Boolean.TRUE);
                put("Address", address);
                remove(customerFieldToRemove);
            }
        };

        data.put("Data", profile);
        return data;
    }

    public JSONObject customerJsonAddressRemoveField(final AlphaTestUser alphaTestUser, String addressFieldToRemove) {
        assertNotNull(alphaTestUser);

        JSONObject address = new JSONObject() {
            {
                put("BuildingNumber", generateRandomBuildingNumber());
                put("StreetName", generateRandomStreetName());
                put("AddressLine", generateRandomAddressLine());
                put("CountrySubDivision", generateRandomCountrySubDivision());
                put("Country", "AE");
                put("PostalCode", generateRandomPostalCode());
                remove(addressFieldToRemove);
            }
        };

        JSONObject data = new JSONObject();
        JSONObject profile = new JSONObject() {
            {
                put("DateOfBirth", LocalDate.of(1996, 8, 12));
                put("MobileNumber", sanitize(alphaTestUser.generateUserTelephone()));
                put("FirstName", generateEnglishRandomString(20));
                put("LastName", generateEnglishRandomString(20));
                put("PreferredName", alphaTestUser.getName());
                put("Nationality", "AE");
                put("Gender", OBGender.MALE);
                put("CountryOfBirth", "AE");
                put("CustomerState", OBCustomerStateV1.IDV_COMPLETED);
                put("Language", alphaTestUser.getLanguage());
                put("Email", generateRandomEmail());
                put("EmailState", OBWriteEmailState1.VERIFIED);
                put("TermsVersion", LocalDate.of(2020, 12, 12));
                put("TermsAccepted", Boolean.TRUE);
                put("Address", address);
            }
        };

        data.put("Data", profile);
        return data;
    }

    /**
     * @param alphaTestUser
     * @param obWritePartialCustomer1
     * @return
     */
    public OBWriteCustomerResponse1 patchCustomerSuccess(final AlphaTestUser alphaTestUser,
                                                         OBWritePartialCustomer1 obWritePartialCustomer1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .contentType(JSON)
                .body(obWritePartialCustomer1)
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }

    public OBWriteCustomerResponse1 patchChildSuccess(final AlphaTestUser alphaTestUser,
                                                      OBWritePartialCustomer1 obWriteCustomer1, String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }

    public OBErrorResponse1 patchChildError(final AlphaTestUser alphaTestUser, OBWritePartialCustomer1 obWriteCustomer1,
                                            String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    /*
        Response types: OBErrorResponse1, OBWriteCustomerResponse1
     */
    public Response patchChildJson(final AlphaTestUser alphaTestUser, JSONObject obWriteCustomer1,
                                   String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteCustomer1.toString())
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    /*
        Response types: OBErrorResponse1, OBReadCustomer1
     */
    public Response getChild(final AlphaTestUser alphaTestUser, String relationshipId, int statusCode) {

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
                .get(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public OBReadCustomer1 getCustomerSuccess(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());


        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomer1.class);
    }

    public OBErrorResponse1 getCustomerErrorResponse(final AlphaTestUser alphaTestUser, final int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 createCustomerError(final AlphaTestUser alphaTestUser,
                                                OBWriteCustomer1 obWriteCustomer1, int errorCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(errorCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void createCustomerErrorJsonObject(final AlphaTestUser alphaTestUser,
                                              JSONObject jsonObject, int errorCode) {

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then().log().all().statusCode(errorCode).assertThat();

    }

    public OBErrorResponse1 createCustomerErrorResponseJsonObject(final AlphaTestUser alphaTestUser,
                                                                  JSONObject jsonObject, int errorCode) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then().log().all().statusCode(errorCode)
                .extract().as(OBErrorResponse1.class);

    }

    /**
     * @param email
     * @return
     */
    public OBReadCustomerId1 getCustomersByEmail(final String email) {

        return given()
                .config(config)
                .queryParam("email", email)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomerId1.class);
    }

    /**
     * @param number
     * @return
     */
    public OBReadCustomerId1 getCustomersByMobile(final String number) {
        return given()
                .config(config)
                .queryParam("mobile", number)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomerId1.class);
    }

    /**
     * @param alphaTestUser
     */
    public void deleteCustomer(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .delete(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(204).assertThat();
    }

    public OBErrorResponse1 deleteCustomerNegativeFlow(final AlphaTestUser alphaTestUser, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .delete(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }


    /**
     * @param alphaTestUser
     * @return
     */
    public OBReadCustomer1 getCurrentCustomer(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomer1.class);
    }

    public OBErrorResponse1 getCurrentCustomerError(final AlphaTestUser alphaTestUser, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public void getCurrentCustomerVoidError(final AlphaTestUser alphaTestUser, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat();
    }


    private OBIdvDetails1 prepareData(AlphaTestUser alphaTestUser){
        OBIdType idType = alphaTestUser.getIdType() == null ? OBIdType.IDCARD:  alphaTestUser.getIdType();

        String documentNumber = idType == OBIdType.IDCARD ?  alphaTestUser.getEid() : "1234567";
        String idNumber = idType == OBIdType.IDCARD ?  "123456789" : null;

        OBIdvDetails1 obIdvDetails1 = OBIdvDetails1.builder()
                .result(OBResult.SUCCESS)
                .reason("TECH_PHOTO")
                .transactionNumber("cb3fe4cb-abd9-4647-841f-35ad8aec6f57")
                .identId("ABC-ABCDE")
                .idType(alphaTestUser.getIdType() == null ? OBIdType.IDCARD:  alphaTestUser.getIdType())
                .documentNumber(documentNumber)
                .idNumber(idNumber)
                .idCountry("AE")
                .dateOfExpiry(LocalDate.now().plusYears(3))
                .identificationTime(OffsetDateTime.of(2021, 01, 01, 10,
                        10, 0, 0, ZoneOffset.UTC))
                .gtcVersion("GTC-Version")
                .type(OBAppType.APP)
                .build();

        if (alphaTestUser.getIdType() != null && alphaTestUser.getIdType() != OBIdType.IDCARD) {
            obIdvDetails1.setNameEn(alphaTestUser.getName());
            obIdvDetails1.setDateOfBirth(alphaTestUser.getDateOfBirth());
            obIdvDetails1.setGender(alphaTestUser.getGender());
            obIdvDetails1.setIssuanceDate(LocalDate.now());
            obIdvDetails1.setIssuingAuthorityEn("IssuenceAuthority");
            obIdvDetails1.setDocumentSource(OBDocumentSource.IDNOW);
        }
        return obIdvDetails1;
    }

    public OBWriteIdvDetailsResponse1 createCustomerIdvDetails(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        SecureRandom random = new SecureRandom();

        OBIdvDetails1 idvDetails1 = prepareData(alphaTestUser);

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(idvDetails1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_IDVS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteIdvDetailsResponse1.class);
    }

    public OBWriteIdvDetailsResponse1 createCustomerIdvDetailsDuplicateDocumentNumber(
            final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        OBIdvDetails1 idvDetails1 = OBIdvDetails1.builder()
                .result(OBResult.SUCCESS)
                .reason("TECH_PHOTO")
                .transactionNumber("cb3fe4cb-abd9-4647-841f-35ad8aec6f57")
                .identId("ABC-ABCDE")
                .idType(OBIdType.IDCARD)
                .documentNumber("173273929316284")
                .idNumber("123456789")
                .idCountry("AE")
                .dateOfExpiry(LocalDate.of(2022, 01, 01))
                .identificationTime(OffsetDateTime.of(2021, 01, 01, 10,
                        10, 0, 0, ZoneOffset.UTC))
                .gtcVersion("GTC-Version")
                .type(OBAppType.APP)
                .build();

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(idvDetails1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_IDVS)
                .then()
                .log().all()
                .statusCode(409).assertThat()
                .extract().body().as(OBWriteIdvDetailsResponse1.class);
    }

    public void createCustomerIdvDetailsJSON(AlphaTestUser alphaTestUser, JSONObject idvDetails1, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(idvDetails1.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_IDVS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat();
    }

    public JSONObject createJSONIdvDetails(String changing, String value) throws JSONException {
        SecureRandom random = new SecureRandom();
        long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);
        return new JSONObject() {
            {
                put("Result", OBResult.SUCCESS);
                put("Reason", "TECH_PHOTO");
                put("TransactionNumber", "cb3fe4cb-abd9-4647-841f-35ad8aec6f57");
                put("IdentId", "ABC-ABCDE");
                put("IdType", OBIdType.IDCARD);
                put("DocumentNumber", String.valueOf(n));
                put("IdNumber", "123456789");
                put("IdCountry", "AE");
                put("DateOfExpiry", LocalDate.of(2022, 01, 01));
                put("IdentificationTime", OffsetDateTime.of(2021, 01, 01, 10,
                        10, 0, 0, ZoneOffset.UTC));
                put("GtcVersion", "GTC-Version");
                put("Type", OBAppType.APP);
                put(changing, value);
            }
        };
    }

    public OBReadIdvDetailsResponse1 getCustomerIdvDetails(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_IDVS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadIdvDetailsResponse1.class);
    }

    public OBErrorResponse1 getCustomerIdvDetailsNegative(final AlphaTestUser alphaTestUser, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getJwtToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_IDVS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadCustomerId1 getCustomersByEid(final String emiratesId) {
        return given()
                .config(config)
                .pathParam("emiratesId", emiratesId)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_EID_ID)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomerId1.class);
    }

    public OBErrorResponse1 getCustomerProtectedError(String invalidField, String queryParameter, int statusCode) {
        return given()
                .config(config)
                .queryParam(queryParameter, invalidField)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);

    }

    public OBWriteCustomerResponse1 updateCustomer(final AlphaTestUser alphaTestUser,
                                                   OBWritePartialCustomer1 obWriteCustomer1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .body(obWriteCustomer1)
                .patch(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }

    public void updateCustomerUsingJSON(final AlphaTestUser alphaTestUser,
                                        JSONObject obWriteCustomer1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCustomer1.toString())
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat();
    }

    public OBErrorResponse1 updateCustomerUsingJSONErrorResponse(final AlphaTestUser alphaTestUser,
                                                                 JSONObject obWriteCustomer1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCustomer1.toString())
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public JSONObject createJSONForUpdateCustomer(String changing, String value) throws JSONException {
        return new JSONObject() {
            {
                put("Data", new JSONObject() {
                    {
                        put("CustomerState", OBCustomerStateV1.IDV_COMPLETED);
                        put("FirstName", generateEnglishRandomString(10));
                        put("LastName", generateEnglishRandomString(20));
                        put("FullName", generateEnglishRandomString(30));
                        put("Gender", OBGender.MALE);
                        put("DateOfBirth", LocalDate.of(1995, 07, 12));
                        put("Email", generateRandomEmail());
                        put("MobileNumber", generateRandomMobile());
                        put("Nationality", "EN");
                        put(changing, value);
                    }
                });
            }
        };
    }

    public OBWriteCustomerResponse1 updateCustomerEmail(final AlphaTestUser alphaTestUser,
                                                        OBWritePartialCustomer1 obWriteCustomer1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }

    public OBErrorResponse1 patchCustomerError(final AlphaTestUser alphaTestUser,
                                               OBWritePartialCustomer1 obWriteCustomer1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBWriteInternalRiskRatingResponse1 addInternalRiskRating(final AlphaTestUser alphaTestUser,
                                                                    final OBWriteInternalRiskRating1 obWriteInternalRiskRating1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteInternalRiskRating1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RISKS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteInternalRiskRatingResponse1.class);
    }

    public Response addInternalRiskRatingChild(final AlphaTestUser alphaTestUser,
                                               final OBWriteInternalRiskRating1 obWriteInternalRiskRating1,
                                               String relationshipId,
                                               int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteInternalRiskRating1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_INTERNAL_RISKS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    public Response addInternalRiskRatingChildJson(final AlphaTestUser alphaTestUser,
                                                   final JSONObject obWriteInternalRiskRating1,
                                                   String relationshipId,
                                                   int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteInternalRiskRating1.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_CUSTOMERS_INTERNAL_RISKS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    public OBErrorResponse1 addInternalRiskRatingError(final AlphaTestUser alphaTestUser,
                                                       final OBWriteInternalRiskRating1 obWriteInternalRiskRating1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteInternalRiskRating1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RISKS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void addInternalRiskRatingError(final AlphaTestUser alphaTestUser,
                                           final JSONObject jsonObject, int statusCode) {

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_RISKS)
                .then()
                .log().all()
                .statusCode(statusCode);
    }

    public OBWriteCRSResponse2 addCRSDetails(final AlphaTestUser alphaTestUser,
                                             final OBWriteCRS2 obWriteCRS1) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCRS1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCRSResponse2.class);
    }

    public OBWriteTermResponse1 postCustomerTerms(final AlphaTestUser alphaTestUser,
                                                  OBWriteTerm1 obWriteTerm1) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteTerm1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_TERMS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteTermResponse1.class);
    }

    public void postCustomerTermsError(final AlphaTestUser alphaTestUser,
                                       JSONObject jsonObject, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_TERMS)
                .then()
                .log().all()
                .statusCode(statusCode);

    }

    public OBWriteTermResponse1 patchCustomerTerms(final AlphaTestUser alphaTestUser,
                                                   OBWriteTerm1 obWriteTerm1) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteTerm1)
                .when()
                .patch(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_TERMS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteTermResponse1.class);
    }


    public OBReadCustomer1 putCustomerCif(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body("")
                .when()
                .put(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CIF)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomer1.class);
    }


    public OBWriteCRSResponse2 postCRSDetails(final AlphaTestUser alphaTestUser,
                                              OBWriteCRS2 obWriteCRS2) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(obWriteCRS2)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CRS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCRSResponse2.class);
    }

    public OBCustomerAuthorization1 getCustomerAuthz(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_AUTHORIZATIONS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBCustomerAuthorization1.class);
    }

    /**
     * @param customerId
     * @return
     */
    public OBReadCustomer1 getCustomersByCustomerId(final String customerId) {

        return given()
                .config(config)
                .pathParam("customerId", customerId)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_ID)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomer1.class);
    }

    public OBErrorResponse1 putCustomerCifNegativeFlow(final AlphaTestUser alphaTestUser, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body("")
                .when()
                .put(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_CIF)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBWriteCustomerResponse1 updateCustomerValidations(final AlphaTestUser alphaTestUser,
                                                              OBWriteEIDStatus1 input) {
        return given()
                .config(config)
                .pathParam("customerId", alphaTestUser.getUserId())
                .log().all()
                .header(X_API_KEY, customerConfig.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(input)
                .when()
                .patch(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_ID_VALIDATIONS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }


    public OBReadCustomer1 getProfileUsingCIF(String cifNumber) {
        return given()
                .config(config)
                .pathParam("cifNumber", cifNumber)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_NUMBER_CIFS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().as(OBReadCustomer1.class);
    }

    public OBErrorResponse1 getProfileUsingCIFError(String cifNumber, int statusCode) {
        return given()
                .config(config)
                .pathParam("cifNumber", cifNumber)
                .log().all()
                .header(X_API_KEY, customerConfig.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_NUMBER_CIFS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 updateCustomerValidationsError(final AlphaTestUser alphaTestUser,
                                                           OBWriteEIDStatus1 input,
                                                           int statusCode) {
        return given()
                .config(config)
                .pathParam("customerId", alphaTestUser.getUserId())
                .log().all()
                .header(X_API_KEY, customerConfig.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(input)
                .when()
                .patch(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_ID_VALIDATIONS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 updateCustomerValidationsErrorJson(final AlphaTestUser alphaTestUser,
                                                               JSONObject input,
                                                               int statusCode) {
        return given()
                .config(config)
                .pathParam("customerId", alphaTestUser.getUserId())
                .log().all()
                .header(X_API_KEY, customerConfig.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(input.toString())
                .when()
                .patch(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_ID_VALIDATIONS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    @Deprecated //Moved to employment api
    public OBWriteEmploymentDetailsResponse1 createCustomerEmploymentDetails(final AlphaTestUser alphaTestUser,
                                                                             OBEmploymentDetails1 obEmploymentDetails1) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obEmploymentDetails1)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_EMPLOYMENTS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteEmploymentDetailsResponse1.class);
    }

    private void sanitizeCustomer(OBWritePartialCustomer1 obWriteCustomer1) {
        if (obWriteCustomer1 != null) {
            if (obWriteCustomer1.getData() != null) {
                sanitize(obWriteCustomer1.getData().getMobileNumber());
            }
        }
    }
}
