package uk.co.deloitte.banking.banking.chequebook.api;

import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.base.BaseApi;


import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class InwardClearingApi extends BaseApi {

    @Inject
    BankingConfig bankingConfig ;


    private final String URL = "/webhooks/v1/cheque/onusLegacyInward/clearing";

    public String checkClearValidate() {

        return given()
                .config(config)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(getRequest())
                .post(bankingConfig.getInwardBasePath() + URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().contentType();

    }

    private String getRequest() {
        return "{\n" +
                "    \"Data\": {\n" +
                "        \"DebitAcctNo\": \"AED1756800100002\",\n" +
                "        \"DebitCurrency\": \"AED\",\n" +
                "        \"DebitValueDate\": \"20220207\",\n" +
                "        \"CreditAmount\": \"7.7\",\n" +
                "        \"DebitReferences\": [\n" +
                "            {\n" +
                "                \"DebitReference\": \"Ch-1234 rt-INF\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"CreditAcctNo\": \"30000008547\",\n" +
                "        \"CreditCurrency\": \"AED\",\n" +
                "        \"CreditReference\": \"DEP12354\",\n" +
                "        \"CreditValueDate\": \"20220207\",\n" +
                "        \"EndToEndReference\": \"O220100000100040\"\n" +
                "    }\n" +
                "}";
    }


}
