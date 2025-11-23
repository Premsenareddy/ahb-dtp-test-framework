package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Introspected
@Builder(toBuilder = true)
@Slf4j
public class User {

    private String username;
    private String givenName;
    private String mail;
    private String userPassword;
    private String inetUserStatus;
    private String uid;
    private String sn;
    private String cn;
    private String createTimestamp;
    private String phoneNumber;
    private List<DeviceProfile> deviceProfiles;
}
