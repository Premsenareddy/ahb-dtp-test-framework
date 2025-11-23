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
@Introspected
@Builder
public class DeviceRegistrationRequest {

    @Size(min = 4, max = 50)
    @NotBlank
    @Schema(description = "Unique Id given to each device.")
    private String deviceId;

    @Size(min = 8, max = 128)
    @NotBlank
    @Schema(description = "Unique hash code for each device.")
    private String deviceHash;

}
