package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class RegistrationRequest {

    @Size(min = 4, max = 50)
    @Schema(description = "Username of the user creating the registration request.")
    private String username;

    //TODO:: //@Email
    @Schema(description = "Email of the user.")
    private String email;

    @Size(min = 4, max = 50)
    @Schema(description = "User's password..")
    private String userPassword;
    private String sn;

    private List<DeviceProfile> deviceProfiles;
}
