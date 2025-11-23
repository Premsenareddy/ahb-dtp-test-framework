package uk.co.deloitte.banking.banking.currency.rate.api;

import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update.CurrencyRateUpdateRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update.CurrencyRateUpdateResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.cbAdaptor.CBAdaptorResponseError;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
@Singleton
@Slf4j
public class CurrencyRateApiFlows extends BaseApi {

    private static final String PROTECTED_CURRENCY_RATE_UPDATE_API = "/webhooks/v1/currencies/{currency}/rates";

    @Inject
    private final BankingConfig bankingConfig;

    public CurrencyRateUpdateResponse1 updateCurrencyRates(final CurrencyRateUpdateRequest1 request, String currency) {

        return given()
                .config(config)
                .log().all()
                .pathParams("currency", currency)
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(bankingConfig.getBasePath() + PROTECTED_CURRENCY_RATE_UPDATE_API)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(CurrencyRateUpdateResponse1.class);
    }
    public CBAdaptorResponseError updateCurrencyNegative(final CurrencyRateUpdateRequest1 request, String currency, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .pathParams("currency", currency)
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(bankingConfig.getBasePath() + PROTECTED_CURRENCY_RATE_UPDATE_API)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(CBAdaptorResponseError.class);
    }
}
