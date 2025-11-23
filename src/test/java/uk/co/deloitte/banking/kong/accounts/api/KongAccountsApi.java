package uk.co.deloitte.banking.kong.accounts.api;

import io.restassured.http.ContentType;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.kongAccounts.KongAccountsConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;

import javax.inject.Inject;

import java.math.BigDecimal;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

public class KongAccountsApi  extends BaseApi {

    @Inject
    private KongAccountsConfig kongAccountsConfig;

    @Inject
    private CardProtectedApi cardProtectedApi;

    private static final String CARDS_DEPOSIT = "dtp/cards/deposits";
    private static final String GET_BALANCES = "{accountId}/balances";

    public OBReadAccount6 getAccounts(String accountNumber, int statusCode){
        return given().config(config)
                .log().body()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(X_API_KEY, kongAccountsConfig.getApiKey())
                .when()
                .get(kongAccountsConfig.getBasePath() + accountNumber)
                .then()
                .log().body()
                .statusCode(statusCode)
                .extract().body().as(OBReadAccount6.class);

    }

    public OBWriteCardDepositResponse1 createCardDepositKong(String creditorAccountId,
                                                         BigDecimal amount, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(X_API_KEY, kongAccountsConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(cardProtectedApi.createCardDepositData(creditorAccountId, amount))
                .when()
                .post(kongAccountsConfig.getBasePath() + CARDS_DEPOSIT)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBWriteCardDepositResponse1.class);
    }

    public OBReadBalance1 getAccountBalanceKong(String accountId, String isoBalance, int statusCode) {
        return given()
                .config(config)
                .log().body()
                .pathParams("accountId", accountId)
                .queryParam("isoBalance", isoBalance)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(X_API_KEY, kongAccountsConfig.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(kongAccountsConfig.getBasePath() + GET_BALANCES)
                .then()
                .log().body()
                .statusCode(statusCode)
                .extract().as(OBReadBalance1.class);
    }
}
