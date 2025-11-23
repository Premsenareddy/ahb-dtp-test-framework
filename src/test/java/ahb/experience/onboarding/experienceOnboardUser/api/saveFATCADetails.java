package ahb.experience.onboarding.experienceOnboardUser.api;

import ahb.experience.onboarding.ExperienceFATCADetails;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import java.util.Map;

import static io.restassured.RestAssured.given;


public class saveFATCADetails extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    ExperienceFATCADetails experienceFATCADetails;

    private final String FATCADETAILS_URL = "/onboarding/protected/v2/fatca";


    public void captureFATCADetails(String BearerToken, boolean isUSCitizen, String strBirthCity, String strBirthCountry, Map<String, String> queryParams, int StatusCode) {
        experienceFATCADetails = ExperienceFATCADetails.builder().usCitizenOrResident(isUSCitizen).
                birthCity(strBirthCity).birthCountry(strBirthCountry).
                build();

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .body(experienceFATCADetails)
                .when()
                .post(authConfiguration.getExperienceBasePath() +FATCADETAILS_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
    }
}
