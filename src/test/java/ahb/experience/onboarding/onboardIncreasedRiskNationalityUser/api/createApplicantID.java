package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api;

import ahb.experience.onboarding.IDNowDocs.ExperienceApplicantID;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class createApplicantID extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    private final String CREATEAPPLICANT_URL = "/onboarding/protected/idnow";

    public ExperienceApplicantID createAppID(String bearerToken, Map<String, String> queryParams, int StatusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + bearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
//                .queryParam("documentType", strDocumentType)
//                .queryParam("applicantType", strApplicantType)
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .post(authConfiguration.getExperienceBasePath() +CREATEAPPLICANT_URL)
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(ExperienceApplicantID.class);
    }
}
