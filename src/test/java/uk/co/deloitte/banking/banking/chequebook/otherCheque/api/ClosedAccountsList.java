package uk.co.deloitte.banking.banking.chequebook.otherCheque.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.chequesettlement.ChequeSettlementRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.chequesettlement.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

public class ClosedAccountsList extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;

    public final String CLOSED_ACCOUNTS_LIST = "/webhooks/v1/accounts/closed";

    public String ClosedAccountsList_requestPayload(String cusAccountId){

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .queryParam("cusAccountId", cusAccountId)
                .when()
                .get(temenosConfig.getWebhook_url()+CLOSED_ACCOUNTS_LIST)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}
