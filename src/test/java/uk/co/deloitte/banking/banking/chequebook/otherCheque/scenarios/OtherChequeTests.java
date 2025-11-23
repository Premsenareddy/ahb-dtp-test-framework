package uk.co.deloitte.banking.banking.chequebook.otherCheque.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.banking.chequebook.otherCheque.api.ChequeSettlementAPI;
import uk.co.deloitte.banking.banking.chequebook.otherCheque.api.onusInwardChequeClearingAPI;
import uk.co.deloitte.banking.banking.chequebook.otherCheque.api.ClosedAccountsList;
import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@Slf4j
@Singleton
public class OtherChequeTests {

    @Inject
    ChequeSettlementAPI chequeSettlementAPI;

    @Inject
    onusInwardChequeClearingAPI onusInwardChequeClearingAPI;

    @Inject
    ClosedAccountsList closedAccountsList;

    String cusAccountId = "019906263001";

    @Test
    public void inwardChequeDepositClearing() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        chequeSettlementAPI.ChequeSettlement_requestPayload();
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }

    @Test
    public void onusInwardChequeClearing() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        onusInwardChequeClearingAPI.onusInwardChequeClearing_requestPayload();
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }

    @Test
    public void closedAccountsList() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        closedAccountsList.ClosedAccountsList_requestPayload(cusAccountId);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }
}
