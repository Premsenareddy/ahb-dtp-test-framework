package uk.co.deloitte.banking.payments.certificate.api;


import uk.co.deloitte.banking.ahb.dtp.test.certificate.config.CertificateConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.*;

public class CertificateProtectedApi extends BaseApi {

    private static final String INTERNAL_CERTIFICATE_PATH = "/protected/v1/certificates";

    @Inject
    private CertificateConfig certificateConfig;


    public String validateCertificate(final AlphaTestUser alphaTestUser, String payload, String signedSignature, Integer responseCode) {
        return validateCertificate(alphaTestUser, alphaTestUser.getDeviceId(), payload, signedSignature, responseCode);
    }

    /**
     * @param alphaTestUser
     * @return
     */
    public String validateCertificate(final AlphaTestUser alphaTestUser, String deviceId, String payload, String signedSignature, Integer responseCode) {

        assertNotNull(alphaTestUser);

        return given()
                .config(config)
                .header(HEADER_X_DEVICE_ID, deviceId )
                .header(HEADER_X_JWS_SIGNATURE, signedSignature )
                .header(HEADER_X_API_KEY, certificateConfig.getApiKey())
                .log().all()
                .contentType(APPLICATION_JSON)
                .body(payload)
                .when()
                .put(certificateConfig.getBasePath() + INTERNAL_CERTIFICATE_PATH + "/" + alphaTestUser.getUserId())
                .then()
                .log().all()
                .statusCode(responseCode).assertThat()
                .extract().body().toString();
    }
}
