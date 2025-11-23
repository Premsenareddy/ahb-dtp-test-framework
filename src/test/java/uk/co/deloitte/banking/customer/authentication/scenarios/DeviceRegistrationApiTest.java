package uk.co.deloitte.banking.customer.authentication.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ErrorResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;


@Slf4j
@MicronautTest
public class DeviceRegistrationApiTest {

    @Inject
    private AuthenticateApi authenticateApiTest;

    @Inject
    private EnvUtils envUtils;

    @Inject
    AuthConfiguration authConfiguration;

    static  AlphaTestUser alphaTestUser = new AlphaTestUser();

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    private static final String SIZE_FOUR_FIFTY_ERROR = "size must be between 4 and 50";

    private static final String SIZE_EIGHT_HUNDRED = "size must be between 8 and 128";

    private static final String NULL_FIELD_ERROR = "must not be blank";

    @ParameterizedTest
    @ValueSource(strings = {"As8qv8LSLwkwUmx4ZckquSMVEL8nIPbe96WCofoKj8sWyPocyNB", "p1f", "1"})
    public void device_registration_negative_test_deviceId(String invalidId) {
        TEST("AHBDB-1673: Invalid Device ID, less than 4 characters greater than 50");
        TEST("device_registration_negative_test_deviceId " + invalidId);

        NOTE("USERId[" + alphaTestUser + "]");

        WHEN("The client has an invalid id");
        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceId(invalidId)
                .deviceHash(alphaTestUser.getDeviceHash())
                .build();

        NOTE("Invalid id " + alphaTestUser.getDeviceId());

        THEN("The client tries to register the device they receive a 400 response");
        final ErrorResponse response = this.authenticateApiTest.authenticateDeviceV2Negative(alphaTestUser, login, 400);

        THEN("The a 400 is returned with the error message");
        Assertions.assertTrue(response.getMessage().contains(SIZE_FOUR_FIFTY_ERROR), "Error message was not as expected, test expected : " + SIZE_FOUR_FIFTY_ERROR);

        DONE();
    }


    private void setupTestUser() {

            this.alphaTestUser = alphaTestUserFactory.setupUser(alphaTestUser);
    }

    @Test
    public void deviceStatus() {

        envUtils.ignoreTestInEnv("Test not deployed in SIT as of now", Environments.SIT);

        TEST("AHBDB-15407: get device status for user");
        setupTestUser();

        WHEN("The client has valid customer id");
        DeviceStatusResponseV2 responseV2 = this.authenticateApiTest.deviceStatus(alphaTestUser);
        Assertions.assertNotNull(responseV2);
        Assertions.assertEquals(responseV2.getDeviceStatusUsers().get(0).getStatus().toString(), "ENABLED");
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "ImPE2mUqtBiN7ivoDpCfft7kXBYvfqFoihw3EZ5z54MMYoeToDGbHTZkSkT8S4GNbFIy2t7WiJqq6bucxU9jZ1TGggfdsadfg4TJBKWUDXlbohgfdewsertyuiuytrewg",
            "Sccjh31", "1"})
    public void device_registration_negative_test_deviceHash(String invalidHash) {
        TEST("AHBDB-1679: Invalid Device Hash, less than 8 characters greater than 100");
        TEST("device_registration_negative_test_deviceHash " + invalidHash);

        NOTE("USERId[" + alphaTestUser + "]");

        WHEN("The client has an invalid deviceHash");
        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceId(alphaTestUser.getDeviceId())
                .deviceHash(invalidHash)
                .build();

        NOTE("Invalid Hash " + alphaTestUser.getDeviceHash());

        THEN("The client trues to registers device they receive a 400 response");
        final ErrorResponse response = this.authenticateApiTest.authenticateDeviceV2Negative(alphaTestUser, login, 400);

        THEN("The a 400 is returned with the error message");
        Assertions.assertTrue(response.getMessage().contains(SIZE_EIGHT_HUNDRED), "Error message was not as expected, test expected : " + SIZE_EIGHT_HUNDRED);

        DONE();
    }

    @Test
    public void device_registration_negative_test_missing_field_deviceId() {
        TEST("AHBDB-1680: User makes request with a missing required field, deviceId");
        TEST("device_registration_negative_test_missing_field_deviceId");
        NOTE("USERId[" + alphaTestUser + "]");

        WHEN("The client has an missing id");
        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceId(null)
                .deviceHash(alphaTestUser.getDeviceHash())
                .build();

        NOTE("Invalid id " + alphaTestUser.getDeviceId());

        THEN("The client trues to registers device they receive a 400 response");
        final ErrorResponse response = this.authenticateApiTest.authenticateDeviceV2Negative(alphaTestUser, login, 400);

        THEN("The a 400 is returned with the error message");
        Assertions.assertTrue(response.getMessage().contains(NULL_FIELD_ERROR), "Error message was not as expected, test expected : " + NULL_FIELD_ERROR);

        DONE();
    }

    @Test
    public void device_registration_negative_test_missing_field_deviceHash() {
        TEST("AHBDB-1681: User makes request with a missing required field, deviceHash");

        TEST("device_registration_negative_test_missing_field_deviceHash");


        NOTE("USERId[" + alphaTestUser + "]");

        WHEN("The client has a missing deviceHash");
        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceId(alphaTestUser.getDeviceId())
                .deviceHash(null)
                .build();

        NOTE("Invalid Hash " + alphaTestUser.getDeviceHash());

        THEN("The client trues to registers device they receive a 400 response");
        final ErrorResponse response = this.authenticateApiTest.authenticateDeviceV2Negative(alphaTestUser, login, 400);

        THEN("The a 400 is returned with the error message");
        Assertions.assertTrue(response.getMessage().contains(NULL_FIELD_ERROR), "Error message was not as expected, test expected : " + NULL_FIELD_ERROR);

        DONE();
    }

    @Test
    public void device_registration_negative_test_duplicate_user() {
        TEST("AHBDB-1858: User makes a second request after successfully registering with their first");

        TEST("device_registration_negative_test_duplicate_user");

        NOTE("USERId[" + alphaTestUser + "]");

        WHEN("Client registers device");
        final LoginResponse response = this.authenticateApiTest.authenticateDevice(alphaTestUser);

        THEN("The device gets registered with a device response containing a token");
        Assertions.assertNotNull(response);

        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceId(alphaTestUser.getDeviceId())
                .deviceHash(alphaTestUser.getDeviceHash())
                .build();

        WHEN("The client tries to register a second time they receive a 409 response");
        final ErrorResponse response2 = this.authenticateApiTest.authenticateDeviceV2Negative(alphaTestUser, login, 409);

        THEN("The a 400 is returned with the error message");
        Assertions.assertEquals(response2.getMessage(), "Device already exists");
        DONE();
    }
}