package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
//@AllArgsConstructor
public class Audit {
    @JsonProperty("T24_time")
    public int t24_time;
    @JsonProperty("ResponseParse_time")
    public int responseParse_time;
    @JsonProperty("RequestParse_time")
    public int requestParse_time;
    @JsonProperty("VersionNumber")
    public String versionNumber;
}
