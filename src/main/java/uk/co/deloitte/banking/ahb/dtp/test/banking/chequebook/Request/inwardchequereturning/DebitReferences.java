package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardchequereturning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
class DebitReferences {
        @JsonProperty("DebitReference")
        public String debitReference;
    }

