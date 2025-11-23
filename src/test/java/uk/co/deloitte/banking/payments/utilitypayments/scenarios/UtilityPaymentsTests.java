package uk.co.deloitte.banking.payments.utilitypayments.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import io.restassured.http.ContentType;
//import org.hibernate.annotations.Check;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry.UtilityInquiryRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry.UtilityInquiryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.*;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.http.common.HttpConstants;
import uk.co.deloitte.banking.payments.utilitypayments.api.UtilityPaymentsApiFlows;
import io.restassured.response.ResponseOptions;

import javax.inject.Inject;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;

import static org.junit.Assert.assertEquals;
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
public class UtilityPaymentsTests {

    private static final String STEP_UP_REQUIRED = "UAE.AUTH.STEP_UP_AUTH_REQUIRED";
    private static final int loginMinWeightExpectedBio = 31;
    @Inject
    private UtilityPaymentsApiFlows utilityPaymentsApiFlows;
    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;
    @Inject
    private EnvUtils envUtils;
    @Inject
    private AuthenticateApiV2 authenticateApi;
    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;
    private AlphaTestUser alphaTestUser;
    @Inject
    private TemenosConfig temenosConfig;

    private void utilityPaymentStepUpAuthBiometrics() {
        authenticateApi.stepUpUserAuthInitiate(alphaTestUser, StepUpAuthInitiateRequest.builder().weight(loginMinWeightExpectedBio).scope("accounts").build());
        final StepUpAuthRequest stepUpAuthValidationRequest = StepUpAuthRequest.builder().password(alphaTestUser.getUserPassword()).scope("accounts").weight(loginMinWeightExpectedBio).build();
        authenticateApi.validateUserStepUpAuth(alphaTestUser, stepUpAuthValidationRequest);
    }


    private void setupTestUser() {
        /**TODO Disabled due to slow performance of ESB Dev**/
        envUtils.ignoreTestInEnv(Environments.CIT);
        if (this.alphaTestUser == null) {

            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
        else {
            this.alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }
    }

    private void setupTestUserNegative() {
        /**TODO Disabled due to slow performance of ESB Dev**/
        envUtils.ignoreTestInEnv(Environments.CIT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);

        }
        else {
            this.alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
        }
    }
    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    private UtilityInquiryRequest1 utilityInquiryRequestDataValid() {
        String reference = RandomDataGenerator.generateRandomAlphanumericUpperCase(12);
        return UtilityInquiryRequest1.builder()
                .referenceNum(reference)
                .utilityCompanyCode("02")
                .utilityAccount("800000040001")
                .utilityAccountPin("0")
                .utilityAccountType("07")
                .build();
    }

    private UtilityPaymentRequest1Data utilityPaymentRequestDataValid() {

        return UtilityPaymentRequest1Data.builder()
                .creditorAccount(CreditorAccount1.builder()
                        .identification("800000040001")
                        .accountType("07")
                        .accountPin("0")
                        .companyCode("02")
                        .build())
                .paymentMode("AC")
                .debtorAccount(DebtorAccount1.builder()
                       //.identification("AED1299900010002")
                        //.identification("019902167001")
                        .identification(temenosConfig.getCreditorAccountId())
                        .currency("AED")
                        .cardNoFlag("F")
                        .build())
                .instructedAmount(InstructedAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .dueAmount(DueAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .build();
    }
    private UtilityPaymentRequest1Data utilityPaymentRequestTPDataValid() {

        return UtilityPaymentRequest1Data.builder()
                .creditorAccount(CreditorAccount1.builder()
                        .identification("800000040001")
                        .accountType("07")
                        .accountPin("0")
                        .companyCode("02")
                        .build())
                .paymentMode("TP")
                .debtorAccount(DebtorAccount1.builder()
                        //.identification(temenosConfig.getCreditorAccountId())
                        .identification(temenosConfig.getTpAccount())
                        //.identification("AED1299900010002")
                        .currency("AED")
                        .cardNoFlag("F")
                        .build())
                .instructedAmount(InstructedAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .dueAmount(DueAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .touchpointData(TouchpointData1.builder()
                        .touchPoints(BigDecimal.valueOf(1000.0))
                        .giftId("6bb3d3cd-868d-321a-a405-5cef4eeff40a")
                        .build()
                )
                .build();
    }

    /*#######################################################
TestCaseID:AHBDB-11177
Description:[POST ./internal/v1/utility-bills/inquiry] Positive flow 200 Response code: When user is send request with valid input data Utility Inquiry and get 0000 or ESB code
CreatedBy:Shilpi Agrawal
UpdatedBy:
LastUpdatedOn:
Comments: DU is not working hence removed Test data ["01, 971777770020714, 0, 04"]
#######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({"02, 800000040001,0, 07",
            "04, 265007704, 0, 99",
           // "06, 30001090550,0, 99"
    })
    public void positive_case_pay_valid_utility_paymentTP_success_verify_response(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType) {
        TEST("AHBDB-9004- User Story -Payment Through TouchPoints");
        TEST("AHBDB-11143- [POST:/internal/v1/utility-bills/payment ]Positive response code 200 : : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        utilityInquiryRequest1.setUtilityAccountPin(utilityAccountPin);
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountType(utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestTPDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        request1Data.getCreditorAccount().setCompanyCode(utilityInquiryResponse.getUtilityCompanyCode());
        request1Data.getCreditorAccount().setIdentification(utilityInquiryResponse.getUtilityAccount());
        request1Data.getCreditorAccount().setAccountType(utilityInquiryResponse.getUtilityAccountType());
        request1Data.getCreditorAccount().setAccountPin(utilityAccountPin);

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data).build();
        THEN("The client tries to pay their payment after doing StepUp Authentication");
        utilityPaymentStepUpAuthBiometrics();
        AND("The client submits payment payload and receives a 200 response and verifies Response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getCompanyCode(), utilityCompanyCode);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getIdentification(), utilityAccount);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getAccountType(), utilityAccountType);
        //Assertions.assertEquals("AED1299900010002", utilityPaymentResponse1.getData().getDebtorAccount().getIdentification());
        DONE();
    }


    @Test
    @Order(1)
    public void positive_case_get_valid_due_amount() {
        TEST("AHBDB-8612- user can get valid due amount");

        GIVEN("I have ignore this test case for CIT and SIT env");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        GIVEN("I have a valid access token and account scope and bank account");

        THEN("The client submits the inquiry payload and receives a 200 response");
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequestDataValid());
        assertNotNull(utilityInquiryResponse);
        assertEquals("07", utilityInquiryResponse.getUtilityAccountType());

        DONE();
    }

    //pass
    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"02"})
    public void positive_case_utility_inquiry_utilityCompanyCode(String utilityCompanyCode) {
        TEST("AHBDB-8612- user can get valid due amount with utilityCompanyCode : " + utilityCompanyCode);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I get due amount");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        THEN("The client submits inquiry payload and receives a 200 response");
        Assertions.assertEquals(utilityInquiryResponse.getUtilityCompanyCode(), utilityCompanyCode);
        DONE();
    }

/*#######################################################
TestCaseID:AHBDB-11177
Description:[POST ./internal/v1/utility-bills/inquiry] Positive flow 200 Response code: When user is send request with valid input data Utility Inquiry and get 0000 or ESB code
CreatedBy:Shilpi Agrawal
UpdatedBy:
LastUpdatedOn:
Comments: DU is not working hence removed Test data ["01, 971777770020714, 0, 04"]
#######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({
            "03, 32100001, 6843, 99",
            "02, 490100001, 0, 06",
            "04, 265007704, 0, 99",
            "05, 2181000000, 0, 99",
            "06, 3929320000, 0, 99",
            "08, 1704676501, 0, 99",
            "09, 490100009, 0, 11"

    })


//As DU is not working "01, 971777770020714, 0, 04",
    public void positive_case_utility_inquiry_utilityAccountType(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType) {
        TEST("AHBDB-6094- User Story -Initiate Utility Inquiry");
        TEST("AHBDB-11177- [POST ./internal/v1/utility-bills/inquiry] Positive flow 200 Response code: When user is send request with valid input data Utility Inquiry and get 0000 or ESB code : Test Data as follows utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountPin: " + utilityAccountPin + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");

       // envUtils.ignoreTestInEnv(Environments.CIT);

        setupTestUser();
        WHEN("I post Request for Utility Inquiry Endpoint and call forwarded to ESB Endpoint Context-path /internal/v1/payments/bill/enquiry with valid inputs");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        utilityInquiryRequest1.setUtilityAccountPin(utilityAccountPin);
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountType(utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        THEN("The client submits inquiry payload and receives a 200 response and verify response");
        Assertions.assertEquals(utilityCompanyCode, utilityInquiryResponse.getUtilityCompanyCode());
        Assertions.assertEquals(utilityAccount, utilityInquiryResponse.getUtilityAccount());
        Assertions.assertEquals(utilityAccountType, utilityInquiryResponse.getUtilityAccountType());
      //  Assertions.assertEquals(5000, utilityInquiryResponse.getAmountMax());
      //  Assertions.assertEquals(50, utilityInquiryResponse.getAmountMin());
        DONE();
    }


    /*#######################################################
    TestCaseID:AHBDB-11576
    Description:[POST ./internal/v1/utility-bills/inquiry] Negative flow 400 Response code: utilityAccountPin - is only mandatory for utility Company SALIK (toll) biller-03 and optional field for other utility Company
    CreatedBy:Shilpi Agrawal
    UpdatedBy:
    LastUpdatedOn:
    Comments:
    #######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({"03, 32100001, 99"})
    public void negative_case_utility_inquiry_SalikUtilityPinMandatory(String utilityCompanyCode, String utilityAccount, String utilityAccountType) {
        TEST("AHBDB-6094- User Story -Initiate Utility Inquiry");
        TEST("AHBDB-11576: [POST ./internal/v1/utility-bills/inquiry] Negative flow 400 Response code: utilityAccountPin - is only mandatory for utility Company SALIK (toll) biller-03 and optional field for other utility Company : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I post Request for Utility Inquiry Endpoint for Salik without Utility Pin /internal/v1/payments/bill/enquiry with valid inputs");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountType(utilityAccountType);
        THEN("The client submits inquiry payload for Salik without Utility Pin and receives a 400 response");
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.utilityInquiryError(alphaTestUser, utilityInquiryRequest1, 400);
        Assertions.assertEquals("Invalid Number of Digits in Utility Account Pin.", response1.getMessage());
        DONE();
    }

    /*#######################################################
    TestCaseID:AHBDB-11557
    Description:[POST ./internal/v1/utility-bills/inquiry] Negative flow 400 Bad Response Error Code: When user is send request with invalid input data and get error from ESBERRORCODE other than 0000
    CreatedBy:Shilpi Agrawal
    UpdatedBy:
    LastUpdatedOn:
    Comments:
    #######################################################*/
    @Order(2)
    @Tag("Defect12346")
    @ParameterizedTest
    @CsvSource({"'','',490100001,06",
            "'',02, '', 06",
            "'',02,490100001,''",
            "'BLANK',02,490100001,06",
            "'1234abcdABCDE',02,490100001,06",
            "'','023',490100001,06",
            "'',02,'ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVW',06",
            "'',02,'ABCDEFGHI',567"

    })
    public void negative_case_missing_utility_inquiry_mandatory_details(String referenceNum, String utilityCompanyCode, String utilityAccount, String utilityAccountType) {
        TEST("AHBDB-6094- User Story -Initiate Utility Inquiry");
        TEST("AHBDB-11557- [POST ./internal/v1/utility-bills/inquiry] Negative flow 400 Bad Response Error Code: When user is send request with invalid input data and get error from ESBERRORCODE other than 0000 : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("Post call is initiated to API endpoint ' /internal/v1/payments/bill/enquiry with invalid inputs'");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        if ((referenceNum.trim()).equalsIgnoreCase("BLANK") ) {
            utilityInquiryRequest1.setReferenceNum("");
        }
        else if((referenceNum.trim()).equalsIgnoreCase("1234abcdABCDE")){
            utilityInquiryRequest1.setReferenceNum(RandomDataGenerator.generateRandomAlphanumericUpperCase(13));
        }
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountType(utilityAccountType);
        THEN("Payment adapter will process and submits payment payload to ESB and receives a 400 response");
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.utilityInquiryError(alphaTestUser, utilityInquiryRequest1, 400);
        AND("Actual Error Message displayed in Response " + response1.getMessage());
        DONE();
    }

    /*#######################################################
  TestCaseID:AHBDB-12075
  Description:[POST ./internal/v1/utility-bills/inquiry] Verify Response for offline billers 09-Zakat and 04-DEWA should not contain fields like amountMin and amountMax
  CreatedBy:Shilpi Agrawal
  UpdatedBy:
  LastUpdatedOn:
  Comments:
  #######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({"04, 265007704, 99"})
    public void negative_case_utility_inquiry_offlineBillersResponseVerification(String utilityCompanyCode, String utilityAccount, String utilityAccountType) {
        TEST("AHBDB-6094- User Story -Initiate Utility Inquiry");
        TEST("AHBDB-12075- [POST ./internal/v1/utility-bills/inquiry] Verify Response for offline billers 09-Zakat and 04-DEWA should not contain fields like amountMin and amountMax : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("Post call is initiated to API endpoint ' /internal/v1/payments/bill/enquiry with invalid inputs' for offline billers i.e. 04 DEWA, 09 ZAKAT");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountType(utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        THEN("The client submits inquiry payload and receives a 200 response and verify response should not have AccountMax, AccountMin and Account fields");
        Assertions.assertEquals(null, utilityInquiryResponse.getAmountMax());
        Assertions.assertEquals(null, utilityInquiryResponse.getAmountMin());
        Assertions.assertEquals(null, utilityInquiryResponse.getAmount());
        DONE();
    }

    /*#######################################################
     TestCaseID:AHBDB-11573
     Description:[POST ./internal/v1/utility-bills/inquiry] Negative flow 401 Unauthorised response: When user is send request with invalid JWT Token
     CreatedBy:Shilpi Agrawal
     UpdatedBy:
     LastUpdatedOn:
     Comments:
     #######################################################*/
    @Test
    @Order(31)
    public void negative_test_utility_inquiry_has_invalid_token() {
        TEST("AHBDB-6094- User Story -Initiate Utility Inquiry");
        TEST("AHBDB-11573 [POST ./internal/v1/utility-bills/inquiry] Negative flow 401 Unauthorised response: When user is send request with invalid JWT Token");
        GIVEN("I have a valid customer with an invalid token");
        setupTestUser();
        this.alphaTestUser.getLoginResponse().setAccessToken(null);
        WHEN("Post call is initiated to API endpoint ' /internal/v1/payments/bill/enquiry with invalid inputs' with invalid bearer token");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        THEN("Payment adapter submits payment payload to ESB and receives a 401 response");
        int statusCode = this.utilityPaymentsApiFlows.utilityInquiryReturnStatusCode(alphaTestUser, utilityInquiryRequest1);
        Assertions.assertEquals(401, statusCode);
        DONE();
    }

    /*#######################################################
    TestCaseID:AHBDB-11581
    Description:[POST:/internal/v1/utility-bills/payment ]Positive response code 200 - User is able to send pay service provider request with valid input data and receive acknowledgment from ESB with return code 0000
    CreatedBy:Shilpi Agrawal
    UpdatedBy:
    LastUpdatedOn:
    Comments:SALIK commented as not working
    #######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({"03, 32100001, 6843, 99",
            "02, 800000040001,0, 07",
            "04, 265007704, 0, 99",
            "02, 490100001, 0, 06"
    })
    public void positive_case_pay_valid_utility_payment_success_verify_response(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType) {
        TEST("AHBDB-8612- User Story -Pay Service provider bill");
        TEST("AHBDB-11581 - user can pay valid service providers bill with utilityCompanyCode : : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        utilityInquiryRequest1.setUtilityAccountPin(utilityAccountPin);
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountType(utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        request1Data.getCreditorAccount().setCompanyCode(utilityInquiryResponse.getUtilityCompanyCode());
        request1Data.getCreditorAccount().setIdentification(utilityInquiryResponse.getUtilityAccount());
        request1Data.getCreditorAccount().setAccountType(utilityInquiryResponse.getUtilityAccountType());
        request1Data.getCreditorAccount().setAccountPin(utilityAccountPin);
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data).build();
        THEN("The client tries to pay their payment after doing StepUp Authentication");
        utilityPaymentStepUpAuthBiometrics();
        AND("The client submits payment payload and receives a 200 response and verifies Response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getCompanyCode(), utilityCompanyCode);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getIdentification(), utilityAccount);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getAccountType(), utilityAccountType);
        DONE();
    }

    /*#######################################################
        TestCaseID:AHBDB-11581
        Description:[POST:/internal/v1/utility-bills/payment ]Positive response code 200 - User is able to send pay service provider request with valid input data and receive acknowledgment from ESB with return code 0000
        CreatedBy:Shilpi Agrawal
        UpdatedBy:
        LastUpdatedOn:
        Comments:User should be able to do payment without entering optional field details
        #######################################################*/
    @Order(1)
    @Test
    public void positive_case_pay_valid_utility_payment_optional_require_fields() {
        TEST("AHBDB-8612- User Story -Pay Service provider bill");
        TEST("AHBDB-11581: [POST:/internal/v1/utility-bills/payment ]Positive response code 200 - User is able to send pay service provider request with valid input data and receive acknowledgment from ESB with return code 0000");
        GIVEN("I have a valid access token and account scope");

        setupTestUser();
        WHEN("I pay service provider bill");

        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequestDataValid());
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        request1Data.getDebtorAccount().setCardNoFlag(null);
        request1Data.getCreditorAccount().setAccountPin("0");
        request1Data.getDueAmount().setCurrency(null);
        request1Data.getInstructedAmount().setCurrency(null);
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data)
                .build();
        THEN("The client tries to pay their payment");
        utilityPaymentStepUpAuthBiometrics();
        AND("The client submits payment payload with optional fields as null and receives a 200 response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        assertNotNull(utilityPaymentResponse1);
        DONE();
    }

    /*#######################################################
    TestCaseID:AHBDB-11144
    Description:[POST:/internal/v1/utility-bills/payment ]Negative response code 400 - User is not able to send pay service provider request and receive acknowledgment from ESB with invalid input data
    CreatedBy:Shilpi Agrawal
    UpdatedBy:
    LastUpdatedOn:
    Comments:
    #######################################################*/
    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"END_TO_END_IDENTIFICATION_BLANK",
            "PAYMENT_MODE",
            "CREDITOR_ACCOUNT_IDENTIFICATION",
            "ACCOUNT_PIN",
            "ACCOUNT_TYPE",
            "DEBTOR_ACCOUNT_IDENTIFICATION",
            "DEBTOR_ACCOUNT_CURRENCY",
            "INSTRUCTED_AMOUNT",
            "DUE_AMOUNT"

    })
    public void negative_case_pay_valid_utility_payment_utilityAccountType_mandatory_details(String mandatoryFieldName) {
        TEST("AHBDB-8612- User Story -Pay Service provider bill");
        TEST("AHBDB-11144 [POST:/internal/v1/utility-bills/payment ]Negative response code 400 - User is not able to send pay service provider request and receive acknowledgment from ESB with missing mandatory input data :Missing/Invalid Test Data for following field: " + mandatoryFieldName);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequestDataValid());

        UtilityPaymentRequest1Data utilityPaymentRequest1Data = utilityPaymentRequestDataValid();
        utilityPaymentRequest1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        utilityPaymentRequest1Data.getCreditorAccount().setCompanyCode(utilityInquiryResponse.getUtilityCompanyCode());
        utilityPaymentRequest1Data.getCreditorAccount().setIdentification(utilityInquiryResponse.getUtilityAccount());
        utilityPaymentRequest1Data.getCreditorAccount().setAccountType(utilityInquiryResponse.getUtilityAccountType());

        switch (mandatoryFieldName.toUpperCase()) {
            case "END_TO_END_IDENTIFICATION_BLANK":
                utilityPaymentRequest1Data.setEndToEndIdentification(null);
                break;
            case "END_TO_END_IDENTIFICATION_INVALID":
                String reference = RandomDataGenerator.generateRandomAlphanumericUpperCase(12);
                utilityPaymentRequest1Data.setEndToEndIdentification(reference);
                break;
            case "PAYMENT_MODE":
                utilityPaymentRequest1Data.setPaymentMode(null);
                break;
            case "CREDITOR_ACCOUNT_IDENTIFICATION":
                utilityPaymentRequest1Data.getCreditorAccount().setIdentification(null);
                break;
            case "ACCOUNT_PIN":
                utilityPaymentRequest1Data.getCreditorAccount().setAccountPin(null);
                break;
            case "ACCOUNT_TYPE":
                utilityPaymentRequest1Data.getCreditorAccount().setAccountType(null);
                break;
            case "DEBTOR_ACCOUNT_IDENTIFICATION":
                utilityPaymentRequest1Data.getDebtorAccount().setIdentification(null);
                break;
            case "DEBTOR_ACCOUNT_CURRENCY":
                utilityPaymentRequest1Data.getDebtorAccount().setCurrency(null);
                break;
            case "INSTRUCTED_AMOUNT":
                utilityPaymentRequest1Data.getInstructedAmount().setAmount(null);
                break;
            case "DUE_AMOUNT":
                utilityPaymentRequest1Data.getDueAmount().setAmount(null);
                break;

        }
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(utilityPaymentRequest1Data)
                .build();
        THEN("The client tries to pay their payment");
        utilityPaymentStepUpAuthBiometrics();
        AND("The client submits payment payload and receives a 400 response");
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser, utilityPaymentRequest1, 400);
        AND("Actual Error Message displayed in Response " + response1.getMessage());
        AND("Actual Error Code displayed in Response " + response1.getCode());
        DONE();
    }

    /*#######################################################
    TestCaseID:AHBDB-11664
    Description:[POST ./internal/v1/utility-bills/payments] Negative flow 400 Response code: utilityAccountPin - is only mandatory for utility Company SALIK (toll) biller-03 and optional field for other utility Company
    CreatedBy:Shilpi Agrawal
    UpdatedBy:
    LastUpdatedOn:
    Comments:
    #######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({"32100001, 6843"})
    public void negative_case_utility_Payment_SalikUtilityPinMandatory(String utilityAccount, String utilityAccountPin) {
        TEST("AHBDB-8612- User Story -Pay Service provider bill");
        TEST("AHBDB-11664: [POST ./internal/v1/utility-bills/payments] Negative flow 400 Response code: utilityAccountPin - is only mandatory for utility Company SALIK (toll) biller-03 and optional field for other utility Company: Test Data as follows utility Pin as '0' and utilityCompanyCode: as 03 and utilityAccount: " + utilityAccount + "Utility AccountType as 99 and utilityAccountPin: " + utilityAccountPin);
        GIVEN("I have a valid access token and account scope");

        setupTestUser();
        AND("I post valid Utility Inquiry request for Salik biller with valid Account Pin");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode("03");
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountPin(utilityAccountPin);
        utilityInquiryRequest1.setUtilityAccountType("99");
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        WHEN("I post Request for Utility Payment Endpoint for Salik without Utility Pin /internal/v1/payments/bill/enquiry with valid inputs");
        UtilityPaymentRequest1Data utilityPaymentRequest1Data = utilityPaymentRequestDataValid();
        utilityPaymentRequest1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        utilityPaymentRequest1Data.getCreditorAccount().setCompanyCode(utilityInquiryResponse.getUtilityCompanyCode());
        utilityPaymentRequest1Data.getCreditorAccount().setIdentification(utilityInquiryResponse.getUtilityAccount());
        utilityPaymentRequest1Data.getCreditorAccount().setAccountType(utilityInquiryResponse.getUtilityAccountType());
        utilityPaymentRequest1Data.getCreditorAccount().setAccountPin(null);
        THEN("The client submits inquiry payload for Salik without Utility Pin and receives a 400 response");
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(utilityPaymentRequest1Data)
                .build();
        utilityPaymentStepUpAuthBiometrics();
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser, utilityPaymentRequest1, 400);
        Assertions.assertEquals("null Bad request utility account pin should be less than or equal to :8", response1.getMessage());
        DONE();
    }

    /*#######################################################
      TestCaseID:AHBDB-11174
      Description:[POST ./internal/v1/utility-bills/payments] Negative flow 401 Unauthorised response: When user is send request with invalid JWT Token
      CreatedBy:Shilpi Agrawal
      UpdatedBy:
      LastUpdatedOn:
      Comments:
      #######################################################*/
    @Order(20)
    @Test
    public void negative_test_utility_payment_has_invalid_token() {
        TEST("AHBDB-8612- User Story -Pay Service provider bill");
        TEST("AHBDB-11174 -[POST ./internal/v1/utility-bills/payments] Negative flow 401 Unauthorised response: When user is send request with invalid JWT Token");
        GIVEN("I have a valid customer with an invalid token");
        setupTestUserNegative();
        this.alphaTestUser.getLoginResponse().setAccessToken(null);
        WHEN("Post call is initiated to API endpoint ' /internal/v1/utility-bills/payments with invalid inputs' with invalid bearer token");
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        THEN("Payment adapter submits payment payload to ESB and receives a 401 response");
        UtilityPaymentRequest1Data utilityPaymentRequest1Data = utilityPaymentRequestDataValid();

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(utilityPaymentRequest1Data)
                .build();
        var response1 = this.utilityPaymentsApiFlows.payUtilityPaymentsErrorTest(alphaTestUser, utilityPaymentRequest1, 401);
        DONE();
    }

    /*#######################################################
      TestCaseID:AHBDB-12120
      Description:[POST ./internal/v1/utility-bills/payments] Negative flow 404 Not Found Response code: When user is send request with invalid data which is not found in ESB
      CreatedBy:Shilpi Agrawal
      UpdatedBy:
      LastUpdatedOn:
      Comments: Do not use test data for Inquiry company( 02,0504930005,02)
      #######################################################*/
    @Order(1)
    @ParameterizedTest
    @CsvSource({"05, 2181000000, 99"})
    public void negative_case_utility_payment_transactionNotFound(String utilityCompanyCode, String utilityAccount, String utilityAccountType) {
        TEST("AHBDB-8612- User Story -Pay Service provider bill");
        TEST("AHBDB-12120 - [POST ./internal/v1/utility-bills/payments] Negative flow 404 Not Found Response code: When user is send request with invalid data which is not found in ESB: Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope and bank account");
        setupTestUserNegative();
        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);
        utilityInquiryRequest1.setUtilityAccount(utilityAccount);
        utilityInquiryRequest1.setUtilityAccountType(utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data)
                .build();

        WHEN("Get an API call for utility payment with invalid data apart from reference number And post request Utility payment  endpoint with which is not found in ESB");

        utilityPaymentStepUpAuthBiometrics();

        THEN("Payment adapter will process and provide error response 404 meaning the utility inquiry failed Response could have ESBERRORCODE As [0609,0404,0313,0605]");
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser, utilityPaymentRequest1, 404);
        AND("Error Message is displayed " + response1.getMessage());
        Assertions.assertEquals("Transaction ID does not exist", response1.getMessage());
        DONE();
    }

    @Test
    @Order(1)
    public void positive_case_pay_valid_utility_payment() {
        TEST("AHBDB-8612-AHBDB-9004 - user can pay valid service provider bills");
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        GIVEN("I have a valid access token and account scope and bank account");

        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequestDataValid());
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data)
                .build();

        THEN("The client tries to pay their payment");
        OBErrorResponse1 obErrorResponse1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser,
                utilityPaymentRequest1, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        utilityPaymentStepUpAuthBiometrics();

        THEN("The client submits the payment payload and receives a 200 response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        assertNotNull(utilityPaymentResponse1);
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"02"})
    public void positive_case_pay_valid_utility_payment_utilityCompanyCode(String utilityCompanyCode) {
        TEST("AHBDB-8612-AHBDB-9004 - user can pay valid service providers bill with utilityCompanyCode : " + utilityCompanyCode);
        GIVEN("I have a valid access token and account scope");

        setupTestUser();
        WHEN("I pay service provider bill");

        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequestDataValid());
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        request1Data.getCreditorAccount().setCompanyCode(utilityCompanyCode);

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data)
                .build();

        THEN("The client tries to pay their payment");
        OBErrorResponse1 obErrorResponse1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser,
                utilityPaymentRequest1, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        utilityPaymentStepUpAuthBiometrics();


        THEN("The client submits payment payload and receives a 200 response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getCompanyCode(), utilityCompanyCode);
        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"07"})
    public void positive_case_pay_valid_utility_payment_utilityAccountType(String utilityAccountType) {
        TEST("AHBDB-8612-AHBDB-9004  - user can pay valid service providers bill with utilityAccountType : " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");

        UtilityInquiryRequest1 utilityInquiryRequest = utilityInquiryRequestDataValid();
        utilityInquiryRequest.setUtilityAccountType(utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest);
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        request1Data.getCreditorAccount().setAccountType(utilityAccountType);

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data)
                .build();

        THEN("The client tries to pay their payment");
        OBErrorResponse1 obErrorResponse1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser,
                utilityPaymentRequest1, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        utilityPaymentStepUpAuthBiometrics();

        THEN("The client submits payment payload and receives a 200 response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getAccountType(), utilityAccountType);


        DONE();
    }

    @Order(1)
    @Test
    public void positive_case_pay_valid_utility_payment_minimal_require_fields() {
        TEST("AAHBDB-8612-AHBDB-9004  - user can pay valid service providers with minimum required fields");
        GIVEN("I have a valid access token and account scope");

        setupTestUser();
        WHEN("I pay service provider bill");

        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequestDataValid());
        UtilityPaymentRequest1Data request1Data = utilityPaymentRequestDataValid();
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        request1Data.getDebtorAccount().setCardNoFlag(null);
        request1Data.getCreditorAccount().setAccountPin("0");


        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data)
                .build();

        THEN("The client tries to pay their payment");
        OBErrorResponse1 obErrorResponse1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser,
                utilityPaymentRequest1, 403);
        Assertions.assertEquals(obErrorResponse1.getCode(), STEP_UP_REQUIRED);

        AND("The user is returned a 403 as they need to complete step up auth");
        utilityPaymentStepUpAuthBiometrics();

        THEN("The client submits payment payload and receives a 200 response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        assertNotNull(utilityPaymentResponse1);
        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"", "1234", "$&*^£"})
    public void negative_case_utility_inquiry_utilityCompanyCode(String utilityCompanyCode) {
        TEST("AHBDB-8612- user can get valid due amount with utilityCompanyCode : " + utilityCompanyCode);

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I get due amount");

        UtilityInquiryRequest1 utilityInquiryRequest1 = utilityInquiryRequestDataValid();
        utilityInquiryRequest1.setUtilityCompanyCode(utilityCompanyCode);

        THEN("The client submits payment payload and receives a 400 response");
        this.utilityPaymentsApiFlows.utilityInquiryError(alphaTestUser, utilityInquiryRequest1, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"", "1234", "$&*^£"})
    public void negative_case_pay_valid_utility_payment_utilityCompanyCode(String utilityCompanyCode) {
        TEST("AHBDB-8612-AHBDB-9004 - user can pay a valid utility payment for service provider");

        GIVEN("I have a valid access token and account scope");
        setupTestUserNegative();
        WHEN("I pay service provider bill");

        UtilityPaymentRequest1Data utilityPaymentRequest1Data = utilityPaymentRequestDataValid();
        utilityPaymentRequest1Data.setEndToEndIdentification("bMzwlFbqHEsj");
        utilityPaymentRequest1Data.getCreditorAccount().setCompanyCode(utilityCompanyCode);

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(utilityPaymentRequest1Data)
                .build();

        THEN("The client submits payment payload and receives a 400 response");
        this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser, utilityPaymentRequest1, 400);

        DONE();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"", "$&*^£"})
    public void negative_case_pay_valid_utility_payment_utilityAccountType(String utilityAccountType) {
        TEST("AHBDB-8612-AHBDB-9004 - user can pay a valid utility payment for service provider");

        GIVEN("I have a valid access token and account scope");

        setupTestUserNegative();
        WHEN("I pay service provider bill");

        UtilityPaymentRequest1Data utilityPaymentRequest1Data = utilityPaymentRequestDataValid();
        utilityPaymentRequest1Data.setEndToEndIdentification("bMzwlFbqHEsj");
        utilityPaymentRequest1Data.getCreditorAccount().setAccountType(utilityAccountType);

        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(utilityPaymentRequest1Data)
                .build();

        THEN("The client submits payment payload and receives a 400 response");
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser, utilityPaymentRequest1, 400);
        response1.getCode();

        DONE();
    }

    @Order(1)
    @ParameterizedTest
    @CsvSource({"04, 265007704, 0, 99"})
    public void positive_case_pay_valid_utility_paymentTP_success_verify_responseTP(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType) {
        TEST("AHBDB-9004- User Story -Touch point");
        TEST("AHBDB-11143 - user can pay valid service providers bill with utilityCompanyCode : : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");
        UtilityInquiryRequest1 utilityInquiryRequest1 = getUtilityInquiryRequest1(utilityCompanyCode,utilityAccount, utilityAccountPin, utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        UtilityPaymentRequest1Data request1Data = getUtilityPaymentRequest1Data( utilityInquiryResponse, utilityAccountPin);
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data).build();
        THEN("The client tries to pay their payment after doing StepUp Authentication");
        utilityPaymentStepUpAuthBiometrics();
        AND("The client submits payment payload and receives a 200 response and verifies Response");
        UtilityPaymentResponse1 utilityPaymentResponse1 = this.utilityPaymentsApiFlows.payUtilityPayments(alphaTestUser, utilityPaymentRequest1);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getCompanyCode(), utilityCompanyCode);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getIdentification(), utilityAccount);
        Assertions.assertEquals(utilityPaymentResponse1.getData().getCreditorAccount().getAccountType(), utilityAccountType);
        //Assertions.assertEquals("AED1299900010002", utilityPaymentResponse1.getData().getDebtorAccount().getIdentification());
        DONE();
    }

    @Order(2)
    @Tag("HappyPath")
    @ParameterizedTest
    @CsvSource({"04, 265007704, 0, 99, 0"})
    public void negative_case_pay_valid_utility_payment_utilityAccountType_mandatory_detailsTP(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType, String invalidCompany) {
        TEST("AHBDB-9004- User Story - Touch points");
        TEST("AHBDB-11144 - user can pay valid service providers bill with utilityCompanyCode : : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");
        UtilityInquiryRequest1 utilityInquiryRequest1 = getUtilityInquiryRequest1(utilityCompanyCode,utilityAccount, utilityAccountPin, utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        UtilityPaymentRequest1Data request1Data = getUtilityPaymentRequest1Data( utilityInquiryResponse, utilityAccountPin);
        request1Data.setEndToEndIdentification(utilityInquiryResponse.getReferenceNum());
        request1Data.getCreditorAccount().setCompanyCode(invalidCompany);
        request1Data.getCreditorAccount().setIdentification(utilityInquiryResponse.getUtilityAccount());
        request1Data.getCreditorAccount().setAccountType(utilityInquiryResponse.getUtilityAccountType());
        request1Data.getCreditorAccount().setAccountPin(utilityAccountPin);
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data).build();
        THEN("The client tries to pay their payment after doing StepUp Authentication");
        utilityPaymentStepUpAuthBiometrics();
        AND("The client submits payment payload and receives a 400 response and verifies Response");
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser, utilityPaymentRequest1, 400);
        Assertions.assertEquals("Invalid utility account length", response1.getMessage());
        DONE();
    }

    @Order(60)
    @ParameterizedTest
    @CsvSource({"04, 265007704, 0, 99"})
    public void negative_case_TP_Forbidden(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType) {
        TEST("AHBDB-9004- User Story - Touch points");
        TEST("AHBDB-12120 - 403 : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");
        UtilityInquiryRequest1 utilityInquiryRequest1 = getUtilityInquiryRequest1(utilityCompanyCode,utilityAccount, utilityAccountPin, utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        UtilityPaymentRequest1Data request1Data = getUtilityPaymentRequest1Data( utilityInquiryResponse, utilityAccountPin);
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data).build();
        THEN("The client tries to pay their payment after without doing StepUp Authentication");
        AND("The client submits payment payload and receives a 403 response and verifies Response");
        OBErrorResponse1 response1 = this.utilityPaymentsApiFlows.payUtilityPaymentsError(alphaTestUser, utilityPaymentRequest1, 403);
        Assertions.assertEquals("Step up permissions needed to perform this operations", response1.getMessage());
        DONE();
    }


    @Order(60)
    @Tag("NegativeFlow")
    @ParameterizedTest
    @CsvSource({"04, 265007704, 0, 99"})
    public void negative_case_TP_Forbiden(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType) {
        TEST("AHBDB-9004- User Story - Touch points");
        TEST("AHBDB-11174 - 401 : Test Data as follows utility Pin as '0' and utilityCompanyCode: " + utilityCompanyCode + " utilityAccount: " + utilityAccount + " utilityAccountType: " + utilityAccountType);
        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I pay service provider bill");
        UtilityInquiryRequest1 utilityInquiryRequest1 = getUtilityInquiryRequest1(utilityCompanyCode,utilityAccount, utilityAccountPin, utilityAccountType);
        UtilityInquiryResponse1 utilityInquiryResponse = this.utilityPaymentsApiFlows.utilityInquiry(alphaTestUser, utilityInquiryRequest1);
        UtilityPaymentRequest1Data request1Data = getUtilityPaymentRequest1Data( utilityInquiryResponse, utilityAccountPin);
        UtilityPaymentRequest1 utilityPaymentRequest1 = UtilityPaymentRequest1.builder().data(request1Data).build();
        THEN("The client tries to pay their payment after doing StepUp Authentication");
        utilityPaymentStepUpAuthBiometrics();
        AND("The client submits payment payload and receives a 401 response and verifies Response code");
        this.utilityPaymentsApiFlows.forbiden(alphaTestUser, utilityPaymentRequest1, 401);
        DONE();
    }

    private  UtilityInquiryRequest1 getUtilityInquiryRequest1(String utilityCompanyCode, String utilityAccount, String utilityAccountPin, String utilityAccountType){
        String reference = RandomDataGenerator.generateRandomAlphanumericUpperCase(12);
        return UtilityInquiryRequest1.builder()
                .referenceNum(reference)
                .utilityCompanyCode(utilityCompanyCode)
                .utilityAccount(utilityAccount)
                .utilityAccountPin(utilityAccountPin)
                .utilityAccountType(utilityAccountType)
                .build();
    }

    private UtilityPaymentRequest1Data getUtilityPaymentRequest1Data(UtilityInquiryResponse1 utilityInquiryRequest1, String accountPin) {
        return UtilityPaymentRequest1Data.builder()
                .endToEndIdentification(utilityInquiryRequest1.getReferenceNum())
                .creditorAccount(CreditorAccount1.builder()
                        .identification(utilityInquiryRequest1.getUtilityAccount())
                        .accountType(utilityInquiryRequest1.getUtilityAccountType())
                        .accountPin(accountPin)
                        .companyCode(utilityInquiryRequest1.getUtilityCompanyCode())
                        .build())
                .paymentMode("TP")
                .debtorAccount(DebtorAccount1.builder()
                        .identification(temenosConfig.getTpAccount())
                        .currency("AED")
                        .cardNoFlag("F")
                        .build())
                .instructedAmount(InstructedAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .dueAmount(DueAmount1.builder()
                        .amount(BigDecimal.valueOf(50.0))
                        .currency("AED")
                        .build())
                .touchpointData(TouchpointData1.builder()
                        .touchPoints(BigDecimal.valueOf(1000.0))
                        .giftId("6bb3d3cd-868d-321a-a405-5cef4eeff40a")
                        .build()
                )
                .build();
    }

}
