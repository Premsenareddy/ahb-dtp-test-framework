package uk.co.deloitte.banking.banking.utility.api;

import io.micronaut.http.HttpStatus;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.ahb.dtp.test.banking.cbAdaptor.CBAdaptorResponseError;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model.UtilityPaymentRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.utility.model.UtilityPaymentResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

@RequiredArgsConstructor
@Singleton
@Slf4j
public class UtilityPaymentApiFlows extends BaseApi {

    private static final String PROTECTED_UTILITY_PAYMENT_API = "/webhooks/v1/utility-payments";
    private static final String UTILITY_PAYMENT_API = "/internal/v1/utility-payments";

    @Inject
    private final BankingConfig bankingConfig;

    public <T> T createUtilityPaymentV1(final AlphaTestUser alphaTestUser, UtilityPaymentRequest1 body, final Class <T> classType, HttpStatus status) {
        return (T) given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .body(body)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .post(bankingConfig.getBasePath() + UTILITY_PAYMENT_API)
                .then().log().all().statusCode(status.getCode())
                .extract().body().as(classType);
    }

    public UtilityPaymentResponse1 doUtilityPayment(final UtilityPaymentRequest1 request) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_UTILITY_PAYMENT_API)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UtilityPaymentResponse1.class);
    }

    public CBAdaptorResponseError doUtilityPaymentNegative(final UtilityPaymentRequest1 request, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_UTILITY_PAYMENT_API)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(CBAdaptorResponseError.class);
    }
}
