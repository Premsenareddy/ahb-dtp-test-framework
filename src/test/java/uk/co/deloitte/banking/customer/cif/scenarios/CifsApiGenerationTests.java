package uk.co.deloitte.banking.customer.cif.scenarios;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomDateOfBirth;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.commons.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.cif.model.CifResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.cif.CifsApi;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;


@Tag("@BuildCycle5")
@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CifsApiGenerationTests {
    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private CifsApi cifsApi;

    @Inject
    private CustomerApiV2 customerApi;

    @Inject
    private EnvUtils envUtils;

    private void setupTestUser() {
        NOTE("Registering and creating a customer");
        envUtils.ignoreTestInEnv(Environments.NFT);
        AlphaTestUser alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
    }

    @Test
    public void happy_path_webhook_post_customer_CIF_200_OK() {
        TEST("AHBDB-8204: AC1 Return CIF - 200 OK");

        WHEN("The client attempts to generate a cif number");
        final CifResponse cif = this.cifsApi.generateCifLegacy(generateRandomDateOfBirth(), generateRandomMobile());

        AND("CIF is populated");
        Assertions.assertTrue(StringUtils.isNotBlank(cif.getCifNumber()));

        DONE();
    }

    @Test
    public void happy_path_webhook_submit_customer_CIF_200_OK() {
        TEST("AHBDB-8204: AC1 Return CIF - 200 OK");

        WHEN("The client attempts submits a cif number");
        final CifResponse cif = this.cifsApi.submitCif();

        AND("CIF is populated");
        Assertions.assertTrue(StringUtils.isNotBlank(cif.getCifNumber()));

        DONE();
    }

    @Test
    public void happy_path_customer_with_no_eid_can_generate_cif_post() {
        envUtils.ignoreTestInEnv("AHBDB-13552", Environments.NFT);
        TEST("AHBDB-8204: AC1 Return CIF - 200 OK");

        WHEN("The client attempts to generate a cif number");
        final CifResponse cif = this.cifsApi
                .generateCifLegacy(null, generateRandomDateOfBirth(), generateRandomMobile());

        AND("CIF is populated");
        Assertions.assertTrue(StringUtils.isNotBlank(cif.getCifNumber()));

        DONE();
    }

    @Test
    public void happy_path_customer_with_no_eid_can_generate_cif_put() {
        envUtils.ignoreTestInEnv("AHBDB-13552", Environments.NFT);

        TEST("AHBDB-8204: AC1 Return CIF - 200 OK");

        WHEN("The client attempts submits a cif number");
        final CifResponse cif = this.cifsApi.submitCifNumber(null,  randomNumeric(7).replace("0", "1"));

        AND("CIF is populated");
        Assertions.assertTrue(StringUtils.isNotBlank(cif.getCifNumber()));

        DONE();
    }

    @Test
    public void negative_test_customer_with_no_eid_can_generate_cif_put_but_second_time_gives_conflict() {
        envUtils.ignoreTestInEnv("AHBDB-13552", Environments.NFT);

        TEST("AHBDB-8204: AC1 Return CIF - 200 OK");

        WHEN("The client attempts submits a cif number");
        String cif = randomNumeric(7).replace("0", "1");

        final CifResponse cifResponse = this.cifsApi.submitCifNumber(null,  cif);

        AND("CIF is populated");
        Assertions.assertTrue(StringUtils.isNotBlank(cifResponse.getCifNumber()));

        OBErrorResponse1 error = cifsApi.submitCifNumberError(null, cif, 409);
        Assertions.assertEquals("UAE.ERROR.CONFLICT", error.getCode());
        DONE();
    }
}
