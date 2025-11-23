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
public class UserLoginRequestV2 {

    @NotBlank
    @Schema(name = "Password", description = "Password that goes with the userId.")
    @JsonProperty("Password")
    private String password;

    @Schema(name = "UserId", description = "User id")
    @JsonProperty("UserId")
    private String userId;



    @Schema(name = "PhoneNumber", description = "Phone number ")
    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @Schema(name = "Email", description = "Email address")
    @JsonProperty("Email")
    private String email;


}
