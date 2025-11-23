package uk.co.deloitte.banking.banking.chequebook.inwardCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequeclearing.InwardChequeBookClearingRequest;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequeclearing.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequeclearing.DebitReference;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@Singleton
public class InwardChequeClearingAPI extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;


    public final String INWARD_CHEQUE_CLEARING = "/webhooks/v1/cheque/inward/clearing";

    public String inwardChequeClearing_requestPayload() {

        InwardChequeBookClearingRequest response_payload = new InwardChequeBookClearingRequest(new Data());

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(response_payload)
                .post(temenosConfig.getWebhook_url()+INWARD_CHEQUE_CLEARING)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
    }
    }