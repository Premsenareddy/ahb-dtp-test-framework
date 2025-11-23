package uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeadsResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeads.LeadRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
@JsonNaming
public class LeadDataRes {


    @JsonProperty("Data")
    //public Data data;
    public LeadValueRes leadValueRes;

}
