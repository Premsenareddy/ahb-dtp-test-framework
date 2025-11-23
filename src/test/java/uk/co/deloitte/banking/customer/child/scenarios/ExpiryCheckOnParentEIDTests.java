package uk.co.deloitte.banking.customer.child.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBReadIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.JsonUtils.extractValue;

@Tag("@BuildCycle5.1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExpiryCheckOnParentEIDTests {

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private AlphaTestUser alphaTestUser;

    private final String ERROR_CODE = "0004";
    private final String ERROR_MESSAGE = "No idvs found for customer";

    private void setupTestUser() {
        
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    @Test
    public void a_userId_does_not_exist_404_response() {

        envUtils.ignoreTestInEnv(Environments.NFT);
        TEST("AHBDB-6987: Expiry check on parents EID");
        TEST("AHBDB-8846: AC2 Customer record doesn't exist - 404 response");
        GIVEN("A userID does not exist");
        AlphaTestUser idvNotCompleted = new AlphaTestUser();
        this.alphaTestUserFactory.setupCustomer(idvNotCompleted);

        WHEN("We pass the request to CRM to get that customer with a valid JWT token");

        OBErrorResponse1 errorResponse = this.customerApi.getCustomerIdvDetailsNegative(idvNotCompleted, 404);

        THEN("We will return a 404 - not found");

        assertEquals("UAE.ERROR.NOT_FOUND", errorResponse.getCode(),
                "Expected: " + "UAE.ERROR.NOT_FOUND" + " but got: "+ errorResponse.getCode());

        assertEquals(ERROR_MESSAGE, errorResponse.getMessage(),
                "Expected: " + ERROR_MESSAGE + " but got: "+ errorResponse.getMessage());

        DONE();
    }

    @Test
    public void get_IDV_200_response() {
        TEST("AHBDB-6987: Expiry check on parents EID");
        TEST("AHBDB-8845: AC1 Get IDV - 200 Success");
        GIVEN("The parent has completed IDV");
        setupTestUser();

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

        customerApi.updateCustomer(alphaTestUser, obWriteCustomer1,200);

        OBWriteIdvDetailsResponse1 postResponse = this.customerApi.createCustomerIdvDetails(alphaTestUser);

        WHEN("The client attempts to get the parent's IDV information with a valid userID that exists");

        OBReadIdvDetailsResponse1 response = this.customerApi.getCustomerIdvDetails(alphaTestUser);

        THEN("We'll return a 200 response");
        AND("We'll return all of the ID&V input fields that relate to that userID from CRM");
        assertEquals(postResponse.getData().toString(), response.getData().toString());
        AND("The client can check the expiry date of the EID returned in the response");
        assertNotNull(response.getData().getDateOfExpiry());

        DONE();
    }


}
