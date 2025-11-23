package uk.co.deloitte.banking.customer.profile.api;

import io.restassured.response.ValidatableResponse;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.PhoneNumberUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.authorization.OBCustomerAuthorization1;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRSResponse2;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBReadEmploymentDetailsResponse2;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBWriteEmploymentDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.*;
import uk.co.deloitte.banking.customer.api.customer.model.internalrisk.OBWriteInternalRiskRating1;
import uk.co.deloitte.banking.customer.api.customer.model.internalrisk.OBWriteInternalRiskRatingResponse1;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEID;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;

public class CustomerApi extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    public static final String CUSTOMERS_PATH = "/internal/v2/customers";
    public static final String CUSTOMERS_PROFILE_PATH = "/internal/v2/customers/profile";
    public static final String PROTECTED_CUSTOMERS_PATH = "/protected/v2/customers";
    public static final String INTERNAL_RISKS = CUSTOMERS_PATH + "/internal-risks";


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
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
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
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH)
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
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH)
                .then().log().all().statusCode(errorCode).assertThat();

    }

    /**
     * @param email
     * @return
     */
    public OBReadCustomerId1 getCustomersByEmail(final String email) {

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_CUSTOMERS_PATH + "?email=" + email)
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
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_CUSTOMERS_PATH + "?mobile=" + number)
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
                .delete(customerConfig.getBasePath() + CUSTOMERS_PATH)
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
                .delete(customerConfig.getBasePath() + CUSTOMERS_PATH)
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

        return getCurrentCustomerRaw(alphaTestUser)
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomer1.class);
    }


    /**
     * @param alphaTestUser
     * @return
     */
    public ValidatableResponse getCurrentCustomerRaw(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + CUSTOMERS_PATH)
                .then()
                .log().all();
    }

    @Deprecated //Moved to employment api
    public OBWriteEmploymentDetailsResponse1 createCustomerEmploymentDetails(final AlphaTestUser alphaTestUser, OBEmploymentDetails1 obEmploymentDetails1) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obEmploymentDetails1)
                .when()
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH + "/employments")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteEmploymentDetailsResponse1.class);
    }

    public JSONObject customerEmployment(String employmentStatus, String companyName,
                                         String employerCode, String monthlyIncome,
                                         String incomeSource, String businessCode,
                                         String lapsCode, String professionCode) throws JSONException {
        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("EmploymentStatus", employmentStatus);
        jsonObject1.put("CompanyName", companyName);
        jsonObject1.put("EmployerCode", employerCode);
        jsonObject1.put("MonthlyIncome", monthlyIncome);
        jsonObject1.put("IncomeSource", incomeSource);
        jsonObject1.put("BusinessCode", businessCode);
        jsonObject1.put("DesignationLAPSCode", lapsCode);
        jsonObject1.put("ProfessionCode", professionCode);

        return jsonObject1;
    }

    public JSONObject customerEmployment(String employmentStatus) throws JSONException {

        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("EmploymentStatus", employmentStatus);
        jsonObject1.put("CompanyName", "AHB");
        jsonObject1.put("EmployerCode", "1010101");
        jsonObject1.put("MonthlyIncome", "AED 1234");
        jsonObject1.put("IncomeSource", "salary");
        jsonObject1.put("BusinessCode", "ADL");
        jsonObject1.put("DesignationLAPSCode", "36");
        jsonObject1.put("ProfessionCode", "99");

        return jsonObject1;
    }

    public void createInvalidCustomerEmploymentDetails(final AlphaTestUser alphaTestUser, JSONObject jsonEmployment, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(jsonEmployment.toString())
                .when()
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH + "/employments")
                .then()
                .log().all()
                .statusCode(statusCode);
    }

    public void readEmploymentFromCustomerNotFound(AlphaTestUser alphaTestUser) {
        given()
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + CUSTOMERS_PATH + "/employments")
                .then()
                .log().all()
                .statusCode(404);
    }

    public OBReadEmploymentDetailsResponse2 getCustomerEmploymentDetails(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + CUSTOMERS_PATH + "/employments")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadEmploymentDetailsResponse2.class);
    }

    public OBWriteIdvDetailsResponse1 createCustomerIdvDetails(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        OBIdvDetails1 idvDetails1 = OBIdvDetails1.builder()
                .result(OBResult.SUCCESS)
                .reason("TECH_PHOTO")
                .transactionNumber("cb3fe4cb-abd9-4647-841f-35ad8aec6f57")
                .identId("ABC-ABCDE")
                .idType(OBIdType.IDCARD)
                .documentNumber(generateRandomEID())
                .idNumber("123456789")
                .idCountry("AE")
                .dateOfExpiry(LocalDate.of(2022, 01, 01))
                .identificationTime(OffsetDateTime.of(2021, 01, 01, 10, 10, 0, 0, ZoneOffset.UTC))
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
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH + "/idvs")
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteIdvDetailsResponse1.class);
    }

    public ValidatableResponse createCustomerIdvDetailsJSON(AlphaTestUser alphaTestUser, JSONObject idvDetails1, int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(idvDetails1.toString())
                .when()
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH + "/idvs")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat();
    }

    public JSONObject createJSONIdvDetails(String changing, String value) throws JSONException {
        return new JSONObject() {
            {
                put("Result", OBResult.SUCCESS);
                put("Reason", "TECH_PHOTO");
                put("TransactionNumber", "cb3fe4cb-abd9-4647-841f-35ad8aec6f57");
                put("IdentId", "ABC-ABCDE");
                put("IdType", OBIdType.IDCARD);
//                put("DocumentNumber", "123-1234-1234567-1");
                put("DocumentNumber", RandomDataGenerator.generateRandomEID());
                put("IdNumber", RandomDataGenerator.generateRandomNumeric(9));
                put("IdCountry", "AE");
                put("DateOfExpiry", LocalDate.of(2022, 01, 01));
                put("IdentificationTime", OffsetDateTime.of(2021, 01, 01, 10, 10, 0, 0, ZoneOffset.UTC));
                put("GtcVersion", "GTC-Version");
                put("Type", OBAppType.APP);
                put(changing, value);
            }
        };
    }

    public OBCustomerAuthorization1 getCustomerAuthz(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + CUSTOMERS_PATH + "/authorizations")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBCustomerAuthorization1.class);
    }

    public OBReadIdvDetailsResponse2 getCustomerIdvDetails(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + CUSTOMERS_PATH + "/idvs")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadIdvDetailsResponse2.class);
    }

    public OBReadCustomerId1 getCustomersByEid(final String emiratesId) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_CUSTOMERS_PATH + "/eid/" + emiratesId)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadCustomerId1.class);
    }

    public OBErrorResponse1 getCustomersByInvalidEid(final String emiratesId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_CUSTOMERS_PATH + "/eid/" + emiratesId)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public void getCustomersByInvalidEidVoidResponse(final String emiratesId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + "/protected/v1/customers/eid/" + emiratesId)
                .then()
                .log().all()
                .statusCode(statusCode);
    }

    public OBReadCustomerId1 getCustomer(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .param("mobile", alphaTestUser.getUserTelephone())
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_CUSTOMERS_PATH)
                .then()
                .log().all()
                .statusCode(200)
                .extract().as(OBReadCustomerId1.class);
    }

    public OBErrorResponse1 getCustomerByErrorScenarios(String invalidfield, int statusCode, String queryParameter) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_CUSTOMERS_PATH + "?" + queryParameter + "=" + invalidfield)
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

        sanitizeCustomer(obWriteCustomer1);

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .patch(customerConfig.getBasePath() + CUSTOMERS_PATH)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }


    public OBWriteCustomerResponse1 updateCustomerProfile(final AlphaTestUser alphaTestUser,
                                                   OBWritePartialCustomer1 obWriteCustomer1, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        sanitizeCustomer(obWriteCustomer1);

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(obWriteCustomer1)
                .when()
                .patch(customerConfig.getBasePath() + CUSTOMERS_PROFILE_PATH)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }


    //WTF
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
                .patch(customerConfig.getBasePath() + CUSTOMERS_PATH)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat();
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
                .patch(customerConfig.getBasePath() + CUSTOMERS_PATH)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBWriteCustomerResponse1.class);
    }

    public OBErrorResponse1 updateCustomerEmailErrorScenarios(final AlphaTestUser alphaTestUser,
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
                .patch(customerConfig.getBasePath() + CUSTOMERS_PATH)
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
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH + "/crs")
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
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH + "/terms")
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
                .post(customerConfig.getBasePath() + CUSTOMERS_PATH + "/terms")
                .then()
                .log().all()
                .statusCode(statusCode);

    }

    public OBReadCustomer1 putCustomerCif(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body("")
                .when()
                .put(customerConfig.getBasePath() + CUSTOMERS_PATH + "/cif")
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
                .put(customerConfig.getBasePath() + CUSTOMERS_PATH + "/cif")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    private void sanitizeCustomer(OBWritePartialCustomer1 obWriteCustomer1) {
        if (obWriteCustomer1 != null) {
            if (obWriteCustomer1.getData() != null) {
                PhoneNumberUtils.sanitize(obWriteCustomer1.getData().getMobileNumber());
            }
        }
    }
}
