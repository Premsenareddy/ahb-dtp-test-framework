package uk.co.deloitte.banking.ahb.dtp.test.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBIdType;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;

import static uk.co.deloitte.banking.ahb.dtp.test.util.PhoneNumberUtils.sanitize;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@Slf4j
public class AlphaTestUser {

    // Instance fields
    private String deviceId;
    private String deviceHash;

    private String previousDeviceId;
    private String previousDeviceHash;

    private String userId;
    private String userPassword;


    private String userTelephone;

    @Min(40)
    private String userEmail;

    private String accountNumber;
    private String CIFNumber;
    private String relationshipId;
    private String currentAccountNumber;
    private OBGender gender;

    @Deprecated
    private LoginResponse loginResponse;

    private String jwtToken;
    private String scope;
    private String refreshToken;

    private String privateKeyBase64;
    private String publicKeyBase64;

    private String previousPrivateKeyBase64;
    private String previousPublicKeyBase64;

    @Builder.Default
    private boolean isOnboarded = false;

    private String customerId;

    private String name;
    private String language;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateOfBirth;

    private String applicantId;

    private String eid;

    private OBIdType idType;

    /**
     * Init methods set various fields and also creates private / public keys.
     */
    public AlphaTestUser() {
        init();
    }

    public AlphaTestUser(String mobile) {
        init(mobile);
    }

    public void init() {

        this.generateUserTelephone();
        this.deviceHash = UUID.randomUUID().toString();
        this.deviceId = UUID.randomUUID().toString();

        this.eid =  generateRandomEID();
        this.userEmail = generateRandomEmail();
        // At the beginning of onboarding flow, user password is set to be device hash
        this.userPassword = generateRandomSHA512enabledPassword();
        // Preffered name has to be single string without any spaces as per current requirements
        this.name = generateEnglishRandomString(10);
        this.language = "en";
        this.gender = OBGender.MALE;

        this.dateOfBirth = LocalDate.now().minusYears(21);
        log.info("[{}] : age of user ", dateOfBirth);


    }
    public void init(String mobile) {

        this.userTelephone = mobile;
        this.deviceHash = UUID.randomUUID().toString();
        this.deviceId = UUID.randomUUID().toString();

        // At the beginning of onboarding flow, user password is set to be device hash
        this.userPassword = "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
        // Preffered name has to be single string without any spaces as per current requirements
        this.name = generateEnglishRandomString(10);
        this.language = "en";
        this.gender = OBGender.MALE;

        this.dateOfBirth = LocalDate.now().minusYears(21);
        log.info("[{}] : age of user ", dateOfBirth);


    }

    public String generateUserTelephone() {
        final String userTelephone = sanitize(generateRandomMobile());
        this.userTelephone = userTelephone;
        return userTelephone;
    }

    @Override
    public String toString() {
        return "UserId::" + userId;
    }


}
