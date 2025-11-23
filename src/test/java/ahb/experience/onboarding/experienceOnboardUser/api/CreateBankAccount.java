package ahb.experience.onboarding.experienceOnboardUser.api;

import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import java.util.Map;

import static io.restassured.RestAssured.given;


public class CreateBankAccount extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    private final String CREATEACCOUNT_URL = "/onboarding/protected/account";

    public void createAccount(String accessToken, Map<String, String> queryParams) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + accessToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .queryParams(queryParams)
                .when()
                .post(authConfiguration.getExperienceBasePath() +CREATEACCOUNT_URL)
                .then().log().all().statusCode(200).assertThat();
    }
}
