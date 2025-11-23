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
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;

@Tag("BuildCycle1")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegisterDeviceTests {

    @Inject
    private AuthenticateApiV2 authenticateApiV2;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private static final String REQUEST_VALIDATION = "REQUEST_VALIDATION";

    private AlphaTestUser alphaTestUser;

    @BeforeEach
    public void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = new AlphaTestUser();
        }
    }

    @Test
    public void negative_test_register_device_with_empty_body() {
        TEST("AHBDB-XXX: Test empty body");

        GIVEN("A client wants to register a new device for a new user");
        DeviceRegistrationRequestV1 deviceRegistrationRequest = DeviceRegistrationRequestV1.builder()
                .build();

        WHEN("Create device is called with a empty body");
        OBErrorResponse1 error =
                authenticateApiV2.registerUserAndDeviceError(alphaTestUser, deviceRegistrationRequest, 400);

        THEN("The platform will return a 400 Response");
        AND("Error message contains the correct code");
        assertEquals(REQUEST_VALIDATION, error.getCode());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12"})
    public void negative_test_register_device_with_invalid_device_id(String invalidDeviceId) {

        TEST("AHBDB-XXX: Test empty body");
        GIVEN("A client wants to register a new device for a new user");
        DeviceRegistrationRequestV1 deviceRegistrationRequest = DeviceRegistrationRequestV1.builder()
                .deviceId(invalidDeviceId)
                .deviceHash(alphaTestUser.getDeviceHash())
                .build();

        WHEN("Create device called with an invalid device ID in the body");
        OBErrorResponse1 error =
                authenticateApiV2.registerUserAndDeviceError(alphaTestUser, deviceRegistrationRequest, 400);

        THEN("The platform will return a Bad Request 400");
        AND("Error message contains the correct code");
        assertEquals(REQUEST_VALIDATION, error.getCode());

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12"})
    public void negative_test_register_device_with_invalid_device_hash(String invalidDeviceHash) {

        TEST("AHBDB-XXX: Test empty body");
        GIVEN("A client wants to register a new device for a new user");
        DeviceRegistrationRequestV1 deviceRegistrationRequest = DeviceRegistrationRequestV1.builder()
                .deviceId(alphaTestUser.getDeviceId())
                .deviceHash(invalidDeviceHash)
                .build();

        WHEN("Create device called with an invalid device ID in the body");
        OBErrorResponse1 error =
                authenticateApiV2.registerUserAndDeviceError(alphaTestUser, deviceRegistrationRequest, 400);

        THEN("The platform will return a Bad Request 400");
        AND("Error message contains the correct code");
        assertEquals(REQUEST_VALIDATION, error.getCode());

        DONE();
    }
}
