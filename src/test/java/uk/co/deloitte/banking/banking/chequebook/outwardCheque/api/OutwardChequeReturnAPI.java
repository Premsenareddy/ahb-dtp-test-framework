package uk.co.deloitte.banking.banking.chequebook.outwardCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequereturning.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequereturning.OutwardChequeBookReturnRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;


@Singleton
public class OutwardChequeReturnAPI extends BaseApi {

    @Inject
    TemenosConfig temenosConfig;

    private final String RETURN_OUTWARD_CHEQUE = "/webhooks/v1/cheque/outward/deposit";

    public String outwardChequeReturning_requestPayload(String chequeID){
        Data chequeDetails = new Data("RETURNED", "CLA", chequeID);
        OutwardChequeBookReturnRequest response_payload = new OutwardChequeBookReturnRequest(chequeDetails);

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(response_payload)
                .post(temenosConfig.getWebhook_url()+RETURN_OUTWARD_CHEQUE)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}