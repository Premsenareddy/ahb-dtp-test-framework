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
public class UpdateForgottenPasswordRequestV1 {


    @JsonProperty("UserPassword")
    @Schema(name = "UserPassword")
    @NotBlank
    @Size(min = 8)
    private String userPassword;


    @JsonProperty("Hash")
    @Schema(name = "Hash")
    @NotBlank
    private String hash;
}
