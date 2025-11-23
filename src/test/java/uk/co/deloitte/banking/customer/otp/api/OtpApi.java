package uk.co.deloitte.banking.customer.otp.api;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DestinationRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ErrorResponse;
import uk.co.deloitte.banking.ahb.dtp.test.otp.OtpConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.ahb.dtp.test.util.PhoneNumberUtils.sanitize;

@Slf4j
public class OtpApi extends BaseApi {

    @Inject
    OtpConfiguration otpConfiguration;

    private static final String INTERNAL_OTPS = "/internal/v1/otps";
    private static final String INTERNAL_OTPS_VALIDATIONS = "/internal/v1/otps/validations";
    private static final String INTERNAL_RELATIONSHIPS_OTPS = "/internal/v1/relationships/{relationshipId}/otps";


    public void sendDestinationToOTP(final AlphaTestUser alphaTestUser, int statusCode) {

        final DestinationRequest destinationRequest = DestinationRequest.builder()
                .destination(sanitize(alphaTestUser.getUserTelephone()))
                .type("TEXT")
                .build();

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(destinationRequest)
                .post(otpConfiguration.getBasePath() + INTERNAL_OTPS)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body();

    }

    public ErrorResponse sendDestinationToOTPError(final AlphaTestUser alphaTestUser,
                                                   final DestinationRequest destinationRequest, int statusCode) {

        sanitize(destinationRequest.getDestination());

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(destinationRequest)
                .post(otpConfiguration.getBasePath() + INTERNAL_OTPS)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(ErrorResponse.class);

    }

    public void postOTPCode(final AlphaTestUser alphaTestUser, int statusCode, String optCode) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Otp", optCode);
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
                .post(otpConfiguration.getBasePath() + INTERNAL_OTPS_VALIDATIONS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();

    }

    public void postOTPCodeError(final AlphaTestUser alphaTestUser, int statusCode, String optCode) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Otp", optCode);
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
                .post(otpConfiguration.getBasePath() + INTERNAL_OTPS_VALIDATIONS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();

    }

    public void sentChildOTPCode(final AlphaTestUser alphaTestUser, int statusCode, String relationshipId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Destination", alphaTestUser.getUserTelephone());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .log().all()
                .header(X_API_KEY, otpConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(otpConfiguration.getBasePath() + INTERNAL_RELATIONSHIPS_OTPS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();
    }
}
