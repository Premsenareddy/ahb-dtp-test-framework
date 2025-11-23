package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Introspected
public class UserLoginResponseV2 {

    @JsonProperty("AccessToken")
    @Schema(name = "AccessToken")
    private String accessToken;


    @JsonProperty("RefreshToken")
    @Schema(name = "RefreshToken")
    private String refreshToken;
    @JsonProperty("Scope")
    @Schema(name = "Scope")
    private String scope;

    @JsonProperty("TokenType")
    @Schema(name = "TokenType")
    private String tokenType;

    @JsonProperty("ExpiresIn")
    @Schema(name = "ExpiresIn")
    private String expiresIn;

    @JsonProperty("UserId")
    @Schema(name = "UserId")
    private String userId;

}
