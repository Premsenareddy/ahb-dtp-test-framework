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
public class DisableDeviceRequestV2 {

    @NotBlank
    @Schema(name = "CustomerId", description = "Customer Id")
    @JsonProperty("customerId")
    private String customerId;

    @Schema(name = "DeviceId", description = "Device id")
    @NotBlank
    @JsonProperty("deviceId")
    private String deviceId;
}
