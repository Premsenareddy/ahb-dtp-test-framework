package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api;

import ahb.experience.onboarding.DebitCard.*;
import ahb.experience.onboarding.DebitCard.Scheduler.SchedulerMain;
import ahb.experience.onboarding.ExperienceAddressDetails;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.base.BaseStep;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class DebitCardDetails extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    //@Inject
    //ExperienceCardProducts experienceCardProducts;
    ExperienceNameOnCard experienceNameOnCard;
    ExperienceScheduleDelivery experienceScheduleDelivery;
    ExperienceAddressDetails experienceAddressDetails;

    private final String MASTERCARDVARIETIES_URL = "/onboarding/protected/card/codes";
    private final String NAMEONCARD_URL = "/onboarding/protected/card";
    private final String CARDDELIVERYTIMESLOT_URL = "/accounts/protected/timeslots";
    private final String CARDSCHEDULE_URL = "/accounts/protected/order/schedule";
    private final String CARDDELIVERYSTATUS_URL = "/accounts/protected/order/status";
    private final String GENERATEDEBITPIN_URL = "/signature/public/pin/block";
    private final String SETDEBITPIN_URL = "/accounts/protected/cards/pin";
    private final String GETDEBITCARDDETAILS_URL = "/accounts/protected/cards";
    private final String CARDDELIVERYUPDATE_URL = "/courier/external/card-delivery-status";
    private final String CARDDELIVERYCONFIRMATION_URL = "/experience/v1.0/webhooks/card-delivery-confirmation";


    public ExperienceCardProducts getCardVarieties(String BearerToken, Map<String, String> queryParams, int StatusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +MASTERCARDVARIETIES_URL)
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(ExperienceCardProducts.class);
    }

    public ExperienceNameOnCard captureNameOnCard(String BearerToken, String strName, String strProductCode, Map<String, String> queryParams, int StatusCode) {
        experienceNameOnCard = ExperienceNameOnCard.builder().name(strName).productCode(strProductCode).build();

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .queryParams(queryParams)
                .body(experienceNameOnCard)
                .when()
                .post(authConfiguration.getExperienceBasePath() +NAMEONCARD_URL)
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(ExperienceNameOnCard.class);
    }

    public SchedulerMain getDeliveryCardTimeSlots(String BearerToken,int StatusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParam("longitude","55.4624448")
                .queryParam("latitude","25.1561085")
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +CARDDELIVERYTIMESLOT_URL)
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(SchedulerMain.class);
    }

    public void saveDeliveryCardTimeSlots(String BearerToken, int timeslotId, String strTimeslotDate, String strName, boolean isHomeAddress, String strTimeslotStart, String strTimeslotEnd, boolean isParentCard, String cifNumber,String strBuildName, String strBuildNum,String strEmirate, String strCity, Map<String, String> queryParams, int StatusCode) {

        experienceAddressDetails = ExperienceAddressDetails.builder().buildingName(strBuildName).buildingNumber(strBuildNum).area("Some area").officeNumber("1234").poBox("Some PO Box").city(strCity).emirate(strEmirate).build();
        experienceScheduleDelivery = ExperienceScheduleDelivery.builder()
                                        .address(experienceAddressDetails)
                                        .timeslotId(timeslotId)
                                        .scheduledDate(strTimeslotDate)
                                        .customerName(strName)
                                        .isHomeAddress(isHomeAddress)
                                        .timeslotStart(strTimeslotStart)
                                        .timeslotEnd(strTimeslotEnd)
                                        .isParentCard(isParentCard)
                                        .cifNumber(cifNumber)
                                        .relationshipId(queryParams.get("relationshipId"))
                                        .build();

         given()
                .config(BaseStep.config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                 .body(experienceScheduleDelivery)
                .post(authConfiguration.getExperienceBasePath() +CARDSCHEDULE_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
                //.extract().body().as(SchedulerMain.class);
    }

    public ExperienceCardDeliveryStatus getDebitCardDeliveryStatus(String BearerToken, boolean isParentFlag, Map<String, String> queryParams, int StatusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParam("isParentFlag",isParentFlag)
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +CARDDELIVERYSTATUS_URL)
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(ExperienceCardDeliveryStatus.class);
    }

    public void saveDebitCardDeliveryConfirmation(String BearerToken, String strTrackingNumber, String strDeliveryStatus,String strDeliveryReason,int StatusCode) {
        ExperienceCardDeliveryConfirmation experienceCardTracking = ExperienceCardDeliveryConfirmation.builder().trackingNumber(strTrackingNumber).deliveryStatus(strDeliveryStatus).deliveryReason(strDeliveryReason).build();
        given()
                .config(config)
                .log().all()
                //.header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey_1())
                .header("x-fapi-interaction-id", "1234567890-asdfg")
                .contentType(ContentType.JSON)
                .when()
                .body(experienceCardTracking)
                .post(authConfiguration.getKongInternalURL() +CARDDELIVERYCONFIRMATION_URL)
                .then().log().all().statusCode(StatusCode).assertThat();

    }

    public void saveDebitCardDeliveryStatus(String BearerToken, String strDeliveryStatus,String strTrackingNumber,String strDeliveryDate,int StatusCode) {
        ExperienceCardDeliveryUpdate experienceCardDliveryUpdate = ExperienceCardDeliveryUpdate.builder().
                status(strDeliveryStatus).tracking_id(strTrackingNumber).date(strDeliveryDate).build();
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(experienceCardDliveryUpdate)
                .post(authConfiguration.getExperienceBasePath() +CARDDELIVERYUPDATE_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
    }

    public ExperienceDebitCardDetails fetchDebitCardDetails(String BearerToken, String cardType, Map<String, String> queryParams, int StatusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParam("type",cardType)
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +GETDEBITCARDDETAILS_URL)
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(ExperienceDebitCardDetails.class);
    }

    public String generateDebitPinBlock(String BearerToken, String strEncryptCardNo) {
         return given()
                .config(config)
                .log().all().queryParam("plainTextPin","4321")
                 .queryParam("encryptedCardNumber",strEncryptCardNo)
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                 .header("env", authConfiguration.getExperienceEnv())
                .contentType(ContentType.JSON)
                .when()
                .post(authConfiguration.getExperienceDevPath() +GENERATEDEBITPIN_URL)
                 .then().extract().asString();
    }

    public void setDebitCardPin(String BearerToken, String strExpirtDate,String strCardNo,String strCardLastFour,String strPinBlock, Map<String, String> queryParams, int StatusCode) {
        ExperienceCardSetDebitPin experienceCardSetDebitPin = ExperienceCardSetDebitPin.builder().cardExpiryDate(strExpirtDate)
                .cardNumber(strCardNo).lastFourDigit(strCardLastFour).pinBlock(strPinBlock).relationshipId(queryParams.get("relationshipId")).build();
         given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(experienceCardSetDebitPin)
                .post(authConfiguration.getExperienceBasePath() +SETDEBITPIN_URL)
                .then().log().all().statusCode(StatusCode).assertThat();
    }
}
