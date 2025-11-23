package ahb.experience.creditCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.ArrayList;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class ExperienceGetCreditCard {

    @JsonProperty("content")
    public ArrayList<ExpGetCreditCardContent> content;

    @JsonProperty("page")
    public ExpGetCreditCardPage page;
}
