package uk.co.deloitte.banking.customer.authentication.scenarios;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DeviceStatusResponseV2 {

    @JsonProperty("DeviceStatusUsers")
    private List<DeviceStatusUser> deviceStatusUsers;
}
