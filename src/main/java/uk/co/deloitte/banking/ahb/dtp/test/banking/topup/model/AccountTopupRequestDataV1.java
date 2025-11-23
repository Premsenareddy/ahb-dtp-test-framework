package uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTopupRequestDataV1 {

    @JsonProperty("InstructedAmount")
    private AccountTopupInstructedAmount instructedAmount;

    @JsonProperty("EndToEndIdentification")
    private String endToEndReference;

    @JsonProperty("RemittanceInformation")
    private List<String> remittanceInformation;

}
