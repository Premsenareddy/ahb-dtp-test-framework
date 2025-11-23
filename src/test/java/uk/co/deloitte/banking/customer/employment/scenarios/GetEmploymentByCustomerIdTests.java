package uk.co.deloitte.banking.customer.employment.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentStatus;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBReadEmploymentDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBWriteEmploymentDetailsResponse1;
import uk.co.deloitte.banking.customer.employment.api.EmploymentApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@Tag("@BuildCycle6")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetEmploymentByCustomerIdTests {

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EmploymentApiV2 employmentApi;

    @Inject
    private EnvUtils envUtils;

    private void setupTestUser() {
        /**
         * TODO :: Ignored in NFT currently
         */
        envUtils.ignoreTestInEnv(Environments.NFT);

        //TODO:: Still failing
        //envUtils.ignoreTestInEnv(Environments.ALL);

        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }


    @Test
    public void get_employment_200_success() {
        TEST("AHBDB-7596: Get Employment for customerId- 200 Success");
        setupTestUser();
        String customerId = this.customerApi.getCurrentCustomer(this.alphaTestUser).getData().getCustomer().get(0).getCustomerId().toString();
        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName("AHB")
                .monthlyIncome("AED 1234")
                .incomeSource("salary")
                .businessCode("AAN")
                .designationLAPSCode("1")
                .professionCode("32")
                .build();
        this.employmentApi.createEmploymentDetails(this.alphaTestUser,employment);
        GIVEN("We have received a request from the client to get a customer with a valid userID that exists");
        WHEN("We pass the request to CRM to get that customer with a valid X-API-KEY");
        OBReadEmploymentDetailsResponse1 result = this.employmentApi.getEmploymentDetailsForCustomerId(alphaTestUser, customerId);

        THEN("the platform will return a 200 response");
        AND("the platform will return the customer’s employment information");
        OBEmploymentDetails1 data = result.getData();

        Assertions.assertNotNull(data, "Data was null");

        DONE();
    }
}
