package uk.co.deloitte.banking.customer.cases.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventV1;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseTypeEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ReasonEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.ProcessOriginEnum;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.models.CaseEventRequest;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.DevSimConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.Alt;
import uk.co.deloitte.banking.http.kafka.BaseEvent;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CasesApi extends BaseApi {

    @Inject
    DevSimConfiguration devSimConfiguration;

    private static final String API_CASES_PUBLISH = "/api/cases/publish";

    public Response createCaseInCRM(CaseEventV1 body, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Basic " + generateDevSimBasicAuth())
                .header(X_API_KEY, devSimConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(body)
                .post(devSimConfiguration.getBasePath() + API_CASES_PUBLISH)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    public Response createCase(final AlphaTestUser alphaTestUser, JSONObject body, int statusCode) {

        assertNotNull(alphaTestUser);
        assertNotNull(alphaTestUser.getLoginResponse());
        assertFalse(isBlank(alphaTestUser.getLoginResponse().getAccessToken()));

        return given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Basic " + generateDevSimBasicAuth())
                .header(X_API_KEY, devSimConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .body(body.toString())
                .post(devSimConfiguration.getBasePath() + API_CASES_PUBLISH)
                .then().log().all()
                .statusCode(statusCode).assertThat()
                .extract().response();
    }

    private String generateDevSimBasicAuth() {
        return Base64.getEncoder().encodeToString(("dev-cleardown-user:ZGQyNDhkYTAtZjBhZS00NmEyLTkyYmEtZmM3YWVhZmFhMTli")
                .getBytes(StandardCharsets.UTF_8));
    }

    public JSONObject generateCaseBodyJson(String customerId, String caseType, String title,
                                           String process, String reason, String priority) {

        String userIdLabel = "|u-";

        JSONObject altBody = new JSONObject() {
            {
                put("originProtected", false);
                put("UserId", customerId);
                put("UserIdLabel", userIdLabel);
                put("Cif", NULL);
                put("DeviceId", NULL);
                put("CorrelationId", NULL);
                put("CorrelationIdLabel", "");
                put("IdempotentId", NULL);
                put("OpenTraceId", NULL);
                put("Span", NULL);
                put("UserSegment", NULL);
                put("StartTime", 0);
                put("RequestId", NULL);
                put("HostName", NULL);
                put("RequestHeaders", NULL);
                put("Origin", NULL);
                put("PerfSpans", NULL);
            }
        };

        JSONObject metaData = new JSONObject() {
            {
                put("Alt", altBody);
                put("Origin", NULL);
                put("Date", NULL);
                put("Version", 0);
                put("Type", NULL);
            }
        };

        JSONObject eventData = new JSONObject() {
            {
                put("AdditionalDetails", NULL);
                put("Description", "Testing Description");
                put("Priority", priority);
                put("Title", title);
                put("Process", process);
                put("CustomerId", customerId);
                put("Reason", reason);
                put("CaseType", caseType);
                put("ResponsibleContactID", customerId);
            }
        };

        JSONObject caseBody = new JSONObject() {
            {
                put("Metadata", metaData);
                put("EventData", eventData);
            }
        };

        return caseBody;
    }

    public CaseEventV1 generateCaseBody(String customerId, CaseTypeEnum caseType, String title,
                                        ProcessOriginEnum process, ReasonEnum reason, String priority,
                                        String responsibleContactField) {

        String userIdLabel = "|u-" + customerId.substring(0,8);

        return CaseEventV1.builder()
                .metadata(BaseEvent.builder()
                        .alt(Alt.builder()
                                .userId(customerId)
                                .userIdLabel(userIdLabel)
                                .correlationIdLabel("")
                                .startTime(0)
                                .build())
                        .version(0)
                        .build())
                .eventData(CaseEventRequest.builder()
                        .caseType(caseType)
                        .customerId(customerId)
                        .createdOn(null)
                        .title(title)
                        .processOrigin(process)
                        .responsibleContactID(responsibleContactField)
                        .reason(reason)
                        .priority(priority)
                        .description("Test description")
                        .additionalDetails("Test details")
                        .build())
                .build();
    }

    public CaseEventV1 generateCaseBody(String customerId, CaseTypeEnum caseType, String title,
                                        ProcessOriginEnum process, ReasonEnum reason, String priority) {
        return this.generateCaseBody(customerId, caseType, title, process, reason, priority, customerId);
    }
}
