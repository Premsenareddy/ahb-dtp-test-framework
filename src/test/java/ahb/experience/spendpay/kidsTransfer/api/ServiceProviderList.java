package ahb.experience.spendpay.kidsTransfer.api;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.Example;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.InvalidAgeGroup;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton

public class ServiceProviderList extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    @Inject
    bankingUserLogin bankingUserLogin;

    private final String UTILITY_SERVICES_KIDS = "/payments/protected/bill-payments/categories/KID";
    private final String INVALID_UTILITY_SERVICES_KIDS = "/payments/protected/bill-payments/categories/Kid";
    private final String UTILITY_SERVICES_TEENS = "/payments/protected/bill-payments/categories/TEEN";
    private final String INVALID_UTILITY_SERVICES_TEENS = "/payments/protected/bill-payments/categories/teen";

    public Example serviceTypesForKids(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + UTILITY_SERVICES_KIDS)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(Example.class);

    }

    public Example serviceTypeForTeen(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + UTILITY_SERVICES_TEENS)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(Example.class);

    }


    public Example serviceTypesForTeens(String BearerToken) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + UTILITY_SERVICES_TEENS)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(Example.class);
    }

    public InvalidAgeGroup invalidAgeGroupTest(String BearerToken) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + INVALID_UTILITY_SERVICES_KIDS)
                .then().log().all().statusCode(400).assertThat()
                .extract().body().as(InvalidAgeGroup.class);
    }
}
