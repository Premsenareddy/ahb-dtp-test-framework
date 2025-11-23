package ahb.experience.creditCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class ExpGetCreditCardPage {

    @JsonProperty("totalElements")
    public int totalElements;
    @JsonProperty("currentPage")
    public int currentPage;
    @JsonProperty("totalPages")
    public int totalPages;
}
