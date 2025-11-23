package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api;

import ahb.experience.onboarding.IDNowDocs.ExperienceIDNowDetails;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class getIDNOWDetails extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    private final String IDNOW_URL = "/onboarding/protected/v2/idnow";

    public ExperienceIDNowDetails getIDnowdetails(String bearerToken, String strUserType) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + bearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParam("applicantType", strUserType)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +IDNOW_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ExperienceIDNowDetails.class);
    }

    public ExperienceIDNowDetails getIDnowdetails(String bearerToken, Map<String, String> queryParams) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + bearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +IDNOW_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ExperienceIDNowDetails.class);
    }
}
