package uk.co.deloitte.banking.banking.bankdetails.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.bankdetails.responses.BankInquiryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.bankdetails.api.BankInquiryApiFlows;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BankInquiryValidationTests {
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

    //Positive flows to validate various swift codes : "ICICINBB", "ICICCATT", "EBILAEAD", "BOMLAEAD" and Assert the Bank Name and Country
    @ParameterizedTest
    @ValueSource(strings = {"ICICINBB", "ICICCATT", "EBILAEAD", "BOMLAEAD"})
    public void positive_case_fetchBankDetailsBySwiftCode(String swiftCode) {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch bank details");
        THEN("The client submits the bank details request and receives a 200 response");
        BankInquiryResponse1 bankInquiryResponse1 = this.bankInquiryApiFlows.fetchBankDetails(alphaTestUser, swiftCode, "S");
        assertNotNull(bankInquiryResponse1);

        switch (swiftCode) {
            case "BOMLAEAD":
                assertEquals("MASHREQBANK PSC.", bankInquiryResponse1.getBankName());
                assertEquals("United Arab Emirates", bankInquiryResponse1.getCountryName());
                break;

            case "ICICINBB":
                assertEquals("ICICI BANK LIMITED", bankInquiryResponse1.getBankName());
                assertEquals("India", bankInquiryResponse1.getCountryName());
                break;

            case "ICICCATT":
                assertEquals("ICICI BANK CANADA", bankInquiryResponse1.getBankName());
                assertEquals("Canada", bankInquiryResponse1.getCountryName());
                break;

            case "EBILAEAD":
                assertEquals("EMIRATES NBD BANK PJSC", bankInquiryResponse1.getBankName());
                assertEquals("United Arab Emirates", bankInquiryResponse1.getCountryName());
                break;

            default:
                break;
        }
        DONE();
    }


    // Tests to validate various iban codes
    @ParameterizedTest
    @ValueSource(strings = {"JO41ARAB1180000000118258581500", "AE070331234567890123456"})
    public void positive_case_fetchBankDetailsByIbanCode(String ibanCode) {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch bank details");
        THEN("The client submits the bank details request and receives a 200 response");
        BankInquiryResponse1 bankInquiryResponse1 = this.bankInquiryApiFlows.fetchBankDetails(alphaTestUser, ibanCode, "I");
        assertNotNull(bankInquiryResponse1);
        switch (ibanCode) {
            case "JO41ARAB1180000000118258581500":
                assertEquals("ARAB BANK PLC", bankInquiryResponse1.getBankName());
                assertEquals("Jordan", bankInquiryResponse1.getCountryName());
                break;

            case "AE070331234567890123456":
                assertEquals("Mashreqbank", bankInquiryResponse1.getBankName());
                assertEquals("United Arab Emirates", bankInquiryResponse1.getCountryName());
                break;

            default:
                break;
        }
        DONE();
    }

    // Test to validate 400 Response code
    @ParameterizedTest
    @CsvSource({"'ICICINBB', 'AE070331234567890123456'"})
    public void negative_case_fetchBankDetailsByIban_wrong_identifierr(String swiftCode, String iban) {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch bank details");
        AND("I pass wrong identifier for Iban code");
        THEN("The client submits the bank details request and receives a 400 response");
        int ibanCode = iban.length();
        int swiftCoe = swiftCode.length();
        var identifier = ibanCode > swiftCoe ? "S" : "I";
        this.bankInquiryApiFlows.fetchBankInquiryError(alphaTestUser, iban, identifier, 400);
        DONE();
    }

    //Validate Response code 404
    @ParameterizedTest
    @CsvSource({"'ICICINBB', 'I'", "'$%$','I'" , "'AE070wf89012345JHJHK6', 'I'"})
    public void negative_case_fetchBankDetailsBySwift_wrong_identifier(String errorSWC, String identifier) {
        TEST("AHBDB-8622 user can retrieve bank details | Test deployed on DEV CIT & SIT");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch bank details");
        AND("I pass blank space for swift code");
        THEN("The client submits the bank details request and receives a 400 response");
        this.bankInquiryApiFlows.fetchBankInquiryError(alphaTestUser, errorSWC, identifier, 404);
        DONE();
    }
}
