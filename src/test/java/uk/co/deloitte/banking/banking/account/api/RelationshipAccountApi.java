package uk.co.deloitte.banking.banking.account.api;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1Data;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

@Slf4j
public class RelationshipAccountApi extends BaseApi {

    @Inject
    BankingConfig bankingConfig;

    public OBWriteAccountResponse1 createDependantCustomerAccount(final AlphaTestUser alphaTestUser,
                                                                  final OBWriteAccount1 request,
                                                                  final String relationshipId) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts")
                .then()
                .statusCode(201).assertThat()
                .log().all().extract().as(OBWriteAccountResponse1.class);
    }

    public OBErrorResponse1 createDependantCustomerAccount(final AlphaTestUser alphaTestUser,
                                                                  final OBWriteAccount1 request,
                                                                  final String relationshipId,
                                                                  int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts")
                .then()
                .statusCode(statusCode).assertThat()
                .log().all().extract().as(OBErrorResponse1.class);
    }


    public OBWriteAccount1 createYouthAccountData() {
        return OBWriteAccount1.builder()
                .data(OBWriteAccount1Data.builder()
                        .accountType(OBExternalAccountType1Code.AHB_YOUTH_SAV)
                        .accountSubType(OBExternalAccountSubType1Code.SAVINGS)
                        .build())
                .build();
    }

    public OBWriteAccount1 createChildAccountData(OBExternalAccountType1Code accountType1Code) {
        return OBWriteAccount1.builder()
                .data(OBWriteAccount1Data.builder()
                        .accountType(accountType1Code)
                        .accountSubType(OBExternalAccountSubType1Code.SAVINGS)
                        .build())
                .build();
    }

    public OBReadAccount6 getAccountDetails(AlphaTestUser alphaTestUser, String accountId, String relationshipId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadAccount6.class);
    }

    public OBErrorResponse1 getAccountDetails(AlphaTestUser alphaTestUser, String accountId, String relationshipId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadAccount6 getAccounts(AlphaTestUser alphaTestUser, String relationshipId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadAccount6.class);
    }

    public OBErrorResponse1 getAccounts(AlphaTestUser alphaTestUser, String relationshipId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadBalance1 getAccountBalances(AlphaTestUser alphaTestUser, String accountId, String relationshipId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId + "/balances")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadBalance1.class);
    }

    public OBErrorResponse1 getAccountBalances(AlphaTestUser alphaTestUser, String accountId, String relationshipId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId + "/balances")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadTransaction6 getLockedAmount(AlphaTestUser alphaTestUser, String accountId, String relationshipId, int page) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId +
                        "/transactions/pending?page=" + page)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBErrorResponse1 getLockedAmount(AlphaTestUser alphaTestUser, String accountId, String relationshipId, int page, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId +
                        "/transactions/pending?page=" + page)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadTransaction6 getAccountTransactions(AlphaTestUser alphaTestUser, String accountId, String relationshipId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId + "/transactions")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBErrorResponse1 getAccountTransactions(AlphaTestUser alphaTestUser, String accountId, String relationshipId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/" + accountId + "/transactions")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadTransaction6 getAccountStatementTransactions(AlphaTestUser alphaTestUser,String accountId, String statementId,String relationshipId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/"+accountId+"/statements/" + statementId + "/transactions")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBErrorResponse1 getAccountStatementTransactions(AlphaTestUser alphaTestUser,String accountId, String statementId,String relationshipId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/relationships/" + relationshipId + "/accounts/"+accountId+"/statements/" + statementId + "/transactions")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }
}
