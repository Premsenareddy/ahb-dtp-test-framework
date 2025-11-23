package uk.co.deloitte.banking.banking.chequebook.internalChequeAPI.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.banking.chequebook.internalChequeAPI.api.ChequeIssueAPI;
import uk.co.deloitte.banking.banking.chequebook.internalChequeAPI.api.ChequeIssueListAPI;
import uk.co.deloitte.banking.banking.chequebook.internalChequeAPI.api.ChequeReturnListAPI;
import uk.co.deloitte.banking.banking.chequebook.internalChequeAPI.api.ChequeRegisterListAPI;
import uk.co.deloitte.banking.banking.chequebook.internalChequeAPI.api.ChequeAccountListAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@Slf4j
@Singleton
public class InternalChequeAPITests {

    @Inject
    ChequeIssueAPI chequeIssueAPI;

    @Inject
    ChequeIssueListAPI chequeIssueListAPI;

    @Inject
    ChequeRegisterListAPI chequeRegisterListAPI;

    @Inject
    ChequeReturnListAPI chequeReturnListAPI;

    @Inject
    ChequeAccountListAPI chequeAccountListAPI;

    String cusAccountId = "019906263001";
    String accountId = "30000000333";

    @Test
    public void chequeIssueListApi() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        chequeIssueListAPI.ChequeIssueList_requestPayload(accountId);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }

    @Test
    public void chequeIssueApi() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        chequeIssueAPI.ChequeIssue_requestPayload();
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }

    @Test
    public void chequeRegisterListApi() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        chequeRegisterListAPI.ChequeRegisterList_requestPayload(accountId);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }

    @Test
    public void chequeReturnListApi() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        chequeReturnListAPI.ChequeReturnList_requestPayload(accountId);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }

    @Test
    public void chequeAccountListApi() throws JSONException {
        TEST("Inward Clear the Cheque");
        GIVEN("The customer Cheque is cleared through inward");
        AND("The client calls DTP to request the Cheque ID which is deposited");
        chequeAccountListAPI.ChequeAccountList_requestPayload(cusAccountId);
        THEN("The client calls the DTP by passing the Cheque ID and trying to Clear the Cheque");
        DONE();
    }
}
