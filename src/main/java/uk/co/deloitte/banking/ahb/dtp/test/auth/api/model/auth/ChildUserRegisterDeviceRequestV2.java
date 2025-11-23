package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
public class ChildUserRegisterDeviceRequestV2 {
    private String password;
    private String childId;
    private String otp;
}
