package ahb.experience.creditCard.ExpApplicationService;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetails{
    @JsonProperty
    public String productCode = "P123";
    @JsonProperty
    public String productType = "CREDIT_CARD";
    @JsonProperty
    public String productName = "Etihad";
}
