package uk.co.deloitte.banking.banking.chequebook.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.banking.chequebook.api.InwardClearingApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
@MicronautTest
@Singleton
public class InwardClearTest {

    @Inject
    private InwardClearingApi inwardClearingApi;

    @Test
    public void testCheckClearing() {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request");
        var response = inwardClearingApi.checkClearValidate();
        THEN("The client calls the DTP by passing the request and trying to Clear the Cheque");
        DONE();
    }
}
