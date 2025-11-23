package ahb.experience.save.chequebook.api;

import ahb.experience.save.checkbook.CheckPINandEmiratesIdSchema;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

public class CheckUserPINandEmiratesIDExp extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

//    @Inject
//    CheckPINandEmiratesIdSchema checkPINandEmiratesIdSchema;

    public final String CHEQUE_ACCOUNT_LIST = "/accounts/protected/cheque/validate";

    public CheckPINandEmiratesIdSchema checkingUserPinandEmiratesid(String BearerToken){

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +CHEQUE_ACCOUNT_LIST)
                .then().log().all().statusCode(200)
                .assertThat()
//                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
                .extract().body().as(CheckPINandEmiratesIdSchema.class);
    }
}
