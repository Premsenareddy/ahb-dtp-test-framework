package ahb.experience.onboarding.TaxDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.ArrayList;

@Data
@ToString
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaxCountries {

        @JsonProperty("haveTaxId")
        @Builder.Default
        private boolean haveTaxId = false;
        @JsonProperty("selectedReason")
        @Builder.Default
        private String selectedReason = "COUNTRY_DOESNT_ISSUE_TIN";
        @JsonProperty("taxCountry")
        @Builder.Default
        private String taxCountry = "IN";
        @JsonProperty("taxId")
        private String taxId;
        @JsonProperty("taxResidencyByInvestmentScheme")
        private boolean taxResidencyByInvestmentScheme;
        @JsonProperty("residentInAnyOtherJurisdiction")
        private boolean residentInAnyOtherJurisdiction;
        @JsonProperty("otherJurisdiction")
        private ArrayList<String> otherJurisdiction;

}
