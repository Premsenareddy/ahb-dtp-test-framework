package uk.co.deloitte.banking.cards.scenarios.creditCard;

import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCreditCard1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;

import java.util.HashMap;
import java.util.Map;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CreditCardSummaryTest extends AdultOnBoardingBase {

    private static AlphaTestUser alphaTestUser;

    @BeforeEach
    void ignore() {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT);
    }

    private void setupTestUser(final String mobile) throws Throwable {
        alphaTestUser = new AlphaTestUser(mobile);
        this.alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin1(alphaTestUser);
    }

    @Order(1)
    @Test()
    public void get_valid_legacy_credit_card_details_V4() throws Throwable {
        setupTestUser("+555501737197");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View legacy credit card details for user");
        GIVEN("I have a valid customer with credit cards in Digital and Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "credit");
        queryParams.put("legacyCif", "2851986");

        final ReadCreditCard1 cards = this.cardsApiFlows
                .fetchCreditCardsForUserV4(alphaTestUser, queryParams, ReadCreditCard1.class, HttpStatus.OK);

        THEN("A list of the user's cards is returned");
        Assertions.assertTrue(cards.getData().getReadCard1DataCard().size() >= 1);
        DONE();
    }

    @Order(2)
    @Test()
    public void get_valid_digital_credit_card_details_V4_2() {
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View Digital credit card details for user");
        GIVEN("I have a valid customer with credit cards in Digital and Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "credit");
        queryParams.put("legacyCif", "33333");

        WHEN("I fetch credit cards details with invalid cif number");
        final OBErrorResponse1 error = this.cardsApiFlows
                .fetchCreditCardsForUserV4(alphaTestUser, queryParams, OBErrorResponse1.class, HttpStatus.OK);

        THEN("A invalid customer error is thrown");
        Assertions.assertTrue(error.getMessage().contains("Invalid account/customer number"));
        DONE();
    }

    @Order(3)
    @Test()
    public void get_valid_digital_credit_card_details_V4_3() {
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View Digital credit card details for user");
        GIVEN("I have a valid customer with credit cards in Digital and Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "credit");

        final ReadCreditCard1 cards = this.cardsApiFlows
                .fetchCreditCardsForUserV4(alphaTestUser, queryParams, ReadCreditCard1.class, HttpStatus.OK);

        DONE();
    }

    @Order(4)
    @Test()
    public void get_valid_digital_credit_card_details_V4_Bad_request() {
        TEST("AHBDB-15961 - BE View credit card details- show");
        GIVEN("I have a valid customer with credit cards in Digital and Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "invalid");
        queryParams.put("legacyCif", "false");

        WHEN("I fetch credit cards details with invalid type");
        final OBErrorResponse1 error = this.cardsApiFlows
                .fetchCreditCardsForUserV4(alphaTestUser, queryParams, OBErrorResponse1.class, HttpStatus.BAD_REQUEST);

        THEN("A invalid card type error is thrown");
        Assertions.assertTrue(error.getMessage().contains("invalid card type"));
        DONE();
    }
}
