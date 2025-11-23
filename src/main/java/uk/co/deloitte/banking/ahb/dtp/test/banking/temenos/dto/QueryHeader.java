package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.Objects;

/**
 * QueryHeader
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QueryHeader {
    @JsonProperty("audit")
    private QueryHeaderAudit audit;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("page_start")
    private Integer pageStart;

    @JsonProperty("total_size")
    private Integer totalSize;

    @JsonProperty("page_token")
    private String pageToken;

    public QueryHeader audit(QueryHeaderAudit audit) {
        this.audit = audit;
        return this;
    }

}

