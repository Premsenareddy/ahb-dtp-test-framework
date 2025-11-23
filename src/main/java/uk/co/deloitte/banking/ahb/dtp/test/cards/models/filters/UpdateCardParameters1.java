package uk.co.deloitte.banking.ahb.dtp.test.cards.models.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class UpdateCardParameters1 {

    @JsonProperty("CardNumber")
    @Schema(name = "CardNumber", required = true)
    private String cardNumber;

    @JsonProperty("CardNumberFlag")
    @Schema(name = "CardNumberFlag", required = true)
    private String cardNumberFlag;

    @JsonProperty("InternetUsage")
    @Schema(name = "InternetUsage")
    private boolean internetUsage;

    @JsonProperty("NationalUsage")
    @Schema(name = "NationalUsage")
    private boolean nationalUsage;

    @JsonProperty("NationalPOS")
    @Schema(name = "NationalPOS")
    private boolean nationalPOS;

    @JsonProperty("NationalATM")
    @Schema(name = "NationalATM")
    private boolean nationalDisATM;

    @JsonProperty("NationalSwitch")
    @Schema(name = "NationalSwitch")
    private boolean nationalSwitch;

    @JsonProperty("InternationalUsage")
    @Schema(name = "InternationalUsage")
    private boolean internationalUsage;

    @JsonProperty("OperationReason")
    @Schema(name = "OperationReason", required = true)
    private String operationReason;

}



