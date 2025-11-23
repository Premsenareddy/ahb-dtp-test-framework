package ahb.experience.onboarding.request.misc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CustomerData {

    private String CustomerId =  null;
    private String Dob =  null;
    private String DateOfBirth =  null;
    private String MobileNumber =  null;
    private String Terms =  null;
    private String PreferredName =  null;
    private String FirstName =  null;
    private String LastName =  null;
    private String FullName =  null;
    private String Gender =  null;
    private String Nationality =  null;
    private String CountryOfBirth =  null;
    private String CityOfBirth =  null;
    private String Language =  null;
    private String Email =  null;
    private String EmailState =  null;
    private String Address =  null;
    private String CustomerState =  null;
    private String Cif =  null;
    private String CustomerType =  null;
    private String Status =  null;
    private String StatusReason =  null;
    private String OnboardedBy =  null;
    private String AgeGroup =  null;
}
