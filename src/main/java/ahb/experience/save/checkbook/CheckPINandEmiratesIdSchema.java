package ahb.experience.save.checkbook;


import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "pinValidationPassed",
        "eIdValidationPassed"
})
@Generated("jsonschema2pojo")
public class CheckPINandEmiratesIdSchema {

    @JsonProperty("pinValidationPassed")
    private Boolean pinValidationPassed;
    @JsonProperty("eIdValidationPassed")
    private Boolean eIdValidationPassed;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("pinValidationPassed")
    public Boolean getPinValidationPassed() {
        return pinValidationPassed;
    }

    @JsonProperty("pinValidationPassed")
    public void setPinValidationPassed(Boolean pinValidationPassed) {
        this.pinValidationPassed = pinValidationPassed;
    }

    @JsonProperty("eIdValidationPassed")
    public Boolean geteIdValidationPassed() {
        return eIdValidationPassed;
    }

    @JsonProperty("eIdValidationPassed")
    public void seteIdValidationPassed(Boolean eIdValidationPassed) {
        this.eIdValidationPassed = eIdValidationPassed;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}