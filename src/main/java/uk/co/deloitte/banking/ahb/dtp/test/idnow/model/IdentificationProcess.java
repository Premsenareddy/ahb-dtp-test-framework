package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class IdentificationProcess {

    @JsonProperty("result")
    @Builder.Default
    private String result = "SUCCESS_DATA_CHANGED";
    @JsonProperty("companyid")
    @Builder.Default
    private String companyId = "alhilalpptest";
    @JsonProperty("filename")
    @Builder.Default
    private String filename = "demosecurityfeatures__20190528_154257__061e65c8ad110.zip";
    @JsonProperty("agentname")
    private String agentName;
    @Builder.Default
    @JsonProperty("identificationtime")
    private String identificationTime = "2021-10-26T15:21:31+02:00";
    @JsonProperty("id")
    private String id;
    @JsonProperty("href")
    private String href;
    @JsonProperty("type")
    private String type;
    @JsonProperty("transactionnumber")
    private String transactionNumber;
}
