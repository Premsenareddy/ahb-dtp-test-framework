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
public class IncomeDetail{
    @JsonProperty
    public String monthlyIncome = "15000";
    @JsonProperty
    public String incomeType = "CREDIT_CARD";
}