package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
@Deprecated
public class UserRegistrationRequest {

  @Size(min = 4, max = 8)
  @NotEmpty(message = "Passcode is empty or too short")
  @Schema(description = "Passcode for the user that goes with the registration request")
  private String passcode;

  @NotEmpty(message = "Telephone number is missing or invalid")
  @Schema(description = "Phone number for the user that goes with the registration request. Should be in UAE format (+555501234567")
  private String phoneNumber;
}
