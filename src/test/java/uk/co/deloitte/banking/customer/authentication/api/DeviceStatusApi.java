package uk.co.deloitte.banking.customer.authentication.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Prototype;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.banking.model.OBWriteCardWithdrawalResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DisableDeviceRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DisabledDeviceStatusV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ErrorResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ResetPasswordResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateForgottenPasswordRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.User;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserDto;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRegisterDeviceRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRegistrationRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserScope;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ValidateResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ValidateResetPasswordResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.customer.authentication.scenarios.DeviceStatusResponseV2;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.HEADER_X_DEVICE_ID;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.HEADER_X_JWS_SIGNATURE;
import static uk.co.deloitte.banking.api.test.BDDUtils.WHEN;
import static uk.co.deloitte.banking.http.common.HttpConstants.*;

@Prototype
@Slf4j
public class DeviceStatusApi extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;

    private static final String WEBHOOKS_DISABLE_DEVICE = "/webhooks/v1/device/disable/";
    private static final String WEBHOOKS_DEVICE_STATUS = "/webhooks/v1/device/status/";

    public DisabledDeviceStatusV2 disableDeviceWebhooks(String deviceId,
                                                                       String customerId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getDisableDeviceApiKey())
                .contentType(ContentType.JSON)
                .body(createDisableDeviceRequest(deviceId, customerId))
                .when()
                .patch(authConfiguration.getBasePath() + WEBHOOKS_DISABLE_DEVICE)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(DisabledDeviceStatusV2.class);
    }

    private DisableDeviceRequestV2 createDisableDeviceRequest(String deviceId, String customerId) {
        return DisableDeviceRequestV2.builder().deviceId(deviceId).customerId(customerId).build();
    }

    public DeviceStatusResponseV2 getdeviceStatusWebhooks(String customerId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getDisableDeviceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getBasePath() + WEBHOOKS_DEVICE_STATUS + customerId)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(DeviceStatusResponseV2.class);
    }

}
