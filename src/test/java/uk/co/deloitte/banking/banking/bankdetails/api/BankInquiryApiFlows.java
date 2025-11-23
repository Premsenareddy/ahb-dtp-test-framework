package uk.co.deloitte.banking.banking.bankdetails.api;

import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.banking.legacy.LegacyBankingAdapterConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.bankdetails.responses.BankInquiryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

public class BankInquiryApiFlows extends BaseApi {

    @Inject
    LegacyBankingAdapterConfig legacyBankingAdapterConfig;

    private final String BANK_DETAILS_INTERNAL = "/internal/v1/bankdetails/";

    public BankInquiryResponse1 fetchBankDetails(final AlphaTestUser alphaTestUser, final String code, final String identifier) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(legacyBankingAdapterConfig.getPath() + BANK_DETAILS_INTERNAL + code + "?identifier=" + identifier)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(BankInquiryResponse1.class);
    }

    public void fetchBankInquiryError(final AlphaTestUser alphaTestUser, final String code, final String identifier, int responseCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(legacyBankingAdapterConfig.getPath() + BANK_DETAILS_INTERNAL + code + "?identifier=" + identifier)
                .then().log().all().statusCode(responseCode).assertThat();
    }
}
