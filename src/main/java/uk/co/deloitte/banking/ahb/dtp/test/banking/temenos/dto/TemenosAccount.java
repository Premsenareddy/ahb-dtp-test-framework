package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TemenosAccount {
    @JsonProperty("arrangementId")
    private String arrangementId;
    @JsonProperty("accountId")
    private String accountId;
    @JsonProperty("availableLimit")
    private String availableLimit;
    @JsonProperty("IBAN")
    private String iBAN;
    @JsonProperty("clearedBalance")
    private String clearedBalance;
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("productGroupId")
    private String productGroupId;
    @JsonProperty("displayName")
    private String displayName;
    @JsonProperty("customerId")
    private String customerId;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("workingBalance")
    private String workingBalance;
    @JsonProperty("onlineActualBalance")
    private String onlineActualBalance;
    @JsonProperty("availableFunds")
    private String availableFunds;
    @JsonProperty("currencyId")
    private String currencyId;
    @JsonProperty("customerName")
    private String customerName;
    @JsonProperty("sortCode")
    private String sortCode;
    @JsonProperty("openingDate")
    private String openingDate;
    @JsonProperty("productName")
    private String productName;
    @JsonProperty("availableBalance")
    private BigDecimal availableBalance;
}
