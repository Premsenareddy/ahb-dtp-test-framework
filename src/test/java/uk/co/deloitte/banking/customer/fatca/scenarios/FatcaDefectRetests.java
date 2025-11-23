package uk.co.deloitte.banking.customer.fatca.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.customer.api.customer.model.fatca.*;
import uk.co.deloitte.banking.customer.fatca.api.FatcaApiV2;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomIntegerInRange;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FatcaDefectRetests {

    private AlphaTestUser alphaTestUser;
    private AlphaTestUser alphaTestUser1;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private FatcaApiV2 fatcaApiV2;

    @Inject
    private EnvUtils envUtils;

    private void setupTestUser() {

        envUtils.ignoreTestInEnv(Environments.NFT);
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser);
        }
    }

    private void setupTestUserFresh() {
        envUtils.ignoreTestInEnv(Environments.NFT);
        this.alphaTestUser1 = new AlphaTestUser();
        this.alphaTestUser1 = new AlphaTestUser();
        this.alphaTestUser1 = this.alphaTestUserFactory.setupCustomer(this.alphaTestUser1);
    }

    @Test
    public void updateDetailsCheck() {
        TEST("AHBDB-5438: DEFECT - FATCA API not updating details v2");
        GIVEN("I have saved the Fatca details with FORM=W8, UsCitizenOrResident=false, SSN=null");
        setupTestUserFresh();

        OBWriteFatca1 body = OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .form(OBFatcaForm1.W8)
                        .usCitizenOrResident(Boolean.FALSE)
                        .federalTaxClassification("Individual/sole proprietor or single-member LLC")
                        .build()
                ).build();

        this.fatcaApiV2.createFatcaDetails(this.alphaTestUser1, body);

        OBReadFatca1 getFatcaResponse = this.fatcaApiV2.getFatcaDetails(this.alphaTestUser1);
        assertNotNull(getFatcaResponse);

        WHEN("I update the customer details with everything identical but FORM=W9");
        OBWriteFatca1 updatedBody = OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .form(OBFatcaForm1.W9)
                        .usCitizenOrResident(Boolean.FALSE)
                        .federalTaxClassification("Individual/sole proprietor or single-member LLC")
                        .build()
                ).build();

        this.fatcaApiV2.createFatcaDetails(this.alphaTestUser1, updatedBody);

        THEN("The FATCA details should have the form updated to W9");
        OBReadFatca1 response = this.fatcaApiV2.getFatcaDetails(this.alphaTestUser1);

        String form = updatedBody.getData().getForm().toString();
        String checkResidency = body.getData().getUsCitizenOrResident().toString();

        assertEquals(form, response.getData().getForm().toString(),
                "Form not as expected, expected:" + form);
        assertSame(checkResidency, response.getData().getUsCitizenOrResident().toString(),
                "Expected: " + checkResidency);
        assertNull(response.getData().getSsn(), "Expected SSN to be null as it was not sent in Fatca POST");
        assertNotNull(response.getData().getFederalTaxClassification());
    }

    @Test
    @Order(2)
    public void fatca_with_missing_ssn_201_response() {
        TEST("AHBDB-8062: DEFECT - FATCA API - Not working with SSN as null");
        TEST("AHBDB-8227: Positive Test - Post - Missing Optional Parameters - 201 response");
        TEST("AHBDB-8058: Select 'No' for US passport - should allow SSN to be NULL");
        TEST("AHBDB-7739-2: Saving details returns 500 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create FATCA");
        AND("A customer is not a resident/citizen of the US and hence has no SSN");

        OBWriteFatca1 body = OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .form(OBFatcaForm1.W8)
                        .usCitizenOrResident(Boolean.FALSE)
                        .federalTaxClassification("Individual/sole proprietor or single-member LLC")
                        .build()
                ).build();

        WHEN("I pass the FATCA details with missing SSN");
        this.fatcaApiV2.createFatcaDetails(this.alphaTestUser, body);
        THEN("The response should be 201 Created");
        OBReadFatca1 response = this.fatcaApiV2.getFatcaDetails(this.alphaTestUser);

        String form = body.getData().getForm().toString();
        String checkResidency = body.getData().getUsCitizenOrResident().toString();

        assertEquals(form, response.getData().getForm().toString(),
                "Form not as expected, expected:" + form);
        assertSame(checkResidency, response.getData().getUsCitizenOrResident().toString(),
                "Expected: " + checkResidency);
        assertNull(response.getData().getSsn(),
                "Expected SSN to be null as it was not sent in Fatca POST");
        assertNotNull(response.getData().getFederalTaxClassification());
    }

    @Test
    @Order(3)
    public void fatca_with_missing_federalTaxClassification_201_response() {
        TEST("AHBDB-8062: DEFECT - FATCA API - Not working with SSN as null");
        TEST("AHBDB-8227: Positive Test - Post - Missing Optional Parameters - 201 response");
        setupTestUserFresh();
        GIVEN("We have received a request from the client to create FATCA");

        String ssn = generateRandomIntegerInRange(100000000, 999999999).toString();

        OBWriteFatca1 body = OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .form(OBFatcaForm1.W8)
                        .usCitizenOrResident(Boolean.FALSE)
                        .ssn(ssn)
                        .build()
                ).build();

        WHEN("We pass the FATCA details with FederalTaxClassification=null");
        THEN("The response should be 201 Created");
        this.fatcaApiV2.createFatcaDetails(this.alphaTestUser1, body);

        AND("The details are as expected");
        OBReadFatca1 response = this.fatcaApiV2.getFatcaDetails(this.alphaTestUser1);
        String form = body.getData().getForm().toString();
        String checkResidency = body.getData().getUsCitizenOrResident().toString();

        assertEquals(form, response.getData().getForm().toString(),
                "Form not as expected, expected:" + form);
        assertSame(checkResidency, response.getData().getUsCitizenOrResident().toString(),
                "Expected: " + checkResidency);
        assertEquals(ssn, response.getData().getSsn(),
                "Expected SSN to be null as it was not sent in Fatca POST");
        assertNull(response.getData().getFederalTaxClassification(),
                "Expected TaxClassification to be null as it was not sent in Fatca POST");

    }

    @Test
    @Order(1)
    public void fatca_with_missing_optional_parameters_201_response() {
        TEST("AHBDB-8062: DEFECT - FATCA API - Not working with SSN as null");
        TEST("AHBDB-8227: Positive Test - Post - Missing Optional Parameters - 201 response");
        setupTestUser();
        GIVEN("We have received a request from the client to create FATCA");
        AND("A customer is not a resident/citizen of the US and hence has no SSN");
        OBWriteFatca1 body = OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .form(OBFatcaForm1.W8)
                        .usCitizenOrResident(Boolean.FALSE)
                        .build()
                ).build();

        WHEN("We pass the FATCA details with missing taxClassification and SSN");
        THEN("The response should be 201 Created");
        this.fatcaApiV2.createFatcaDetails(this.alphaTestUser, body);

        AND("The details are as expected");
        OBReadFatca1 response = this.fatcaApiV2.getFatcaDetails(this.alphaTestUser);

        String form = body.getData().getForm().toString();
        String checkResidency = body.getData().getUsCitizenOrResident().toString();

        assertEquals(form, response.getData().getForm().toString(),
                "Form not as expected, expected:" + form);
        assertSame(checkResidency, response.getData().getUsCitizenOrResident().toString(),
                "Expected: " + checkResidency);
        assertNull(response.getData().getSsn());
        assertNull(response.getData().getFederalTaxClassification());
    }

    @Test
    public void save_fatca_twice_201_response() {
        TEST("AHBDB-8059: [Update FATCA] [API] Error occurs by saving fatca details for the second time");
        TEST("AHBDB-8126: Updating FATCA giving error");
        setupTestUserFresh();
        GIVEN("I want to save FATCA details for a customer");

        OBWriteFatca1 body = OBWriteFatca1.builder()
                .data(OBWriteFatcaDetails1.builder()
                        .form(OBFatcaForm1.W9)
                        .usCitizenOrResident(Boolean.TRUE)
                        .ssn("123456789")
                        .federalTaxClassification("Individual/sole proprietor or single-member LLC")
                        .build()
                ).build();

        this.fatcaApiV2.createFatcaDetails(this.alphaTestUser1, body);

        OBReadFatca1 getFatcaResponse = this.fatcaApiV2.getFatcaDetails(this.alphaTestUser1);
        assertNotNull(getFatcaResponse);

        WHEN("I update the customer details again with the same body");

        this.fatcaApiV2.createFatcaDetails(this.alphaTestUser1, body);

        THEN("The client should return a 201 response");
        OBReadFatca1 response = this.fatcaApiV2.getFatcaDetails(this.alphaTestUser1);

        AND("The details should be saved");
        String form = body.getData().getForm().toString();
        String checkResidency = body.getData().getUsCitizenOrResident().toString();

        assertEquals(form, response.getData().getForm().toString(),
                "Form not as expected, expected:" + form);
        assertSame(checkResidency, response.getData().getUsCitizenOrResident().toString(),
                "Expected: " + checkResidency);
        assertNotNull(response.getData().getFederalTaxClassification());
    }
}
