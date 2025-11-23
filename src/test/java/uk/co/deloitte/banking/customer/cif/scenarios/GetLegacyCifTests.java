package uk.co.deloitte.banking.customer.cif.scenarios;

import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifDetailsResp;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.ErrorResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.cif.CifsApi;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("cif")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetLegacyCifTests extends AdultOnBoardingBase {

    @Inject
    CifsApi cifsApi;

    @BeforeEach
    void ignore() {
        envUtils.ignoreTestInEnv(Environments.SIT, Environments.CIT);
    }

    @ParameterizedTest
    @MethodSource("getLegacyValidEmiratesId")
    public void getLegacyCifDetails_for_valid_user_by_emiratedId(String emiratesId) {
        TEST("AHBDB-15961 - Successfully fetch legacy cif by emiratesId");
        GIVEN("I have a valid legacy customer emirates ID");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("emiratesId", emiratesId);

        WHEN("I call customer get cif service by EID");
        final CifDetailsResp legacyCifDataResp = this.cifsApi
                .getLegacyCIFByEmiratesId(queryParams, CifDetailsResp.class, HttpStatus.OK);

        THEN("I will get successgull response with CIF details");
        Assertions.assertNotNull(legacyCifDataResp.getData().getLegacyCustomerDetails().cIFId);

        DONE();
    }

    @ParameterizedTest
    @MethodSource("getDigitalValidEmiratesId")
    @Disabled("Not applicable now")
    public void getDigitalCifDetails_for_valid_user_by_emiratedId(String emiratesId) {
        TEST("AHBDB-15961 - Successfully fetch legacy cif by emiratesId");
        GIVEN("I have a valid legacy customer emirates ID");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("emiratesId", emiratesId);

        WHEN("I call customer get cif service by EID");
        final CifDetailsResp legacyCifDataResp = this.cifsApi
                .getLegacyCIFByEmiratesId(queryParams, CifDetailsResp.class, HttpStatus.OK);

        THEN("I will get successgull response with CIF details");
        Assertions.assertNotNull(legacyCifDataResp.getData().getLegacyCustomerDetails().cIFId);

        DONE();
    }

    @Test
    public void getLegacyCifDetails_for_valid_user_by_emiratedId_error1() {
        TEST("AHBDB-15961 - Successfully fetch legacy cif by emiratesId");
        GIVEN("I have a invalid legacy customer emirates ID");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("emiratesId", "123");

        WHEN("I call customer get cif service by EID");
        final ErrorResponse error = this.cifsApi
                .getLegacyCIFByEmiratesId(queryParams, ErrorResponse.class, HttpStatus.BAD_GATEWAY);

        THEN("I will get successgull response with CIF details");
        Assertions.assertEquals(error.Message, "Invalid HPS error code");

        DONE();
    }

    @Test
    public void getLegacyCifDetails_for_valid_user_by_emiratedId_error2() {
        TEST("AHBDB-15961 - Should throw error for service called with empty emiratesID");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("emiratesId", "");

        WHEN("I call customer get cif service without EID");
        final ErrorResponse error = this.cifsApi
                .getLegacyCIFByEmiratesId(queryParams, ErrorResponse.class, HttpStatus.BAD_REQUEST);

        THEN("I will get successgull response with CIF details");
        MatcherAssert.assertThat(error.Message, CoreMatchers.containsString("emiratesId: must not be blank"));

        DONE();
    }

    @Test
    public void getLegacyCifDetails_for_valid_user_by_emiratedId_error3() {
        TEST("AHBDB-15961 - Should throw error for service called without emiratesID");
        Map<String, String> queryParams = new HashMap<>();

        WHEN("I call customer get cif service without EID");
        final ErrorResponse error = this.cifsApi
                .getLegacyCIFByEmiratesId(queryParams, ErrorResponse.class, HttpStatus.BAD_REQUEST);

        THEN("I will get successgull response with CIF details");
        Assertions.assertEquals(error.Message, "Required QueryValue [emiratesId] not specified");

        DONE();
    }

    private static Stream<Arguments> getLegacyValidEmiratesId() {
        return Stream.of(
                Arguments.of("784196800447361"),
                Arguments.of("784198376364927"),
                Arguments.of("784196800113701"),
                Arguments.of("784196800699681"),
                Arguments.of("784196800699691"),
                Arguments.of("784196800699701")
        );
    }
}
