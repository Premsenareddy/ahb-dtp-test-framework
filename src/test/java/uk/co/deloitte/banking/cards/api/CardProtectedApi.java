package uk.co.deloitte.banking.cards.api;

import io.restassured.http.ContentType;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import uk.co.deloitte.banking.account.api.banking.model.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_JWS_SIGNATURE;

public class CardProtectedApi extends BaseApi {
    private static final String PROTECTED_ACCOUNTS = "/protected/v1/accounts/";
    private static final String WEBHOOKS_ACCOUNTS = "/webhooks/v1/accounts/";

    private static final String CARDS_PAYMENTS = "/cards/payments";

    private static final String CARDS_DEPOSITS = "/cards/deposits";

    private static final String CARDS_WITHDRAWALS = "/cards/withdrawals";

    private static final String CARDS_INTERNATIONAL_WITHDRAWALS = "/cards/international-withdrawals";

    private static final DecimalFormat amountFormat = new DecimalFormat("0000000000.00",
            new DecimalFormatSymbols(Locale.ENGLISH));

    @Inject
    BankingConfig bankingConfig;

    public OBWriteCardPaymentResponse1 createCardPayment(String debtorAccountId, BigDecimal amount) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(createCardPaymentData(debtorAccountId, amount))
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_ACCOUNTS + debtorAccountId + CARDS_PAYMENTS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCardPaymentResponse1.class);
    }

    public void createCardPaymentError(String debtorAccountId, BigDecimal amount, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(createCardPaymentData(debtorAccountId, amount))
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_ACCOUNTS + debtorAccountId + CARDS_PAYMENTS)
                .then()
                .log().all()
                .statusCode(statusCode);
    }

    public OBWriteCardDepositResponse1 createCardDeposit(String debtorAccountId,
                                                         String creditorAccountId,
                                                         BigDecimal amount) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(createCardDepositData(creditorAccountId, amount))
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_ACCOUNTS + debtorAccountId + CARDS_DEPOSITS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCardDepositResponse1.class);
    }

    public OBWriteCardWithdrawalResponse1 createCardWithdrawal(String debtorAccountId,
                                                         String creditorAccountId,
                                                         BigDecimal amount) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(createCardWithdrawalData(debtorAccountId, creditorAccountId, amount))
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_ACCOUNTS + debtorAccountId + CARDS_WITHDRAWALS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCardWithdrawalResponse1.class);
    }

    public OBWriteCardWithdrawalResponse1 createCardWithdrawalWebhooks(String debtorAccountId,
                                                                        BigDecimal amount,
                                                                       String fee,
                                                                       String termRef) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(createCardWithdrawalData(debtorAccountId, amount, fee, termRef))
                .when()
                .post(bankingConfig.getBasePath() + WEBHOOKS_ACCOUNTS + debtorAccountId + CARDS_WITHDRAWALS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCardWithdrawalResponse1.class);
    }

    private OBWriteCardPayment1 createCardPaymentData(String debtorAccountId, BigDecimal amount) {
        final String atUniqueId = RandomStringUtils.randomNumeric(12);
        final String retrievalReference = RandomStringUtils.randomNumeric(12);
        final String amountFormatted = amountFormat.format(amount).replace(".", "");

        return OBWriteCardPayment1.builder()
                .data(OBWriteCardPayment1Data.builder()
                        .initiation(OBWriteCardPayment1DataInitiation.builder()
                                .instructedAmount(OBWriteCardPayment1DataInitiationInstructedAmount.builder()
                                        .lockedAmount(amount.toString())
                                        .currency("AED")
                                        .build())
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .identification(debtorAccountId)
                                        .build())
                                .build())
                        .build())
                .supplementaryData(OBWriteCardPayment1SupplementaryData.builder()
                        .atUniqueId(atUniqueId)
                        .atmTermRef("VISA")
                        .retrievalReference(retrievalReference)
                        .cardNumber("BA0B4CA02072DE48")
                        .atLocalDate("240621")
                        .atTransDate("240621")
                        .terminalId("AHBD0042")
                        .atTraceNo("017659")
                        .atTxnAmount(amountFormatted)
                        .atProcCode("210000")
                        .merchantLoc("HILAL ABU DHABI AE")
                        .txnAmount(amountFormatted)
                        .txnCurrency("784")
                        .build())
                .build();
    }

    public OBWriteCardDeposit1 createCardDepositData(String creditorAccountId,
                                                      BigDecimal amount) {
        final String atUniqueId = RandomStringUtils.randomNumeric(12);
        final String retrievalReference = RandomStringUtils.randomNumeric(12);
        final String amountFormatted = amountFormat.format(amount).replace(".", "");

        return OBWriteCardDeposit1.builder()
                .data(OBWriteCardDeposit1Data.builder()
                        .initiation(OBWriteCardDeposit1DataInitiation.builder()
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .creditorAccount(OBWriteCardDeposit1DataInitiationCreditorAccount.builder()
                                        .identification(creditorAccountId)
                                        .build())
                                .build())
                        .build())
                .supplementaryData(OBWriteCardDeposit1SupplementaryData.builder()
                        .atUniqueId(atUniqueId)
                        .retrievalReference(retrievalReference)
                        .cardNumber("BA0B4CA02072DE48")
                        .atLocalDate("240621")
                        .atTransDate("240621")
                        .atmTermRef("AHBD")
                        .terminalId("0042")
                        .atTraceNo("017659")
                        .atTxnAmount(amountFormatted)
                        .atProcCode("210000")
                        .merchantLoc("HILAL ABU DHABI AE")
                        .txnAmount(amountFormatted)
                        .txnCurrency("784")
                        .build())
                .build();
    }


    private OBWriteCardWithdrawal1 createCardWithdrawalData(String debtorAccountId,
                                                            BigDecimal amount,
                                                            String fee,
                                                            String termRef) {
        final String atUniqueId = RandomStringUtils.randomNumeric(12);
        final String retrievalReference = RandomStringUtils.randomNumeric(12);
        final String amountFormatted = amountFormat.format(amount).replace(".", "");

        return OBWriteCardWithdrawal1.builder()
                .data(OBWriteCardWithdrawal1Data.builder()
                        .initiation(OBWriteCardWithdrawal1DataInitiation.builder()
                                .instructedAmount(OBWriteCardWithdrawal1DataInitiationInstructedAmount.builder()
                                        .amount(amount.toString())
                                        .currency("AED")
                                        .build())
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .identification(debtorAccountId)
                                        .build())
                                .build())
                        .build())
                .supplementaryData(OBWriteCardWithdrawal1SupplementaryData.builder()
                        .atUniqueId(atUniqueId)
                        .retrievalReference(retrievalReference)
                        .cardNumber("BA0B4CA02072DE48")
                        .atLocalDate("240621")
                        .atTransDate("240621")
                        .atmTermRef(termRef)
                        .terminalId("0042")
                        .atTraceNo("017659")
                        .atmFees(fee)
                        .atTxnAmount(amountFormatted)
                        .atProcCode("210000")
                        .merchantLoc("HILAL ABU DHABI AE")
                        .txnAmount(amountFormatted)
                        .txnCurrency("784")
                        .build())
                .build();
    }

    private OBWriteCardDeposit1 createCardWithdrawalData(String debtorAccountId,
                                                      String creditorAccountId,
                                                      BigDecimal amount) {
        final String atUniqueId = RandomStringUtils.randomNumeric(12);
        final String retrievalReference = RandomStringUtils.randomNumeric(12);
        final String amountFormatted = amountFormat.format(amount).replace(".", "");

        return OBWriteCardDeposit1.builder()
                .data(OBWriteCardDeposit1Data.builder()
                        .initiation(OBWriteCardDeposit1DataInitiation.builder()
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .identification(debtorAccountId)
                                        .build())
                                .creditorAccount(OBWriteCardDeposit1DataInitiationCreditorAccount.builder()
                                        .identification(creditorAccountId)
                                        .build())
                                .build())
                        .build())
                .supplementaryData(OBWriteCardDeposit1SupplementaryData.builder()
                        .atUniqueId(atUniqueId)
                        .retrievalReference(retrievalReference)
                        .cardNumber("BA0B4CA02072DE48")
                        .atLocalDate("240621")
                        .atTransDate("240621")
                        .atmTermRef("AHBD")
                        .terminalId("0042")
                        .atTraceNo("017659")
                        .atTxnAmount(amountFormatted)
                        .atProcCode("210000")
                        .merchantLoc("HILAL ABU DHABI AE")
                        .txnAmount(amountFormatted)
                        .txnCurrency("784")
                        .build())
                .build();
    }

    public OBWriteCardDepositResponse1 createCardDepositWebhook(String creditorAccountId,
                                                                BigDecimal amount) {

        String path = "/webhooks/v1/accounts/{accountId}/cards/deposits";

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .pathParams("accountId", creditorAccountId)
                .contentType(ContentType.JSON)
                .body(createCardDepositData(creditorAccountId, amount))
                .when()
                .post(bankingConfig.getBasePath() + path)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteCardDepositResponse1.class);
    }
}
