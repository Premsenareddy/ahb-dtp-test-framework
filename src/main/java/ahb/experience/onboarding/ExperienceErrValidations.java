package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceErrValidations {

        @JsonProperty("code")
        public String code;
        @JsonProperty("message")
        public String message;
        @JsonProperty("errors")
        public Errors errors;
        @JsonProperty("status")
        public String status;
        @JsonProperty("timestamp")
        public String timestamp;

}
