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
public class ObReadLoanDetails2 {
    @JsonProperty("ObreadLoanDetail2")
    public ObreadLoanDetail2 obreadLoanDetail2;
    @JsonProperty("LoanMeta")
    public LoanMeta loanMeta;
}
