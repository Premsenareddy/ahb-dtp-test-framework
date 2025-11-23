package uk.co.deloitte.banking.customer.idnow.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomerId1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle6")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetIdvCardDataByCustomerId {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private IdNowApi idNowApi;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private CustomerConfig customerConfig;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {

        this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());

    }

    @Test
    public void retrieve_applicant_information_200_success_response() {

        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        TEST("AHBDB-18128: The get Idnow details by customerId - 200 success response");
        setupTestUser();
        TokenHolder token = idNowApi.createApplicant(this.alphaTestUser);
        this.alphaTestUser.setApplicantId(token.getApplicantId());
        this.idNowApi.setIdNowAnswer(this.alphaTestUser, "SUCCESS");
        String customerId = this.customerApi.getCurrentCustomer(this.alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();

        GIVEN("A customer has completed their ID&V process");
        WHEN("The client attempts to retrieve the applicant's full IDNow result information with a valid api key");
        ApplicantExtractedDTO applicantDTO = this.idNowApi.
                getApplicantResultsForCustomerId(this.alphaTestUser,customerId);

        THEN("The platform will return a 200 response");
        AND("The platform will return the JSON related to the user ID");
        Assertions.assertEquals(applicantDTO.getIdentificationProcess().get("Id"), this.alphaTestUser.getApplicantId()
                , "Applicant ID did not match expected, expected: " + this.alphaTestUser.getApplicantId());

        DONE();
    }
}
