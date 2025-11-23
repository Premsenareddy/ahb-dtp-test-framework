package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequeclearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({
        "Data"
})
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InwardChequeBookClearingRequest {
        @JsonProperty("Data")
        public Data data;
}

