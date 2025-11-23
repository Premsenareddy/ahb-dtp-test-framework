package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * QueryHeaderAudit
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class QueryHeaderAudit {
    @JsonProperty("T24_time")
    private Integer t24Time;

    @JsonProperty("versionNumber")
    private String versionNumber;

    @JsonProperty("parse_time")
    private Integer parseTime;

    public QueryHeaderAudit t24Time(Integer t24Time) {
        this.t24Time = t24Time;
        return this;
    }

}

