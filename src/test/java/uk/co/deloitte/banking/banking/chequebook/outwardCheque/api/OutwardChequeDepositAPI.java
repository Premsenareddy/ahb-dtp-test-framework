package uk.co.deloitte.banking.banking.chequebook.outwardCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequedeposit.OutwardChequeBookDepositRequest;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequedeposit.Data;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;

import static io.restassured.RestAssured.given;

@Singleton
public class OutwardChequeDepositAPI extends BaseApi {

    @Inject
    TemenosConfig temenosConfig;

    private final String DEPOSIT_OUTWARD_CHEQUE = "/webhooks/v1/cheque/outward/deposit";

    OutwardChequeBookDepositRequest request_payload = new OutwardChequeBookDepositRequest(new Data());

    public String outwardChequeDeposit(){
        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(request_payload)
                .post(temenosConfig.getWebhook_url()+DEPOSIT_OUTWARD_CHEQUE)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
    }
}