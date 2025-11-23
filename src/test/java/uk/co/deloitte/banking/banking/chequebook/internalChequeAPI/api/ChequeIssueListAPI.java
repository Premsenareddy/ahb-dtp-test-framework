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

public class ChequeIssueListAPI extends BaseApi {

    @Inject
    public TemenosConfig temenosConfig;



    public String ChequeIssueList_requestPayload(String accountId){

        final String CHEQUE_ISSUE_LIST = "/internal/v1/cheque/issueList/" + accountId;

        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getWebhook_url()+CHEQUE_ISSUE_LIST)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().jsonPath().getString("Data.Header.Id");
//                .assertThat()
//                .extract().body().as(outwardChequeBookReturnSchema.class);
    }
}
