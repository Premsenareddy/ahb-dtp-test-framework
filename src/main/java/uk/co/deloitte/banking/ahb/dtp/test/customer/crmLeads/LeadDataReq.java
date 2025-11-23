package uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
@JsonNaming
public class LeadDataReq {


    @JsonProperty("Data")
    //public Data data;
    public LeadRequest leadRequest;

}
