package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class RegistrationResponseV2 {

    @JsonProperty("Type")
    @Schema(name = "Type", description = "Type of registration response.")
    private String type;

    @JsonProperty("Tag")
    @Schema(name = "Tag", description = "Registration response tag.")
    private String tag;

    @JsonProperty("Status")
    @Schema(name = "Status", description = "Status of the registration response.", example = "Success", allowableValues = "Success")
    private Status status;

}
