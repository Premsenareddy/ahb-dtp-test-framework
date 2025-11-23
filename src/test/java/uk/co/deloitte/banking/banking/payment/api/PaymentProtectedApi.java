package uk.co.deloitte.banking.banking.payment.api;

import io.restassured.http.ContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;

@Singleton
@Slf4j
public class PaymentProtectedApi extends BaseApi {
    private static final String PROTECTED_DOMESTIC_PAYMENTS = "/protected/v1/domestic-payments";
    private static final String WEBHOOK_DOMESTIC_PAYMENTS = "/webhooks/v1/accounts/domestic-payments";

    @Inject
    BankingConfig bankingConfig;

    public OBWriteDomesticResponse5 createLegacyDtpPaymentWebhooks(String debtorAccountId,
                                                           String creditorAccountId,
                                                           BigDecimal amount) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(createLegacyDtpPaymentWebhookData(debtorAccountId, creditorAccountId, amount))
                .when()
                .post(bankingConfig.getBasePath() + WEBHOOK_DOMESTIC_PAYMENTS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteDomesticResponse5.class);
    }

    public OBWriteDomesticResponse5 createLegacyDtpPayment(String debtorAccountId,
                                                           String creditorAccountId,
                                                           BigDecimal amount) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(createLegacyDtpPaymentData(debtorAccountId, creditorAccountId, amount, null))
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_DOMESTIC_PAYMENTS)
                .then()
                .log().all()
                .statusCode(201).assertThat()
                .extract().body().as(OBWriteDomesticResponse5.class);
    }

    private OBWriteDomestic2 createLegacyDtpPaymentWebhookData(String debtorAccountId,
                                                        String creditorAccountId,
                                                        BigDecimal amount) {
        LegacyToDTPSupplementaryData supplementaryData = null;
        if (debtorAccountId.startsWith("0")) {
            supplementaryData = getSupplementaryData(debtorAccountId);
        }

        return createLegacyDtpPaymentData(debtorAccountId, creditorAccountId, amount, supplementaryData);
    }

    private LegacyToDTPSupplementaryData getSupplementaryData(String debtorAccount) {
        return new LegacyToDTPSupplementaryData(debtorAccount);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class LegacyToDTPSupplementaryData {
        private String debitAccountId;
    }

    private OBWriteDomestic2 createLegacyDtpPaymentData(String debtorAccountId,
                                                        String creditorAccountId,
                                                        BigDecimal amount,
                                                        Object supplementaryData) {
        final String instructionIdentification = RandomStringUtils.randomAlphanumeric(11);
        final String endToEndIdentification = RandomStringUtils.randomAlphanumeric(11);

        return OBWriteDomestic2.builder()
                .data(OBWriteDomestic2Data.builder()
                        .consentId("TEST_CONSENT_ID")
                        .initiation(OBWriteDomestic2DataInitiation.builder()
                                .instructionIdentification(instructionIdentification)
                                .endToEndIdentification(endToEndIdentification)
                                .localInstrument("LegacyDtp")
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .identification(debtorAccountId)
                                        .name("test")
                                        .build())
                                .creditorAccount(OBWriteDomestic2DataInitiationCreditorAccount.builder()
                                        .identification(creditorAccountId)
                                        .name("test")
                                        .build())
                                .remittanceInformation(OBWriteDomestic2DataInitiationRemittanceInformation.builder()
                                        .reference("TEST_REFERENCE")
                                        .unstructured("CHC")
                                        .build())
                                .supplementaryData(supplementaryData)
                                .build())
                        .build())
                .build();
    }
}
