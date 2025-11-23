package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionResponseAudit {
    @JsonProperty("T24_time")
    private Integer t24Time;
    @JsonProperty("parse_time")
    private Integer parseTime;
    @JsonProperty("responseParse_time")
    private Integer responseParseTime;
    @JsonProperty("requestParse_time")
    private Integer requestParseTime;
    @JsonProperty("versionNumber")
    private String versionNumber;
}
