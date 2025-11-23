package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.internationalCharges;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InternationalChargesData {

    @JsonProperty("ChargeAmount")
    private BigDecimal chargeAmount;


}
