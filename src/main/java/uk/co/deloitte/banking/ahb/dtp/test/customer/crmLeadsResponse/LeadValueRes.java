package uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeadsResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mapstruct.Mapper;

@Mapper
@Builder
@EqualsAndHashCode
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)


public class LeadValueRes {

    @JsonProperty("CustomerId")
    public String customerId;
    @JsonProperty("LeadId")
    public String leadId;
    @JsonProperty("ProductType")
    public String productType;
}
