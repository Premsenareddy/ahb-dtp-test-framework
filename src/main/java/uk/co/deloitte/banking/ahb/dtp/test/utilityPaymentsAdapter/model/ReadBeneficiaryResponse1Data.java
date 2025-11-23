package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadBeneficiaryResponse1Data {


    @JsonProperty("Beneficiaries")
    List<BeneficiaryResponse1Data> beneficiaryList;

}
