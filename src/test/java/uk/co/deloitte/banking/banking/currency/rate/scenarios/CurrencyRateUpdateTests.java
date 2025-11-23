package uk.co.deloitte.banking.banking.currency.rate.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update.CurrencyRate1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update.CurrencyRateUpdate1Data;
import uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update.CurrencyRateUpdateRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.currency.rate.model.update.CurrencyRateUpdateResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.cbAdaptor.CBAdaptorResponseError;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.banking.currency.rate.api.CurrencyRateApiFlows;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
public class CurrencyRateUpdateTests {

    public static final String CURRENCY = "USD";
    public static final double SELL_BUY_DIFFERENCE = 0.1;
    public static final String BUY_RATE = "3.6519";
    public static final String SPREAD = "0.019";
    public static final String BAD_REQUEST_ERROR = "Transact server error - Bad Request :: currencyRateUpdate";
    public static final int MARKET = 1;

    @Inject
    private CurrencyRateApiFlows utilityPaymentApi;

    @Inject
    private EnvUtils envUtils;

    @ParameterizedTest
    @ValueSource(strings = {"3.6516", "3.6517", "3.6518"})
    public void valid_currency_rate_update(String buyRate) {
        TEST("AHBDB-10031 - Currency Rate Update");

        GIVEN("The currency:USD and market:1 already exists in the Transact");
        WHEN("I update the currency rates");
        THEN("The client submits the currency rates and receives a 200 response");
        CurrencyRateUpdateResponse1 response = this.utilityPaymentApi.updateCurrencyRates(
                getValidRequest(buyRate), CURRENCY);
        Assertions.assertEquals("success", response.getData().getStatus());
        DONE();
    }

    @Test
    public void negative_update_currency_rate_missing_currency_type() {
        TEST("AHBDB-13118 - Put:./webhooks/v1/currencies/USD/rates]Negative when put request to Currency Update with invalid details");

        GIVEN("The currency:XXX and doesn't exists in the Transact");
        WHEN("I update the currency rates");
        THEN("The client submits the currency rates and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.updateCurrencyNegative(
                getValidRequest(BUY_RATE), "XXX", 400);
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", response.code);
        Assertions.assertEquals(BAD_REQUEST_ERROR, response.message);
        DONE();
    }

    @Test
    public void negative_update_currency_rate_missing_market() {
        TEST("AHBDB-13118 - Put:./webhooks/v1/currencies/USD/rates]Negative when put request to Currency Update with invalid details");

        GIVEN("The currency:USD and market:0 doesn't exists in the Transact");
        WHEN("I update the currency rates");
        THEN("The client submits the currency rates and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.updateCurrencyNegative(
                getValidRequest(0, BUY_RATE, BUY_RATE, SPREAD, SPREAD), CURRENCY, 400);
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", response.code);
        Assertions.assertEquals(BAD_REQUEST_ERROR, response.message);
        DONE();
    }

    @Test
    public void negative_update_currency_rate_missing_buy_rate() {
        TEST("AHBDB-13118 - Put:./webhooks/v1/currencies/USD/rates]Negative when put request to Currency Update with invalid details");

        GIVEN("The currency:USD and market:1 already exists in the Transact");
        WHEN("I update the currency buy rates to 0");
        THEN("The client submits the currency rates and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.updateCurrencyNegative(
                getValidRequest(MARKET, "0", BUY_RATE, SPREAD, SPREAD), CURRENCY, 400);
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", response.code);
        Assertions.assertEquals(BAD_REQUEST_ERROR, response.message);
        DONE();
    }

    @Test
    public void negative_update_currency_rate_missing_sell_rate() {
        TEST("AHBDB-13118 - Put:./webhooks/v1/currencies/USD/rates]Negative when put request to Currency Update with invalid details");

        GIVEN("The currency:USD and market:1 already exists in the Transact");
        WHEN("I update the currency sell rates to 0 ");
        THEN("The client submits the currency rates and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.updateCurrencyNegative(
                getValidRequest(MARKET, BUY_RATE, "0", SPREAD, SPREAD), CURRENCY, 400);
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", response.code);
        Assertions.assertEquals(BAD_REQUEST_ERROR, response.message);
        DONE();
    }

    @Test
    public void negative_update_currency_rate_no_update() {
        TEST("AHBDB-13119 - Put:./webhooks/v1/currencies/USD/rates]Negative 400 response" +
                " when put request to Currency Update with valid details that was already sent(No record update)");

        GIVEN("The currency:USD and market:1 already exists in the Transact");
        WHEN("I update the currency rates");
        this.utilityPaymentApi.updateCurrencyRates(getValidRequest(BUY_RATE), CURRENCY);
        WHEN("I update the currency rates with same details");
        THEN("The client submits the currency rates and receives a 400 response");
        CBAdaptorResponseError response = this.utilityPaymentApi.updateCurrencyNegative(
                getValidRequest(BUY_RATE), CURRENCY, 400);
        Assertions.assertEquals("UAE.ERROR.BAD_REQUEST", response.code);
        Assertions.assertEquals(BAD_REQUEST_ERROR, response.message);
        DONE();
    }

    private CurrencyRateUpdateRequest1 getValidRequest(String buyRate) {

        return CurrencyRateUpdateRequest1.builder()
                .data(CurrencyRateUpdate1Data.builder()
                        .currencyRates(List.of(getDummyCurrencyRate1(buyRate)))
                        .build()
                )
                .build();
    }
    private CurrencyRateUpdateRequest1 getValidRequest(int market, String buyRate, String sellRate, String customerSpread, String forexSpread) {

        return CurrencyRateUpdateRequest1.builder()
                .data(CurrencyRateUpdate1Data.builder()
                        .currencyRates(List.of(getDummyCurrencyRate1( market,  buyRate, sellRate, customerSpread, forexSpread )))
                        .build()
                )
                .build();
    }

    private CurrencyRate1 getDummyCurrencyRate1(String buyRate) {
        BigDecimal buyRateNum = new BigDecimal(buyRate);
        CurrencyRate1 rate = CurrencyRate1.builder()
                .buyRate(buyRateNum)
                .sellRate(buyRateNum.add(BigDecimal.valueOf(SELL_BUY_DIFFERENCE)))
                .market(MARKET)
                .customerSpread(new BigDecimal(SPREAD))
                .forexSpread(new BigDecimal(SPREAD))
                .build();
        return rate;
    }

    private CurrencyRate1 getDummyCurrencyRate1(int market, String buyRate, String sellRate, String customerSpread, String forexSpread) {
        BigDecimal buyRateNum = new BigDecimal(buyRate);
        return CurrencyRate1.builder()
                .buyRate(buyRateNum)
                .sellRate(new BigDecimal(sellRate))
                .market(market)
                .customerSpread(new BigDecimal(customerSpread))
                .forexSpread(new BigDecimal(forexSpread))
                .build();
    }
}
