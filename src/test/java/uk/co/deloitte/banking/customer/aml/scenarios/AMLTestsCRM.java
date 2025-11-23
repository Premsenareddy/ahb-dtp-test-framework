package uk.co.deloitte.banking.customer.aml.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;

import javax.inject.Inject;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("BuildCycle3")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AMLTestsCRM {


    @Inject
    private CustomerApi customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private SanctionsApi sanctionsApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private static final String REQUEST_VALIDATION = "REQUEST_VALIDATION";
    private static final String TIMESTAMP_REGEX = "(\\d{4})(-)(\\d{2})(-)(\\d{2})(T)(\\d{2})(:)(\\d{2})(:)(\\d{2})(Z)";
    private static final String REFERENCE_NUMBER_REGEX = "(\\w{8})(-)(\\w{4})(-)(\\w{4})(-)(\\w{4})(-)(\\w{12})";

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Test
    void happy_path_request_to_e_name_checker_nohit() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6286: AC1 Positive Test - Happy Path Successful Request to E-name Checker");
        setupTestUser();
        String name = "FHFRKJH RKHKRJ";
        String country = "GE";
        String dateOfBirth = "1935-07-03";
        String gender = "M";
        String expectedResult = "NOHIT";

        GIVEN("A customer has completed IDV");
        WHEN("The client attempts to perform a blacklist check on e-name checker");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(name)
                .country(country)
                .dateOfBirth(LocalDate.parse(dateOfBirth))
                .gender(gender)
                .build();

        CustomerBlacklistResponseDTO response =
                sanctionsApi.checkBlacklistedCustomer(alphaTestUser, customerBlacklistRequestDTO);

        THEN("The platform will return a 200 Response");
        AND("The result will be a NOHIT");

        assertEquals(expectedResult, response.getResult());
        assertEquals("0000", response.getReturnCode());

        assertTrue(response.getTimestamp().toString().matches(TIMESTAMP_REGEX));
        assertTrue(response.getReferenceNumber().matches(REFERENCE_NUMBER_REGEX));

        DONE();
    }

    @Test
    void happy_path_request_to_e_name_checker_hit() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6285: AC1 Positive Test - Happy Path Successful Request to E-name Checker");
        setupTestUser();
        String name = "JAVED AKHTAR";
        String country = "IN";
        String dateOfBirth = "1989-11-09";
        String gender = "F";
        String expectedResult = "HIT";

        GIVEN("A customer has completed IDV");
        WHEN("The client attempts to perform a blacklist check on e-name checker");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(name)
                .country(country)
                .dateOfBirth(LocalDate.parse(dateOfBirth))
                .gender(gender)
                .build();

        CustomerBlacklistResponseDTO response =
                sanctionsApi.checkBlacklistedCustomer(alphaTestUser, customerBlacklistRequestDTO);

        THEN("The platform will return a 200 Response");
        AND("The result will be a HIT");

        assertEquals(expectedResult, response.getResult());
        assertTrue(response.getDetectionId().matches("\\d+"));
        assertEquals("3050", response.getReturnCode());

        assertTrue(response.getTimestamp().toString().matches(TIMESTAMP_REGEX));
        assertTrue(response.getReferenceNumber().matches(REFERENCE_NUMBER_REGEX));

        DONE();
    }

    @Test
    void request_to_ename_checker_error_empty_country() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6288: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerDOB and Gender, but with CustomerCountry left empty");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .country("")
                .dateOfBirth(LocalDate.of(1989,11,9))
                .gender("M")
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    void request_to_ename_checker_error_empty_name() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6287: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerCountry, CustomerDOB and Gender, but with CustomerName left empty");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("")
                .country("IN")
                .dateOfBirth(LocalDate.of(1989,11,9))
                .gender("Z")
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    void request_to_ename_checker_error_empty_gender() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6290: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerDOB and CustomerCountry, but with Gender left empty");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .country("IN")
                .dateOfBirth(LocalDate.of(1989,11,9))
                .gender("")
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    void request_to_ename_checker_error_empty_dob() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6289: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerCountry and Gender, but with Date of Birth left empty");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .country("IN")
                .dateOfBirth(null)
                .gender("Z")
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    void request_to_ename_checker_error_missing_country() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6291: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerDOB and Gender, but with CustomerCountry missing");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .dateOfBirth(LocalDate.of(1989,11,9))
                .gender("Z")
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    void request_to_ename_checker_error_missing_name() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6291: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerDOB, CustomerCountry and Gender but with CustomerName missing");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .country("IN")
                .dateOfBirth(LocalDate.of(1989,11,9))
                .gender("Z")
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    void request_to_ename_checker_error_missing_gender() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6291: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerCountry and CustomerDOB, but with gender missing");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .country("IN")
                .dateOfBirth(LocalDate.of(1989,11,9))
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    void request_to_ename_checker_error_missing_dob() {
        TEST("AHBDB-4849: Retrieve and store E-name checker results in AML resource (customer)");
        TEST("AHBDB-6291: AC3 Negative Test - Missing or invalid data - 400 response");
        setupTestUser();
        GIVEN("A customer has completed ID&V");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields: CustomerName (full), CustomerCountry and Gender, but with Date of Birth missing");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .country("IN")
                .gender("Z")
                .build();
        OBErrorResponse1 response = sanctionsApi.checkBlackListedCustomerError(alphaTestUser, customerBlacklistRequestDTO);
        THEN("The platform will respond with the result with the status code of 400");
        assertEquals(REQUEST_VALIDATION, response.getCode());
        DONE();
    }

    @Test
    public void exception_error_for_ename_checker() {
        TEST("AHBDB-8524: [API][e-name] Exception while e name check for user");
        setupTestUser();
        GIVEN("A customer has completed IDV");
        WHEN("The client attempts to perform a blacklist check on E-name checker with the mandatory fields completed");
        CustomerBlacklistRequestDTO requestBody = CustomerBlacklistRequestDTO.builder()
                .fullName("Benz Mathew Philip")
                .country("IN")
                .dateOfBirth(LocalDate.parse("1989-03-30"))
                .gender("M")
                .build();
        CustomerBlacklistResponseDTO response = sanctionsApi.checkBlacklistedCustomer(this.alphaTestUser, requestBody);
        Assertions.assertNotNull(response);
        THEN("The platform will respond with the result with the status code of 200");
    }
}

