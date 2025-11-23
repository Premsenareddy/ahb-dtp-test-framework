package uk.co.deloitte.banking.banking.chequebook.inwardCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse.Header;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse.Audit;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse.Body;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse.InwardReturnChequeReverseRequest;

import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class InwardReturnChequeReverseAPI extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;



    public String inwardReturnChequeReverse_requestPayload(String chequeId){

        final String INWARD_CHEQUE_REVERSE_RETURNING = "/webhooks/v1/cheque/inward/reverse/"+ chequeId;
        InwardReturnChequeReverseRequest response_payload = new InwardReturnChequeReverseRequest(new Data(new Header(new Audit()),new Body()));

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(response_payload)
                .delete(temenosConfig.getWebhook_url()+INWARD_CHEQUE_REVERSE_RETURNING)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}