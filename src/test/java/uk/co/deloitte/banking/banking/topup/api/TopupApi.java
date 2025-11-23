package uk.co.deloitte.banking.banking.topup.api;

import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model.AccountTopupResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
@Singleton
@Slf4j
public class TopupApi extends BaseApi {

    private static final String PROTECTED_TOPUP_API = "/protected/v1/accounts/{account}/top-up-payments";

    @Inject
    private final BankingConfig bankingConfig;

    public AccountTopupResponseV1 doTopup(final AlphaTestUser alphaTestUser,
                                          final AccountTopupRequestV1 accountTopupRequest) {

        return given()
                .config(config)
                .log().all()
                .pathParams("account", alphaTestUser.getAccountNumber())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header(HttpConstants.HEADER_X_API_KEY, bankingConfig.getApiKey())
                .contentType(ContentType.JSON)
                .body(accountTopupRequest)
                .when()
                .post(bankingConfig.getBasePath() + PROTECTED_TOPUP_API)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(AccountTopupResponseV1.class);
    }
}
