package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.api.customer.model.internalrisk.*;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Set;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@Tag("@BuildCycle2")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InternalRiskRatingTests {

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory newCustomerCreationUtil;

    @Inject
    private CustomerApi customerApi;

    private AlphaTestUser alphaTestUser;

    private static final String NULL_REASON = "Reason cannot be null for this internal risk rating";

    private static final String NULL_RATING = "must not be null";


    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = newCustomerCreationUtil.createCustomerWithValidatedEmail();
        }
    }

    @ParameterizedTest
    @CsvSource({"INCREASED, NATIONALITY", "NON_UAE_PEP, E_NAME_CHECKER_HIT", "UAE_PEP, NATURE_OF_BUSINESS", "UNACCEPTABLE, NATIONALITY"})
    public void update_customer_successfully_valid_risk_ratings(String validRiskRating, String validRiskReason) {
        TEST(String.format("AHBDB-285 customer can be updated with a valid internal risk rating and reason Risk rating : %s Risk Reason : %s ", validRiskRating, validRiskReason));
        TEST("AHBDB-3083 - AC1 Customer receives an internal risk rating");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");

        WHEN("I update the customer with a valid internal risk rating and reason");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .reason(Set.of(OBRiskReason.valueOf(validRiskReason)))
                        .rating(OBInternalRiskRating.valueOf(validRiskRating))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        THEN("A 201 is returned from the service");
        OBWriteInternalRiskRatingResponse1 obWriteInternalRiskRatingResponse1 = this.customerApi.addInternalRiskRating(alphaTestUser, obWriteInternalRiskRating1);
        Assertions.assertEquals(obWriteInternalRiskRatingResponse1.getData().getRating(), OBInternalRiskRating.valueOf(validRiskRating));
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"INCREASED, NATIONALITY, E_NAME_CHECKER_HIT, NATURE_OF_BUSINESS"})
    public void update_customer_successfully_valid_risk_ratings_multiple_reasons(String validRiskRating, String validRiskReason1, String validRiskReason2, String validRiskReason3) {
        TEST(String.format("AHBDB-285 customer can be updated with multiple valid internal risk rating reasons Rating : %s Reason 1 : %s, Reason 2 : %s, Reason 3 : %s", validRiskRating, validRiskReason1, validRiskReason2, validRiskReason3));
        TEST("AHBDB-3083 - AC1 Customer receives an internal risk rating");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");

        WHEN("I update the customer with a valid internal risk rating");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .reason(Set.of(OBRiskReason.valueOf(validRiskReason1), OBRiskReason.valueOf(validRiskReason2), OBRiskReason.valueOf(validRiskReason3)))
                        .rating(OBInternalRiskRating.valueOf(validRiskRating))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        THEN("A 201 is returned from the service");
        OBWriteInternalRiskRatingResponse1 obWriteInternalRiskRatingResponse1 = this.customerApi.addInternalRiskRating(alphaTestUser, obWriteInternalRiskRating1);
        Assertions.assertEquals(obWriteInternalRiskRatingResponse1.getData().getReason().size(), 3);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PRE_DEFINED_NEUTRAL", "NEUTRAL"})
    public void update_customer_successfully_valid_risk_rating_null_reason(String validRiskRatingForNullReason) {
        TEST("AHBDB-285 customer can be updated with a valid internal risk rating without a reason, valid rating " + validRiskRatingForNullReason);
        TEST("AHBDB-3083 - AC1 Customer receives an internal risk rating");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");

        WHEN("I update the customer with a valid internal risk rating without a reason");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(validRiskRatingForNullReason))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        THEN("A 201 is returned from the service");
        OBWriteInternalRiskRatingResponse1 obWriteInternalRiskRatingResponse1 = this.customerApi.addInternalRiskRating(alphaTestUser, obWriteInternalRiskRating1);
        Assertions.assertEquals(obWriteInternalRiskRatingResponse1.getData().getRating(), OBInternalRiskRating.valueOf(validRiskRatingForNullReason));
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"NATIONALITY", "E_NAME_CHECKER_HIT", "NATURE_OF_BUSINESS"})
    public void negative_update_customer_invalid_null_rating(String riskReasonNullRating) {
        TEST("AHBDB-285 customer can't be updated with a valid internal risk rating and a null rating");
        TEST("AHBDB-3084 - AC2 - null field - 400");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");

        WHEN("I update the customer with a valid internal risk rating");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .reason(Set.of(OBRiskReason.valueOf(riskReasonNullRating)))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        THEN("A 400 error response is returned from the service");
        OBErrorResponse1 obErrorResponse1 = this.customerApi.addInternalRiskRatingError(alphaTestUser, obWriteInternalRiskRating1, 400);
        Assertions.assertTrue(obErrorResponse1.getMessage().contains(NULL_RATING), "Error message was not as " +
                "expected, test expected : " + NULL_RATING);
        DONE();
    }


    @ParameterizedTest
    @ValueSource(strings = {"INCREASED", "NON_UAE_PEP", "UAE_PEP", "UNACCEPTABLE"})
    public void negative_update_customer_risk_ratings_null_reason(String riskRatingNullReason) {
        TEST("AHBDB-285 customer can't be updated with a null internal risk rating and a valid rating");
        TEST("AHBDB-3084 - AC2 - null field - 400");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");

        WHEN("I update the customer with a valid internal risk rating");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(riskRatingNullReason))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        THEN("The a 400 response is returned from the service");
        OBErrorResponse1 obErrorResponse1 = this.customerApi.addInternalRiskRatingError(alphaTestUser, obWriteInternalRiskRating1, 400);
        Assertions.assertTrue(obErrorResponse1.getMessage().contains(NULL_REASON), "Error message was not as " +
                "expected, test expected : " + NULL_REASON);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2021-02-19T16:18:42", "AB@", "1234567"})
    public void negative_update_customer_risk_ratings_invalid_timestamp(String invalidTimeStamp) throws JSONException {
        TEST("AHBDB-285 - internal risk rating - null reason returns 400");
        TEST("AHBDB-3086 - AC3 - invalid timestamp - 400");
        setupTestUser();
        GIVEN("I have a valid access token and account scope");

        WHEN("I update the customer with a valid internal risk rating");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Rating", "PRE_DEFINED_NEUTRAL");
        jsonObject.put("Reason", "PRE_DEFINED_NEUTRAL");
        jsonObject.put("Timestamp", invalidTimeStamp);

        JSONObject data = new JSONObject();
        data.put("Data", jsonObject);

        THEN("The a 400 response is returned from the service");
        this.customerApi.addInternalRiskRatingError(alphaTestUser, data, 400);

        DONE();
    }
}
