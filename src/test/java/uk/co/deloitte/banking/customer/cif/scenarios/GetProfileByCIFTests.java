package uk.co.deloitte.banking.customer.cif.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerType1;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomIntegerInRange;

@Tag("BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetProfileByCIFTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private static final String ERROR_NOT_FOUND = "UAE.ERROR.NOT_FOUND";
    private static final String GET_ERROR_MESSAGE = "No results found for specified CIF";

    private void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());

            this.customerApi.createCustomerIdvDetails(this.alphaTestUser);

            OBReadCustomer1 getCustomerResponse = this.customerApi.getCurrentCustomer(this.alphaTestUser);
            assertEquals(OBCustomerType1.MARKETPLACE,
                    getCustomerResponse.getData().getCustomer().get(0).getCustomerType());

            this.customerApi.putCustomerCif(this.alphaTestUser);

            OBReadCustomer1 getCustomerResponse2 = this.customerApi.getCurrentCustomer(this.alphaTestUser);
            assertEquals(OBCustomerType1.BANKING,
                    getCustomerResponse2.getData().getCustomer().get(0).getCustomerType());
        }
    }

    @Test
    @Order(1)
    void happy_path_put_cif_get_profile() {
        TEST("AHBDB-9090: Get customer profile by Cif Id");
        TEST("AHBDB-9742: AC1 - Success - Response 200");
        GIVEN("A user is a banking customer");
        AND("Has generated a CIF number");
        setupTestUser();

        OBCustomer1 customer1 = customerApi.getCurrentCustomer(alphaTestUser).getData().getCustomer().get(0);
        String cif = customer1.getCif();

        OBReadCustomer1 getCustomer = this.customerApi.getProfileUsingCIF(cif);

        assertNotNull(getCustomer);
        assertEquals(alphaTestUser.getUserId(),
                getCustomer.getData().getCustomer().get(0).getCustomerId().toString());

        DONE();
    }

    @Test
    @Order(1)
    void negative_test_cif_does_not_exist_404_response() {
        TEST("AHBDB-9090: Get customer profile by Cif Id");
        TEST("AHBDB-9743: AC2 - Error - Response 404");
        GIVEN("A client wants to query for a customer profile in CRM with a CIF number");
        AND("That CIF number does not exist in CRM against a customer");
        String randomCif = generateRandomIntegerInRange(1000000, 9999999).toString();

        WHEN("The client calls the GET customer profile by CIF endpoint with the random CIF number: " + randomCif);

        OBErrorResponse1 error =
                this.customerApi.getProfileUsingCIFError(randomCif, 404);

        THEN("The platform will return a 404 Not Found");
        assertNotNull(error);
        assertEquals(GET_ERROR_MESSAGE, error.getErrors().get(0).getMessage());
        assertEquals(ERROR_NOT_FOUND, error.getCode());

        DONE();
    }
}
