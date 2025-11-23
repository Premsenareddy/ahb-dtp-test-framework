package ahb.experience.onboarding.experienceOnboardUser.api;

import io.restassured.http.ContentType;
import ahb.experience.onboarding.ExperienceAddressDetails;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class saveResidentialAddress extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    ExperienceAddressDetails experienceAddressDetails;

    private final String SAVERESADDRESS_URL = "/onboarding/protected/customer/address";

    public void saveResAddressUser(String BearerToken, String strBuildName, String strBuildNum, String strStreet, String strVillageNum, String strEmirate, String strCity, List<String> lstAddressLine, Map<String, String> queryParams, int StatusCode) {
        experienceAddressDetails = ExperienceAddressDetails.builder()
                .buildingName(strBuildName).buildingNumber(strBuildNum).street(strStreet).addressLine(lstAddressLine)
                .villaNameNumber(strVillageNum).emirate(strEmirate).city(strCity).build();

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .body(experienceAddressDetails)
                .when()
                .post(authConfiguration.getExperienceBasePath() +SAVERESADDRESS_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
    }
}
