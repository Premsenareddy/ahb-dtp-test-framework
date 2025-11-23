package uk.co.deloitte.banking.banking.exchangerate.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.exchangeRate.response.ExchangeRateResponseDo;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.exchangerate.api.ExchangeRateApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExchangeRateTests {

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
        envUtils.ignoreTestInEnv("Feature not deployed yet on NFT, STG", Environments.NFT, Environments.STG);
        if (this.alphaTestUser == null) {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
    }


    @Test
    @Order(1)
    public void positive_case_currency_exchange_rate() {
        TEST("AHBDB-9306 user can retrieve exchange rate currency");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I fetch currency exchange rate");


        THEN("The client submits the exchange rate  payload and receives a 200 response");
        ExchangeRateResponseDo exchangeRateResponse1 = this.exchangeRateApiFlows.fetchExchangeRate(alphaTestUser, "USD");
        assertNotNull(exchangeRateResponse1);
        assertEquals("USD", exchangeRateResponse1.getData().get(0).getCreditCurrency());


        DONE();
    }

    @Test
    @Order(1)
    public void negative_case_currency_exchange_rate_bad_request() {
        TEST("AHBDB-9306 user can retrive exchange rate currency");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch currency exchange rate without valid currency id");


        THEN("The client submits exchange rate request and receives a 400 response");
        this.exchangeRateApiFlows.fetchExchangeRateError(alphaTestUser, 400, " ");

        DONE();
    }


    @Test
    @Order(1)
    public void negative_case_currency_exchange_rate_not_found() {
        TEST("AHBDB-9306 user can retrive exchange rate currency");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch currency exchange rate using a wrong currency id");


        THEN("The client submits exchange rate request and receives a 404 response");
        this.exchangeRateApiFlows.fetchExchangeRateError(alphaTestUser, 404, "USDINR");

        DONE();
    }


}
