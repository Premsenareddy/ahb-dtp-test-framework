package ahb.experience.onboarding.childMarketPlace.api;

import ahb.experience.onboarding.ChildOnboardingDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.json.JSONException;
import ahb.experience.onboarding.request.child.*;
import ahb.experience.onboarding.response.Child.*;
import ahb.experience.onboarding.response.Child.ChildLogin.ChildLoginRes;
import uk.co.deloitte.banking.base.BaseStep;

import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Singleton
public class ChildApis extends BaseStep<ChildApis> {

    private final String SAVE_CHILD_URL = "/onboarding/protected/child";
    private final String CHILD_OTP_URL = "/api/otps/{childid}?X-Request-Id=Benz";
    private final String CHILD_KEYS_URL = "/signature/public/signature/keys";
    private final String CHILD_SIGNATURE_URL = "/signature/public/signature";
    private final String CHILD_DEVICE_REGISTRATION_URL = "/onboarding/public/devices/register";
    private final String CHILD_SAVE_PASSCODE = "/onboarding/protected/child/passcode";
    private final String CHILD_LOGIN_URL = "/onboarding/public/customer/login";


    public ChildApis saveChild(final ChildDetailsReqBody childDetailsReqBody, final String accessToken) throws JSONException {
        RestAssured.defaultParser = Parser.JSON;
        child = given()
                    .config(config)
                    .log().all()
                    .header("x-api-key", authConfiguration.getExperienceApiKey())
                    .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                    .contentType(ContentType.JSON)
                    .when()
                    .body(childDetailsReqBody)
                    .post(authConfiguration.getExperienceBasePath() + SAVE_CHILD_URL)
                    .then().log().all().statusCode(200).assertThat()
                    .extract().body().as(Child.class);

        return this;
    }

    public ChildApis getChildOTP(final ChildOTPReqBody childOTPReqBody, final String accessToken) {
        RestAssured.defaultParser = Parser.JSON;
        childOtpRes = given()
                        .config(config)
                        .log().all()
                        .pathParams("childid", child.getChildId())
                        .header("x-api-key", authConfiguration.getExperienceApiKey())
                        .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID)
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                        .contentType(ContentType.JSON)
                        .when()
                        .body(childOTPReqBody)
                        .get(devSimConfiguration.getBasePath()+CHILD_OTP_URL)
                        .then().log().all().statusCode(200).assertThat()
                        .extract().body().as(ChildOtpRes.class);

        return this;
    }

    public ChildApis getChildKeys() {
        RestAssured.defaultParser = Parser.JSON;
        childKeysRes = given()
                        .config(config)
                        .log().all()
                        .header("x-api-key", authConfiguration.getExperienceApiKey())
                        .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                        .accept("*/*")
                        .when()
                        .get(authConfiguration.getExperienceDevPath() +CHILD_KEYS_URL)
                        .then().log().all().statusCode(200).assertThat()
                        .extract().body().as(ChildKeysRes.class);

        return this;
    }

    public ChildApis childDeviceRegistration() {
        RestAssured.defaultParser = Parser.JSON;
        childDeviceRegRes = given()
                    .config(config)
                    .log().all()
                    .header("x-api-key", authConfiguration.getExperienceApiKey())
                    .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID + CHILD_DEVICE_ID)
                    .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                    .accept("*/*")
                    .contentType(ContentType.JSON)
                    .when()
                    .body(ChildDeviceRegistrationBody.builder()
                            .deviceId("4A7B2UIZL-FG34-45F9-I90O-401EC" + CHILD_DEVICE_ID)
                            .userId(child.getChildId())
                            .otp(childOtpRes.getPassword())
                            .publicKey(childKeysRes.getPublicKey())
                            .build())
                    .post(authConfiguration.getExperienceBasePath()+CHILD_DEVICE_REGISTRATION_URL)
                    .then().log().all().statusCode(200).assertThat()
                    .extract().body().as(ChildDeviceRegRes.class);

        return this;
    }

    public ChildApis childSignature() {
        RestAssured.defaultParser = Parser.JSON;
        childSignatureRes = given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("privateKey", childKeysRes.getPrivateKey())
                .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID)
                .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                .header("Authorization", "Bearer " + childDeviceRegRes.getChildAccessToken())
                .accept("*/*")
                .contentType(ContentType.JSON)
                .when()
                .body(ChildSignatureBody.builder()
                        .deviceId("4A7B2UIZL-FG34-45F9-I90O-401EC" + CHILD_DEVICE_ID)
                        .build())
                .post(authConfiguration.getExperienceDevPath() + CHILD_SIGNATURE_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ChildSignatureRes.class);

        return this;
    }

    public ChildApis childSavePassCode() {
        RestAssured.defaultParser = Parser.JSON;
        childSavePassCodeRes = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + childDeviceRegRes.getChildAccessToken())
                .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                .header("x-jws-signature", childSignatureRes.getChildXJWSSignature())
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID + CHILD_DEVICE_ID)
                .contentType(ContentType.JSON)
                .when()
                .body(childSignatureRes.getChildPayload())
                .post(authConfiguration.getExperienceBasePath() +CHILD_SAVE_PASSCODE)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ChildSavePassCodeRes.class);

        return this;
    }

    public ChildApis childSignatureUserID() {
        RestAssured.defaultParser = Parser.JSON;
        childSignatureRes = given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID + CHILD_DEVICE_ID)
                .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                .header("privateKey", childKeysRes.getPrivateKey())
                .header("Authorization", "Bearer " + childDeviceRegRes.getChildAccessToken())
                .accept("*/*")
                .contentType(ContentType.JSON)
                .when()
                .body(SignatureBody.builder()
                        .deviceId("4A7B2UIZL-FG34-45F9-I90O-401EC" + CHILD_DEVICE_ID)
                        .userId(child.getChildId())
                        .build())
                .post(authConfiguration.getExperienceDevPath() +CHILD_SIGNATURE_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ChildSignatureRes.class);

        return this;
    }

    public ChildApis childLogin() {
        RestAssured.defaultParser = Parser.JSON;
        childLoginRes = given()
                .config(config)
                .log().all()
                .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID + CHILD_DEVICE_ID)
                .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                .header("x-jws-signature", childSignatureRes.getChildXJWSSignature())
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .accept("*/*")
                .contentType(ContentType.JSON)
                .when()
                .body(childSignatureRes.getChildPayload())
                .post(authConfiguration.getExperienceBasePath() + CHILD_LOGIN_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ChildLoginRes.class);

        assertNotNull(childLoginRes.getProfile());
        return this;
    }

    @Override
    protected ChildApis getThis() {
        return this;
    }
}