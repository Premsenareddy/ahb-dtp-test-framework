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
@Introspected
@Builder
public class DeviceRegistrationRequestV2 {

    @Size(min = 4, max = 50)
    @NotBlank
    @Schema(name = "DeviceId", description = "Unique Id given to each device.")
    @JsonProperty("DeviceId")
    private String deviceId;

    @Size(min = 8, max = 128)
    @NotBlank
    @Schema(name = "DeviceHash", description = "Unique hash code for each device.")
    @JsonProperty("DeviceHash")
    private String deviceHash;

}
