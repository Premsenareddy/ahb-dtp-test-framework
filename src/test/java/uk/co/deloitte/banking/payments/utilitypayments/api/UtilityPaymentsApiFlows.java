package uk.co.deloitte.banking.payments.utilitypayments.api;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.config.UtilityPaymentsConfig;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry.UtilityInquiryRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.inquiry.UtilityInquiryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.UtilityPaymentRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.utilitypayments.payment.UtilityPaymentResponse1;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class UtilityPaymentsApiFlows extends BaseApi {

    @Inject
    UtilityPaymentsConfig utilityPaymentsConfig;

    private final String PAYMENT_INTERNAL = "/internal/v1/utility-bills/payments";

    private final String INQUIRY_INTERNAL = "/internal/v1/utility-bills/inquiry";

    private final String Kids_INTERNAL_STEP_UP = "/internal/v1/utility-bills/kids/step-up";

    private final String Kids_PAYMENT_INTERNAL = "/internal/v1/utility-bills/relationships";


    public UtilityPaymentResponse1 payUtilityPayments(final AlphaTestUser alphaTestUser, final UtilityPaymentRequest1 utilityPaymentRequest1) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityPaymentRequest1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + PAYMENT_INTERNAL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UtilityPaymentResponse1.class);
    }


    public OBErrorResponse1 payUtilityPaymentsError(final AlphaTestUser alphaTestUser, final UtilityPaymentRequest1 utilityPaymentRequest1, int responseCode) {

       return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityPaymentRequest1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + PAYMENT_INTERNAL)
                .then().log().all().statusCode(responseCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public ValidatableResponse payUtilityPaymentsErrorTest(final AlphaTestUser alphaTestUser, final UtilityPaymentRequest1 utilityPaymentRequest1, int responseCode) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityPaymentRequest1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + PAYMENT_INTERNAL)
                .then().log().ifError().statusCode(responseCode).assertThat();
    }



    public UtilityInquiryResponse1 utilityInquiry(final AlphaTestUser alphaTestUser, final UtilityInquiryRequest1 utilityInquiryRequest) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityInquiryRequest)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + INQUIRY_INTERNAL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UtilityInquiryResponse1.class);
    }

/*#######################################################

Description:To send request and Status code is returned
InputParameters:AlphaTestUser User,UtilityInquiryRequest1 Request
Output: int Status Code
CreatedBy:Shilpi Agrawal
UpdatedBy:
LastUpdatedOn:
Comments:
#######################################################*/
    public int utilityInquiryReturnStatusCode(final AlphaTestUser alphaTestUser, final UtilityInquiryRequest1 utilityInquiryRequest) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityInquiryRequest)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + INQUIRY_INTERNAL)
                .then().log().all().extract().response().getStatusCode();
    }


    public OBErrorResponse1 utilityInquiryError(final AlphaTestUser alphaTestUser, final UtilityInquiryRequest1 utilityInquiryRequest1, int responseCode) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityInquiryRequest1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + INQUIRY_INTERNAL)
                .then().log().all().statusCode(responseCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void forbiden(final AlphaTestUser alphaTestUser, final UtilityPaymentRequest1 utilityInquiryRequest1, int responseCode) {

         given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + "invalidrequest")
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityInquiryRequest1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + INQUIRY_INTERNAL)
                .then().log().all().statusCode(responseCode).assertThat();
    }


    public Boolean validateKidsStepUp(final AlphaTestUser alphaTestUser) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(utilityPaymentsConfig.getBasePath() + Kids_INTERNAL_STEP_UP)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(Boolean.class);
    }



    public UtilityPaymentResponse1 kidsUtilityPayments(final AlphaTestUser alphaTestUser, final UtilityPaymentRequest1 utilityPaymentRequest1 ,  final String relationshipId) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(utilityPaymentRequest1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + Kids_PAYMENT_INTERNAL + "/" + relationshipId + "/payments")
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(UtilityPaymentResponse1.class);
    }


}
