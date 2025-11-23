package uk.co.deloitte.banking.banking.payment.charges.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.internationalCharges.InternationalChargesResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.banking.payment.charges.api.InternationalPaymentChargesApiFlows;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternationalPaymentChargesTest {

    @Inject
    private InternationalPaymentChargesApiFlows internationalPaymentChargesApiFlows;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;


    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;

    @BeforeEach
    private void setupTestUser() {
        envUtils.ignoreTestInEnv(Environments.CIT,Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            alphaTestUserBankingCustomerFactory.setUpAccount(alphaTestUser);
        }
    }


    @Test
    public void positive_case_international_payment_charges() {
        TEST("AHBDB-8637 user can retrive international payment charges");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch international payment charges");
        THEN("The client submits the international payment charges  payload and receives a 200 response");
        InternationalChargesResponse1 chargesResponse1 = internationalPaymentChargesApiFlows.fetchInternationalCharges(alphaTestUser, "BEN");
        assertNotNull(chargesResponse1);
        assertNotNull(chargesResponse1.getData().getInternationalChargesData().get(0).getChargeAmount());
        DONE();
    }
    /*#######################################################
    TestCaseID:AHBDB-14339
    Description: - [GET ./international-payment-charges/accounts/{accountId}?feeType={feeType}] Negative flow 404 Response code: if Invalid FeeType or Invalid Account No Is passed in request
    CreatedBy:Ala
    UpdatedBy:
    LastUpdatedOn:
    Comments:
    #######################################################*/
    @Test
    public void negative_case_international_payment_charges_NotFound_404() {
        TEST("AHBDB-8637 user can retrive international payment charges");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch international payment charges with invalid feeType");

        THEN("The client submits exchange rate payload and receives a 404 response");
        this.internationalPaymentChargesApiFlows.fetchInternationalChargesError(alphaTestUser, "BFF", 404);

        DONE();
    }
    /*#######################################################
    TestCaseID:AHBDB-14340
    Description:[GET ./international-payment-charges/accounts/{accountId}?feeType={feeType}] Negative flow 400 Response code: if missed any mandatory fields in request
    CreatedBy:Ala
    UpdatedBy:
    LastUpdatedOn:
    Comments:
    #######################################################*/
    @Test
    public void negative_case_international_payment_charges_BadRequest_400() {
        TEST("AHBDB-8637 user can retrive international payment charges");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch international payment charges with empty feeType");

        THEN("The client submits exchange rate payload and receives a 400 response");
        this.internationalPaymentChargesApiFlows.fetchInternationalChargesError(alphaTestUser, "", 400);

        DONE();
    }
/*#######################################################
TestCaseID:AHBDB-14337
Description:[GET /international-payment-charges/accounts/{accountId}?feeType={feeType}] Positive flow 200 Response code: if Account ID and FeeType Is correctly passed in request
CreatedBy:Shilpi Agrawal
UpdatedBy:
LastUpdatedOn:
Comments:
#######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({"BEN,21.00", "OUR,126.00", "SHA,21.00" })
    public void positive_case_international_payment_charges_differntFeeType(String feeType, BigDecimal feeCharge) {
        TEST("AHBDB-8637 user can retrive international payment charges");
        TEST("AHBDB-14337 [GET /international-payment-charges/accounts/{accountId}?feeType={feeType}] Positive flow 200 Response code: if Account ID and FeeType Is correctly passed in request");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch international payment charges");
        THEN("The client submits the international payment charges  payload and receives a 200 response");
        InternationalChargesResponse1 chargesResponse1 = this.internationalPaymentChargesApiFlows.fetchInternationalCharges(alphaTestUser, feeType);
        assertNotNull(chargesResponse1);
        assertNotNull(chargesResponse1.getData().getInternationalChargesData().get(0).getChargeAmount());
        THEN("Charges for Requested fee"+feeType+" FeeType  is"+chargesResponse1.getData().getInternationalChargesData().get(0).getChargeAmount());
        BigDecimal amt=chargesResponse1.getData().getInternationalChargesData().get(0).getChargeAmount();
        assertEquals(feeCharge,chargesResponse1.getData().getInternationalChargesData().get(0).getChargeAmount());
        DONE();
    }
/*#######################################################
Description: - Account Number does not belongs to User created or invalid account No is passed 403 error will come
CreatedBy:Shilpi Agrawal
UpdatedBy:
LastUpdatedOn:
Comments:Extra Validation "User is not authorized to perform this operation"
#######################################################*/

    @ParameterizedTest
    @ValueSource(strings = {"45637372" })
    public void negative_case_international_payment_charges_NotFound_403_forAccount(String account) {
        TEST("AHBDB-8637 user can retrive international payment charges");
        TEST("[GET ./international-payment-charges/accounts/{accountId}?feeType={feeType}] Negative flow 403 Response code: if Invalid Invalid Account No Is passed in request");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I fetch international payment charges with invalid Account");

        THEN("The client submits exchange rate payload and receives a 403 response");
        this.internationalPaymentChargesApiFlows.fetchInternationalAccountError(alphaTestUser, account, "BEN",403);

        DONE();
    }
}
