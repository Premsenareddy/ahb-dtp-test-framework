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
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceLoginRequest;
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
import java.util.concurrent.TimeUnit;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.HEADER_X_DEVICE_ID;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.HEADER_X_JWS_SIGNATURE;
import static uk.co.deloitte.banking.api.test.BDDUtils.WHEN;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_IDEMPOTENCY_KEY;

@Prototype
@Slf4j
public class AuthenticateApi extends BaseApi {

    public static final String BEARER = "Bearer ";
    public static final String SCOPE = "Scope";
    public static final String TOKEN_TYPE = "TokenType";
    public static final String ACCESS_TOKEN = "AccessToken";
    public static final int MAX_DESIRED_RESPONSE_TIME = 2000;

    private static final String INITIATE_RESET_PASSWORD_ENDPOINT = "/protected/v2/users/passwords/reset";

    @Inject
    AuthConfiguration authConfiguration;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private CertificateProtectedApi certificateProtectedApi;

    @Inject
    private BankingConfig bankingConfig;

    final ObjectMapper ob = new ObjectMapper();

    @Inject
    private CertificateApi certificateApi;



    /**
     * @param alphaTestUser
     * @return
     */
    public LoginResponse authenticateDevice(final AlphaTestUser alphaTestUser) {

        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceId(alphaTestUser.getDeviceId())
                .deviceHash(alphaTestUser.getDeviceHash())
                .build();

        final Response response = given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post(authConfiguration.getBasePath() + "/protected/v2/devices");


        long responseTimeIn = response.getTimeIn(TimeUnit.MILLISECONDS);
        if(responseTimeIn > MAX_DESIRED_RESPONSE_TIME){
            log.warn("authenticateDevice:: responded over max time [{}]", responseTimeIn);
        }else {
            log.info("authenticateDevice:: responded in [{}]", responseTimeIn);
        }

        return response
                .then().log().all().statusCode(201).assertThat()
                .body("Scope", equalTo("device"))
                .body("TokenType", equalTo("Bearer"))
                .body("AccessToken", notNullValue())
                .extract().body().as(LoginResponse.class);
    }

    public ErrorResponse authenticateDeviceV2Negative(final AlphaTestUser alphaTestUser, final DeviceLoginRequest deviceLoginRequest, int statusCode) {


        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(deviceLoginRequest)
                .when()
                .post(authConfiguration.getBasePath() + "/protected/v2/devices")
                .then().log().all().statusCode(statusCode)
                .extract().body().as(ErrorResponse.class);
    }

    public User getUser(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getBasePath() + "/internal/v2/users")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(User.class);
    }

    public DeviceStatusResponseV2 deviceStatus(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getBasePath() + "/protected/v2/device/"+  alphaTestUser.getUserId())
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(DeviceStatusResponseV2.class);
    }

    public User getUser(final AlphaTestUser alphaTestUser, String token) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + token)
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getBasePath() + "/internal/v2/users")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(User.class);
    }

    public User patchUser(final AlphaTestUser alphaTestUser, User user) throws JsonProcessingException {

        String signedSignature = "";
        if (alphaTestUser.getPublicKeyBase64() != null) {
            String payload = ob.writeValueAsString(user);
            signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

            this.certificateApi.validateCertificate(alphaTestUser,
                    payload,
                    signedSignature,
                    204);
        }
            return given()
                    .config(config)
                    .log().all()
                    .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                    .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                    .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                    .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                    .contentType(ContentType.JSON)
                    .when()
                    .body(user)
                    .patch(authConfiguration.getBasePath() + "/internal/v2/users")
                    .then().log().all().statusCode(200).assertThat()
                    .extract().body().as(User.class);
    }

    public ValidatableResponse patchUserV1(final AlphaTestUser alphaTestUser, UpdateUserRequestV1 user) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .patch(authConfiguration.getBasePath() + "/internal/v2/users")
                .then().log().all();
    }

    public ValidatableResponse patchUserV2(final AlphaTestUser alphaTestUser, UpdateUserRequestV1 user) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .patch(authConfiguration.getBasePath() + "/internal/v2/users")
                .then().log().all();
    }


    public User patchUserScope(final AlphaTestUser alphaTestUser, UserScope user) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .patch(authConfiguration.getBasePath() + "/internal/v2/users")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(User.class);
    }

    public ResetPasswordResponse initiateResetPassword(final AlphaTestUser alphaTestUser, ResetPasswordRequest request) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())

                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + INITIATE_RESET_PASSWORD_ENDPOINT)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ResetPasswordResponse.class);
    }

    public OBErrorResponse1 initiateResetPasswordNegativeFlow(final AlphaTestUser alphaTestUser, ResetPasswordRequest request, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + INITIATE_RESET_PASSWORD_ENDPOINT)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public ValidateResetPasswordResponse validateResetPasswordOtp(final AlphaTestUser alphaTestUser, ValidateResetPasswordRequest request) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/passwords/validate")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ValidateResetPasswordResponse.class);
    }

    public OBErrorResponse1 validateResetPasswordOtpError(final AlphaTestUser alphaTestUser, ValidateResetPasswordRequest request, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/passwords/validate")
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 validateResetPasswordOtpJSON(JSONObject request, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request.toString())
                .post(authConfiguration.getBasePath() + "/protected/v2/users/passwords/validate")
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public UserDto updateForgottenPassword(final AlphaTestUser alphaTestUser, UpdateForgottenPasswordRequestV1 request) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + "/protected/v2/users/passwords")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UserDto.class);
    }


    public OBErrorResponse1 updateUserFail(final AlphaTestUser alphaTestUser, User user, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .patch(authConfiguration.getBasePath() + "/internal/v2/users")
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }


    /**
     * @param alphaTestUser
     * @return
     */
    public LoginResponse loginDevice(final AlphaTestUser alphaTestUser) {

        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceHash(alphaTestUser.getDeviceHash())
                .deviceId(alphaTestUser.getUserId())
                .build();

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + "/protected/v2/devices/login")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(LoginResponse.class);
    }


    public LoginResponse loginUser(final AlphaTestUser alphaTestUser) {

        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceHash(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getUserId())
                .build();

        final Response post = given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + "/protected/v2/devices/login");

        return post
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(LoginResponse.class);
    }

    //Replaced with registerNewDevice
    @Deprecated
    public LoginResponseV1 loginUserViaTelephoneNumber(final AlphaTestUser alphaTestUser) {

        final UserLoginRequest user = UserLoginRequest.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        String payload = null;
        try {
            payload = ob.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", user);
        }

        final String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        payload,
                        alphaTestUser.getPrivateKeyBase64());

        this.certificateApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/login")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(LoginResponseV1.class);
    }

    public LoginResponseV1 registerNewDevice(final AlphaTestUser alphaTestUser) {

        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userRegisterDeviceRequestV1)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/devices")
                .then().log().all().statusCode(201).assertThat()
                .extract().body().as(LoginResponseV1.class);
    }

    public UserLoginResponseV2 loginExistingUserProtected(UserLoginRequestV2 login,AlphaTestUser alphaTestUser) {
        String payload = null;
        try {
            payload = ob.writeValueAsString(login);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", login);
        }

        final String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        payload,
                        alphaTestUser.getPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        final Response post = given()
                .config(config)
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/login");
        return post
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(UserLoginResponseV2.class);
    }

    public void registerNewDeviceNotImplemented(final AlphaTestUser alphaTestUser) {

        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userRegisterDeviceRequestV1)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/devices")
                .then().log().all().statusCode(501).assertThat();
    }

    public OBErrorResponse1 updateForgottenPasswordErrorResponse(final AlphaTestUser alphaTestUser, UpdateForgottenPasswordRequestV1 request, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + "/protected/v2/users/passwords")
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public void updateForgottenPasswordVoidResponse(final AlphaTestUser alphaTestUser, UpdateForgottenPasswordRequestV1 request, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + "/protected/v2/users/passwords")
                .then().log().all().statusCode(statusCode).assertThat();
    }

    public OBErrorResponse1 updateForgottenPasswordInvalidJSON(final AlphaTestUser alphaTestUser, JSONObject request, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request.toString())
                .put(authConfiguration.getBasePath() + "/protected/v2/users/passwords")
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public void loginUserViaTelephoneNumberVoid(final AlphaTestUser alphaTestUser, final UserLoginRequest userLoginRequest, int statusCode) {


        String payload = null;
        try {
            payload = ob.writeValueAsString(userLoginRequest);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", userLoginRequest);
        }

        final String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        payload,
                        alphaTestUser.getPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        given()
            .config(config)
            .log().all()
            .header(X_API_KEY, authConfiguration.getApiKey())
            .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
            .header(HEADER_X_JWS_SIGNATURE, signedSignature)
            .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
            .contentType(ContentType.JSON)
            .when()
            .body(userLoginRequest)
            .post(authConfiguration.getBasePath() + "/protected/v2/users/login")
            .then().log().all().statusCode(statusCode).assertThat();
    }

    public ValidatableResponse loginUserViaTelephoneNumberError(final AlphaTestUser alphaTestUser, final UserLoginRequest userLoginRequest, int statusCode) throws JsonProcessingException {

        String payload = null;
        try {
            payload = ob.writeValueAsString(userLoginRequest);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", userLoginRequest);
        }

        final String signedSignature =
                alphaKeyService.generateJwsSignature(alphaTestUser,
                        payload,
                        alphaTestUser.getPrivateKeyBase64());

        this.certificateProtectedApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequest)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/login")
                .then().log().all().statusCode(statusCode).assertThat();
    }

    public void loginUserViaTelephoneNumberInternalVoid(final AlphaTestUser alphaTestUser, final UserLoginRequest userLoginRequest, int statusCode) {

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequest)
                .post(authConfiguration.getBasePath() + "/internal/v2/users/login")
                .then().log().all().statusCode(statusCode);
    }

    public LoginResponse loginUserViaEmail(final AlphaTestUser alphaTestUser) throws JsonProcessingException {

        final UserLoginRequest user = UserLoginRequest.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .email(alphaTestUser.getUserEmail())
                .build();

        String signedSignature = "";
        if (alphaTestUser.getPublicKeyBase64() != null) {
            String payload = ob.writeValueAsString(user);
            signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

            this.certificateApi.validateCertificate(alphaTestUser, payload, signedSignature, 204);
        }

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/login")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(LoginResponse.class);
    }

    public OBErrorResponse1 loginUserViaEmailError(final UserLoginRequest userLoginRequest, int StatusCode) {


        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequest)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/login")
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 registerUserFail(final AlphaTestUser alphaTestUser, UserRegistrationRequestV1 request,int statuscode){

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .patch(authConfiguration.getBasePath() + "/internal/v2/users")
                .then()
                .log().all()
                .statusCode(statuscode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 registerUserFailJsonObj(final AlphaTestUser alphaTestUser, JSONObject request,int statusCode){

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .patch(authConfiguration.getBasePath() + "/internal/v2/users")
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    //TODO Should we have a loginResponseV2?
    public OBErrorResponse1 registerNewDeviceError(final AlphaTestUser alphaTestUser, int statusCode) {
        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .email(alphaTestUser.getUserEmail())
                .build();

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userRegisterDeviceRequestV1)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/devices")
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 registerNewDeviceError(final AlphaTestUser alphaTestUser, final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userRegisterDeviceRequestV1)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/devices")
                .then().log().all().statusCode(400).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    /*
    TODO:: Remove as logic should not be in the api
     */
    @Deprecated
    public void loginUserViaTelephoneNumberNotFound(final AlphaTestUser alphaTestUser) {

        final UserLoginRequest user = UserLoginRequest.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .post(authConfiguration.getBasePath() + "/protected/v2/users/login")
                .then().log().all()
                .statusCode(401);
    }


    @Deprecated
    public void upgradeAccountAndLoginWithAwait(AlphaTestUser alphaTestUser, String scope) {
        WHEN("Client calls api to update user status " + scope);

        String sn = scope.toUpperCase();

        User user = this.patchUserScope(alphaTestUser,
                UserScope.builder()
                        .sn(sn)
                        .build());


        Assertions.assertNotNull(user);
        assertEquals(user.getSn(), sn);

        WHEN("Scope is updated " + user.getSn());

        LoginResponse loginResponse = this.loginUser(alphaTestUser);
        alphaTestUser.setLoginResponse(loginResponse);

    }


    public JSONObject buildJSONObjectForMissingMandatoryField(String resetPasswordHash, String simulatedOtpPasswordReset) throws JSONException {
        return new JSONObject() {
            {
                put("Otp", simulatedOtpPasswordReset);
                put("Hash", resetPasswordHash);
            }
        };
    }
}
