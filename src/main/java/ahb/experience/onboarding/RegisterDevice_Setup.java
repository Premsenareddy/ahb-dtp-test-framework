package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.List;

@Data
@ToString
@Builder
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDevice_Setup {

        @JsonProperty("accessToken")
        public String accessToken;
        @JsonProperty("refreshToken")
        public String refreshToken;
        @JsonProperty("scope")
        public String scope;
        @JsonProperty("tokenType")
        public String tokenType;
        @JsonProperty("expiresIn")
        public Integer expiresIn;
        @JsonProperty("userId")
        public String userId;
        @JsonProperty("entitlements")
        public List<Object> entitlements = null;
}
