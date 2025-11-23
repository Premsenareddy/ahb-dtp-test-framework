package uk.co.deloitte.banking.cards.scenarios.internalEndpoints;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1Data;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCardAccount1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.TransactionType;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.card.model.virtualcards.CardProduct.ADULT_DIGITAL_DC_PLATINUM;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@Tag("Cards")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CardsAuthorisationCheckTests {

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;

    private AlphaTestUser setupTestUser() {
        AlphaTestUser alphaTestUserToSetup = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUserToSetup);

        CreateCard1 createCard1 = CreateCard1.builder()
                .data(CreateCard1Data.builder()
                        .cardProduct(ADULT_DIGITAL_DC_PLATINUM)
                        .embossedName(alphaTestUserToSetup.getName())
                        .accounts(List.of(CreateCardAccount1.builder()
                                .accountCurrency("AED")
                                .accountName("CURRENT")
                                .accountNumber(alphaTestUserToSetup.getAccountNumber())
                                .accountType(AccountType.CURRENT.getDtpValue())
                                .openDate(LocalDateTime.now())
                                .seqNumber("1")
                                .build()))
                        .build())
                .build();

        cardsApiFlows.createVirtualDebitCard(alphaTestUserToSetup, createCard1);

        return alphaTestUserToSetup;
    }

    private void setupTestUsers() {
        if (alphaTestUser == null || alphaTestUser2 == null) {
            alphaTestUser = setupTestUser();
            alphaTestUser2 = setupTestUser();
        }
    }

    @Test
    public void negative_test_another_customer_attempts_to_query_parameters() {
        TEST("AHBDB-13240: Cross-Account Manipulation");
        setupTestUsers();

        GIVEN("A customer exists with a valid JWT token");
        WHEN("They attempt to query for another customer's information using that token");
        String cardId = cardsApiFlows.fetchCardsForUser(alphaTestUser)
                .getData().getReadCard1DataCard().get(0).getCardNumber().replace("000000", "");

        OBErrorResponse1 error =
                cardsApiFlows.fetchCardFiltersError(alphaTestUser2, cardId, 404);
        THEN("The platform should not allow them to do so");
        assertNotNull(error.getCode());
        DONE();
    }

    @Test
    public void negative_test_another_customer_attempts_to_query_transaction_types_purchases() {
        TEST("AHBDB-13240: Cross-Account Manipulation");
        setupTestUsers();

        GIVEN("A customer exists with a valid JWT token");
        WHEN("They attempt to query for another customer's information using that token");
        String cardId = cardsApiFlows.fetchCardsForUser(alphaTestUser)
                .getData().getReadCard1DataCard().get(0).getCardNumber().replace("000000", "");

        OBErrorResponse1 error = cardsApiFlows.fetchCardLimitsForTransactionTypeError(alphaTestUser2,
                TransactionType.PURCHASE.getLabel(), cardId, 404);

        THEN("The platform should not allow them to do so");
        assertNotNull(error.getCode());
        DONE();
    }

    @Test
    public void negative_test_another_customer_attempts_to_query_transaction_types_withdrawals() {
        TEST("AHBDB-13240: Cross-Account Manipulation");
        setupTestUsers();

        GIVEN("A customer exists with a valid JWT token");
        WHEN("They attempt to query for another customer's information using that token");
        String cardId = cardsApiFlows.fetchCardsForUser(alphaTestUser)
                .getData().getReadCard1DataCard().get(0).getCardNumber().replace("000000", "");

        OBErrorResponse1 error = cardsApiFlows.fetchCardLimitsForTransactionTypeError(alphaTestUser2,
                TransactionType.WITHDRAWAL.getLabel(), cardId, 404);

        THEN("The platform should not allow them to do so");
        assertNotNull(error.getCode());
        DONE();
    }

    @Test
    public void negative_test_another_customer_attempts_to_query_cvv() {
        TEST("AHBDB-13240: Cross-Account Manipulation");
        setupTestUsers();

        GIVEN("A customer exists with a valid JWT token");
        WHEN("They attempt to query for another customer's information using that token");
        String cardId = cardsApiFlows.fetchCardsForUser(alphaTestUser)
                .getData().getReadCard1DataCard().get(0).getCardNumber().replace("000000", "");

        OBErrorResponse1 error = cardsApiFlows.fetchCardsCvvForUserError(alphaTestUser2, cardId, 404);

        THEN("The platform should not allow them to do so");
        assertNotNull(error.getCode());
        DONE();
    }
}
