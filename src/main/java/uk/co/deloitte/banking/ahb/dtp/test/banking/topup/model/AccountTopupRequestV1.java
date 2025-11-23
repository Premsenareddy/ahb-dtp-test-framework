package uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTopupRequestV1 {

    @JsonProperty("Data")
    private AccountTopupRequestDataV1 accountTopupRequestData;
}
