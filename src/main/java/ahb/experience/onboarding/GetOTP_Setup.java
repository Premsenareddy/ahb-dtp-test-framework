package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Password",
        "Destination",
        "Type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOTP_Setup {

        @JsonProperty("Password")
        public String password;
        @JsonProperty("Destination")
        public String destination;
        @JsonProperty("Type")
        public String type;
}
