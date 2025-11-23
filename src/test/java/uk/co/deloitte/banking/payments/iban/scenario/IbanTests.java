package uk.co.deloitte.banking.payments.iban.scenario;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;

import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.payments.iban.api.IbanApi;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IbanTests {

    @Inject
    private AuthenticateApi authenticateApi;

    @Inject
    private IbanApi ibanApi;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private AlphaTestUser alphaTestUser;


    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);

            this.alphaTestUserBankingCustomerFactory.setUpBankingCustomer(this.alphaTestUser);
        }
    }

    @BeforeEach
    void setUpTest() {

        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }


    @ParameterizedTest
    @ValueSource(strings = {"GB33BUKB20201555555555", "GB94BARC10201530093459", "LC14BOSL123456789012345678901234", "NO8330001234567"})
    public void valid_iban_request(String validIban) {
        TEST("AHBDB-1406 - valid iban is returned by the service with a 200 response : " + validIban);
        TEST("valid_iban_request");

        //TODO map body to OBReadAccount6 once deserizialing issue has been resolved in the tester

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I have a valid iban");
        THEN("I get a 200 response");
        this.ibanApi.getIban(alphaTestUser, 200, validIban);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB94BARC20201530093459", "GB24BARC20201630093459", "GB2LABBY09012857201707", "GB00HLFX11016111455365", "GB96BARC202015300934591", "GB94BARC10201530093459$%£"})
    public void invalid_iban_request(String invalidIban) {
        envUtils.ignoreTestInEnv("NFT IBAN stubbed", Environments.NFT);
        TEST("AHBDB-1406 - invalid iban is returned by the service with a 400 response : " + invalidIban);
        TEST("invalid_iban_request");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I have a valid iban");
        THEN("I get a 400 response");
        OBErrorResponse1 errorResponse1 = this.ibanApi.getIbanError(alphaTestUser, 400, invalidIban);
        DONE();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB94BARC20", "GB24BARC2020163009345920201630093459"})
    public void invalid_iban_request_length(String invalidIbanLength) {
        TEST("AHBDB-1406 - invalid iban is returned by the service with a 400 response : " + invalidIbanLength);
        TEST("invalid_iban_request_length");

        GIVEN("I have a valid access token and account scope");
        setupTestUser();
        WHEN("I have a valid iban");
        THEN("I get a 400 response");
        OBErrorResponse1 errorResponse1 = this.ibanApi.getIbanError(alphaTestUser, 400, invalidIbanLength);
        DONE();
    }
}
