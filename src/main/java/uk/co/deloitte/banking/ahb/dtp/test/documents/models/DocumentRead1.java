package uk.co.deloitte.banking.ahb.dtp.test.documents.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.deloitte.banking.customer.api.openbanking.v3.Meta;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentRead1 {

    @JsonProperty("Data")
    @Schema(
            name = "Data"
    )
    private DocumentRead1Data data;
    @JsonProperty("Meta")
    @Schema(
            name = "Meta",
            description = "Pagination not yet implemented"
    )
    private Meta meta;

}
