package ahb.experience.onboarding.experienceOnboardUser.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;


public class bankingUserLogin_Para extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    private final String LOGIN_URL = "/onboarding/public/customer/login";


    public JSONObject getLoginObject_Common(String deviceID, String mobileNumber, String passcode) throws JSONException {
        JSONObject tokenrequest = new JSONObject(){
            {
                        put("deviceId", deviceID).
                        put("mobileNumber", mobileNumber).
                        put("passcode", passcode);
            }
        };
        return tokenrequest;
    }

    public String getAccessToken_Common(String deviceID, String mobileNumber, String passcode, String jwsSignature) throws JSONException {
        RestAssured.defaultParser = Parser.JSON;
        System.out.print("URL ++++++"+authConfiguration.getExperienceBasePath()+LOGIN_URL);
        return given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("x-jws-signature", jwsSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(getLoginObject_Common(deviceID,mobileNumber,passcode).toString())
                .post(authConfiguration.getExperienceBasePath()+LOGIN_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().jsonPath().getString("token.accessToken");
    }
}
