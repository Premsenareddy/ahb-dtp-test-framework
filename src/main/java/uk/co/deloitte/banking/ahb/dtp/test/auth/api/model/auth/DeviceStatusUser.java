package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DeviceStatusUser {
    private DeviceStatus status;
    private String id;

}
