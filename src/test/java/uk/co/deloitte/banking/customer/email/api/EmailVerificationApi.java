package uk.co.deloitte.banking.customer.email.api;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.EmailVerification;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.DevSimConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.otp.OtpConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class EmailVerificationApi extends BaseApi {

    @Inject
    DevSimConfiguration devSimConfiguration;

    @Inject
    OtpConfiguration otpConfiguration;

    private static final String INTERNAL_OTPS = "/internal/v1/otps";
    private static final String INTERNAL_V1_EMAILS = "/internal/v1/emails";
    private static final String PROTECTED_V1_EMAILS = "/protected/v1/emails";
    private static final String INTERNAL_V1_EMAILS_PROFILE = "/internal/v1/emails/profile";
    private static final String PROTECTED_V1_EMAILS_PROFILE = "/protected/v1/emails/profile";
    private static final String API_EMAILS = "/api/emails";


    public void generateEmailVerificationLink(AlphaTestUser alphaTestUser, String email, int statusCode) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("Email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(otpConfiguration.getBasePath() + INTERNAL_V1_EMAILS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();
    }

    public void generateProfileEmailVerificationLink(AlphaTestUser alphaTestUser, String email, int statusCode) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("Email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(otpConfiguration.getBasePath() + INTERNAL_V1_EMAILS_PROFILE)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();
    }


    public EmailVerification getEmailVerificationLink(AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getJwtToken());

        return given()
                .config(config)
                .queryParam("email", alphaTestUser.getUserEmail())
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(devSimConfiguration.getBasePath() + API_EMAILS)
                .then().log().all()
                .statusCode(200).assertThat()
                .extract().body().as(EmailVerification.class);
    }

    public void verifyEmailLink(AlphaTestUser alphaTestUser, EmailVerification emailVerification, int statusCode) {
        given()
                .config(config)
                .queryParam("email", emailVerification.getEmail())
                .queryParam("token", emailVerification.getToken())
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(otpConfiguration.getBasePath() + PROTECTED_V1_EMAILS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();
    }

    public void verifyProfileEmailLink(AlphaTestUser alphaTestUser, EmailVerification emailVerification, int statusCode) {
        given()
                .config(config)
                .queryParam("email", emailVerification.getEmail())
                .queryParam("token", emailVerification.getToken())
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(otpConfiguration.getBasePath() + PROTECTED_V1_EMAILS_PROFILE)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();
    }

    public OBErrorResponse1 verifyEmailLinkError(AlphaTestUser alphaTestUser,
                                                 EmailVerification emailVerification, int statusCode) {
        return given()
                .config(config)
                .queryParam("email", emailVerification.getEmail())
                .queryParam("token", emailVerification.getToken())
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(otpConfiguration.getBasePath() + PROTECTED_V1_EMAILS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }
}
