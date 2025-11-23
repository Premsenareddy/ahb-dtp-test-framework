package uk.co.deloitte.banking.customer.idnow.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.DocumentType;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.GetApplicantListResponse;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.idnow.model.DocumentType.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

@Tag("@BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IdNowTestsBasedOnDocumentType {

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private IdNowApi idNowApi;

    private void setupTestUser() {
        alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
    }

    private TokenHolder createApplicant(DocumentType documentType){
        TokenHolder token = idNowApi.createApplicantBasedOnDocumentType(alphaTestUser, documentType);
        alphaTestUser.setApplicantId(token.getApplicantId());
        return token;
    }


    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS"})
    public void happy_path_success_response_based_on_document_type_applicant_id(String successStatus) {
        TEST("AHBDB-16719: AC1 - Request Applicant Id");
        setupTestUser();
        GIVEN("IDNow has finished processing the applicant's ID");
        AND("The verification has been successful");
        createApplicant(PASSPORT);
        WHEN("The Ident status value " + successStatus + " is returned from IDNow");
        this.idNowApi.setIdNowAnswer(alphaTestUser, successStatus, PASSPORT);
        createApplicant(EID);
        this.idNowApi.setIdNowAnswer(alphaTestUser, successStatus, EID);
        THEN("I will trigger an event saying IDV is completed along with the IDNow response");
        GetApplicantListResponse applicantResultV2 = this.idNowApi.getApplicantResultV2(this.alphaTestUser);
        assertEquals(2, applicantResultV2.getIdNowDetails().size());
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS"})
    public void happy_path_success_response_based_on_document_type_validate_shortname(String successStatus) {
        TEST("AHBDB-16719: AC2 - Request Applicant Id");
        setupTestUser();
        GIVEN("IDNow has finished processing the applicant's ID");
        AND("The verification has been successful");
        createApplicant(PASSPORT);
        WHEN("The Ident status value " + successStatus + " is returned from IDNow");
        this.idNowApi.setIdNowAnswer(alphaTestUser, successStatus, PASSPORT);
        createApplicant(EID);
        this.idNowApi.setIdNowAnswer(alphaTestUser, successStatus, EID);
        THEN("I will trigger an event saying IDV is completed along with the IDNow response");
        GetApplicantListResponse applicantResultV2 = this.idNowApi.getApplicantResultV2(this.alphaTestUser);
        assertEquals(2, applicantResultV2.getIdNowDetails().size());
        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"EID"})
    public void successfullyCreateIdNow_PASSPORT(DocumentType documentType) {
        TEST("AHBDB-16719: AC3 - Request Applicant Id");
        setupTestUser();
        GIVEN("I have a valid access token and customer scope");

        WHEN("I submit a valid payload to the endpoint");

        TokenHolder response = createApplicant(documentType);
        THEN("Status code 201(CREATED) is returned");
        AND("Response contains a token");
        assertNotNull(response.getSdkToken());
        assertNotNull(response.getApplicantId());
        assertEquals(response.getApplicantId(), alphaTestUser.getApplicantId());
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASSPORT"})
    public void successfullyCreateIdNow_Only_PASSPORT(DocumentType documentType) {
        TEST("AHBDB-16719 AC-4- successfully create applicant with passport scan");
        setupTestUser();
        GIVEN("I have a valid access token and customer scope");

        WHEN("I submit a valid payload to the endpoint");

        TokenHolder response = createApplicant(documentType);
        THEN("Status code 201(CREATED) is returned");
        AND("Response contains a token");
        assertNotNull(response.getSdkToken());
        assertNotNull(response.getApplicantId());
        assertNotNull(response.getShortName());
        assertEquals(response.getApplicantId(), alphaTestUser.getApplicantId());
        DONE();
    }
}
