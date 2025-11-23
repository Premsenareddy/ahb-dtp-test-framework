package uk.co.deloitte.banking.customer.authentication.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceRegistrationRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DisabledDeviceStatusV2;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.authentication.api.DeviceStatusApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("BuildCycle1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DisableDeviceStatusTests {

    @Inject
    private DeviceStatusApi deviceStatusApi;

    @Test
    public void test_disable_device_already_deactivated() {
        TEST("AHBDB-XXX: Test with body");

        GIVEN("CRM wants to de-register a device for an existing user where deviceId is already disabled");

        WHEN("Disable device is called with a already disabled device body");
        DisabledDeviceStatusV2 response =
                deviceStatusApi.disableDeviceWebhooks("4A7B2089-CCBE-FF00-B3B3-106E5C307", "7d619010-1ae9-361c-afc6-09d7df8ff658", 404);

        THEN("The platform will return a 404 Response");
        AND("Error message contains the correct code");
        assertNotNull(response);
        DONE();
    }

    @Test
    public void test_fetch_customer_device() {
        TEST("AHBDB-XXX: Test with CustomerId");

        GIVEN("CRM wants to fetch the devices status of a customer");

        WHEN("Fetch devices of a customer");
        DeviceStatusResponseV2 response =
                deviceStatusApi.getdeviceStatusWebhooks("7d619010-1ae9-361c-afc6-09d7df8ff658", 200);

        THEN("The platform will return a 200 Response");
        AND("Data returned with correct response code");
        assertNotNull(response);
        DONE();
    }
}
