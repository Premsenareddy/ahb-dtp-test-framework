package ahb.experience.onboarding.auth.api;

import ahb.experience.onboarding.ExperienceProfileDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Introspected
public class ExperienceLoginResponse {
    @JsonProperty("token")
    private ExperienceRegisterResponse token;
    @JsonProperty("profile")
    private ExperienceProfileDetails profile;
}

