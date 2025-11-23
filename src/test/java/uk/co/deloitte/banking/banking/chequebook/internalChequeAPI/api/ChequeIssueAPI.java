package uk.co.deloitte.banking.banking.chequebook.internalChequeAPI.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.chequeIssueInternal.Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.chequeIssueInternal.chequeIssueInternal;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

public class ChequeIssueAPI extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;

    public final String CHEQUE_ISSUE = "/internal/v1/cheque/issue";

    public String ChequeIssue_requestPayload(){

        chequeIssueInternal request_payload = new chequeIssueInternal(new Data());
        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(request_payload)
                .post(temenosConfig.getWebhook_url()+CHEQUE_ISSUE)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}
