package uk.co.deloitte.banking.banking.temenos.api;

import io.restassured.http.ContentType;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.finance.FinanceResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

public class FinanceAPI extends BaseApi {

    private static final String GET_FINANCE = "/internal/v1/finance";

    @Inject
    TemenosConfig temenosConfig;

    public FinanceResponse getActiveLoanDetails(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(temenosConfig.getPath() + GET_FINANCE)
                .then().log().all().statusCode(200).assertThat().extract().body().as(FinanceResponse.class);
    }
}
