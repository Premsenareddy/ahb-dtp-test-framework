package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantExtractedDTO {
    @JsonProperty("IdentificationProcess")
    private Map<String, Object> identificationProcess;
    @JsonProperty("CustomData")
    private Map<String, Object> customData;
    @JsonProperty("ContactData")
    private Map<String, Object> contactData;
    @JsonProperty("UserData")
    private Map<String, Object> userData;
    @JsonProperty("IdentificationDocument")
    private Map<String, Object> identificationDocument;
    @JsonProperty("Attachments")
    private Map<String, Object> attachments;
}
