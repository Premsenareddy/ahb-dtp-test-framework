package uk.co.deloitte.banking.customer.idnow.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.idv.*;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdateCustomerEIDAndIDVCRMTests {

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private AuthenticateApiV2 authenticateApiV2;


    private AlphaTestUser alphaTestUser;

    public void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
            alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        }
    }

    public void setupRefreshTestUser() {
        alphaTestUser = new AlphaTestUser();
        alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
    }

    @Test
    public void happy_path_successfully_changed_customer_state() {
        TEST("AHBDB-1887: AC1 Change customer state to IDV completed from IDV review required - 200 response");
        TEST("AHBDB-2713: AC1 Positive Test - Happy Path Successfully Changed customer state to IDV completed from IDV review required");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6262: Positive Test - Happy Path Scenario - AC1 Change customer state to IDV completed from IDV review required - 200 response");
        setupTestUser();
        GIVEN("The IDNow result was 'SUCCESS DATA CHANGED'");
        AND("The customer has accepted that their DOB can be changed according to the EID information");
        OBWritePartialCustomer1 obWriteCustomer1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_REVIEW_REQUIRED).build())
                .build();

        customerApi.updateCustomer(alphaTestUser, obWriteCustomer1, 200);

        WHEN("The client updates the customer profile with the customer status IDV complete");
        OBWritePartialCustomer1 obWriteCustomer2 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build())
                .build();

        customerApi.updateCustomer(alphaTestUser, obWriteCustomer2, 200);

        THEN("The Status Code is 200 OK");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"IDCARD", "RESIDENCE_PERMIT", "PASSPORT", "DRIVER_LICENSE"})
    public void happy_path_customer_has_completed_IDV(String idType) {
        TEST("AHBDB-1887: AC2 Customer has completed IDV - 200 response");
        TEST("AHBDB-2714: AC2 Positive Test - Happy Path Customer has completed IDV");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6260: AC1 Positive Test - Happy Path Scenario - Post ID&V information - 201 Created");
        TEST("AHBDB-6261: AC2 & AC4, AC5 Positive Test - Happy Path Scenario - Patch Customer - 200 Success");
        TEST("AHBDB-4789: Customer IDVS Save call not accepting the given IDType");
        setupRefreshTestUser();
        TokenHolder token = idNowApi.createApplicant(alphaTestUser);
        alphaTestUser.setApplicantId(token.getApplicantId());

        this.idNowApi.setIdNowAnswer(alphaTestUser, "SUCCESS");
        GIVEN("The customer has completed IDV");
        ApplicantExtractedDTO applicantResult = this.idNowApi.getApplicantResult(alphaTestUser);
        var userData = applicantResult.getUserData();
        OBWritePartialCustomer1 obWriteCustomer1 = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .fullName(extractValue(userData, "FullName"))
                        .firstName(extractValue(userData, "FirstName"))
                        .lastName(extractValue(userData, "LastName"))
                        .nationality(extractValue(userData, "Nationality"))
                        .gender(OBGender.valueOf(extractValue(userData, "Gender")))
                        .customerState(OBCustomerStateV1.IDV_COMPLETED).build())
                .build();

        WHEN("The client updates the customer profile with valid fields from the IDNow JSON response");
        customerApi.updateCustomer(alphaTestUser, obWriteCustomer1, 200);

        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("IdType", idType);
        String documentNumber = jObj.getString("DocumentNumber");

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 201);

        THEN("We will return a 201 response");

        OBReadIdvDetailsResponse1 result = this.customerApi.getCustomerIdvDetails(alphaTestUser);
        THEN("the platform will return a 200 response");

        AND("the platform will return the customer’s IDV information");
        var data = result.getData();
        assertEquals(idType, data.getIdType().toString());
        assertEquals(documentNumber, data.getDocumentNumber());

        DONE();
    }

    @Test
    public void customer_record_does_not_exist_404_not_found() {
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6283: AC6 Negative Test - Customer record doesn’t exist - 404");
        setupTestUser();
        alphaTestUser.setApplicantId(RandomDataGenerator.generateRandomString());
        THEN("We will return a 400 response");
        this.idNowApi.setIdNowAnswerNotFound(alphaTestUser, "SUCCESS");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"IDV COMPLETED", "idv_review_failed", "123456789", "!@£$%^&*()"})
    public void invalid_data_field_CustomerState_400_response(String customerState) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3025: AC3 Negative Test Invalid data field - CustomerState - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6274: AC3 Negative Test - Invalid data field - CustomerState - <CustomerState> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONForUpdateCustomer("CustomerState", customerState);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"success", "SUCCESS DATA CHANGED", "1234567", "!@£$%^&*()"})
    public void invalid_data_field_Result_400_response(String result) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3026: AC3 Negative Test Invalid data field - Result - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6263: AC3 Negative Test - Post - Missing or invalid data - Result: <Result> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("Result", result);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"sdfghjhgfdfghjkjhgfdfghjhgfghjhghjghghgggggggggggggggggggggg", "123454345454345654345654567654567654567654567865676567656765", "9999999", "!@£$%^&*()"})
    public void invalid_data_field_TransactionNumber_400_response(String transactionNumber) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3028: AC3 Negative Test Invalid data field - TransactionNumber - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6265: AC3: Negative Test - Post - Missing or invalid data - TransactionNumber: <TransactionNumber> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("TransactionNumber", transactionNumber);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"QK-NBNHZ", "KQK NBNHZ", "!@£-$%^&*"})
    public void invalid_data_field_Ident_ID_400_response(String identID) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3029: AC3 Negative Test Invalid data field - Ident-ID - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-3029: AC3 Negative Test Invalid data field - Ident-ID - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("IdentId", identID);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "!@£$%^&*", "sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb"})
    public void invalid_data_field_FirstName_400_response(String firstName) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3030: AC3 Negative Test Invalid data field - FirstName - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6276: AC3 Negative Test - Invalid data field - FirstName - <FirstName> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONForUpdateCustomer("FirstName", firstName);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "!@£$%^&*", "sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb"})
    public void invalid_data_field_LastName_400_response(String lastName) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3031: AC3 Negative Test Invalid data field - LastName - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6277: AC3 Negative Test - Invalid data field - LastName - <IDVLastName> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONForUpdateCustomer("LastName", lastName);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "!@£$%^&*", "sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgbsdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdg"})
    public void invalid_data_field_FullName_400_response(String fullName) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3032: AC3 Negative Test Invalid data field - FullName - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6278: AC3 Negative Test - Invalid data field - FullName - <IDVFullName> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONForUpdateCustomer("FullName", fullName);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"male", "female", "asdfg", "12344", "!@£$%"})
    public void invalid_data_field_Gender_400_response(String gender) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3033: AC3 Negative Test Invalid data field - Gender - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6279: AC3 Negative Test - Invalid data field - Gender - <IDVGender> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONForUpdateCustomer("Gender", gender);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-13-01", "AA-BB-ABCD", "09/05/1990"})
    public void invalid_data_field_DateOfBirth_400_response(String dateOfBirth) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3034: AC3 Negative Test Invalid data field - DateOfBirth - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6281: AC3 Negative Test - Invalid data field - DateOfBirth - <DateOfBirth> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONForUpdateCustomer("DateOfBirth", dateOfBirth);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"aa", "!@", "12"})
    public void invalid_data_field_Nationality_400_response(String nationality) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3035: AC3 Negative Test Invalid data field - Nationality - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6282: AC3 Negative Test - Invalid data field - Nationality - <IDVNationality> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = customerApi.createJSONForUpdateCustomer("Nationality", nationality);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"idcard", "12344", "!@£$%^"})
    public void invalid_data_field_IdType_400_response(String idType) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3036: AC3 Negative Test Invalid data field - IdType - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6267: AC3: Negative Test - Post - Missing or invalid data - IdNumber: <IdType> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("IdType", idType);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);


        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"xxx-xxxx-xxxxxxx-x", "123-1234-1234567-12", "2345678567876577", "!@£-$%^&*"})
    public void invalid_data_field_DocumentNumber_400_response(String documentNumber) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3037: AC3 Negative Test Invalid data field - DocumentNumber - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6266: AC3: Negative Test - Post - Missing or invalid data - DocumentNumber: <DocumentNumber> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("DocumentNumber", documentNumber);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "1234567890", "abAAababa", "!@£-$%^&*("})
    public void invalid_data_field_IdNumber_400_response(String idNumber) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3038: AC3 Negative Test Invalid data field - IdNumber - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6268: AC3: Negative Test - Post - Missing or invalid data - IdNumber: <IdNumber> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("IdNumber", idNumber);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"gaggagag", "123456", "!@£-$%"})
    public void invalid_data_field_IdCountry_400_response(String idCountry) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3039: AC3 Negative Test Invalid data field - IdCountry - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6269: AC3: Negative Test - Post - Missing or invalid data - IdCountry: <IdCountry> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("IdCountry", idCountry);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2023-13-01", "AA-BB-ABCD", "09/05/2023"})
    public void invalid_data_field_ExpirationDate_400_response(String dateOfExpiry) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3040: AC3 Negative Test Invalid data field - ExpirationDate - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-3040: AC3 Negative Test Invalid data field - ExpirationDate - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("DateOfExpiry", dateOfExpiry);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1996-08-14T20:30:24+04:75", "30-05-2020T20:30:24+04:00", "ANAA-RE-MET20:30:24+04:00"})
    public void invalid_data_field_IdentificationTime_400_response(String identificationTime) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3041: AC3 Negative Test Invalid data field - IdentificationTime - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6270: AC3 Negative Test - Invalid data field - IdentificationTime - <IdentificationTime> - 400 response");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("IdentificationTime", identificationTime);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "1234567890", "2345678567876577", "!@£$%^&*()", "sdfdfvggfhbfgdgvfhgfvhdsfvjdhsvfjdhfvdjhvfdjhvfdjhvdjhvcdjhvcjdhvcjdhvcdjhvcdjhvcdjhvcdfvdvfbxdgfbdgb"})
    public void invalid_data_field_GTCVersion_400_response(String gtcVersion) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3042: AC3 Negative Test Invalid data field - GTCVersion - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6271: AC3: Negative Test - Post - Missing or invalid data - GtcVersion: <GtcVersion> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("GtcVersion", gtcVersion);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"app", "web", "123456", "!@£$%^&"})
    public void invalid_data_field_Type_400_response(String type) throws JSONException {
        TEST("AHBDB-1887: AC3 Invalid data field - 400 response");
        TEST("AHBDB-3043: AC3 Negative Test Invalid data field - Type - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6272: AC3: Negative Test - Post - Missing or invalid data - Type: <Type> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store a IDNow result field");
        AND("The validation does not satisfy the validation details in the data table");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("Type", type);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Result", "TransactionNumber", "IdentId", "IdType", "DocumentNumber",
            "IdNumber", "IdCountry", "DateOfExpiry", "IdentificationTime", "Type"})
    public void missing_mandatory_IDNow_data_field_400_response(String toRemove) throws JSONException {
        TEST("AHBDB-1887: AC4 Missing data field - 400 response");
        TEST("AHBDB-3044: Negative Test Missing Mandatory IDNow data field - 400 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6273: AC3: Negative Test - Post - Missing data - Remove the following mandatory value: <MandatoryValue> - 400 bad request");
        setupTestUser();
        GIVEN("The client wants to store the IDNow result information");
        AND("There are missing mandatory fields");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("Type", String.valueOf(OBAppType.APP));
        jObj.remove(toRemove);

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 400);

        THEN("The Status Code is 400");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS", "SUCCESS_DATA_CHANGED", "REVIEW_PENDING"})
    public void missing_optional_data_field_Reason_201_response(String happyResult) throws JSONException {
        TEST("AHBDB-1887: AC4 Missing data field - 400 response");
        TEST("AHBDB-3078: Negative Test Missing Optional data field - Reason - 200 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-6275: AC3 Negative Test - Missing Conditional data field - Reason - 200 response");
        setupTestUser();
        GIVEN("The client wants to store the IDNow result information");
        AND("There are missing optional fields");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("Result", happyResult);
        jObj.remove("Reason");

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 201);

        THEN("The Status Code is 201");
        DONE();
    }

    @Test
    public void missing_optional_data_field_GTCVersion_201_response() throws JSONException {
        TEST("AHBDB-1887: AC4 Missing data field - 400 response");
        TEST("AHBDB-3079: Negative Test Missing Optional data field - GTCVersion - 200 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-3079: Negative Test Missing Optional data field - GTCVersion - 200 response");
        setupTestUser();
        GIVEN("The client wants to store the IDNow result information");
        AND("There are missing optional fields");
        JSONObject jObj = this.customerApi.createJSONIdvDetails("GtcVersion", "GTC-Version");
        jObj.remove("GtcVersion");

        WHEN("The client updates the customer profile");
        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, jObj, 201);

        THEN("The Status Code is 201");

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FirstName", "LastName", "FullName", "Gender", "Nationality", "DateOfBirth"})
    public void missing_optional_data_field_for_Customer_200_response(String toRemoveFromCustomer) throws JSONException {
        TEST("AHBDB-1887: AC4 Missing data field - 400 response");
        TEST("AHBDB-3227: Negative Test Missing Optional data field for Customer- 200 response");
        TEST("AHBDB-3689: [CRM] API - Post and get ID&V");
        TEST("AHBDB-3227: Negative Test Missing Optional data field for Customer- 200 response");
        setupTestUser();
        GIVEN("The client wants to store the IDNow result information");
        AND("There are missing optional fields");
        JSONObject jObj = this.customerApi.createJSONForUpdateCustomer("Nationality", "AE");
        JSONObject jObj2 = jObj.getJSONObject("Data");
        jObj2.remove(toRemoveFromCustomer);
        jObj.put("Data", jObj2);

        WHEN("The client updates the customer profile");
        this.customerApi.updateCustomerUsingJSON(this.alphaTestUser, jObj, 200);

        THEN("The Status Code is 200");

        DONE();
    }
}
