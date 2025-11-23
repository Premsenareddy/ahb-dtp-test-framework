package ahb.experience.spendpay.kidsTransfer.scenarios;

import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin;
import ahb.experience.spendpay.kidsTransfer.DomesticPaymentPurposeAndCharges;
import ahb.experience.spendpay.kidsTransfer.api.PaymentPurpose;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.InvalidAgeGroup;
import uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer.PurposeOfPayment;


import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.Assert.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;


@MicronautTest
@Slf4j
@Singleton

public class PurposeOfPaymentTest {

    @Inject
    bankingUserLogin bankingUserLogin;

    @Inject
    PaymentPurpose paymentPurpose;




    @Order(1)
    @Test
    public void verify_payment_purpose_AHB_kids() {
        TEST("AHBDB-18173 - API | Validate 200 response for AHB transfer purpose for kids");
        TEST("AHBDB-13849 ");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the payment purpose API for kid for AHB transfers");

        PurposeOfPayment[] pop = paymentPurpose.paymentPurposeForAHBTransfersKids(bearerToken);
        THEN("I verify that the tye is PURPOSE_OF_PAYMENT_KIDS ");
        //assertNotNull(pop);
        System.out.print("pop is " + pop);
        Assert.assertTrue("Type is not correct", pop.length ==9);
        assertNotNull(pop);
        DONE();
    }


    @Order(2)
    @Test
    public void verify_payment_purpose_AHB_kids_invalid_age_group() {
        TEST("AHBDB-18172 - API| Validate 400 response code and error message if age group is wrong");
        TEST("AHBDB-13849 ");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the payment purpose API for kid for AHB transfers");

        InvalidAgeGroup invalidAgeGroup =paymentPurpose.invalidAgeGroupRequest(bearerToken);
        THEN("I verify that the error message is correct for invalid age group ");
        Assert.assertEquals("Error message is not correct ",invalidAgeGroup.message, "Invalid ageGroup provided");
        Assert.assertEquals("Error code is not correct",invalidAgeGroup.code, "INVALID_AGE_GROUP");

        DONE();
    }


    @Order(3)
    @Test
    public void verify_payment_purpose_domesticTransfers_kids() {
        TEST("AHBDB-13849 ");
        TEST("AHBDB-18171 API | Validate 200 response for domestic transfer purpose API for kids");

        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the payment purpose API for kid for AHB transfers");

        DomesticPaymentPurposeAndCharges pop = paymentPurpose.paymentPurposeForDomesticTransfersKids(bearerToken);
        THEN("I verify that all the purpose codes are in list and response is not null ");
        //assertNotNull(pop);
        Assert.assertTrue("All purpose codes are not in list", pop.purposeOfPayment.size() ==9);
        Assert.assertTrue("All purpose codes are not in list", pop.transferCharges.size() ==3);
        assertNotNull(pop);
        DONE();
    }

    @Order(4)
    @Test
    public void verify_correct_transfer_charge_domesticTransfers_kids() {
        TEST("AHBDB-13849 ");
        TEST("AHBDB-18234 - API | Validate that the value of charges are correct");

        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the payment purpose API for kid for AHB transfers");

        DomesticPaymentPurposeAndCharges pop = paymentPurpose.paymentPurposeForDomesticTransfersTeens(bearerToken);
        THEN("I verify that the values of charges are correct");
        //assertNotNull(pop);

        Assert.assertTrue("Charges are not correct", pop.transferCharges.get(0).transferCharge.toString().equals("1.05"));
        Assert.assertTrue("Charges are not correct", pop.transferCharges.get(1).transferCharge.toString().equals("1.05"));
        Assert.assertTrue("Charges are not correct", pop.transferCharges.get(2).transferCharge.toString().equals("2.10"));
        Assert.assertTrue("Charge code is not correct", pop.transferCharges.get(0).code.equals("SHARED"));
        Assert.assertTrue("Charge code is not correct", pop.transferCharges.get(1).code.equals("BORNE_BY_CREDITOR"));
        Assert.assertTrue("Charge code is not correct", pop.transferCharges.get(2).code.equals("BORNE_BY_DEBTOR"));
        DONE();
    }


    @Order(5)
    @Test
    public void verify_payment_purpose_AHB_teens() {
        TEST("AHBDB-18173 - API | Validate 200 response for AHB transfer purpose for kids");
        TEST("AHBDB-13849 ");
        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the payment purpose API for kid for AHB transfers");

        PurposeOfPayment[] pop = paymentPurpose.paymentPurposeForAHBTransfersTeens(bearerToken);
        THEN("I verify that the tye is PURPOSE_OF_PAYMENT_KIDS ");
        //assertNotNull(pop);
        System.out.print("pop is " + pop);
        Assert.assertTrue("Type is not correct", pop.length ==9);
        assertNotNull(pop);
        DONE();
    }

    @Order(6)
    @Test
    public void verify_payment_purpose_domesticTransfers_teens() {
        TEST("AHBDB-13849 ");
        TEST("AHBDB-18358 - API | Validate 200 response for domestic transfer purpose API for teens");

        GIVEN("I have a valid test user with phone number, device Id and passcode");
        WHEN(" I try to login with that user");
        String bearerToken= bankingUserLogin.getAccessToken();

        AND("I am trying to call the payment purpose API for kid for AHB transfers");

        DomesticPaymentPurposeAndCharges pop = paymentPurpose.paymentPurposeForDomesticTransfersTeens(bearerToken);
        THEN("I verify that the values of charges are correct");
        //assertNotNull(pop);

        Assert.assertTrue("Charges are not correct", pop.transferCharges.get(0).transferCharge.toString().equals("1.05"));
        Assert.assertTrue("Charges are not correct", pop.transferCharges.get(1).transferCharge.toString().equals("1.05"));
        Assert.assertTrue("Charges are not correct", pop.transferCharges.get(2).transferCharge.toString().equals("2.10"));
        Assert.assertTrue("Charge code is not correct", pop.transferCharges.get(0).code.equals("SHARED"));
        Assert.assertTrue("Charge code is not correct", pop.transferCharges.get(1).code.equals("BORNE_BY_CREDITOR"));
        Assert.assertTrue("Charge code is not correct", pop.transferCharges.get(2).code.equals("BORNE_BY_DEBTOR"));
        DONE();
    }
}

