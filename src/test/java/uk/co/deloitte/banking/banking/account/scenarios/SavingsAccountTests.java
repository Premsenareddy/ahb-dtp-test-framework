package uk.co.deloitte.banking.banking.account.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

@MicronautTest
public class SavingsAccountTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AccountApi accountApi;

    private AlphaTestUser alphaTestUser;

    public void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        }
    }

    @Test
    public void create_correct_current_or_savings_account() {

        TEST("AHBDB-6879: Current account is created regardless of the value sent in AccountSubType");
        GIVEN("A valid new customer exists within DTP and CRM");
        setupTestUser();

        AND("They create a current account and a savings account");

//      At time of writing: will create banking customer and setup a Savings account
        alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);
        String savingsAccountId = this.alphaTestUser.getAccountNumber();

        OBWriteAccountResponse1 currentAccount = this.accountApi.createCustomerCurrentAccount(alphaTestUser);
        Assertions.assertNotNull(currentAccount);
        alphaTestUser.setAccountNumber(currentAccount.getData().getAccountId());

        String currentAccountId = alphaTestUser.getAccountNumber();

        WHEN("We get the accounts listed under the customer");
        OBReadAccount6 accountsResponse = this.accountApi.getAccountsV2(alphaTestUser);

        THEN("They will have both a savings account and a current account");

//      Stores both account types in response in a list and asserts that the list has a savings and current account
        String firstAccount = accountsResponse.getData().getAccount().get(0).getAccountType().toString();
        String secondAccount = accountsResponse.getData().getAccount().get(1).getAccountType().toString();

        ArrayList<String> accountTypes = new ArrayList<>();
        Collections.addAll(accountTypes, firstAccount, secondAccount);

        Assertions.assertTrue(accountTypes.contains("AHB_BASIC_SAV"));
        Assertions.assertTrue(accountTypes.contains("AHB_BASIC_CUR_AC"));

        DONE();
    }
}
