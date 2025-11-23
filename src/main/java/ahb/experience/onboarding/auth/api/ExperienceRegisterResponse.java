package ahb.experience.onboarding.auth.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Introspected
public class ExperienceRegisterResponse {
    @JsonProperty("accessToken")
    @Schema(name = "accessToken")
    public String accessToken;
    @JsonProperty("refreshToken")
    @Schema(name = "refreshToken")
    public String refreshToken;
    public String scope;
    public String tokenType;
    public int expiresIn;
    @JsonProperty("userId")
    @Schema(name = "userId")
    public String userId;
    public List<Object> entitlements;
}

