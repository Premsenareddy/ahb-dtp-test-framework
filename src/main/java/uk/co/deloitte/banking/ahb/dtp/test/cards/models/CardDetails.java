package uk.co.deloitte.banking.ahb.dtp.test.cards.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CardDetails {

        private String CardName;
        private String CardNumber;
        private String CardNumberFlag;
        private String CardType;
        private String CardSubType;
        private String CardNetwork;
        private String Currency;
        private String CurrentOutstandingAmount;
        private String CreditsTotalAmount;
        private String TotalCreditLimit;
        private String TotalCashLimit;
        private String AvailableCreditLimit;
        private String LastPaymentAmount;
        private String NextStatementDate;
        private String MinimumDueAmount;
        private String ServiceChargeAmount;
        private String PaymentsAndCreditsAmount;
        private String PrimarySupplementaryFlag;
        private String PrimaryCardNumberFlag;
        private String DeliveryCardFlag;
        private String ActivationFlag;
        private String StatusCode;
        private String ExpiryDate;
        private String TermDueDate;
        private String LastStatementDate;
}
