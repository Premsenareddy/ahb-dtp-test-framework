package uk.co.deloitte.banking.documents.statements.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentRead1;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.StatementGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.TransactionDetails;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.documents.api.DocumentAdapterApi;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomBNumericTwoDB;

@Tag("Documents")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatementGenerationTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private DocumentAdapterApi documentAdapterApi;

    @Inject
    private TemenosConfig temenosConfig;

    final private static String LEGAL_BLURB = "This statement shall be considered correct unless it is contested";

    final private static String TRANS_NARRATIVE = "narrativeOne";

    private AlphaTestUser alphaTestUser;

    @BeforeEach
    public void setupTestUser() {
        //envUtils.ignoreTestInEnv(Environments.NFT);
        if (alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            alphaTestUser.setAccountNumber(RandomDataGenerator.generateRandomNumeric(12));
        }
    }

    private TransactionDetails transactionDetailsMoneyOut(String bookingDate, String valueDate) {
        return TransactionDetails.builder()
                .bookingDate(bookingDate)
                .narrativeOne(TRANS_NARRATIVE)
                .narrativeTwo("narrativeTwo")
                .narrativeThree("narrativeThree")
                .valueDate(valueDate)
                .moneyOut(generateRandomBNumericTwoDB(5))
                .moneyIn(null)
                .balance(generateRandomBNumericTwoDB(7))
                .build();
    }

    private TransactionDetails transactionDetailsMoneyIn(String bookingDate, String valueDate) {
        return TransactionDetails.builder()
                .bookingDate(bookingDate)
                .narrativeOne(TRANS_NARRATIVE)
                .narrativeTwo("narrativeTwo")
                .narrativeThree("narrativeThree")
                .valueDate(valueDate)
                .moneyOut(null)
                .moneyIn(generateRandomBNumericTwoDB(5))
                .balance(generateRandomBNumericTwoDB(7))
                .build();
    }

    private StatementGenerationRequestEvent statementGenerationRequestEvent(String statementStartDate, String statementEndDate, String customerId, String iban, List<TransactionDetails> transactionDetailsList) {
        return StatementGenerationRequestEvent.builder()
                .customerId(customerId)
                .customerName(alphaTestUser.getName())
                .statementDate(bookingDateValueDate(0))
                .statementStartDate(statementStartDate)
                .statementEndDate(statementEndDate)
                .accountNumber(alphaTestUser.getAccountNumber())
                .productDescription("productDescription")
                .postCode("12345")
                .townCountry("Abu Dhabi")
                .country("UAE")
                .ibanNumber(iban)
                .currency("AED")
                .openingBalance(generateRandomBNumericTwoDB(7))
                .totalMoneyIn(generateRandomBNumericTwoDB(6))
                .totalMoneyOut(generateRandomBNumericTwoDB(5))
                .transactionDetails(transactionDetailsList)
                .build();
    }

    private String bookingDateValueDate(int days) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        return dtf.format(LocalDate.now().minusDays(days));
    }

    private String statementDates(int months) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        return dtf.format(LocalDate.now().minusMonths(months));
    }

    private String searchStatementDates(int months) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
        return dtf.format(LocalDate.now().minusMonths(months));
    }

    private static String extractPdfText(byte[] pdfData) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            return new PDFTextStripper().getText(pdfDocument);
        }
    }

    @Test
    public void user_with_statement_with_multiple_transactions_can_get_all_transactions_on_statement() throws IOException {
        TEST("AHBDB-6182 who has a statement with multiple transactions can retrieve and count the transactions on the statement");
        TEST("AHBDB-6181 who has multiple statements can retrieve all of them within the correct date range");
        TEST("AHBDB-11445 Remove address validations during statement creation");

        GIVEN("I have a valid user with a bank account");

        String statementStartDate1 = statementDates(6);
        String statementEndDate1 = statementDates(5);

        String iban =  temenosConfig.getCreditorIban();
        AND("The user has an valid IBAN : " + iban);

        List<TransactionDetails> transactionDetailsList = new ArrayList<>();
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(30)), (bookingDateValueDate(29))));

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        AND("customerId : " + customerId);

        WHEN(String.format("A statement is generated with null address fields startDate  %s and endDate  %s ", statementStartDate1, statementEndDate1));
        StatementGenerationRequestEvent statementGenerationRequest = statementGenerationRequestEvent(statementStartDate1, statementEndDate1, customerId, iban, transactionDetailsList);
        statementGenerationRequest.setCountry(null);
        statementGenerationRequest.setPostCode(null);
        statementGenerationRequest.setTownCountry(null);
        this.developmentSimulatorService.generateStatement(statementGenerationRequest, 200);

        THEN("The expected statement can be retrieved from the document service with 200");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "statements", alphaTestUser.getAccountNumber(), searchStatementDates(6), searchStatementDates(5), 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });

        String statement1Id = this.documentAdapterApi.getDocumentList(alphaTestUser, "statements", alphaTestUser.getAccountNumber(), searchStatementDates(6), searchStatementDates(5), 200).getData().getDocumentFiles().get(0).getId();
        WHEN("The statement ID is used to get the individual statement : " + statement1Id );

        int expectedNumberOfTransactions = 4;
        THEN("The PDF statement is returned with the correct information with a 200 with expected number of transactions : " + expectedNumberOfTransactions);

        byte[] pdfFile = this.documentAdapterApi.getDocumentById(alphaTestUser, statement1Id, 200);
        String statement1String = extractPdfText(pdfFile);
        String[] transactionsCount = statement1String.split(TRANS_NARRATIVE);
        int numberOfTransactions = transactionsCount.length - 1;

        assertThat(statement1String).contains(alphaTestUser.getAccountNumber());
        assertThat(statement1String).contains(LEGAL_BLURB);
        assertEquals(expectedNumberOfTransactions, numberOfTransactions, "Number of transactions did not equal expected");

        DONE();
    }

    @Test
    public void user_with_multiple_statements_with_multiple_transactions_can_get_all_transactions_on_statement() throws IOException {
        TEST("AHBDB-6182 who has a statement with multiple transactions can retrieve and count the transactions on the statement");
        TEST("AHBDB-6181 who has multiple statements can retrieve all of them within the correct date range");

        GIVEN("I have a valid user with a bank account");

        String statementStartDate1 = statementDates(4);
        String statementEndDate1 = statementDates(3);

        String statementStartDate2 = statementDates(3);
        String statementEndDate2 = statementDates(2);

        String statementStartDate3 = statementDates(2);
        String statementEndDate3 = statementDates(1);

        String iban =  temenosConfig.getCreditorIban();
        AND("The user has an valid IBAN : " + iban);

        List<TransactionDetails> transactionDetailsList = new ArrayList<>();
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(30)), (bookingDateValueDate(29))));

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        AND("customerId : " + customerId);

        WHEN(String.format("A statement is generated with startDate  %s and endDate  %s ", statementStartDate1, statementEndDate1));
        StatementGenerationRequestEvent statementGenerationRequest = statementGenerationRequestEvent(statementStartDate1, statementEndDate1, customerId, iban, transactionDetailsList);
        this.developmentSimulatorService.generateStatement(statementGenerationRequest, 200);

        WHEN(String.format("A statement is generated with startDate  %s and endDate  %s ", statementStartDate2, statementEndDate2));
        StatementGenerationRequestEvent statementGenerationRequest2 = statementGenerationRequestEvent(statementStartDate2, statementEndDate2, customerId, iban, transactionDetailsList);
        this.developmentSimulatorService.generateStatement(statementGenerationRequest2, 200);

        WHEN(String.format("A statement is generated with startDate  %s and endDate  %s ", statementStartDate2, statementEndDate2));
        StatementGenerationRequestEvent statementGenerationRequest3 = statementGenerationRequestEvent(statementStartDate3, statementEndDate3, customerId, iban, transactionDetailsList);
        this.developmentSimulatorService.generateStatement(statementGenerationRequest3, 200);

        THEN("The 3 expected statements can be retrieved from the document service with 200");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "statements", alphaTestUser.getAccountNumber(), searchStatementDates(5), searchStatementDates(1), 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(3);
                });

        String statement1Id = this.documentAdapterApi.getDocumentList(alphaTestUser, "statements", alphaTestUser.getAccountNumber(), searchStatementDates(5), searchStatementDates(1), 200).getData().getDocumentFiles().get(2).getId();
        WHEN("The statement ID is used to get the individual statement : " + statement1Id);

        int expectedNumberOfTransactions = 2;
        THEN("The PDF statement is returned with the correct information with a 200 with expected number of transactions : " + expectedNumberOfTransactions);

        byte[] pdfFile = this.documentAdapterApi.getDocumentById(alphaTestUser, statement1Id, 200);
        String statement1String = extractPdfText(pdfFile);
        String[] transactionsCount = statement1String.split(TRANS_NARRATIVE);
        int numberOfTransactions = transactionsCount.length - 1;

        assertThat(statement1String).contains(alphaTestUser.getAccountNumber());
        assertThat(statement1String).contains(LEGAL_BLURB);
        assertEquals(expectedNumberOfTransactions, numberOfTransactions, "Number of transactions did not equal expected");

        DONE();
    }

    @Test
    public void user_with_statement_with_multiple_transactions_that_goes_over_two_pages_on_statement() throws IOException {
        TEST("AHBDB-6182 who has a statement with multiple transactions can retrieve and count the transactions on the statement");
        TEST("AHBDB-6181 who has multiple statements can retrieve all of them within the correct date range");

        GIVEN("I have a valid user with a bank account");

        String statementStartDate1 = statementDates(8);
        String statementEndDate1 = statementDates(7);

        String iban =  temenosConfig.getCreditorIban();
        AND("The user has an valid IBAN : " + iban);

        List<TransactionDetails> transactionDetailsList = new ArrayList<>();
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyOut((bookingDateValueDate(30)), (bookingDateValueDate(29))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(70)), (bookingDateValueDate(70))));
        transactionDetailsList.add(transactionDetailsMoneyIn((bookingDateValueDate(30)), (bookingDateValueDate(29))));

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        AND("customerId : " + customerId);

        WHEN(String.format("A statement is generated with startDate  %s and endDate  %s ", statementStartDate1, statementEndDate1));
        StatementGenerationRequestEvent statementGenerationRequest = statementGenerationRequestEvent(statementStartDate1, statementEndDate1, customerId, iban, transactionDetailsList);
        this.developmentSimulatorService.generateStatement(statementGenerationRequest, 200);

        THEN("The expected statement can be retrieved from the document service with 200");
        //Awaitility has been used to remove flakiness from the sim service
        await().atMost(30, SECONDS).with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                {
                    DocumentRead1 documentRead1 = this.documentAdapterApi.getDocumentList(alphaTestUser, "statements", alphaTestUser.getAccountNumber(), searchStatementDates(8), searchStatementDates(7), 200);
                    assertThat(documentRead1.getData().getDocumentFiles()).hasSize(1);
                });

        String statement1Id = this.documentAdapterApi.getDocumentList(alphaTestUser, "statements", alphaTestUser.getAccountNumber(), searchStatementDates(8), searchStatementDates(7), 200).getData().getDocumentFiles().get(0).getId();
        WHEN("The statement ID is used to get the individual statement : " + statement1Id );

        int expectedNumberOfTransactions = 24;
        THEN("The PDF statement is returned with the correct information with a 200 with expected number of transactions : " + expectedNumberOfTransactions);
        byte[] pdfFile = this.documentAdapterApi.getDocumentById(alphaTestUser, statement1Id, 200);

        String statement1String = extractPdfText(pdfFile);
        String[] transactionsCount = statement1String.split(TRANS_NARRATIVE);
        int numberOfTransactions = transactionsCount.length - 1;

        assertThat(statement1String).contains(alphaTestUser.getAccountNumber());
        assertThat(statement1String).contains(LEGAL_BLURB);
        assertEquals(expectedNumberOfTransactions, numberOfTransactions, "Number of transactions did not equal expected");

        DONE();
    }
}
