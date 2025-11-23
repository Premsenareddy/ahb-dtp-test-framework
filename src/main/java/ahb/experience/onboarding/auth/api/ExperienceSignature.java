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
public class ExperienceSignature {
    @JsonProperty("payload")
    @Schema(name = "payload")
    private String payload;
    @JsonProperty("signature")
    @Schema(name = "signature")
    private String signature;
}
