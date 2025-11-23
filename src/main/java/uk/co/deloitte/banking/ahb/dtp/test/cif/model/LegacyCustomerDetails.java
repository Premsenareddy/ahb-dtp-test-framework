package uk.co.deloitte.banking.ahb.dtp.test.cif.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LegacyCustomerDetails {
    @JsonProperty("CIFId")
    public String cIFId;
    @JsonProperty("CustomerStatus")
    public String customerStatus;
    @JsonProperty("FirstName")
    public String firstName;
    @JsonProperty("Gender")
    public String gender;
    @JsonProperty("Nationality")
    public String nationality;
    @JsonProperty("PassportNum")
    public String passportNum;
    @JsonProperty("PassportExpiryDate")
    public String passportExpiryDate;
    @JsonProperty("VisaRefNum")
    public String visaRefNum;
    @JsonProperty("VisaExpDate")
    public String visaExpDate;
    @JsonProperty("AddressLine1")
    public String addressLine1;
    @JsonProperty("AddressLine2")
    public String addressLine2;
    @JsonProperty("City")
    public String city;
    @JsonProperty("Country")
    public String country;
    @JsonProperty("Zip")
    public String zip;
    @JsonProperty("MobilePhone")
    public String mobilePhone;
    @JsonProperty("EmailId")
    public String emailId;
    @JsonProperty("EmployerName")
    public String employerName;
    @JsonProperty("JoiningDate")
    public String joiningDate;
    @JsonProperty("Title")
    public String title;

}
