package uk.co.deloitte.banking.banking.chequebook.otherCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.chequesettlement.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.chequesettlement.ChequeSettlementRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class ChequeSettlementAPI extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;

    public final String CHEQUE_SETTLEMENT = "/webhooks/v1/cheque/settlement";

    public String ChequeSettlement_requestPayload(){

        ChequeSettlementRequest response_payload = new ChequeSettlementRequest(new Data());

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(response_payload)
                .post(temenosConfig.getWebhook_url()+CHEQUE_SETTLEMENT)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}