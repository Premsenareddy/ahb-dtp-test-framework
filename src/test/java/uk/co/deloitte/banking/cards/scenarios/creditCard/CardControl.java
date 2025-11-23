package uk.co.deloitte.banking.cards.scenarios.creditCard;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCreditCard1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Cards")
public class CardControl {
    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private CardsApiFlows cardsApiFlows;


    private AlphaTestUser alphaTestUser;
    private HashMap<String, Object> request;
    private String cardNumber;
    private String cardId;

    private void setupTestUser(String mobile) {
        request = new HashMap<>();
        request.put("CardNumber", "0000");
        request.put("CardNumberFlag", "M");
        request.put("InternetUsage", true);
        request.put("NationalUsage", true);
        request.put("NationalPOS", true);
        request.put("NationalATM", true);
        request.put("NationalSwitch", true);
        request.put("InternationalUsage", true);

        envUtils.ignoreTestInEnv(Environments.NFT, Environments.DEV);
        if (alphaTestUser == null || !alphaTestUser.getUserTelephone().equalsIgnoreCase(mobile)) {
            alphaTestUser = new AlphaTestUser(mobile);
            alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin(alphaTestUser);
            final ReadCreditCard1 cards = this.cardsApiFlows.fetchCreditCardsForUser(alphaTestUser);
            cardNumber = cards.getData().getReadCard1DataCard().get(0).getCardNumber();
            cardId = StringUtils.left(cardNumber, 6).concat(StringUtils.right(cardNumber, 4));
        }
    }

    @Order(1)
    @Test()
    public void set_InterNet_National_Ussage() {
        if (envUtils.isCit())
            setupTestUser("+555508712119");
        else
            setupTestUser("+555508711388");

        request.put("NationalPOS", false);
        request.put("NationalATM", false);
        request.put("NationalSwitch", false);
        request.put("InternationalUsage", false);

        TEST("User toggle between allow internet and national usage on its credit card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User set Internet and National usage to yes and no to rest");
        request.put("CardNumber", cardNumber);
        cardsApiFlows.putCardControl(alphaTestUser, request, "credit");

        THEN("User validates the updated card control");
        ReadCardParameters1 readCardParameters1 = cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");
        Assertions.assertTrue(readCardParameters1.getData().getInternetUsage().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalUsage().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getInternationalUsage().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalHilalATM().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("N"));

        THEN("User toggles the card controls from true to false");
        request.put("InternetUsage", false);
        request.put("NationalUsage", false);
        request.put("NationalPOS", true);
        request.put("NationalATM", true);
        cardsApiFlows.putCardControl(alphaTestUser, request, "credit");

        AND("User validates the updated controls");
        readCardParameters1 = cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");
        Assertions.assertTrue(readCardParameters1.getData().getInternetUsage().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalUsage().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalHilalATM().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("Y"));
    }

    @Order(3)
    @Test()
    public void set_International_Usage_false() {
        if (envUtils.isCit())
            setupTestUser("+555508712119");
        else
            setupTestUser("+555508711388");
        request.put("InternationalUsage", false);

        TEST("User toggle between allow international usage on its credit card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User set International usage to No");
        request.put("CardNumber", cardNumber);
        cardsApiFlows.putCardControl(alphaTestUser, request, "credit");

        THEN("User validates the updated card control");
        ReadCardParameters1 readCardParameters1 = cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");
        Assertions.assertTrue(readCardParameters1.getData().getInternetUsage().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalUsage().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getInternationalUsage().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalHilalATM().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("Y"));

        THEN("User toggles the card controls from false to true");
        request.put("InternationalUsage", true);
        request.put("CardNumber", cardNumber);
        cardsApiFlows.putCardControl(alphaTestUser, request, "credit");

        AND("User validates the updated card control");
        readCardParameters1 = cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");
        Assertions.assertTrue(readCardParameters1.getData().getInternationalUsage().equals("Y"));
    }

    @Order(2)
    @Test()
    public void set_All_Control_True() {
        if (envUtils.isCit())
            setupTestUser("+555508712119");
        else
            setupTestUser("+555508711388");
        TEST("User toggle between allow all usage on its credit card");
        GIVEN("I have a valid customer with one credit card linked to account");

        WHEN("User set all controls to Yes");
        request.put("CardNumber", cardNumber);
        cardsApiFlows.putCardControl(alphaTestUser, request, "credit");

        THEN("User validates the updated card control");
        ReadCardParameters1 readCardParameters1 = cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");
        Assertions.assertTrue(readCardParameters1.getData().getInternetUsage().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalUsage().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getInternationalUsage().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalHilalATM().equals("Y"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("Y"));

        THEN("User toggles the card controls from false to true");
        request.put("InternetUsage", false);
        request.put("NationalUsage", false);
        request.put("NationalPOS", false);
        request.put("NationalATM", false);
        request.put("NationalSwitch", false);
        request.put("InternationalUsage", false);
        request.put("CardNumber", cardNumber);
        cardsApiFlows.putCardControl(alphaTestUser, request, "credit");

        AND("User validates the updated card control");
        readCardParameters1 = cardsApiFlows.fetchCardFiltersV2(alphaTestUser, cardId, "credit");
        Assertions.assertTrue(readCardParameters1.getData().getInternetUsage().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalUsage().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getInternationalUsage().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalHilalATM().equals("N"));
        Assertions.assertTrue(readCardParameters1.getData().getNationalPOS().equals("N"));
    }
}
