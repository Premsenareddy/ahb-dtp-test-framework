package uk.co.deloitte.banking.ahb.dtp.test.payment.kidsTransfer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.List;

@Data
@ToString
@Builder
@Introspected
@AllArgsConstructor
@NoArgsConstructor

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class POP {

    @JsonProperty("pop")
    public PurposeOfPayment[] pop1;


}
