package ahb.experience.onboarding.updateProfileManagement.api;

import ahb.experience.onboarding.EmailGenerateToken;
import ahb.experience.onboarding.ExperienceErrValidations;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.DevSimConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;


public class updateEmailAddress extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;

    @Inject
    DevSimConfiguration devSimConfiguration;

    private final String UPDATEEMAIL_GENERATETOKEN = "/api/emails";
    private final String UPDATEEMAIL_VALIDATEEMAIL = "/onboarding/public/emails/profile";
    private final String UPDATEEMAIL_UPDATEEMAIL = "/onboarding/protected/email/verification";

    public JSONObject getEmailJSON(String strValue) throws JSONException {
        JSONObject emailJSON = new JSONObject() {
            {
                put("email", strValue);
            }
        };
        return emailJSON;
    }

    public EmailGenerateToken generateEmailToken(String BearerToken, String strEmail) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParam("email", strEmail)
                .contentType(ContentType.JSON)
                .when()
                .get(devSimConfiguration.getBasePath()+UPDATEEMAIL_GENERATETOKEN)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(EmailGenerateToken.class);
    }

    public int validateEmail(String BearerToken,String strEmail,String strToken) {
        return  given()
                .config(config)
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParam("email", strEmail)
                .queryParam("token", strToken)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath()+UPDATEEMAIL_VALIDATEEMAIL)
                .getStatusCode();
    }

    public int updateEmail(String BearerToken,String jwsSignature,String strNewEmailAddress) {
        return  given()
                .config(config)
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("x-jws-signature", jwsSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(getEmailJSON(strNewEmailAddress).toString())
                .post(authConfiguration.getExperienceBasePath()+UPDATEEMAIL_UPDATEEMAIL)
                .getStatusCode();
    }

    public ExperienceErrValidations getUpdateEmailValidationErr(String BearerToken, String jwsSignature, String strNewEmailAddress) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("x-jws-signature", jwsSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(getEmailJSON(strNewEmailAddress).toString())
                .post(authConfiguration.getExperienceBasePath()+UPDATEEMAIL_UPDATEEMAIL)
                .then()
                .extract().body().as(ExperienceErrValidations.class);
    }
}
