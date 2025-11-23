package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api;

import ahb.experience.onboarding.TaxDetails.ExperienceCRSDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class saveTAXDetails extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    ExperienceCRSDetails experienceCRSDetails;
    TaxCountries taxCountries;

    private final String TAXDETAILS_URL = "/onboarding/protected/v2/tax";


    public void captureTaxDetails(String BearerToken, boolean isTaxID, String strSelectedReason, String strTaXCountry, Map<String, String> queryParams, int StatusCode) {
        taxCountries = TaxCountries.builder().haveTaxId(isTaxID).selectedReason(strSelectedReason).taxCountry(strTaXCountry).build();
        ArrayList<TaxCountries> listOfTaxCountires = new ArrayList<>();
        listOfTaxCountires.add(taxCountries);
        experienceCRSDetails = ExperienceCRSDetails.builder().taxCountries(listOfTaxCountires).build();

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .body(experienceCRSDetails)
                .when()
                .post(authConfiguration.getExperienceBasePath() +TAXDETAILS_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
    }
}
