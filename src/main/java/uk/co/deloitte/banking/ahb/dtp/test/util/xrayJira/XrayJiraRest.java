package uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira;

import uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira.model.*;

import javax.inject.Singleton;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.restassured.RestAssured.given;

import static io.restassured.http.ContentType.JSON;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.JOURNEY_BDD;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST_BDD;

@Singleton
public class XrayJiraRest {

    private static final Optional<String> CREATE_JIRA_TEST = Optional.ofNullable(System.getenv("CREATEJIRATEST"));

    private static final String JIRA_URL = "https://ahbdigitalbank.atlassian.net/rest/api/2/issue/";

    private static final String XRAY_URL = "";

    private static final String JIRA_USERNAME = "jtroughton@deloitte.co.uk";
    private static final String JIRA_SEPARATOR = ":";
    private static final String JIRA_API_KEY = "";

    //Jira Ticket Options - these could be set in the BDD or some other way
    final static String BUILD_CYCLE = "Build_Cycle_3";
    final static String ENV = "CIT";
    final static String COMPONENT = "Digital Technology Platform";

    //base64 encode username:apiKey
    private static String encodedBasicAuth() {
        return Base64.getEncoder().encodeToString((JIRA_USERNAME + JIRA_SEPARATOR + JIRA_API_KEY).getBytes(StandardCharsets.UTF_8));
    }

    public static void sendTestToJira(final HashMap<String, List<String>> bddMap) {
        //double check the env variables
        if (CREATE_JIRA_TEST.isPresent() && CREATE_JIRA_TEST.get().equals("TRUE")) {
            createdTicketResponse(bddMap);
        }
    }

    //get the ticket number again, this should always be the key but this method is a double check on it
    private static String ticketNumber(final HashMap<String, List<String>> bddMap) {

        String ticketNumber = "";
        for (Map.Entry<String, List<String>> entry : bddMap.entrySet()) {
            String key = entry.getKey();
            if (key.contains("AHBDB")) {
                ticketNumber = key;
            }
        }
        return ticketNumber;
    }

    //get all of the bdd from the map and add /n to make each part go on to a new line in jira
    private static String createTestSteps(final HashMap<String, List<String>> bddMap) {
        List<String> stepList = bddMap.get(ticketNumber(bddMap));
        List<String> bddSteps = new ArrayList<>();
        for (String step : stepList) {
            bddSteps.add(step + "\n");
        }
        String bdd = String.join(",", bddSteps);
        return bdd.replace(",", "");
    }

    //get the journey from the map
    private static String getJourney(final HashMap<String, List<String>> bddMap) {
        List<String> stepList = bddMap.get(ticketNumber(bddMap));
        String journey = "";
        for (String bdd : stepList) {
            if (bdd.contains(JOURNEY_BDD)) {
                journey = bdd;
            }
        }
        return journey.replace(JOURNEY_BDD , "");
    }

    //get the test case title
    private static String getTestTitle(final HashMap<String, List<String>> bddMap) {
        List<String> stepList = bddMap.get(ticketNumber(bddMap));
        String testTitle = "";
        for (String bdd : stepList) {
            if (bdd.contains(TEST_BDD)) {
                testTitle = bdd;
            }
        }
        return testTitle.replace(TEST_BDD , "");
    }

    //This builds the fields part of the payload which includes component, project key etc.
    private static Fields fields(final HashMap<String, List<String>> bddMap) {

        final Project project = Project.builder().key("AHBDB").build();
        final Customfield_10032 customfield_10032 = Customfield_10032.builder().value(getJourney(bddMap)).build();
        final FixVersions fixVersions = FixVersions.builder().name(BUILD_CYCLE).build();
        final Customfield_10040 customfield_10040 = Customfield_10040.builder().value(ENV).build();
        final Components components = Components.builder().name(COMPONENT).build();
        final Issuetype issuetype = Issuetype.builder().id("10024").build();

        return Fields.builder()
                .project(project)
                .customfield_10032(Collections.singletonList(customfield_10032))
                .fixVersions(Collections.singletonList(fixVersions))
                .customfield_10040(customfield_10040)
                .components(Collections.singletonList(components))
                .issuetype(issuetype)
                .description(createTestSteps(bddMap))
                .summary(getTestTitle(bddMap)).build();
    }

    //This builds the update part of the payload which links the test case to the story
    private static Update update(final HashMap<String, List<String>> bddMap) {
        final Type type = Type.builder().name("Test").inward("is tested by").outward("tests").build();
        final OutwardIssue outwardIssue = OutwardIssue.builder().key(ticketNumber(bddMap)).build();

        return Update.builder()
                .issuelinks(Collections.singletonList(Issuelinks.builder()
                        .add(Add.builder()
                                .type(type)
                                .outwardIssue(outwardIssue).build())
                        .build()))
                .build();
    }

    //build the overall ticket payload
    private static TicketRequest ticketRequest(final HashMap<String, List<String>> bddMap) {
        return TicketRequest.builder().fields(fields(bddMap)).update(update(bddMap)).build();
    }

    //using rest assured for easy tester debugging
    private static CreatedTicketResponse createdTicketResponse(final HashMap<String, List<String>> bddMap) {
        return given()
                .log().all()
                .header("Authorization", "Basic " + "encodedBasicAuth()")
                .contentType(JSON)
                .when()
                .body(ticketRequest(bddMap))
                .post(JIRA_URL)
                .then().log().all()
                .statusCode(201)
                .extract().response().getBody().as(CreatedTicketResponse.class);
    }
}
