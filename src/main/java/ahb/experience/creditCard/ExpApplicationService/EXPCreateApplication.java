package ahb.experience.creditCard.ExpApplicationService;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EXPCreateApplication {

    @JsonProperty("productCategory")
    public String productCategory = "CREDIT_CARD";
    @JsonProperty("applicationReferenceNumber")
    public String applicationReferenceNumber;
    @JsonProperty("personalDetails")
    public PersonalDetails personalDetails;
    @JsonProperty("productDetails")
    public ProductDetails productDetails;
    @JsonProperty("addresses")
    public ArrayList<Address> addresses;
    @JsonProperty("employmentDetails")
    public EmploymentDetails employmentDetails;
    @JsonProperty("incomeDetails")
    public ArrayList<IncomeDetail> incomeDetails;
    @JsonProperty("salaryDetails")
    public SalaryDetails salaryDetails;
    @JsonProperty("consents")
    public ArrayList<Consent> consents;

}
