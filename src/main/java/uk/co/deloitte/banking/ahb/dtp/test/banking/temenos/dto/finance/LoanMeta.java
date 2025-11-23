package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoanMeta{
    @JsonProperty("PageSize")
    public int pageSize;
    @JsonProperty("PageStart")
    public int pageStart;
    @JsonProperty("PageToken")
    public String pageToken;
    @JsonProperty("T24time")
    public int t24time;
}