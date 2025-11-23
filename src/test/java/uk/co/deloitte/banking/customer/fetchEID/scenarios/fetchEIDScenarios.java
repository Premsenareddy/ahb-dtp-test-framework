package uk.co.deloitte.banking.customer.fetchEID.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.customer.fetchEID.fetchEIDStatus;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.cards.scenarios.creditCard.CreditCardTransaction;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomerId1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.fetchEID.api.FetchEIDAPI;
import uk.co.deloitte.banking.customer.idnow.api.IdNowApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class fetchEIDScenarios {

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


    @Inject
    private FetchEIDAPI fetchEIDAPI;

    private AlphaTestUser alphaTestUser;

    public void setupTestUser(String mobile) {
        envUtils.ignoreTestInEnv(Environments.NFT, Environments.DEV);
        if (alphaTestUser == null || !alphaTestUser.getUserTelephone().equalsIgnoreCase(mobile)) {
            alphaTestUser = new AlphaTestUser(mobile);
            alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin(alphaTestUser);
        }
    }

    @Test
    public void retrieve_eid_details_200_success_response() {


        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);
        if (envUtils.isCit())
          setupTestUser("+555508711334");
        else
            setupTestUser("+555508711388");
        TEST("AHBDB: AC1 Retrieve Applicant EID information - 200 success response");
        TokenHolder token = idNowApi.createApplicant(this.alphaTestUser);
        this.alphaTestUser.setApplicantId(token.getApplicantId());
        this.idNowApi.setIdNowAnswer(this.alphaTestUser, "SUCCESS");

        GIVEN("A customer has already logged in to the application");
        WHEN("The client tries to fetch the EID information from the token");
        fetchEIDStatus response1= fetchEIDAPI.fetchEID(alphaTestUser);

        THEN("The platform will return a 200 response");
        AND("All the mandatory fields are not blank");
        Assertions.assertTrue(StringUtils.isNotBlank(response1.getFetchEIDDetails().result));
        Assertions.assertTrue(StringUtils.isNotBlank(response1.getFetchEIDDetails().dateOfExpiry));
        Assertions.assertTrue(StringUtils.isNotBlank(response1.getFetchEIDDetails().documentNumber));
        Assertions.assertTrue(StringUtils.isNotBlank(response1.getFetchEIDDetails().transactionNumber));
        Assertions.assertTrue(StringUtils.isNotBlank(response1.getFetchEIDDetails().type));
        Assertions.assertTrue(StringUtils.isNotBlank(response1.getFetchEIDDetails().documentSource));



        DONE();
    }

}
