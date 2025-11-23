package ahb.experience.spendpay.kidsTransfer;

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
public class DomesticPaymentPurposeAndCharges {

    @JsonProperty("purposeOfPayment")
    public List<PurposeOfPayment> purposeOfPayment = null;
    @JsonProperty("transferCharges")
    public List<TransferCharge> transferCharges = null;
}
