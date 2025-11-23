package uk.co.deloitte.banking.banking.temenos.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.banking.temenos.api.TemenosApi;
import javax.inject.Inject;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TemenosTests {

    @Inject
    private TemenosApi temenosApi;
    @Inject
    private BankingConfig bankingConfig;

    @Test
    public void positive_case_temenos_health_check() {
        TEST("AHBDB-14362 : Healthcheck Temenos Status endpoint by getting the Status as OK");
        GIVEN("I have a valid api test method to check health");
        WHEN("I make a call to temenos health check api");
        THEN("Healthcheck Temenos end point should return 200 response");
        String response = temenosApi.callTemenos(bankingConfig.getApiKey(), 200);
        Assert.assertThat(response, CoreMatchers.containsString("OK"));
        DONE();
    }

    @Test
    public void unauthorized_case_temenos_health_check() {
        TEST("AHBDB-14362 : Healthcheck Temenos Status with unauthorized case as 401");
        GIVEN("I have a valid api test method to check health");
        WHEN("I make a call to temenos health check api");
        THEN("Healthcheck Temenos end point should return 401 response");
        String response = temenosApi.callTemenos("invalidKey", 401);
        DONE();
    }
    // As discussed with Andrew McKee(Dev Team) Down Status can not be automated

}

