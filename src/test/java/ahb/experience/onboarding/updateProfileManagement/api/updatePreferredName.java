package ahb.experience.onboarding.updateProfileManagement.api;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;
import javax.inject.Inject;
import static io.restassured.RestAssured.given;

public class updatePreferredName extends BaseApi {
    @Inject
    AuthConfiguration authConfiguration;
    private final String UPDATENAME_URL = "/onboarding/protected/profile";
    public JSONObject getPreferredValue(String strValue) throws JSONException {
        JSONObject preferredValue = new JSONObject() {
            {
                put("preferredName", strValue);
            }
        };
        return preferredValue;
    }
    public int getUpdateNameStatusCode(String BearerToken,String valueToUpdate) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(getPreferredValue(valueToUpdate).toString())
                .patch(authConfiguration.getExperienceBasePath()+UPDATENAME_URL).getStatusCode();
    }
}
