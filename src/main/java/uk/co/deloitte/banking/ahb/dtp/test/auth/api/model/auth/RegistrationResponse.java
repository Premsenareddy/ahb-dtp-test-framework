package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class RegistrationResponse {

    @Schema(description = "Type of registration response.")
    private String type;

    @Schema(description = "Registration response tag.")
    private String tag;

    @Schema(description = "Status of the registration response.", example = "Success", allowableValues = "Success")
    private Status status;

}
