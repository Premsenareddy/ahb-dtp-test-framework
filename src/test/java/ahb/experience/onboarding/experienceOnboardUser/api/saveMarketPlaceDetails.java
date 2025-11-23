package ahb.experience.onboarding.experienceOnboardUser.api;

import ahb.experience.onboarding.ExperienceFATCADetails;
import ahb.experience.onboarding.MarketPlaceUserDetails;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;


public class saveMarketPlaceDetails extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    MarketPlaceUserDetails marketPlaceUserDetails;

    private final static String PROTECTED_MARKET_PLACE_DETAILS= "/onboarding/protected/marketplace-personal-details";


    public void updateUser(String bearerToken, String strDeviceId, String strEmail, String strName, String strLanguage, String strGender, String strDOB,String strNationality,boolean termsAccepted, boolean consentToMarketingCommunication, boolean consentToPrivacyPolicy,int statusCode){
        marketPlaceUserDetails = MarketPlaceUserDetails.builder().name(strName).nationality(strNationality).
                language(strLanguage).gender(strGender).email(strEmail).dateOfBirth(strDOB).
                termsAccepted(termsAccepted).consentToMarketingCommunication(consentToMarketingCommunication)
                .consentToPrivacyPolicy(consentToPrivacyPolicy).build();

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer "+bearerToken)
                .header("accessToken", bearerToken)
                .queryParam("X-Request-Id", "TestUser-"+strDeviceId)
                .contentType(ContentType.JSON)
                .when()
                .body(marketPlaceUserDetails)
                .post(authConfiguration.getExperienceBasePath()+PROTECTED_MARKET_PLACE_DETAILS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();
    }
}
