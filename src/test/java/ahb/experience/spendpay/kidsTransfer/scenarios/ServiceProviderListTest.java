package ahb.experience.spendpay.kidsTransfer.scenarios;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin;
import ahb.experience.spendpay.kidsTransfer.api.ServiceProviderList;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.Example;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.InvalidAgeGroup;

import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@MicronautTest
@Slf4j
@Singleton

public class ServiceProviderListTest {

    @Inject
    bankingUserLogin bankingUserLogin;

    @Inject
    ServiceProviderList serviceProviderList;




    @Order(1)
    @Test
    public void verify_service_provider_list_kids() {
        TEST("AHBDB-14237");
        TEST("AHBDB-18259 - API | Verify 200 status code for service provider list for kids");
        TEST("AHBDB-18260 - API | Validate that the default provider value is true for both DU and ETISALAT");

        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the service provider API ");

        Example example =serviceProviderList.serviceTypesForKids(bearerToken);
        THEN("I verify that the default provider value is true");
        //System.out.println("Default provider is " + example.categories.get(0).providers.get(0).defaultProvider);
        Assert.assertEquals("Service provider default value is not true",example.categories.get(0).providers.get(0).defaultProvider, true);
        Assert.assertEquals("Service provider default value is not true",example.categories.get(0).providers.get(1).defaultProvider, true);
        DONE();
    }
    @Order(2)
    @Test
    public void verify_service_provider_list_teens() {
        TEST("AHBDB-14237");
        TEST("AHBDB-18261 - API | Verify 200 status code for service provider list for teens");

        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the service provider API ");

        Example example =serviceProviderList.serviceTypesForTeens(bearerToken);
        THEN("I verify that the default provider value is true");
        //System.out.println("Default provider is " + example.categories.get(0).providers.get(0).defaultProvider);
        Assert.assertEquals("Service provider default value is not true",example.categories.get(0).providers.get(0).defaultProvider, true);
        Assert.assertEquals("Service provider default value is not true",example.categories.get(0).providers.get(1).defaultProvider, true);
        Assert.assertEquals("ETISALAT provider is not present",example.categories.get(0).providers.get(0).providerName, "Etisalat");
        Assert.assertEquals("DU provider is not present",example.categories.get(0).providers.get(1).providerName, "DU");
        DONE();
    }

    @Order(3)
    @Test
    public void verify_invalid_age_group_status_code_400() {
        TEST("AHBDB-18263 - API |Validate 400 error code and correct message for invalid age group");
        TEST("AHBDB-14237");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the service provider API ");

        InvalidAgeGroup invalidAgeGroup =serviceProviderList.invalidAgeGroupTest(bearerToken);
        THEN("I verify that the error code and error message is correct ");

        Assert.assertEquals("Error message is not correct ",invalidAgeGroup.message, "Invalid ageGroup provided");
        Assert.assertEquals("Error code is not correct",invalidAgeGroup.code, "INVALID_AGE_GROUP");
        DONE();
    }

}

