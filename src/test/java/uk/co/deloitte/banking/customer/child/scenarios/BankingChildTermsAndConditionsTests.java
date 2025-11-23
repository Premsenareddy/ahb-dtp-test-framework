package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBTermType;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteTerm1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.devsim.api.DevelopmentSimulatorService;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.customer.api.customer.model.OBTermType.BANKING;
import static uk.co.deloitte.banking.customer.api.customer.model.OBTermType.PRIVACY_POLICY;

@Tag("BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BankingChildTermsAndConditionsTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private DevelopmentSimulatorService developmentSimulatorService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private OtpApi otpApi;

    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUserChild;
    private AlphaTestUser alphaTestUserParent;

    private String childId;
    private String connectionId;
    private String fullName = "testUser";

    private final String ERROR_CODE_MISSING_FIELD = "REQUEST_VALIDATION";

    private void setupTestUserChildFresh() {

        alphaTestUserParent = new AlphaTestUser();
        alphaTestUserChild = new AlphaTestUser();
        alphaTestUserParent = alphaTestUserFactory.setupCustomer(alphaTestUserParent);
        childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, "validtestpassword");
        connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent,
                alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
        alphaTestUserChild =
                alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, connectionId, childId);
    }

    private void setupTestUserChild() {
        if (this.alphaTestUserParent == null) {
            alphaTestUserParent = new AlphaTestUser();
            alphaTestUserChild = new AlphaTestUser();
            alphaTestUserParent = alphaTestUserFactory.setupCustomer(alphaTestUserParent);
            childId = alphaTestUserFactory.createChildInForgerock(alphaTestUserParent, "validtestpassword");
            connectionId = alphaTestUserFactory.createChildInCRM(alphaTestUserParent,
                    alphaTestUserFactory.generateDependantBody(childId, 15, fullName, OBGender.MALE, OBRelationshipRole.FATHER));
            alphaTestUserChild =
                    alphaTestUserFactory.createChildCustomer(alphaTestUserParent, alphaTestUserChild, connectionId, childId);
        }
    }

    private JSONObject validChildObject() throws JSONException {
        return new JSONObject(){
            {
                put("TermsAccepted", true);
                put("TermsAcceptedDate", OffsetDateTime.parse("2021-06-26T12:30:24+04:00"));
                put("TermsVersion", LocalDate.now());
                put("Type", "BANKING");
            }
        };
    }

    @Test
    public void happy_path_accept_terms_201_response() {
        TEST("AHBDB-7255: Banking T&Cs for Child");
        TEST("AHBDB-11899: AC1 - T&Cs data and AC2 - T&Cs type");

        setupTestUserChildFresh();

        GIVEN("An onboarded parent is registering a banking child");
        OBWriteTerm1 obWriteTerm = OBWriteTerm1.builder()
                .termsAccepted(true)
                .termsAcceptedDate(OffsetDateTime.parse("2021-06-26T12:30:24+04:00"))
                .termsVersion(LocalDate.now())
                .type(BANKING)
                .build();

        WHEN("He/she accepts the T&Cs on child’s behalf");
        THEN("The customer endpoint should be called");
        final OBWriteTerm1 response =
                this.relationshipApi.createDependantTerm(alphaTestUserParent, connectionId, obWriteTerm).getData();

        AND("The T&Cs data will be saved against the child’s userID");
        AND("201 Create Response will be returned");

        assertNotNull(response);
        assertEquals(response.getType(), OBTermType.BANKING);

        DONE();
    }

    @Test
    public void patch_terms_successfully_valid_terms_version() {
        TEST("AHBDB-16835: Patching child terms ");
        setupTestUserChildFresh();
        GIVEN("I have a valid access token and account scope");

        OBWriteTerm1 obWriteTerm = OBWriteTerm1.builder()
                .termsAccepted(true)
                .termsAcceptedDate(OffsetDateTime.parse("2021-06-26T12:30:24+04:00"))
                .termsVersion(LocalDate.parse("2021-10-05"))
                .type(PRIVACY_POLICY)
                .build();

        WHEN("I try to patch terms accepted");
        final OBWriteTerm1 response =
                this.relationshipApi.createDependantTerm(alphaTestUserParent, connectionId, obWriteTerm).getData();

        obWriteTerm.setTermsAcceptedDate(OffsetDateTime.parse("2021-10-03T12:30:24+04:00"));
        obWriteTerm.setTermsAccepted(false);
        obWriteTerm.setTermsVersion(LocalDate.parse("2021-10-06"));

        final OBWriteTerm1 responsePatch = this.relationshipApi.patchDependantTerm(alphaTestUserParent, connectionId, obWriteTerm).getData();

        AND("The terms data will be updated against the child’s userID");
        AND("200 Create Response will be returned");

        assertNotNull(response);
        assertEquals(responsePatch.getType(), PRIVACY_POLICY);
        assertEquals(responsePatch.getTermsAccepted(), false);
        assertEquals(LocalDate.parse("2021-10-06"), responsePatch.getTermsVersion());
        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"1990-13-01", "AA-BB-ABCD", "00-00-0000", "09/05/1990", "", "1990-02-31", "-09-05-1990",
            "!@#$%^&*(", "1990", "ashdhfhfhf", "1", "00000000", "12"})
    public void unhappy_path_invalid_terms_version_400_response(String invalidTermsVersion) throws JSONException {
        TEST("AHBDB-7255: Banking T&Cs for Child");
        TEST("AHBDB-11900: AC3 - T&Cs version accepted validity");

        setupTestUserChild();

        GIVEN("The platform receives the terms and conditions version as a parameter");
        JSONObject jsonObject1 = validChildObject();
        jsonObject1.put("TermsVersion", invalidTermsVersion);

        WHEN("Parameter does not satisfy the validation in AC2");
        THEN("A 400 response will be returned");
        OBErrorResponse1 response = this.relationshipApi.createDependantTermError(alphaTestUserParent, connectionId, jsonObject1);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"3093720947365", "dhgnrhcosnrb", "", "_+{}[],.<>/?",
            "1996-13-14T20:30:24+04:001", "90-05-09T20:30:24+04:00", "1996-08-14T20:30:24+AA:00", "1996-08-14T20:65:24+04:00"})
    public void unhappy_path_invalid_terms_accepted_date_400_response(String invalidTermsAcceptedDate) throws JSONException {
        TEST("AHBDB-7255: Banking T&Cs for Child");
        TEST("AHBDB-11901: AC4 T&Cs accepted timestamp validity");

        setupTestUserChild();

        GIVEN("The platform receives the terms and conditions timestamp as a parameter");
        JSONObject jsonObject1 = validChildObject();
        jsonObject1.put("TermsAcceptedDate", invalidTermsAcceptedDate);

        WHEN("Parameter does not satisfy the validation in AC2");
        THEN("A 400 response will be returned");
        OBErrorResponse1 response = this.relationshipApi.createDependantTermError(alphaTestUserParent, connectionId, jsonObject1);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "FALSE", "ahvdal", "12345", "!@#$%^&*()", ""})
    public void unhappy_path_invalid_terms_accepted_400_response(String invalidTermsAccepted) throws JSONException {
        TEST("AHBDB-7255: Banking T&Cs for Child");
        TEST("AHBDB-11902: T&Cs Invalid Terms Accepted ");

        setupTestUserChild();

        GIVEN("The platform receives the terms and conditions Terms Accepted as a parameter");
        JSONObject jsonObject1 = validChildObject();
        jsonObject1.put("TermsAccepted", invalidTermsAccepted);

        WHEN("Parameter does not satisfy the validation in AC2");
        THEN("A 400 response will be returned");
        OBErrorResponse1 response = this.relationshipApi.createDependantTermError(alphaTestUserParent, connectionId, jsonObject1);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"registration", "banking", "Registration", "Banking", "!@#$%^&*()", "ahsfvjsafgbs", "1234455", ""})
    public void unhappy_path_invalid_type_400_response(String invalidType) throws JSONException {
        TEST("AHBDB-7255: Banking T&Cs for Child");
        TEST("AHBDB-11903: T&Cs Invalid Type");

        setupTestUserChild();

        GIVEN("The platform receives the terms and conditions Type as a parameter");
        JSONObject jsonObject1 = validChildObject();
        jsonObject1.put("Type", invalidType);

        WHEN("Parameter does not satisfy the validation in AC2");
        THEN("A 400 response will be returned");
        OBErrorResponse1 response = this.relationshipApi.createDependantTermError(alphaTestUserParent, connectionId, jsonObject1);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TermsAccepted", "TermsAcceptedDate", "TermsVersion", "Type"})
    public void missingMandatoryDataField_EmploymentStatus(String fieldToRemove) throws JSONException {
        TEST("AHBDB-7255: Banking T&Cs for Child");
        TEST("AHBDB-11904: AC5 Missing parameter " + fieldToRemove);
        setupTestUserChild();
        GIVEN("A POST request has been sent");

        WHEN("A parameter is missing in any set of information");
        JSONObject jsonObject1 = validChildObject();

        THEN("A 400 response will be returned");
        jsonObject1.remove(fieldToRemove);
        OBErrorResponse1 response = this.relationshipApi.createDependantTermError(alphaTestUserParent, connectionId, jsonObject1);

        assertEquals( ERROR_CODE_MISSING_FIELD, response.getCode(), "Error Code is not matching. Expected "
                +ERROR_CODE_MISSING_FIELD + " but received " + response.getCode());
        assertTrue(response.getMessage().contains("must not be null"));

        DONE();
    }
}
