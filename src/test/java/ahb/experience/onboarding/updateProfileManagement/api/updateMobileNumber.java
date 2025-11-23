package ahb.experience.onboarding.updateProfileManagement.api;

import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;


public class updateMobileNumber extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    private final String UPDATEPROFILE_URL = "/onboarding/protected/profile";

    public JSONObject getMobileNumberValue(String strValue) throws JSONException {
        JSONObject mobileNumberObject = new JSONObject(){
            {
                put("mobileNumber", strValue);
            }
        };
        return mobileNumberObject;
    }

    public int getUpdateMobileNumberStatusCode(String BearerToken,String strMobNumber) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(getMobileNumberValue(strMobNumber).toString())
                .patch(authConfiguration.getExperienceBasePath()+UPDATEPROFILE_URL).getStatusCode();
    }
}
