package ahb.experience.save.transactionList.api;

import ahb.experience.save.transactionList.TransactionListResponseSchema;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

public class transactionListParentApi extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    public TransactionListResponseSchema transactionList_parentApi(String BearerToken, String accountNumber){

        final String TRANSACTION_LIST = "/accounts/protected/"+accountNumber+"/transactions";
        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .queryParam("page", "1")
                .when()
                .get(authConfiguration.getExperienceBasePath() +TRANSACTION_LIST)
                .then().log().all().statusCode(200)
                .assertThat()
                .extract().body().as(TransactionListResponseSchema.class);
    }
}
