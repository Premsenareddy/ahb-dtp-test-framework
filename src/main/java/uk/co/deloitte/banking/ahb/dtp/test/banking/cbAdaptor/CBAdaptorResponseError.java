package uk.co.deloitte.banking.ahb.dtp.test.banking.cbAdaptor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CBAdaptorResponseError {

    @JsonProperty("Code")
    public String code;

    @JsonProperty("Message")
    public String message;

    @JsonProperty("Errors")
    public List<Error> errors;

    @JsonProperty("TraceId")
    public String traceId;
}
