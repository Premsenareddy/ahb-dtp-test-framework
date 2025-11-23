package ahb.experience.borrow.creditCards.api;

import ahb.experience.creditCard.ExpApplicationService.EXPCreateApplication;
import ahb.experience.creditCard.ExperienceGetCreditCard;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

public class ApplicationService extends BaseApi{
    @Inject
    AuthConfiguration authConfiguration;

    private final String CREATE_APP = "/applications/protected/application";

    public EXPCreateApplication createApplication(String BearerToken, String createBody, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .body(createBody)
                .when()
                .post(authConfiguration.getExperienceBasePath() + CREATE_APP)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(EXPCreateApplication.class);
    }
}
