package uk.co.deloitte.banking.banking.temenos.api;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.deposit.model.OBWriteDeposit1;
import uk.co.deloitte.banking.account.api.deposit.model.OBWriteDeposit1Data;
import uk.co.deloitte.banking.account.api.deposit.model.OBWriteDeposit1DataInitiation;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2DataInitiationCreditorAccount;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.co.deloitte.banking.ahb.dtp.test.banking.account.model.AccountData;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.TransactionResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.RestAssuredConfig.config;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@Singleton
public class TemenosApi extends BaseApi {

    /***************
     *
     *  NEVER USE THIS
     *
     *
     */

    @Inject
    private TemenosConfig temenosConfig;

    public String callTemenos(String xApiKey, int statusCode){
        return given()
                .config(config)
                .log().all()
                .header(HttpConstants.HEADER_X_API_KEY, xApiKey)
                .contentType(JSON)
                .when()
                .get(temenosConfig.getPath() + "/protected/v1/t24/health")
                .then().log().ifError().statusCode(statusCode).assertThat()
                .extract().response().asString();

    }

    @Deprecated
    public TransactionResponse depositFundsToAccountLoop(final AlphaTestUser alphaTestUser, final int amount, final String accountId) {
        //loop due to t24 instance timing out due to scale this should be removed once t24 is no longer PoC
        for (int i = 0; i < 3; i++) {
            try {
                final Response transactionResponse = depositFundsToAccount(alphaTestUser, amount, accountId);
                if (transactionResponse.getStatusCode() == 201) {
                    return transactionResponse.as(TransactionResponse.class);
                }
            } catch (Exception ex) {
                log.error("Transferring test balance  failed ...");
            }
        }
        fail();
        return null;
    }

    @Deprecated
    public Response depositFundsToAccount(final AlphaTestUser alphaTestUser, final int amount,
                                          final String accountId) {

        OBWriteDeposit1 obWriteDeposit1 = OBWriteDeposit1.builder()
                .data(OBWriteDeposit1Data.builder()
                        .initiation(OBWriteDeposit1DataInitiation.builder()
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(BigDecimal.valueOf(amount))
                                        .build())
                                .creditorAccount(OBWriteDomestic2DataInitiationCreditorAccount.builder()
                                        .identification(alphaTestUser.getAccountNumber())
                                        .build())

                                .build())

                        .build())

                .build();

        Response response = given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .when()
                .body(obWriteDeposit1)
                .post(temenosConfig.getPath() + "/internal/v1/internal-deposits");
        int statusCode = response.getStatusCode();
        log.info("Statuscode {}", statusCode);
        log.info("Time taken for temenos depositFundsToCreatedAccountRest request {}", (response.getTimeIn(TimeUnit.MILLISECONDS)));

        return response;
    }

}
