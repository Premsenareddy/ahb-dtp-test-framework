package ahb.experience.onboarding.IDNowDocs;
import ahb.experience.onboarding.IDNowDocs.IDDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.ArrayList;

@Data
@ToString
@Builder
@Getter
@Setter
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceIDNowDetails {

    @JsonProperty("status")
    public Object status;
    @JsonProperty("iDDetails")
    public ArrayList<IDDetails> idDetails;


}
