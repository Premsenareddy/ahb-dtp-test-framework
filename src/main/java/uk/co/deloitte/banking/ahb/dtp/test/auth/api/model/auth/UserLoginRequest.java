package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

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
public class UserLoginRequest {

    @Size(min = 4, max = 50)
    @Schema(description = "User's unique Id.")
    private String userId;

    @Size(min = 8, max = 100)
    @NotBlank
    @Schema(description = "Password that goes with the user.")
    private String password;

    @Schema(description = "Phone number ")
    @NotBlank
    private String phoneNumber;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Device id")
    private String deviceId;
}
