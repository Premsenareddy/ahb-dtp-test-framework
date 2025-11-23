package ahb.experience.onboarding.experienceOnboardUser.api;

import ahb.experience.onboarding.VerifyCustomer;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;


public class VerifyMobileNumber extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    VerifyCustomer verifyCustomer;

    private final String MOBILENUMBER_URL = "/onboarding/public/customer/registered";


    public String verifyMobile(String strMobileNumber) {
        //verifyCustomer = new VerifyCustomer();
        StringBuilder sb = new StringBuilder();
        sb.append("%2B971");
        sb.append(strMobileNumber);

        return given()
                .config(config)
                .log().all()
                //.header("Authorization", "Bearer " + BearerToken)
                .queryParam("mobileNumber",sb.toString())
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                //.body(experienceCRSDetails)
                .when()
                .get(authConfiguration.getExperienceBasePath() +MOBILENUMBER_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().jsonPath().getString("exists");
    }
}
