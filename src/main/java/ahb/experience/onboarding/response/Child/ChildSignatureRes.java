package ahb.experience.onboarding.response.Child;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChildSignatureRes {

    @JsonProperty("payload")
    public String childPayload;
    @JsonProperty("signature")
    public String childXJWSSignature;
}
