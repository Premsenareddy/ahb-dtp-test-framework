package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Deprecated
public class DeviceProfile {

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("metadata")
    private Map<String, String> metadata;
}
