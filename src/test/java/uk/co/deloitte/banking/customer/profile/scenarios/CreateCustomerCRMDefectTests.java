package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.residentialaddress.api.ResidentialAddressApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.Collections;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateCustomerCRMDefectTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private ResidentialAddressApi residentialAddressApi;

    @Inject
    private EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;

    private final String ERROR_CODE_REQUEST_VALIDATION = "REQUEST_VALIDATION";
    private final String STANDARD_ERROR_MESSAGE = "createCustomer.arg7.data.";
    private final String ERROR_CODE_0002 = "0002";

    public void setupTestUser() {
        /**
         * TODO :: These tests do not currently work in SIT, NFT
         */
        envUtils.ignoreTestInEnv(Environments.NFT);
        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = this.alphaTestUserFactory.setupUser(this.alphaTestUser);
    }

    @Test
    public void customer_state_missing_201_response() {
        TEST("AHBDB-7733: Customer state missing - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        OBWriteCustomer1 obWriteCustomer1 =
                OBWriteCustomer1.builder().data(OBWriteCustomer1Data.builder()
                        .preferredName(this.alphaTestUser.getName())
                        .dateOfBirth(this.alphaTestUser.getDateOfBirth())
                        .email(this.alphaTestUser.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .mobileNumber(this.alphaTestUser.getUserTelephone())
                        .language(this.alphaTestUser.getLanguage())
                        .gender(OBGender.FEMALE)
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .termsVersion(LocalDate.of(2020, 12 ,20))
                        .address(OBPostalAddress6.builder()
                                .buildingNumber(generateRandomBuildingNumber())
                                .streetName(generateRandomStreetName())
                                .countrySubDivision(generateRandomCountrySubDivision())
                                .country("AE")
                                .postalCode(generateRandomPostalCode())
                                .addressLine(Collections.singletonList(generateRandomAddressLine()))
                                .build())
                        .termsAccepted(true)
                        .build()).build();
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing customer state");
        this.customerApi.createCustomerSuccess(this.alphaTestUser, obWriteCustomer1);
        THEN("We'll receive a 201 response");
    }

    @Test
    public void address_missing_201_response() {
        TEST("AHBDB-7733: Address missing - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");
        OBWriteCustomer1 obWriteCustomer1 =
                OBWriteCustomer1.builder().data(OBWriteCustomer1Data.builder()
                        .preferredName(this.alphaTestUser.getName())
                        .dateOfBirth(this.alphaTestUser.getDateOfBirth())
                        .email(this.alphaTestUser.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .mobileNumber(this.alphaTestUser.getUserTelephone())
                        .language(this.alphaTestUser.getLanguage())
                        .gender(OBGender.FEMALE)
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .customerState(OBCustomerStateV1.IDV_REVIEW_REQUIRED)
                        .termsVersion(LocalDate.of(2020, 12 ,20))
                        .termsAccepted(true)
                        .build()).build();
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but missing address");
        this.customerApi.createCustomerSuccess(this.alphaTestUser, obWriteCustomer1);
        THEN("We'll receive a 201 response");
    }

    @Test
    public void experience_body_201_response() {
        TEST("AHBDB-7733: Using the same body as the experience team at that part of the journey");
        setupTestUser();
        GIVEN("We have received a request from the client to create a customer");

        LocalDate date = LocalDate.of(1990, 6, 25);
        String preferredName = "TechUser";
        String language = "en";
        OBWriteEmailState1 state = OBWriteEmailState1.NOT_VERIFIED;
        LocalDate termsVersion = LocalDate.of(2021, 2, 2);

//      Body being sent
//      CustomerId":null,"CreationDateTime":null,"DateOfBirth":"1990-06-25",
//      "MobileNumber":"+555549900411","PreferredName":"TechUser","Language":"en",
//      "Email":"mkyu.poo7411@test.com","EmailState":"NOT_VERIFIED","TermsVersion":"2021-02-02","TermsAccepted":true}

        this.alphaTestUser.setDateOfBirth(date);
        this.alphaTestUser.setName(preferredName);
        this.alphaTestUser.setLanguage(language);

        OBWriteCustomer1 obWriteCustomer1 =
                OBWriteCustomer1.builder().data(OBWriteCustomer1Data.builder()
                        .dateOfBirth(this.alphaTestUser.getDateOfBirth())
                        .mobileNumber(this.alphaTestUser.getUserTelephone())
                        .preferredName(this.alphaTestUser.getName())
                        .language(this.alphaTestUser.getLanguage())
                        .email(this.alphaTestUser.getUserEmail())
                        .emailState(state)
                        .termsVersion(termsVersion)
                        .termsAccepted(true)
                        .build()).build();

        WHEN("We pass the request to CRM to create the customer with a valid JWT token");
        this.customerApi.createCustomerSuccess(this.alphaTestUser, obWriteCustomer1);
        THEN("We'll receive a 201 response");
    }
}
