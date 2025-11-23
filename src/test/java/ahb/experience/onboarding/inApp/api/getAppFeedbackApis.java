package ahb.experience.onboarding.inApp.api;

import ahb.experience.InApp.request.FeedbackData;
import ahb.experience.InApp.request.InAppReqBody;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.Transactions.CardTransaction;
import uk.co.deloitte.banking.ahb.dtp.test.inappfeedback.config.InAppFeedbackConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;


public class getAppFeedbackApis extends BaseApi {


    @Inject
    InAppFeedbackConfiguration inAppFeedbackConfiguration;

    FeedbackData feedbackData;
    InAppReqBody inAppReqBody;
    ExperienceLoginResponse experienceLoginResponse;

    private final String getAPPFeedBack_URL = "/protected/retrieve/{userId}";


    public getAppFeedbackApis getinAppFeedback(String BearerToken, String userId, int StatusCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .pathParams("userId", userId)
                .contentType(ContentType.JSON)
                .when()
                .get(inAppFeedbackConfiguration.getBaseurl()+getAPPFeedBack_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
        return this;

    }
}
