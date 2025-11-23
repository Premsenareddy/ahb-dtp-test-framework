package uk.co.deloitte.banking.cards.scenarios.CreditCardPayments;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.fetchDigitalCards.FetchDigitalCards1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.fetchDigitalCards.InvalidLegacyCif;
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
public class FetchLegacyCards {
    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;

    private AlphaTestUser alphaTestUser;

    public void setupTestUser(String mobile) {
        envUtils.ignoreTestInEnv(Environments.NFT, Environments.DEV);
        if (alphaTestUser == null || !alphaTestUser.getUserTelephone().equalsIgnoreCase(mobile)) {
            alphaTestUser = new AlphaTestUser(mobile);
            alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin(alphaTestUser);
        }
    }

    @Order(1)
    @Test()
    public void get_legacy_cards() {
        TEST("AHBDB 25094 Validate legacy cards for a user");
        GIVEN("I have a valid customer with one credit card linked to account and I login to the account");
        if (envUtils.isCit())
            setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");

        WHEN("User makes a call to get their cards to ESB / HPS");
        String legacyCIF = "3666217";
        final FetchDigitalCards1 cards1 = this.cardsApiFlows.fetchLegacyCardsForUser(alphaTestUser,legacyCIF);

        THEN("I see the legacy cards linked to the user");
        Assertions.assertTrue(StringUtils.isNotBlank(cards1.getData().getFetchDigitalCards3().get(0).getCardNumber()));
        Assertions.assertTrue(StringUtils.isNotBlank(cards1.getData().getFetchDigitalCards3().get(0).getStatus()));
        Assertions.assertTrue(StringUtils.isNotBlank(cards1.getData().getFetchDigitalCards3().get(0).getCardProduct().getProductCode()));
        Assertions.assertTrue(StringUtils.isNotBlank(cards1.getData().getFetchDigitalCards3().get(0).getCardExpiryDate()));
        DONE();
    }


    @Order(2)
    @Test()
    public void get_legacy_cards_empty_response() {
        TEST("Fetch digital cards for a user when user has no card");
        GIVEN("I have a valid customer with no credit card linked to account and I login to the account");
        if (envUtils.isCit())
            setupTestUser("+555581369975");
        else
            setupTestUser("+555508711388");

        String legacyCIF = "5821358";
        WHEN("User makes a call to get their cards to ESB / HPS");
        final FetchDigitalCards1 cards1 = this.cardsApiFlows.fetchLegacyCardsForUser(alphaTestUser,legacyCIF);

        THEN("I see that there are no digital cards linked to the user");
        Assertions.assertNull(cards1.getData().getFetchDigitalCards3());
        DONE();
    }

    @Order(3)
    @Test()
    public void get_legacy_cards_invalid_cif() {
        TEST("Fetch digital cards for a user when user has no card");
        GIVEN("I have a valid customer with no credit card linked to account and I login to the account");
        if (envUtils.isCit())
            setupTestUser("+555581369975");
        else
            setupTestUser("+555508711388");

        String legacyCIF = "123456";
        WHEN("User makes a call to get their cards to ESB / HPS");
        final InvalidLegacyCif invalidLegacyCif = this.cardsApiFlows.fetchLegacyCardsForInvalidCif(alphaTestUser,legacyCIF);

        THEN("I see that the system displays error message");
        Assertions.assertTrue(invalidLegacyCif.getCode().equalsIgnoreCase("UAE.ERROR.NOT_FOUND"));
        Assertions.assertTrue(invalidLegacyCif.getMessage().equalsIgnoreCase("Invalid account/customer number"));
        DONE();
    }

}
