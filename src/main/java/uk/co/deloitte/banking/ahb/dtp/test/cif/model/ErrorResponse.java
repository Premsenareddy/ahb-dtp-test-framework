package uk.co.deloitte.banking.ahb.dtp.test.cif.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    @JsonProperty("Code")
    public String Code;
    @JsonProperty("Message")
    public String Message;
}
