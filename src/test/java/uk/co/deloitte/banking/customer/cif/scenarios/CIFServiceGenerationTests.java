package uk.co.deloitte.banking.customer.cif.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBReadIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import uk.co.deloitte.banking.customer.cif.CifsApi;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomDateOfBirth;

@Tag("BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CIFServiceGenerationTests {

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
    private AlphaTestUser alphaTestUser13;
    private AlphaTestUser alphaTestUserFresh;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private CifsApi cifsApi;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private EnvUtils envUtils;

    private final String CODE_ERROR_0002 = "UAE.ERROR.BAD_REQUEST";
    private final String INVALID_DOCUMENT_NUMBER_MESSAGE = "Invalid document number";
    private final String INVALID_PHONE_NUMBER_MESSAGE = "Invalid phone number";
    private final String phonePrefix = "+555";

    private String emiratesId;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    private void setupTestUser() {
        NOTE("Registering and creating a customer");
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);

            this.customerApi.updateCustomer(this.alphaTestUser, OBWritePartialCustomer1.builder()
                    .data(OBWritePartialCustomer1Data.builder()
                            .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

            this.customerApi.createCustomerIdvDetails(this.alphaTestUser);
            OBReadIdvDetailsResponse1 responseIdv = this.customerApi.getCustomerIdvDetails(alphaTestUser);
            this.emiratesId = responseIdv.getData().getDocumentNumber();

            OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
            this.dateOfBirth = getCustomerResponse.getData().getCustomer().get(0).getDob();
            this.phoneNumber = getCustomerResponse.getData().getCustomer().get(0).getMobileNumber();
        }
    }

    private void setupTestUserFresh() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        this.alphaTestUserFresh = new AlphaTestUser();
        this.alphaTestUserFresh = this.alphaTestUserFactory.setupCustomer(this.alphaTestUserFresh);
    }

    private void setupTestUser13() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser13 == null) {
            this.alphaTestUser13 = new AlphaTestUser();
            this.alphaTestUser13 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser13);
        }
    }

    private AlphaTestUser setupTestUserWithPhone(String phone) {
        AlphaTestUser alphaTestUserToSetUp = new AlphaTestUser();
        alphaTestUserToSetUp.setUserTelephone(phone);

        return alphaTestUserFactory.setupCustomer(alphaTestUserToSetUp);
    }

    @Test
    @Tag("SmokeTest")
    @Order(2)
    public void happy_path_EID_exists_in_CIF_service_generate_new_CIF() {
        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9486: AC2 Positive Test - Happy Path Scenario - EID exists in CIF service - Generate new CIF - 201 OK ");
        setupTestUserFresh();
        GIVEN("A customer’s EID is matched to a CIF in the CIF service");
        this.customerApi.updateCustomer(this.alphaTestUserFresh, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUserFresh);

        OBReadIdvDetailsResponse1 responseIdv = this.customerApi.getCustomerIdvDetails(alphaTestUserFresh);
        String emiratesId = responseIdv.getData().getDocumentNumber();

        AND("The CIF exists in the database with SystemOfOrigin == Legacy");
        String cifNumber = randomNumeric(7).replace("0","1");
        final CifResponse cifResponse = this.cifsApi.submitCifNumber(emiratesId, cifNumber);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse.getCifNumber()));
        Assertions.assertEquals(cifNumber, cifResponse.getCifNumber());

        WHEN("The customer adaptor attempts to generate a CIF with DateOfBirth, PhoneNumber, DocumentNumber");
        final CifResponse cifResponse1 = this.cifsApi.generateCifNumber(alphaTestUserFresh, emiratesId);

        THEN("Then the CIF service will generate a CIF according to the generation rules");
        AND("The CIF service will return a 201 OK");
        AND("The CIF service will return the new CIF ");
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse1.getCifNumber()));

        DONE();
    }

    @Test
    @Tag("SmokeTest")
    @Order(3)
    public void happy_path_EID_exists_in_CIF_service_return_existing_CIF() {
        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9487: AC3 Positive Test - Happy Path Scenario - EID exists in CIF service - Do not generate CIF - 201 OK  ");
        setupTestUserFresh();
        GIVEN("A customer’s EID is matched to a CIF in the CIF service");
        this.customerApi.updateCustomer(this.alphaTestUserFresh, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUserFresh);
        String cifFromPhoneNumber =
                this.alphaTestUserFresh.getUserTelephone().substring(this.alphaTestUserFresh.getUserTelephone().length() - 7);

        OBReadIdvDetailsResponse1 responseIdv = this.customerApi.getCustomerIdvDetails(alphaTestUserFresh);
        String emiratesId = responseIdv.getData().getDocumentNumber();

        AND("The CIF exists in the database with SystemOfOrigin == DTP");
        final CifResponse cifResponse1 = this.cifsApi.generateCifNumber(alphaTestUserFresh, emiratesId);
        Assertions.assertEquals(cifResponse1.getCifNumber(), cifFromPhoneNumber);

        WHEN("The customer adaptor attempts to generate a CIF with DateOfBirth, PhoneNumber, DocumentNumber");
        final CifResponse cifResponse2 = this.cifsApi.generateCifNumber(alphaTestUserFresh, emiratesId);
        THEN("The CIF service will return a 201 OK");
        AND("The CIF service will return the CIF value of the customer who exists with that Emirates ID number");
        Assertions.assertEquals(cifResponse2.getCifNumber(), cifFromPhoneNumber);

        DONE();
    }

    @Test
    @Tag("SmokeTest")
    @Tag("Regression")
    @Order(4)
    public void happy_path_EID_does_not_exist_in_CIF_service_generate_CIF() {
//       TODO :: Long test, run only for regression
        envUtils.ignoreTestInEnv(Environments.ALL);

        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9140: AC1: EID does not exist in CIF service - Generate CIF - 201 OK Ascending digit (1-9) + 6 digit " +
                "birthday (DD-MM-YY) and Try next ascending digit and Save CIF - Random Number");

        GIVEN("The last 7 digits of the customer's mobile number was taken.");
        log.info("Creating Customer 1");
        setupTestUserFresh();
        LocalDate randomDoB = generateRandomDateOfBirth();
        String sameDateOfBirth = randomDoB.format(DateTimeFormatter.ofPattern("ddMMyy"));

        String cifFromPhoneNumber =
                this.alphaTestUserFresh.getUserTelephone().substring(this.alphaTestUserFresh.getUserTelephone().length() - 7);

        this.customerApi.updateCustomer(this.alphaTestUserFresh, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .mobileNumber(phonePrefix + "50" + cifFromPhoneNumber)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUserFresh);
        OBReadIdvDetailsResponse1 responseIdv = this.customerApi.getCustomerIdvDetails(alphaTestUserFresh);
        String emiratesId1 = responseIdv.getData().getDocumentNumber();

        CifResponse cifResponse = this.cifsApi.generateCifNumber(this.alphaTestUserFresh, emiratesId1);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse.getCifNumber()));
        Assertions.assertEquals(cifFromPhoneNumber, cifResponse.getCifNumber());

        // Customer 2
        log.info("Creating Customer 2");
        String cifBasedPhoneNumber = phonePrefix + "52" + cifFromPhoneNumber;
        this.alphaTestUser2 = setupTestUserWithPhone(cifBasedPhoneNumber);

        String cifNumberDobBased = "1" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser2, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser2.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser2);
        OBReadIdvDetailsResponse1 responseIdv2 = this.customerApi.getCustomerIdvDetails(alphaTestUser2);
        String emiratesId2 = responseIdv2.getData().getDocumentNumber();

        CifResponse cifResponse2 = this.cifsApi.generateCifNumber(this.alphaTestUser2, emiratesId2);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse2.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse2.getCifNumber());


        //Customer 3
        log.info("Creating Customer 3");
        cifBasedPhoneNumber = phonePrefix + "54" + cifFromPhoneNumber;
        this.alphaTestUser3 = setupTestUserWithPhone(cifBasedPhoneNumber);

        cifNumberDobBased = "2" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser3, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser3.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser3);
        OBReadIdvDetailsResponse1 responseIdv3 = this.customerApi.getCustomerIdvDetails(alphaTestUser3);
        String emiratesId3 = responseIdv3.getData().getDocumentNumber();

        CifResponse cifResponse3 = this.cifsApi.generateCifNumber(this.alphaTestUser3, emiratesId3);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse3.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse3.getCifNumber());


        //Customer 4
        log.info("Creating Customer 4");
        cifBasedPhoneNumber = phonePrefix + "55" + cifFromPhoneNumber;
        this.alphaTestUser4 = setupTestUserWithPhone(cifBasedPhoneNumber);

        cifNumberDobBased = "3" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser4, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser4.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser4);
        OBReadIdvDetailsResponse1 responseIdv4 = this.customerApi.getCustomerIdvDetails(alphaTestUser4);
        String emiratesId4 = responseIdv4.getData().getDocumentNumber();

        CifResponse cifResponse4 = this.cifsApi.generateCifNumber(this.alphaTestUser4, emiratesId4);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse4.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse4.getCifNumber());


        //Customer 5
        log.info("Creating Customer 5");
        cifBasedPhoneNumber = phonePrefix + "56" + cifFromPhoneNumber;
        this.alphaTestUser5 = setupTestUserWithPhone(cifBasedPhoneNumber);

        cifNumberDobBased = "4" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser5, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser5.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser5);
        OBReadIdvDetailsResponse1 responseIdv5 = this.customerApi.getCustomerIdvDetails(alphaTestUser5);
        String emiratesId5 = responseIdv5.getData().getDocumentNumber();

        CifResponse cifResponse5 = this.cifsApi.generateCifNumber(this.alphaTestUser5, emiratesId5);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse5.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse5.getCifNumber());


        //Customer 6
        log.info("Creating Customer 6");
        cifBasedPhoneNumber = phonePrefix + "58" + cifFromPhoneNumber;
        this.alphaTestUser6 = setupTestUserWithPhone(cifBasedPhoneNumber);

        cifNumberDobBased = "5" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser6, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser6.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser6);
        OBReadIdvDetailsResponse1 responseIdv6 = this.customerApi.getCustomerIdvDetails(alphaTestUser6);
        String emiratesId6 = responseIdv6.getData().getDocumentNumber();

        CifResponse cifResponse6 = this.cifsApi.generateCifNumber(this.alphaTestUser6, emiratesId6);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse6.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse6.getCifNumber());

        //Customer 7 - which will have cif based on phone number
        log.info("Creating Customer 7");
        this.alphaTestUser7 = new AlphaTestUser();
        this.alphaTestUser7 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser7);

        String cifFromPhoneNumber2 =
                this.alphaTestUser7.getUserTelephone().substring(this.alphaTestUser7.getUserTelephone().length() - 7);
        String cifBasedPhoneNumber2 = phonePrefix + "50" + cifFromPhoneNumber2;

        this.customerApi.updateCustomer(this.alphaTestUser7, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .mobileNumber(cifBasedPhoneNumber2)
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser7.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser7);
        OBReadIdvDetailsResponse1 responseIdv7 = this.customerApi.getCustomerIdvDetails(alphaTestUser7);
        String emiratesId7 = responseIdv7.getData().getDocumentNumber();

        CifResponse cifResponse7 = this.cifsApi.generateCifNumber(this.alphaTestUser7, emiratesId7);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse7.getCifNumber()));
        Assertions.assertEquals(cifFromPhoneNumber2, cifResponse7.getCifNumber());


        //Customer 8
        log.info("Creating Customer 8");
        cifBasedPhoneNumber2 = phonePrefix + "52" + cifFromPhoneNumber2;
        this.alphaTestUser8 = setupTestUserWithPhone(cifBasedPhoneNumber2);

        cifNumberDobBased = "6" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser8, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser8.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser8);
        OBReadIdvDetailsResponse1 responseIdv8 = this.customerApi.getCustomerIdvDetails(alphaTestUser8);
        String emiratesId8 = responseIdv8.getData().getDocumentNumber();

        CifResponse cifResponse8 = this.cifsApi.generateCifNumber(this.alphaTestUser8, emiratesId8);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse8.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse8.getCifNumber());

        //Customer 9
        log.info("Creating Customer 9");
        cifBasedPhoneNumber2 = phonePrefix + "54" + cifFromPhoneNumber2;
        this.alphaTestUser9 = setupTestUserWithPhone(cifBasedPhoneNumber2);

        cifNumberDobBased = "7" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser9, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser9.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser9);
        OBReadIdvDetailsResponse1 responseIdv9 = this.customerApi.getCustomerIdvDetails(alphaTestUser9);
        String emiratesId9 = responseIdv9.getData().getDocumentNumber();

        CifResponse cifResponse9 = this.cifsApi.generateCifNumber(this.alphaTestUser9, emiratesId9);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse4.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse9.getCifNumber());


        //Customer 10
        log.info("Creating Customer 10");
        cifBasedPhoneNumber2 = phonePrefix + "55" + cifFromPhoneNumber2;
        this.alphaTestUser10 = setupTestUserWithPhone(cifBasedPhoneNumber2);

        cifNumberDobBased = "8" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser10, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser10.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser10);
        OBReadIdvDetailsResponse1 responseIdv10 = this.customerApi.getCustomerIdvDetails(alphaTestUser10);
        String emiratesId10 = responseIdv10.getData().getDocumentNumber();

        CifResponse cifResponse10 = this.cifsApi.generateCifNumber(this.alphaTestUser10, emiratesId10);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse10.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse10.getCifNumber());


        //Customer 11
        log.info("Creating Customer 11");
        cifBasedPhoneNumber2 = phonePrefix + "56" + cifFromPhoneNumber2;
        this.alphaTestUser11 = setupTestUserWithPhone(cifBasedPhoneNumber2);

        cifNumberDobBased = "9" + sameDateOfBirth;

        this.customerApi.updateCustomer(this.alphaTestUser11, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser11.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser11);
        OBReadIdvDetailsResponse1 responseIdv11 = this.customerApi.getCustomerIdvDetails(alphaTestUser11);
        String emiratesId11 = responseIdv11.getData().getDocumentNumber();

        CifResponse cifResponse11 = this.cifsApi.generateCifNumber(this.alphaTestUser11, emiratesId11);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse11.getCifNumber()));
        Assertions.assertEquals(cifNumberDobBased, cifResponse11.getCifNumber());


        //Customer 12
        log.info("Creating Customer 12");
        cifBasedPhoneNumber2 = phonePrefix + "58" + cifFromPhoneNumber2;
        this.alphaTestUser12 = setupTestUserWithPhone(cifBasedPhoneNumber2);

        this.customerApi.updateCustomer(this.alphaTestUser12, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .dateOfBirth(randomDoB)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build(), 200);
        this.alphaTestUser12.setDateOfBirth(randomDoB);

        this.customerApi.createCustomerIdvDetails(this.alphaTestUser12);
        OBReadIdvDetailsResponse1 responseIdv12 = this.customerApi.getCustomerIdvDetails(alphaTestUser12);
        String emiratesId12 = responseIdv12.getData().getDocumentNumber();

        CifResponse cifResponse12 = this.cifsApi.generateCifNumber(this.alphaTestUser12, emiratesId12);
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse12.getCifNumber()));
        Assertions.assertFalse(cifResponse12.getCifNumber().contains(sameDateOfBirth));

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+5555250020", " +555525002045", "+55550123456", "+5555012345678", "+55550", "+55521",
            "+55570", "+555", "+555501234567273645", "+55550!@#$%^&", "+555511234567", "+555531234567", "+555571234567",
            "+555591234567", "+555301234567", "+555171234567", "+855171234567", "+22521234567 ", "+1123456789", "",
            "+abcdefghijkl", "+27123456789"})
    public void invalid_phone_number_when_generating_cif_400_response(String invalidPhoneNumber) {
        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9488: Negative Test - Invalid Phone Number - 400 response");
        setupTestUser();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV complete");

        WHEN("The customer adaptor attempts to generate a CIF with DateOfBirth, invalid PhoneNumber, DocumentNumber.");

        JSONObject jsonCifObject = this.cifsApi.createCifBodyJson(invalidPhoneNumber, emiratesId, dateOfBirth.toString());

        OBErrorResponse1 response = this.cifsApi.generateCifError(alphaTestUser, jsonCifObject, 400);

        THEN("The platform will return a 400");
        assertEquals(CODE_ERROR_0002, response.getCode(), "Error code not as expected, expected: " + CODE_ERROR_0002);
        assertTrue(response.getMessage().contains(INVALID_PHONE_NUMBER_MESSAGE), "Error Message not as expected, expected: " + INVALID_PHONE_NUMBER_MESSAGE);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12312341234567", "1231234123456712", "abcabcagaggagaa", "!@#$%^&*()_+_)(", ""})
    public void invalid_emirates_id_when_generating_cif_400_response(String invalidEmiratesId) {
        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9490: Negative Test - Invalid Document Number - 400 response");
        setupTestUser();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV complete");
        WHEN("The customer adaptor attempts to generate a CIF with DateOfBirth, PhoneNumber, invalid DocumentNumber.");

        JSONObject jsonCifObject = this.cifsApi.createCifBodyJson(phoneNumber, invalidEmiratesId, dateOfBirth.toString());

        OBErrorResponse1 response = this.cifsApi.generateCifError(alphaTestUser, jsonCifObject, 400);

        THEN("The platform will return a 400");
        assertEquals(CODE_ERROR_0002, response.getCode(), "Error code not as expected, expected: " + CODE_ERROR_0002);
        assertTrue(response.getMessage().contains(INVALID_DOCUMENT_NUMBER_MESSAGE), "Error Message not as expected, expected: " + INVALID_DOCUMENT_NUMBER_MESSAGE);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12312341234567", "abcabcagaggagaa", "!@#$%^&*()_+_)(", "1990-13-01", "990-02-31",
            "2021-02-29", "AA-BB-ABCD", "00-00-0000", "09--05--1990", "-09-05-1990", "90-05-09", "09/05/1990"})
    public void invalid_date_of_birth_when_generating_cif_400_response(String invalidDateOfBirth) {
        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9491: Negative Test - Invalid Date of Birth - 400 response");
        setupTestUser();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV complete");
        WHEN("The customer adaptor attempts to generate a CIF with invalid DateOfBirth, PhoneNumber, DocumentNumber.");
        JSONObject jsonCifObject =
                this.cifsApi.createCifBodyJson("+555524327039", emiratesId, invalidDateOfBirth);

        this.cifsApi.generateCifError(alphaTestUser, jsonCifObject, 400);

        THEN("The platform will return a 400");
        DONE();
    }

    @Test
    public void null_document_number_when_generating_cif_400_response() {
        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9492: Negative Test - Null Document Number - 400 response");
        setupTestUser();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV complete");
        WHEN("The customer adaptor attempts to generate a CIF with DateOfBirth, PhoneNumber, null DocumentNumber.");

        JSONObject jsonCifObject = this.cifsApi.createCifBodyJson(phoneNumber, null, dateOfBirth.toString());

        OBErrorResponse1 response = this.cifsApi.generateCifError(alphaTestUser, jsonCifObject, 400);

        THEN("The platform will return a 400");
        assertEquals(CODE_ERROR_0002, response.getCode(), "Error code not as expected, expected: " + CODE_ERROR_0002);
        assertTrue(response.getMessage().contains(INVALID_DOCUMENT_NUMBER_MESSAGE), "Error Message not as expected, expected: " + INVALID_DOCUMENT_NUMBER_MESSAGE);

        DONE();
    }

    @Test
    @Order(1)
    public void null_phone_number_when_generating_cif_201_response() {
        TEST("AHBDB-4389: CIF Service - CIF generation");
        TEST("AHBDB-9495: Positive Test - Null Phone Number - 201 response");
        setupTestUserFresh();
        GIVEN("A customer has met all the requirements to become a banking customer");
        AND("The customer has a populated EID and IDV complete");

        LocalDate randomDoB = generateRandomDateOfBirth();
        this.customerApi.createCustomerIdvDetails(this.alphaTestUserFresh);
        OBReadIdvDetailsResponse1 responseIdv = this.customerApi.getCustomerIdvDetails(alphaTestUserFresh);
        String emiratesId = responseIdv.getData().getDocumentNumber();

        OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUserFresh);

        WHEN("The customer adaptor attempts to generate a CIF with DateOfBirth, null PhoneNumber, DocumentNumber.");

        JSONObject jsonCifObject = this.cifsApi.createCifBodyJson(null, emiratesId, randomDoB.toString());
        CifResponse cifResponse = this.cifsApi.generateCifNumberJson(alphaTestUserFresh, jsonCifObject);

        THEN("The platform will return a 201");
        String cifNumberFromDob = randomDoB.format(DateTimeFormatter.ofPattern("ddMMyy"));
        assertTrue(cifResponse.getCifNumber().contains(cifNumberFromDob), "Wrong CIF number. Expecting CIF " +
                "number to contain" + cifNumberFromDob);

        setupTestUser13();

        AND("The customer adaptor attempts to generate a CIF with DateOfBirth, null PhoneNumber, DocumentNumber for another customer.");
        this.customerApi.createCustomerIdvDetails(this.alphaTestUser13);
        OBReadIdvDetailsResponse1 responseIdv2 = this.customerApi.getCustomerIdvDetails(alphaTestUser13);
        String emiratesId2 = responseIdv2.getData().getDocumentNumber();

        JSONObject jsonCifObject2 = this.cifsApi.createCifBodyJson(null, emiratesId2, randomDoB.toString());
        this.cifsApi.generateCifNumberJson(alphaTestUser13, jsonCifObject2);

        THEN("The platform will return a 201");
        assertTrue(cifResponse.getCifNumber().contains(cifNumberFromDob), "Wrong CIF number. Expecting CIF " +
                "number to contain" + cifNumberFromDob);

        DONE();
    }
}
