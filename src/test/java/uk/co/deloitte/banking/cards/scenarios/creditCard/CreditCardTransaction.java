package uk.co.deloitte.banking.cards.scenarios.creditCard;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCreditCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.Transactions.CardTransaction;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.Transactions.Transaction;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CreditCardTransaction {
    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    private AlphaTestUser alphaTestUser;

    private String errorCardInvalid = "Card Id not found - Please provide a valid Card Id could not be found.";

    private void setupTestUser(String mobile) {
        envUtils.ignoreTestInEnv(Environments.NFT, Environments.DEV);
        if (alphaTestUser == null || !alphaTestUser.getUserTelephone().equalsIgnoreCase(mobile)) {
            alphaTestUser = new AlphaTestUser(mobile);
            alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin(alphaTestUser);
        }
    }

    @Order(1)
    @Test()
    public void get_valid_transaction_cc() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCard(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        DONE();
    }

    @Order(2)
    @Test()
    public void get_valid_transaction_cc_full_filter() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCardWithFilter(alphaTestUser, cardId, "2022-02-01T00:00:00.000",
                1300, 10, "2022-03-01T00:00:00.000", "DEBIT", "BILLED");
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        DONE();
    }

    @Order(3)
    @Test()
    public void get_valid_transaction_cc_V2() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCardV2(alphaTestUser, cardId);
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        DONE();
    }

    @Order(4)
    @Test()
    public void get_valid_transaction_cc_full_filter_V2() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCardWithFilterV2(alphaTestUser, cardId, "2022-02-01T00:00:00.000",
                1300, 10, "2022-03-01T00:00:00.000", "DEBIT", "BILLED");
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        DONE();
    }

    @Order(5)
    @Test()
    public void get_valid_transaction_cc_only_dates() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCardWithFilter(alphaTestUser, cardId, "2022-02-01T00:00:00.000", "2022-03-01T00:00:00.000", "DEBIT");
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        DONE();
    }

    @Order(6)
    @Test()
    public void get_valid_transaction_cc_filter() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCardWithFilter(alphaTestUser, cardId, "2022-02-01T00:00:00.000",
                100, 0, "2022-03-20T00:00:00.000", "DEBIT", "BILLED");
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        DONE();
    }

    @Order(7)
    @Test()
    public void get_valid_transaction_cc_Credit() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCardWithFilter(alphaTestUser, cardId, "2022-02-01T00:00:00.000",
                1300, 10, "2022-03-01T00:00:00.000", "Credit", "");
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        DONE();
    }

    @Order(8)
    @Test()
    public void get_valid_transaction_cc_Unbilled() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Get all transaction for card");
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        CardTransaction response = this.cardsApiFlows.fetchTransactionForCardWithFilterV2(alphaTestUser, cardId, "2022-02-01T00:00:00.000",
                1300, 0, "2022-03-15T00:00:00.000", "Debit", "UNBILLED");
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDate().toString()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionAmount().getAmount()));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getData().getTransaction().get(0).getTransactionDescription()));
        for (Transaction trans : response.getData().getTransaction()) {
            Assertions.assertTrue(trans.billingAmount.amount.equals("null"));
            Assertions.assertTrue(trans.transactionBillingStatus.equalsIgnoreCase("UNBILLED"));
        }
        DONE();
    }
}
