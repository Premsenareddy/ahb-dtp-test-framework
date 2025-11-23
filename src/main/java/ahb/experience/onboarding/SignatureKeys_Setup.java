package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
        "publicKey",
        "privateKey"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignatureKeys_Setup {

        @JsonProperty("publicKey")
        public String publicKey;
        @JsonProperty("privateKey")
        public String privateKey;
}
