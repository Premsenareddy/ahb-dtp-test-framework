package ahb.experience.onboarding.IDNowDocs;
import ahb.experience.onboarding.StatusValue_Object;
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
public class IdentificationDocument {
    @JsonProperty("country")
    private StatusValue_Object country;
    @JsonProperty("number")
    private StatusValue_Object number;
    @JsonProperty("type")
    private StatusValue_Object type;
    @JsonProperty("validUntil")
    private StatusValue_Object validUntil;
    @JsonProperty("dateIssued")
    private StatusValue_Object DateIssued;
    @JsonProperty("IssuedBy")
    private Object IssuedBy;

}
