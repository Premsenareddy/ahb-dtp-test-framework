package uk.co.deloitte.banking.documents.statements.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;

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
public class CCStatementGenerationTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

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
    public void happy_path_generateCCStatementAccountSpecific() throws Throwable  {
        GIVEN("A customer exists as a bank account user");
        setupTestUser("+555501837507");
        TEST("AHBDB-24732: AC1 - Generate CC Statement Account specific with date filter");

        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "ccstatement","4584614489","2022-01","2022-04",200);
                    String documentId = documentRead1.getData().getDocumentFiles().get(0).getId();
                    byte[] documentBytes = this.documentAdapterApi.getDocumentById(this.alphaTestUser, documentId, 200);
                    assertThat(documentBytes).isNotEmpty();
                });
        DONE();
    }

    @Test
    public void happy_path_generateCCStatementList() throws Throwable  {
        GIVEN("A customer exists as a bank account user");
        setupTestUser("+555501837507");
        TEST("AHBDB-24732: AC1 - Generate CC Statement List");

        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "ccstatement",200);
                    String documentId = documentRead1.getData().getDocumentFiles().get(0).getId();
                    byte[] documentBytes = this.documentAdapterApi.getDocumentById(this.alphaTestUser, documentId, 200);
                    assertThat(documentBytes).isNotEmpty();
                });
        DONE();
    }
}
