package ahb.experience.borrow.creditCards.scenario;

import ahb.experience.borrow.creditCards.api.CardService;
import ahb.experience.creditCard.ExpCardTransaction;
import ahb.experience.creditCard.ExpCardTransactionContent;
import ahb.experience.creditCard.ExperienceGetCreditCard;
import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin_Para;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@MicronautTest
@Slf4j
@Singleton
public class CreditCardSummary {

    @Inject
    bankingUserLogin_Para bankingUserLogin;

    @Inject
    CardService cardService;

    String jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFEWEpyUUQ1bHd1d3llcVA1Vm54dUd0dW91cUxPQlwvemZzVGpjaHhjXC8zbmZnSWdGYXdFM25iSjdaTkdVWUVOaUhyYnpOQXZxeE5Gc216bTNcL3o4UjlZUEYxST0ifQ.KFhknWoNNKntI8eukr1I64ZnQRy8qaCy2IHYYAuhwE0PSZVkvjd2qCGYwT62WIOYE_jYjK8UTXSYme8qbOOllw";
    String deviceId = "4A7B2099-AE76-45F9-B8B3-371E5LQ731";
    String mobileNumber = "+971508711437";
    String passcode = "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
    String bearerToken = "";

    @Order(2)
    @Test
    public void getAllTransactionForCard() {

        TEST("Get transaction for card without filter");
        GIVEN("User has a valid test user with phone number, device Id and passcode and Credit card linked");
        WHEN("User login with that user");
        bearerToken = bankingUserLogin.getAccessToken_Common(deviceId, mobileNumber, passcode, jwsSignature);
        THEN("User fetches its credit card");
        ExperienceGetCreditCard card = cardService.getCards(bearerToken, "CREDIT", 200);
        String cardID = card.content.get(0).getCardNo().substring(0,6).concat(card.content.get(0).getCardNo().substring(12,16));
        AND("USER fetches transaction on credit card");
        ExpCardTransaction transactions = cardService.getCardTransaction(bearerToken, cardID, 200);
        for (ExpCardTransactionContent transContent : transactions.content){
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.transactionBillingStatus), "Transaction billing status is not displayed");
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.currency), "Transaction currency status is not displayed");
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.type), "Transaction type status is not displayed");
        }
    }

    @Order(3)
    @Test
    public void getAllDebitTransaction() {

        Map<String, String> pram = new HashMap<>();
        pram.put("type", "DEBIT");

        TEST("Get transaction for card without filter");
        GIVEN("User has a valid test user with phone number, device Id and passcode and Credit card linked");
        WHEN("User login with that user");
        bearerToken = bankingUserLogin.getAccessToken_Common(deviceId, mobileNumber, passcode, jwsSignature);
        THEN("User fetches its credit card");
        ExperienceGetCreditCard card = cardService.getCards(bearerToken, "CREDIT", 200);
        String cardID = card.content.get(0).getCardNo().substring(0,6).concat(card.content.get(0).getCardNo().substring(12,16));
        AND("USER fetches transaction on credit card");
        ExpCardTransaction transactions = cardService.getCardTransaction(bearerToken, cardID, pram, 200);
        for (ExpCardTransactionContent transContent : transactions.content){
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.transactionBillingStatus), "Transaction billing status is not displayed");
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.currency), "Transaction currency status is not displayed");
            Assertions.assertTrue(transContent.type.equals("DEBIT"));
        }
    }

    @Order(4)
    @Test
    public void getAllCreditTransaction() {

        Map<String, String> pram = new HashMap<>();
        pram.put("type", "CREDIT");

        TEST("Get transaction for card without filter");
        GIVEN("User has a valid test user with phone number, device Id and passcode and Credit card linked");
        WHEN("User login with that user");
        bearerToken = bankingUserLogin.getAccessToken_Common(deviceId, mobileNumber, passcode, jwsSignature);
        THEN("User fetches its credit card");
        ExperienceGetCreditCard card = cardService.getCards(bearerToken, "CREDIT", 200);
        String cardID = card.content.get(0).getCardNo().substring(0,6).concat(card.content.get(0).getCardNo().substring(12,16));
        AND("USER fetches transaction on credit card");
        ExpCardTransaction transactions = cardService.getCardTransaction(bearerToken, cardID,pram, 200);
        for (ExpCardTransactionContent transContent : transactions.content){
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.transactionBillingStatus), "Transaction billing status is not displayed");
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.currency), "Transaction currency status is not displayed");
            Assertions.assertTrue(transContent.type.equals("CREDIT"));
        }
    }

    @Order(1)
    @Test
    public void getCardTransactionAllFilter() {

        Map<String, String> pram = new HashMap<>();
        pram.put("fromDate", "2022-02-05");
        pram.put("minAmount", "4.50");
        pram.put("maxAmount", "30");
        pram.put("toDate", "2022-03-15");
        pram.put("duration", "LAST_THREE_MONTHS");
        pram.put("type", "DEBIT");

        TEST("Get transaction for card without filter");
        GIVEN("User has a valid test user with phone number, device Id and passcode and Credit card linked");
        WHEN("User login with that user");
        bearerToken = bankingUserLogin.getAccessToken_Common(deviceId, mobileNumber, passcode, jwsSignature);
        THEN("User fetches its credit card");
        ExperienceGetCreditCard card = cardService.getCards(bearerToken, "CREDIT", 200);
        String cardID = card.content.get(0).getCardNo().substring(0,6).concat(card.content.get(0).getCardNo().substring(12,16));
        AND("USER fetches transaction on credit card");
        ExpCardTransaction transactions = cardService.getCardTransaction(bearerToken, cardID, pram, 200);
        Assertions.assertTrue(transactions.content.size() > 0);
        for (ExpCardTransactionContent transContent : transactions.content){
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.transactionBillingStatus), "Transaction billing status is not displayed");
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.currency), "Transaction currency status is not displayed");
            Assertions.assertTrue(StringUtils.isNotBlank(transContent.type), "Transaction type status is not displayed");
            Assertions.assertTrue(transContent.type.equals("DEBIT")  , "Transaction type status is not displayed");
            Assertions.assertTrue(transContent.amount >= 4.50, "Transaction amount less than min value.");
            Assertions.assertTrue(transContent.amount <= 30.00, "Transaction amount more than max value.");

        }
    }
}
