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
public class CBAdaptorResponseDataError {

    @JsonProperty("Message")
    public String message;
    @JsonProperty("Path")
    public String path;
}
