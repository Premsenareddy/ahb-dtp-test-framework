package uk.co.deloitte.banking.customer.residentialaddress.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBCustomerStateV1;
import uk.co.deloitte.banking.customer.api.customer.model.OBReadCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomerResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWritePartialCustomer1Data;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPostalAddress6;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.residentialaddress.api.ResidentialAddressApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomAddressLine;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomBuildingNumber;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomCountrySubDivision;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomDepartment;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomPostalCode;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomStreetName;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomSubDepartment;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomTownName;


@Tag("@BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentialAddressTests {

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private ResidentialAddressApi residentialAddressApi;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    @Order(1)
    @Test
    public void happy_path_create_residential_address_201_response() {
        TEST("AHBDB-1996: AC1 Create residential address - 201 response");
        TEST("AHBDB-3359: AC1 Positive Test - Happy Path Create residential address - 201 response");
        TEST("AHBDB-3359: AC4 Positive Test - Happy Path Get residential address - 200 response");
        TEST("AHBDB-1996: AC4 Get customer - 200 success response");
        AlphaTestUser testUser = alphaTestUserFactory.setupUser(new AlphaTestUser());
        GIVEN("The customer has provided their residential address");
        AND("The client creates the customer profile with a valid residential address");
        AND("The Status Code is 201");
        String buildingNumber = generateRandomBuildingNumber();
        String streetName = generateRandomStreetName();
        String countrySubDivision = generateRandomCountrySubDivision();
        String postalCode = generateRandomPostalCode();
        String addressLine = generateRandomAddressLine();

        OBWriteCustomer1 customer1 = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(generateEnglishRandomString(10))
                        .dateOfBirth(LocalDate.now().minusYears(25))
                        .email(testUser.getUserEmail())
                        .mobileNumber(testUser.getUserTelephone())
                        .language(testUser.getLanguage())
                        .firstName(generateEnglishRandomString(10))
                        .lastName(generateEnglishRandomString(10))
                        .fullName(generateEnglishRandomString(20))
                        .gender(testUser.getGender())
                        .nationality("AE")
                        .countryOfBirth("AE")
                        .cityOfBirth("Dubai")
                        .email(testUser.getUserEmail())
                        .emailState(OBWriteEmailState1.VERIFIED)
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .termsVersion(LocalDate.of(2020, 12, 20))
                        .termsAccepted(true)
                        .address(OBPostalAddress6.builder()
                                .buildingNumber(buildingNumber)
                                .streetName(streetName)
                                .countrySubDivision(countrySubDivision)
                                .country("AE")
                                .postalCode(postalCode)
                                .addressLine(Collections.singletonList(addressLine))
                                .build())
                        .build())
                .build();

        customerApi.createCustomerSuccess(testUser, customer1);

        WHEN("The client attempts to retrieve the applicant's customer information with a valid JWT token");
        OBReadCustomer1 user = customerApi.getCurrentCustomer(testUser);

        OBCustomer1 customerDetails = user.getData().getCustomer().get(0);
        Assertions.assertEquals(testUser.getUserId(), customerDetails.getCustomerId().toString());

        OBPostalAddress6 addressDetails = customerDetails.getAddress();

        THEN("The Status Code is 200(OK)");
        AND("The platform will return the customer information which includes the residential address");
        Assertions.assertEquals(buildingNumber, addressDetails.getBuildingNumber());
        Assertions.assertEquals(streetName, addressDetails.getStreetName());
        Assertions.assertEquals(countrySubDivision, addressDetails.getCountrySubDivision());
        Assertions.assertEquals("AE", addressDetails.getCountry());
        Assertions.assertEquals(postalCode, addressDetails.getPostalCode());
        Assertions.assertEquals(addressLine, addressDetails.getAddressLine().get(0));
        DONE();
    }

    @Order(2)
    @Test
    public void happy_path_patch_residential_address_200_response() {
        TEST("AHBDB-1996: AC2 Patch residential address - 200 response");
        TEST("AHBDB-3067: AC2 Positive Test - Happy Path Patch residential address - 200 response");
        setupTestUser();
        GIVEN("The customer has provided their residential address");
        WHEN("The client updates the customer profile with a valid residential address");

        String newDepartment = generateRandomDepartment();
        String subDepartment = generateRandomSubDepartment();
        String buildingNumber = generateRandomBuildingNumber();
        String streetName = generateRandomStreetName();
        String addressLine = generateRandomAddressLine();
        String townName = generateRandomTownName();
        String countrySubDivision = generateRandomCountrySubDivision();
        String postalCode = generateRandomPostalCode();

        OBPartialPostalAddress6 newAddress = OBPartialPostalAddress6.builder()
                .department(newDepartment)
                .subDepartment(subDepartment)
                .buildingNumber(buildingNumber)
                .streetName(streetName)
                .addressLine(Collections.singletonList(addressLine))
                .townName(townName)
                .countrySubDivision(countrySubDivision)
                .country("AE")
                .postalCode(postalCode)
                .build();

        OBWritePartialCustomer1 updateCustomer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .address(newAddress)
                        .build())
                .build();

        THEN("The Status Code is 200(OK)");
        OBWriteCustomerResponse1 response = customerApi.updateCustomer(alphaTestUser, updateCustomer, 200);
        Assertions.assertEquals(alphaTestUser.getUserId(), response.getData().getCustomerId().toString());

        AND("The details are persisted");
        OBReadCustomer1 getResponse = customerApi.getCurrentCustomer(alphaTestUser);
        Assertions.assertEquals(newAddress.getCountry(), getResponse.getData().getCustomer().get(0).getAddress().getCountry());
        Assertions.assertEquals(newAddress.getBuildingNumber(), getResponse.getData().getCustomer().get(0).getAddress().getBuildingNumber());
        Assertions.assertEquals(newAddress.getPostalCode(), getResponse.getData().getCustomer().get(0).getAddress().getPostalCode());
        Assertions.assertEquals(newAddress.getDepartment(), getResponse.getData().getCustomer().get(0).getAddress().getDepartment());
        Assertions.assertEquals(newAddress.getCountrySubDivision(), getResponse.getData().getCustomer().get(0).getAddress().getCountrySubDivision());
        Assertions.assertEquals(newAddress.getTownName(), getResponse.getData().getCustomer().get(0).getAddress().getTownName());
        DONE();
    }

    @Order(999)
    @Test
    public void customer_not_found_404_response() {

        TEST("AHBDB-1996: AC5 Customer not found - 404 not found ");
        TEST("AHBDB-3070: AC5 Negative Test - Customer not found - 404 not found");
        setupTestUser();
        GIVEN("The customer is not in the records");
        customerApi.deleteCustomer(alphaTestUser);

        WHEN("The client attempts to retrieve the applicant's customer profile with a UserID that does not exist");
        customerApi.getCurrentCustomerError(alphaTestUser, 404);
        THEN("The Status Code is 404");
    }

    @Order(1)
    @Test
    public void invalid_residential_address_long_AddressLine_400_response() throws JSONException {
        TEST("AHBDB-1996: AC3 Invalid residential address - 400 response");
        TEST("AHBDB-3362: AC3 Negative Test - Invalid residential address AddressLine - 400 response");
        setupTestUser();

        List<String> addressLine = new ArrayList<>() {
            {
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
                add(generateEnglishRandomString(20));
            }
        };

        GIVEN("The client wants to store a residential address against the customer");
        AND("The address format does not satisfy the validation in the data table");
        JSONObject address = this.residentialAddressApi.createAddressJSON("AddressLine", addressLine);
        JSONObject user = this.residentialAddressApi.userProfileJSON(address, alphaTestUser);

        WHEN("The client updates the customer profile with this address");
        this.customerApi.createCustomerErrorJsonObject(alphaTestUser, user, 400);

        THEN("The Status Code is 400");
    }
}