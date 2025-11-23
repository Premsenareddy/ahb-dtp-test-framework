package uk.co.deloitte.banking.customer.authentication.scenarios;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DeviceStatusUser {
    private DeviceStatus status;
    private String id;

}
