package uk.co.deloitte.banking.banking.account.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentStatus;
import uk.co.deloitte.banking.customer.employment.api.EmploymentApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateAccountEmploymentTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    @Inject
    private EmploymentApiV2 employmentApi;

    private AlphaTestUser alphaTestUser;


    public void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }

    }

    private OBEmploymentDetails1 employmentDetails1(OBEmploymentStatus obEmploymentStatus) {
        return OBEmploymentDetails1.builder()
                .employmentStatus(obEmploymentStatus)
                .companyName("MERRILL LYNCH BANK")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("36")
                .professionCode("99")
                .build();

    }

    @ParameterizedTest
    @ValueSource(strings = {"EMPLOYED", "SELF_EMPLOYED", "OTHER"})
    public void happyPath_CustomerHasValidEmploymentDetails(String employmentStatus) {
        TEST("AHBDB-229: AC1 Customer is employed - 200 response");
        TEST(String.format("AHBDB-2718 to AHBDB-2720: AC1 Positive Test - Happy Path Customer is %s", employmentStatus));

        setupTestUser();
        GIVEN("A valid customer is set up");

        this.alphaTestUserBankingCustomerFactory.setupIdv(alphaTestUser);

        WHEN(String.format("The client updates the employmentStatus to %s", employmentStatus));
        OBEmploymentDetails1 obEmploymentDetails1 = employmentDetails1(OBEmploymentStatus.valueOf(employmentStatus));

        THEN("Their employment status is set");
        this.employmentApi.createEmploymentDetails(alphaTestUser, obEmploymentDetails1);

        AND("They are onboarded to become a banking customer");
        this.alphaTestUserBankingCustomerFactory.setupCrs(alphaTestUser);

        this.alphaTestUserBankingCustomerFactory.updateCustomerInformation(alphaTestUser, false);

        this.alphaTestUserBankingCustomerFactory.setupCif(alphaTestUser);

        this.alphaTestUserBankingCustomerFactory.updateEidStatus(alphaTestUser);

        this.alphaTestUserBankingCustomerFactory.assertAccountScope(alphaTestUser);

        THEN("A bank account can be successfully created for them");
        accountApi.createCustomerSavingsAccount(alphaTestUser);
        accountApi.createCustomerCurrentAccount(alphaTestUser);

        DONE();
    }
}
