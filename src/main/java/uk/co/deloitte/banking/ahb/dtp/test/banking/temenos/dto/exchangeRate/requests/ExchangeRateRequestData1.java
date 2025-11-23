package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.exchangeRate.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ExchangeRateRequestData1 {
    @Schema(name = "ReferenceNum", description = "the unique reference number for DTP requests", required = true)
    @NotBlank
    @JsonProperty("ReferenceNum")
    private String referenceNum;

    @Schema(name = "DebitAccountCurrency", description = "the debit account currency", required = true)
    @NotBlank
    @JsonProperty("DebitAccountCurrency")
    private String debitAccountCurrency;

    @Schema(name = "TransactionCurrency", description = "the transaction currency", required = true)
    @NotBlank
    @JsonProperty("TransactionCurrency")
    private String transactionCurrency;

    @Schema(name = "PaymentType", description = "the payment type", required = true)
    @NotBlank
    @JsonProperty("PaymentType")
    private String paymentType;

    @Schema(name = "CreditAccountCurrency", description = "the Credit Account Currency", required = true)
    @NotBlank
    @JsonProperty("CreditAccountCurrency")
    private String creditAccountCurrency;

    @Schema(name = "ModeOfPayment", description = "the Mode Of Payment", required = true)
    @NotNull
    @JsonProperty("ModeOfPayment")
    private String modeOfPayment;

    @Schema(name = "SpecialSpread", description = "the special spread")
    @JsonProperty("SpecialSpread")
    private String specialSpread;
}
