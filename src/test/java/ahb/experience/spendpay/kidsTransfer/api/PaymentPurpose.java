package ahb.experience.spendpay.kidsTransfer.api;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin;
import ahb.experience.spendpay.kidsTransfer.DomesticPaymentPurposeAndCharges;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.InvalidAgeGroup;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.PurposeOfPayment;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton

public class PaymentPurpose extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    @Inject
    bankingUserLogin bankingUserLogin;

    private final String PAYMENT_PURPOSE_AHB = "/payments/protected/transfers/within-ahb/transfer-purpose/KID?language=en";
    private final String PAYMENT_PURPOSE_DOMESTIC = "/payments/protected/transfers/other-local-banks/transfer-purpose/KID?language=en";
    private final String INVALID_PAYMENT_PURPOSE_AHB= "/payments/protected/transfers/within-ahb/transfer-purpose/Kid?language=en";
    private final String PAYMENT_PURPOSE_AHB_TEENS = "/payments/protected/transfers/within-ahb/transfer-purpose/TEEN?language=en";
    private final String PAYMENT_PURPOSE_DOMESTIC_TEENS = "/payments/protected/transfers/other-local-banks/transfer-purpose/TEEN?language=en";

    public PurposeOfPayment[] paymentPurposeForAHBTransfersKids(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("language", "en")
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + PAYMENT_PURPOSE_AHB)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(PurposeOfPayment[].class);
    }

    public InvalidAgeGroup invalidAgeGroupRequest(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("language","en")
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + INVALID_PAYMENT_PURPOSE_AHB)
                .then().log().all().statusCode(400).assertThat()
                .extract().body().as(InvalidAgeGroup.class);

    }

    public DomesticPaymentPurposeAndCharges paymentPurposeForDomesticTransfersKids(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("language", "en")
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + PAYMENT_PURPOSE_DOMESTIC)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(DomesticPaymentPurposeAndCharges.class);
    }

    public PurposeOfPayment[] paymentPurposeForAHBTransfersTeens(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("language", "en")
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + PAYMENT_PURPOSE_AHB_TEENS)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(PurposeOfPayment[].class);
    }


    public DomesticPaymentPurposeAndCharges paymentPurposeForDomesticTransfersTeens(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .queryParam("language", "en")
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + PAYMENT_PURPOSE_DOMESTIC_TEENS)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(DomesticPaymentPurposeAndCharges.class);
    }

}
