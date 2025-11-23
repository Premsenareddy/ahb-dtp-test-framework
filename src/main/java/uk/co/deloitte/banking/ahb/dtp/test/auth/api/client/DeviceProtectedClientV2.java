package uk.co.deloitte.banking.ahb.dtp.test.auth.api.client;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceRegistrationRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.operation.DeviceProtectedOperationV2;

import javax.validation.Valid;

@Client("${authentication-adapter.path}/protected/v2/devices")
@Header(name = "X-API-KEY", value = "${authentication-adapter.apiKey}")
public interface DeviceProtectedClientV2 extends DeviceProtectedOperationV2 {

    /**
     * Register a device on the platform
     *
     * @param registrationRequest The device id and password to register
     * @param apiKey
     * @return Tokens to allow device access
     */
    @Override
    Single<HttpResponse<LoginResponseV2>> registerDevice(@Valid DeviceRegistrationRequestV2 registrationRequest, String apiKey);


    /**
     * Login with a device id and password
     *
     * @param loginRequest The device id and password to register with
     * @return Tokens that allow device access
     */
    @Override
    Single<HttpResponse<LoginResponseV2>> loginDevice(@Valid DeviceLoginRequest loginRequest);

}
