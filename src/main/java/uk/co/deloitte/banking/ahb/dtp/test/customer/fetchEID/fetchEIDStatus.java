package uk.co.deloitte.banking.ahb.dtp.test.customer.fetchEID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mapstruct.Mapper;

@Mapper
@EqualsAndHashCode
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Data"
})

public class fetchEIDStatus {
    @JsonProperty("Data")
    public fetchEIDDetails fetchEIDDetails;
}
