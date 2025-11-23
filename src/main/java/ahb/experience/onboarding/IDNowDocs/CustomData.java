package ahb.experience.onboarding.IDNowDocs;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomData {

        @JsonProperty("custom3")
        private Object custom3;
        @JsonProperty("custom4")
        private Object custom4;
        @JsonProperty("custom1")
        private Object custom1;
        @JsonProperty("custom2")
        private Object custom2;
        @JsonProperty("custom5")
        private Object custom5;
        @JsonProperty("transactionNumber")
        private Object transactionNumber = "dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777";
}
