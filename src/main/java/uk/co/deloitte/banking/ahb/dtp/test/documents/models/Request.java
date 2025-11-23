package uk.co.deloitte.banking.ahb.dtp.test.documents.models;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.deloitte.banking.http.kafka.BaseEvent;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
@JsonNaming
public class Request {
    @NotNull
    private BaseEvent metadata;
    @NotBlank
    private String templateId;
    @NotBlank
    private String customerName;
    @NotBlank
    private String customerId;
    @NotBlank
    private String origin;
    @NotNull
    private Map<String, Object> fieldMappings;

    private Map<String, String> documentMetadata;

}
