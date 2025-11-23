package ahb.experience.onboarding.childMarketPlace.api;

import ahb.experience.onboarding.request.misc.PrivilegedReqBody;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import uk.co.deloitte.banking.base.BaseStep;

import javax.inject.Singleton;

import static io.restassured.RestAssured.given;

@Singleton
public class PrivilegesApi extends BaseStep<PrivilegesApi> {

    private static final String PRIVILEGE_URL = "/permissions/protected/privileges/{childId}";

    public PrivilegesApi assignPrivileges(String accessToken, PrivilegedReqBody privilegedReqBody) {
        RestAssured.defaultParser = Parser.JSON;
        given()
            .config(config)
            .log().all()
            .pathParams("childId", child.getChildId())
            .header("x-api-key", authConfiguration.getExperienceApiKey())
            .header("X-Request-Id", "Benz-"+ DEVICE_ID)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(ContentType.JSON)
            .body(privilegedReqBody)
            .when()
            .patch(authConfiguration.getExperienceBasePath() +PRIVILEGE_URL)
            .then().log().all().statusCode(204);

        return this;
    }

    @Override
    protected PrivilegesApi getThis() {
        return this;
    }
}
