package uk.co.deloitte.banking.customer.cif.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerType1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.authorization.OBCustomerAuthorization1;
import uk.co.deloitte.banking.customer.cif.CifsApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;

import javax.inject.Inject;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomDateOfBirth;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@Tag("BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CRMGenerateCIFTests {

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;
    private AlphaTestUser alphaTestUser3;
    private AlphaTestUser alphaTestUser4;
    private AlphaTestUser alphaTestUser5;
    private AlphaTestUser alphaTestUser6;
    private AlphaTestUser alphaTestUser7;
    private AlphaTestUser alphaTestUser8;
    private AlphaTestUser alphaTestUser9;
    private AlphaTestUser alphaTestUser10;
    private AlphaTestUser alphaTestUser11;
    private AlphaTestUser alphaTestUser12;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private CifsApi cifsApi;

    private void setupTestUser() {
        NOTE("Registering and creating a customer");
        AlphaTestUser alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
    }

    private AlphaTestUser setupTestUser(AlphaTestUser alphaTestUserToSetUp) {
        NOTE("Registering and creating a customer");
        alphaTestUserToSetUp = new AlphaTestUser();

        alphaTestUserToSetUp = alphaTestUserFactory.setupCustomer(alphaTestUserToSetUp);
        return alphaTestUserToSetUp;
    }

    private AlphaTestUser setupTestUser(String phone, AlphaTestUser alphaTestUserToSetUp) {
        NOTE("Registering and creating a customer");

        alphaTestUserToSetUp = new AlphaTestUser();
        alphaTestUserToSetUp.setUserTelephone(phone);

        alphaTestUserToSetUp = alphaTestUserFactory.setupCustomer(alphaTestUserToSetUp);
        return alphaTestUserToSetUp;
    }

    @Test
    public void test_get_customer_authz() {
        TEST("AHBDB-4843: CIF Library Update");
        GIVEN("A customer already exists and has generated a CIF");
        setupTestUser();
        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);

        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
        this.customerApi.putCustomerCif(this.alphaTestUser);

        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        WHEN("The client queries for the customer's authorizations");
        OBCustomerAuthorization1 customerAuthz = this.customerApi.getCustomerAuthz(this.alphaTestUser);

        THEN("The customer authz properties are returned");
        Assertions.assertNotNull(customerAuthz);
        AND("CIF is populated");
        Assertions.assertNotNull(customerAuthz.getData().getCif());

        DONE();
    }

    @Test
    public void happy_path_put_customer_CIF_200_OK() {
        TEST("AHBDB-5406: AC1 Put Customer CIF - 200 OK");
        TEST("AHBDB-6332: AC1 Positive Test - Happy Path Scenario - Put Customer CIF - 200 OK");
        setupTestUser();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV complete");
        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);

        WHEN("The client attempts to change the customer to a banking user");
        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
        OBReadCustomer1 response = this.customerApi.putCustomerCif(this.alphaTestUser);

        THEN("The platform will return a 200");

        AND("The response will contain the customer record with the CIF number generated");
        AND("CustomerType = Banking");
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);
    }

    @Test
    public void happy_path_CustomerType_already_Banking() {
        TEST("AHBDB-5406: AC2 CustomerType already Banking");
        TEST("AHBDB-6333: AC2 Positive Test - Happy Path Scenario - CustomerType already Banking");
        setupTestUser();
        GIVEN("A customer already exists with CustomerType = Banking");
        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);
        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
        String cifFromPhoneNumber = this.alphaTestUser.getUserTelephone().substring(this.alphaTestUser.getUserTelephone().length() - 7);
        OBReadCustomer1 response = this.customerApi.putCustomerCif(this.alphaTestUser);

        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        WHEN("The client attempts to change the customer to a banking user");
        OBReadCustomer1 response2 = this.customerApi.putCustomerCif(this.alphaTestUser);

        THEN("The platform will return the customer record containing the existing CIF number and CustomerType");
        Assertions.assertEquals(OBCustomerType1.BANKING, response2.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");

        Assertions.assertNotNull(response2.getData().getCustomer().get(0).getCif());
    }

    @Test
    public void happy_path_save_CIF_last_7_digits_of_mobile_number() {
        TEST("AHBDB-5406: AC3 Save CIF - last 7 digits of mobile number");
        TEST("AHBDB-6334: AC3 Positive Test - Happy Path Scenario - Save CIF - last 7 digits of mobile number");
        setupTestUser();
        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);

        GIVEN("The platform wants to use the last 7 digits of the customer's mobile number to create the CIF");
        AND("The CIF does not previously exist");
        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
        String cifFromPhoneNumber = this.alphaTestUser.getUserTelephone().substring(this.alphaTestUser.getUserTelephone().length() - 7);

        WHEN("The platform attempts to save the CIF number");
        OBReadCustomer1 response = this.customerApi.putCustomerCif(this.alphaTestUser);

        THEN("They will be able to successfully save it");
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");

        Assertions.assertNotNull(response.getData().getCustomer().get(0).getCif());
    }

    @Test
    public void EID_does_not_exist_404_not_found() {
        TEST("AHBDB-5406: AC8 EID does not exist - 404 Not Found");
        TEST("AHBDB-6336: AC8 Negative Test - EID does not exist - 404 Not Found");

        GIVEN("A customer does not have an existing EID");
        setupTestUser();
        WHEN("The client attempts to change the customer to a banking user");
        OBErrorResponse1 error = this.customerApi.putCustomerCifNegativeFlow(this.alphaTestUser, 404);

        THEN("The platform will return a 404 Not Found");
        Assertions.assertEquals("UAE.ERROR.NOT_FOUND", error.getCode(),
                "Error Code was not as expected, expected was 0004");
        Assertions.assertTrue(error.getMessage().contains("Customer does not have an existing EID"),
                "Error Message not as expected, expected to contain 'IDV details not found for customer'");

        AND("A CIF number will NOT be generated");
        AND("The CustomerType will NOT be updated to Banking");
    }

    @Test
    public void happy_path_save_CIF_ascending_digit_with_6_digit_birthday() {

        envUtils.ignoreTestInEnv("Very long test, run for regression only", Environments.ALL);

        TEST("AHBDB-5406: AC4 Save CIF - Ascending digit (1-9) + 6 digit birthday (DD-MM-YY)");
        TEST("AHBDB-5406: AC5 Try next ascending digit");
        TEST("AHBDB-5406: AC6 Save CIF - Random Number");
        TEST("AHBDB-6335: AC4 Positive Test - Save CIF - Ascending digit (1-9) + 6 digit birthday (DD-MM-YY) \n" +
                "and AC5 Try next ascending digit \n" +
                "and AC6 Save CIF - Random Number");

        GIVEN("The last 7 digits of the customer's mobile number was taken");
        //Customer 1 - will use up the CIF from phone number
        log.info("Creating Customer 1");
        setupTestUser();
        LocalDate randomDoB = generateRandomDateOfBirth();
        String sameDateOfBirth = randomDoB.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String cifFromPhoneNumber =
                this.alphaTestUser.getUserTelephone().substring(this.alphaTestUser.getUserTelephone().length() - 7);

        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .mobileNumber("+55550" + cifFromPhoneNumber)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);
        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);


        OBReadCustomer1 response = this.customerApi.putCustomerCif(this.alphaTestUser);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifFromPhoneNumber, response.getData().getCustomer().get(0).getCif());

        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 2 - AC4 - uses 1 + DOB
        AND("The platform wants to use an ascending digit (1-9) + customer's 6 digit birthday (DD-MM-YY) to create the CIF");
        AND("The platform starts with ascending digit = 1");
        AND("The CIF does not previously exist");
        log.info("Creating Customer 2");

        String cifBasedPhoneNumber = "+55552" + cifFromPhoneNumber;
        this.alphaTestUser2 = setupTestUser(cifBasedPhoneNumber, this.alphaTestUser2);

        String cifNumberDobBased = "1" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser2, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser2);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser2);

        WHEN("The platform attempts to save the CIF number");
        response = this.customerApi.putCustomerCif(this.alphaTestUser2);
        THEN("They will be able to successfully save it");
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 3 - Start of AC5
        log.info("Creating Customer 3");
        AND("The platform wants to use an ascending digit (1-9) + customer's 6 digit birthday to create the CIF");
        AND("The CIF does exist");
        WHEN("The platform attempts to save the CIF number");
        THEN("They will not be able to successfully save it");
        AND("They will need to move on to the next ascending digit");
        cifBasedPhoneNumber = "+55554" + cifFromPhoneNumber;
        this.alphaTestUser3 = setupTestUser(cifBasedPhoneNumber, this.alphaTestUser3);

        cifNumberDobBased = "2" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser3, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser3);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser3);

        response = this.customerApi.putCustomerCif(this.alphaTestUser3);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 4
        log.info("Creating Customer 4");
        cifBasedPhoneNumber = "+55555" + cifFromPhoneNumber;
        this.alphaTestUser4 = setupTestUser(cifBasedPhoneNumber, this.alphaTestUser4);

        cifNumberDobBased = "3" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser4, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser4);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser4);

        response = this.customerApi.putCustomerCif(this.alphaTestUser4);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 5
        log.info("Creating Customer 5");
        cifBasedPhoneNumber = "+55556" + cifFromPhoneNumber;
        this.alphaTestUser5 = setupTestUser(cifBasedPhoneNumber, this.alphaTestUser5);

        cifNumberDobBased = "4" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser5, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser5);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser5);

        response = this.customerApi.putCustomerCif(this.alphaTestUser5);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 6
        log.info("Creating Customer 6");
        cifBasedPhoneNumber = "+55558" + cifFromPhoneNumber;
        this.alphaTestUser6 = setupTestUser(cifBasedPhoneNumber, this.alphaTestUser6);

        cifNumberDobBased = "5" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser6, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser6);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser6);

        response = this.customerApi.putCustomerCif(this.alphaTestUser6);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 7 - which will have cif based on phone number
        log.info("Creating Customer 7");
        this.alphaTestUser7 = setupTestUser(this.alphaTestUser7);
        String cifFromPhoneNumber2 =
                this.alphaTestUser7.getUserTelephone().substring(this.alphaTestUser7.getUserTelephone().length() - 7);
        String cifBasedPhoneNumber2 = "+55550" + cifFromPhoneNumber2;

        this.customerApi.updateCustomer(this.alphaTestUser7, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .mobileNumber(cifBasedPhoneNumber2)
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser7);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser7);

        response = this.customerApi.putCustomerCif(this.alphaTestUser7);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifFromPhoneNumber2, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 8
        log.info("Creating Customer 8");
        cifBasedPhoneNumber2 = "+55552" + cifFromPhoneNumber2;
        this.alphaTestUser8 = setupTestUser(cifBasedPhoneNumber2, this.alphaTestUser8);

        cifNumberDobBased = "6" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser8, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser8);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser8);

        response = this.customerApi.putCustomerCif(this.alphaTestUser8);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 9
        log.info("Creating Customer 9");
        cifBasedPhoneNumber2 = "+55554" + cifFromPhoneNumber2;
        this.alphaTestUser9 = setupTestUser(cifBasedPhoneNumber2, this.alphaTestUser9);

        cifNumberDobBased = "7" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser9, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser9);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser9);

        response = this.customerApi.putCustomerCif(this.alphaTestUser9);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 10
        log.info("Creating Customer 10");
        cifBasedPhoneNumber2 = "+55555" + cifFromPhoneNumber2;
        this.alphaTestUser10 = setupTestUser(cifBasedPhoneNumber2, this.alphaTestUser10);

        cifNumberDobBased = "8" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser10, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser10);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser10);

        response = this.customerApi.putCustomerCif(this.alphaTestUser10);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        //Customer 11
        log.info("Creating Customer 11");
        cifBasedPhoneNumber2 = "+55556" + cifFromPhoneNumber2;
        this.alphaTestUser11 = setupTestUser(cifBasedPhoneNumber2, this.alphaTestUser11);

        cifNumberDobBased = "9" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser11, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser11);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser11);

        response = this.customerApi.putCustomerCif(this.alphaTestUser11);
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertEquals(cifNumberDobBased, response.getData().getCustomer().get(0).getCif());
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);


        //Customer 12 - AC6 - random num; prev customers take up all the CIFs with digit (1-9) + 6 digit birthday
        log.info("Creating Customer 12");
        AND("All the customer's DOB iterations were taken (9)");
        AND("The platform wants to use 7 random digits to create the CIF");
        AND("The CIF does not previously exist");
        cifBasedPhoneNumber2 = "+55558" + cifFromPhoneNumber2;
        this.alphaTestUser12 = setupTestUser(cifBasedPhoneNumber2, this.alphaTestUser12);

        this.customerApi.updateCustomer(this.alphaTestUser12, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser12);
        getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser12);
        WHEN("The platform attempts to save the CIF number");
        response = this.customerApi.putCustomerCif(this.alphaTestUser12);
        THEN("They will be able to successfully save it");
        Assertions.assertEquals(OBCustomerType1.BANKING, response.getData().getCustomer().get(0).getCustomerType(),
                "CustomerType was not as expected, expecting BANKING");
        Assertions.assertFalse(response.getData().getCustomer().get(0).getCif().contains(cifNumberDobBased));
        getCustomerResponse.getData().getCustomer().get(0).setCustomerType(OBCustomerType1.BANKING);

        deleteAllCustomers();
    }

    public void deleteAllCustomers() {

        Field[] fields = CRMGenerateCIFTests.class.getFields();

        for (Field field : fields){
            if (field.getName().contains("alphaTestUser")) {
                try {
                    this.customerApi.deleteCustomer((AlphaTestUser)field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    @Tag("AHBDB-10476")
    @Tag("SmokeTest")
    public void customer_tries_to_generate_CIF_with_0_at_start() {
        TEST("AHBDB-10476: CIF changes to avoid leading 0s - Customer tries to generate CIF with 0 at the start ");

        GIVEN("A customer has met all the requirements to become a banking customer");
        setupTestUser();
        String cifNumber = "0" + randomNumeric(6);
        String phoneNumber = "+55552" + cifNumber;

        this.alphaTestUser = setupTestUser(phoneNumber, this.alphaTestUser);

        this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(generateRandomDateOfBirth())
                        .mobileNumber(phoneNumber)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);


        AND("The customer has populated EID and IDV complete");
        this.customerApi.createCustomerIdvDetails(this.alphaTestUser);

        WHEN("The customer adapter attempts to generate a CIF with a 0 at the beginning");
        this.customerApi.putCustomerCif(this.alphaTestUser);

        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
        Assertions.assertFalse(getCustomerResponse.getData().getCustomer().get(0).getCif().startsWith("0"));

        THEN("The platform will return a 200 and change the CIF Number");
        DONE();
    }

    @Test
    public void delete_customer_api_should_delete_the_cif_for_the_customer_too() {
        envUtils.ignoreTestInEnv("AHBDB-13279", Environments.SIT, Environments.NFT);
        TEST("AHBDB-13279: Delete customer api is not deleting the CIF");

        AlphaTestUser user = new AlphaTestUser();
        user = alphaTestUserFactory.setupCustomer(user);

        customerApi.createCustomerIdvDetails(user);

        customerApi.putCustomerCif(user);

        OBReadCustomer1 getCustomerResponse2 = customerApi.getCurrentCustomer(user);
        assertEquals(OBCustomerType1.BANKING,
                getCustomerResponse2.getData().getCustomer().get(0).getCustomerType());

        GIVEN("The client wants to delete the customer");
        OBCustomer1 customer1 = customerApi.getCurrentCustomer(user).getData().getCustomer().get(0);
        String cif = customer1.getCif();

        OBCustomer1 checkUserExistsWithCif = customerApi.getProfileUsingCIF(cif).getData().getCustomer().get(0);
        assertEquals(user.getUserId(), checkUserExistsWithCif.getCustomerId().toString());

        WHEN("A customer is deleted using the delete customer API");
        customerApi.deleteCustomer(user);

        THEN("Their CIF should also be deleted from the database");
        String userTelephone1 = user.getUserTelephone();

        AlphaTestUser user2 = new AlphaTestUser();
        user2.setUserTelephone(userTelephone1);
        user2 = alphaTestUserFactory.setupCustomer(user2);

        customerApi.createCustomerIdvDetails(user2);

        customerApi.putCustomerCif(user2);

        OBCustomer1 customer = customerApi.getCurrentCustomer(user2).getData().getCustomer().get(0);
        assertEquals(user2.getUserId(), customer.getCustomerId().toString());
        assertEquals(cif, customer.getCif());

        DONE();
    }
}
