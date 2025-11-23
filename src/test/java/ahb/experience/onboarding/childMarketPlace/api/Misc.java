package ahb.experience.onboarding.childMarketPlace.api;

import ahb.experience.onboarding.request.misc.CustomerData;
import ahb.experience.onboarding.request.misc.CustomerServiceReqBody;
import ahb.experience.onboarding.response.Misc.IdNowRes;
import ahb.experience.onboarding.response.Misc.IdNowResV2;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dzieciou.testing.curl.CurlRestAssuredConfigFactory;
import com.github.dzieciou.testing.curl.Options;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.json.JSONException;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IDNowValue;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IdentificationDocument;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IdentificationProcess;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.WebhookEvent;
import ahb.experience.onboarding.request.child.QRCodeReqBody;
import ahb.experience.onboarding.request.misc.IdnowUserDetailsReqBody;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.base.BaseStep;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static io.restassured.RestAssured.given;

@Singleton
public class Misc extends BaseStep<Misc> {

    BaseApi baseApi;

    private final String QRCODE_URL = "/onboarding/protected/child/{childid}/qrcode";
    private final String CUSTOMER_URL = "/protected/v2/customers/{childId}";

    public Misc QRCode(String accessToken) throws JSONException {
        RestAssured.defaultParser = Parser.JSON;
        given()
            .config(config)
            .log().all()
            .pathParams("childid", child.getChildId())
            .header("Authorization", "Bearer " + accessToken)
            .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID)
            .header("x-api-key", authConfiguration.getExperienceApiKey())
            .contentType(ContentType.JSON)
            .body(QRCodeReqBody.builder().relationshipId(child.getRelationshipId()).build())
            .when()
            .post(authConfiguration.getExperienceBasePath() + QRCODE_URL)
            .then().log().all().statusCode(200);

        return this;
    }

    public Misc customerService(CustomerServiceReqBody customerServiceReqBody) {
        RestAssured.defaultParser = Parser.JSON;
        given()
                .config(baseApi.config)
                .log().all()
                .pathParams("childId", child.getChildId())
                .header("x-api-key", customerConfig.getApiKey())
                .header("x-fapi-interaction-id", X_FAPI_INTERACTION_ID)
                .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                .contentType(ContentType.JSON)
                .body(customerServiceReqBody)
                .when()
                .patch(customerConfig.getBasePath() + CUSTOMER_URL)
                .then().log().all().statusCode(200);

        return this;
    }

    protected Misc getThis() {
        return this;
    }
}
