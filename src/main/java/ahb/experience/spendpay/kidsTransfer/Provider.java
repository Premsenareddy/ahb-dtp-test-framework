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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor

public class Provider {

    @JsonProperty("defaultProvider")
    public Boolean defaultProvider;

    @JsonProperty("providerId")
    public String providerId;

    @JsonProperty("providerName")
    public String providerName;

    @JsonProperty("providerLogo")
    public String providerLogo;

    public List<ServiceType> serviceTypes = null;
}
