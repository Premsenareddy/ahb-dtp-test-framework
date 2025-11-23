package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepUpAuthInitiateRequest {
    @NotNull
    @JsonProperty("Weight")
    @Schema(name = "Weight", description = "Step up weight required")
    private Integer weight;

    @NotBlank
    @JsonProperty("Scope")
    @Schema(name = "Scope", description = "Step up scope required")
    private String scope;
}
