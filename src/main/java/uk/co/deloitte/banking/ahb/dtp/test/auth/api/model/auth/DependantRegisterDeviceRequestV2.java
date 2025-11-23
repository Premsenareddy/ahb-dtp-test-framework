package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
public class DependantRegisterDeviceRequestV2 {
    @Schema(name = "Password", description = "Password that goes with the userId.")
    @JsonProperty("Password")
    @Size(min= 8)
    @NotBlank
    private String password;

    @Schema(name = "UserId", description = "UserId ")
    @JsonProperty("UserId")
    private String userId;


    @Schema(name ="Otp", description = "Otp code sent to parent device")
    @JsonProperty("Otp")
    @NotBlank
    private String otp;
}
