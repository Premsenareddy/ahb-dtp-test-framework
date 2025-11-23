package ahb.experience.onboarding.inApp.api;

import ahb.experience.InApp.request.FeedbackData;
import ahb.experience.InApp.request.InAppReqBody;

import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import io.restassured.http.ContentType;

import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import uk.co.deloitte.banking.account.api.banking.model.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.co.deloitte.banking.ahb.dtp.test.inappfeedback.config.InAppFeedbackConfiguration;
import uk.co.deloitte.banking.base.BaseApi;


import javax.inject.Inject;

import java.math.BigDecimal;
import java.util.Map;


import static io.restassured.RestAssured.given;


public class InAppFeedbackApis extends BaseApi {


    @Inject
    InAppFeedbackConfiguration inAppFeedbackConfiguration;
    FeedbackData feedbackData;
    InAppReqBody inAppReqBody;
    ExperienceLoginResponse experienceLoginResponse;

    private final String APPFeedBack_URL = "/protected/record";

    public InAppFeedbackApis inAppFeedback(String BearerToken,String userId, int StatusCode) {

        inAppReqBody = InAppReqBody.builder().customerId(userId).build();
        feedbackData = FeedbackData.builder().inAppReqBody(inAppReqBody).build();
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .contentType(ContentType.JSON)
                .body(feedbackData)
                .when()
                .post(inAppFeedbackConfiguration.getBaseurl()+APPFeedBack_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
        return this;

    }


    public InAppFeedbackApis inAppFeedbackData(String BearerToken, FeedbackData feedback, int StatusCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .contentType(ContentType.JSON)
                .body(feedback)
                .when()
                .post(inAppFeedbackConfiguration.getBaseurl()+APPFeedBack_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
        return this;

    }


}
