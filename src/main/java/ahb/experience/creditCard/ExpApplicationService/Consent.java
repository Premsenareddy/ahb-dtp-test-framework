package ahb.experience.creditCard.ExpApplicationService;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Consent{
    @JsonProperty("isAccepted")
    public boolean isAccepted = true;
    @JsonProperty("consentType")
    public String consentType = "MURABAHA";
}