package uk.co.deloitte.banking.banking.chequebook.outwardCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequeclearing.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequeclearing.OutwardChequeBookClearingRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import static io.restassured.RestAssured.given;

@Singleton
public class OuwardChequeClearingAPI extends BaseApi {
    @Inject
    TemenosConfig temenosConfig;

    private final String CLEARING_OUTWARD_CHEQUE = "/webhooks/v1/cheque/outward/deposit";

    public String outwardChequeClearing_requestPayload(String chequeID) {
        Data chequeDetails = new Data("CLEARED", "20220208", chequeID, "Message");
        OutwardChequeBookClearingRequest response_payload = new OutwardChequeBookClearingRequest(chequeDetails);

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(response_payload)
                .post(temenosConfig.getWebhook_url()+CLEARING_OUTWARD_CHEQUE)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
    }
}