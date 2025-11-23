package uk.co.deloitte.banking.customer.residentialaddress.api;

import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;

import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;

public class ResidentialAddressApi {

    @Inject
    CustomerConfig customerConfig;

    public JSONObject userProfileJSON(JSONObject address, AlphaTestUser alphaTestUser) throws JSONException {

        JSONObject data = new JSONObject();
        JSONObject profile = new JSONObject();

        profile.put("DateOfBirth", LocalDate.of(1996, 8, 12));
        profile.put("MobileNumber", alphaTestUser.generateUserTelephone());
        profile.put("PreferredName", alphaTestUser.getName());
        profile.put("Language", alphaTestUser.getLanguage());
//        profile.put("Email", generateRandomEmail());
//        profile.put("EmailState", OBWriteEmailState1.VERIFIED);
        profile.put("TermsVersion", LocalDate.of(2020, 12, 12));
        profile.put("TermsAccepted", Boolean.TRUE);
        profile.put("Address", address);

        data.put("Data", profile);
        return data;
    }

    public JSONObject userProfileJSONRemoveField(JSONObject address, AlphaTestUser alphaTestUser, String toRemove) throws JSONException {

        JSONObject data = new JSONObject();
        JSONObject profile = new JSONObject();

        profile.put("DateOfBirth", LocalDate.of(1996, 8, 12));
        profile.put("MobileNumber", alphaTestUser.generateUserTelephone());
        profile.put("PreferredName", alphaTestUser.getName());
        profile.put("Language", alphaTestUser.getLanguage());
//        profile.put("Email", generateRandomEmail());
//        profile.put("EmailState", OBWriteEmailState1.VERIFIED);
        profile.put("TermsVersion", LocalDate.of(2020, 12, 12));
        profile.put("TermsAccepted", Boolean.TRUE);
        profile.put("Address", address);
        profile.remove(toRemove);

        data.put("Data", profile);
        return data;
    }

    public JSONObject userProfileJSON(JSONObject address, AlphaTestUser alphaTestUser, String changing, String value) throws JSONException {

        JSONObject data = new JSONObject();
        JSONObject profile = new JSONObject();

        profile.put("DateOfBirth", LocalDate.of(1996, 8, 12));
        profile.put("MobileNumber", alphaTestUser.generateUserTelephone());
        profile.put("PreferredName", alphaTestUser.getName());
        profile.put("Language", alphaTestUser.getLanguage());
//        profile.put("Email", generateRandomEmail());
//        profile.put("EmailState", OBWriteEmailState1.VERIFIED);
        profile.put("TermsVersion", LocalDate.of(2020, 12, 12));
        profile.put("TermsAccepted", Boolean.TRUE);
        profile.put("Address", address);
        profile.put(changing, value);

        data.put("Data", profile);
        return data;
    }

    public JSONObject createAddressJSON(String line, String value) throws JSONException {
        return new JSONObject() {
            {
                put(line, value);
            }
        };
    }

    public JSONObject createAddressJSON(String line, List value) throws JSONException {
        return new JSONObject() {
            {
                put(line, value);
            }
        };
    }

    public JSONObject createAddressJSON(String buildingNumber, String buildingNumberValue, String country, String countryValue, String countrySubDivision, String countrySubDivisionValue) {
        return new JSONObject() {
            {
                put(buildingNumber, buildingNumberValue);
                put(country, countryValue);
                put(countrySubDivision, countrySubDivisionValue);
            }
        };
    }
}
