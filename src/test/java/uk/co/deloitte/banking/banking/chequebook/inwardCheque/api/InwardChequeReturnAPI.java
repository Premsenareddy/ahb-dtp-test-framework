package uk.co.deloitte.banking.banking.chequebook.inwardCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequeclearing.DebitReference;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequereturning.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequereturning.InwardChequeBookReturningRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class InwardChequeReturnAPI extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;

    public final String INWARD_CHEQUE_RETURNING = "/webhooks/v1/cheque/inward/return";

    public String inwardChequeReturning_requestPayload(){

        InwardChequeBookReturningRequest response_payload = new InwardChequeBookReturningRequest(new Data());

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(response_payload)
                .post(temenosConfig.getWebhook_url()+INWARD_CHEQUE_RETURNING)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}