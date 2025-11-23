package uk.co.deloitte.banking.documents.pdfs;

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
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
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
public class FatcaPDFGenerationTests {

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

    @BeforeEach
    public void setupTestUser() {
        //envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Test
    public void happy_path_generate_W8_form() {
        TEST("AHBDB-1114: AC1 - Generate W8 form");
        TEST("AHBDB-7798: AC1 Positive Test - Happy Path Scenario - Generate W8 Form");
        TEST("AHBDB-8001: AC2 Positive Test - Happy Path Scenario - Get list of documents (DocumentType, userID - 200 Success ");
        TEST("AHBDB-8002: AC3 Positive Test - Happy Path Scenario - Get specific document (DocumentID) - 200 Success ");

        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        AND("The customer's 'Form' field is set to W8");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the " +
                "mandatory consolidated list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getW8DocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the W8 form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");

        WHEN("The client attempts to retrieve a specific document with a valid document ID");
        THEN("The platform will return a 200 response with the specific document");

        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "W8", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                    String documentId = documentRead1.getData().getDocumentFiles().get(0).getId();
                    byte[] documentBytes = this.documentAdapterApi.getDocumentById(this.alphaTestUser, documentId, 200);
                    assertThat(documentBytes).isNotEmpty();
                });

        DONE();
    }

    @Test
    public void happy_path_generate_W9_form() {
        TEST("AHBDB-1114: AC2 - Generate W9 form");
        TEST("AHBDB-7799: AC2 Positive Test - Happy Path Scenario - Generate W9 Form");
        TEST("AHBDB-8001: AC2 Positive Test - Happy Path Scenario - Get list of documents (DocumentType, userID - 200 Success ");
        TEST("AHBDB-8002: AC3 Positive Test - Happy Path Scenario - Get specific document (DocumentID) - 200 Success ");

        GIVEN("A customer exists as a bank account user");
        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        AND("The customer's 'Form' field is set to W8");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the " +
                "mandatory consolidated list of fields (using Acrofield names) from CRM");
        DocumentGenerationRequestEvent documentRequest = this.developmentSimulatorService.getW9DocumentRequest(this.alphaTestUser);

        THEN("We will generate the form against the W8 form existing template");
        this.developmentSimulatorService.generateDocument(documentRequest, 200);

        AND("We will store it in Sharepoint");

        WHEN("The client attempts to retrieve a specific document with a valid document ID");
        THEN("The platform will return a 200 response with the specific document");

        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "W9", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                    String documentId = documentRead1.getData().getDocumentFiles().get(0).getId();
                    byte[] documentBytes = this.documentAdapterApi.getDocumentById(this.alphaTestUser, documentId, 200);
                    assertThat(documentBytes).isNotEmpty();
                });

        DONE();
    }
}
