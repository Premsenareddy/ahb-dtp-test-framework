package uk.co.deloitte.banking.customer.employment.api;

import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentStatus;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBReadEmploymentDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBWriteEmploymentDetailsResponse1;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomInteger;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

public class EmploymentApiV2 extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    private static final String INTERNAL_V2_CUSTOMERS_EMPLOYMENTS = "/internal/v2/customers/employments";
    private static final String PROTECTED_V2_CUSTOMERS_EMPLOYMENTS = "/protected/v2/customers/{customerId}/employments";

    public OBWriteEmploymentDetailsResponse1 createEmploymentDetails(final AlphaTestUser alphaTestUser,
                                                                     OBEmploymentDetails1 employmentDetails) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(employmentDetails)
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_EMPLOYMENTS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBWriteEmploymentDetailsResponse1.class);
    }

    public OBReadEmploymentDetailsResponse1 getEmploymentDetails(final AlphaTestUser alphaTestUser) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_EMPLOYMENTS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadEmploymentDetailsResponse1.class);
    }

    public void getEmploymentInvalid(final AlphaTestUser alphaTestUser, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_EMPLOYMENTS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat();
    }

    public JSONObject employmentJSON(String toChange, String value) throws JSONException {
        JSONObject employment = new JSONObject() {
            {
                put("EmploymentStatus", OBEmploymentStatus.EMPLOYED);
                put("CompanyName", generateEnglishRandomString(20));
                put("EmployerCode", "800971");
                put("MonthlyIncome", "AED " + generateRandomInteger(999999));
                put("IncomeSource", generateEnglishRandomString(20));
                put("BusinessCode", "AAN");
                put("DesignationLAPSCode", "36");
                put("ProfessionCode", "99");
                put(toChange, value);
            }
        };

        return employment;
    }

    public OBWriteEmploymentDetailsResponse1 createEmploymentDetailsJson(final AlphaTestUser alphaTestUser,
                                                                         JSONObject employmentDetails, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(employmentDetails.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_EMPLOYMENTS)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().as(OBWriteEmploymentDetailsResponse1.class);
    }

    public OBErrorResponse1 createEmploymentDetailsJsonErrorResponse(final AlphaTestUser alphaTestUser,
                                                                     JSONObject employmentDetails, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(JSON)
                .body(employmentDetails.toString())
                .when()
                .post(customerConfig.getBasePath() + INTERNAL_V2_CUSTOMERS_EMPLOYMENTS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public JSONObject employmentMissingField(String missingField) throws JSONException {
        JSONObject employment = new JSONObject() {
            {
                put("EmploymentStatus", OBEmploymentStatus.EMPLOYED);
                put("CompanyName", generateEnglishRandomString(20));
                put("EmployerCode", "800971");
                put("MonthlyIncome", "AED " + generateRandomInteger(999999));
                put("IncomeSource", generateEnglishRandomString(20));
                put("BusinessCode", "AAN");
                put("DesignationLAPSCode", "36");
                put("ProfessionCode", "99");
                remove(missingField);
            }
        };

        return employment;
    }

    public JSONObject employmentMissingFieldAndChangingField(String missingField, String toChange, String value) throws JSONException {
        JSONObject employment = new JSONObject() {
            {
                put("EmploymentStatus", OBEmploymentStatus.EMPLOYED);
                put("CompanyName", generateEnglishRandomString(20));
                put("EmployerCode", "800971");
                put("MonthlyIncome", "AED " + generateRandomInteger(999999));
                put("IncomeSource", generateEnglishRandomString(20));
                put("BusinessCode", "AAN");
                put("DesignationLAPSCode", "36");
                put("ProfessionCode", "99");
                put(toChange, value);
                remove(missingField);
            }
        };

        return employment;
    }

    public OBReadEmploymentDetailsResponse1 getEmploymentDetailsForCustomerId(final AlphaTestUser alphaTestUser,String customerId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertNotNull(customerId);

        return given()
                .config(config)
                .pathParam("customerId", customerId)
                .log().all()
                .header(X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .when()
                .get(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_EMPLOYMENTS)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadEmploymentDetailsResponse1.class);
    }
}
