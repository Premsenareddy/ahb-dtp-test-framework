package uk.co.deloitte.banking.ahb.dtp.test.cards.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import javax.validation.Valid;
import java.util.List;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class ReadCard1Data {
    @JsonProperty("RequestNumber")
    private String requestNumber;
    @JsonProperty("Card")
    private List<ReadCard1DataCard> readCard1DataCard;
}
