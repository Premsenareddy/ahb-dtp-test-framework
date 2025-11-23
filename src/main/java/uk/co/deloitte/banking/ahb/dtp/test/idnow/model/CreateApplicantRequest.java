package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

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
public class CreateApplicantRequest {

    /**
     * TODO - This class appears to be  a duplicate of DeviceRegistrationRequest class
     */
    @NotBlank
    @Schema(description = "First name")
    @JsonProperty("FirstName")
    private String firstName;

    @NotBlank
    @Schema(description = "Last name")
    @JsonProperty("LastName")
    private String lastName;

}