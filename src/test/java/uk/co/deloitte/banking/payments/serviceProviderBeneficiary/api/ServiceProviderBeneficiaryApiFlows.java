package uk.co.deloitte.banking.payments.serviceProviderBeneficiary.api;

import io.restassured.http.ContentType;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.beneficary.model.BeneficiarySupplementary1Data;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiary1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiary1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBranchAndFinancialInstitutionIdentification60;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBCashAccount50;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.config.UtilityPaymentsConfig;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.BeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.ReadBeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model.WriteBeneficiary1;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;

@Singleton
public class ServiceProviderBeneficiaryApiFlows extends BaseApi {

    @Inject
    UtilityPaymentsConfig utilityPaymentsConfig;

    private final String BENEFICIARY_INTERNAL = "/internal/v1/utility-bills/beneficiaries/";

    public ReadBeneficiaryResponse1 getServiceBeneficiaries(final AlphaTestUser alphaTestUser) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().all().assertThat()
                .extract().body().as(ReadBeneficiaryResponse1.class);
    }

    public void getServiceBeneficiaries(final AlphaTestUser alphaTestUser, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().all().statusCode(statusCode);
    }

    public void getServiceBeneficiariesInvalidBearerToken(int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer ")
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().all().statusCode(statusCode);
    }

    public void deleteServiceBeneficiary(final AlphaTestUser alphaTestUser, String beneficiaryId, int statusCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .delete(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryId)
                .then().log().ifError().statusCode(statusCode).assertThat();
    }

    public void deleteServiceBeneficiaryNoBearerToken(String beneficiaryId) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer ")
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .delete(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryId)
                .then().log().ifError().statusCode(401).assertThat();
    }

    public OBErrorResponse1 deleteServiceBeneficiaryError(final AlphaTestUser alphaTestUser, String beneficiaryId, int statusCode) {

      return   given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .delete(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryId)
                .then().log().ifError()
                .statusCode(statusCode)
                .extract().as(OBErrorResponse1.class);
    }

    public BeneficiaryResponse1 createServiceBeneficiary(final AlphaTestUser alphaTestUser, final WriteBeneficiary1 writeBeneficiary1) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(writeBeneficiary1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().all().statusCode(201).assertThat()
                .extract().body().as(BeneficiaryResponse1.class);
    }

    public void createServiceBeneficiaryError(final AlphaTestUser alphaTestUser, final WriteBeneficiary1 writeBeneficiary1, int responseCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(writeBeneficiary1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().all().statusCode(responseCode).assertThat();
    }

    public void createServiceBeneficiaryWithoutBearerToken(final AlphaTestUser alphaTestUser, final WriteBeneficiary1 writeBeneficiary1, int responseCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer ")
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(writeBeneficiary1)
                .when()
                .post(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().all().statusCode(responseCode).assertThat();
    }

    public OBErrorResponse1 createServiceBeneErrorResponse(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, int statusCode) {

        OBWriteBeneficiary1 beneficiary = OBWriteBeneficiary1.builder()
                .data(OBWriteBeneficiary1Data.builder()
                        .beneficiary(OBBeneficiary5.builder()
                                .creditorAgent(OBBranchAndFinancialInstitutionIdentification60.builder()
                                        .name(beneficiaryData.getBeneficiaryType())
                                        .identification(beneficiaryData.getSwiftCode()).build())
                                .supplementaryData(BeneficiarySupplementary1Data.builder()
                                        .nickname(beneficiaryData.getNickName())
                                        .mobileNumber(beneficiaryData.getMobileNumber()).build())
                                .creditorAccount(OBCashAccount50.builder()
                                        .name(beneficiaryData.getBeneficiaryName())
                                        .identification(beneficiaryData.getAccountNumber()).build())
                                .build()).build())
                .build();

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(beneficiary)
                .post(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);

    }

    public OBErrorResponse1 updateServiceBeneficiaryError(final AlphaTestUser alphaTestUser, OBBeneficiary5 beneficiary, int statusCode) {
        OBWriteBeneficiary1 updatedBeneficiary = OBWriteBeneficiary1.builder()
                .data(OBWriteBeneficiary1Data.builder()
                        .beneficiary(beneficiary)
                        .build())
                .build();

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(updatedBeneficiary)
                .when()
                .put(utilityPaymentsConfig.getBasePath() + BENEFICIARY_INTERNAL + beneficiary.getBeneficiaryId())
                .then()
                .log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

}
