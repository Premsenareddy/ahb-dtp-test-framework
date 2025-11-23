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
public class EmploymentDetails{

    @JsonProperty
    public String employerName = "Al Hilal Bank";
    @JsonProperty
    public String employerEmirate = "Abu Dhabi";
    @JsonProperty
    public EmployerAddress employerAddress;
    @JsonProperty
    public String employmentStartDate = "01/2021";
}