package uk.co.deloitte.banking.customer.authentication.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.CardPinValidationRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantRegisterDeviceRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DependantValidateResetPasswordRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceRegistrationRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ResetPasswordResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthInitiateRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.StepUpAuthRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateForgottenPasswordRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UpdateUserRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.User;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserDto;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRegisterDeviceRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserRelationshipWriteRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserScope;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ValidateResetPasswordRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.ValidateResetPasswordResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.api.CertificateProtectedApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.HEADER_X_DEVICE_ID;
import static uk.co.deloitte.banking.account.api.common.HttpConstants.HEADER_X_JWS_SIGNATURE;
import static uk.co.deloitte.banking.api.test.BDDUtils.WHEN;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_IDEMPOTENCY_KEY;

@Singleton
@Slf4j
public class AuthenticateApiV2 extends BaseApi {

    public static final String BEARER = "Bearer ";
    public static final String SCOPE = "Scope";
    public static final String TOKEN_TYPE = "TokenType";
    public static final String ACCESS_TOKEN = "AccessToken";
    public static final int MAX_DESIRED_RESPONSE_TIME = 2000;

    @Inject
    AuthConfiguration authConfiguration;


    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private CertificateProtectedApi certificateProtectedApi;

    final ObjectMapper ob = new ObjectMapper();

    private final static String PROTECTED_V2_DEVICES = "/protected/v2/devices";
    private final static String PROTECTED_V2_DEVICES_LOGIN = "/protected/v2/devices/login";
    private final static String PROTECTED_V2_USERS_DEVICES = "/protected/v2/users/devices";
    private final static String PROTECTED_V2_USERS_LOGIN = "/protected/v2/users/login";
    private final static String PROTECTED_V2_USERS_PASSWORDS = "/protected/v2/users/passwords";
    private final static String PROTECTED_V2_USERS_PASSWORDS_RESET = "/protected/v2/users/passwords/reset";
    private final static String PROTECTED_V2_USERS_PASSWORDS_VALIDATE = "/protected/v2/users/passwords/validate";
    private final static String PROTECTED_V2_RELATIONSHIPS_USERS_DEVICES = "/protected/v2/relationships/users/devices";
    private final static String PROTECTED_V2_RELATIONSHIPS_USERS_PASSWORDS_VALIDATE = "/protected/v2/relationships/users/passwords/validate";

    private final static String INTERNAL_V2_USERS = "/internal/v2/users";
    private final static String INTERNAL_V2_USERS_TOKENS = "/internal/v2/users/tokens";
    private final static String INTERNAL_V2_USERS_LOGIN = "/internal/v2/users/login";
    private final static String INTERNAL_V2_USERS_CREDENTIALS = "/internal/v2/users/credentials";
    private final static String INTERNAL_V2_USERS_CARDS_ID_TOKENS = "/internal/v2/users/cards/{cardId}/tokens";
    private final static String INTERNAL_V2_RELATIONSHIPS_USERS = "/internal/v2/relationships/users";
    private final static String INTERNAL_V2_RELATIONSHIPS_ID_USERS_PASSWORDS_RESET= "/internal/v2/relationships/{relationshipId}/users/passwords/reset";

    /**
     * @param alphaTestUser
     * @return
     */
    public LoginResponseV1 registerUserAndDevice(final AlphaTestUser alphaTestUser) {

        final DeviceRegistrationRequestV1 deviceRegistrationRequest = DeviceRegistrationRequestV1.builder()
                .deviceId(alphaTestUser.getDeviceId())
                .deviceHash(alphaTestUser.getDeviceHash())
                .build();

        return given().log().all()
                .config(config)
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .body(deviceRegistrationRequest)
                .when()
                .post(authConfiguration.getBasePath() + PROTECTED_V2_DEVICES)
                .then().log().ifError().statusCode(201).assertThat()
                .body("Scope", equalTo("device"))
                .body("TokenType", equalTo("Bearer"))
                .body("AccessToken", notNullValue())
                .extract().body().as(LoginResponseV1.class);
    }

    public OBErrorResponse1 registerUserAndDeviceError(final AlphaTestUser alphaTestUser,
                                                       DeviceRegistrationRequestV1 deviceRegistrationRequestV1,
                                                       int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .body(deviceRegistrationRequestV1)
                .when()
                .post(authConfiguration.getBasePath() + PROTECTED_V2_DEVICES)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public UserLoginResponseV2 registerDependantUserDevice(final AlphaTestUser alphaTestUser,
                                                           final DependantRegisterDeviceRequestV2 request) {

        return given()
                .config(config)
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(authConfiguration.getBasePath() + PROTECTED_V2_RELATIONSHIPS_USERS_DEVICES)
                .then().log().all().statusCode(201).assertThat()
                .body("Scope", equalTo("device"))
                .body("TokenType", equalTo("Bearer"))
                .body("AccessToken", notNullValue())
                .extract().body().as(UserLoginResponseV2.class);
    }

    public OBErrorResponse1 registerDependantUserDeviceError(final AlphaTestUser alphaTestUser,
                                                             final DependantRegisterDeviceRequestV2 request,
                                                             int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(authConfiguration.getBasePath() + PROTECTED_V2_RELATIONSHIPS_USERS_DEVICES)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 registerDependantUserDeviceErrorJson(final AlphaTestUser alphaTestUser,
                                                                 final JSONObject request,
                                                                 int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(authConfiguration.getBasePath() + PROTECTED_V2_RELATIONSHIPS_USERS_DEVICES)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }


    public LoginResponseV1 loginDevice(final AlphaTestUser alphaTestUser) {

        //TODO::Rename device Id to userId
        final DeviceLoginRequest login = DeviceLoginRequest.builder()
                .deviceHash(alphaTestUser.getDeviceHash())
                .deviceId(alphaTestUser.getUserId())
                .build();

        final Response post = given().log().all()
                .config(config)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_DEVICES_LOGIN);

        return post
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(LoginResponseV1.class);
    }

    public ValidatableResponse loginDeviceValidatable(final AlphaTestUser alphaTestUser, DeviceLoginRequest login) {

        return given()
                .config(config)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_DEVICES_LOGIN)
                .then()
                .log().all();
    }

    public User patchUserCredentials(final AlphaTestUser alphaTestUser, UpdateUserRequestV1 updateUserRequestV1) {
        try {
            String signedSignature = "";
            if (alphaTestUser.getPublicKeyBase64() != null) {
                String payload = ob.writeValueAsString(updateUserRequestV1);
                signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

                this.certificateApi.validateCertificate(alphaTestUser,
                        payload,
                        signedSignature,
                        204);
            }

            return given()
                    .config(config)
                    .log().all()
                    .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                    .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                    .header(AUTHORIZATION, BEARER + alphaTestUser.getJwtToken())
                    .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                    .contentType(ContentType.JSON)
                    .when()
                    .body(updateUserRequestV1)
                    .patch(authConfiguration.getBasePath() + INTERNAL_V2_USERS_CREDENTIALS)
                    .then().log().all().statusCode(200).assertThat()
                    .extract().body().as(User.class);
        } catch (JsonProcessingException e) {
            log.error("Error patchUser", e);
            return null;
        }
    }

    public OBErrorResponse1 patchUserCredentialsError(final AlphaTestUser alphaTestUser,
                                                      UpdateUserRequestV1 updateUserRequestV1,
                                                      int statusCode) {
        try {
            String signedSignature = "";
            if (alphaTestUser.getPublicKeyBase64() != null) {
                String payload = ob.writeValueAsString(updateUserRequestV1);
                signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

                this.certificateApi.validateCertificate(alphaTestUser,
                        payload,
                        signedSignature,
                        204);
            }

            final Response post = given()
                    .config(config)
                    .log().all()
                    .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                    .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                    .header(AUTHORIZATION, BEARER + alphaTestUser.getJwtToken())
                    .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                    .contentType(ContentType.JSON)
                    .when()
                    .body(updateUserRequestV1)
                    .patch(authConfiguration.getBasePath() + INTERNAL_V2_USERS_CREDENTIALS);

            return post
                    .then().log().all().statusCode(statusCode).assertThat()
                    .extract().body().as(OBErrorResponse1.class);
        } catch (JsonProcessingException e) {
            log.error("Error patchUser", e);
            return null;
        }
    }

    public OBErrorResponse1 patchUserError(AlphaTestUser alphaTestUser, UpdateUserRequestV1 updateUserRequestV1, int statusCode) {
        return patchUserError(alphaTestUser, updateUserRequestV1, statusCode, false);
    }

    public OBErrorResponse1 patchUserError(AlphaTestUser alphaTestUser, UpdateUserRequestV1 updateUserRequestV1,
                                           int statusCode, boolean validate) {
        try {
            String signedSignature = "";

            if (validate && alphaTestUser.getPublicKeyBase64() != null) {

                String payload = ob.writeValueAsString(updateUserRequestV1);
                signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

                this.certificateApi.validateCertificate(alphaTestUser,
                        payload,
                        signedSignature,
                        204);
            }

            return given()
                    .config(config)
                    .log().all()
                    .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                    .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                    .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                    .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                    .contentType(ContentType.JSON)
                    .when()
                    .body(updateUserRequestV1)
                    .patch(authConfiguration.getBasePath() + INTERNAL_V2_USERS)
                    .then()
                    .log().all()
                    .statusCode(statusCode).assertThat()
                    .extract().body().as(OBErrorResponse1.class);

        } catch (JsonProcessingException e) {
            log.error("Error patchUser", e);

            return null;
        }
    }

    public User patchUser(final AlphaTestUser alphaTestUser, User user) throws JsonProcessingException {
        String signedSignature = "";
        String payload = ob.writeValueAsString(user);
        signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

        this.certificateApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        return given()
                .config(config)
                //.log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .patch(authConfiguration.getBasePath() + INTERNAL_V2_USERS)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(User.class);
    }

    public ValidatableResponse patchUser(final AlphaTestUser alphaTestUser, UpdateUserRequestV1 user) {
        return this.patchUser(alphaTestUser, user, true);
    }

    public ValidatableResponse patchUser(final AlphaTestUser alphaTestUser,
                                         UpdateUserRequestV1 user,
                                         boolean validate) {

        try {
            String signedSignature = "";
            if (validate && alphaTestUser.getPublicKeyBase64() != null) {
                String payload = ob.writeValueAsString(user);
                signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

                this.certificateApi.validateCertificate(alphaTestUser,
                        payload,
                        signedSignature,
                        204);
            }

            return given()
                    .config(config)
                    //.log().all()
                    .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                    .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                    .header(AUTHORIZATION, BEARER + alphaTestUser.getJwtToken())
                    .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                    .contentType(ContentType.JSON)
                    .when()
                    .body(user)
                    .patch(authConfiguration.getBasePath() + INTERNAL_V2_USERS)
                    .then().log().ifError();
        } catch (JsonProcessingException e) {
            log.error("Error patchUser", e);
            return null;
        }


    }

    public User patchUserScope(final AlphaTestUser alphaTestUser, UserScope user) {
        return given()
                .config(config)
//                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .patch(authConfiguration.getBasePath() + INTERNAL_V2_USERS)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(User.class);
    }

    @Deprecated
    public ResetPasswordResponse initiateResetPassword(final AlphaTestUser alphaTestUser,
                                                       ResetPasswordRequest request) {
        return given()
                .config(config)
                //.log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_PASSWORDS_RESET)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(ResetPasswordResponse.class);
    }

    public OBErrorResponse1 initiateResetPasswordError(AlphaTestUser alphaTestUser,
                                                       ResetPasswordRequest request,
                                                       int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_PASSWORDS_RESET)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    @Deprecated
    public ValidateResetPasswordResponse validateResetPasswordOtp(final AlphaTestUser alphaTestUser,
                                                                  ValidateResetPasswordRequest request) {
        return given()
                .config(config)
                //.log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_PASSWORDS_VALIDATE)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(ValidateResetPasswordResponse.class);
    }

    public OBErrorResponse1 validateResetPasswordOtpError(final AlphaTestUser alphaTestUser,
                                                          ValidateResetPasswordRequest request,
                                                          int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_PASSWORDS_VALIDATE)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public UserDto updateForgottenPassword(final AlphaTestUser alphaTestUser,
                                           UpdateForgottenPasswordRequestV1 request) {
        return given()
                .config(config)
                //.log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + PROTECTED_V2_USERS_PASSWORDS)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(UserDto.class);
    }

    public OBErrorResponse1 updateForgottenPasswordError(final AlphaTestUser alphaTestUser,
                                                         UpdateForgottenPasswordRequestV1 request,
                                                         int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + PROTECTED_V2_USERS_PASSWORDS)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 updateUserFail(final AlphaTestUser alphaTestUser,
                                           User user,
                                           int statusCode) throws JsonProcessingException {

        String signedSignature = "";
        if (alphaTestUser.getPublicKeyBase64() != null) {
            try {
                String payload = ob.writeValueAsString(user);
                signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

                this.certificateApi.validateCertificate(alphaTestUser,
                        payload,
                        signedSignature,
                        204);

            } catch (JsonProcessingException e) {
                log.error("ERROR loginUserViaTelephoneNumber", e);
            }
        }

        return given()
                .config(config)
                //.log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .patch(authConfiguration.getBasePath() + INTERNAL_V2_USERS)
                .then().log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public UserLoginResponseV2 loginUser(final AlphaTestUser alphaTestUser) {
        final UserLoginRequestV2 login = UserLoginRequestV2.builder()
                .password(alphaTestUser.getUserPassword())
                .userId(alphaTestUser.getUserId())
                .build();

        return this.loginUser(alphaTestUser, login, alphaTestUser.getDeviceId(), false);
    }

    public UserLoginResponseV2 loginUser(final AlphaTestUser alphaTestUser,
                                         UserLoginRequestV2 login,
                                         String deviceId,
                                         boolean validate) {

        if (validate) {
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

            this.certificateApi.validateCertificate(alphaTestUser,
                    payload,
                    signedSignature,
                    204);
        }

        final Response post = given()
                .config(config)
                .log().all()
                .header(HEADER_X_DEVICE_ID, deviceId)
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + INTERNAL_V2_USERS_LOGIN);

        return post
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UserLoginResponseV2.class);
    }

    public OBErrorResponse1 loginUserError(final AlphaTestUser alphaTestUser,
                                           UserLoginRequestV2 login,
                                           String deviceId,
                                           boolean validate) {

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

        this.certificateApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);


        final Response post = given()
                .config(config)
                //.log().all()
                .header(HEADER_X_DEVICE_ID, deviceId)
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + INTERNAL_V2_USERS_LOGIN);

        return post
                .then().log().ifError().statusCode(401).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public ValidatableResponse loginUserError(final AlphaTestUser alphaTestUser,
                                              UserLoginRequestV2 login,
                                              String deviceId,
                                              boolean validate,
                                              String jwt) {

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

        this.certificateApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        final Response post = given()
                .config(config)
                //.log().all()
                .header(HEADER_X_DEVICE_ID, deviceId)
                .header(AUTHORIZATION, BEARER + jwt)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + INTERNAL_V2_USERS_LOGIN);

        return post
                .then().log().ifError().statusCode(401).assertThat();
    }

    public ValidatableResponse loginUserValidatable(final AlphaTestUser alphaTestUser,
                                                    UserLoginRequestV2 login,
                                                    String deviceId,
                                                    boolean validate) {

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

        this.certificateApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        final Response post = given()
                .config(config)
                .log().all()
                .header(HEADER_X_DEVICE_ID, deviceId)
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + INTERNAL_V2_USERS_LOGIN);

        return post.then().log().ifError();
    }

    public UserLoginResponseV2 loginUserProtected(final AlphaTestUser alphaTestUser,
                                                  UserLoginRequestV2 login,
                                                  String deviceId,
                                                  boolean validate) {

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

        this.certificateApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);


        final Response post = given()
                .config(config)
                .log().all()
                .header(HEADER_X_DEVICE_ID, deviceId)
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN);

        return post
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(UserLoginResponseV2.class);

    }

    public UserLoginResponseV2 loginUserViaTelephoneNumber(final AlphaTestUser alphaTestUser) {

        final UserLoginRequestV2 user = UserLoginRequestV2.builder()
                .password(alphaTestUser.getUserPassword())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();


        String signedSignature = "";
        if (alphaTestUser.getPublicKeyBase64() != null) {
            try {
                String payload = ob.writeValueAsString(user);
                signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

                this.certificateApi.validateCertificate(alphaTestUser,
                        payload,
                        signedSignature,
                        204);

            } catch (JsonProcessingException e) {
                log.error("ERROR loginUserViaTelephoneNumber", e);
            }
        }

        return given()
                .config(config)
                //.log().all()
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(UserLoginResponseV2.class);
    }

    public LoginResponseV1 registerNewDevice(final AlphaTestUser alphaTestUser) {

        final UserRegisterDeviceRequestV1 userRegisterDeviceRequestV1 = UserRegisterDeviceRequestV1.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .phoneNumber(alphaTestUser.getUserTelephone())
                .build();

        return given()
                .config(config)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userRegisterDeviceRequestV1)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_DEVICES)
                .then().log().ifError().statusCode(201).assertThat()
                .extract().body().as(LoginResponseV1.class);
    }

    public OBErrorResponse1 registerNewDeviceError(final AlphaTestUser alphaTestUser,
                                                   UserRegisterDeviceRequestV1 request,
                                                   int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_DEVICES)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }


    public OBErrorResponse1 loginUserViaTelephoneNumberError(final UserLoginRequest userLoginRequest, int statusCode) {

        return given()
                .config(config)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequest)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public ValidatableResponse loginUserViaTelephoneNumberError(final UserLoginRequest userLoginRequest) {

        return given()
                .config(config)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequest)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().ifError();
    }

    public LoginResponse loginUserViaEmail(final AlphaTestUser alphaTestUser) {

        final UserLoginRequest user = UserLoginRequest.builder()
                .password(alphaTestUser.getUserPassword())
                .deviceId(alphaTestUser.getDeviceId())
                .email(alphaTestUser.getUserEmail())
                .build();

        String signedSignature = "";
        try {
            String payload = ob.writeValueAsString(user);
            signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, payload);

            this.certificateApi.validateCertificate(alphaTestUser,
                    payload,
                    signedSignature,
                    204);

        } catch (JsonProcessingException e) {
            log.error("ERROR loginUserViaTelephoneNumber", e);
        }

        return given()
                .config(config)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(LoginResponse.class);
    }

    public OBErrorResponse1 loginUserViaEmailError(final UserLoginRequest userLoginRequest, int StatusCode) {


        return given()
                .config(config)
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequest)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().ifError().statusCode(StatusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void stepUpUserAuthInitiate(final AlphaTestUser alphaTestUser, final StepUpAuthInitiateRequest request) {
        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + INTERNAL_V2_USERS_TOKENS)
                .then().log().all().statusCode(204).assertThat();
    }

    public void validateUserStepUpAuth(final AlphaTestUser alphaTestUser, final StepUpAuthRequest request) {
        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + INTERNAL_V2_USERS_TOKENS)
                .then().log().all().statusCode(200);
    }

    public OBErrorResponse1 validateUserStepUpAuthError(final AlphaTestUser alphaTestUser,
                                                        final StepUpAuthRequest request, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + INTERNAL_V2_USERS_TOKENS)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public void resetChildPasscode(final AlphaTestUser alphaTestUser,
                                   UserRelationshipWriteRequest input,
                                   String relationshipId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .contentType(JSON)
                .body(input)
                .when()
                .put(authConfiguration.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_USERS_PASSWORDS_RESET)
                .then()
                .log().all()
                .statusCode(200).assertThat();
    }

    public OBErrorResponse1 resetChildPasscodeError(final AlphaTestUser alphaTestUser,
                                                    UserRelationshipWriteRequest input,
                                                    String relationshipId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .pathParam("relationshipId", relationshipId)
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .contentType(JSON)
                .body(input)
                .when()
                .put(authConfiguration.getBasePath() + INTERNAL_V2_RELATIONSHIPS_ID_USERS_PASSWORDS_RESET)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public UserLoginResponseV2 validateChildPasscode(final AlphaTestUser alphaTestUser,
                                                     DependantValidateResetPasswordRequestV2 input,
                                                     String deviceId) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HttpConstants.HEADER_X_DEVICE_ID, deviceId)
                .log().all()
                .contentType(JSON)
                .body(input)
                .when()
                .post(authConfiguration.getBasePath() + PROTECTED_V2_RELATIONSHIPS_USERS_PASSWORDS_VALIDATE)
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(UserLoginResponseV2.class);
    }

    public OBErrorResponse1 validateChildPasscodeError(final AlphaTestUser alphaTestUser,
                                                       DependantValidateResetPasswordRequestV2 input,
                                                       String deviceId, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HttpConstants.HEADER_X_DEVICE_ID, deviceId)
                .log().all()
                .contentType(JSON)
                .body(input)
                .when()
                .post(authConfiguration.getBasePath() + PROTECTED_V2_RELATIONSHIPS_USERS_PASSWORDS_VALIDATE)
                .then()
                .log().all()
                .statusCode(statusCode).assertThat()
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
                //.log().all()
                .header(X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(user)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().ifError()
                .statusCode(401);
    }

    @Deprecated
    public UserLoginResponseV2 upgradeAccountAndLoginWithAwait(AlphaTestUser alphaTestUser, String scope) {
        WHEN("Client calls api to update user status");

        User user = this.patchUserScope(alphaTestUser,
                UserScope.builder()
                        .sn(scope)
                        .build());


        Assertions.assertNotNull(user);
        assertEquals(user.getSn(), scope);


        UserLoginResponseV2 loginResponse = this.loginUser(alphaTestUser);
        assertEquals(loginResponse.getScope(), scope.toLowerCase());
        return loginResponse;


    }

    public LoginResponse createRelationshipAndUser(AlphaTestUser alphaTestUser, UserRelationshipWriteRequest request) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .post(authConfiguration.getBasePath() + INTERNAL_V2_RELATIONSHIPS_USERS)
                .then().log().all().statusCode(201).assertThat()
                .extract().body().as(LoginResponse.class);
    }

    public OBErrorResponse1 createRelationshipError(final AlphaTestUser alphaTestUser, JSONObject passwordToUse,
                                                    int statusCode) {
        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(JSON)
                .body(passwordToUse.toString())
                .when()
                .post(authConfiguration.getBasePath() + INTERNAL_V2_RELATIONSHIPS_USERS)
                .then()
                .log().all()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public void loginUserProtectedError(AlphaTestUser alphaTestUser, UserLoginRequestV2 userLoginRequestV2,
                                        int statusCode) {
        String payload = null;
        try {
            payload = ob.writeValueAsString(userLoginRequestV2);
        } catch (JsonProcessingException e) {
            log.error("ERROR:: Parsing login[{}]", userLoginRequestV2);
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
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequestV2)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().all().statusCode(statusCode).assertThat();
    }

    public OBErrorResponse1 loginUserProtectedError(String deviceId, UserLoginRequestV2 userLoginRequestV2,
                                                    int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_DEVICE_ID, deviceId)
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(userLoginRequestV2)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public void cardPinValidation(final AlphaTestUser alphaTestUser, final CardPinValidationRequest request,
                                  String cardId, String signedSignature) {

        given()
                .config(config)
                .pathParam("cardId", cardId)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + INTERNAL_V2_USERS_CARDS_ID_TOKENS)
                .then().log().all().statusCode(200);
    }

    public OBErrorResponse1 loginUserProtectedWithoutSignature(final AlphaTestUser alphaTestUser,
                                                               UserLoginRequestV2 login,
                                                               String deviceId) {

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

        this.certificateApi.validateCertificate(alphaTestUser,
                payload,
                signedSignature,
                204);

        final Response post = given()
                .config(config)
                .log().all()
                .header(HEADER_X_DEVICE_ID, deviceId)
                .header(HEADER_X_API_KEY, authConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(login)
                .post(authConfiguration.getBasePath() + PROTECTED_V2_USERS_LOGIN);

        return post
                .then().log().all().statusCode(401).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 cardPinValidationError(final AlphaTestUser alphaTestUser,
                                                   final CardPinValidationRequest request,
                                                   String cardId,
                                                   String signedSignature,
                                                   int statusCode) {
        return given()
                .config(config)
                .pathParam("cardId", cardId)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + INTERNAL_V2_USERS_CARDS_ID_TOKENS)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().as(OBErrorResponse1.class);
    }

    public void cardPinValidationErrorVoid(final AlphaTestUser alphaTestUser,
                                           final CardPinValidationRequest request,
                                           String cardId,
                                           String signedSignature,
                                           int statusCode) {
        given()
                .config(config)
                .pathParam("cardId", cardId)
                .log().all()
                .header(AUTHORIZATION, BEARER + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HEADER_X_IDEMPOTENCY_KEY, RequestUtils.generateIdempotentId())
                .header(HEADER_X_JWS_SIGNATURE, signedSignature)
                .header(HEADER_X_DEVICE_ID, alphaTestUser.getDeviceId())
                .contentType(ContentType.JSON)
                .when()
                .body(request)
                .put(authConfiguration.getBasePath() + INTERNAL_V2_USERS_CARDS_ID_TOKENS)
                .then().log().all()
                .statusCode(statusCode).assertThat();
    }
}
