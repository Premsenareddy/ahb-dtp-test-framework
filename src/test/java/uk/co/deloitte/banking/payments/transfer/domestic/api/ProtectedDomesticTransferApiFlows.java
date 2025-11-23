package uk.co.deloitte.banking.payments.transfer.domestic.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.payment.model.domestic.WriteDomesticPayment1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

@Singleton
@Slf4j
public class ProtectedDomesticTransferApiFlows extends BaseApi {

    @Inject
    BankingConfig bankingConfig;

    private final String DOMESTIC_TRANSFER_PROTECTED = "/protected/v1/domestic-payments";

    public OBWriteDomesticResponse5 createDomesticPayment(final WriteDomesticPayment1 domesticPayment) {
        Response response = given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .post(bankingConfig.getBasePath() + DOMESTIC_TRANSFER_PROTECTED);

        response.then().log().all().statusCode(201);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }

    @Deprecated
    public OBWriteDomesticResponse5 updateDomesticPayment(final OBWriteDomestic2 domesticPayment) {
        Response response = given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .put(bankingConfig.getBasePath() + DOMESTIC_TRANSFER_PROTECTED);

        response.then().log().all().statusCode(200);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }

    public OBWriteDomesticResponse5 updateDomesticPayment(final WriteDomesticPayment1 domesticPayment) {
        Response response = given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .put(bankingConfig.getBasePath() + DOMESTIC_TRANSFER_PROTECTED);

        response.then().log().all().statusCode(200);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }

    @Deprecated
    public OBWriteDomesticResponse5 deleteDomesticPayment(final OBWriteDomestic2 domesticPayment) {
        Response response = given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .delete(bankingConfig.getBasePath() + DOMESTIC_TRANSFER_PROTECTED);

        response.then().log().all().statusCode(200);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }

    public OBWriteDomesticResponse5 deleteDomesticPayment(final WriteDomesticPayment1 domesticPayment) {
        Response response = given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(domesticPayment)
                .when()
                .delete(bankingConfig.getBasePath() + DOMESTIC_TRANSFER_PROTECTED);

        response.then().log().all().statusCode(200);

        log.info("Time taken for createDomesticPayment request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response.as(OBWriteDomesticResponse5.class);
    }

}