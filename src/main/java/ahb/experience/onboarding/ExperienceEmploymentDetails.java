package ahb.experience.onboarding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import javax.inject.Singleton;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceEmploymentDetails {

        @JsonProperty("employmentStatus")
        @Builder.Default
        public String employmentStatus= "EMPLOYED";

        @JsonProperty("companyName")
        @Builder.Default
        public String companyName = "LITTLE ME";

        @JsonProperty("employerCode")
        @Builder.Default
        public String employerCode = "800971";

        @JsonProperty("monthlyIncome")
        @Builder.Default
        public float monthlyIncome = 10000;

        @JsonProperty("incomeSource")
        @Builder.Default
        public String incomeSource= "OTHERS";

        @JsonProperty("businessCode")
        @Builder.Default
        public String businessCode= "TXG";

        @JsonProperty("designationLapsCode")
        @Builder.Default
        public String designationLapsCode="9";

        @JsonProperty("professionCode")
        @Builder.Default
        public String professionCode= "25";

        @JsonProperty("otherSourceOfIncome")
        @Builder.Default
        public String otherSourceOfIncome= "My Income is my income none of your income";
}
