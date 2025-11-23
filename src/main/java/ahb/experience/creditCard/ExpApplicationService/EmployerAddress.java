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
public class EmployerAddress{
    @JsonProperty("country")
    public String country = "UAE";
    @JsonProperty("city")
    public String city = "Abu Dhabi";
    @JsonProperty("address")
    public String address = "Al Bahr Tower";
    @JsonProperty("phoneCountryCode")
    public String phoneCountryCode = "+971";
    @JsonProperty("phoneNumber")
    public String phoneNumber = "508711111";
    @JsonProperty("addressType")
    public String addressType = "OFFICE";
}
