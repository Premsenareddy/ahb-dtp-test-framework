package ahb.experience.onboarding.experienceOnboardUser.api;

import ahb.experience.onboarding.auth.api.*;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.DevSimConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;


@Singleton
@Slf4j
public class ExperienceAuthenticateApi extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;


    @Inject
    DevSimConfiguration devSimConfiguration;

    private final static String PUBLIC_DEVICES = "/onboarding/public/devices";
    private final static String PROTECTED_OTP = "/onboarding/protected/otp";
    private final static String PROTECTED_OTP_VALIDATE = "/onboarding/protected/otp/validate";
    //private final static String PROTECTED_MARKET_PLACE_DETAILS= "/onboarding/protected/marketplace-personal-details";
    private final static String PUBLIC_SIGNATURE_KEYS= "/signature/public/signature/keys";
    private final static String PUBLIC_SIGNATURE= "/signature/public/signature";
    private final static String PROTECTED_PASSCODE= "/onboarding/protected/customer/passcode";
    private final static String PROTECTED_PROFILE = "/onboarding/protected/profile";
    private final static String PUBLIC_LOGIN= "/onboarding/public/customer/login";
    private final static String API_OTPS= "/api/otps/";
    private final static String API_EMAILS= "/api/emails";
    private final static String PUBLIC_EMAILS= "/onboarding/public/emails";

    public ExperienceRegisterResponse registerDevice(final ExperienceTestUser experienceTestUser) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", experienceTestUser.getDeviceId());
            jsonObject.put("deviceHash", experienceTestUser.getDeviceHash());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .body(jsonObject.toString())
                .when()
                .post(authConfiguration.getExperienceBasePath() + PUBLIC_DEVICES)
                .then().log().ifError().statusCode(200).assertThat()
                .body("scope", equalTo("device"))
                .body("tokenType", equalTo("Bearer"))
                .body("accessToken", notNullValue())
                .extract().body().as(ExperienceRegisterResponse.class);
    }

    public void generateOtp(final ExperienceTestUser experienceTestUser, int statusCode, ExperienceRegisterResponse experienceRegisterResponse) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobileNumber", experienceTestUser.getMobileNumber());
            jsonObject.put("type", "REGISTRATION_TEXT");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("X-Request-Id", "TestUser-"+experienceTestUser.getDeviceId())
                .header("Authorization", "Bearer "+ experienceRegisterResponse.getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath() + PROTECTED_OTP)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body();

    }

    public OtpCO getOtpFromDevSimulator(String userId) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(devSimConfiguration.getBasePath()+API_OTPS+userId)
                .then().log().all().statusCode(200).assertThat()
                .body("Password", notNullValue())
                .extract().body().as(OtpCO.class);
    }

    public ExperienceKeys getSignatureKeys() {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceDevPath()+PUBLIC_SIGNATURE_KEYS)
                .then().log().all().statusCode(200).assertThat()
                .body("publicKey", notNullValue())
                .body("privateKey", notNullValue())
                .extract().body().as(ExperienceKeys.class);
    }

    public void validateOtp(final ExperienceTestUser experienceTestUser, ExperienceKeys experienceKeys, ExperienceRegisterResponse experienceRegisterResponse, int statusCode, OtpCO otpCO) {
        JSONObject jsonObject = new JSONObject();
        try {
            //jsonObject.put("otp", otpCO.getPassword());
            jsonObject.put("otp", "123456");
            jsonObject.put("deviceId", experienceTestUser.getDeviceId());
            jsonObject.put("mobileNumber", experienceTestUser.getMobileNumber());
            jsonObject.put("publicKey", experienceKeys.getPublicKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, "d8d4a69e-beb6-4878-be50-ee3455fc09f9")
                .header(AUTHORIZATION, "Bearer " + experienceRegisterResponse.getAccessToken())
                .queryParam("X-Request-Id","")
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath()+PROTECTED_OTP_VALIDATE)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body();

    }

    public ExperienceSignature fetchSignature(final ExperienceTestUser experienceTestUser,  ExperienceKeys experienceKeys, int statusCode){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", experienceTestUser.getDeviceId());
            jsonObject.put("passcode", "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("privateKey", experienceKeys.getPrivateKey())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceDevPath()+PUBLIC_SIGNATURE)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(ExperienceSignature.class);
    }

    public ExperienceRegisterResponse savePasscode(ExperienceTestUser experienceTestUser, ExperienceSignature experienceSignature, ExperienceRegisterResponse experienceRegisterResponse, int statusCode){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", experienceTestUser.getDeviceId());
            jsonObject.put("passcode", "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer "+ experienceRegisterResponse.getAccessToken())
                .header("x-jws-signature", experienceSignature.getSignature())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath()+PROTECTED_PASSCODE)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(ExperienceRegisterResponse.class);
    }

    public ExperienceProfile getProfile(ExperienceLoginResponse experienceLoginResponse){
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer "+ experienceLoginResponse.getToken().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath()+PROTECTED_PROFILE)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ExperienceProfile.class);

    }

    public ExperienceSignature fetchLoginSignature(final ExperienceTestUser experienceTestUser,  String bearerToken, ExperienceKeys experienceKeys, int statusCode){
        JSONObject jsonObject = new JSONObject();
        try {
            java.lang.reflect.Field changeMap = jsonObject.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObject,new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        //try {
            jsonObject.put("deviceId", experienceTestUser.getDeviceId());
            jsonObject.put("mobileNumber", experienceTestUser.getMobileNumber());
            jsonObject.put("passcode", "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("privateKey", experienceKeys.getPrivateKey())
                .header("Authorization", "Bearer "+bearerToken)
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceDevPath()+PUBLIC_SIGNATURE)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(ExperienceSignature.class);
    }

    public ExperienceLoginResponse Login(ExperienceTestUser experienceTestUser, ExperienceSignature experienceSignature, ExperienceRegisterResponse experienceRegisterResponse, int statusCode){

        return given()
            .config(config)
            .log().all()
            .header(X_API_KEY, authConfiguration.getExperienceApiKey())
            //.header("Authorization", "Bearer "+experienceLoginResponse.getAccessToken())
            .header("x-jws-signature", experienceSignature.getSignature())
            .header("x-device-id", experienceTestUser.getDeviceId())
            .header("x-Request-id", "Benz-928")
            .contentType(APPLICATION_JSON)
            .when()
            .body(experienceSignature.getPayload())
            .post(authConfiguration.getExperienceBasePath()+PUBLIC_LOGIN)
            .then().log().all()
            .statusCode(statusCode).assertThat()
            .extract().body().as(ExperienceLoginResponse.class);
    }
    public ExperienceLoginResponse Login(String strDeviceId, String strMobNumber, String strPasscode, String strSignature, int statusCode){

        JSONObject jsonObject = new JSONObject();
        try {
            java.lang.reflect.Field changeMap = jsonObject.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObject,new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        //try {
        jsonObject.put("deviceId", strDeviceId);
        jsonObject.put("mobileNumber", strMobNumber);
        jsonObject.put("passcode", strPasscode);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                //.header("Authorization", "Bearer "+experienceLoginResponse.getAccessToken())
                .header("x-jws-signature", strSignature)
                .header("x-device-id", strDeviceId)
                .header("x-Request-id", "Benz-928")
                .contentType(APPLICATION_JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath()+PUBLIC_LOGIN)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().body().as(ExperienceLoginResponse.class);
    }

    public ExperienceEmail generateEmailToken(ExperienceTestUser experienceTestUser, ExperienceRegisterResponse experienceRegisterResponse) {
        return given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer "+ experienceRegisterResponse.getAccessToken())
                .queryParam("email", experienceTestUser.getEmail())
                .contentType(ContentType.JSON)
                .when()
                .get(devSimConfiguration.getBasePath()+API_EMAILS)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ExperienceEmail.class);
    }

    public void verifyEmail(ExperienceEmail experienceEmail, int statusCode) {
        given()
                .config(config)
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .queryParam("token", experienceEmail.getToken())
                .queryParam("email", experienceEmail.getEmail())
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath()+PUBLIC_EMAILS)
                .then().statusCode(statusCode).assertThat();
    }


}
