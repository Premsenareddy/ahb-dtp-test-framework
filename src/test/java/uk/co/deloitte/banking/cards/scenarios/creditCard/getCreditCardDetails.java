package uk.co.deloitte.banking.cards.scenarios.creditCard;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCreditCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class getCreditCardDetails {
    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    private String errorMsg = "Invalid account/customer number";
    private String errorCardNotFound = "Card Id not found - Please provide a valid Card Id could not be found.";

    private AlphaTestUser alphaTestUser;

    private static final String CARD_MASK = "000000";

    private void setupTestUser(String mobile) {
        envUtils.ignoreTestInEnv(Environments.NFT, Environments.DEV);
        if (alphaTestUser == null || !alphaTestUser.getUserTelephone().equalsIgnoreCase(mobile)) {
            alphaTestUser = new AlphaTestUser(mobile);
            alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin(alphaTestUser);
        }
    }

    @Order(1)
    @Test()
    public void get_valid_credit_card() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() == 1);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).isVirtualCardActivated() == true);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getIsPintSet() == true);
        DONE();
    }

    @Order(2)
    @Test()
    public void get_credit_card_cvv() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        ReadCardCvv1 cardCVvDetails = this.cardsApiFlows.fetchCardsCvvForUserV2(alphaTestUser, cardId, "credit");

        THEN("Verifes the response has required tags for CVV number");
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
        DONE();
    }

    @Order(3)
    @Test()
    public void get_credit_card_params() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        ReadCardParameters1 readCardParameters = this.cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");

        THEN("Verifes the response has required tags for POS and ATM limits");
        Assertions.assertTrue(StringUtils.isNotBlank(readCardParameters.getData().getMaxPOSLimit()));
        Assertions.assertTrue(StringUtils.isNotBlank(readCardParameters.getData().getMaxATMLimit()));
        DONE();
    }

    @Order(4)
    @Test()
    public void negative_get_CC_cvv_by_another_User() {
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));

        if (envUtils.isCit())
            setupTestUser("+555508711346");
        else
            setupTestUser("+555508711389");
        OBErrorResponse1 cardCVvDetails = this.cardsApiFlows.fetchCardsCvvForUserV2Error(alphaTestUser, cardId, "credit", 404);

        THEN("Verifies the error response");
        Assertions.assertTrue(cardCVvDetails.getMessage().equals(errorCardNotFound));
        DONE();
    }

    @Order(5)
    @Test()
    public void get_credit_card_cvv_multiple_cards() {
        if (envUtils.isCit())
            setupTestUser("+555508711346");
        else
            setupTestUser("+555508711389");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View credit card details for user with single card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        ReadCardCvv1 cardCVvDetails = this.cardsApiFlows.fetchCardsCvvForUserV2(alphaTestUser, cardId, "credit");

        THEN("Verifies the response has required tags for CVV number");
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));

        AND("User tries to get cvv for 2nd card");
        cardNumber = cards.getData().getReadCard1DataCard().get(1).getCardNumber();
        cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        cardCVvDetails = this.cardsApiFlows.fetchCardsCvvForUserV2(alphaTestUser, cardId, "credit");

        THEN("Verifies the response has required tags for CVV number");
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cardCVvDetails.getData().getCvv()));
        DONE();
    }

    @Order(6)
    @Test()
    public void get_credit_card_params_multiple_cards() {
        if (envUtils.isCit())
            setupTestUser("+555508711346");
        else
            setupTestUser("+555508711389");
        TEST("AHBDB-15961 - BE View credit card details- show");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        ReadCardParameters1 readCardParameters = this.cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");

        THEN("Verifies the response has required tags for POS and ATM limits");
        Assertions.assertTrue(StringUtils.isNotBlank(readCardParameters.getData().getMaxPOSLimit()));
        Assertions.assertTrue(StringUtils.isNotBlank(readCardParameters.getData().getMaxATMLimit()));

        AND("User tries to get card filters for second card");
        cardNumber = cards.getData().getReadCard1DataCard().get(1).getCardNumber();
        cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        readCardParameters = this.cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");

        THEN("Verifies the response has required tags for POS and ATM limits");
        Assertions.assertTrue(StringUtils.isNotBlank(readCardParameters.getData().getMaxPOSLimit()));
        Assertions.assertTrue(StringUtils.isNotBlank(readCardParameters.getData().getMaxATMLimit()));
        DONE();
    }

    @Order(7)
    @Test()
    public void get_valid_credit_card_multiple_cards() {
        if (envUtils.isCit())
            setupTestUser("+555508711346");
        else
            setupTestUser("+555508711389");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23757 View credit card details for user with Multiple cards");
        GIVEN("I have a valid customer with multiple credit cards linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("A list of the user's cards is returned with CardNo masked with : " + CARD_MASK);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() > 1);
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().get(0).getCardNumber().contains(CARD_MASK));
        DONE();
    }

    @Order(8)
    @Test()
    public void negative_get_CC_params_by_another_user() {
        if (envUtils.isCit())
            setupTestUser("+555508711346");
        else
            setupTestUser("+555508711389");
        TEST("AHBDB-15961 - BE View credit card details- show");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);
        String cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
        final String cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));

        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        OBErrorResponse1 readCardParameters = this.cardsApiFlows.fetchCardFiltersV2Error(alphaTestUser, cardId, "credit", 404);

        THEN("Verifies the error response");
        Assertions.assertTrue(readCardParameters.getMessage().equals(errorCardNotFound));
        DONE();
    }

    @Order(9)
    @Test()
    public void negative_user_get_cc_with_no_cc_linked() {
        if (envUtils.isCit())
            setupTestUser("+555508711398");
        else
            setupTestUser("+555508711392");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23758 View credit card details for user with no card");
        GIVEN("I have a valid customer with no credit card linked to account");

        WHEN("User makes a call to get their cards to ESB / HPS");
        final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);

        THEN("Verify the error message got from DTP");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard() == null);
        DONE();
    }

}
