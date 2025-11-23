package ahb.experience.onboarding.updateProfileManagement.api;

import ahb.experience.onboarding.ExperienceProfileDetails;
import ahb.experience.onboarding.ExperienceErrValidations;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class getProfile extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    private final String UPDATEPROFILE_URL = "/onboarding/protected/profile";

    public JSONObject getUserJSONValue(Map<String, Object> newPayLoad) throws JSONException {
        return new JSONObject(newPayLoad);
    }


    public ExperienceProfileDetails getUserDetails(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath()+UPDATEPROFILE_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ExperienceProfileDetails.class);
    }
    public ExperienceErrValidations getUpdateProfileValidationErr(String BearerToken, Map<String, Object> newUserPayLoad) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(getUserJSONValue(newUserPayLoad).toString())
                .patch(authConfiguration.getExperienceBasePath()+UPDATEPROFILE_URL)
                .then()
                .extract().body().as(ExperienceErrValidations.class);
    }

    public ExperienceErrValidations getUpdateProfileValidationErr(String BearerToken, JSONObject jsonObject) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .patch(authConfiguration.getExperienceBasePath()+UPDATEPROFILE_URL)
                .then()
                .extract().body().as(ExperienceErrValidations.class);
    }
}
