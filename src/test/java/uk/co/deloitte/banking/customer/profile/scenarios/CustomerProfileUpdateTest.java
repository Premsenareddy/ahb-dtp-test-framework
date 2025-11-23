package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.util.UUID;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerProfileUpdateTest {

    @Inject
    private CustomerApi customerApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(new AlphaTestUser());
    }

    @Test
    public void test_addressUpdate_404() {
        TEST("AHBDB-18254: Update residential address - No idvs found for customer 404");
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);
        setupTestUser();
        OBWriteCustomerResponse1 response = this.customerApi.updateCustomerProfile(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(OBPartialPostalAddress6.builder().subDepartment("Test").streetName("Test").townName("Abu Dhabi").countrySubDivision("Abu Dhabi").country("AE").build()).build()).build(), 404);

        WHEN("The client attempts to update customer address and fails due to idv details");

        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getData());
        DONE();
    }

    @Test
    public void test_mobileUpdate_400() {
        TEST("AHBDB-18254: Update Mobile No - Not valid mobile number");
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);
        setupTestUser();
        OBWriteCustomerResponse1 response = this.customerApi.updateCustomerProfile(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder().mobileNumber("971559976479").build()).build(), 400);
        WHEN("The client attempts to update mobile number, it fails due to in-valid no. ");
        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getData());
        DONE();
    }

    @Test
    public void test_EmailIdUpdate_409() {
        TEST("AHBDB-18254: Update Email id - Not valid email Id");
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);
        setupTestUser();
        OBWriteCustomerResponse1 response = this.customerApi.updateCustomerProfile(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder().email("test4@yopmail.com").build()).build(), 409);
        WHEN("The client attempts to update mobile number, it fails due to in-valid no. ");
        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getData());
        DONE();
    }

    @Test
    public void test_EmailIdUpdate_200() {
        TEST("AHBDB-18254: Update Email id - Valid email Id");
        /**
         * TODO :: Currently ignored in NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);
        setupTestUser();
        OBWriteCustomerResponse1 response = this.customerApi.updateCustomerProfile(this.alphaTestUser, OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder().email(generateRandomEmail()).build()).build(), 200);
        WHEN("The client attempts to update mobile number, it updates the mobile no ");
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getData());
        DONE();
    }

    public static String generateRandomEmail() {
        return UUID.randomUUID().toString().substring(0, 30) + "@ahb.com";
    }
}
