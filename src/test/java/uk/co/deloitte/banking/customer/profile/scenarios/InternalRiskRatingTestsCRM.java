package uk.co.deloitte.banking.customer.profile.scenarios;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.api.customer.model.internalrisk.*;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;

import javax.inject.Inject;

import java.time.ZonedDateTime;
import java.util.Set;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle3")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InternalRiskRatingTestsCRM {

    private AlphaTestUser alphaTestUser;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    private static final String NULL_RATING_RESPONSE = "Reason cannot be null for this internal risk rating";

    private static final String NULL_RATING_RESPONSE_CODE = "0002";

    private static final String VALID_RATING = "INCREASED";

    private static final String VALID_REASON = "NATIONALITY";

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    @ParameterizedTest
    @CsvSource({"INCREASED, NATIONALITY", "UAE_PEP, E_NAME_CHECKER_HIT", "NON_UAE_PEP, NATURE_OF_BUSINESS", "UNACCEPTABLE, NATIONALITY"})
    public void customer_receives_internal_risk_rating_and_reason_201_response(String riskRating, String riskReason) {
        TEST("AHBDB-4845: AC1 Post Internal Risk Rating - 201 Created");
        TEST("AHBDB-5893: AC1 Customer receives an internal risk rating - Rating & Reason - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .reason(Set.of(OBRiskReason.valueOf(riskReason)))
                        .rating(OBInternalRiskRating.valueOf(riskRating))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 obWriteInternalRiskRatingResponse1 = this.customerApi.addInternalRiskRating(alphaTestUser, obWriteInternalRiskRating1);
        Assertions.assertEquals(obWriteInternalRiskRatingResponse1.getData().getRating(), OBInternalRiskRating.valueOf(riskRating));
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"PRE_DEFINED_NEUTRAL", "NEUTRAL"})
    public void customer_receives_internal_risk_rating_rating_only_201_response(String riskRating) {
        TEST("AHBDB-4845: AC1 Post Internal Risk Rating - 201 Created");
        TEST("AHBDB-5894: AC1b Customer receives an internal risk rating - Rating - 201 response");
        TEST("AHBDB-8228: AC3 Remove optional data.Reason field - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");
        AND("The risk rating is either PRE_DEFINED_NEUTRAL or NEUTRAL");

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(riskRating))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();

        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 obWriteInternalRiskRatingResponse1 = this.customerApi.addInternalRiskRating(alphaTestUser, obWriteInternalRiskRating1);
        Assertions.assertEquals(obWriteInternalRiskRatingResponse1.getData().getRating(), OBInternalRiskRating.valueOf(riskRating));
        DONE();
    }

    @ParameterizedTest
    @CsvSource({"INCREASED, E_NAME_CHECKER_HIT, NATIONALITY"})
    public void customer_receives_internal_risk_rating_2_reasons_201_response(String riskRating, String riskReason1, String riskReason2) {
        TEST("AHBDB-4845: AC1 Post Internal Risk Rating - 201 Created");
        TEST("AHBDB-5895: AC1c Customer receives an internal risk rating - 2 Reasons - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(riskRating))
                        .reason(Set.of(OBRiskReason.valueOf(riskReason1), OBRiskReason.valueOf(riskReason2)))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 obWriteInternalRiskRatingResponse1 = this.customerApi.addInternalRiskRating(alphaTestUser, obWriteInternalRiskRating1);
        Assertions.assertEquals(obWriteInternalRiskRatingResponse1.getData().getReason().size(), 2);

        DONE();
    }

    @ParameterizedTest
    @CsvSource({"INCREASED, E_NAME_CHECKER_HIT, NATIONALITY, NATURE_OF_BUSINESS"})
    public void customer_receives_internal_risk_rating_3_reasons_201_response(String riskRating, String riskReason1, String riskReason2, String riskReason3) {
        TEST("AHBDB-4845: AC1 Post Internal Risk Rating - 201 Created");
        TEST("AHBDB-5896: AC1d Customer receives an internal risk rating - 3 Reasons - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to store the customer's Internal Risk Rating information");

        WHEN("We pass the request to CRM to create the internal risk rating with a valid JWT token and valid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(riskRating))
                        .reason(Set.of(OBRiskReason.valueOf(riskReason1), OBRiskReason.valueOf(riskReason2), OBRiskReason.valueOf(riskReason3)))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 201 created response");
        OBWriteInternalRiskRatingResponse1 obWriteInternalRiskRatingResponse1 = this.customerApi.addInternalRiskRating(alphaTestUser, obWriteInternalRiskRating1);
        Assertions.assertEquals(obWriteInternalRiskRatingResponse1.getData().getReason().size(), 3);

        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"INCREASED", "UAE_PEP", "NON_UAE_PEP", "UNACCEPTABLE"})
    public void invalid_data_field_missing_mandatory_reason_400_response(String riskRating) {
        TEST("AHBDB-4845: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5897: AC2 Invalid data field - Remove Mandatory Data.Reason value for Reason when the Rating is: <Rating> - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Internal Risk Rating");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but with invalid field inputs");
        OBWriteInternalRiskRating1 obWriteInternalRiskRating1 = OBWriteInternalRiskRating1.builder()
                .data(OBInternalRiskRatingDetails1.builder()
                        .rating(OBInternalRiskRating.valueOf(riskRating))
                        .timestamp(ZonedDateTime.now())
                        .build())
                .build();
        THEN("We'll receive a 400 bad request");
        OBErrorResponse1 error = this.customerApi.addInternalRiskRatingError(alphaTestUser, obWriteInternalRiskRating1, 400);
        Assertions.assertTrue(error.getMessage().contains(NULL_RATING_RESPONSE), "Error message was not as expected, test expected : " + NULL_RATING_RESPONSE);
        Assertions.assertTrue(error.getCode().equals("UAE.ERROR.BAD_REQUEST"), "Error code was not as expected, test expected : " + NULL_RATING_RESPONSE_CODE);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2021-02-19T16:18:42", "ABC", "!@£$%^&^", "", "2021-02-19"})
    public void invalid_data_field_invalid_timestamp_400_response(String invalidTimeStamp) throws JSONException {
        TEST("AHBDB-4845: AC2 Post - missing or invalid data - 400 bad request");
        TEST("AHBDB-5898: AC2 Invalid data field - Invalid Data.Timestamp field - 400 response");
        setupTestUser();
        GIVEN("We have received a post request from the client to store the Internal Risk Rating");
        WHEN("We pass the request to CRM to create the customer with a valid JWT token but with invalid field inputs");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Rating", VALID_RATING);
        jsonObject.put("Reason", VALID_REASON);
        jsonObject.put("Timestamp", invalidTimeStamp);

        JSONObject data = new JSONObject();
        data.put("Data", jsonObject);

        THEN("We'll receive a 400 bad request");

        this.customerApi.addInternalRiskRatingError(alphaTestUser, data, 400);
        DONE();
    }

    @Test
    public void missing_mandatory_ratings_field_400_response() throws JSONException {
        TEST("AHBDB-4845: AC3 Missing data field - 400 response");
        TEST("AHBDB-5899: AC3 Missing data field - 400 response - Remove Mandatory Data.Rating value - 400 response");
        setupTestUser();
        GIVEN("The client wants to store the internal risk rating");
        AND("There are missing mandatory fields");

        WHEN("The client updates the customer profile");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Reason", VALID_REASON);
        jsonObject.put("Timestamp", ZonedDateTime.now());

        JSONObject data = new JSONObject();
        data.put("Data", jsonObject);

        THEN("We'll receive a 400 bad request");

        this.customerApi.addInternalRiskRatingError(alphaTestUser, data, 400);
        DONE();
    }

}
