package uk.co.deloitte.banking.ahb.dtp.test.documents.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMetadataResponse {

    @JsonProperty("Result")
    private boolean result;

    public boolean getResult() {
        return this.result;
    }
}
