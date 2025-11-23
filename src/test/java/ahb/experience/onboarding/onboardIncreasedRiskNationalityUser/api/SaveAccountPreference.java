package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api;

import ahb.experience.onboarding.ApplicationType;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;


public class SaveAccountPreference extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    private final String ACCTYPEDETAILS_URL = "/onboarding/protected/account/preference";
    public JSONObject getAccountJSON(String strValue) throws JSONException {
        JSONObject accJSON = new JSONObject() {
            {
                put("accountType", strValue);
            }
        };
        return accJSON;
    }

    public void captureAccountType(String BearerToken, ApplicationType type) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .body(getAccountJSON(type.getType()).toString())
                .when()
                .post(authConfiguration.getExperienceBasePath() +ACCTYPEDETAILS_URL)
                .then().log().all().statusCode(200).assertThat();
    }
}
