package ahb.experience.onboarding.childMarketPlace.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import ahb.experience.onboarding.request.parent.ParentLoginReqBody;
import ahb.experience.onboarding.response.Parent.ParentLoginRes;
import uk.co.deloitte.banking.base.BaseStep;

import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class ParentApis extends BaseStep<ParentApis> {

    private final String PARENT_LOGIN_URL = "/onboarding/public/customer/login";

    public ParentApis parentLogin(final String accessToken) {
        String mobPrefix= "5017";
        String mobNumber = "37156";
        RestAssured.defaultParser = Parser.JSON;
        parentLoginRes = given()
                .config(config)
                .log().all()
                .header("x-fapi-interaction-id", "4A7B2089-FG34-45F9-I90O-401E5C" + CHILD_DEVICE_ID)
                .header("X-Request-Id", "Benz-"+ DEVICE_ID)
                .header("Authorization", "Bearer " + accessToken)
                .header("x-jws-signature", "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVFDSUM4MUZ4akdhOTg1emlYVFRKVW5wSitvRGl3Z2M5MHlPcHVZWTFUMkR2MW5BaUJUZXNOMkRTMVpDWnQxdXBGY1FpdkdvTDZLbU5WWGlidTlFUkVkSGJLN3pRPT0ifQ.77ulbLQt0qZn1779KK7P7h8S4LGAv6TTpedaXfh2vMhaVmBCzV_5G3_6jw_sn4YjX-IKJQ6_7p2zWfCYn3ACOA")
                .contentType(ContentType.JSON)
                .when()
                .body(ParentLoginReqBody.builder()
                        .deviceId(DEVICE_ID+DEVICE_ID)
                        .mobileNumber("+971"+ mobPrefix + mobNumber)
                        .build())
                .post(authConfiguration.getExperienceBasePath() +PARENT_LOGIN_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().body().as(ParentLoginRes.class);

        return this;
    }

    @Override
    protected ParentApis getThis() {
        return this;
    }
}
