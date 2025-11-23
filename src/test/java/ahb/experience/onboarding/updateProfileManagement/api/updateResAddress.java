package ahb.experience.onboarding.updateProfileManagement.api;

import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import java.util.Map;

import static io.restassured.RestAssured.given;


public class updateResAddress extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    private final String UPDATEPROFILE_URL = "/onboarding/protected/profile";

    public JSONObject getResidentialValue(Map<String, Object> newAddressPayLoad) throws JSONException {
        JSONObject addressObject = new JSONObject(newAddressPayLoad);
        JSONObject addressJSONValue = new JSONObject();
        addressJSONValue.put("address", addressObject);

        return addressJSONValue;
    }

    public int getUpdateResAddressStatusCode(String BearerToken, Map<String, Object> newAddressPayLoad) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(getResidentialValue(newAddressPayLoad).toString())
                .patch(authConfiguration.getExperienceBasePath()+UPDATEPROFILE_URL).getStatusCode();
    }
}
