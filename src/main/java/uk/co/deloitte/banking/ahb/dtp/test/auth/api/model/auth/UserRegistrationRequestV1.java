package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
public class UserRegistrationRequestV1 {


    @JsonProperty("Password")
    @NotBlank(message = "Password is empty")
    @Schema(description = "Password for the user that goes with the registration request")
    private String password;

    @JsonProperty("PhoneNumber")
    @NotEmpty(message = "Telephone number is missing or invalid")
    @Schema(description = "Phone number for the user that goes with the registration request. Should be in UAE format (+555501234567")
    private String phoneNumber;
}
