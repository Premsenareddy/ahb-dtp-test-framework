package uk.co.deloitte.banking.payments.beneficiary.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.deloitte.banking.account.api.beneficary.model.BeneficiarySupplementary1Data;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiary1;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiary1Data;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.InternationalProfileData;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBeneficiary5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBBranchAndFinancialInstitutionIdentification60;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBCashAccount50;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadBeneficiary5;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.config.PaymentConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.customer.api.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

@Singleton
public class BeneficiaryApiFlows extends BaseApi {

    @Inject
    PaymentConfiguration paymentConfiguration;

    private final String BENEFICIARY_INTERNAL = "/internal/v1/beneficiaries/";
    private final String RELATIONSHIP_BENEFICIARY_INTERNAL = "/internal/v1/relationships/";
    private final String BENEFICIARY_PROTECTED = "/protected/v1/user/";


    public OBReadBeneficiary5 getBeneficiaryById(final AlphaTestUser alphaTestUser, String id) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + id)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(OBReadBeneficiary5.class);
    }


    public OBReadBeneficiary5 getBeneficiaries(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(OBReadBeneficiary5.class);
    }


    public OBWriteBeneficiaryResponse1 updateBeneficiary(final AlphaTestUser alphaTestUser, OBBeneficiary5 beneficiary) {
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
                .put(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiary.getBeneficiaryId())
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(OBWriteBeneficiaryResponse1.class);
    }

    public Response updateBeneficiaryResponse(final AlphaTestUser alphaTestUser, OBBeneficiary5 beneficiary) {
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
                .put(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiary.getBeneficiaryId());
    }

    public void deleteBeneficiary(final AlphaTestUser alphaTestUser, String beneficiaryId, int statusCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .delete(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryId)
                .then().log().ifError().statusCode(statusCode).assertThat();
    }

    public OBWriteBeneficiaryResponse1 createBeneficiaryFlex(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData) {

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
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(beneficiary)
                .when()
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(201).assertThat()
                .extract().body().as(OBWriteBeneficiaryResponse1.class);
    }

    public OBWriteBeneficiaryResponse1 createBeneficiaryProtected(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData) {

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
                .header(HttpConstants.HEADER_X_API_KEY, paymentConfiguration.getApiKey())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(beneficiary)
                .when()
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_PROTECTED + alphaTestUser.getUserId() + "/beneficiaries")
                .then().log().ifError().statusCode(201).assertThat()
                .extract().body().as(OBWriteBeneficiaryResponse1.class);
    }

    public OBWriteBeneficiaryResponse1 createBeneficiaryInternational(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData) {

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
                                .internationalProfileData(InternationalProfileData.builder()
                                        .addressLine1(beneficiaryData.getAddressLine1())
                                        .addressLine2(beneficiaryData.getAddressLine2())
                                        .addressLine3(beneficiaryData.getAddressLine3())
                                        .creditorAccountCurrency(beneficiaryData.getCreditorAccountCurrency()).build())
                                .build()).build())
                .build();


        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(beneficiary)
                .when()
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(201).assertThat()
                .extract().body().as(OBWriteBeneficiaryResponse1.class);
    }

    public Response createBeneficiaryFlexResponse(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData) {

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
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(beneficiary)
                .when()
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL);
    }

    public OBErrorResponse1 createBeneErrorResponse(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, int statusCode) {

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
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);

    }

    public OBErrorResponse1 createBeneErrorResponseInternational(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, int statusCode) {

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
                                .internationalProfileData(InternationalProfileData.builder()
                                        .addressLine1(beneficiaryData.getAddressLine1())
                                        .addressLine2(beneficiaryData.getAddressLine2())
                                        .addressLine3(beneficiaryData.getAddressLine3())
                                        .creditorAccountCurrency(beneficiaryData.getCreditorAccountCurrency()).build())
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
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);

    }

    public void createBeneErrorResponseVoid(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, int statusCode) {

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

         given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(beneficiary)
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(statusCode);

    }

    public OBErrorResponse1 updateBeneficiaryError(final AlphaTestUser alphaTestUser, OBBeneficiary5 beneficiary, int statusCode) {
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
                .put(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiary.getBeneficiaryId())
                .then()
                .log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 getBeneficiaryError(final AlphaTestUser alphaTestUser, String id, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + id)
                .then().log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBWriteBeneficiaryResponse1 createRelationshipBeneficiaryFlex(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, final String relationshipId) {

        OBWriteBeneficiary1 beneficiary = OBWriteBeneficiary1.builder()
                .data(OBWriteBeneficiary1Data.builder()
                        .beneficiary(OBBeneficiary5.builder()
                                .creditorAgent(OBBranchAndFinancialInstitutionIdentification60.builder()
                                        .name(beneficiaryData.getBeneficiaryType())
                                        .identification(beneficiaryData.getSwiftCode()).build())
                                .supplementaryData(BeneficiarySupplementary1Data.builder()
                                        .nickname(beneficiaryData.getNickName())
                                        .mobileNumber(beneficiaryData.getMobileNumber())
                                        .build())
                                .creditorAccount(OBCashAccount50.builder()
                                        .name(beneficiaryData.getBeneficiaryName())
                                        .identification(beneficiaryData.getAccountNumber()).build())
                                .build()).build())
                .build();


        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(beneficiary)
                .when()
                .post(paymentConfiguration.getBasePath() + RELATIONSHIP_BENEFICIARY_INTERNAL + relationshipId + "/beneficiaries")
                .then().log().ifError().statusCode(201).assertThat()
                .extract().body().as(OBWriteBeneficiaryResponse1.class);
    }

    public void createRelationshipBeneficiaryVoid(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, final String relationshipId, int statusCode) {

        OBWriteBeneficiary1 beneficiary = OBWriteBeneficiary1.builder()
                .data(OBWriteBeneficiary1Data.builder()
                        .beneficiary(OBBeneficiary5.builder()
                                .creditorAgent(OBBranchAndFinancialInstitutionIdentification60.builder()
                                        .name(beneficiaryData.getBeneficiaryType())
                                        .identification(beneficiaryData.getSwiftCode()).build())
                                .supplementaryData(BeneficiarySupplementary1Data.builder()
                                        .nickname(beneficiaryData.getNickName())
                                        .mobileNumber(beneficiaryData.getMobileNumber())
                                        .build())
                                .creditorAccount(OBCashAccount50.builder()
                                        .name(beneficiaryData.getBeneficiaryName())
                                        .identification(beneficiaryData.getAccountNumber()).build())
                                .build()).build())
                .build();


         given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(beneficiary)
                .when()
                .post(paymentConfiguration.getBasePath() + RELATIONSHIP_BENEFICIARY_INTERNAL + relationshipId + "/beneficiaries")
                .then().log().ifError().statusCode(statusCode);
    }

    public OBReadBeneficiary5 getRelationshipBeneficiaries(final AlphaTestUser alphaTestUser, final String relationshipId) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(paymentConfiguration.getBasePath() + RELATIONSHIP_BENEFICIARY_INTERNAL + relationshipId + "/beneficiaries")
                .then().log().body()
                .statusCode(200).assertThat()
                .extract().body().as(OBReadBeneficiary5.class);
    }

    public OBErrorResponse1 getRelationshipBeneficiariesError(final AlphaTestUser alphaTestUser, final String relationshipId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(paymentConfiguration.getBasePath() + RELATIONSHIP_BENEFICIARY_INTERNAL + relationshipId + "/beneficiaries")
                .then().log().body()
                .statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void getRelationshipBeneficiariesVoid(final AlphaTestUser alphaTestUser, final String relationshipId, int statusCode) {
         given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(paymentConfiguration.getBasePath() + RELATIONSHIP_BENEFICIARY_INTERNAL + relationshipId + "/beneficiaries")
                .then().log().body()
                .statusCode(statusCode);
    }

    public OBWriteBeneficiaryResponse1 updateBeneficiaryInternational(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, final String beneficiaryID) {

        OBWriteBeneficiary1 updatedBeneficiary = OBWriteBeneficiary1.builder()
                .data(OBWriteBeneficiary1Data.builder()
                        .beneficiary(OBBeneficiary5.builder()
                                .beneficiaryId(beneficiaryID)
                                .creditorAgent(OBBranchAndFinancialInstitutionIdentification60.builder()
                                        .name(beneficiaryData.getBeneficiaryType())
                                        .identification(beneficiaryData.getSwiftCode()).build())
                                .supplementaryData(BeneficiarySupplementary1Data.builder()
                                        .nickname(beneficiaryData.getNickName())
                                        .mobileNumber(beneficiaryData.getMobileNumber()).build())
                                .creditorAccount(OBCashAccount50.builder()
                                        .name(beneficiaryData.getBeneficiaryName())
                                        .identification(beneficiaryData.getAccountNumber()).build())
                                .internationalProfileData(InternationalProfileData.builder()
                                                .addressLine1(beneficiaryData.getAddressLine1())
                                                .addressLine2(beneficiaryData.getAddressLine2())
                                                .addressLine3(beneficiaryData.getAddressLine3())
                                                .creditorAccountCurrency(beneficiaryData.getCreditorAccountCurrency()).build())
                                .build()).build())
                .build();
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(updatedBeneficiary)
                .when()
                .put(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryID)
                .then().log().ifError().statusCode(200).assertThat()
                .extract().body().as(OBWriteBeneficiaryResponse1.class);
    }

    public OBErrorResponse1 updateBeneficiaryInternationalError(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, final String beneficiaryID, int statusCode) {
        OBWriteBeneficiary1 updatedBeneficiary = OBWriteBeneficiary1.builder()
                .data(OBWriteBeneficiary1Data.builder()
                        .beneficiary(OBBeneficiary5.builder()
                                .beneficiaryId(beneficiaryID)
                                .creditorAgent(OBBranchAndFinancialInstitutionIdentification60.builder()
                                        .name(beneficiaryData.getBeneficiaryType())
                                        .identification(beneficiaryData.getSwiftCode()).build())
                                .supplementaryData(BeneficiarySupplementary1Data.builder()
                                        .nickname(beneficiaryData.getNickName())
                                        .mobileNumber(beneficiaryData.getMobileNumber()).build())
                                .creditorAccount(OBCashAccount50.builder()
                                        .name(beneficiaryData.getBeneficiaryName())
                                        .identification(beneficiaryData.getAccountNumber()).build())
                                .internationalProfileData(InternationalProfileData.builder()
                                        .addressLine1(beneficiaryData.getAddressLine1())
                                        .addressLine2(beneficiaryData.getAddressLine2())
                                        .addressLine3(beneficiaryData.getAddressLine3())
                                        .creditorAccountCurrency(beneficiaryData.getCreditorAccountCurrency()).build())
                                .build()).build())
                .build();

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(updatedBeneficiary)
                .when()
                .put(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryID)
                .then()
                .log().ifError().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void createBeneficiaryInternationalInvalidBearer(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, int statusCode) {

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
                                .internationalProfileData(InternationalProfileData.builder()
                                        .addressLine1(beneficiaryData.getAddressLine1())
                                        .addressLine2(beneficiaryData.getAddressLine2())
                                        .addressLine3(beneficiaryData.getAddressLine3())
                                        .creditorAccountCurrency(beneficiaryData.getCreditorAccountCurrency()).build())
                                .build()).build())
                .build();

                 given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer invalid token")
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(beneficiary)
                .when()
                .post(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL)
                .then().log().ifError().statusCode(statusCode).assertThat();
    }

    public void updateBeneficiaryInternationalInvalidBearer(final AlphaTestUser alphaTestUser, final BeneficiaryData beneficiaryData, final String beneficiaryID, int statusCode) {

        OBWriteBeneficiary1 updatedBeneficiary = OBWriteBeneficiary1.builder()
                .data(OBWriteBeneficiary1Data.builder()
                        .beneficiary(OBBeneficiary5.builder()
                                .beneficiaryId(beneficiaryID)
                                .creditorAgent(OBBranchAndFinancialInstitutionIdentification60.builder()
                                        .name(beneficiaryData.getBeneficiaryType())
                                        .identification(beneficiaryData.getSwiftCode()).build())
                                .supplementaryData(BeneficiarySupplementary1Data.builder()
                                        .nickname(beneficiaryData.getNickName())
                                        .mobileNumber(beneficiaryData.getMobileNumber()).build())
                                .creditorAccount(OBCashAccount50.builder()
                                        .name(beneficiaryData.getBeneficiaryName())
                                        .identification(beneficiaryData.getAccountNumber()).build())
                                .internationalProfileData(InternationalProfileData.builder()
                                        .addressLine1(beneficiaryData.getAddressLine1())
                                        .addressLine2(beneficiaryData.getAddressLine2())
                                        .addressLine3(beneficiaryData.getAddressLine3())
                                        .creditorAccountCurrency(beneficiaryData.getCreditorAccountCurrency()).build())
                                .build()).build())
                .build();

                 given()
                .config(config)
                .log().all()
                .header("Authorization", "invalid bearer token")
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .body(updatedBeneficiary)
                .when()
                .put(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryID)
                .then().log().ifError().statusCode(statusCode).assertThat();
    }

    public void deleteBeneficiaryInvalidBearer(final AlphaTestUser alphaTestUser, String beneficiaryId, int statusCode) {
                given()
                .config(config)
                .log().all()
                .header("Authorization", "invalid bearer")
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .delete(paymentConfiguration.getBasePath() + BENEFICIARY_INTERNAL + beneficiaryId)
                .then().log().ifError().statusCode(statusCode).assertThat();
    }
}
