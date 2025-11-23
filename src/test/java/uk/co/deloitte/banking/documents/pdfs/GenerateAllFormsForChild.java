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
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;

import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@Slf4j
@Tag("Documents")
@Tag("BuildCycle5.1")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenerateAllFormsForChild {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    @BeforeEach
    public void setupTestUsers() {
        //envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser == null ) {
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            String childId = this.alphaTestUserFactory.createChildInForgerock(alphaTestUser, "validtestpassword");

            OBWriteDependant1 obWriteDependant1 = this.alphaTestUserFactory.generateDependantBody(childId,
                    15, "childForFormsTest", OBGender.MALE, OBRelationshipRole.FATHER);

            String connectionId = this.alphaTestUserFactory.createChildInCRM(alphaTestUser, obWriteDependant1);
            alphaTestUserChild = this.alphaTestUserFactory.createChildCustomer(alphaTestUser, new AlphaTestUser(), connectionId, childId);
        }
    }

    @Test
    public void happy_path_generate_fatca_w8_form() {
        TEST("AHBDB-7504: Generate all forms for child, excluding guarantor");
        TEST("AHBDB-11045: Generate W8 Form");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getW8DocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "W8", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });

        DONE();
    }

    @Test
    public void happy_path_generate_fatca_w9_form() {
        TEST("AHBDB-7504: Generate all forms for child, excluding guarantor");
        TEST("AHBDB-11046: Generate W9 Form");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getW9DocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W9 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "W9", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    public void happy_path_generate_crs_form() {
        TEST("AHBDB-7504: Generate all forms for child, excluding guarantor");
        TEST("AHBDB-11047: Generate CRS Form");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getCRSDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "CRS", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    public void happy_path_generate_account_opening_form() {
        TEST("AHBDB-7504: Generate all forms for child, excluding guarantor");
        TEST("AHBDB-11048: Generate Account Opening Form");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getAccountOpeningDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "AC_OPEN", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    public void happy_path_generate_kyc_form() {
        TEST("AHBDB-7504: Generate all forms for child, excluding guarantor");
        TEST("AHBDB-11049: Generate KYC Form");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getKYCDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "KYC", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }

    @Test
    public void happy_path_generate_iban_form() {
        TEST("AHBDB-7504: Generate all forms for child, excluding guarantor");
        TEST("AHBDB-11050: Generate IBAN Form");

        GIVEN("A customers exists as a banking user");
        WHEN("The client attempts to generate a FATCA declaration form for the customer with all of the mandatory fields from CRM");

        DocumentGenerationRequestEvent documentGenerationRequestEvent =
                this.developmentSimulatorService.getIBANDocumentRequest(alphaTestUserChild);

        THEN("We will generate the form against the W8 form existing template");
        AND("We will store it in Sharepoint");
        this.developmentSimulatorService.generateDocument(documentGenerationRequestEvent, 200);
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUserChild, "IBAN", 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });
        DONE();
    }
}
