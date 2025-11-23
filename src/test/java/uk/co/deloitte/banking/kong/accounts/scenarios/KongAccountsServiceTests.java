package uk.co.deloitte.banking.kong.accounts.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardDepositResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.kong.accounts.api.KongAccountsApi;

import javax.inject.Inject;

import java.math.BigDecimal;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KongAccountsServiceTests {

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private KongAccountsApi kongAccountsApi;

    @Inject
    EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    public void setupTestUser() {
        if (alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
            this.alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);

        }

    }

    @Test
    @Order(1)
    public void positive_a_customer_with_an_account_can_be_retrieved_kong_endpoint() {
        TEST("User account can be retrieved with kong endpoints");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        THEN("I go to get their bank account details");
        OBReadAccount6 obReadAccount6 = this.kongAccountsApi.getAccounts(alphaTestUser.getAccountNumber(), 200);
        Assertions.assertNotNull(obReadAccount6);

        DONE();
    }

    @Test
    @Order(2)
    public void positive_a_customer_with_an_account_can_make_cards_deposit() {
        TEST("Card deposit can be made with kong endpoint");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        WHEN("I go to get their bank account details");
        THEN("Their details are returned with their CIF in the response");
        OBWriteCardDepositResponse1 obWriteCardDepositResponse1 = this.kongAccountsApi.createCardDepositKong(alphaTestUser.getAccountNumber(), BigDecimal.valueOf(20), 201);
        Assertions.assertNotNull(obWriteCardDepositResponse1);

        DONE();
    }


    @Test
    @Order(3)
    public void positive_a_customer_with_an_account_can_get_balance() {
        TEST("balance can be retrieved with kong endpoint");
        GIVEN("I have a test user set up with a bank account");
        setupTestUser();
        WHEN("I go to get their bank account details");
        THEN("Their details are returned with their CIF in the response");
        OBReadBalance1 obReadBalance1 = this.kongAccountsApi.getAccountBalanceKong(alphaTestUser.getAccountNumber(), "false", 200);
        Assertions.assertNotNull(obReadBalance1);

        String interimAvailable = obReadBalance1.getData().getBalance().get(0).getAmount().getAmount();
        String interimBooked = obReadBalance1.getData().getBalance().get(1).getAmount().getAmount();

        Assertions.assertEquals(interimAvailable, "20.00");
        Assertions.assertEquals(interimBooked, "20.00");

        DONE();
    }

}


