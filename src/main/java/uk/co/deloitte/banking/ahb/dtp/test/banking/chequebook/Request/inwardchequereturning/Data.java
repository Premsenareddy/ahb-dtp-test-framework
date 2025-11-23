package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequereturning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Data {
        @Builder.Default
        public String debitAcctNo = "011150683001";

        @Builder.Default
        public String debitCurrency = "AED";

        @Builder.Default

        public String debitValueDate = new SimpleDateFormat("yyyyMMdd").format(new Date());;

        @Builder.Default
        public String debitAmount = "1";

        @JsonProperty("DebitReferences")
        public List<DebitReferences> debitReferences = Collections.singletonList(new DebitReferences("chq -14 Inward"));

        @Builder.Default
        public String creditCurrency = "AED";

        @Builder.Default
        public String creditReference = "Inward Txn";

        @Builder.Default
        public String creditValueDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        @Builder.Default
        public String chequeNumber = "1";

        @Builder.Default
        public String bcSortCode = "205320110";

        @Builder.Default
        public String endToEndReference = "I220100000003";

        @Builder.Default
        public String chequeType ="";

}
