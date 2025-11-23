package uk.co.deloitte.banking.banking.temenos.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.StringUtils;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.finance.FinanceResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.temenos.api.FinanceAPI;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Loan")
public class GetLoan {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private FinanceAPI financeAPI;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser(String mobile) {
        envUtils.ignoreTestInEnv(Environments.NFT, Environments.DEV);
        if (alphaTestUser == null || !alphaTestUser.getUserTelephone().equalsIgnoreCase(mobile)) {
            alphaTestUser = new AlphaTestUser(mobile);
            alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin(alphaTestUser);
        }
    }

    @Order(1)
    @Test()
    public void get_Loan_details() {
        if (envUtils.isCit())
            setupTestUser("+555508712115");
        else
            setupTestUser("+555508711388");
        TEST("Get loan details");
        GIVEN("I have a valid customer with active loan with AHB");

        WHEN("User makes a call to get their loan to DTP");
        final FinanceResponse finResp = financeAPI.getActiveLoanDetails(alphaTestUser);

        THEN("A list of the user's loan is returned");
        Assertions.assertTrue(StringUtils.isNotBlank(finResp.getObReadLoanDetails2().get(0).getObreadLoanDetail2().getAccountNumber()));
        DONE();
    }

    @Order(2)
    @Test()
    public void get_Loan_No_Active_LOAN() {
        if (envUtils.isCit())
            setupTestUser("+555508712117");
        else
            setupTestUser("+555508711388");
        TEST("Get loan details");
        GIVEN("I have a valid customer with  NO active loan with AHB");

        WHEN("User makes a call to get their loan to DTP");
        final FinanceResponse finResp = financeAPI.getActiveLoanDetails(alphaTestUser);

        THEN("The user's loan is NOT returned");
        Assertions.assertTrue(finResp.getObReadLoanDetails2() == null);
        DONE();
    }
}
