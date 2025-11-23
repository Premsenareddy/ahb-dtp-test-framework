package uk.co.deloitte.banking.banking.exchangerate.scenarios;

import com.nimbusds.jose.util.StandardCharset;
import io.micronaut.test.annotation.MicronautTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.testcontainers.shaded.org.apache.commons.lang.Validate;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.exchangeRate.response.ExchangeRateResponseDo;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.exchangerate.api.ExchangeRateApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.net.URLEncoder;

import static org.junit.Assert.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExchangeRateValidationsTests {

    @Inject
    private ExchangeRateApiFlows exchangeRateApiFlows;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        envUtils.ignoreTestInEnv(Environments.SIT);
        if (this.alphaTestUser == null) {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
    }

    /* Test to Validate currency id = USD and Assert the request body params */

    @Test
    @Order(1)
    public void positive_case_currency_exchange_rate_USD() {
        TEST("AHBDB-9306 user can retrieve exchange rate currency");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I fetch currency exchange rate");


        THEN("The client submits the exchange rate  payload and receives a 200 response");
        ExchangeRateResponseDo exchangeRateResponse1 = this.exchangeRateApiFlows.fetchExchangeRate(alphaTestUser, "USD");
        assertNotNull(exchangeRateResponse1);
        assertEquals("USD", exchangeRateResponse1.getData().get(0).getCreditCurrency());
        assertNotNull(exchangeRateResponse1.getData().get(0).getCustomerRate().toString());
        assertNotNull(exchangeRateResponse1.getData().get(0).getCustomerSpread().toString());
        assertTrue(exchangeRateResponse1.getData().get(0).getCustomerRate().doubleValue() > 0);
        assertTrue(exchangeRateResponse1.getData().get(0).getCustomerSpread().doubleValue() > 0);
        DONE();
    }



   /* Test to Validate currency id = AED and assert for the param customerRate = 0 */

    @Test
    @Order(1)
    public void positive_case_currency_exchange_rate_AED() {
        TEST("AHBDB-9306 user can retrieve exchange rate currency");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch currency exchange rate");
        THEN("The client submits the exchange rate  payload and receives a 200 response");
        ExchangeRateResponseDo exchangeRateResponse1 = this.exchangeRateApiFlows.fetchExchangeRate(alphaTestUser, "AED");
        assertNotNull(exchangeRateResponse1);
        assertEquals("AED", exchangeRateResponse1.getData().get(0).getCreditCurrency());
        assertEquals("0",exchangeRateResponse1.getData().get(0).getCustomerRate().toString());
        DONE();
    }

    /* Test to Validate currency id = INR and assert the various params of the request body */


    @Test
    @Order(1)
    public void positive_case_currency_exchange_rate_INR() {
        TEST("AHBDB-9306 user can retrieve exchange rate currency");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch currency exchange rate");
        THEN("The client submits the exchange rate  payload and receives a 200 response");
        ExchangeRateResponseDo exchangeRateResponse1 = this.exchangeRateApiFlows.fetchExchangeRate(alphaTestUser, "INR");
        assertNotNull(exchangeRateResponse1);
        assertEquals("INR", exchangeRateResponse1.getData().get(0).getCreditCurrency());
        assertNotNull(exchangeRateResponse1.getData().get(0).getCustomerRate().toString());
        assertNotNull(exchangeRateResponse1.getData().get(0).getCustomerSpread().toString());
        assertTrue(exchangeRateResponse1.getData().get(0).getCustomerSpread().doubleValue() > 0);
        assertTrue(exchangeRateResponse1.getData().get(0).getCustomerRate().doubleValue() > 0);
        DONE();
    }

    /* Bad Request : Test to Validate currency id = " " and validate Error Param "Message" in hashmap format */

    @Test
    @Order(1)
    public void negative_case_currency_exchange_rate_bad_request() {
        TEST("AHBDB-9306 user can retrive exchange rate currency");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch currency exchange rate without valid currency id");
        THEN("The client submits exchange rate request and receives a 400 response");
        this.exchangeRateApiFlows.fetchExchangeRateError(alphaTestUser, 400, " ");
        var errorResponse = this.exchangeRateApiFlows.fetchExchangeRateRequestError(alphaTestUser, " ");
        assertEquals("fetchExchangeRate.currency: must not be blank", errorResponse.get("Message"));
        DONE();
    }

    /* Invalid Request : 404 Not Found : Test to Validate invalid code for currency id = "gjhghj" and verify Error Param "Message" in hashmap format */

    @Test
    @Order(1)
    public void negative_case_currency_exchange_rate_not_found() {
        TEST("AHBDB-9306 user can retrive exchange rate currency");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch currency exchange rate using a wrong currency id");
        THEN("The client submits exchange rate request and receives a 404 response");
        this.exchangeRateApiFlows.fetchExchangeRateError(alphaTestUser, 404, "gjhghj");
        var errorResponse = this.exchangeRateApiFlows.fetchExchangeRateRequestError(alphaTestUser, "gjhghj");
        assertEquals("Transact server error - Not Found :: exchangeRate", errorResponse.get("Message"));
        DONE();
    }


}
