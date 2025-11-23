package uk.co.deloitte.banking.banking.chequebook.otherCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.onusinwardchequeClearance.OnusInwardChequeClearingRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.onusinwardchequeClearance.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class onusInwardChequeClearingAPI extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;

    public final String ONUS_INWARD_CHEQUE_CLEARING = "/webhooks/v1/cheque/onusInward/clearing";

    public String onusInwardChequeClearing_requestPayload(){

        OnusInwardChequeClearingRequest response_payload = new OnusInwardChequeClearingRequest(new Data());

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(response_payload)
                .post(temenosConfig.getWebhook_url()+ONUS_INWARD_CHEQUE_CLEARING)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}