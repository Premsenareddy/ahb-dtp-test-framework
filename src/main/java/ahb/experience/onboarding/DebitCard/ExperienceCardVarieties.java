package ahb.experience.onboarding.DebitCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceCardVarieties {

        @JsonProperty("productCode")
        private String productCode;
        @JsonProperty("cardImageUrl")
        private String cardImageUrl;
        @JsonProperty("selectedImageUrl")
        private String selectedImageUrl;
        @JsonProperty("unSelectedImageUrl")
        private String unSelectedImageUrl;
}
