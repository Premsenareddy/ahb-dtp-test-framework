package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordResponse {

    @JsonProperty("Hash")
    @Schema(name = "Hash", example = "Unique identifier for reset password")
    private String hash;
}
