package ahb.experience.onboarding.IDNowDocs;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.List;

@Data
@ToString
@Builder
@Introspected
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentificationProcess {

        @JsonProperty("result")
        private String result = "SUCCESS_DATA_CHANGED";
        @JsonProperty("companyId")
        private String companyId ="alhilalpptest";
        @JsonProperty("fileName")
        private String fileName = "dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777.zip";
        @JsonProperty("AgentName")
        private String AgentName = null;
        @JsonProperty("identificationTime")
        private String identificationTime = "2021-10-26T15:21:31+02:00";
        @JsonProperty("id")
        private String id;
        @JsonProperty("href")
        private String href = "/api/v1/alhilalpptest/identifications/dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777.zip";
        @JsonProperty("type")
        private String type ="APP";
        @JsonProperty("transactionNumber")
        private String transactionNumber = "dc47e427-e381-3d94-ae46-8664cb0b2d0b";
        @JsonProperty("Reason")
        private String Reason = null;

}
