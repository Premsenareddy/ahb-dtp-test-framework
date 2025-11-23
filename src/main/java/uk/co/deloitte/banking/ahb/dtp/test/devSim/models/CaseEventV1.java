package uk.co.deloitte.banking.ahb.dtp.test.devSim.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.co.deloitte.banking.http.kafka.BaseEvent;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CaseEventV1 {

    @NotNull
    @JsonProperty("Metadata")
    private BaseEvent metadata;
    @JsonProperty("EventData")
    @NotNull
    @Valid
    private CaseEventRequest eventData;
}
