package uk.co.deloitte.banking.payments.transfer.international.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternational3;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternationalConsent5;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternationalConsentResponse6;
import uk.co.deloitte.banking.account.api.payment.model.international.OBWriteInternationalResponse5;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_JWS_SIGNATURE;

@Singleton
@Slf4j
public class InternationalTransferApiFlows extends BaseApi {

    @Inject
    PaymentConfiguration paymentConfiguration;

    @Inject
    private AlphaKeyService alphaKeyService;

    private final String INTERNATIONAL_TRANSFER_CONSENT_INTERNAL = "/internal/v1/international-payment-consents";
    private final String INTERNATIONAL_TRANSFER_INTERNAL = "/internal/v1/international-payments";

    public OBWriteInternationalConsentResponse6 createInternationalPaymentConsent(final AlphaTestUser alphaTestUser, final OBWriteInternationalConsent5 consent5) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent5.toString());
        log.info("Cert validated and jws generated");

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(consent5)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNATIONAL_TRANSFER_CONSENT_INTERNAL);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createDomesticPaymentConsent request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteInternationalConsentResponse6.class);
    }

    public OBWriteInternationalResponse5 executeInternationalPayment(final AlphaTestUser alphaTestUser, final OBWriteInternational3 obWriteInternational3) {

        String jws =  alphaKeyService.validateCertForPayments(alphaTestUser, obWriteInternational3.toString());

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(obWriteInternational3)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNATIONAL_TRANSFER_INTERNAL);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createInternationalPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteInternationalResponse5.class);
    }


    public OBErrorResponse1 internationalPaymentConsentError(final AlphaTestUser alphaTestUser, final OBWriteInternationalConsent5 consent5,int responseCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent5.toString());
        log.info("Cert validated and jws generated");

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(consent5)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNATIONAL_TRANSFER_CONSENT_INTERNAL)
                .then().log().all().statusCode(responseCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);


    }
    public OBErrorResponse1 internationalPaymentError(final AlphaTestUser alphaTestUser, final OBWriteInternational3 paymentRequest,int responseCode) {

        String jws =  alphaKeyService.validateCertForPayments(alphaTestUser, paymentRequest.toString());

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNATIONAL_TRANSFER_INTERNAL)
                        .then().log().all().statusCode(responseCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);


    }
    public void internationalPaymentConsentErrorWithoutJws(final AlphaTestUser alphaTestUser, final OBWriteInternationalConsent5 consent5,String jws, int responseCode) {

        log.info("Cert validated and jws generated");

         given()
                .config(config)
                .log().all()

                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                //.contentType("application/json")
                .body(consent5)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNATIONAL_TRANSFER_CONSENT_INTERNAL)
                .then().log().all().statusCode(responseCode).assertThat();
                //.extract().body().as(OBErrorResponse1.class);


    }
    public void internationalPaymentErrorWithoutJws(final AlphaTestUser alphaTestUser, final OBWriteInternational3 obWriteInternational3,String jws, int responseCode) {
       given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(obWriteInternational3)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNATIONAL_TRANSFER_INTERNAL)

        .then().log().all().statusCode(responseCode).assertThat();

    }
}
