package uk.co.deloitte.banking.ahb.dtp.test.aml.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerBlacklistResponseDTO {
    @JsonProperty("Result")
    private String result;
    @JsonProperty("ReferenceNumber")
    private String referenceNumber;
    @JsonProperty("DetectionId")
    private String detectionId;
    @JsonProperty("ReturnCode")
    private String returnCode;
    @JsonProperty("Timestamp")
    private OffsetDateTime timestamp;

}
