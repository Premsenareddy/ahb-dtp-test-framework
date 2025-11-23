package uk.co.deloitte.banking.payments.transfer.domestic.util;

import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.payment.model.domestic.*;
import uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants;

import java.math.BigDecimal;

import static uk.co.deloitte.banking.payments.transfer.common.SchemeNamesConstants.ACCOUNT_IBAN;

public class DomesticPaymentRequestUtils {

    public static WriteDomesticPayment1 prepareDomesticRequest(final String debtorAccountId,
                                                                       final String creditorIBAN,
                                                                       final String localInstrument,
                                                                       final String instructionIdentification,
                                                                       final BigDecimal amount) {
        return WriteDomesticPayment1.builder()
                .risk(OBRisk1.builder().build())
                .data(WriteDomesticPayment1Data.builder()
                        .consentId("CONSENT")
                        .initiation(WriteDomesticPayment1DataInitiation.builder()
                                .instructionIdentification(instructionIdentification)
                                .endToEndIdentification(RandomStringUtils.randomAlphanumeric(11))
                                .localInstrument(localInstrument)
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .name("DEBTORNAME")
                                        .schemeName(SchemeNamesConstants.ACCOUNT_NUMBER)
                                        .identification(debtorAccountId)
                                        .build())
                                .creditorAccount(OBWriteDomestic2DataInitiationCreditorAccount.builder()
                                        .name("CREDITORNAME")
                                        .schemeName(ACCOUNT_IBAN)
                                        .identification(creditorIBAN)
                                        .build())
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .remittanceInformation(OBWriteDomestic2DataInitiationRemittanceInformation.builder()
                                        .unstructured("TESTUNSTRUCTURED")
                                        .reference("DCP")
                                        .build())
                                .supplementaryData(WriteDomesticPayment1DataInitiationSupplementaryData.builder()
                                        .charges(WriteDomesticPayment1RequestedChargeCode.builder()
                                                .requestedChargeCode(WriteDomesticPayment1RequestedChargeCodePaymentBearer.BORNE_BY_DEBTOR)
                                                .build())
                                        .clearingChannel(WriteDomesticPayment1ClearingChannelCode.builder()
                                                .clearingChannelCode(WriteDomesticPayment1ClearingChannel.PS)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
