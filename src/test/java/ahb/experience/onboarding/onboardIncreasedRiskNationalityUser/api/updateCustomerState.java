package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api;

import ahb.experience.onboarding.ExperienceProfileDetails;
import ahb.experience.onboarding.UpdateCustomerState;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;


public class updateCustomerState extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    ExperienceProfileDetails experienceProfileDetails;
    UpdateCustomerState updateCustomerState;

    private final String CUSTOMERDETAILS_URL = "/protected/v2/customers/";


    public void updateCustomerState(String strCustId,String strCustomerState, int StatusCode) {
        experienceProfileDetails = new ExperienceProfileDetails();
        updateCustomerState = new UpdateCustomerState();
        experienceProfileDetails.setCustomerState(strCustomerState);
        updateCustomerState.setData(experienceProfileDetails);
        given()
                .config(config)
                .log().all()
                //.header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", "ec9c7211-f942-40e4-8823-38e9edeb7955")
                .contentType(ContentType.JSON)
                .body(updateCustomerState)
                .when()
                .patch(customerConfig.getBasePath()+CUSTOMERDETAILS_URL+strCustId)
                .then().log().all().statusCode(StatusCode).assertThat();
    }
}
