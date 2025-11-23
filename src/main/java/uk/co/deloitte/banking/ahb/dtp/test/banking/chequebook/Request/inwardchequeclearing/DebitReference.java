package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequeclearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DebitReference {
        @JsonProperty("DebitReference")
//        @Builder.Default
        public String debitReference;
    }

