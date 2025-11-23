package uk.co.deloitte.banking.customer.idnow.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomerId1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle4")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UniqueEIDTestsCRM {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
//      TODO :: Ignored in NFT
        envUtils.ignoreTestInEnv(Environments.NFT);

        AlphaTestUser alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
    }

    @Test
    public void happy_path_duplicate_eid_409_response() {
        TEST("AHBDB-5678: AC1 Duplicate personal/document number - 409");
        TEST("AHBDB-7717: AC1 Existing EID returns 409");
        GIVEN("A customer has completed IDV");
        setupTestUser();
        WHEN("The client tries to save an EID personal/document number which already exists");

        OBWriteIdvDetailsResponse1 error =
                this.customerApi.createCustomerIdvDetailsDuplicateDocumentNumber(this.alphaTestUser);

        THEN("We will return a 409 error");
        Assertions.assertNotNull(error);
        DONE();
    }

    @Test
    public void check_eid_endpoint_200_response() {
        TEST("AHBDB-7740: API to check EmiratesId Duplication giving 403 - V2");
        GIVEN("A customer has completed IDV");
        setupTestUser();

        String customerId = this.customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        this.alphaTestUser.setCustomerId(customerId);

        JSONObject idvDetails = this.customerApi.createJSONIdvDetails("IdCountry", "AE");
        String emiratesId = idvDetails.getString("DocumentNumber");

        this.customerApi.createCustomerIdvDetailsJSON(this.alphaTestUser, idvDetails, 201);

        WHEN("The client tries to retrieve a customer by their EID");
        THEN("We will return a 200 OK");
        OBReadCustomerId1 response = this.customerApi.getCustomersByEid(emiratesId);
        Assertions.assertEquals(1, response.getData().size());
        Assertions.assertEquals(customerId, response.getData().get(0).getCustomerId().toString());
        AND("The customer's customerId is returned as a response");
        AND("It is the only customerId being returned");
        DONE();
    }
}
