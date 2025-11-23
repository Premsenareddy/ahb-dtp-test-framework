package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FinanceResponse {
    @JsonProperty("ObReadLoanDetails2")
    public ArrayList<ObReadLoanDetails2> obReadLoanDetails2;
}
