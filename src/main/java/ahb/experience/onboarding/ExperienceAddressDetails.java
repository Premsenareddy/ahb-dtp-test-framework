package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@ToString
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceAddressDetails {

        @JsonProperty("buildingName")
        @Builder.Default
        public String buildingName= "Chamsin center";

        @JsonProperty("villaNameNumber")
        @Builder.Default
        public String villaNameNumber= "12";

        @JsonProperty("buildingNumber")
        @Builder.Default
        public String buildingNumber= "12";

        @JsonProperty("street")
        @Builder.Default
        public String street= "Hamad2";

        @JsonProperty("addressLine")
        @Builder.Default
        public List<String> addressLine = Arrays.asList("AL karama Street");

        @JsonProperty("city")
        @Builder.Default
        public String city= "Dubai";

        @JsonProperty("emirate")
        @Builder.Default
        public String emirate="Dubai";

        @JsonProperty("country")
        @Builder.Default
        public String country = "AE";

        @JsonProperty("officeNumber")
        public String officeNumber;
        @JsonProperty("poBox")

        public String poBox;
        @JsonProperty("area")
        public String area;
}
