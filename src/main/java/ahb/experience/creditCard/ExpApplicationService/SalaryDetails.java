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
public class SalaryDetails{
    @JsonProperty
    public String iban = "AE121212131314";
    @JsonProperty
    public String bankName = "Mashreq";
    @JsonProperty
    public String amount = "15000";
}