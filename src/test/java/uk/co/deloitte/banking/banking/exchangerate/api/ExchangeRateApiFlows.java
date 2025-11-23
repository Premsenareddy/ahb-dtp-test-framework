package uk.co.deloitte.banking.banking.exchangerate.api;

import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.exchangeRate.response.ExchangeRateResponseDo;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.HttpConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

@Singleton
public class ExchangeRateApiFlows extends BaseApi {

    @Inject
    TemenosConfig temenosConfig;

    private final String EXCHANGE_RATE_INTERNAL = "/internal/v1/exchange-rate?currency=";

    private final int BAD_REQUEST = 400;
    private final int NOT_FOUND = 404;

    public ExchangeRateResponseDo fetchExchangeRate(final AlphaTestUser alphaTestUser, String currencyId) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getPath() + EXCHANGE_RATE_INTERNAL + currencyId)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ExchangeRateResponseDo.class);
    }


    public void fetchExchangeRateError(final AlphaTestUser alphaTestUser, int responseCode, String currencyId) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getPath() + EXCHANGE_RATE_INTERNAL + currencyId)
                .then().log().all().statusCode(responseCode).assertThat();
    }

    // Error methods for handling 400 and 404 response

    public HashMap<String, String> fetchExchangeRateRequestError(final AlphaTestUser alphaTestUser, String currencyId) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HttpConstants.HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getPath() + EXCHANGE_RATE_INTERNAL + currencyId)
                .then().log().all().statusCode(anyOf(is(BAD_REQUEST),is(NOT_FOUND))).assertThat()
                .extract().body().as(HashMap.class);
    }

}
