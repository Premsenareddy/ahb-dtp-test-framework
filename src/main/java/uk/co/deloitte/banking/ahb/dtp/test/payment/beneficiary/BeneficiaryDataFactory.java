package uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary;

import groovy.lang.Singleton;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;

import javax.inject.Inject;


@Singleton
public class BeneficiaryDataFactory {


    @Inject
    EnvUtils envUtils;

    @Inject
    TemenosConfig temenosConfig;

    public BeneficiaryData createBeneficiaryData(){
        final BeneficiaryData beneficiaryData = new BeneficiaryData();
        beneficiaryData.setAccountNumber(temenosConfig.getCreditorAccountId());
        return beneficiaryData;
    }
}
