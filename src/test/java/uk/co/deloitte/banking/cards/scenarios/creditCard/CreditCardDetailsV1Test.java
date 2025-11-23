package uk.co.deloitte.banking.cards.scenarios.creditCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.cbAdaptor.CBAdaptorResponseDataError;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.CardDetailsErr;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCardDetails;
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
public class CreditCardDetailsV1Test extends AdultOnBoardingBase {
    private static AlphaTestUser alphaTestUser;

    private void setupTestUser(final String mobile) throws Throwable {
        alphaTestUser = new AlphaTestUser(mobile);
        this.alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin1(alphaTestUser);
    }

    @Order(1)
    @Test()
    public void get_valid_legacy_credit_card_details_V1() throws Throwable {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT);
        setupTestUser("+555501737197");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View legacy credit card details for user");
        GIVEN("I have a valid customer with credit cards in Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "credit");
        queryParams.put("legacyCif", "2851986");

        final ReadCreditCard1 cardsSummary = this.cardsApiFlows
                .fetchCreditCardsForUserV4(alphaTestUser, queryParams, ReadCreditCard1.class, HttpStatus.OK);

        queryParams.remove("type");
        queryParams.put("cardNo", cardsSummary.getData()
                .getReadCard1DataCard().get(0)
                .getCardNumber());

        final ReadCardDetails cardsDetails = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, ReadCardDetails.class, HttpStatus.OK);
        THEN("A list of the user's cards is returned");
        Assertions.assertEquals(cardsSummary.getData()
                .getReadCard1DataCard().get(0)
                .getCardNumber(), cardsDetails.getData().getCardDetails().getCardNumber());
        DONE();
    }

    @Order(2)
    @Test()
    public void validate_cardNumber_check() {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT);
        GIVEN("I have a valid customer with credit cards in Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("legacyCif", "2851986");
        WHEN("I call cards details servie without card number");

        final CBAdaptorResponseDataError cardsDetails = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, CBAdaptorResponseDataError.class, HttpStatus.BAD_REQUEST);
        THEN("service return error as card number required");
        Assertions.assertEquals(cardsDetails.getMessage(), "Required QueryValue [cardNo] not specified");
        DONE();
    }

    @Order(3)
    @Test()
    public void validateCreditCardNoRecordsFound() {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT);
        GIVEN("I have a valid customer with credit cards in Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("legacyCif", "2851986");
        queryParams.put("cardNo", "576575757");

        WHEN("I call cards details service with invalid credit card number");

        final CBAdaptorResponseDataError cardsDetails = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, CBAdaptorResponseDataError.class, HttpStatus.NOT_FOUND);
        THEN("service return error as card number not found");
        Assertions.assertEquals(cardsDetails.getMessage(), "Card not found");
        DONE();
    }

    @Order(3)
    @Test()
    public void validateErrorForInvalidCIF() {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT);
        GIVEN("I have a valid customer with credit cards in Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("legacyCif", "2323232");
        queryParams.put("cardNo", "576575757");

        WHEN("I call cards details service with invalid CIF");

        final CBAdaptorResponseDataError cardsDetails = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, CBAdaptorResponseDataError.class, HttpStatus.NOT_FOUND);
        THEN("service return error as card number not found");
        Assertions.assertEquals(cardsDetails.getMessage(), "Card not found");
        DONE();
    }

    @Order(4)
    @Disabled
    @Test()
    public void get_valid_digital_credit_card_details_V1() throws Throwable {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT);
        setupTestUser("+555501737197");
        TEST("AHBDB-15961 - BE View credit card details- show");
        TEST("AHBDB-23368 View legacy credit card details for user");
        GIVEN("I have a valid customer with credit cards in Legacy accounts");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("type", "credit");

        final ReadCreditCard1 cardsSummary = this.cardsApiFlows
                .fetchCreditCardsForUserV4(alphaTestUser, queryParams, ReadCreditCard1.class, HttpStatus.OK);

        queryParams.remove("type");
        queryParams.put("cardNo", cardsSummary.getData()
                .getReadCard1DataCard().get(0)
                .getCardNumber());

        final ReadCardDetails cardsDetails = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, ReadCardDetails.class, HttpStatus.OK);
        THEN("A list of the user's cards is returned");
        Assertions.assertEquals(cardsSummary.getData()
                .getReadCard1DataCard().get(0)
                .getCardNumber(), cardsDetails.getData().getCardDetails().getCardNumber());
        DONE();
    }

    @Order(5)
    @Test()
    public void validateCCSummaryEssentialFields() throws Throwable {
        setupTestUser("+555508711346");
        GIVEN("I have a valid customer with credit cards");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("cardNo", "4584610007");

        WHEN("I call cards details service with valid card number");

        final ReadCardDetails cardsSummary = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, ReadCardDetails.class, HttpStatus.OK);
        THEN("card details are returned by API");
        THEN("Validate card Last Statement date is returned by API");
        Assertions.assertTrue(!cardsSummary.getData().getCardDetails().getLastStatementDate().isEmpty(),"CC Statement dete is blank");

        THEN("Validate card CurrentOutstandingAmount is returned by API");
        Assertions.assertTrue(!cardsSummary.getData().getCardDetails().getCurrentOutstandingAmount().isEmpty(),"CC CurrentOutstandingAmount is blank");

        THEN("Validate card MinimumDueAmount is returned by API");
        Assertions.assertTrue(!cardsSummary.getData().getCardDetails().getMinimumDueAmount().isEmpty(),"CC MinimumDueAmount is blank");

        THEN("Validate card TermDueDate is returned by API");
        Assertions.assertTrue(!cardsSummary.getData().getCardDetails().getTermDueDate().isEmpty(),"CC TermDueDate is blank");

        DONE();
    }

    @Order(6)
    @Test()
    public void validateCCSummaryEssentialFields_InvalidCard() throws Throwable {
        setupTestUser("+555508711346");
        GIVEN("I have a valid customer with credit cards");

        WHEN("I call cards details service with Invalid card number");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("cardNo", "45846100");

        CardDetailsErr cardDetailsErr = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, CardDetailsErr.class, HttpStatus.NOT_FOUND);
        THEN("card details are returned by API");
        THEN("Validate error code returned by API");
        Assertions.assertTrue(cardDetailsErr.getCode().equalsIgnoreCase("UAE.ERROR.NOT_FOUND"));

        THEN("Validate error message returned by API");
        Assertions.assertTrue(cardDetailsErr.getMessage().equalsIgnoreCase("Card not found"));

        WHEN("I call cards details service with card number not available");

        queryParams = new HashMap<>();
        queryParams.put("cardNo", "4584610008");

        cardDetailsErr = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, CardDetailsErr.class, HttpStatus.NOT_FOUND);

        THEN("card details are returned by API");
        THEN("Validate error code returned by API");
        Assertions.assertTrue(cardDetailsErr.getCode().equalsIgnoreCase("UAE.ERROR.NOT_FOUND"));

        THEN("Validate error message returned by API");
        Assertions.assertTrue(cardDetailsErr.getMessage().equalsIgnoreCase("Card not found"));

        WHEN("I call cards details service with card number is not available");

        queryParams = new HashMap<>();
        queryParams.put("cardNo", "");

        cardDetailsErr = this.cardsApiFlows
                .getCreditCardDetails(alphaTestUser, queryParams, CardDetailsErr.class, HttpStatus.NOT_FOUND);

        THEN("card details are returned by API");
        THEN("Validate error code returned by API");
        Assertions.assertTrue(cardDetailsErr.getCode().equalsIgnoreCase("UAE.ERROR.NOT_FOUND"));

        THEN("Validate error message returned by API");
        Assertions.assertTrue(cardDetailsErr.getMessage().equalsIgnoreCase("Card not found"));

        DONE();
    }
}
