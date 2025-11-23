package ahb.experience.onboarding.experienceOnboardUser.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;


public class bankingUserLogin extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    private final String LOGIN_URL = "/onboarding/public/customer/login";


    public JSONObject getLoginObject() throws JSONException {
        JSONObject tokenrequest = new JSONObject() {
            {
                        put("deviceId", "4A7B2099-AE76-45F9-B3B3-391E5Ex5670").
                        put("mobileNumber", "+971581362265").
                        put("passcode", "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db");
            }
        };
        return tokenrequest;
    }

    public String getAccessToken() throws JSONException {
        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("x-jws-signature", "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVFDSUJnTEdnc3gxdUxZMjJuVkhHXC9OWlp6czVjZ0kwNlRhUjczazVLMmM2MXdOQWlCNHZDZ05vdUV6RWNVOVlneVlqbUZTUTNSVk1jdU1Cd0tpbDB6aWs2OFk1UT09In0.hJcktTz4W-L4O5XQaTnyjyOY-NVUI9I3o2e48m26ux6m20FP2FWTta9rmwctAaA4moOmYVLuuvmslQ9Lk0TC1A")
                .contentType(ContentType.JSON)
                .when()
                .body(getLoginObject().toString())
                .post(authConfiguration.getExperienceBasePath()+LOGIN_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().jsonPath().getString("token.accessToken");
    }
}
