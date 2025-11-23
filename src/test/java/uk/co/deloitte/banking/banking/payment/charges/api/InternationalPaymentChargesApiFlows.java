package uk.co.deloitte.banking.banking.payment.charges.api;

import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.internationalCharges.InternationalChargesResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class InternationalPaymentChargesApiFlows extends BaseApi {

    @Inject
    TemenosConfig temenosConfig;

    private final String INTERNATIONAL_PAYMENT_CHARGES_INTERNAL = "/internal/v1/international-payment-charges/accounts/{accountId}";


    public InternationalChargesResponse1 fetchInternationalCharges(final AlphaTestUser alphaTestUser, final String feeType) {

        return given()
                .config(config)
                .log().all()
                .pathParams("accountId", alphaTestUser.getAccountNumber())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getPath() + INTERNATIONAL_PAYMENT_CHARGES_INTERNAL +"?feeType="+ feeType)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(InternationalChargesResponse1.class);
    }


    public void fetchInternationalChargesError(final AlphaTestUser alphaTestUser, final String feeType, int responseCode) {

        given()
                .config(config)
                .log().all()

                .pathParams("accountId", alphaTestUser.getAccountNumber())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getPath() + INTERNATIONAL_PAYMENT_CHARGES_INTERNAL +"?feeType="+ feeType)
                .then().log().all().statusCode(responseCode).assertThat();
    }
    /*#######################################################
    Description: - To Pass Account No in building Request and to handle error scenrioes
    CreatedBy:Shilpi Agrawal
    UpdatedBy:
    LastUpdatedOn:
    Comments:
    #######################################################*/
    public void fetchInternationalAccountError(final AlphaTestUser alphaTestUser, String accountNumber, final String feeType, int responseCode) {
        if(accountNumber.equalsIgnoreCase("")){
            accountNumber = alphaTestUser.getAccountNumber();
        }
        given()
                .config(config)
                .log().all()
                .pathParams("accountId", accountNumber)
                //.pathParams("accountId", alphaTestUser.getAccountNumber())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getPath() + INTERNATIONAL_PAYMENT_CHARGES_INTERNAL +"?feeType="+ feeType)
                .then().log().all().statusCode(responseCode).assertThat();
    }

}
