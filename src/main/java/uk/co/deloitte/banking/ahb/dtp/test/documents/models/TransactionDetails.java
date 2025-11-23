package uk.co.deloitte.banking.ahb.dtp.test.documents.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetails {

    @NotBlank
    @JsonProperty("BookingDate")
    private String bookingDate;
    @JsonProperty("NarrativeOne")
    private String narrativeOne;
    @JsonProperty("NarrativeTwo")
    private String narrativeTwo;
    @JsonProperty("NarrativeThree")
    private String narrativeThree;
    @NotBlank
    @JsonProperty("ValueDate")
    private String valueDate;
    @JsonProperty("MoneyOut")
    private String moneyOut;
    @JsonProperty("MoneyIn")
    private String moneyIn;
    @NotBlank
    @JsonProperty("Balance")
    private String balance;
}
