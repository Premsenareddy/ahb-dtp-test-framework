package uk.co.deloitte.banking.banking.topup.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupInstructedAmount;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupRequestDataV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.topup.api.TopupApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
public class AccountTopupTests {

    public static final String REMITTANCE_INFORMATION = "test";

    @Inject
    private TopupApi topupApi;

    private AlphaTestUser alphaTestUser;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @BeforeEach
    private void setupTestUser() {
        envUtils.ignoreTestInEnv("Cannot run tests which add money using topup as this messes with calculations in " +
                "Transact", Environments.ALL);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);
        }
    }


    @Test
    public void positive_account_topup() {
        TEST("AHBDB-10048 - Account topup");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I top-up an account");
        THEN("The client submits the top-up request and receives a 200 response");
        AccountTopupResponseV1 response = this.topupApi.doTopup(alphaTestUser,
                getValidTopupRequest());
        Assertions.assertEquals("success", response.getAccountTopupResponseData().getStatus());
        Assertions.assertNotNull(response.getAccountTopupResponseData().getPaymentSystemId());
        DONE();
    }


    private AccountTopupRequestV1 getValidTopupRequest() {
        return AccountTopupRequestV1.builder()
                .accountTopupRequestData(AccountTopupRequestDataV1.builder()
                        .instructedAmount(AccountTopupInstructedAmount.builder()
                                .amount(BigDecimal.TEN)
                                .build())
                        .endToEndReference(RandomStringUtils.randomAlphanumeric(12))
                        .remittanceInformation(
                                List.of(REMITTANCE_INFORMATION)
                        )
                        .build()
                )
                .build();
    }
}
