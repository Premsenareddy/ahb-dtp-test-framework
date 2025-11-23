package ahb.experience.onboarding.auth.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceKeys {
    @JsonProperty("publicKey")
    @Schema(name = "publicKey")
    private String publicKey;
    @JsonProperty("privateKey")
    @Schema(name = "privateKey")
    private String privateKey;
}
