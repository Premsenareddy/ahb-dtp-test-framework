package uk.co.deloitte.banking.journey.scenarios.adult;

import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBReadTransaction6;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;

public class AdultAccountTransactionV2Scenario extends AdultOnBoardingBase{

    OBReadTransaction6 obReadTransaction6;
    public static OBReadRelationship1 obReadRelationship1;
    public static OBReadAccount6 obReadAccount6;

    private static AlphaTestUser alphaTestUser;

    @BeforeEach
    void ignore() {
        envUtils.ignoreTestInEnv(Environments.CIT, Environments.SIT);
    }

    private void setupTestUser(String mobile) throws Throwable {
        alphaTestUser = new AlphaTestUser(mobile);
        this.alphaTestUser = alphaTestUserFactory.reRegistorDeviceAndLogin1(alphaTestUser);
    }

    @Tag("Transactions")
    @Order(1)
    @Test
    void get_statement_transactionV2_details_test_success() throws Throwable {
        TEST("AHBDB-21271 get transaction V2 by transactionStartIndex ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionStartIndex", "1");
        queryParams.put("transactionEndIndex", "2");

        setupTestUser("+555501737197");

        // Get user account details
        obReadAccount6= this.accountApi.getAccounts(alphaTestUser);

        // fetch account transactions v2
        obReadTransaction6 = this.accountApi.accountTransactionsV2(alphaTestUser,
                obReadAccount6.getData().getAccount().get(0).getAccountId(), queryParams, OBReadTransaction6.class, HttpStatus.OK);
        // assert transaction count equal to 2
        assertEquals(obReadTransaction6.getData().getTransaction().size(), 2);

        DONE();
    }

    @Tag("Transactions")
    @Order(2)
    @Test
    void get_statement_transactionV2_details_test_bad_request() {
        TEST("AHBDB-21271 get transaction V2 by transactionStartIndex ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionEndIndex", "2");

        OBErrorResponse1 obErrorResponse1 = this.accountApi.accountTransactionsV2(alphaTestUser,
                obReadAccount6.getData().getAccount().get(0).getAccountId(), queryParams, OBErrorResponse1.class,HttpStatus.BAD_REQUEST);
        assertEquals(obErrorResponse1.getMessage(), "Invalid transaction range");

        DONE();
    }


    @Tag("Transactions")
    @Order(3)
    @Test
    void get_statement_transactionV2_details_test_bad_request_2() {
        TEST("AHBDB-21271 get transaction V2 by transactionStartIndex ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionStartIndex", "-1");
        queryParams.put("transactionEndIndex", "2");

        OBErrorResponse1 obErrorResponse1 = this.accountApi.accountTransactionsV2(alphaTestUser,
                obReadAccount6.getData().getAccount().get(0).getAccountId(), queryParams, OBErrorResponse1.class,HttpStatus.BAD_REQUEST);
        assertEquals(obErrorResponse1.getMessage(), "Invalid transaction range");

        DONE();
    }

    @Tag("Transactions")
    @Order(4)
    @Test
    void get_statement_transactionV2_details_test_bad_request_3() {
        TEST("AHBDB-21271 get transaction V2 by transactionStartIndex ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionStartIndex", "1");
        queryParams.put("transactionEndIndex", "-2");

        OBErrorResponse1 obErrorResponse1 = this.accountApi.accountTransactionsV2(alphaTestUser,
                obReadAccount6.getData().getAccount().get(0).getAccountId(), queryParams, OBErrorResponse1.class, HttpStatus.BAD_REQUEST);
        assertEquals(obErrorResponse1.getMessage(), "Invalid transaction range");

        DONE();
    }


    @Tag("Transactions")
    @Order(5)
    @Test
    void get_statement_transactionV2_with_relationshipID_details_test_success() throws Throwable {
        TEST("AHBDB-21271 get transaction V2 by relationshipid and accountid using index ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionStartIndex", "1");
        queryParams.put("transactionEndIndex", "2");

        obReadRelationship1 = this.relationshipApi.getRelationships(alphaTestUser);

        OBReadTransaction6 obReadTransaction6 = this.relationshipApi
                .getTransactionsOnRelationshipId(alphaTestUser, obReadRelationship1.getData().getRelationships()
                                .stream()
                                .reduce((first, second) -> second)
                                .get().getConnectionId().toString()
                        , "017300106001", queryParams, OBReadTransaction6.class, HttpStatus.OK);

        // assert transaction count equal to 2
        assertEquals(obReadTransaction6.getData().getTransaction().size(), 2);

        DONE();
    }

    @Tag("Transactions")
    @Order(6)
    @Test
    void get_statement_transactionV2_with_relationshipID_details_test_bad_request() {
        TEST("AHBDB-21271 get transaction V2 by relationshipid and accountid using index ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionEndIndex", "2");

        OBErrorResponse1 obErrorResponse1 = this.relationshipApi
                .getTransactionsOnRelationshipId(alphaTestUser, obReadRelationship1.getData().getRelationships()
                                .stream()
                                .reduce((first, second) -> second)
                                .get().getConnectionId().toString()
                        , "017300106001", queryParams, OBErrorResponse1.class, HttpStatus.BAD_REQUEST);
        assertEquals(obErrorResponse1.getMessage(), "Invalid transaction range");

        DONE();
    }

    @Tag("Transactions")
    @Order(7)
    @Test
    void get_statement_transactionV2_with_relationshipID_details_test_bad_request_2() {
        TEST("AHBDB-21271 get transaction V2 by relationshipid and accountid using index ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionStartIndex", "1");

        OBErrorResponse1 obErrorResponse1 = this.relationshipApi
                .getTransactionsOnRelationshipId(alphaTestUser, obReadRelationship1.getData().getRelationships()
                                .stream()
                                .reduce((first, second) -> second)
                                .get().getConnectionId().toString()
                        , "017300106001", queryParams, OBErrorResponse1.class, HttpStatus.BAD_REQUEST);
        assertEquals(obErrorResponse1.getMessage(), "Invalid transaction range");

        DONE();
    }

    @Tag("Transactions")
    @Order(8)
    @Test
    void get_statement_transactionV2_with_relationshipID_details_test_bad_request_3() {
        TEST("AHBDB-21271 get transaction V2 by relationshipid and accountid using index ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionStartIndex", "-1");
        queryParams.put("transactionEndIndex", "2");

        OBErrorResponse1 obErrorResponse1 = this.relationshipApi
                .getTransactionsOnRelationshipId(alphaTestUser, obReadRelationship1.getData().getRelationships()
                                .stream()
                                .reduce((first, second) -> second)
                                .get().getConnectionId().toString()
                        , "017300106001", queryParams, OBErrorResponse1.class, HttpStatus.BAD_REQUEST);
        assertEquals(obErrorResponse1.getMessage(), "Invalid transaction range");

        DONE();
    }

    @Tag("Transactions")
    @Order(9)
    @Test
    void get_statement_transactionV2_with_relationshipID_details_test_bad_request_4() {
        TEST("AHBDB-21271 get transaction V2 by relationshipid and accountid using index ");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("transactionStartIndex", "1");
        queryParams.put("transactionEndIndex", "-2");

        OBErrorResponse1 obErrorResponse1 = this.relationshipApi
                .getTransactionsOnRelationshipId(alphaTestUser, obReadRelationship1.getData().getRelationships()
                                .stream()
                                .reduce((first, second) -> second)
                                .get().getConnectionId().toString()
                        , "017300106001", queryParams, OBErrorResponse1.class, HttpStatus.BAD_REQUEST);
        assertEquals(obErrorResponse1.getMessage(), "Invalid transaction range");

        DONE();
    }
}
