package uk.co.deloitte.banking.payments.certificate.api;


import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.certificate.config.CertificateConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.TEXT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.*;

public class CertificateApi extends BaseApi {

    private static final String INTERNAL_CERTIFICATE_PATH = "/internal/v1/certificates";

    @Inject
    private CertificateConfig certificateConfig;

    /**
     * @param alphaTestUser
     * @return
     */
    public String uploadCertificate(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId() )
                .log().all()
                .contentType(TEXT)
                .body(alphaTestUser.getPublicKeyBase64())
                .when()
                .post(certificateConfig.getBasePath() + INTERNAL_CERTIFICATE_PATH)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().toString();
    }

    public String uploadCertificateForbbidenError(final AlphaTestUser alphaTestUser) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId() )
                .log().all()
                .contentType(TEXT)
                .body(alphaTestUser.getPublicKeyBase64())
                .when()
                .post(certificateConfig.getBasePath() + INTERNAL_CERTIFICATE_PATH)
                .then()
                .log().all()
                .statusCode(403).assertThat()
                .extract().body().toString();
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public String validateCertificate(final AlphaTestUser alphaTestUser, String payload, String signedSignature, Integer responseCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));


        return given()
                .config(config)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId() )
                .header(HEADER_X_JWS_SIGNATURE, signedSignature )
                .header(HEADER_X_API_KEY, certificateConfig.getApiKey())
                .log().all()
                .contentType(APPLICATION_JSON)
                .body(payload)
                .when()
                .put(certificateConfig.getBasePath() + INTERNAL_CERTIFICATE_PATH)
                .then()
                .log().all()
                .statusCode(responseCode).assertThat()
                .extract().body().toString();
    }

    public String validateUserCertificate(final AlphaTestUser alphaTestUser, UpdateUserRequestV1 payload, String signedSignature, Integer responseCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));


        return given()
                .config(config)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId() )
                .header(HEADER_X_JWS_SIGNATURE, signedSignature )
                .header(HEADER_X_API_KEY, certificateConfig.getApiKey())
                .log().all()
                .contentType(APPLICATION_JSON)
                .body(payload)
                .when()
                .put(certificateConfig.getBasePath() + INTERNAL_CERTIFICATE_PATH)
                .then()
                .log().all()
                .statusCode(responseCode).assertThat()
                .extract().body().toString();
    }








}
