package uk.co.deloitte.banking.ahb.dtp.test.util;


import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira.XrayJiraRest;
import uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira.constants.Journey;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Optional;


@Slf4j
public class BDDUtils {

    private static final Optional<String> PRINT_BDD_TO_FILE = Optional.ofNullable(System.getenv("PRINTBDDTOFILE"));
    private static final Optional<String> CREATE_JIRA_TEST = Optional.ofNullable(System.getenv("CREATEJIRATEST"));



    private static HashMap<String, List<String>> testMap = new HashMap<>();
    private static List<String> bddList = new ArrayList<>();
    private static String storyNumber = "placeholder";


    public static int indentLevel = 0;
    public static boolean loggingActive = true;

    public static final String TEST_BDD = "Test: ";
    public static final String JOURNEY_BDD = "Journey: ";
    public static final String GIVEN_BDD = "Given: ";
    public static final String WHEN_BDD = "When: ";
    public static final String THEN_BDD = "Then: ";
    public static final String AND_BDD =  "And: ";
    public static final String NOTE_BDD = "NOTE: ";
    public static final String DONE_BDD = "Test complete";


    public static void TEST(String message) {
        indentLevel = 0;
        bdd(bddBuilder().append(TEST_BDD).append(message));
        indentLevel++;
    }

    public static void STORY(String message) {
        indentLevel = 0;
        bdd(bddBuilder().append(message));
        indentLevel++;
    }

    public static void JOURNEY(Journey journey) {
        indentLevel = 0;
        bdd(bddBuilder().append(JOURNEY_BDD).append(journey.getJourney()));
        indentLevel++;
    }

    public static void GIVEN(String message) {
        bdd(bddBuilder().append(GIVEN_BDD).append(message));
    }

    public static void WHEN(String message) {
        bdd(bddBuilder().append(WHEN_BDD).append(message));
    }

    public static void THEN(String message) {
        bdd(bddBuilder().append(THEN_BDD).append(message));
    }

    public static void AND(String message) {
        indentLevel++;
        bdd(bddBuilder().append(AND_BDD).append(message));
        indentLevel--;
    }

    public static void NOTE(String message) {
        indentLevel++;
        indentLevel++;
        bdd(bddBuilder().append("NOTE: ").append(message));
        indentLevel--;
        indentLevel--;
    }

    public static void DONE() {
        indentLevel = 0;
        bdd(bddBuilder().append(DONE_BDD));
    }

    public static void DONE(String message) {
        indentLevel = 0;
        bdd(bddBuilder().append(DONE_BDD + ":: ").append(message));
    }

    public static void FAIL(String message) {
        indentLevel = 0;
        bdd(bddBuilder().append("Test Failed:: ").append(message));
    }

    private static void bdd(StringBuilder gherkin) {
        writeBddStepsToFile(gherkin.toString());
        writeBddStepsToMap(gherkin.toString());
        if (loggingActive) {
            log.info(gherkin.toString());
//            System.out.println(gherkin);
        }
    }

    private static StringBuilder bddBuilder() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\t".repeat(Math.max(0, indentLevel)));
        return stringBuilder;
    }

    private static void writeBddStepsToFile(String bdd) {
        if (PRINT_BDD_TO_FILE.isPresent() && PRINT_BDD_TO_FILE.get().equals("TRUE")) {
            try {
                BufferedWriter output = new BufferedWriter(new FileWriter("BDD_Output.txt", true));
                output.write(bdd.stripLeading());
                output.newLine();

                if (bdd.contains(DONE_BDD)) {
                    output.newLine();
                }
                output.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeBddStepsToMap(String bdd) {
        //only turned on if the env variable is present and equal to TRUE
        if (CREATE_JIRA_TEST.isPresent() && CREATE_JIRA_TEST.get().equals("TRUE")) {

            //set the story number
            if (bdd.contains("AHBDB")) {
                storyNumber = bdd.stripLeading();
            }
            //add bdd to hashmap with story as the key
            bddList.add(bdd.stripLeading());
            testMap.put(storyNumber, bddList);


            //BDD_DONE marks the end of the test
            if (bdd.contains(DONE_BDD)) {
                //reset the bdd map

                //send to jira xray
                XrayJiraRest.sendTestToJira(testMap);
                testMap = new HashMap<>();
            }
        }
    }
}
