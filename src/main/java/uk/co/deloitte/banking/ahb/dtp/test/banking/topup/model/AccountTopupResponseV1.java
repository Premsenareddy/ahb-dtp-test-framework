package uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountTopupResponseV1 {

    @JsonProperty("Data")
    private AccountTopupResponseDataV1 accountTopupResponseData;
}
