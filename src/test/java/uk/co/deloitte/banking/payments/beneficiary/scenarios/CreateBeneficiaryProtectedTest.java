package uk.co.deloitte.banking.payments.beneficiary.scenarios;


import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.co.deloitte.banking.account.api.beneficary.model.OBWriteBeneficiaryResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryData;
import uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary.BeneficiaryDataFactory;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.Environments;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.beneficiary.api.BeneficiaryApiFlows;

import javax.inject.Inject;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateBeneficiaryProtectedTest {

    @Inject
    private BeneficiaryApiFlows beneficiaryApiFlows;

    @Inject
    private BeneficiaryDataFactory beneficiaryDataFactory;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private static final Environments ALL_SKIP_ENV = Environments.NONE;

    private AlphaTestUser alphaTestUser;

    private void setupTestUser() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
            this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
        }
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    public void positive_case_create_beneficiary_protected() {

        TEST("AHBDB-18640 - beneficiary is created with a 201 response");

        setupTestUser();
        BeneficiaryData beneficiaryData = beneficiaryDataFactory.createBeneficiaryData();
        beneficiaryData.setBeneficiaryName("bhavnesh");

        THEN("The client tries to create their beneficiary");

        THEN("They can created their beneficiary");
        OBWriteBeneficiaryResponse1 beneOne = this.beneficiaryApiFlows.createBeneficiaryProtected(alphaTestUser,
                beneficiaryData);
        Assertions.assertEquals(beneOne.getData().getBeneficiary().get(0).getCreditorAccount().getName(), beneficiaryData.getBeneficiaryName());

        DONE();
    }

}
