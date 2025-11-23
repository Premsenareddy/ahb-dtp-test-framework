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
        "Data"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCustomerState {

        @JsonProperty("Data")
        public ExperienceProfileDetails Data;

}
