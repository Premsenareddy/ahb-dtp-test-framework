package uk.co.deloitte.banking.payments.beneficiary;

import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBChargeBearerType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.transaction.OBPostalAddress6;
import uk.co.deloitte.banking.account.api.payment.model.domestic.*;
import uk.co.deloitte.banking.account.api.payment.model.international.*;

import java.math.BigDecimal;

@Data
public class PaymentRequestUtils {
    public static OBWriteDomesticConsent4 prepareInternalConsent(final String debtorPrimary,
                                                                 final String debtorScheme,
                                                                 final String creditorPrimary,
                                                                 final String creditorScheme,
                                                                 final BigDecimal amount,
                                                                 final String currency,
                                                                 final String reference,
                                                                 final String unstructured,
                                                                 final String endToEndIdentification) {
        final OBWriteDomestic2DataInitiationRemittanceInformation inf = new OBWriteDomestic2DataInitiationRemittanceInformation();
        final OBWriteDomestic2DataInitiationCreditorAccount creditorAccout = OBWriteDomestic2DataInitiationCreditorAccount.builder()
                .name("creditor")
                .identification(creditorPrimary)
                .schemeName(creditorScheme)
                .build();

        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = OBWriteDomestic2DataInitiationDebtorAccount.builder()
                .name("debtor")
                .identification(debtorPrimary)
                .schemeName(debtorScheme)
                .build();
        inf.setReference(reference);
        inf.setUnstructured(unstructured);
        return OBWriteDomesticConsent4.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteDomesticConsent4Data.builder()
                        .initiation(OBWriteDomestic2DataInitiation.builder()
                                .instructionIdentification("instruction identification")
                                .endToEndIdentification(endToEndIdentification)
                                .creditorAccount(creditorAccout)
                                .debtorAccount(debtorAccount)
                                .remittanceInformation(inf)
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency(currency)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public static OBWriteDomesticConsent4 prepareDomesticConsent(final String debtorPrimary,
                                                                 final String debtorScheme,
                                                                 final String creditorPrimary,
                                                                 final String creditorScheme,
                                                                 final BigDecimal amount,
                                                                 final String currency,
                                                                 final String reference,
                                                                 final String unstructured,
                                                                 final OBChargeBearerType1Code requestedChargeCode,
                                                                 final String endToEndIdentification) {
        final OBWriteDomestic2DataInitiationRemittanceInformation inf = new OBWriteDomestic2DataInitiationRemittanceInformation();
        final OBWriteDomestic2DataInitiationCreditorAccount creditorAccout = OBWriteDomestic2DataInitiationCreditorAccount.builder()
                .identification(creditorPrimary)
                .schemeName(creditorScheme)
                .name("creditor")
                .build();

        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccout = OBWriteDomestic2DataInitiationDebtorAccount.builder()
                .identification(debtorPrimary)
                .schemeName(debtorScheme)
                .name("debtor")
                .build();
        inf.setReference(reference);
        inf.setUnstructured(unstructured);
        return OBWriteDomesticConsent4.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteDomesticConsent4Data.builder()
                        .supplementaryData(OBWriteDomesticConsentResponse5DataSupplementaryData.builder()
                                .charges(RequestedChargeCode.builder().requestedChargeCode(requestedChargeCode).build())
                                .build())
                        .initiation(OBWriteDomestic2DataInitiation.builder()
                                .instructionIdentification("instruction identification")
                                .localInstrument(PaymentScheme.DOMESTIC_OUTGOING.getScheme())
                                .endToEndIdentification(endToEndIdentification)
                                .creditorAccount(creditorAccout)
                                .debtorAccount(debtorAccout)
                                .remittanceInformation(inf)
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency(currency)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public static OBWriteDomesticConsent4 prepareLegacyConsent(final String debtorPrimary,
                                                               final String debtorScheme,
                                                               final String creditorPrimary,
                                                               final String creditorScheme,
                                                               final BigDecimal amount,
                                                               final String currency,
                                                               final String reference,
                                                               final String unstructured,
                                                               final String endToEndIdentification,
                                                               final String instructionIdentification) {
        final OBWriteDomestic2DataInitiationRemittanceInformation inf = new OBWriteDomestic2DataInitiationRemittanceInformation();
        final OBWriteDomestic2DataInitiationCreditorAccount creditorAccount = OBWriteDomestic2DataInitiationCreditorAccount.builder()
                .identification(creditorPrimary)
                .schemeName(creditorScheme)
                .name("creditor name")
                .build();

        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccout = OBWriteDomestic2DataInitiationDebtorAccount.builder()
                .identification(debtorPrimary)
                .schemeName(debtorScheme)
                .name("debtor name")
                .build();
        inf.setReference(reference);
        inf.setUnstructured(unstructured);
        return OBWriteDomesticConsent4.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteDomesticConsent4Data.builder()
                        .initiation(OBWriteDomestic2DataInitiation.builder()
                                .creditorAccount(creditorAccount)
                                .instructionIdentification(instructionIdentification)
                                .endToEndIdentification(endToEndIdentification)
                                .localInstrument(PaymentScheme.DTP_LEGACY.getScheme())
                                .debtorAccount(debtorAccout)
                                .remittanceInformation(inf)
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency(currency)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public static OBWriteDomestic2 prepareInternalTransferRequest(final String consentId,
                                                                  final String debtorPrimary,
                                                                  final String debtorScheme,
                                                                  final String creditorPrimary,
                                                                  final String creditorScheme,
                                                                  final BigDecimal amount,
                                                                  final String reference,
                                                                  final String unstructured,
                                                                  final String endToEndIdentification) {
        final OBWriteDomestic2DataInitiationRemittanceInformation inf = new OBWriteDomestic2DataInitiationRemittanceInformation();
        inf.setReference(reference);
        inf.setUnstructured(unstructured);
        return OBWriteDomestic2.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteDomestic2Data.builder()
                        .consentId(consentId)
                        .initiation(OBWriteDomestic2DataInitiation.builder()
                                .endToEndIdentification(endToEndIdentification)
                                .instructionIdentification(RandomStringUtils.randomAlphabetic(5) + "-instructed identification")
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .name("debtor")
                                        .schemeName(debtorScheme)
                                        .identification(debtorPrimary)
                                        .build())
                                .creditorAccount(OBWriteDomestic2DataInitiationCreditorAccount.builder()
                                        .name("creditor")
                                        .schemeName(creditorScheme)
                                        .identification(creditorPrimary)
                                        .build())
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .remittanceInformation(inf)
                                .build())
                        .build())
                .build();
    }


    public static WriteDomesticPayment1 prepareDomesticTransferRequest(final String consentId,
                                                                       final String debtorPrimary,
                                                                       final String debtorScheme,
                                                                       final String creditorPrimary,
                                                                       final String creditorScheme,
                                                                       final BigDecimal amount,
                                                                       final String reference,
                                                                       final String unstructured,
                                                                       final WriteDomesticPayment1RequestedChargeCodePaymentBearer chargeCode,
                                                                       final String endToEndIdentification
                                                                      ) {
        final OBWriteDomestic2DataInitiationRemittanceInformation inf = new OBWriteDomestic2DataInitiationRemittanceInformation();
        inf.setReference(reference);
        inf.setUnstructured(unstructured);
        return WriteDomesticPayment1.builder()
                .risk(OBRisk1.builder().build())
                .data(WriteDomesticPayment1Data.builder()
                        .consentId(consentId)
                        .initiation(WriteDomesticPayment1DataInitiation.builder()
                                .endToEndIdentification(endToEndIdentification)
                                .instructionIdentification(RandomStringUtils.randomAlphabetic(5) + "-instructed identification")
                                .localInstrument(PaymentScheme.DOMESTIC_OUTGOING.getScheme())
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .name("debtor")
                                        .schemeName(debtorScheme)
                                        .identification(debtorPrimary)
                                        .build())
                                .creditorAccount(OBWriteDomestic2DataInitiationCreditorAccount.builder()
                                        .name("creditor")
                                        .schemeName(creditorScheme)
                                        .identification(creditorPrimary)
                                        .build())
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .remittanceInformation(inf)
                                .supplementaryData(WriteDomesticPayment1DataInitiationSupplementaryData.builder()
                                        .charges(WriteDomesticPayment1RequestedChargeCode.builder()
                                                .requestedChargeCode(chargeCode)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public static OBWriteDomestic2 prepareLegacyRequest(final String consentId,
                                                                             final String debtorPrimary,
                                                                             final String debtorScheme,
                                                                             final String creditorPrimary,
                                                                             final String creditorScheme,
                                                                             final BigDecimal amount,
                                                                             final String reference,
                                                                             final String unstructured,
                                                                             final String endToEndIdentification,
                                                                             final String instructionIdentification) {
        final OBWriteDomestic2DataInitiationRemittanceInformation inf = new OBWriteDomestic2DataInitiationRemittanceInformation();
        inf.setReference(reference);
        inf.setUnstructured(unstructured);
        return OBWriteDomestic2.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteDomestic2Data.builder()
                        .consentId(consentId)
                        .initiation(OBWriteDomestic2DataInitiation.builder()
                                .endToEndIdentification(endToEndIdentification)
                                .instructionIdentification(RandomStringUtils.randomAlphabetic(5) + "-instructed identification")
                                .localInstrument(PaymentScheme.DTP_LEGACY.getScheme())
                                .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
                                        .name("debtor")
                                        .schemeName(debtorScheme)
                                        .identification(debtorPrimary)
                                        .build())
                                .creditorAccount(OBWriteDomestic2DataInitiationCreditorAccount.builder()
                                        .name("creditor")
                                        .schemeName(creditorScheme)
                                        .identification(creditorPrimary)
                                        .build())
                                .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .remittanceInformation(inf)
                                .build())
                        .build())
                .build();
    }

    public static OBWriteInternationalConsent5 prepareInternationalConsent(final String debtorPrimary,
                                                                           final String debtorScheme,
                                                                           final String creditorPrimary,
                                                                           final String creditorScheme,
                                                                           final BigDecimal amount,
                                                                           final BigDecimal amountInAed,
                                                                           final String currency,
                                                                           final String reference,
                                                                           final String unstructured,
                                                                           final OBChargeBearerType1Code requestedChargeCode,
                                                                           final String endToEndIdentification) {

        final OBWriteInternational3DataInitiationCreditorAccount creditorAccount = OBWriteInternational3DataInitiationCreditorAccount.builder()
                .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf(creditorScheme))
                .identification(creditorPrimary)
                .name("Test Creditor Name")
                .secondaryIdentification("test routing code")
                .build();

        final OBWriteInternational3DataInitiationDebtorAccount debtorAccount = OBWriteInternational3DataInitiationDebtorAccount.builder()
                .schemeName(debtorScheme)
                .identification(debtorPrimary)
                .build();


        final OBWriteInternational3DataInitiationRemittanceInformation inf = OBWriteInternational3DataInitiationRemittanceInformation.builder()
                .reference(reference)
                .unstructured(unstructured)
                .build();
        return OBWriteInternationalConsent5.builder()
                .risk(OBRisk1.builder().build())
                .data(OBWriteInternationalConsent5Data.builder()
                        .initiation(OBWriteInternational3DataInitiation.builder()
                                .endToEndIdentification(endToEndIdentification)
                                .creditor(OBWriteInternational3DataInitiationCreditor.builder()
                                        .name("Bhavnesh Gupta")
                                        .postalAddress(OBWriteInternational3DataInitiationPostalAddress.builder()
                                                .addressLine1("line 1")
                                                .addressLine2("line 2")
                                                .addressLine3("line 3")
                                                .country("IN")
                                                .build())
                                        .build())
                                .creditorAgent(OBWriteInternational3DataInitiationCreditorAgent.builder()
                                        .identification("CHASUS33")
                                        .postalAddress(OBPostalAddress6.builder()
                                                .country("IN")
                                                .build())
                                        .build())
                                .creditorAccount(creditorAccount)
                                .debtorAccount(debtorAccount)
                                .remittanceInformation(inf)
                                .chargeBearer(requestedChargeCode)
                                .instructedAmount(OBWriteInternational3DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency(currency).build())
                                .supplementaryData(OBWriteInternational3DataInitiationSupplementaryData.builder()
                                        .debitCurrency("AED")
                                        .orgRefNumber("12345678980")
                                        .beneficiaryNickName("Bhav")
                                        .amountInAed(amountInAed)
                                        .build())
                                .build())
                        .build())
                .build();

    }

    public static OBWriteInternational3 prepareInternationalTransferRequest(final String consentId,
                                                                  final String debtorPrimary,
                                                                  final String debtorScheme,
                                                                  final String creditorPrimary,
                                                                  final String creditorScheme,
                                                                  final BigDecimal amount,
                                                                  final BigDecimal amountInAed,
                                                                  final String reference,
                                                                  final String unstructured,
                                                                  final OBChargeBearerType1Code chargeCode,
                                                                  final String endToEndIdentification) {
        return OBWriteInternational3.builder()
                .data(OBWriteInternational3Data.builder()
                        .consentId(consentId)
                        .initiation(OBWriteInternational3DataInitiation.builder()
                                .endToEndIdentification(endToEndIdentification)
                                .creditor(OBWriteInternational3DataInitiationCreditor.builder()
                                        .name("Bhavnesh Gupta")
                                        .postalAddress(OBWriteInternational3DataInitiationPostalAddress.builder()
                                                .addressLine1("line 1")
                                                .addressLine2("line 2")
                                                .addressLine3("line 3")
                                                .country("IN")
                                                .build())
                                        .build())
                                .remittanceInformation(OBWriteInternational3DataInitiationRemittanceInformation.builder()
                                        .unstructured(unstructured)
                                        .reference(reference)
                                        .build())
                                .debtorAccount(OBWriteInternational3DataInitiationDebtorAccount.builder()
                                        .schemeName(debtorScheme)
                                        .identification(debtorPrimary)
                                        .build())
                                .creditorAccount(OBWriteInternational3DataInitiationCreditorAccount.builder()
                                        .schemeName(OBWriteInternational3DataInitiationCreditorAccount.SchemeNameEnum.valueOf(creditorScheme))
                                        .identification(creditorPrimary)
                                        .name("Test Creditor Name")
                                        .secondaryIdentification("test routing code")
                                        .build())
                                .instructedAmount(OBWriteInternational3DataInitiationInstructedAmount.builder()
                                        .amount(amount)
                                        .currency("AED")
                                        .build())
                                .creditorAgent(OBWriteInternational3DataInitiationCreditorAgent.builder()
                                        .identification("CHASUS33")
                                        .name("Test Bank name")
                                        .postalAddress(OBPostalAddress6.builder()
                                                .country("IN")
                                                .build())
                                        .build())
                                .supplementaryData(OBWriteInternational3DataInitiationSupplementaryData.builder()
                                        .debitCurrency("AED")
                                        .amountInAed(amountInAed)
                                        .build())
                                .chargeBearer(chargeCode)
                                .build())
                        .build())
                .build();
    }

}
