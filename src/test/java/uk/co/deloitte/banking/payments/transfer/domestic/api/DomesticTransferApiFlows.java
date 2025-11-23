package uk.co.deloitte.banking.payments.transfer.domestic.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_JWS_SIGNATURE;

@Singleton
@Slf4j
public class DomesticTransferApiFlows extends BaseApi {
    @Inject
    PaymentConfiguration paymentConfiguration;

    @Inject
    private AlphaKeyService alphaKeyService;

    private final String DOMESTIC_TRANSFER_CONSENT_INTERNAL = "/internal/v1/domestic-payment-consents";
    private final String DOMESTIC_TRANSFER_INTERNAL = "/internal/v1/domestic-payments";
    private final String RELATIONSHIP_DOMESTIC_TRANSFER_INTERNAL  = "/internal/v1/relationships/";

    public OBWriteDomesticConsentResponse5 createDomesticPaymentConsent(final AlphaTestUser alphaTestUser, final OBWriteDomesticConsent4 consent4) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent4.toString());
        log.info("Cert validated and jws generated");

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(consent4)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_CONSENT_INTERNAL);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createDomesticPaymentConsent request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticConsentResponse5.class);
    }

    public OBErrorResponse1 createDomesticPaymentConsentErrorResponse(final AlphaTestUser alphaTestUser, final OBWriteDomesticConsent4 consent4, int statusCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent4.toString());
        log.info("Cert validated and jws generated");

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(consent4)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_CONSENT_INTERNAL)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void createDomesticPaymentConsentErrorResponseVoid(final AlphaTestUser alphaTestUser, final OBWriteDomesticConsent4 consent4, int statusCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent4.toString());

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(consent4)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_CONSENT_INTERNAL)
                .then().log().all().statusCode(statusCode);
    }


    public void createDomesticPaymentConsentErrorResponseNoMap(final AlphaTestUser alphaTestUser, final OBWriteDomesticConsent4 consent4, int statusCode) {
        String jws =  alphaKeyService.validateCertForPayments(alphaTestUser, consent4.toString());

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(consent4)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_CONSENT_INTERNAL)
                .then().log().all().statusCode(statusCode);
    }


    public OBWriteDomesticResponse5 executeDomesticPayment(final AlphaTestUser alphaTestUser, final OBWriteDomestic2 obWriteDomestic2) {
        String jws =  alphaKeyService.validateCertForPayments(alphaTestUser, obWriteDomestic2.toString());

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(obWriteDomestic2)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_INTERNAL);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }

    public OBWriteDomesticResponse5 executeDomesticPayment(final AlphaTestUser alphaTestUser, final WriteDomesticPayment1 writeDomesticPayment1) {
        String jws =  alphaKeyService.validateCertForPayments(alphaTestUser, writeDomesticPayment1.toString());

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(writeDomesticPayment1)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_INTERNAL);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }



    public OBWriteDomesticResponse5 executeRelationshipDomesticPayment(final AlphaTestUser alphaTestUser, final WriteDomesticPayment1 writeDomesticPayment1 , final String relationshipId) {
        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(writeDomesticPayment1)
                .when()
                .post(paymentConfiguration.getBasePath() + RELATIONSHIP_DOMESTIC_TRANSFER_INTERNAL  + relationshipId + "/domestic-payments");

        response.then().log().all().statusCode(201);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }


    public OBErrorResponse1 createDomesticPaymentError(final AlphaTestUser alphaTestUser, final OBWriteDomestic2 domesticPayment, int statusCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, domesticPayment.toString());

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_INTERNAL)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 createDomesticPaymentError(final AlphaTestUser alphaTestUser, final WriteDomesticPayment1 domesticPayment, int statusCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, domesticPayment.toString());

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_INTERNAL)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void createDomesticPaymentErrorNoMap(final AlphaTestUser alphaTestUser, final OBWriteDomestic2 domesticPayment, int statusCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, domesticPayment.toString());

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .post(paymentConfiguration.getBasePath() + DOMESTIC_TRANSFER_INTERNAL)
                .then().log().all().statusCode(statusCode);
    }
}
