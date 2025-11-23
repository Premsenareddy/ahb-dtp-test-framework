package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @JsonProperty("identificationprocess")
    private IdentificationProcess identificationprocess;
    @JsonProperty("identificationdocument")
    private IdentificationDocument identificationdocument;
    @JsonProperty("attachments")
    private Attachments attachments;
    @JsonProperty("userdata")
    private Map<String, Object> userdata;
    @JsonProperty("contactdata")
    private Map<String, Object> contactdata;
    @JsonProperty("customdata")
    private Map<String, Object> customdata;
}
