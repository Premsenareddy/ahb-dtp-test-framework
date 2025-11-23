package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequeclearing;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Data"
})
@Generated("jsonschema2pojo")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutwardChequeBookClearingRequest {

    @JsonProperty("Data")
    private Data data;

    @JsonProperty("Data")
    public Data getData() {
        return data;
    }

    @JsonProperty("Data")
    public void setData(Data data) {
        this.data = data;
    }
}