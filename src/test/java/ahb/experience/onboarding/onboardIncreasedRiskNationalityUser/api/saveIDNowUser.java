package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api;

import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class saveIDNowUser extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    private final String SAVEIDNOWUSER_URL = "/onboarding/protected/idnow/save";

     JSONObject saveIDUser_JSON(String strFirstname, String strLastname,String strFullname) throws JSONException {
        JSONObject tokenrequest = new JSONObject(){
            {
                    put("firstName", strFirstname).
                    put("lastName", strLastname);
                    put("fullName", strFullname);
            }
        };
        return tokenrequest;
    }

    public void saveIDUser(String BearerToken, String strFirstname, String strLastname, String strFullname, Map<String, String> queryParams, int StatusCode) {
                 given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .body(saveIDUser_JSON(strFirstname,strLastname,strFullname).toString())
                .post(authConfiguration.getExperienceBasePath() +SAVEIDNOWUSER_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
    }
}
