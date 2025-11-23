package uk.co.deloitte.banking.payments.transfer.internal.api;


import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsent4;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticConsentResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;

import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;


import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.*;


@Singleton
@Slf4j
public class InternalTransferApiFlows extends BaseApi {

    @Inject
    PaymentConfiguration paymentConfiguration;

    @Inject
    private AlphaKeyService alphaKeyService;


    private final String INTERNAL_TRANSFER_INTERNAL = "/internal/v1/internal-payments";
    private final String INTERNAL_TRANSFER_CONSENT_INTERNAL = "/internal/v1/internal-payment-consents";
    private final String RELATIONSHIP_INTERNAL_TRANSFER_INTERNAL = "/internal/v1/relationships/";

    public OBWriteDomesticConsentResponse5 createInternalPaymentConsent(final AlphaTestUser alphaTestUser, final OBWriteDomesticConsent4 consent4) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent4.toString());
        log.info("Cert validated and jws generated");

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(consent4)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNAL_TRANSFER_CONSENT_INTERNAL);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createInternalPaymentConsent request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticConsentResponse5.class);

    }

    public OBWriteDomesticConsentResponse5 createInternalPaymentConsentWithStatus(final AlphaTestUser alphaTestUser, final OBWriteDomesticConsent4 consent4, final int status) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent4.toString());
        log.info("Cert validated and jws generated");

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(consent4)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNAL_TRANSFER_CONSENT_INTERNAL);

        response.then().log().all().statusCode(status);

        log.info("Time taken for createInternalPaymentConsent request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticConsentResponse5.class);

    }

    public OBErrorResponse1 createInternalPaymentConsentError(final AlphaTestUser alphaTestUser, final OBWriteDomesticConsent4 consent4, int statusCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, consent4.toString());
        log.info("Cert validated and jws generated");

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(consent4)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNAL_TRANSFER_CONSENT_INTERNAL)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBWriteDomesticResponse5 createInternalTransferPayment(final AlphaTestUser alphaTestUser, final OBWriteDomestic2 obWriteDomestic2) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, obWriteDomestic2.toString());
        log.info("Cert validated and jws generated");

        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .contentType(ContentType.JSON)
                .body(obWriteDomestic2)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNAL_TRANSFER_INTERNAL);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createInternalTransferPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }



    public OBWriteDomesticResponse5 createRelationshipInternalTransferPayment(final AlphaTestUser alphaTestUser, final OBWriteDomestic2 obWriteDomestic2, final String relationshipId) {
        Response response = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(obWriteDomestic2)
                .when()
                .post(paymentConfiguration.getBasePath() + RELATIONSHIP_INTERNAL_TRANSFER_INTERNAL + relationshipId + "/internal-payments");

        response.then().log().all().statusCode(201);

        log.info("Time taken for createInternalTransferPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }


    public OBErrorResponse1 createInternalTransferPaymentError(final AlphaTestUser alphaTestUser, final OBWriteDomestic2 obWriteDomestic2, int statusCode) {
        String jws = alphaKeyService.validateCertForPayments(alphaTestUser, obWriteDomestic2.toString());
        log.info("Cert validated and jws generated");

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(obWriteDomestic2)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, jws)
                .when()
                .post(paymentConfiguration.getBasePath() + INTERNAL_TRANSFER_INTERNAL)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

}
