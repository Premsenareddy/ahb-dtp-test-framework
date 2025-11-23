package uk.co.deloitte.banking.banking.chequebook.inwardCheque.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.banking.chequebook.inwardCheque.api.InwardChequeClearingAPI;
import uk.co.deloitte.banking.banking.chequebook.inwardCheque.api.InwardChequeReturnAPI;
import uk.co.deloitte.banking.banking.chequebook.inwardCheque.api.InwardReturnChequeReverseAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@Slf4j
@Singleton
public class InwardChequeTests {

    @Inject
    InwardChequeClearingAPI inwardChequeClearingAPI;

    @Inject
    InwardChequeReturnAPI inwardChequeReturnAPI;

    @Inject
    InwardReturnChequeReverseAPI inwardReturnChequeReverseAPI;

    String chequeId = "FT21186F65FS";

    @Test
    public void inwardChequeDepositClearing() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        inwardChequeClearingAPI.inwardChequeClearing_requestPayload();
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }


    @Test
    public void inwardChequeDepositReturning() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        inwardChequeReturnAPI.inwardChequeReturning_requestPayload();
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }

    @Test
    public void inwardChequeReturnReverse() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        inwardReturnChequeReverseAPI.inwardReturnChequeReverse_requestPayload(chequeId);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }
}
