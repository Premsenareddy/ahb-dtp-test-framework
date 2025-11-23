package ahb.experience.onboarding;
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
public class ExperienceFATCADetails {

        @JsonProperty("usCitizenOrResident")
        @Builder.Default
        public boolean usCitizenOrResident = false;

        @JsonProperty("birthCity")
        @Builder.Default
        public String birthCity = "Ajman";

        @JsonProperty("birthCountry")
        @Builder.Default
        public String birthCountry = "AE";
}
