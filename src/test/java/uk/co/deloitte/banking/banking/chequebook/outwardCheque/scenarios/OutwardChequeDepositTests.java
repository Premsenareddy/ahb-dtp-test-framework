package uk.co.deloitte.banking.banking.chequebook.outwardCheque.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.banking.chequebook.outwardCheque.api.OutwardChequeDepositAPI;
import uk.co.deloitte.banking.banking.chequebook.outwardCheque.api.OuwardChequeClearingAPI;
import uk.co.deloitte.banking.banking.chequebook.outwardCheque.api.OutwardChequeReturnAPI;


import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@Slf4j
@Singleton
public class OutwardChequeDepositTests {

    @Inject
    OutwardChequeDepositAPI outwarddepositChequeApi;

    @Inject
    OuwardChequeClearingAPI ouwardChequeClearingApi;

    @Inject
    OutwardChequeReturnAPI outwardChequeReturnAPI;

    @Test
    public void outwardChequeDepositClearing() throws JSONException {
        TEST("Deposit the Cheque and Clear the Cheque");

        GIVEN("The customer Deposits a Cheque");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        String chequeID = outwarddepositChequeApi.outwardChequeDeposit();
        WHEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Deposited Cheque");
        ouwardChequeClearingApi.outwardChequeClearing_requestPayload(chequeID);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Deposited Cheque");
        DONE();
    }

    @Test
    public void outwardChequeDepositReturn() throws JSONException {
        TEST("Deposit the Cheque and Return the Cheque");

        GIVEN("The customer Deposits a Cheque");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        String chequeID = outwarddepositChequeApi.outwardChequeDeposit();
        WHEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Deposited Cheque");
        outwardChequeReturnAPI.outwardChequeReturning_requestPayload(chequeID);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Deposited Cheque");
        DONE();
    }
}
