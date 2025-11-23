package uk.co.deloitte.banking.customer.refreshtoken.api;

import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.RefreshRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

public class RefreshTokenApi extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    private static final String PROTECTED_V2_USERS_REFRESH = "/protected/v2/users/refresh";
    private static final String PROTECTED_V2_DEVICES_REFRESH = "/protected/v2/devices/refresh";

    public LoginResponse refreshAccessToken(JSONObject refreshTokenJSON) throws JSONException {
        return refreshAccessToken(refreshTokenJSON.toString());
    }

    public LoginResponse refreshAccessToken(String refreshToken) throws JSONException {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(refreshToken)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_REFRESH)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(LoginResponse.class);
    }

    public UserLoginResponseV2 refreshAccessToken(RefreshRequest refreshRequest) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(refreshRequest)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_REFRESH)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UserLoginResponseV2.class);
    }

    public OBErrorResponse1 getNewAccessTokenErrorResponse(RefreshRequest refreshToken, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(refreshToken)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_REFRESH)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 getNewAccessTokenErrorResponse(JSONObject refreshTokenJSON, int statusCode) throws JSONException {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(refreshTokenJSON.toString())
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_REFRESH)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public UserLoginResponseV2 getNewAccessTokenDevices(RefreshRequest refreshRequest) throws JSONException {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(refreshRequest)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_DEVICES_REFRESH)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UserLoginResponseV2.class);
    }

    public OBErrorResponse1 getNewAccessTokenDevicesErrorResponse(RefreshRequest refreshRequest, int statusCode)
            throws JSONException {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(refreshRequest)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_DEVICES_REFRESH)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public JSONObject getJsonObject(String refreshToken) throws JSONException {
        JSONObject refreshTokenJSON = new JSONObject() {
            {
                put("RefreshToken", refreshToken);
            }
        };
        return refreshTokenJSON;
    }
}
