package uk.co.deloitte.banking.banking.account.api;

import io.micronaut.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1Data;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBCreditDebitCode0;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountSubType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.banking.account.model.AccountBalance;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.payments.beneficiary.PaymentRequestUtils;
import uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.HEADER_X_API_KEY;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

@Slf4j
public class AccountApi extends BaseApi {

    private static final String CURRENCY = "AED";

    private static final String GET_ACCOUNT_WEBHOOK = "/webhooks/v1/accounts/{accountId}";
    private static final String GET_ACCOUNT_PROTECTED = "/protected/v1/accounts/{accountId}";
    private static final String GET_ACCOUNT_BY_CIF_WEBHOOK = "/webhooks/v1/accounts/cifs/{cif}";
    private static final String GET_ACCOUNT_BALANCE_WEBHOOK = "/webhooks/v1/accounts/{accountId}/balances";
    private static final String GET_TRANSACTION_BY_REF = "/internal/v1/accounts/{AccountId}/transactions/{TransactionReference}";
    private static final String GET_TRANSACTION_BY_ACT_V2= "/internal/v2/accounts/{accountId}/transactions";
    private static final String GET_ACCOUNT_BALANCE = "/api/v1.0.0/holdings/ahb/accounts/{accountId}/balances";
    private static final String GET_ACCOUNT_CASHDEPOSIT = "/webhooks/v1/cashdeposit/accounts/{accountId}";
    private static final String GET_ACCOUNT_CASHDEPOSIT_CIF = "/webhooks/v1/cashdeposit/accounts/cifs/{cif}";

    @Inject
    BankingConfig bankingConfig;

    public OBWriteAccountResponse1 createCustomerSavingsAccount(final AlphaTestUser alphaTestUser) {
        return createCustomerAccountExec(alphaTestUser, createAccountData(), "v1");
    }

    public OBWriteAccountResponse1 createAccount(final AlphaTestUser alphaTestUser, OBExternalAccountType1Code type, OBExternalAccountSubType1Code subType) {

        return createCustomerAccountExec(alphaTestUser, createAccountData(type, subType), "v1");
    }

    public OBWriteAccountResponse1 createCustomerCurrentAccount(final AlphaTestUser alphaTestUser) {
        return createCustomerAccountExec(alphaTestUser, createCurrentAccountData(), "v1");
    }


    private OBWriteAccountResponse1 createCustomerAccountExec(final AlphaTestUser alphaTestUser,
                                                              final OBWriteAccount1 request,
                                                              final String version) {


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
                .post(bankingConfig.getBasePath() + "/internal/" + version + "/accounts")
                .then()
                .statusCode(201).assertThat()
                .log().all().extract().as(OBWriteAccountResponse1.class);
    }

    public OBWriteDomesticResponse5 executeInternalTransfer(AlphaTestUser alphaTestUser, String debtorAccountId, final BigDecimal amount) {
        final OBWriteDomestic2 request = PaymentRequestUtils.prepareInternalTransferRequest(UUID.randomUUID().toString(),
                debtorAccountId, SchemeNamesConstants.ACCOUNT_NUMBER,
                alphaTestUser.getAccountNumber(), SchemeNamesConstants.ACCOUNT_NUMBER,
                amount, "reference", "unstructured", RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(bankingConfig.getBasePath() + "/internal/v0/internal-payments")
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteDomesticResponse5.class);

    }

    public OBWriteDomesticResponse5 executeInternalTransferBetweenAccounts(AlphaTestUser alphaTestUser, String creditorAccount, String debtorAccountId, final BigDecimal amount) {
        final OBWriteDomestic2 request = PaymentRequestUtils.prepareInternalTransferRequest(UUID.randomUUID().toString(),
                debtorAccountId, SchemeNamesConstants.ACCOUNT_NUMBER,
                creditorAccount, SchemeNamesConstants.ACCOUNT_NUMBER,
                amount, "reference", "unstructured",
                RandomStringUtils.randomAlphabetic(7) + "E2Eidenti");
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(bankingConfig.getBasePath() + "/internal/v1/internal-payments")
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteDomesticResponse5.class);
    }

    public OBReadTransaction6 accountTransactions(AlphaTestUser alphaTestUser, String accountId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId + "/transactions")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);

    }

    public <T> T accountTransactionsV2(AlphaTestUser alphaTestUser, String accountId, Map<String, String> queryParams, final Class<T> typeClass, HttpStatus statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParam("accountId", accountId)
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + GET_TRANSACTION_BY_ACT_V2)
                .then()
                .log().all()
                .statusCode(statusCode.getCode()).assertThat()
                .extract().body().as(typeClass);
    }

    public <T> T getAccountDetailsForCashDeposit(AlphaTestUser alphaTestUser, String accountId, final Class<T> typeClass, HttpStatus statusCode) {
        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParam("accountId", accountId)
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_CASHDEPOSIT)
                .then()
                .log().all()
                .statusCode(statusCode.getCode()).assertThat()
                .extract().body().as(typeClass);
    }

    public <T> T getAccountDetailsForCashDepositCIF(AlphaTestUser alphaTestUser, String cif, final Class<T> typeClass, HttpStatus statusCode) {
        RestAssured.defaultParser = Parser.JSON;
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParam("cif", cif)
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_CASHDEPOSIT_CIF)
                .then()
                .log().all()
                .statusCode(statusCode.getCode()).assertThat()
                .extract().body().as(typeClass);
    }


    public void getAccountDetailsForCashDeposit(AlphaTestUser alphaTestUser, String accountId, HttpStatus statusCode) {
        RestAssured.defaultParser = Parser.JSON;
        given()
            .config(config)
            .log().all()
            .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
            .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
            .pathParam("accountId", accountId)
            .when()
            .get(bankingConfig.getBasePath() + GET_ACCOUNT_CASHDEPOSIT)
            .then()
            .log().all()
            .statusCode(statusCode.getCode()).assertThat();
    }


    public OBErrorResponse1 accountTransactionsErrorResponse(AlphaTestUser alphaTestUser, String accountId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId + "/transactions")
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);

    }

    public OBReadAccount6 getAccountDetails(AlphaTestUser alphaTestUser, String accountId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadAccount6.class);
    }

    public OBErrorResponse1 getAccountDetails(AlphaTestUser alphaTestUser, String accountId, int statusCode) {
       return  given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId)
                .then()
                .log().all()
                .statusCode(statusCode).extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 getAccountDetailsWithError(AlphaTestUser alphaTestUser, String accountId, int errorCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId)
                .then()
                .log().all()
                .statusCode(errorCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public OBReadAccount6 getAccounts(AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadAccount6.class);
    }

    @Deprecated(forRemoval = false)
    private OBWriteAccount1 createAccountData() {
        return OBWriteAccount1.builder()
                .data(OBWriteAccount1Data.builder()
                        .accountType(OBExternalAccountType1Code.AHB_BASIC_SAV)
                        .accountSubType(OBExternalAccountSubType1Code.SAVINGS)
                        .build())
                .build();
    }

    private OBWriteAccount1 createCurrentAccountData() {
        return OBWriteAccount1.builder()
                .data(OBWriteAccount1Data.builder()
                        .accountType(OBExternalAccountType1Code.AHB_BASIC_CUR_AC)
                        .accountSubType(OBExternalAccountSubType1Code.CURRENT_ACCOUNT)
                        .build())
                .build();
    }


    private OBWriteAccount1 createAccountData(OBExternalAccountType1Code type, OBExternalAccountSubType1Code subType) {
        return OBWriteAccount1.builder()
                .data(OBWriteAccount1Data.builder()
                        .accountType(type)
                        .accountSubType(subType)
                        .build())
                .build();
    }

    private OBWriteAccount1 createAccountDataV2() {
        return OBWriteAccount1.builder()
                .data(OBWriteAccount1Data.builder()
                        .currency(CURRENCY)
                        .accountType(OBExternalAccountType1Code.AHB_BASIC_SAV)
                        .description("TEST_DESCRIPTION")
                        .nickname("TEST_NICKNAME")
                        .build())
                .build();
    }

    public OBReadBalance1 getAccountBalances(AlphaTestUser alphaTestUser, String debtorAccountId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + debtorAccountId + "/balances")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadBalance1.class);
    }

    public OBReadBalance1 getAccountBalances(String debtorAccountId, boolean charge) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .param("charge", charge)
                .get(bankingConfig.getBasePath() + "/webhooks/v1/accounts/" + debtorAccountId + "/balances")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadBalance1.class);
    }

    public OBReadBalance1 getAccountBalancesCharge(String debtorAccountId) {
        return getAccountBalances(debtorAccountId, true);
    }

    public OBErrorResponse1 getAccountBalances(AlphaTestUser alphaTestUser, String debtorAccountId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + debtorAccountId + "/balances")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }


    public OBReadTransaction6 getAccountStatementTransactions(AlphaTestUser alphaTestUser, String statementId) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("AccountId", alphaTestUser.getAccountNumber())
                .pathParams("TransactionReference", statementId)
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + GET_TRANSACTION_BY_REF)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBErrorResponse1 getAccountStatementTransactions(AlphaTestUser alphaTestUser, String accountId, String statementId, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("AccountId", accountId)
                .pathParams("TransactionReference", statementId)
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + GET_TRANSACTION_BY_REF)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }


    public OBReadTransaction6 getTransactionDetailsForAccountV2(AlphaTestUser alphaTestUser, final String accountId) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId + "/transactions?creditDebitIndicator=DEBIT&toBookingDateTime=" + LocalDateTime.parse(dateString()))
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBErrorResponse1 getTransactionDetailsForAccountV2(AlphaTestUser alphaTestUser, final String accountId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId + "/transactions?creditDebitIndicator=DEBIT&toBookingDateTime=" + LocalDateTime.parse(dateString()))
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    // search date needs to be in the future for tests run of friday and saturday due to UAE working week
    private String dateString() {
        final String DEFAULT_DATE_TIME_FORMAT_WITH_MILLI = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT_WITH_MILLI);
        if (LocalDateTime.now().getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            return DATE_TIME_FORMATTER.format(LocalDateTime.now().plusDays(3));
        } else if (LocalDateTime.now().getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            return DATE_TIME_FORMATTER.format(LocalDateTime.now().plusDays(2));
        } else if (LocalDateTime.now().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            return DATE_TIME_FORMATTER.format(LocalDateTime.now().plusDays(1));
        }
        return DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }

    public OBReadAccount6 getAccountsV2(AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadAccount6.class);
    }

    public OBReadTransaction6 getLockedAmount(AlphaTestUser alphaTestUser, String debtorAccountId, int page) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + debtorAccountId + "/transactions" +
                        "/pending?page=" + page)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBErrorResponse1 getLockedAmount(AlphaTestUser alphaTestUser, String debtorAccountId, int page, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + debtorAccountId + "/transactions" +
                        "/pending?page=" + page)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBReadTransaction6 getAccountTransactions(AlphaTestUser alphaTestUser, String accountId) {

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId + "/transactions")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBReadTransaction6 getAccountTransactionsWithRange(AlphaTestUser alphaTestUser, String accountId) {
        final LocalDateTime dateFrom =
                LocalDateTime.parse(
                        "2021-01-01T00:00:00.001", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        final LocalDateTime dateTo =
                LocalDateTime.parse(
                        "2021-12-01T00:00:00.001", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        final BigDecimal minAmount = new BigDecimal("-1123.99");
        final BigDecimal maxAmount = new BigDecimal("1123.99");

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .queryParam("fromBookingDateTime", dateFrom.toString())
                .queryParam("toBookingDateTime", dateTo.toString())
                .queryParam("minAmount", minAmount)
                .queryParam("maxAmount", maxAmount)
                .queryParam("creditDebitIndicator", OBCreditDebitCode0.DEBIT.toString())
                .queryParam("page", 1)
                .get(bankingConfig.getBasePath() + "/internal/v1/accounts/" + accountId + "/transactions")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadTransaction6.class);
    }

    public OBReadAccount6 getWebhookAccounts(AlphaTestUser alphaTestUser, String accountNumber) {
        return given().config(config)
                .log().body()
                .pathParams("accountId", accountNumber)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_WEBHOOK)
                .then()
                .log().body()
                .statusCode(200)
                .extract().body().as(OBReadAccount6.class);

    }

    public OBErrorResponse1 getWebhookAccounts(AlphaTestUser alphaTestUser, String accountNumber, int statusCode) {
        return given().config(config)
                .log().body()
                .pathParams("accountId", accountNumber)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_WEBHOOK)
                .then()
                .log().body()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);

    }

    public OBReadAccount6 getProtectedAccounts(String accountNumber, int statusCode) {
        return given().config(config)
                .log().all()
                .pathParams("accountId", accountNumber)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_API_KEY, bankingConfig.getApiKey())
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_PROTECTED)
                .then()
                .log().body()
                .statusCode(statusCode)
                .extract().body().as(OBReadAccount6.class);
    }

    public OBReadAccount6 getAccountByCif(AlphaTestUser alphaTestUser, String cif) {
        return given().config(config)
                .log().body()
                .pathParams("cif", cif)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getJwtToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_BY_CIF_WEBHOOK)
                .then()
                .log().body()
                .statusCode(200)
                .extract().body().as(OBReadAccount6.class);

    }

    public OBErrorResponse1 getAccountByCif(AlphaTestUser alphaTestUser, String cif, int statusCode) {
        return given().config(config)
                .log().body()
                .pathParams("cif", cif)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getJwtToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_BY_CIF_WEBHOOK)
                .then()
                .log().body()
                .statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);

    }

    public OBReadBalance1 getAccountBalancesATM(String accountId, Map<String, String> queryParams) {
        return given()
                .config(config)
                .log().body()
                .pathParams("accountId", accountId)
                .queryParams(queryParams)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_BALANCE_WEBHOOK)
                .then()
                .log().body()
                .statusCode(200)
                .extract().as(OBReadBalance1.class);
    }

    public OBErrorResponse1 getAccountBalancesATMError(String accountId, int statusCode) {
        return given()
                .config(config)
                .log().body()
                .pathParams("accountId", accountId)
                .queryParam("charge", "true")
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(bankingConfig.getBasePath() + GET_ACCOUNT_BALANCE_WEBHOOK)
                .then()
                .log().body()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public AccountBalance getAccountBalance(String accountId) {
        return given()
                .config(config)
                .relaxedHTTPSValidation()
                .log().body()
                .pathParams("accountId", accountId)
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getAccountBalancePath() + GET_ACCOUNT_BALANCE)
                .then()
                .log().body()
                .statusCode(200)
                .extract().as(AccountBalance.class);
    }
}
