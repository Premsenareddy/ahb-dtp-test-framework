package uk.co.deloitte.banking.banking.chequebook.api;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ValidateSigCapApi extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    private final String URL = "internal/v1/share/sigcap/";

    private final String DOC_URL = "https://experience.cit.alpha-platform.uk/accounts/protected/uploadDocument?docType=UAEPASS_SIGNATURE";
    public String sigCapValidate(final String token, String sigCap) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " +token)
                .contentType(ContentType.JSON)
                .when()
                .post(customerConfig.getEligibilityPath() + URL + sigCap)
                .then().log().all().statusCode(403).assertThat()
                .extract().contentType();

    }

    public Map<String, String> getSinCapCode(String token) {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("document", "image.jpg");
        return given()
                .config(RestAssured.config().encoderConfig(EncoderConfig
                        .encoderConfig()
                        .encodeContentTypeAs("multipart/form-data", ContentType.TEXT)))
                .contentType("multipart/form-data; boundary=--MyBoundary")
                .log().all()
                .header("Authorization", "Bearer " +token)
                .header("Content-Type", "multipart/form-data")
                .header("x-api-key", "d8d4a69e-beb6-4878-be50-ee3455fc09f9")
                .header("Content-Type","multipart/form-data;boundary=--MyBoundary")
                .queryParams(formParams)
                .when()
                .post(DOC_URL)
                .then().log().all().statusCode(500).assertThat()
                .extract().as(HashMap.class);
    }

}
