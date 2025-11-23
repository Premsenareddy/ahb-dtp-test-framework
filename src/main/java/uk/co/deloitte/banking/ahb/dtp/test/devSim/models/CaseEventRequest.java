package uk.co.deloitte.banking.ahb.dtp.test.devSim.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseEventRequest {

    @NotNull
    @JsonProperty("CaseType")
    private CaseTypeEnum caseType;
    @JsonProperty("Title")
    private String title;
    @JsonProperty("Process")
    private ProcessOriginEnum processOrigin;
    @JsonProperty("Reason")
    private ReasonEnum reason;
    @JsonProperty("Priority")
    private String priority;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("CreatedOn")
    private String createdOn;
    @JsonProperty("ClosedOn")
    private String closedOn;
    @JsonProperty("StatusCode")
    private String statusCode;
    @JsonProperty("StateCode")
    private String stateCode;
    @JsonProperty("AdditionalDetails")
    private String additionalDetails;
    @NotNull
    @JsonProperty("ResponsibleContactID")
    private String responsibleContactID;
    @NotNull
    @JsonProperty("CustomerId")
    private String customerId;
}
