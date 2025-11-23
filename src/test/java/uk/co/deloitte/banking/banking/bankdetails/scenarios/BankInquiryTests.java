package uk.co.deloitte.banking.banking.bankdetails.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.bankdetails.responses.BankInquiryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.bankdetails.api.BankInquiryApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BankInquiryTests {
    @Inject
    private BankInquiryApiFlows bankInquiryApiFlows;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        envUtils.ignoreTestInEnv("Feature not deployed yet on NFT, STG", Environments.NFT, Environments.STG);
        if (this.alphaTestUser == null) {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
    }

    @Test
    public void positive_case_fetchBankDetailsBySwift() {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch bank details");
        THEN("The client submits the bank details request and receives a 200 response");
        String swiftCode = "ICICINBB";
        BankInquiryResponse1 bankInquiryResponse1 = this.bankInquiryApiFlows.fetchBankDetails(alphaTestUser, swiftCode, "S");
        assertNotNull(bankInquiryResponse1);
        DONE();
    }

    @Test
    public void positive_case_fetchBankDetailsByIban() {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch bank details");
        THEN("The client submits the bank details request and receives a 200 response");
        String ibanCode = "AE880340003708345940501";
        BankInquiryResponse1 bankInquiryResponse1 = this.bankInquiryApiFlows.fetchBankDetails(alphaTestUser, ibanCode, "I");
        assertNotNull(bankInquiryResponse1);
        DONE();
    }


    @Test
    public void negative_case_fetchBankDetailsByIban_wrong_identifier() {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I fetch bank details");
        AND("I pass wrong identifier for Iban code");

        THEN("The client submits the bank details request and receives a 400 response");
        String ibanCode = "AE880340003708345940501";
        this.bankInquiryApiFlows.fetchBankInquiryError(alphaTestUser, ibanCode, "S", 400);

        DONE();
    }

    @Test
    public void negative_case_fetchBankDetailsBySwift_wrong_identifier() {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();

        WHEN("I fetch bank details");
        AND("I pass blank space for swift code");

        THEN("The client submits the bank details request and receives a 400 response");
        String swiftCode = "ICICINBB";
        this.bankInquiryApiFlows.fetchBankInquiryError(alphaTestUser, " ", "I", 400);


        DONE();
    }


}
