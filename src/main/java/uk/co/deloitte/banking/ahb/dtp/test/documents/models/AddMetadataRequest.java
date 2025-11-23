package uk.co.deloitte.banking.ahb.dtp.test.documents.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@Introspected
public class AddMetadataRequest {

    @JsonProperty("Metadata")
    @NotEmpty
    private Map<String, String> metadata;

}
