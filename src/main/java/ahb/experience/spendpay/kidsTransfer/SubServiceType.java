package ahb.experience.spendpay.kidsTransfer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubServiceType {

    @JsonProperty("subServiceTypeId")
    public String subServiceTypeId;

    @JsonProperty("subServiceTypeName")
    public String subServiceTypeName;
}
