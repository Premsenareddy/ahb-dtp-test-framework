package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventV1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseTypeEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ProcessOriginEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ReasonEnum;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.aml.SanctionsApi;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.cases.api.CasesApi;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;

@Tag("BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChildNameScreening {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CasesApi casesApi;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private SanctionsApi sanctionsApi;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUserChild;

    private String childId;
    private String connectionId;
    private String fullName;

    private String TEMPORARY_PASSWORD = "validtestpassword";

    void setupTestUser() {
        if (this.alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
            alphaTestUserChild = new AlphaTestUser();

            fullName = generateEnglishRandomString(10);

            alphaTestUser = this.alphaTestUserFactory.setupCustomer(alphaTestUser);

            childId = this.alphaTestUserFactory.createChildInForgerock(alphaTestUser, TEMPORARY_PASSWORD);
            OBWriteDependant1 obWriteDependant1 = this.alphaTestUserFactory.generateDependantBody(childId,
                    15, fullName, OBGender.MALE, OBRelationshipRole.FATHER);

            connectionId = this.alphaTestUserFactory.createChildInCRM(alphaTestUser, obWriteDependant1);
            alphaTestUserChild = this.alphaTestUserFactory.createChildCustomer(alphaTestUser, alphaTestUserChild, connectionId, childId);
        }
    }

    @Test
    public void happy_path_parent_requests_name_screening() {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10978: AC1, AC3 - Parent requests name screening and e-name checker is run");
        setupTestUser();

        GIVEN("A parent has completed IDV for a child");
        AND("The connectionId between parent and child exists in the list of relationships for the parent");
        AND("The platform has verified that the connectionId exists");
        AND("The platform has extracted the child's userID following the verification");

        CreateApplicantRequest applicantRequest = CreateApplicantRequest.builder()
                .firstName("js" + generateEnglishRandomString(10))
                .lastName("js" + generateEnglishRandomString(10))
                .build();

        TokenHolder childApplicant =
                this.idNowApi.createChildApplicant(alphaTestUser, connectionId, applicantRequest);

        alphaTestUserChild.setApplicantId(childApplicant.getApplicantId());

        WHEN("The platform sends a request to the e-name checker to run the check on the child");
        THEN("The platform will receive the results from the e-name checker");
        AND("The platform will respond with a 200 Response");
        AND("The platform will trigger an event");

        String fullName = this.alphaTestUserChild.getName();

        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(fullName)
                .country("AE")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("F")
                .build();

        CustomerBlacklistResponseDTO response =
                sanctionsApi.checkBlacklistedChild(alphaTestUser, customerBlacklistRequestDTO, connectionId);
        assertNotNull(response);

        this.customerApiV2.getCurrentCustomer(alphaTestUserChild);
        DONE();
    }

    @Test
    public void negative_test_run_e_name_check_on_child_using_invalid_connectionId() {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10979: AC2 - Relationship not verified - 403 Forbidden");
        setupTestUser();
        GIVEN("The connectionId does not exist in the the relationship list of the parent");
        WHEN("Experience attempts to initiate the blacklist check on E-name checker process with the connectionID and the mandatory fields");

        THEN("The connectionId will not appear in the list of relationships for the parent");
        AND("The platform will be unsuccessful in verifying the relationship");
        AND("The platform will return a 403 Forbidden");
        String fullName = this.alphaTestUserChild.getName();
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(fullName)
                .country("AE")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("F")
                .build();

        String randomRelationshipId = UUID.randomUUID().toString();

        OBErrorResponse1 error = this.sanctionsApi.checkBlacklistedChildError(alphaTestUser,
                customerBlacklistRequestDTO, randomRelationshipId, 403);
        Assertions.assertNotNull(error);
        DONE();
    }

    @Test
    public void create_case_if_e_name_checker_returns_hit() {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10981: AC5 - Create case if failed");
        setupTestUser();
        GIVEN("A child has failed name screening");
        WHEN("Experience attempts to push an event to create the case on CRM with the relevant case information");
        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName("JAVED AKHTAR")
                .country("IN")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("M")
                .build();

        THEN("A CRM agent will be able to access the case");
        CustomerBlacklistResponseDTO response = this.sanctionsApi.checkBlacklistedChild(alphaTestUser, customerBlacklistRequestDTO, connectionId);

        assertEquals("HIT", response.getResult());

        AND("The parent's ID will have been added to the case under ResponsibleContactId field");

        CaseEventV1 casesBody = this.casesApi.generateCaseBody(alphaTestUserChild.getUserId(), CaseTypeEnum.EXCEPTION,
                "TITLE", ProcessOriginEnum.NAME_SCREENING, ReasonEnum.E_NAME_CHECKER_HIT, "High", alphaTestUser.getUserId());
        this.casesApi.createCaseInCRM(casesBody, 200);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FullName", "Country", "DateOfBirth", "Gender"})
    public void negative_test_request_to_e_name_checker_missing_mandatory_fields(String fieldToRemove) {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10983: Negative Test - Missing mandatory fields");
        setupTestUser();
        GIVEN("The platform attempts to screen the name of a child");
        AND("The request has a missing mandatory field");
        LocalDate dob = LocalDate.now().minusYears(15);

        JSONObject body = new JSONObject() {
            {
                put("FullName", fullName);
                put("Country", "AE");
                put("DateOfBirth", List.of(dob.getYear(), dob.getMonthValue(), dob.getDayOfMonth()));
                put("Gender", "F");
                remove(fieldToRemove);
            }
        };

        WHEN("The platform receives the request to check the name of a customer");

        OBErrorResponse1 error = this.sanctionsApi.checkBlacklistedChildErrorJson(alphaTestUser,
                body, connectionId, 400);
        THEN("The platform will return a 400 Bad Request");
        assertNotNull(error);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {""})
    public void negative_test_invalid_full_name(String invalidFullName) {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10988: Negative Test - Invalid FullName");
        GIVEN("A client wants to run the e-name checker for their child");
        setupTestUser();

        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(invalidFullName)
                .country("IN")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("M")
                .build();

        WHEN("They send a request with an invalid FullName");
        OBErrorResponse1 error =
                this.sanctionsApi.checkBlacklistedChildError(alphaTestUser, customerBlacklistRequestDTO, connectionId, 400);

        THEN("The platform will respond with a 400 Response");
        assertNotNull(error);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {""})
    public void negative_test_invalid_country(String invalidCountry) {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10989: Negative Test - Invalid Country");
        setupTestUser();
        GIVEN("A client wants to run the e-name checker for their child");

        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(generateEnglishRandomString(10))
                .country(invalidCountry)
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender("M")
                .build();
        WHEN("They send a request with an invalid Country");

        OBErrorResponse1 error =
                this.sanctionsApi.checkBlacklistedChildError(alphaTestUser, customerBlacklistRequestDTO, connectionId, 400);

        THEN("The platform will respond with a 400 Response");

        assertNotNull(error);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {""})
    public void negative_test_invalid_date_of_birth(String invalidDateOfBirth) {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10991: Negative Test - Invalid DateOfBirth");
        setupTestUser();
        GIVEN("A client wants to run the e-name checker for their child");

        JSONObject body = new JSONObject() {
            {
                put("FullName", fullName);
                put("Country", "AE");
                put("DateOfBirth", invalidDateOfBirth);
                put("Gender", "F");
            }
        };

        WHEN("They send a request with an invalid DateOfBirth");

        OBErrorResponse1 error = this.sanctionsApi.checkBlacklistedChildErrorJson(alphaTestUser,
                body, connectionId, 400);
        THEN("The platform will respond with a 400 Response");

        assertNotNull(error);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1231", "!@£@£@"})
    public void negative_test_invalid_gender(String invalidGender) {
        TEST("AHBDB-7024: Child's Name Screening");
        TEST("AHBDB-10992: Negative Test - Invalid Gender");
        setupTestUser();
        GIVEN("A client wants to run the e-name checker for their child");

        CustomerBlacklistRequestDTO customerBlacklistRequestDTO = CustomerBlacklistRequestDTO.builder()
                .fullName(generateEnglishRandomString(10))
                .country("IN")
                .dateOfBirth(LocalDate.now().minusYears(15))
                .gender(invalidGender)
                .build();
        WHEN("They send a request with an invalid Gender");

        OBErrorResponse1 error =
                this.sanctionsApi.checkBlacklistedChildError(alphaTestUser, customerBlacklistRequestDTO, connectionId, 400);
        THEN("The platform will respond with a 400 Response");

        assertNotNull(error);
        DONE();
    }
}
