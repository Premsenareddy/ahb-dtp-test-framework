package ahb.experience.InApp.request;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InAppReqBody {

        @JsonProperty("customerId")
        @Builder.Default
        private String customerId = "{{customerId}}";

        @JsonProperty("rating")
        @Builder.Default
        private String rating ="5";

        @JsonProperty("ratingType")
        @Builder.Default
        private String ratingType = "Promoter 123";

        @JsonProperty("comment")
        @Builder.Default
        private String comment = ":)";
}
