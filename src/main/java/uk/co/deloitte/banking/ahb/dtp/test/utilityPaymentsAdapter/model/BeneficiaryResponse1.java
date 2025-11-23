package uk.co.deloitte.banking.ahb.dtp.test.utilityPaymentsAdapter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.Meta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeneficiaryResponse1 {

    @JsonProperty("Data")
    private BeneficiaryResponse1Data data;

    @JsonProperty("Meta")
    private Meta meta;
}