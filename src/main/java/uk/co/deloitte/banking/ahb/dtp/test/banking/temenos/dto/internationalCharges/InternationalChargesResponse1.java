package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.internationalCharges;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InternationalChargesResponse1 {

    @JsonProperty("Data")
    private InternationalChargesResponse1Data data;
}
