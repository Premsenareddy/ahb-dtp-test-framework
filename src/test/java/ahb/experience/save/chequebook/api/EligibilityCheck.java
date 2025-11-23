package ahb.experience.save.chequebook.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;


import static io.restassured.RestAssured.given;

public class EligibilityCheck extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;


    public final String ELIGIBILITY_CHECK_URL = "/accounts/protected/cheque/eligibility";

    public Object checkEligibility(String BearerToken){

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +ELIGIBILITY_CHECK_URL)
                .then().log().status()
                .extract().body().as(Object.class);
    }
}
