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
public class UserRegisterDeviceRequestV1 {

    @NotBlank
    @Schema(name = "Password", description = "Password that goes with the userId.")
    @JsonProperty("Password")
    private String password;

    @Schema(name = "PhoneNumber", description = "Phone number ")
    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @Schema(name = "Email", description = "Email address")
    @JsonProperty("Email")
    private String email;

    @Schema(name = "DeviceId", description = "Device id")
    @JsonProperty("DeviceId")
    private String deviceId;
}
