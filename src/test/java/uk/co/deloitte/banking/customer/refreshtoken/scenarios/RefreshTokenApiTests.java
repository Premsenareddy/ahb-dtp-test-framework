package uk.co.deloitte.banking.customer.refreshtoken.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.RefreshRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.refreshtoken.api.RefreshTokenApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;


@Tag("BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RefreshTokenApiTests {

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private RefreshTokenApi refreshTokenApi;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    EnvUtils envUtils;

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser2;

    private final String NULL_ERROR_CODE = "REQUEST_VALIDATION";
    private final String UAE_ERROR_UNAUTHORIZED = "UAE.ERROR.UNAUTHORIZED";


    private void setupTestUser() {
        if (alphaTestUser == null) {
            alphaTestUser = alphaTestUserFactory.setupCustomer(new AlphaTestUser());
        }
    }

    private void setupDeviceScopedUser() {
        if (alphaTestUser2 == null){
            alphaTestUser2 = new AlphaTestUser();

            LoginResponseV1 loginResponse = authenticateApi.registerUserAndDevice(alphaTestUser2);
            parseLoginResponse(alphaTestUser2, loginResponse);

            LoginResponseV1 response = authenticateApi.loginDevice(alphaTestUser2);
            parseLoginResponse(alphaTestUser2, response);

            UpdateUserRequestV1 user = UpdateUserRequestV1.builder()
                    .phoneNumber(alphaTestUser2.getUserTelephone())
                    .mail(alphaTestUser2.getUserEmail())
                    .build();

            authenticateApi.patchUser(alphaTestUser2, user);
        }
    }

    @Test
    public void happy_path_retrieve_new_access_token_using_valid_refresh_token() {
        TEST("AHBDB-3874: AC1 Valid refresh token - 200 OK");
        TEST("AHBDB-4250: AC1 Positive Test - Happy Path Scenario - Valid refresh token - Devices/Refresh - 200 OK");
        setupTestUser();

        GIVEN("The client's JWT has expired");
        AND("They have a valid refresh token");

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        UserLoginResponseV2 newResponse =
                refreshTokenApi.refreshAccessToken(refreshRequest(alphaTestUser.getRefreshToken()));

        THEN("The platform will return a 200 with a new valid JWT token, same scope");
        assertEquals(alphaTestUser.getScope(), newResponse.getScope());
        DONE();
    }

    @Test
    public void happy_path_retrieve_new_access_token_using_valid_refresh_token_devices() {
        TEST("AHBDB-3874: AC1 Valid refresh token - 200 OK");
        TEST("AHBDB-4243: AC1 Positive Test - Happy Path Scenario - Valid refresh token - Users/Refresh - 200 OK");
        setupDeviceScopedUser();

        GIVEN("A customer has a scope of devices");
        AND("They have a valid refresh token");
        UserLoginResponseV2 getNewToken =
                refreshTokenApi.getNewAccessTokenDevices(refreshRequest(alphaTestUser2.getRefreshToken()));

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        assertEquals(alphaTestUser2.getScope(), getNewToken.getScope());
        THEN("The platform will return a 200 with a new valid JWT token, same scope");
        DONE();
    }

    @Test
    public void invalid_refresh_token_401_unauthorized() {
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4245: AC2 Negative Test - Invalid refresh token - Devices/Refresh - RefreshToken - 401 Unauthorized");
        setupTestUser();

        GIVEN("The client's JWT has expired");
        AND("They have an expired/invalid refresh token");

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        OBErrorResponse1 error = refreshTokenApi
                .getNewAccessTokenErrorResponse(refreshRequest(UUID.randomUUID().toString()), 401);

        THEN("The platform will return a 401 response");
        assertEquals(UAE_ERROR_UNAUTHORIZED, error.getCode());

        DONE();
    }

    @Test
    public void invalid_refresh_token_using_refresh_token_twice_401_unauthorized() {
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4247: AC2 Negative Test - Invalid refresh token - Users/Refresh - Using Refresh Token Twice - 401 Unauthorized");
        setupTestUser();

        GIVEN("The client's JWT has expired");
        AND("They have an expired/invalid refresh token");
        RefreshRequest refresh = refreshRequest(alphaTestUser.getRefreshToken());
        UserLoginResponseV2 response = refreshTokenApi.refreshAccessToken(refresh);
        parseLoginResponse(alphaTestUser, response);

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        OBErrorResponse1 error = refreshTokenApi.getNewAccessTokenErrorResponse(refresh, 401);

        THEN("The platform will return a 401 Unauthorized");
        assertEquals(UAE_ERROR_UNAUTHORIZED, error.getCode());
        DONE();
    }


    @Test
    public void invalid_refresh_token_null_400_response() {
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4248: AC2 Negative Test - Invalid refresh token - Users/Refresh - RefreshToken Null - 400 response");
        setupTestUser();

        GIVEN("The client's JWT has expired");
        AND("They pass a null refresh token");

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        OBErrorResponse1 error =
                refreshTokenApi.getNewAccessTokenErrorResponse(refreshRequest(null), 400);

        THEN("The platform will return a 400 response");
        assertEquals(NULL_ERROR_CODE, error.getCode());
        DONE();
    }

    @Test
    public void invalid_refresh_token_missing_mandatory_RefreshToken_400_response() throws JSONException {
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4249: AC2 Negative Test - Invalid refresh token - Users/Refresh - Missing Mandatory RefreshToken - 400 response");
        setupTestUser();

        GIVEN("The client's JWT has expired");
        AND("They have an expired/invalid refresh token");

        JSONObject obj = this.refreshTokenApi.getJsonObject("refreshToken");
        obj.remove("RefreshToken");

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        OBErrorResponse1 error = this.refreshTokenApi.getNewAccessTokenErrorResponse(obj, 400);

        THEN("The platform will return a 400 response");
        assertEquals(NULL_ERROR_CODE, error.getCode());
        DONE();
    }

    @Test
    public void invalid_refresh_token_devices_401_unauthorized() {
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4251: AC2 Negative Test - Invalid refresh token - Devices/Refresh - RefreshToken - 401 Unauthorized");
        setupTestUser();

        GIVEN("The client's JWT has expired");
        AND("They have an expired/invalid refresh token");

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        OBErrorResponse1 error = refreshTokenApi
                .getNewAccessTokenDevicesErrorResponse(refreshRequest(UUID.randomUUID().toString()), 401);

        THEN("The platform will return a 401 response");
        assertEquals(UAE_ERROR_UNAUTHORIZED, error.getCode());
        DONE();
    }

    @Test
    public void invalid_refresh_token_using_refresh_token_twice_devices_401_unauthorized() {
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4252: AC2 Negative Test - Invalid refresh token - Devices/Refresh - Using Refresh Token Twice - 401 Unauthorized");
        setupDeviceScopedUser();

        GIVEN("The client's JWT has expired");
        AND("They have an expired/invalid refresh token");
        RefreshRequest refreshRequest = refreshRequest(alphaTestUser2.getRefreshToken());

        UserLoginResponseV2 getNewToken =
                refreshTokenApi.getNewAccessTokenDevices(refreshRequest);
        assertEquals(alphaTestUser2.getScope(), getNewToken.getScope());
        parseLoginResponse(alphaTestUser2, getNewToken);

        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        OBErrorResponse1 error = refreshTokenApi.getNewAccessTokenDevicesErrorResponse(refreshRequest, 401);

        THEN("The platform will return a 401 Unauthorized");
        assertEquals(UAE_ERROR_UNAUTHORIZED, error.getCode());
        DONE();
    }

    @Test
    public void invalid_refresh_token_RefreshToken_null_devices_400_response() {
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4253: AC2 Negative Test - Invalid refresh token - Devices/Refresh - RefreshToken Null - 400 response");
        setupTestUser();

        GIVEN("The client's JWT has expired");
        WHEN("The client calls the DTP to obtain a new JWT with same scope");
        AND("They send a null refreshToken");
        OBErrorResponse1 error =
                refreshTokenApi.getNewAccessTokenDevicesErrorResponse(refreshRequest(null), 400);

        THEN("The platform will return a 400 response");
        assertEquals(NULL_ERROR_CODE, error.getCode());
        DONE();
    }

    @Test
    public void check_refresh_and_access_token_times() throws InterruptedException {
        envUtils.ignoreTestInEnv("Involves waiting 15 minutes for the refresh token to expire", Environments.ALL);
        int accessTokenLengthInSeconds = 300;
        int refreshTokenLengthInSeconds = 900;

        TEST("AHBDB-7479: Reduce Refresh Token to 15 minutes and Access token to 5 minutes");
        TEST("AHBDB-3874: AC2 Expired/Invalid refresh token - 401 unauthorized");
        TEST("AHBDB-4247: AC2 Negative Test - Invalid refresh token - Users/Refresh - Using Refresh Token Twice - 401 Unauthorized");
        setupTestUser();
        GIVEN("A customer wants to retrieve a new access and refresh token");
        RefreshRequest refreshRequest = RefreshRequest.builder()
                .refreshToken(alphaTestUser.getRefreshToken())
                .build();

        WHEN("They receive the tokens");
        UserLoginResponseV2 newToken = refreshTokenApi.refreshAccessToken(refreshRequest);
        int validLength = Integer.parseInt(newToken.getExpiresIn());

        THEN("The length of time they are valid for are 5 and 15 minutes respectively");
        assertTrue(298 <= validLength && validLength <= 300);

        NOTE("Waiting 5 minutes then attempting to use access token");
        TimeUnit.SECONDS.sleep(accessTokenLengthInSeconds);
        customerApiV2.getCurrentCustomerVoidError(alphaTestUser, 401);

        NOTE("Waiting an additional 10 minutes then attempting to refresh token");
        TimeUnit.SECONDS.sleep(refreshTokenLengthInSeconds - accessTokenLengthInSeconds);

        OBErrorResponse1 error = refreshTokenApi.getNewAccessTokenErrorResponse(refreshTokenApi
                .getJsonObject(alphaTestUser.getRefreshToken()), 401);
        assertEquals(UAE_ERROR_UNAUTHORIZED, error.getCode());
        DONE();
    }

    private RefreshRequest refreshRequest(String refreshToken) {
        return RefreshRequest.builder().refreshToken(refreshToken).build();
    }
}
