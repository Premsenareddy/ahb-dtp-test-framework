package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
public class ValidateResetPasswordRequest {
    @JsonProperty("Otp")
    @NotBlank
    @Schema(name = "Otp", description = "OTP code received")
    private String otp;

    @JsonProperty("Hash")
    @NotBlank
    @Schema(name = "Hash", description = "Uniquer identifier of the request")
    private String hash;
}
