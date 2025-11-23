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
public class UserRelationshipWriteRequest {

    @Schema(name = "Password", description = "Password that goes with the userId.")
    @JsonProperty("Password")
    @Size(min= 8)
    @NotBlank
    private String tempPassword;
}
