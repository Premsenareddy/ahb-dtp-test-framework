package uk.co.deloitte.banking.customer.crmLeads.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeadsResponse.LeadValueRes;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.crmLeads.api.LeadsApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@Tag("Documents")
@Tag("@BuildCycle4")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CRMLeadsTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private LeadsApiV2 leadsApiV2;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser(final String mobile) throws Throwable {
        alphaTestUser = new AlphaTestUser(mobile);
        this.alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin1(alphaTestUser);
    }

    @Test
    public void happy_path_crmLeadTest() throws Throwable {
        GIVEN("A customer exists as a bank account user");
        setupTestUser("+555501837631");
        String userId = alphaTestUser.getUserId();
        TEST("AHBDB 26053-: AC1 - CRM leads");


        LeadValueRes leadValueRes = leadsApiV2.createCrmLead(userId);
        Assert.assertTrue("Lead id is empty",leadValueRes.getLeadId().isEmpty());

        DONE();
    }


}