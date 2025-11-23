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
public class PersonalDetails{
    @JsonProperty("maritalStatus")
    public String maritalStatus = "S";
    @JsonProperty("academicDegree")
    public String academicDegree = "1";
}