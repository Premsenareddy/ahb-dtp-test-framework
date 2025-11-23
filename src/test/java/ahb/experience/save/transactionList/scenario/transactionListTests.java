package ahb.experience.save.transactionList.scenario;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin_Para;
import ahb.experience.save.transactionList.TransactionListResponseSchema;
import ahb.experience.save.transactionList.api.transactionListParentApi;
import ahb.experience.save.transactionList.api.transactionListKidsApi;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@MicronautTest
@Slf4j
@Singleton
public class transactionListTests {

    String deviceId = "6de6bb9f-5998-46a8-1646779035396-b481-7b56f82aada0";
    String mobileNumber = "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
    String passcode = "+971584444820";
    String x_jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVFDSUErbnNYMzJ3UGU2MG1kOHR4UzFHSzlkVUxqeGV4Sm9XSkxyd3pmdnNVdm9BaUJNY3E3TG15a2FMSzU1Rjk1a2RIMmxVMlJNRVNSTlU2RXY5SFwvT0FEZEt3Zz09In0.ga7yIiEEhE1ImjktewluBroRyW84wp7gfutVpVPJXQLtaI3V_yCwPLN6zoOuR46D8_d3gyMpBBCtywyXByuXrQ";


    String accountNumber = "014444820001";

    String relationshipId = "c072848e-3da0-ec11-b400-002248cb87b1";

    @Inject
    bankingUserLogin_Para bankingUserLoginPara;

    @Inject
    transactionListParentApi transactionListParentApi;

    @Inject
    transactionListKidsApi transactionListKidsApi;


    @Test
    public void transactionListParentApi() throws JSONException {
        TEST("Test to Validate the Transaction List of the Parent");
        GIVEN("I have a valid test user with phone number, device Id and passcode");

        WHEN(" I try to login with that user");
        String bearerToken = bankingUserLoginPara.getAccessToken_Common(deviceId,passcode,mobileNumber,x_jwsSignature);

        AND("And I am trying to validate if the Transaction List is retrieved");
        TransactionListResponseSchema schemaValidation = transactionListParentApi.transactionList_parentApi(bearerToken, accountNumber);

        THEN("Validating the response of the code");
        Assertions.assertNotNull(schemaValidation.getData().getTransactions(), "Transactions are not Listed in the response");

        DONE();
    }


    @Test
    public void transactionListKidsApi() throws JSONException {
        TEST("Test to Validate the Transaction List of the Kid");
        GIVEN("I have a valid test user with phone number, device Id and passcode");

        WHEN(" I try to login with the Parent user");
        String bearerToken = bankingUserLoginPara.getAccessToken_Common(deviceId,passcode,mobileNumber,x_jwsSignature);

        AND("And I am trying to validate if the Transaction List is retrieved");
        TransactionListResponseSchema schemaValidation = transactionListKidsApi.transactionList_kidApi(bearerToken, accountNumber, relationshipId);

        THEN("Validating the response of the code");
        Assertions.assertNotNull(schemaValidation.getData().getTransactions(), "Transactions are not Listed in the response");

        DONE();
    }
}

