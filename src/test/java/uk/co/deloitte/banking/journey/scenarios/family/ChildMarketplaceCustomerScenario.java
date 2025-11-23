package uk.co.deloitte.banking.journey.scenarios.family;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantRegisterDeviceRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantValidateResetPasswordRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentFile1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUsers;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBEIDStatus;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEIDStatus1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.authorization.OBCustomerAuthorization1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBFatcaForm1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatca1;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.OBWriteFatcaDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationAddress1;
import uk.co.deloitte.banking.customer.api.customer.model.location.OBLocationDetails1;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.REGISTRATION_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomString;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildMarketplaceCustomerScenario extends AdultOnBoardingBase {

    @Inject
    private RelationshipApi relationshipApi;

    static String dependantId;
    static String relationshipId;
    static String otpCode;


    static String childFullName;
    @Inject
    AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private static AlphaTestUser childAlphaTestUser = new AlphaTestUser();

    private final static String TEMPORARY_PASSWORD = "validtestpassword";


    @Test
    @Order(1)
    void marketplace_customer_setup_success_test() {
        this.marketplace_customer_setup_success(false);
    }

    @Test
    @Order(2)
    void reauthenticate_test() {
        this.reauthenticate(ACCOUNT_SCOPE);
    }

    @Test
    @Order(3)
    void generate_customer_cif_test() {
        this.generate_customer_cif();
    }

    @Test
    @Order(4)
    void dump() {
        AlphaTestUsers.builder().alphaTestUsers(List.of(this.alphaTestUser))
                .build()
                .writeToFile();
        DONE();
    }

    @Test
    @Order(5)
    void create_parent_account_test() {
        this.create_account();
    }

    @Test
    @Order(6)
    void verify_parent_eid_status_test() {
        TEST("AHBDB-8292 - Set EID status");
        GIVEN("Customer exists");
        assertNotNull(alphaTestUser.getLoginResponse());

        WHEN("The customer receives the card and client wants to mark it as validated");
        OBWriteEIDStatus1 build = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();
        OBWriteCustomerResponse1 obWriteCustomerResponse1 = customerApiV2.updateCustomerValidations(alphaTestUser,
                build);
        THEN("Status 200 is returned");
        assertNotNull(obWriteCustomerResponse1);
        AND("EID status is set to VALID");
        OBReadCustomer1 currentCustomer = customerApiV2.getCurrentCustomer(alphaTestUser);
        assertEquals(OBEIDStatus.VALID, currentCustomer.getData().getCustomer().get(0).getEidStatus());
    }

    @Test
    @Order(10)
    void create_user_relationship_test_success() {
        TEST("AHBDB-6178-Create child in Forgerock");
        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword(TEMPORARY_PASSWORD).build();

        WHEN("Calling post relationship from authenticate api with a temporary password");
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        THEN("Status code 200 is returned");
        assertNotNull(response);
        AND("The userId of the newly created user is returned");
        assertNotNull(response.getUserId());

        dependantId = response.getUserId();

        NOTE("Dependant id " + dependantId);

        DONE();
    }

    @Test
    @Order(11)
    void create_dependant_customer_and_relationship() {
        TEST("AHBDB-6177 , AHBDB-6180 - Create dependant and relationship");

        String fullName = "ete" + generateRandomString(5) + " " + generateRandomString(5);
        childFullName = fullName;
        NOTE("Child Name: " + fullName);
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .fullName(fullName)
                        .gender(OBGender.FEMALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.MOTHER)
                        .dependantRole(OBRelationshipRole.DAUGHTER)
                        .build())
                .build();

        WHEN("Calling post to create dependant and relationship");
        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        THEN("Status 201(CREATED) is returned");
        assertNotNull(response);
        AND("Relationship contains onboarded by");
        assertEquals(alphaTestUser.getUserId(), response.getData().getRelationships().get(0).getOnboardedBy());
        relationshipId = response.getData().getRelationships().get(0).getConnectionId().toString();
    }

    @Test
    @Order(12)
    void request_to_ename_checker() {
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerCountry, CustomerDOB and Gender");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(childFullName)
                .country("AE")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("F")
                .build();
        CustomerBlacklistResponseDTO response = sanctionsApi.checkBlacklistedChild(alphaTestUser, customerBlacklistRequestDTO, relationshipId);
        if (response.getResult().equals("HIT")) {
            assertTrue(response.getDetectionId().matches("\\d+"));
            assertEquals("3050", response.getReturnCode());
        } else {
            assertEquals("0000", response.getReturnCode());
        }
        assertTrue(response.getTimestamp().toString().matches("(\\d{4})(-)(\\d{2})(-)(\\d{2})(T)(\\d{2})(:)(\\d{2})(:)(\\d{2})(Z)"));
        assertTrue(response.getReferenceNumber().matches("(\\w{8})(-)(\\w{4})(-)(\\w{4})(-)(\\w{4})(-)(\\w{12})"));
        THEN("The platform will respond with the result with the status code of 200");
        AND("The platform will trigger an event with the result");
    }

    @Test
    @Order(13)
    void get_relationships() {
        TEST("AHBDB-6180 - Get customer relationships");

        WHEN("A customer want to see his/her relationships");
        OBReadRelationship1 relationships = this.relationshipApi.getRelationships(alphaTestUser);
        THEN("Status 200(OK) is returned");
        assertNotNull(relationships);
        AND("Relationships contains one relationship");
        assertEquals(1, relationships.getData().getRelationships().size());
        assertEquals(OBGender.MALE, relationships.getData().getGender());
        assertEquals(alphaTestUser.getUserId(), relationships.getData().getRelationships().get(0).getOnboardedBy());
    }

    @Test
    @Order(14)
    void get_child_profile_based_on_relationship() {
        TEST("AHBDB-6988 - Get child profile based on relationship");

        WHEN("A parent wants to get the child profile");
        OBReadCustomer1 relatioships = this.relationshipApi.getChildBasedOnRelationship(alphaTestUser, relationshipId);
        THEN("Status 200(OK) is returned");
        assertNotNull(relatioships);
        AND("Relationships contains  child user id");
        assertEquals(dependantId, relatioships.getData().getCustomer().get(0).getCustomerId().toString());
    }

    @Test
    @Order(15)
    void send_child_otp() {
        TEST("AHDB-6179 - OTP Generation from Parent’s Device ");
        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUser, 204, relationshipId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);

        assertNotNull(otpCO);

        otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        assertNotNull(otpCode);
    }

    @Test
    @Order(16)
    void register_child_device() {
        TEST("AHDB-6179 - AC8 Initial registration");
        WHEN("A child want to generate an OTP for the child");
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(TEMPORARY_PASSWORD)
                .otp(otpCode)
                .build();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(childAlphaTestUser, request);

        THEN("Status code 201 is returned");
        assertNotNull(userLoginResponseV2);
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(childAlphaTestUser, userLoginResponseV2);

    }


    @Test
    @Order(17)
    void certificate_upload_test() {
        childAlphaTestUser = alphaTestUserFactory.setupUserCerts(childAlphaTestUser);
        assertNotNull(childAlphaTestUser);
    }


    @Test
    @Order(18)
    void child_reset_password_test() {
        final String oldPassword = childAlphaTestUser.getUserPassword();
        String newPassword = "newvalidpassword";

        this.authenticateApi.patchUser(childAlphaTestUser,
                UpdateUserRequestV1.builder()
                        .userPassword(newPassword)
                        .build());

        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(childAlphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(childAlphaTestUser.getUserId())
                        .password(newPassword)
                        .build(),
                childAlphaTestUser.getDeviceId(), true);

        parseLoginResponse(childAlphaTestUser, userLoginResponse);
        assertNotEquals(oldPassword, newPassword);
        childAlphaTestUser.setUserPassword(newPassword);
        parseLoginResponse(childAlphaTestUser, userLoginResponse);

    }


    @Test
    @Order(19)
    void child_registration_scope_test() {
        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(childAlphaTestUser);
        THEN("Status code 200(OK) is returned");
        AND("Response contains a token");
        AND("Scope of the token is REGISTRATION");

        Assertions.assertEquals(REGISTRATION_SCOPE, loginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, loginResponse);
    }


    @Test
    @Order(20)
    void child_customer_scope_test() {
        this.authenticateApi.patchUser(childAlphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn("CUSTOMER")
                        .build());


        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(childAlphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(childAlphaTestUser.getUserId())
                        .password(childAlphaTestUser.getUserPassword())
                        .build(),
                childAlphaTestUser.getDeviceId(), true);

        Assertions.assertEquals(CUSTOMER_SCOPE, userLoginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, userLoginResponse);
    }

    @Test
    @Order(21)
    void patch_child_success_test() {
        TEST("AHBDB-5218:: AC1 Patch Customer in CRM");

        GIVEN("Customer is created");
        OBWritePartialCustomer1 patchCustomer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .preferredName("Test" + generateRandomString(5))
                        .dateOfBirth(LocalDate.now().minusYears(15))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language("en")
                        .gender(OBGender.MALE)
                        .build())
                .build();

        WHEN("The client calls patch on the customers endpoint");
        final OBWriteCustomerResponse1 patchResponse = this.customerApiV2.patchChildSuccess(alphaTestUser,
                patchCustomer, relationshipId);

        THEN("we will return a 200 response");
        assertNotNull(patchResponse);

        DONE();
    }

    @Test
    @Order(22)
    void create_child_applicant() {
        TEST("AHDB-6990 - Register Applicant from Parent’s Device ");
        WHEN("A parent want to generate an Applicant for the child");
        final TokenHolder childApplicant = idnowApi.createChildApplicant(alphaTestUser, relationshipId,
                CreateApplicantRequest.builder().firstName(childAlphaTestUser.getName())
                        .lastName(childAlphaTestUser.getName()).build());
        THEN("Status 201(CREATED) is returned");
        assertNotNull(childApplicant);
        assertNotNull(childApplicant.getApplicantId());
        assertNotNull(childApplicant.getSdkToken());
        childAlphaTestUser.setApplicantId(childApplicant.getApplicantId());
    }

    @Test
    @Order(23)
    void send_idnow_webhook_callback() {
        TEST("AHBDB - 6990 - Store IDV status and JSON blob");

        GIVEN("IDNow has finished processing the applicant’s ID");
        AND("the verification has been successful");
        Assertions.assertNotNull(childAlphaTestUser.getApplicantId());


        WHEN("the Ident status value “Success” is returned from IDNow");
        var response = idnowApi.setIdNowAnswer(childAlphaTestUser, "SUCCESS");
        THEN("I will trigger an event saying IDV is completed along with the IDNow response");
        assertTrue(response);
        DONE();
    }

    @Test
    @Order(24)
    void retrieve_child_applicant() {
        TEST("AHBDB - 6990 - Get EID information for a child");
        GIVEN("A customer has completed their ID&V process for the child");
        AND("the verification has been successful");
        Assertions.assertNotNull(childAlphaTestUser.getApplicantId());

        WHEN("the client attempts to retrieve the child applicant’s full IDNow result information with a valid " +
                "JWT token");
        ApplicantExtractedDTO response = idnowApi.getChildApplicantResults(alphaTestUser, relationshipId);

        THEN("the platform will return a 200 response");
        AND("the platform will return the JSON related to the user ID/ transaction ID");
        assertNotNull(response);
        DONE();
    }

    @Test
    @Order(25)
    void persist_child_eid_information_to_customer() throws JsonProcessingException {

        TEST("AHBDB - 6990 - Persist child information from IDV");

        GIVEN("A child exists");
        OBReadCustomer1 currentChild = this.relationshipApi.getChildBasedOnRelationship(alphaTestUser, relationshipId);
        AND("The child has completed his/her ID Check and we have all the information");
        ApplicantExtractedDTO applicantResult = idnowApi.getChildApplicantResults(alphaTestUser, relationshipId);

        WHEN("Client attempt to update child profile with those information");

        Map<String, Object> userData = applicantResult.getUserData();

        OBWritePartialCustomer1 obWriteCustomer1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .build())
                .build();
        obWriteCustomer1.getData().setFullName(extractValue(userData, "FullName"));
        obWriteCustomer1.getData().setFirstName(extractValue(userData, "FirstName"));
        obWriteCustomer1.getData().setLastName(extractValue(userData, "LastName"));
        obWriteCustomer1.getData().setNationality(extractValue(userData, "Nationality"));
        obWriteCustomer1.getData().setGender(OBGender.valueOf(extractValue(userData, "Gender").toUpperCase(Locale.ROOT)));

        obWriteCustomer1.getData().setCustomerState(OBCustomerStateV1.IDV_COMPLETED);

        OBWriteCustomerResponse1 obWriteCustomerResponse1 = this.customerApi.updateCustomer(childAlphaTestUser,
                obWriteCustomer1, 200);
        AND("Expect information to be persisted");

        currentChild = customerApi.getCurrentCustomer(childAlphaTestUser);

        assertEquals(extractValue(userData, "FullName"), currentChild.getData().getCustomer().get(0).getFullName());
        DONE();
    }

    @Test
    @Order(26)
    void create_idv_details_for_child() {
        TEST("AHBDB-6991 - AC2 Customer has completed IDV for child - 200 response");

        GIVEN("The customer provided the ");
        WHEN("Customer enters his child idv details");
        OBWriteIdvDetailsResponse1 customerIdvDetails = relationshipApi.createChildIdvDetails(alphaTestUser, relationshipId);
        THEN("Employment idv are persisted for the customer's child");
        assertNotNull(customerIdvDetails);

        DONE();
    }

    @Test
    @Order(27)
    void get_idv_details_for_child() {
        TEST("AHBDB-7791: Create and get endpoint for IDV details for the child");
        TEST("AC3 Get IDV details - 200 success response");
        GIVEN("a customer exists with IDV details");
        WHEN("the client attempts to retrieve the child's IDV information with a valid JWT token");

        var result = relationshipApi.getIdvDetailsChild(alphaTestUser, relationshipId);
        THEN("the platform will return a 200 response");

        AND("the platform will return the customer’s IDV information");
        assertNotNull(result.getData());
    }

    @Test
    @Order(28)
    void child_customer_crs_test() {

        alphaTestUserBankingCustomerFactory.setupEmploymentChild(childAlphaTestUser);
    }

    @Test
    @Order(29)
    void create_fatca_details_for_child() {
        TEST("AHBDB-4848: Create and get endpoint for FATCA declaration in CRM");
        TEST("AC1 Store FATCA details - 201 response");
        GIVEN("the customer has provided their FATCA details");
        var fatca = OBWriteFatca1.builder().data(OBWriteFatcaDetails1.builder()
                        .usCitizenOrResident(true)
                        .ssn("123456789")
                        .form(OBFatcaForm1.W8)
                        .federalTaxClassification("S Corporation")
                        .build())
                .build();

        WHEN("the client updates the customer profile with valid FATCA details");
        var result = this.fatcaApiV2.createFatcaDetailsChild(alphaTestUser, fatca, relationshipId);
        THEN("we will return a 201 response");
        assertNotNull(result);
        DONE();
    }

    @Test
    @Order(30)
    void get_fatca_details_for_child() {
        TEST("AHBDB-4848: Create and get endpoint for FATCA declaration");
        TEST("AC3 Get FATCA details - 200 success response");
        GIVEN("a customer exists with FATCA input fields");
        WHEN("the client attempts to retrieve the applicant’s FATCA information with a valid JWT token");

        var result = this.fatcaApiV2.getFatcaDetailsChild(alphaTestUser, relationshipId);
        THEN("the platform will return a 200 response");

        AND("the platform will return the customer’s FATCA information");
        var data = result.getData();
        assertEquals("123456789", data.getSsn());
        assertEquals(OBFatcaForm1.W8, data.getForm());
        assertEquals("S Corporation", data.getFederalTaxClassification());
        assertTrue(data.getUsCitizenOrResident());

        DONE();
    }

    @Test
    @Order(31)
    void create_location_details_for_child() {
        TEST("AHBDB-4846: Create and get endpoint for Location declaration");
        TEST("AC1 Create address - 201 response");
        GIVEN("the customer has provided their Location details");
        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("WorkTest")
                .address(OBLocationAddress1.builder()
                        .streetName("Al Saada Street")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 63111")
                        .build())
                .build();

        WHEN("the client updates the customer profile with valid Location details");
        var result = this.locationsApi.createLocationDetailsChild(alphaTestUser, location, relationshipId);
        THEN("we will return a 201 response");
        assertNotNull(result);
        assertNotNull(result.getData().get(0).getId());

        DONE();
    }

    @Test
    @Order(32)
    void update_location_details_for_child() {
        TEST("AHBDB-4846: Create and get endpoint for Location declaration");
        TEST("AC4 Get addresses - 200 success response");
        TEST("AC2 Put additional address - 200 response");
        GIVEN("the customer has provided their Location details");

        AND("the client retrieves the customer profile with valid Location details");
        var getResult = this.locationsApi.getLocationsDetailsChild(alphaTestUser, relationshipId);
        AND("we will return a 200 response");
        assertNotNull(getResult);
        assertNotNull(getResult.getData().get(0).getId());

        OBLocationDetails1 updatedLocation = OBLocationDetails1.builder()
                .id(getResult.getData().get(0).getId())
                .name("Work")
                .address(OBLocationAddress1.builder()
                        .department("CoolDepartment")
                        .subDepartment("CoolSubdepartment")
                        .streetName("CoolStreet")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 55555")
                        .build())
                .build();
        DONE();

        WHEN("the client updates the customer profile with valid Location details");
        var updateResult = this.locationsApi.updateLocationDetailsChild(alphaTestUser, updatedLocation, relationshipId);
        THEN("we will return a 200 response");
        assertNotNull(updateResult);
        assertEquals(getResult.getData().get(0).getId(), updateResult.getData().get(0).getId());
    }

    @Test
    @Order(33)
    void delete_location_details_for_child() {
        TEST("AHBDB-4846: Create and get endpoint for Location declaration");
        TEST("AC5 Delete address - 200 response");
        GIVEN("the customer has provided their Location details");

        OBLocationDetails1 location = OBLocationDetails1.builder()
                .name("WorkTest")
                .address(OBLocationAddress1.builder()
                        .streetName("Al Saada Street")
                        .townName("Dubai")
                        .countrySubDivision("Abu Dhabi")
                        .country("AE")
                        .postalCode("P.O. Box 63111")
                        .build())
                .build();

        WHEN("the client updates the customer profile with valid Location details");
        var result = this.locationsApi.createLocationDetailsChild(alphaTestUser, location, relationshipId);

        AND("the client retrieves the customer profile with valid Location details");
        var getResult = this.locationsApi.getLocationsDetailsChild(alphaTestUser, relationshipId);
        AND("we will return a 200 response");
        assertNotNull(getResult);
        assertNotNull(getResult.getData().get(0).getId());
        assertEquals("AE", getResult.getData().get(0).getAddress().getCountry());


        WHEN("the client deletes the Location details");
        var deleteResult = this.locationsApi.deleteLocationDetailsChild(alphaTestUser, getResult.getData().get(0).getId(), relationshipId);

        THEN("we will return a 200 response");
        assertNotNull(deleteResult);
    }

    @Test
    @Order(34)
    void child_customer_details_test() {

        //TODO:: NOT ALL THIS INFO SHOULD BE SENT
        //update customer information
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName(generateRandomString(5))
                        .lastName(generateRandomString(10))
                        .preferredName("Test" + generateRandomString(5))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language("en")
                        .cityOfBirth("Dubai")
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .gender(alphaTestUser.getGender())
                        .nationality("AE") //TODO:: Fails when not set
                        .address(OBPartialPostalAddress6.builder()
                                .addressLine(List.of(generateRandomString(10),
                                        generateRandomString(5)))
                                .buildingNumber("101")
                                .country("AE")
                                .countrySubDivision("Dubai")
                                .postalCode("123456") //TODO::Remove
                                .build())
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .build())
                .build();

        OBWriteCustomerResponse1 obWriteCustomerResponse1 = this.customerApi.updateCustomer(childAlphaTestUser,
                customer,
                200);
    }


    @Test
    @Order(35)
    void send_child_otp_for_registering_new_device() {

        TEST("AHDB-6179 - OTP Generation from Parent's Device ");
        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUser, 204, relationshipId);

        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);

        assertNotNull(otpCO);
        otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        assertNotNull(otpCode);
    }

    @Test
    @Order(36)
    void child_register_new_device() {

        TEST("AHDB-6179 - AC8 Initial registration");
        WHEN("A child want to generate an OTP for the child");
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(childAlphaTestUser.getUserPassword())
                .otp(otpCode)
                .build();

        childAlphaTestUser.setPreviousDeviceId(childAlphaTestUser.getDeviceId());
        childAlphaTestUser.setDeviceId(UUID.randomUUID().toString());
        childAlphaTestUser.setPreviousPrivateKeyBase64(childAlphaTestUser.getPrivateKeyBase64());
        childAlphaTestUser.setPreviousPublicKeyBase64(childAlphaTestUser.getPublicKeyBase64());

        AsymmetricCipherKeyPair asymmetricCipherKeyPair = alphaKeyService.generateEcKeyPair();
        childAlphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(asymmetricCipherKeyPair));
        childAlphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(asymmetricCipherKeyPair));

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(childAlphaTestUser, request);

        THEN("Status code 201 is returned");
        assertNotNull(userLoginResponseV2);
        assertEquals("device", userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getUserId());
        assertNotNull(userLoginResponseV2.getScope());
        assertNotNull(userLoginResponseV2.getAccessToken());

        parseLoginResponse(childAlphaTestUser, userLoginResponseV2);

        this.certificateApi.uploadCertificate(childAlphaTestUser);

        assertNotNull(childAlphaTestUser);
    }


    @Test
    @Order(37)
    void child_login_old_deviceId_test() {

        THEN("Status code 401 is returned");
        await().atMost(3, TimeUnit.SECONDS).pollDelay(1, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            OBErrorResponse1 loginResponse = this.authenticateApi.loginUserError(childAlphaTestUser,
                    UserLoginRequestV2.builder()
                            .password(childAlphaTestUser.getUserPassword())
                            .userId(childAlphaTestUser.getUserId())
                            .build(), childAlphaTestUser.getPreviousDeviceId(), false);
            Assertions.assertEquals("UAE.ERROR.UNAUTHORIZED", loginResponse.getCode());
        });
    }

    @Test
    @Order(38)
    void child_login_new_deviceId_test() {
        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(childAlphaTestUser);
        THEN("Status code 401 is returned");
        Assertions.assertEquals(ACCOUNT_SCOPE, loginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, loginResponse);
    }


    @Test
    @Order(39)
    void get_customer_authz_test() {

        TEST("AHBDB-229 - Retrieve customer cif and auths");
        GIVEN("A customer already exists and has generated CIF ");

        WHEN("Client queries for the customers authorizations");

        OBCustomerAuthorization1 customerAuthz = customerApi.getCustomerAuthz(childAlphaTestUser);
        THEN("The customer authz properties are returned");
        assertNotNull(customerAuthz);

    }


    @Test
    @Order(40)
    void upload_doc_for_child() throws JsonProcessingException {

        TEST("AHBDB-6996 - Upload child's birth certificate ");
        WHEN("A parent wants to upload a doc for a child");
        final DocumentFile1 uploadedDoc = documentRelationshipApi.uploadDocument(alphaTestUser, "BIRTH_CERTIFICATE", relationshipId, 201);

        THEN("Status 201(CREATED) is returned");
        assertNotNull(uploadedDoc);
    }


    @Test
    @Order(41)
    void forgot_passcode_child_success_test() {
        TEST("AHBDB-7818:: AC1 Reset child passcode");

        String newPassword = UUID.randomUUID().toString();

        GIVEN("Request to reset password is created");
        UserRelationshipWriteRequest resetPasscodeReq = UserRelationshipWriteRequest.builder()
                .tempPassword(newPassword)
                .build();

        WHEN("The client calls put to reset child passcode");
        this.authenticateApi.resetChildPasscode(alphaTestUser, resetPasscodeReq, relationshipId);


        WHEN("A child want to generate an OTP for the child");
        otpApi.sentChildOTPCode(alphaTestUser, 204, relationshipId);
        THEN("The client receives a 204 response code");
        WHEN("Dev calls dev-sim to obtain the otp from the cache");
        NOTE("This simulates when the user receives the OTP on their phone");
        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);

        assertNotNull(otpCO);

        otpCode = otpCO.getPassword();
        THEN("Dev-Sim returns a OTP code " + otpCode);
        assertNotNull(otpCode);

        GIVEN("Request to validate password is created");
        DependantValidateResetPasswordRequestV2 validatePasscodeResetReq = DependantValidateResetPasswordRequestV2.builder()
                .otp(otpCode)
                .password(newPassword)
                .userId(childAlphaTestUser.getUserId())
                .build();
        UserLoginResponseV2 validatePasswordResponse = this.authenticateApi.validateChildPasscode(childAlphaTestUser, validatePasscodeResetReq, childAlphaTestUser.getDeviceId());

        THEN("we will return a 200 response");
        assertNotNull(validatePasswordResponse);

        DONE();
    }
}
