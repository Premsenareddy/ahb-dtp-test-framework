package uk.co.deloitte.banking.payments.iban.api;

import io.restassured.http.ContentType;

import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6Data;
import uk.co.deloitte.banking.ahb.dtp.test.payment.iban.config.IbanConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.base.BaseApi.X_API_KEY;

public class IbanApi {

    @Inject
    IbanConfiguration ibanConfiguration;

    private final String IBAN_ENDPOINT = "/internal/v1/iban/";

    public void getIban(AlphaTestUser alphaTestUser, int statusCode, String iban) {
        given()
                .log().all()
                .header(X_API_KEY, ibanConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(ibanConfiguration.getBasePath() + IBAN_ENDPOINT + iban)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();

    }

    public OBErrorResponse1 getIbanError(AlphaTestUser alphaTestUser, int statusCode, String iban) {
        return  given()
                .log().all()
                .header(X_API_KEY, ibanConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(ibanConfiguration.getBasePath() + IBAN_ENDPOINT + iban)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);

    }
}
