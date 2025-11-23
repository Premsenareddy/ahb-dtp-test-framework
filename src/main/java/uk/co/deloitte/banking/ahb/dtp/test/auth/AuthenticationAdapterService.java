package uk.co.deloitte.banking.ahb.dtp.test.auth;

import lombok.AllArgsConstructor;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.client.DeviceProtectedClientV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.client.UserProtectedClientV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@AllArgsConstructor
@Deprecated
public class AuthenticationAdapterService {

    @Inject
    public DeviceProtectedClientV2 deviceProtectedClientV2;

    @Inject
    public AuthConfiguration authConfiguration;

    @Inject
    public UserProtectedClientV2 userProtectedClientV2;

}
