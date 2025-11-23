package ahb.experience.onboarding.updateProfileManagement.api;

import ahb.experience.onboarding.RegisterDevice_Setup;
import ahb.experience.onboarding.SignatureKeys_Setup;
import ahb.experience.onboarding.GetOTP_Setup;
import ahb.experience.onboarding.Payload_Setup;
import ahb.experience.onboarding.VerifyCustomer;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.DevSimConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class  setupUser extends BaseApi {

    @Inject
    DevSimConfiguration devSimConfiguration;

    private final String REGISTER_DEVICE = "/onboarding/public/devices";
    private final String VERIFY_CUSTOMER = "/onboarding/public/customer/registered";
    private final String GENERATE_OTP = "/onboarding/protected/otp";
    private final String GET_OTP = "/api/otps/";
    private final String SIGNATURE_KEYS = "/signature/public/signature/keys";
    private final String VALIDATE_OTP = "/onboarding/protected/otp/validate/profile";
    private final String UPDATE_MARKETUSER = "/onboarding/protected/marketplace-personal-details";
    private final String FETCH_SIGNATURE = "/signature/public/signature";
    private final String SAVE_PASSCODE = "/onboarding/protected/customer/passcode";

    public static int getRandom(int max){ return (int) (Math.random()*max); }

    String strEmail = "email"+String.valueOf(getRandom(1000));

    @Inject
    AuthConfiguration authConfiguration;

    private final String UPDATEPROFILE_URL = "/onboarding/protected/profile";

    public JSONObject getUserJSONValue(Map<String, Object> newPayLoad) throws JSONException {
        return new JSONObject(newPayLoad);
    }

    public RegisterDevice_Setup registerDevice(String deviceId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", deviceId);
            jsonObject.put("deviceHash", "initialdevice003");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .body(jsonObject.toString())
                .when()
                .post(authConfiguration.getExperienceBasePath() + REGISTER_DEVICE)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(RegisterDevice_Setup.class);
    }

    public VerifyCustomer verifyCustomer(String strMobileNumber) {
        return given()
                .config(config)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParam("mobileNumber",strMobileNumber)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + VERIFY_CUSTOMER)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(VerifyCustomer.class);
    }

    public void generateOTP_CreateUser(String BearerToken,String strMobileNumber,String strXRequestID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobileNumber", strMobileNumber);
            jsonObject.put("type", "REGISTRATION_TEXT");
        } catch (JSONException e) {
            e.printStackTrace();
        }

         given()
                .config(config)
                .header("Authorization", "Bearer " + BearerToken)
                .header("X-Request-Id", strXRequestID)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath() + GENERATE_OTP)
                .then().log().all().statusCode(200).assertThat()
                .extract().body();
    }

    public void generateOTP_UpdateUser(String BearerToken,String strMobileNumber) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobileNumber", strMobileNumber);
            jsonObject.put("type", "PROFILE_UPDATE");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        given()
                .config(config)
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath() + GENERATE_OTP)
                .then().log().all().statusCode(200).assertThat()
                .extract().body();
    }

    public GetOTP_Setup getOTP(String userId, String strMobileNumber,String BearerToken, String strXRequestID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobileNumber", strMobileNumber);
            jsonObject.put("type", "REGISTRATION_TEXT");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer " + BearerToken)
                .header("X-Request-Id",strXRequestID)
                .queryParam("X-Request-Id","TestUser")
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .get(devSimConfiguration.getBasePath()+GET_OTP+userId)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(GetOTP_Setup.class);
    }

    public SignatureKeys_Setup getSignatureKeys(String BearerToken) {
        return given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("x-device-id", "ABCD-91")
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceDevPath()+ SIGNATURE_KEYS)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(SignatureKeys_Setup.class);
    }

    public void validateOtp_UpdateUser( String deviceId, String password, String strMobileNumber,String BearerToken) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("otp", password);
            jsonObject.put("mobileNumber", strMobileNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer " + BearerToken)
                .header("accessToken", BearerToken)
                .header("X-Request-Id", "ABCD-123")
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath()+VALIDATE_OTP)
                .then().log().all()
                .statusCode(200).assertThat()
                .extract().body();
    }

    public void updateUser(String BearerToken,String strXRequestID){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "TestUser");
            jsonObject.put("dateOfBirth", "1989-03-03");
            jsonObject.put("language", "en");
            jsonObject.put("email", strEmail);
            jsonObject.put("termsAccepted", "true");
            jsonObject.put("gender", "MALE");
            jsonObject.put("nationality", "US");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer " + BearerToken)
                .header("accessToken", BearerToken)
                .queryParam("X-Request-Id", strXRequestID)
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath()+UPDATE_MARKETUSER)
                .then().log().all()
                .statusCode(200).assertThat()
                .extract().body();
    }

    public Payload_Setup signature_Login(String privateKey, String deviceId, String passcode, String mobileNumber){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", deviceId);
            jsonObject.put("passcode", passcode);
            jsonObject.put("mobileNumber", mobileNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("privateKey", privateKey)
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceDevPath()+FETCH_SIGNATURE)
                .then().log().all()
                .statusCode(200).assertThat()
                .extract().body().as(Payload_Setup.class);
    }

    public Payload_Setup signature(String privateKey, String deviceId, String passcode){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", deviceId);
            jsonObject.put("passcode", passcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("privateKey", privateKey)
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceDevPath()+FETCH_SIGNATURE)
                .then().log().all()
                .statusCode(200).assertThat()
                .extract().body().as(Payload_Setup.class);
    }

    public RegisterDevice_Setup savePasscode(String BearerToken,String strSignature, String deviceId, String passcode){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", deviceId);
            jsonObject.put("passcode", passcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return given()
                .config(config)
                .log().all()
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .header("Authorization", "Bearer "+BearerToken)
                .header("x-jws-signature", strSignature)
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .post(authConfiguration.getExperienceBasePath()+SAVE_PASSCODE)
                .then().log().all()
                .statusCode(200).assertThat()
                .extract().body().as(RegisterDevice_Setup.class);
    }
}
